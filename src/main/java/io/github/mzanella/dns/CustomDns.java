package io.github.mzanella.dns;

import io.github.mzanella.dns.exception.ExceptionUtils;
import io.vavr.control.Try;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.Section;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;
import org.xbill.DNS.lookup.LookupFailedException;
import org.xbill.DNS.lookup.LookupResult;
import org.xbill.DNS.lookup.NoSuchDomainException;
import org.xbill.DNS.lookup.NoSuchRRSetException;
import org.xbill.DNS.lookup.ServerFailedException;

/**
 * Resolver that allows to specify the DNS server to use
 */
class CustomDns implements DnsResolver {

  private final ExtendedResolver resolver;

  private static abstract class ResolverWrapper {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    abstract Resolver internalResolver() throws UnknownHostException;
    public Resolver toResolver(Duration timeout) throws UnknownHostException {
      Resolver r = internalResolver();
      r.setTimeout(timeout == null ? DEFAULT_TIMEOUT : timeout);
      return r;
    }
  }

  private static class StringServerResolver extends ResolverWrapper {
    private final String server;

    public StringServerResolver(String server){
      this.server = server;
    }


    @Override
    Resolver internalResolver() throws UnknownHostException {
      return new SimpleResolver(server);
    }
  }

  private static class InetSocketAddressResolver extends ResolverWrapper {
    private final InetSocketAddress inetSocketAddress;

    public InetSocketAddressResolver(InetSocketAddress inetSocketAddress){
      this.inetSocketAddress = inetSocketAddress;
    }


    @Override
    Resolver internalResolver() throws UnknownHostException {
      return new SimpleResolver(inetSocketAddress);
    }
  }

  public CustomDns(
      List<String> servers,
      List<InetSocketAddress> addresses,
      Duration timeout
  ) {
    List<Resolver> resolvers = Stream.concat(
            Optional.ofNullable(servers)
                .orElse(Collections.emptyList())
                .stream()
                .map(StringServerResolver::new),
            Optional.ofNullable(addresses)
                .orElse(Collections.emptyList())
                .stream()
                .map(InetSocketAddressResolver::new)
        ).map(address -> Try.of(() -> address.toResolver(timeout)))
        .map(tryResolve -> tryResolve.getOrElseThrow(e -> {
          if (e.getClass().isAssignableFrom(UnknownHostException.class)) {
            throw new RuntimeException(e);
          } else {
            throw (RuntimeException) e;
          }
        }))
        .collect(Collectors.toList());

    resolver = resolvers.isEmpty() ? new ExtendedResolver() : new ExtendedResolver(resolvers);
  }


  @Override
  public List<InetAddress> resolve(String hostname) throws UnknownHostException {

    Message queryMessage = Message.newQuery(Record.newRecord(getName(hostname), Type.A, DClass.IN));

    Optional<Message> send = Optional.ofNullable(
        Try.of(() -> resolver.send(queryMessage)).getOrElseThrow(ExceptionUtils::wrap)
    );
    if (send.isEmpty()) {
      return null;
    }

    LookupResult lookupResult = buildResult(send.get(), queryMessage.getQuestion());
    return lookupResult.getRecords()
        .stream()
        .filter(record -> record.getType() == Type.A)
        .map(record -> (ARecord) record)
        .map(ARecord::getAddress)
        .collect(Collectors.toList());
  }

  private Name getName(String hostname) throws UnknownHostException {
    if (hostname == null) {
      throw new UnknownHostException("null");
    }

    Name name;
    try {
      name = Name.fromString(hostname.endsWith(".") ? hostname : hostname + ".");
    } catch (TextParseException e) {
      throw ExceptionUtils.toUnknownHostException(hostname, e);
    }
    return name;
  }

  private static LookupResult buildResult(Message answer, Record query) throws UnknownHostException {
    int rcode = answer.getRcode();
    List<Record> answerRecords = answer.getSection(Section.ANSWER);
    if (answerRecords.isEmpty() && rcode != Rcode.NOERROR) {
      switch (rcode) {
        case Rcode.NXDOMAIN:
          throw ExceptionUtils.toUnknownHostException(
              query.getName().toString(),
              new NoSuchDomainException(query.getName(), query.getType())
          );
        case Rcode.NXRRSET:
          throw new NoSuchRRSetException(query.getName(), query.getType());
        case Rcode.SERVFAIL:
          throw new ServerFailedException();
        default:
          throw new LookupFailedException(String.format("Unknown non-success error code %s", Rcode.string(rcode)));
      }
    }
    return new LookupResult(answerRecords, null);
  }
}

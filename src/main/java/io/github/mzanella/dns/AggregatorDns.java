package io.github.mzanella.dns;

import io.vavr.control.Try;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DnsResolver aggregator. Only delegates the resolution to specified resolvers.
 */
class AggregatorDns implements DnsResolver {

  private final List<DnsResolver> resolvers;

  public AggregatorDns(DnsResolver ... resolvers) {
    this.resolvers = Arrays.asList(resolvers);
  }

  public AggregatorDns(List<DnsResolver> resolvers) {
    this.resolvers = resolvers;
  }

  @Override
  public List<InetAddress> resolve(String hostname) throws UnknownHostException {
    if (hostname == null) {
      throw new UnknownHostException("null");
    }

    List<InetAddress> collect = resolvers.stream()
        .map(resolver -> Try.of(() -> resolver.resolve(hostname)))
        .filter(Try::isSuccess)
        .map(Try::get)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    if (collect.isEmpty()) {
      throw new UnknownHostException(hostname);
    }

    return collect;
  }
}

package io.github.mzanella.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.function.Predicate;

public class MatcherDns implements DnsResolver {

  private final Predicate<String> matcher;
  private final DnsResolver dnsCustom;

  public MatcherDns(Match match) {
    this.matcher = match.matcher;
    this.dnsCustom = match.resolver;
  }

  @Override
  public List<InetAddress> resolve(String hostname) throws UnknownHostException {
    if (hostname != null && matcher.test(hostname)) {
      return dnsCustom.resolve(hostname);
    }

    throw new UnknownHostException(hostname);
  }
}

package io.github.mzanella.dns.testutils;

import io.github.mzanella.dns.DnsResolver;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class DnsResolverDelegator implements DnsResolver {

  private final DnsResolver resolver;
  private int counter;

  public DnsResolverDelegator(DnsResolver resolver) {
    this.resolver = resolver;
    counter = 0;
  }

  @Override
  public List<InetAddress> resolve(String hostname) throws UnknownHostException {
    counter++;
    return resolver.resolve(hostname);
  }

  public int getCounter() {
    return counter;
  }
}

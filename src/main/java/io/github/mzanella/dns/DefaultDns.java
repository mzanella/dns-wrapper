package io.github.mzanella.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * System DNS class wrapper
 */
class DefaultDns implements DnsResolver {

  public static final DefaultDns SYSTEM = new DefaultDns();

  private DefaultDns() {}

  @Override
  public List<InetAddress> resolve(String hostname) throws UnknownHostException {
    if (hostname == null) {
      throw new UnknownHostException("hostname == null");
    }

    return Optional.ofNullable(InetAddress.getAllByName(hostname))
        .map(Arrays::asList)
        .orElseThrow(() -> new UnknownHostException(hostname));
  }
}

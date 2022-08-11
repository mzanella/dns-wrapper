package io.github.mzanella.dns;

import io.github.mzanella.dns.DnsResolver;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DefaultDnsTest {

  @Test
  public void unknown_host() {
    Assertions.assertThrows(
        UnknownHostException.class,
        () -> new DnsResolver.Builder().build().resolve("reallyreallyrandostuff_kmsdngmdsvnmdnbdvmdnvmdns")
    );
  }

  @Test
  public void unknown_host_on_null() {
    Assertions.assertThrows(
        UnknownHostException.class,
        () -> new DnsResolver.Builder().build().resolve(null)
    );
  }

  @Test
  public void test() throws UnknownHostException {
    List<InetAddress> resolve = new DnsResolver.Builder().build().resolve("github.com");
    Assertions.assertFalse(resolve.isEmpty());
  }
}
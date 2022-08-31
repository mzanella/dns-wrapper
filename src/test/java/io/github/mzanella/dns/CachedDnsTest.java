package io.github.mzanella.dns;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CachedDnsTest extends DnsTestBase {

  private static DnsResolver cachedDns;

  @BeforeAll
  public static void setup() throws IOException {
    DnsTestBase.setup();
    cachedDns = new DnsResolver.Builder()
        .withDnsAddress(Collections.singletonList(new InetSocketAddress("127.0.0.1", mockDNSServer.getPort())))
        .withCache(Duration.ofMinutes(30), 10, true)
        .build();
  }

  @Test
  public void unknown_host() {
    Assertions.assertThrows(
        UnknownHostException.class,
        () -> cachedDns.resolve("reallyreallyrandostuff_kmsdngmdsvnmdnbdvmdnvmdns")
    );
    Assertions.assertThrows(
        UnknownHostException.class,
        () -> cachedDns.resolve("reallyreallyrandostuff_kmsdngmdsvnmdnbdvmdnvmdns")
    );
    Assertions.assertEquals(1, mockDNSServer.getRequestCount());
  }

  @Test
  public void unknown_host_on_null() {
    Assertions.assertThrows(
        UnknownHostException.class,
        () -> cachedDns.resolve(null)
    );
    Assertions.assertEquals(0, mockDNSServer.getRequestCount());
  }

  @Test
  public void test() throws UnknownHostException {
    List<InetAddress> resolve1 = cachedDns.resolve("github.com");
    Assertions.assertFalse(resolve1.isEmpty());
    List<InetAddress> resolve2 = cachedDns.resolve("github.com");
    Assertions.assertFalse(resolve2.isEmpty());
    Assertions.assertEquals(1, mockDNSServer.getRequestCount());
    Assertions.assertEquals(resolve1, resolve2);
    cachedDns.resolve("google.com");
    Assertions.assertEquals(2, mockDNSServer.getRequestCount());
  }
}
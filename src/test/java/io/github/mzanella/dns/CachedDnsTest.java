package io.github.mzanella.dns;

import io.github.mzanella.dns.testutils.DnsResolverDelegator;
import io.github.mzanella.dns.testutils.DnsSimulatorTestContainer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class CachedDnsTest {

  @Container
  public static DnsSimulatorTestContainer dnsServer = new DnsSimulatorTestContainer.Builder().build();
  private DnsResolver cachedDns;
  private DnsResolverDelegator delegator;

  @BeforeEach
  public void setup() throws IOException {
    delegator = new DnsResolverDelegator(new CustomDns(
        null, Collections.singletonList(new InetSocketAddress(dnsServer.getIp(), dnsServer.getPort())), null
    ));

    cachedDns = new DnsResolver.Builder()
        .withMatchesOnDns(s -> true, delegator)
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
    Assertions.assertEquals(1, delegator.getCounter());
  }

  @Test
  public void unknown_host_on_null() {
    Assertions.assertThrows(
        UnknownHostException.class,
        () -> cachedDns.resolve(null)
    );
    Assertions.assertEquals(0, delegator.getCounter());
  }

  @Test
  public void test() throws UnknownHostException {
    List<InetAddress> resolve1 = cachedDns.resolve("github.com");
    Assertions.assertFalse(resolve1.isEmpty());
    List<InetAddress> resolve2 = cachedDns.resolve("github.com");
    Assertions.assertFalse(resolve2.isEmpty());
    Assertions.assertEquals(1, delegator.getCounter());
    Assertions.assertEquals(resolve1, resolve2);
    cachedDns.resolve("google.com");
    Assertions.assertEquals(2, delegator.getCounter());
  }
}
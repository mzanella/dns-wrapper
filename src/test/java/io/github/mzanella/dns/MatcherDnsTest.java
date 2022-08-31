package io.github.mzanella.dns;

import io.github.mzanella.dns.testutils.DnsResolverDelegator;
import io.github.mzanella.dns.testutils.DnsSimulatorTestContainer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class MatcherDnsTest {

  private static DnsResolver matcherDns;
  @Container
  public static DnsSimulatorTestContainer dnsServer = new DnsSimulatorTestContainer.Builder().build();
  private DnsResolverDelegator delegator;

  @BeforeEach
  public void setup() throws IOException {
    this.delegator = new DnsResolverDelegator(new CustomDns(
        null, Collections.singletonList(new InetSocketAddress(dnsServer.getIp(), dnsServer.getPort())), null
    ));
    matcherDns = new DnsResolver.Builder()
        .withMatchesOnDns(s -> !s.startsWith("not-a-match"), delegator)
        .build();
  }

  @Test
  public void unknown_host() {
    Assertions.assertThrows(
        UnknownHostException.class,
        () -> matcherDns.resolve("reallyreallyrandostuff_kmsdngmdsvnmdnbdvmdnvmdns")
    );
    Assertions.assertEquals(1, delegator.getCounter());
  }

  @Test
  public void unknown_host_on_not_match() {
    Assertions.assertThrows(
        UnknownHostException.class,
        () -> matcherDns.resolve("not-a-match.google.com")
    );
    Assertions.assertEquals(0, delegator.getCounter());
  }

  @Test
  public void unknown_host_on_null() {
    Assertions.assertThrows(
        UnknownHostException.class,
        () -> matcherDns.resolve(null)
    );
    Assertions.assertEquals(0, delegator.getCounter());
  }

  @Test
  public void test() throws UnknownHostException {
    List<InetAddress> resolve = matcherDns.resolve("github.com");
    Assertions.assertFalse(resolve.isEmpty());
    Assertions.assertEquals(1, delegator.getCounter());
  }
}
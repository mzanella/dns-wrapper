package io.github.mzanella.dns;

import io.github.mzanella.dns.testutils.DnsSimulatorTestContainer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class CustomDnsTest {

  private static DnsResolver customDns;
  @Container
  public static DnsSimulatorTestContainer dnsServer = new DnsSimulatorTestContainer.Builder().build();

  @BeforeAll
  public static void setup() throws IOException {
    DnsTestBase.setup();
    customDns = new DnsResolver.Builder()
        .withDnsAddress(Collections.singletonList(new InetSocketAddress(dnsServer.getIp(), dnsServer.getPort())))
        .build();
  }

  @Test
  public void unknown_host() {
    Assertions.assertThrows(
        UnknownHostException.class,
        () -> customDns.resolve("reallyreallyrandostuff_kmsdngmdsvnmdnbdvmdnvmdns")
    );
  }

  @Test
  public void unknown_host_on_null() {
    Assertions.assertThrows(
        UnknownHostException.class,
        () -> customDns.resolve(null)
    );
  }

  @Test
  public void test() throws UnknownHostException {
    List<InetAddress> resolve = customDns.resolve("github.com");
    Assertions.assertFalse(resolve.isEmpty());
  }
}
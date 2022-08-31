package io.github.mzanella.dns;

import io.github.mzanella.dns.exception.DnsTimeoutException;
import io.github.mzanella.dns.testutils.DnsSimulatorTestContainer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class TimeoutDnsTest {


  @Container
  public static DnsSimulatorTestContainer dnsServer = new DnsSimulatorTestContainer.Builder().withTimeoutMillis(1000).build();

  private DnsResolver getDnsResolver(Duration timeout) {
    return new DnsResolver.Builder()
        .withDnsAddress(Collections.singletonList(new InetSocketAddress(dnsServer.getIp(), dnsServer.getPort())))
        .withTimeout(timeout)
        .withFallbackToDefault(false)
        .build();
  }

  @Test
  public void test_timeout_reached() {
    Assertions.assertThrows(
        DnsTimeoutException.class,
        () -> getDnsResolver(Duration.ofMillis(1)).resolve("github.com")
    );
  }

  @Test
  public void test_timeout_not_reached() throws Exception {
    List<InetAddress> resolve = getDnsResolver(Duration.ofMillis(2000)).resolve("github.com");
    Assertions.assertFalse(resolve.isEmpty());
  }
}
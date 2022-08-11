package io.github.mzanella.dns;

import io.github.mzanella.dns.exception.DnsTimeoutException;
import io.github.mzanella.dns.testutils.DnsTestBase;
import io.github.mzanella.dns.testutils.MockDNSServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TimeoutDnsTest extends DnsTestBase {

  @BeforeAll
  public static void setup() throws IOException {
    mockDNSServer = new MockDNSServer(Duration.ofMillis(1000));
    mockDNSServer.start();
  }

  private static DnsResolver getDnsResolver(Duration timeout) {
    return new DnsResolver.Builder()
        .withDnsAddress(Collections.singletonList(new InetSocketAddress("127.0.0.1", mockDNSServer.getPort())))
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
    Assertions.assertEquals(1, mockDNSServer.getRequestCount());
  }
}
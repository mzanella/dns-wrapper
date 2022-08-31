package io.github.mzanella.dns;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MatcherDnsTest extends DnsTestBase {

  private DnsResolver matcherDns;

  @BeforeEach
  public void setup() throws IOException {
    super.setup();
    matcherDns = new DnsResolver.Builder()
        .withMatchesOnDns(
            s -> !s.startsWith("not-a-match"),
            new CustomDns(null, Collections.singletonList(new InetSocketAddress("127.0.0.1", mockDNSServer.getPort())), null)
        ).build();
  }

  @Test
  public void unknown_host() {
    Assertions.assertThrows(
        UnknownHostException.class,
        () -> matcherDns.resolve("reallyreallyrandostuff_kmsdngmdsvnmdnbdvmdnvmdns")
    );
    Assertions.assertEquals(1, mockDNSServer.getRequestCount());
  }

  @Test
  public void unknown_host_on_not_match() {
    Assertions.assertThrows(
        UnknownHostException.class,
        () -> matcherDns.resolve("not-a-match.google.com")
    );
    Assertions.assertEquals(0, mockDNSServer.getRequestCount());
  }

  @Test
  public void unknown_host_on_null() {
    Assertions.assertThrows(
        UnknownHostException.class,
        () -> matcherDns.resolve(null)
    );
    Assertions.assertEquals(0, mockDNSServer.getRequestCount());
  }

  @Test
  public void test() throws UnknownHostException {
    List<InetAddress> resolve = matcherDns.resolve("github.com");
    Assertions.assertFalse(resolve.isEmpty());
    Assertions.assertEquals(1, mockDNSServer.getRequestCount());
  }
}
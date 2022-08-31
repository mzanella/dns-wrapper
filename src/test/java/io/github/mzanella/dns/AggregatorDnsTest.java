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

class AggregatorDnsTest extends DnsTestBase {

  private AggregatorDns aggregatorDns;

  @BeforeEach
  public void setup() throws IOException {
    super.setup();
    aggregatorDns = new AggregatorDns(
        hostname -> {throw new UnknownHostException(hostname);},
        hostname -> {throw new UnknownHostException(hostname);},
        hostname -> Collections.emptyList(),
        hostname -> Collections.emptyList(),
        new CustomDns(null, Collections.singletonList(new InetSocketAddress("127.0.0.1", mockDNSServer.getPort())), null),
        new CustomDns(null, Collections.singletonList(new InetSocketAddress("127.0.0.1", mockDNSServer.getPort())), null),
        hostname -> {throw new RuntimeException();}
    );
  }

  @Test
  public void unknown_host() {
    Assertions.assertThrows(
        UnknownHostException.class,
        () -> aggregatorDns.resolve("reallyreallyrandostuff_kmsdngmdsvnmdnbdvmdnvmdns")
    );
    Assertions.assertEquals(2, mockDNSServer.getRequestCount());
  }

  @Test
  public void unknown_host_on_null() {
    Assertions.assertThrows(
        UnknownHostException.class,
        () -> aggregatorDns.resolve(null)
    );
    Assertions.assertEquals(0, mockDNSServer.getRequestCount());
  }

  @Test
  public void test() throws UnknownHostException {
    List<InetAddress> resolve = aggregatorDns.resolve("github.com");
    Assertions.assertFalse(resolve.isEmpty());
    Assertions.assertEquals(2, mockDNSServer.getRequestCount());
  }
}
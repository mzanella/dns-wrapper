package io.github.mzanella.dns.testutils;

import io.github.mzanella.dns.simulator.MockDNSServer;
import java.util.Optional;
import org.testcontainers.containers.GenericContainer;

public class DnsSimulatorTestContainer extends GenericContainer<DnsSimulatorTestContainer> {

  public static class Builder {
    private final String tag;
    private final Integer port;
    private final Integer timeoutMillis;
    private final String[] servers;

    public Builder() {
      this(null, null, null, null);
    }
    private Builder(
        String tag,
        Integer port,
        Integer timeoutMillis,
        String[] servers
    ) {
      this.tag = tag;
      this.port = port;
      this.timeoutMillis = timeoutMillis;
      this.servers = servers;
    }

    public Builder withTag(String tag) {
      return new Builder(
          tag,
          this.port,
          this.timeoutMillis,
          this.servers
      );
    }

    public Builder withPort(int port) {
      return new Builder(
          this.tag,
          port,
          this.timeoutMillis,
          this.servers
      );
    }

    public Builder withTimeoutMillis(int timeoutMillis) {
      return new Builder(
          this.tag,
          this.port,
          timeoutMillis,
          this.servers
      );
    }

    public Builder withServers(String ... servers) {
      return new Builder(
          this.tag,
          this.port,
          this.timeoutMillis,
          servers
      );
    }

    public DnsSimulatorTestContainer build() {
      StringBuilder command = new StringBuilder();
      int actualPort = port == null ? MockDNSServer.DEFAULT_PORT : port;
      Optional<String> timeoutString = Optional.ofNullable(timeoutMillis).map(Object::toString);
      Optional<String[]> optServers = Optional.ofNullable(servers);
      String actualTimeout = optServers.isPresent() ? timeoutString.orElse("0") : timeoutString.orElse("");
      String actualServers = optServers.map(s -> String.join(" ", s)).orElse("");
      return new DnsSimulatorTestContainer(
          tag == null ? DEFAULT_TAG : tag,
          actualPort,
          command.append(actualPort)
              .append(" ").append(actualTimeout)
              .append(" ").append(actualServers)
              .toString()
      );
    }
  }

  private static final String IMAGE = "mzanella5/dnswrapper-simulator";
  public static final String DEFAULT_TAG = "latest";
  private final int port;

  private DnsSimulatorTestContainer(String tag, Integer port, String command) {
    super(IMAGE + ":" + tag);
    this.port = port;
    withExposedPorts(this.port);
    withCommand(command);
  }

  public String getIp() {
    return getContainerInfo().getNetworkSettings().getIpAddress();
  }

  public int getPort() {
    return port;
  }
}

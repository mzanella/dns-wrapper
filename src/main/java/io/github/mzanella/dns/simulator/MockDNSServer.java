package io.github.mzanella.dns.simulator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Message;

public class MockDNSServer extends Thread {

  public static final int DEFAULT_PORT = 5794;
  private static final Duration DEFAULT_WAIT = Duration.ofMillis(0);
  private static final String[] DEFAULT_DNS = {"8.8.8.8", "1.1.1.1"};
  private final Duration timeout;
  private volatile boolean running = false;
  private static final int UDP_SIZE = 512;
  private int port;
  private final String[] dns;
  private ServerSocket serverSocket = null;
  private DatagramSocket socket = null;
  private AtomicInteger requestCount;
  public MockDNSServer(Duration timeout, Integer port, List<String> dns) {
    this.timeout = timeout == null ? DEFAULT_WAIT : timeout;
    this.port = port == null ? DEFAULT_PORT : port;
    this.dns = dns == null ? DEFAULT_DNS : dns.toArray(new String[0]);
    requestCount = new AtomicInteger();
  }
  public MockDNSServer(Duration timeout) {
    this(timeout, null, null);
  }

  public MockDNSServer() {
    this(null, null, null);
  }

  public MockDNSServer(int port) {
    this(null, port, null);
  }

  public void run() {
    try {
      synchronized (this) {
        serverSocket = new ServerSocket(Math.max(port, 0));
        port = serverSocket.getLocalPort();
        running = true;
      }
      serve();
    } catch (SocketException e){
      if(socket != null && !socket.isClosed()) {
        throw new RuntimeException(e);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  public synchronized void close() {
    try {
      socket.close();
      serverSocket.close();
    } catch (Exception ignore) {}
    running = false;
  }

  public int getPort() {
    if (port == -1) {
      throw new UnsupportedOperationException("Server not started");
    }
    return port;
  }

  public int getRequestCount() {
    return requestCount.get();
  }
  private void serve() throws IOException {
    socket = new DatagramSocket(port);
    while (running && !socket.isClosed()) {
      process(socket);
    }
    System.out.println("stop serving requests");
  }
  private void process(DatagramSocket socket) throws IOException {
    byte[] in = new byte[UDP_SIZE];
    // Read the request
    DatagramPacket inputDataPacket = new DatagramPacket(in, UDP_SIZE);
    socket.receive(inputDataPacket);
    requestCount.incrementAndGet();
    System.out.println("new request received (count " + requestCount.get() + ")");
    // Build the response
    Message request = new Message(in);
    Message message = new ExtendedResolver(dns).send(request);

    try {
      if (timeout != null && timeout.toMillis() > 0) {
        System.out.println("sleeping " + timeout.toMillis() + "ms before answering ...");
        Thread.sleep(timeout.toMillis());
        System.out.println("Done!");
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    byte[] resp = message.toWire();
    DatagramPacket outputDataPacket = new DatagramPacket(resp, resp.length, inputDataPacket.getAddress(), inputDataPacket.getPort());
    socket.send(outputDataPacket);
  }

  /**
   * args[0] port, default 5794
   * args[1] time to wait in milliseconds before sending response, default no wait
   * args[2] ... dns servers to use, default 8.8.8.8 and 1.1.1.1
   */
  public static void main(String[] args) {
    Arrays.stream(args).forEach(System.out::println);
    int port = 5794;
    int wait = 0;
    List<String> dnsServers = List.of("8.8.8.8", "1.1.1.1");
    if (args.length > 0) {
      port = Integer.parseInt(args[0]);
    }
    if (args.length > 1) {
      wait = Integer.parseInt(args[1]);
    }
    if (args.length > 2) {
      dnsServers = Arrays.stream(args, 2, args.length).collect(Collectors.toList());
    }
    System.out.printf(
        "starting dns simulator on port %d - waiting %dms before answer - using %s servers ...\n",
        port, wait, String.join(", ", dnsServers)
    );
    MockDNSServer mockDNSServer = new MockDNSServer(
        Duration.ofMillis(wait),
        port,
        dnsServers
    );
    mockDNSServer.start();
  }
}

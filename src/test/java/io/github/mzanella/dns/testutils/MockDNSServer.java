package io.github.mzanella.dns.testutils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.Arrays;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Message;

public class MockDNSServer {

  private final Duration timeout;
  private Thread thread = null;
  private volatile boolean running = false;
  private static final int UDP_SIZE = 512;
  private int port = -1;
  private ServerSocket serverSocket = null;
  private int requestCount = 0;
  public MockDNSServer(Duration timeout) {
    this.timeout = timeout;
  }

  MockDNSServer() {
    this(null);
  }

  public void start() throws IOException {
    serverSocket = new ServerSocket(0);
    port = serverSocket.getLocalPort();
    running = true;
    thread = new Thread(() -> {
      try {
        serve();
      } catch (IOException ex) {
        stop();
        throw new RuntimeException(ex);
      }
    });
    thread.start();
  }
  public void stop() {
    try {
      serverSocket.close();
    } catch (Exception ignore) {}
    running = false;
    thread.interrupt();
    thread = null;
  }

  public int getPort() {
    if (port == -1) {
      throw new UnsupportedOperationException("Server not started");
    }
    return port;
  }

  public int getRequestCount() {
    return requestCount;
  }

  public void resetCount() {
    requestCount = 0;
  }
  private void serve() throws IOException {
    DatagramSocket socket = new DatagramSocket(port);
    while (running) {
      process(socket);
    }
  }
  private void process(DatagramSocket socket) throws IOException {
    byte[] in = new byte[UDP_SIZE];
    // Read the request
    DatagramPacket indp = new DatagramPacket(in, UDP_SIZE);
    socket.receive(indp);
    ++requestCount;
    // Build the response
    Message request = new Message(in);
    Message message = new ExtendedResolver(Arrays.asList("8.8.8.8", "1.1.1.1").toArray(new String[]{})).send(request);

    try {
      if (timeout != null) {
        Thread.sleep(timeout.toMillis());
      }
    } catch (Exception e) {}

    byte[] resp = message.toWire();
    DatagramPacket outdp = new DatagramPacket(resp, resp.length, indp.getAddress(), indp.getPort());
    socket.send(outdp);
  }
}

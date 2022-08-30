package io.github.mzanella.dns.exception;

/**
 * Class wrapping all exceptions that can be thrown that are not UnknownHostException or exception thrown because of a timeout
 */
public class DnsWrapperException extends RuntimeException {

  DnsWrapperException(Throwable e) {
    super(e);
  }

}

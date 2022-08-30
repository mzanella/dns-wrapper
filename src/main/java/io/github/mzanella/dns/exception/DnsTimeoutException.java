package io.github.mzanella.dns.exception;

/**
 * Class wrapping exceptions thrown because of a timeout
 */
public class DnsTimeoutException extends DnsWrapperException {

  DnsTimeoutException(Throwable e) {
    super(e);
  }
}

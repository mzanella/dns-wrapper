package io.github.mzanella.dns.exception;

public class DnsTimeoutException extends DnsWrapperException {

  DnsTimeoutException(Throwable e) {
    super(e);
  }
}

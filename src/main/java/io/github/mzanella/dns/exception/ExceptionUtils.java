package io.github.mzanella.dns.exception;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class ExceptionUtils {

  private ExceptionUtils(){}

  public static UnknownHostException toUnknownHostException(String host, Throwable cause) {
    UnknownHostException exception = new UnknownHostException(host);
    exception.initCause(cause);
    return exception;
  }

  public static DnsWrapperException wrap(Throwable e) {
    if (e.getClass().isAssignableFrom(SocketTimeoutException.class)) {
      throw new DnsTimeoutException(e);
    } else {
      throw new DnsWrapperException(e);
    }
  }
}

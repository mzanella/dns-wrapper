package io.github.mzanella.dns;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.mzanella.dns.exception.ExceptionUtils;
import io.vavr.control.Try;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

class CachedDns implements DnsResolver {

  private static final Integer DEFAULT_MAX_SIZE = 100000;
  private static final Duration DEFAULT_EXPIRATION = Duration.ofMinutes(5);
  private final Cache<String, Optional<Try<List<InetAddress>>>> cache;
  private final DnsResolver resolver;
  private final boolean cacheErrors;

  public CachedDns(
      DnsResolver resolver,
      CacheConfig cacheConfig
  ) {
    this.resolver = resolver;
    this.cacheErrors = cacheConfig.cacheErrors;
    this.cache = CacheBuilder.newBuilder()
        .expireAfterAccess(cacheConfig.expiration)
        .maximumSize(cacheConfig.maxSize)
        .build();
  }

  @Override
  public List<InetAddress> resolve(String hostname) throws UnknownHostException {
    if (hostname == null) {
      throw new UnknownHostException("null");
    }

    try {
      Optional<Try<List<InetAddress>>> lists = cache.get(hostname, () -> {
        Try<List<InetAddress>> addresses = Try.of(() -> resolver.resolve(hostname));
        if (addresses.isFailure() && !cacheErrors) {
          return Optional.empty();
        }
        return Optional.of(addresses);
      });

      if (cacheErrors && lists.isPresent()) {
        return lists.get().get();
      } else {
        return lists.orElseGet(() -> {
          Try<List<InetAddress>> resolve = Try.of(() -> resolver.resolve(hostname));
          if (resolve.isSuccess()) {
            cache.put(hostname, Optional.of(resolve));
          }
          return resolve;
        }).get();
      }
    } catch (ExecutionException e) {
      throw ExceptionUtils.wrap(e);
    }
  }
}

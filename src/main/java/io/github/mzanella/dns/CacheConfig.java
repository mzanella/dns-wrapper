package io.github.mzanella.dns;

import java.time.Duration;

class CacheConfig {

  public static final Duration DEFAULT_CACHE_EXPIRATION = Duration.ofMinutes(10);
  public static final int DEFAULT_MAX_CACHE_SIZE = 1000;
  public static boolean DEFAULT_CACHE_ERRORS = false;
  final Duration expiration;
  final Integer maxSize;
  final boolean cacheErrors;

  CacheConfig(Duration expiration, Integer maxSize, Boolean cacheErrors) {
    this.expiration = expiration == null ? DEFAULT_CACHE_EXPIRATION : expiration;
    this.maxSize = maxSize == null ? DEFAULT_MAX_CACHE_SIZE : maxSize;
    this.cacheErrors = (cacheErrors == null ? DEFAULT_CACHE_ERRORS : cacheErrors);
  }
}

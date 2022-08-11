package io.github.mzanella.dns;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface DnsResolver {

  List<InetAddress> resolve(String hostname) throws UnknownHostException;



  class Builder {

    private Duration timeout = null;
    private List<Match> matches = new ArrayList<>();
    private List<String> servers = null;
    private List<InetSocketAddress> addresses = null;
    private boolean fallback = true;
    private CacheConfig cacheConfig = null;

    public Builder() {}
    private Builder(
        Duration timeout,
        List<Match> matches,
        List<String> servers,
        List<InetSocketAddress> addresses,
        boolean fallback,
        CacheConfig cacheConfig
    ){
      this.timeout = timeout;
      this.matches = matches;
      this.servers = servers;
      this.addresses = addresses;
      this.fallback = fallback;
      this.cacheConfig = cacheConfig;
    }

    public Builder withTimeout(Duration timeout) {
      return new Builder(
          timeout,
          this.matches,
          this.servers,
          this.addresses,
          this.fallback,
          this.cacheConfig
      );
    }

    public Builder withMatchesOnDns(Predicate<String> matcher, DnsResolver resolver) {

      List<Match> newMatches = new ArrayList<>(matches);
      newMatches.add(new Match(matcher, resolver));

      return new Builder(
          this.timeout,
          newMatches,
          this.servers,
          this.addresses,
          this.fallback,
          this.cacheConfig
      );
    }

    public Builder withMatchesOnSystemDns(Predicate<String> matcher) {
      return withMatchesOnDns(matcher, DefaultDns.SYSTEM);
    }

    public Builder withDnsServers(List<String> servers) {
      return new Builder(
          this.timeout,
          this.matches,
          servers,
          this.addresses,
          this.fallback,
          this.cacheConfig
      );
    }

    public Builder withDnsAddress(List<InetSocketAddress> addresses) {
      return new Builder(
          this.timeout,
          this.matches,
          this.servers,
          addresses,
          this.fallback,
          this.cacheConfig
      );
    }

    public Builder withFallbackToDefault(boolean fallback) {
      return new Builder(
          this.timeout,
          this.matches,
          this.servers,
          this.addresses,
          fallback,
          this.cacheConfig
      );
    }

    public Builder withCache(Duration expiration, Integer maxSize, Boolean cacheErrors) {
      return new Builder(
          this.timeout,
          this.matches,
          this.servers,
          this.addresses,
          this.fallback,
          new CacheConfig(expiration, maxSize, cacheErrors)
      );
    }

    public DnsResolver build(){
      DnsResolver resolver = DefaultDns.SYSTEM;

      if (!matches.isEmpty()) {

        List<DnsResolver> dnsMatchers = matches.stream()
            .map(MatcherDns::new)
            .collect(Collectors.toList());

        if (fallback) {
          if (servers != null || addresses != null) {
            dnsMatchers.add(new CustomDns(servers, addresses, timeout));
          }
          dnsMatchers.add(DefaultDns.SYSTEM);
        }

        resolver = new AggregatorDns(dnsMatchers);
      } else {
        if (servers != null || addresses != null) {
          resolver = new CustomDns(servers, addresses, timeout);

          if (fallback) {
            resolver = new AggregatorDns(
                resolver,
                DefaultDns.SYSTEM
            );
          }
        }

        if (cacheConfig != null) {
          resolver = new CachedDns(resolver, cacheConfig);
        }
      }

      return resolver;
    }

  }

}

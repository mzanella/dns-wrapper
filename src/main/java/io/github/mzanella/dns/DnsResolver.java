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

  /**
   * Given a server name returns the addresses associated to that
   * @param hostname Server name to resolve the name of
   * @return The list of addresses associated to the server name
   * @throws UnknownHostException thrown if hostname cannot be resolved
   */
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

    /**
     * Defines that the name server that matches the predicate should be resolved using the given DnsResolver
     * @param timeout Predicate that define which name servers to take into account
     * @return the builder
     */
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

    /**
     * Defines that the name server that matches the predicate should be resolved using the given DnsResolver
     * @param matcher Predicate that define which name servers to take into account
     * @param resolver The resolver to use to resolve the name servers matching the specified predicate
     * @return the builder
     */
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

    /**
     * Defines that the name server that matches the predicate should be resolved using the system DNS configuration
     * @param matcher Predicate that define which name servers to take into account
     * @return the builder
     */
    public Builder withMatchesOnSystemDns(Predicate<String> matcher) {
      return withMatchesOnDns(matcher, DefaultDns.SYSTEM);
    }

    /**
     * Defines the DNS servers to use
     * @param servers DNS names to use
     * @return the builder
     */
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

    /**
     * Defines the DNS servers to use
     * @param addresses DNS address to use
     * @return the builder
     */
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

    /**
     * Defines whether to fallback to system DNS if the resolution fails or not (Default value is 'true')
     * @param fallback whether to fallback to system DNS or not
     * @return the builder
     */
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

    /**
     * Defines cache properties to be used for name resolution (Default no caching)
     * @param expiration Cache expiration
     * @param maxSize Cache max size
     * @param cacheErrors Whether to cache name server resolution errors or not
     * @return the builder
     */
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

    /**
     * @return the dns resolver
     */
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

      }

      if (cacheConfig != null) {
        resolver = new CachedDns(resolver, cacheConfig);
      }

      return resolver;
    }

  }

}

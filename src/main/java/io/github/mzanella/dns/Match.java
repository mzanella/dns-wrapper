package io.github.mzanella.dns;

import java.util.function.Predicate;

class Match {

  final Predicate<String> matcher;
  final DnsResolver resolver;

  public Match(Predicate<String> matcher, DnsResolver resolver) {
    this.matcher = matcher;
    this.resolver = resolver;
  }
}

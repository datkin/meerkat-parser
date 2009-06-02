package meerkat.parser;

import java.util.Map;
import java.util.HashMap;

import meerkat.Stream;
import meerkat.grammar.Grammar;
import meerkat.grammar.Rule;

public class RecursiveParser<T> extends AbstractParser<T> {
  private final Map<CacheKey<T>, CacheEntry<T>> cache = new HashMap<CacheKey<T>, CacheEntry<T>>();

  public RecursiveParser(Grammar<T> grammar) {
    super(grammar, new BaseEngineFactory<T>());
  }

  @Override
  public Result<T> parse(Stream<T> stream, Rule<T> rule) {
    CacheKey<T> key = new CacheKey<T>(rule, stream);
    if (!cache.containsKey(key)) {
      CacheEntry<T> entry = new CacheEntry<T>(new BasicResult<T>(null, null));
      cache.put(key, entry);
      entry.result = super.parse(stream, rule);
      return entry.result;
    } else {
      return cache.get(key).result;
    }
  }
}

class CacheEntry<T> {
  boolean lrDetected = false;
  Result<T> result = null;

  public CacheEntry(Result<T> result) {
    this.result = result;
  }

  // check this to determine if it's a check for LR or not
  public boolean hasResult() {
    return result != null;
  }

  public boolean lrDetected() {
    return lrDetected;
  }

  public void setResult(Result<T> result) {
    this.result = result;
  }

  public Result<T> getResult() {
    return this.result;
  }
}

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

  // Remember: no need to do Stream position management;
  // whatever stream is returned by out result will be the one
  // that the parse engine continues parsing with
  @Override
  public Result<T> parse(Stream<T> stream, Rule<T> rule) {
    CacheKey<T> key = new CacheKey<T>(rule, stream);
    if (!cache.containsKey(key)) {
      CacheEntry<T> entry = new CacheEntry<T>();
      cache.put(key, entry);
      entry.result = super.parse(stream, rule);
      if (entry.detectedLR()) {
        return growLR(stream, rule, entry);
      }
      return entry.result;
    } else {
      CacheEntry<T> entry = cache.get(key);
      if (!entry.hasResult()) { // if m.ans is LR
        entry.detectedLR = true;
        return new BasicResult<T>(null, null);
      }
      return entry.result;
    }
  }

  private Result<T> growLR(Stream<T> stream, Rule<T> rule, CacheEntry<T> entry) {
    while (true) {
      Result<T> result = super.parse(stream, rule);
      if (!result.successful() || result.getRest().getPosition() <= entry.result.getRest().getPosition()) {
        return entry.result;
      }
      entry.result = result;
    }
  }
}

class CacheEntry<T> {
  boolean detectedLR = false;
  Result<T> result = null;

  public CacheEntry() {
  }

  public CacheEntry(Result<T> result) {
    this.result = result;
  }

  // check this to determine if it's a check for LR or not
  public boolean hasResult() {
    return result != null;
  }

  public boolean detectedLR() {
    return detectedLR;
  }

  public void setResult(Result<T> result) {
    this.result = result;
  }

  public Result<T> getResult() {
    return this.result;
  }
}

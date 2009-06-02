package meerkat.parser;

import java.util.Map;
import java.util.HashMap;

import meerkat.Stream;
import meerkat.grammar.*;

class CachedEngine<T> implements Engine<T> {
  private final Engine<T> child;
  private final Map<CacheKey<T>, ListResult<T>> cache = new HashMap<CacheKey<T>, ListResult<T>>();

  public CachedEngine(Parser<T> parser) {
    if (parser == null)
      throw new IllegalArgumentException();
    this.child = new BaseEngine<T>(parser, this);
  }

  @Override
  public Stream<T> setStream(Stream<T> stream) {
    return child.setStream(stream);
  }

  @Override
  public Stream<T> getStream() {
    return child.getStream();
  }

  private ListResult<T> get(CacheKey<T> key) {
    //System.out.println("cache hit on: " + key);
    ListResult<T> r = cache.get(key);
    setStream(r.getRest());
    return r;
  }

  @Override
  public ListResult<T> visit(Rule<T> rule) {
    CacheKey<T> key = new CacheKey<T>(rule, getStream());
    if (cache.containsKey(key))
      return get(key);
    ListResult<T> result = child.visit(rule);
    cache.put(key, result);
    return result;
  }

  // All these other cachings are useless unless we can do some sort of
  // "common sub expr elimination" and use the cache results across
  // different rules. The other problem here is that even if we cache
  // correctly, we do not restore the state properly! (?)
  @Override
  public ListResult<T> visit(Sequence<T> expr) {
    CacheKey<T> key = new CacheKey<T>(expr, getStream());
    if (cache.containsKey(key))
      return get(key);
    ListResult<T> result = child.visit(expr);
    cache.put(key, result);
    return result;
  }

  @Override
  public ListResult<T> visit(Choice<T> expr) {
    CacheKey<T> key = new CacheKey<T>(expr, getStream());
    if (cache.containsKey(key))
      return get(key);
    ListResult<T> result = child.visit(expr);
    cache.put(key, result);
    return result;
  }

  @Override
  public ListResult<T> visit(Optional<T> expr) {
    CacheKey<T> key = new CacheKey<T>(expr, getStream());
    if (cache.containsKey(key))
      return get(key);
    ListResult<T> result = child.visit(expr);
    cache.put(key, result);
    return result;
  }

  @Override
  public ListResult<T> visit(And<T> expr) {
    CacheKey<T> key = new CacheKey<T>(expr, getStream());
    if (cache.containsKey(key))
      return get(key);
    ListResult<T> result = child.visit(expr);
    cache.put(key, result);
    return result;
  }

  @Override
  public ListResult<T> visit(Not<T> expr) {
    CacheKey<T> key = new CacheKey<T>(expr, getStream());
    if (cache.containsKey(key))
      return get(key);
    ListResult<T> result = child.visit(expr);
    cache.put(key, result);
    return result;
  }

  @Override
  public ListResult<T> visit(ZeroOrMore<T> expr) {
    CacheKey<T> key = new CacheKey<T>(expr, getStream());
    if (cache.containsKey(key))
      return get(key);
    ListResult<T> result = child.visit(expr);
    cache.put(key, result);
    return result;
  }

  @Override
  public ListResult<T> visit(OneOrMore<T> expr) {
    CacheKey<T> key = new CacheKey<T>(expr, getStream());
    if (cache.containsKey(key))
      return get(key);
    ListResult<T> result = child.visit(expr);
    cache.put(key, result);
    return result;
  }

  // probably faster to just delegate than the cache for these two
  @Override
  public ListResult<T> visit(Class<? extends T> expr) {
    return child.visit(expr);
  }

  @Override
  public ListResult<T> visit(T t) {
    return child.visit(t);
  }

}

class CacheKey<T> {
  private final Expr<T> expr;
  private final Stream<T> start;

  public CacheKey(Expr<T> expr, Stream<T> start) {
    if (expr == null || start == null)
      throw new IllegalArgumentException();
    this.expr = expr;
    this.start = start;
  }

  @Override
    public int hashCode() {
      return 31 * this.start.hashCode() + this.expr.hashCode();
    }

  @Override
    public boolean equals(Object obj) {
      if (obj != null && obj.getClass().equals(this.getClass())) {
        CacheKey key = (CacheKey)obj;
        return key.expr.equals(this.expr) &&
          key.start.equals(this.start);
      }
      return false;
    }

  @Override
    public String toString() {
      return "<Key: " + expr + ", " + start + ">";
    }
}

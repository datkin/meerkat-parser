package meerkat.parser;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import meerkat.Stream;
import meerkat.grammar.Grammar;
import meerkat.grammar.Rule;

public class RecursiveParser<T> extends AbstractParser<T> {
  private final Map<CacheKey<T>, CacheEntry<T>> cache = new HashMap<CacheKey<T>, CacheEntry<T>>();
  private final Map<Stream<T>, Head<T>> heads = new HashMap<Stream<T>, Head<T>>();
  private LeftRecur<T> topLR = null;

  public RecursiveParser(Grammar<T> grammar) {
    super(grammar, new BaseEngineFactory<T>());
  }

  // Remember: no need to do Stream position management;
  // whatever stream is returned by the result will be the one
  // the parse engine continues parsing with
  @Override
  public Result<T> parse(Rule<T> rule, Stream<T> stream) {
    CacheEntry<T> entry = recall(stream, rule);
    if (entry == null) {
      CacheKey<T> key = new CacheKey<T>(rule, stream);
      // Create an LR and push it on the stack
      LeftRecur<T> lr = new LeftRecur<T>(rule, this.topLR);
      this.topLR = lr;
      // Memoize the LR and evaluate the rule
      entry = new CacheEntry<T>(lr);
      cache.put(key, entry);
      Result<T> result = super.parse(rule, stream);
      // Pop LR from the stack
      this.topLR = this.topLR.next;
      // MISSING: update the memoized position?
      if (lr.head != null) {
        lr.seed = result;
        return answerLR(stream, rule, entry);
      } else {
        entry.result = result;
        return result;
      }
    } else {
      if (!entry.hasResult()) { // ie entry.hasLR()
        return setupLR(rule, entry.getLR()).seed;
      }
    }
    return entry.getResult();
  }

  private LeftRecur<T> setupLR(Rule<T> rule, LeftRecur<T> lr) {
    if (lr.head == null)
      lr.head = new Head<T>(rule);
    LeftRecur<T> stack = this.topLR;
    while (!lr.head.equals(stack.head)) { // expect stack.head to be null, b/c it hasn't be setup yet
      stack.head = lr.head;
      lr.head.addRule(stack.rule);
      stack = stack.next;
    }
    return lr;
  }

  private Result<T> answerLR(Stream<T> stream, Rule<T> rule, CacheEntry<T> entry) {
    Head<T> head = entry.getLR().head;
    if (!head.rule.equals(rule)) {
      return entry.getLR().seed;
    }
    entry.setResult(entry.getLR().seed);
    if (!entry.getResult().successful()) {
      return entry.getResult();
    }
    return growLR(stream, rule, entry, head);
  }

  private Result<T> growLR(Stream<T> stream, Rule<T> rule, CacheEntry<T> entry, Head<T> head) {
    this.heads.put(stream, head);
    while (true) {
      head.resetEvalSet();
      Result<T> result = super.parse(rule, stream);
      if (!result.successful() || result.getRest().getPosition() <= entry.getResult().getRest().getPosition()) {
        this.heads.remove(stream);
        return entry.getResult();
      }
      entry.setResult(result);
    }
  }

  // TODO: make this take a cache key instead?
  // Note: this does *not* add anything to the cache (aka memo table)
  private CacheEntry<T> recall(Stream<T> stream, Rule<T> rule) {
    CacheKey<T> key = new CacheKey<T>(rule, stream);
    CacheEntry<T> entry = cache.get(key);
    Head<T> head = this.heads.get(stream);
    if (head == null) {
      return entry;
    }
    if (entry == null && !head.rule.equals(rule) && !head.hasRule(rule)) {
      return new CacheEntry<T>(new BasicResult<T>(null, null));
    }
    if (head.evalSet.contains(rule)) {
      head.evalSet.remove(rule);
      entry.result = super.parse(rule, stream);
    }
    return entry;
  }
}

class CacheEntry<T> {
  LeftRecur<T> lr = null;
  Result<T> result = null;

  public CacheEntry(LeftRecur<T> lr) {
    this.lr = lr;
  }

  public CacheEntry(Result<T> result) {
    this.result = result;
  }

  public boolean hasLR() {
    return this.lr != null;
  }

  // check this to determine if it's a check for LR or not
  public boolean hasResult() {
    return this.result != null;
  }

  public void setResult(Result<T> result) {
    this.lr = null;
    this.result = result;
  }

  public LeftRecur<T> getLR() {
    return this.lr;
  }

  public Result<T> getResult() {
    return this.result;
  }
}

class LeftRecur<T> {
  Result<T> seed = new BasicResult<T>(null, null);
  Rule<T> rule;
  Head<T> head = null;
  LeftRecur<T> next;

  public LeftRecur(Rule<T> rule, LeftRecur<T> next) {
    this.rule = rule;
    this.next = next;
  }
}

class Head<T> {
  Rule<T> rule;
  Set<Rule<T>> involvedSet = new HashSet<Rule<T>>();
  Set<Rule<T>> evalSet = new HashSet<Rule<T>>();

  public Head(Rule<T> rule) {
    this.rule = rule;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj.getClass().equals(this.getClass())) {
      return ((Head)obj).rule.equals(this.rule); // don't both comparing the sets?
    }
    return false;
  }

  public void addRule(Rule<T> rule) {
    involvedSet.add(rule);
  }

  public void resetEvalSet() {
    this.evalSet = new HashSet<Rule<T>>(this.involvedSet);
  }
  /*
  public Set<Rule<T>> getRules() {
    return this.involvedSet; // immutable copy of this?
  }
  */

  public boolean hasRule(Rule<T> rule) {
    return this.involvedSet.contains(rule);
  }
}

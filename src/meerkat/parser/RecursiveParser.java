package meerkat.parser;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import meerkat.Stream;
import meerkat.grammar.Grammar;
import meerkat.grammar.Rule;
import meerkat.parser.basic.BasicResult;

public class RecursiveParser<T> extends AbstractParser<T> {
  private final Map<CacheKey<T>, CacheEntry<T>> cache = new HashMap<CacheKey<T>, CacheEntry<T>>();
  private final Map<Stream<T>, Head<T>> heads = new HashMap<Stream<T>, Head<T>>();
  private Recursion<T> topRecursion = null;

  public RecursiveParser(Grammar<T> grammar) {
    super(grammar, new BaseEngineFactory<T>());
  }

  // Remember: no need to do Stream position management;
  // whatever stream is returned by the result will be the one
  // the parse engine continues parsing with
  @Override
  public Result<T> parse(Rule<T> rule, Stream<T> stream) {
    CacheEntry<T> entry = recall(rule, stream);
    if (entry == null) {
      Recursion<T> recursion = this.topRecursion = new Recursion<T>(rule, this.topRecursion);
      entry = new CacheEntry<T>(recursion);
      cache.put(new CacheKey<T>(rule, stream), entry);
      Result<T> result = super.parse(rule, stream);
      this.topRecursion = this.topRecursion.getNext();
      if (recursion.getHead() == null) { // No left recursion found
        return entry.setResult(result);
      } else {
        recursion.setSeed(result); // only called once
        return parseRecursive(rule, stream, entry);
      }
    } else if (entry.hasRecursion()) {
      // seed will always be a failure result here?
      return entry.getRecursion().setup(rule, this.topRecursion).getSeed();
    }
    return entry.getResult();
  }

  private Result<T> parseRecursive(Rule<T> rule, Stream<T> stream, CacheEntry<T> entry) {
    Head<T> head = entry.getRecursion().getHead();
    Result<T> seed = entry.getRecursion().getSeed();
    if (!head.getRule().equals(rule)) {
      return seed;
    }
    entry.setResult(seed); // setting the result clears the recursion field!
    if (!entry.getResult().successful()) {
      return entry.getResult();
    }
    return growRecursion(rule, stream, entry, head);
  }

  private Result<T> growRecursion(Rule<T> rule, Stream<T> stream, CacheEntry<T> entry, Head<T> head) {
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

  // Note: this does *not* add anything to the cache (aka memo table)
  private CacheEntry<T> recall(Rule<T> rule, Stream<T> stream) {
    CacheEntry<T> entry = cache.get(new CacheKey<T>(rule, stream));
    Head<T> head = this.heads.get(stream);
    // TODO: remove both these calls if I cannot think of a proper test case
    if (head == null) { // No recursion for this entry, just give the cached result
      return entry;
    }
    if (entry == null && !head.involvesRule(rule)) {
      System.out.println("unexpected call!!!!!!!!!!");
      // creation of failure results is taken care of by calls to super.parse
      // is there any reason to do it here?
      //return null;
      return new CacheEntry<T>(new BasicResult<T>(null, null));
    }
    if (head != null && head.canEval(rule)) {
      head.evalRule(rule);
      entry.setResult(super.parse(rule, stream));
    }
    return entry;
  }
}

class CacheEntry<T> {
  private Recursion<T> recursion = null;
  private Result<T> result = null;

  public CacheEntry(Recursion<T> recursion) {
    this.recursion = recursion;
  }

  public CacheEntry(Result<T> result) {
    this.result = result;
  }

  public boolean hasRecursion() {
    return this.recursion != null;
  }

  public Result<T> setResult(Result<T> result) {
    this.recursion = null;
    return this.result = result;
  }

  public Recursion<T> getRecursion() {
    return this.recursion;
  }

  public Result<T> getResult() {
    return this.result;
  }
}

class Recursion<T> {
  private Result<T> seed = new BasicResult<T>(null, null);
  private final Rule<T> rule;
  private Head<T> head = null;
  private final Recursion<T> next;

  public Recursion(Rule<T> rule, Recursion<T> next) {
    this.rule = rule;
    this.next = next;
  }

  public Head<T> getHead() {
    return this.head;
  }

  public void setSeed(Result<T> seed) {
    this.seed = seed;
  }

  public Result<T> getSeed() {
    return this.seed;
  }

  public Recursion<T> getNext() {
    return this.next;
  }

  /* Create a Head for this marker and walk up the stack of
   * recursion markers associating each marker with this marker's
   * head, until we reach this marker on the stack.
   * TODO: Alternately could we do this setup each time the recursions are created?
   * (or could we do some static analysis of the grammar to find recursions
   * ahead of time and use that knowledge to optimize/amortize this process)
   */
  public Recursion<T> setup(Rule<T> rule, Recursion<T> stack) {
    assert this.head == null;
    this.head = new Head<T>(rule);
    while (!this.head.equals(stack.getHead())) { // could also do this != stack
      this.head.addRule(stack.rule);
      assert stack.head == null;
      stack.head = this.head;
      stack = stack.getNext();
    }
    return this;
  }
}

class Head<T> {
  private Rule<T> rule;
  private Set<Rule<T>> involvedSet = new HashSet<Rule<T>>();
  private Set<Rule<T>> evalSet = new HashSet<Rule<T>>();

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

  public Rule<T> getRule() {
    return this.rule;
  }

  public boolean involvesRule(Rule<T> rule) {
    return this.rule.equals(rule) || this.involvedSet.contains(rule);
  }

  public void resetEvalSet() {
    this.evalSet = new HashSet<Rule<T>>(this.involvedSet);
  }

  public boolean canEval(Rule<T> rule) {
    return this.evalSet.contains(rule);
  }

  // doesn't actually eval the rule, but marks it as evaled (by removing it from the set)
  public void evalRule(Rule<T> rule) {
    this.evalSet.remove(rule);
  }
}

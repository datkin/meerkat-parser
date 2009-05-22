package meerkat.grammar.basic;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import meerkat.grammar.*;

// A class for building grammars with "low" syntactic overhead (checked at runtime)
public class UnsafeGrammarFactory<T> implements Grammar<T> {
  private Rule<T> startingRule = null;
  private final Map<Rule<T>, Expr<T>> rules = new HashMap<Rule<T>, Expr<T>>();
  private final Map<String, Rule<T>> names = new HashMap<String, Rule<T>>();

  private final Class<T> clazz;
  private final GrammarFactory<T> realFactory;

  public UnsafeGrammarFactory(Class<T> clazz) {
    if (clazz == null)
      throw new IllegalArgumentException();
    this.clazz = clazz; // we need a class object so we can do sanity checking method arguments
    this.realFactory = new GrammarFactory<T>();
  }

  public Rule<T> newRule(String name) {
    return realFactory.newRule(name);
  }

  @Override
  public Iterable<Rule<T>> getRules() {
    return realFactory.getRules();
  }

  @Override
  public Expr<T> getExpr(Rule<T> rule) {
    return realFactory.getExpr(rule);
  }

  @Override
  public Rule<T> getStartingRule() {
    return realFactory.getStartingRule();
  }

  public void setStartingRule(Rule<T> rule) {
    realFactory.setStartingRule(rule);
  }

  public Grammar<T> getGrammar() {
    return null; // return a final/sane grammar from this grammar
  }

  @SuppressWarnings("unchecked")
  public List<Expr<T>> objectsToExprs(Object... objs) {
    List<Expr<T>> exprs = new LinkedList<Expr<T>>();
    for (int i = 0; i < objs.length; i++) {
      Object obj = objs[i];
      if (clazz.isInstance(obj)) {
        exprs.add(new BasicTerminal<T>(clazz.cast(obj)));
      } else if (obj instanceof Class) {
        Class<?> matchClazz = (Class)obj;
        if (clazz.isAssignableFrom(matchClazz)) {
          exprs.add(new BasicTerminalClass<T>(matchClazz.asSubclass(clazz)));
        } else {
          // we could possibly skip the assignabledFrom check and just call "asSubclass"?
          throw new RuntimeException(matchClazz + " is not a subclass of " + clazz);
        }
      } else if (obj instanceof Expr) {
        // I don't think there's any way to check the value of the type parameter here.
        // If we really wanted safety we'd need to traverse the whole object and check
        // each field for make sure it was a T. For now we'll just cross our fingers.
        exprs.add((Expr<T>)obj);
      } else {
        throw new RuntimeException(obj + " is not a subclass of " + clazz);
      }
    }
    return exprs;
  }

  public Expr<T> listToExpr(List<Expr<T>> exprs) {
    if (exprs.size() == 1)
      return exprs.get(0);
    return new BasicSequence<T>(exprs);
  }

  // use this?
  public void setRule(Rule<T> rule, Expr<T> expr) {
    realFactory.setRule(rule, expr);
  }

  public Rule<T> setRule(Rule<T> rule, Object... objs) {
    return realFactory.seq(rule, objectsToExprs(objs));
  }

  // Sequences
  public Sequence<T> seq(Object... objs) { return realFactory.seq(objectsToExprs(objs)); }
  public Rule<T> seqRule(String name, Object... objs) { return seqRule(newRule(name), objs); }
  public Rule<T> seqRule(Rule<T> rule, Object... objs) { return realFactory.seq(rule, objectsToExprs(objs)); }

  // Choices
  public Choice<T> or(Object... objs) { return realFactory.or(objectsToExprs(objs)); }
  public Rule<T> orRule(String name, Object... objs) { return orRule(newRule(name), objs); }
  public Rule<T> orRule(Rule<T> rule, Object... objs) { return realFactory.or(rule, objectsToExprs(objs)); }

  // Optional
  public Optional<T> opt(Object... objs) { return realFactory.opt(objectsToExprs(objs)); }
  public Rule<T> optRule(String name, Object... objs) { return optRule(newRule(name), objs); }
  public Rule<T> optRule(Rule<T> rule, Object... objs) { return realFactory.opt(rule, objectsToExprs(objs)); }

  // And
  public And<T> and(Object... objs) { return realFactory.and(objectsToExprs(objs)); }
  public Rule<T> andRule(String name, Object... objs) { return andRule(newRule(name), objs); }
  public Rule<T> andRule(Rule<T> rule, Object... objs) { return realFactory.and(rule, objectsToExprs(objs)); }

  // Not
  public Not<T> not(Object... objs) { return realFactory.not(objectsToExprs(objs)); }
  public Rule<T> notRule(String name, Object... objs) { return notRule(newRule(name), objs); }
  public Rule<T> notRule(Rule<T> rule, Object... objs) { return realFactory.not(rule, objectsToExprs(objs)); }

  // ZeroOrMore
  public ZeroOrMore<T> star(Object... objs) { return realFactory.star(objectsToExprs(objs)); }
  public Rule<T> starRule(String name, Object... objs) { return starRule(newRule(name), objs); }
  public Rule<T> starRule(Rule<T> rule, Object... objs) { return realFactory.star(rule, objectsToExprs(objs)); }

  // OneOrMore
  public OneOrMore<T> plus(Object... objs) { return realFactory.plus(objectsToExprs(objs)); }
  public Rule<T> plusRule(String name, Object... objs) { return plusRule(newRule(name), objs); }
  public Rule<T> plusRule(Rule<T> rule, Object... objs) { return realFactory.plus(rule, objectsToExprs(objs)); }
}

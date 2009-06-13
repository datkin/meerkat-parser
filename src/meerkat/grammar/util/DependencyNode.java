package meerkat.grammar.util;

import java.util.Set;
import java.util.HashSet;

import meerkat.grammar.Rule;

public class DependencyNode<T> {
  private final Rule<T> rule;
  private final Set<Rule<T>> dependents = new HashSet<Rule<T>>();
  private final Set<Rule<T>> requirements = new HashSet<Rule<T>>();

  public DependencyNode(Rule<T> rule) {
    if (rule == null)
      throw new IllegalArgumentException();
    this.rule = rule;
  }

  /*
  public void addDependencies(DependencyGraph<T> graph) {
    assert graph.getRule().equals(this.rule);
    this.dependents.addAll(graph.getDependents());
    this.requirements.addAll(graph.getRequirements());
  }
  */

  public void addDependent(Rule<T> r) {
    this.dependents.add(r);
  }

  public void addRequirement(Rule<T> r) {
    this.requirements.add(r);
  }

  public Rule<T> getRule() {
    return this.rule;
  }

  public Set<Rule<T>> getDependents() {
    return this.dependents;
  }

  public Set<Rule<T>> getRequirements() {
    return this.requirements;
  }

  public boolean equals(Object obj) {
    if (obj != null && obj.getClass().equals(this.getClass())) {
      DependencyNode other = (DependencyNode)obj;
      return
        other.rule.equals(this.rule) &&
        other.requirements.equals(this.requirements) &&
        other.dependents.equals(this.dependents);
    }
    return false;
  }
}

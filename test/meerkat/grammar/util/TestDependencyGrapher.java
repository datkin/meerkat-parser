package meerkat.grammar.util;

import org.junit.Test;
import static org.junit.Assert.*;
import static meerkat.parser.ParserTester.*;

import java.util.Map;
import java.util.HashMap;

import meerkat.grammar.Grammar;
import meerkat.grammar.Rule;
import meerkat.grammar.util.DependencyGraph;
import meerkat.grammar.util.DependencyNode;

public class TestDependencyGrapher {

  @Test
  public void testSampleGrammar() {
    Grammar<String> g = getSampleGrammar();
    DependencyGraph<String> graph = new DependencyGraph<String>(g);

    DependencyGraph<String> expectedGraph = new DependencyGraph<String>();

    DependencyNode<String> number = new DependencyNode<String>(getRule(g, "Number"));
    DependencyNode<String> primary = new DependencyNode<String>(getRule(g, "Primary"));
    DependencyNode<String> multitive = new DependencyNode<String>(getRule(g, "Multitive"));
    DependencyNode<String> additive = new DependencyNode<String>(getRule(g, "Additive"));

    number.addDependent(primary.getRule());
    expectedGraph.add(number);

    primary.addRequirement(number.getRule());
    primary.addRequirement(additive.getRule());
    primary.addDependent(multitive.getRule());
    expectedGraph.add(primary);

    multitive.addRequirement(primary.getRule());
    multitive.addRequirement(multitive.getRule());
    multitive.addDependent(multitive.getRule());
    multitive.addDependent(additive.getRule());
    expectedGraph.add(multitive);

    additive.addRequirement(multitive.getRule());
    additive.addRequirement(additive.getRule());
    additive.addDependent(additive.getRule());
    additive.addDependent(primary.getRule());
    expectedGraph.add(additive);

    assertEquals(expectedGraph, graph);
  }
}

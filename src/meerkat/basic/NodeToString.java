package meerkat.basic;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import meerkat.TreeVisitor;
import meerkat.Leaf;
import meerkat.Node;
import meerkat.Tree;

// This bad boy is stateful, so you ought to use a new one each time you
// want to get a string
// Also: all this cycle checking cute. In practice it can be removed b/c
// trees are trees, not graphs, so there are no cycles. I'm keeping it
// around however as a good "reference implementation" in case I need to
// do this sort of thing somewhere else.
public class NodeToString<T, L> implements TreeVisitor<T, L, String> {
  private int indent = 0;
  private final Set<Node<T, L>> seen = new HashSet<Node<T, L>>();
  private Map<Node<T, L>, String> aliases = null;

  @Override
  public String visit(Tree<T, L> tree) {
    if (aliases == null)
      aliases = new MakeAliases<T, L>().visit(tree);
    if (aliases.containsKey(tree)) {
      if (seen.contains(tree)) {
        // can assume indent is non zero here (and therefore always include the newline)
        return indent() + "#" + aliases.get(tree) + "\n";
      } else {
        seen.add(tree);
      }
    }
    StringBuilder sb = new StringBuilder();
    sb.append(indent()).append("Tree");
    if (aliases.containsKey(tree))
      sb.append("#").append(aliases.get(tree));
    sb.append(":").append(tree.getValue()).append("[");
    indent++;
    for (Node<T, L> n : tree.getNodes()) {
      sb.append("\n");
      sb.append(n.accept(this));
    }
    indent--;
    sb.append(indent()).append("]");
    return sb.toString();
  }

  @Override
  public String visit(L leaf) {
    if (indent == 0)
      return "Leaf:" + leaf;
    return indent() + "Leaf:" + leaf + "\n";
  }

  private String indent() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < indent; i++)
      sb.append("  ");
    return sb.toString();
  }

  // Build a list of aliases for all nodes that are part of a cycle.
  // Once this list is built, we can reference it when building the string representation
  // so we know where to insert aliases (we cannot built the string until we've completely
  // traversed the tree (graph) to find all the cycles).
  private static class MakeAliases<T, L> implements TreeVisitor<T, L, Map<Node<T, L>, String>> {
    private int count = 0;
    private final Set<Node<T, L>> seen = new HashSet<Node<T, L>>();
    private final Map<Node<T, L>, String> aliases = new HashMap<Node<T, L>, String>();

    @Override
    public Map<Node<T, L>, String> visit(Tree<T, L> tree) {
      if (seen.contains(tree) && !aliases.containsKey(tree)) {
        aliases.put(tree, String.valueOf(count));
        count++;
        return aliases;
      }
      seen.add(tree);
      for (Node<T, L> n : tree.getNodes()) {
        n.accept(this);
      }
      return aliases;
    }

    @Override
    public Map<Node<T, L>, String> visit(L leaf) {
      return aliases;
    }
  }
}

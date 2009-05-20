package meerkat.util;

import org.junit.Test;

import java.util.List;
import java.util.LinkedList;

import meerkat.Leaf;
import meerkat.Node;
import meerkat.Tree;

import meerkat.basic.BasicLeaf;
import meerkat.basic.BasicNode;
import meerkat.basic.BasicTree;

public class TestNodeChecker {

  Node<String, String> empty = new BasicTree<String, String>("empty", new LinkedList<Node<String, String>>());

  Node<String, String> leaf = new BasicLeaf<String, String>("leaf");
  Node<String, String> leaf1 = new BasicLeaf<String, String>("leaf1");
  Node<String, String> leaf2 = new BasicLeaf<String, String>("leaf2");
  Node<String, String> leaf3 = new BasicLeaf<String, String>("leaf3");

  @Test
  public void leaf() {
    NodeChecker<String, String> nc = new NodeChecker<String, String>(leaf);
    leaf.accept(nc);
    leaf.accept(nc); // maintain state
    //empty.accept(nc);
    //leaf1.accept(nc);
  }

  @Test
  public void simpleTree() {
    NodeChecker<String, String> nc = new NodeChecker<String, String>(
      new BasicTree<String, String>("simple", new LinkedList<Node<String, String>>() {{
        add(leaf1);
        add(leaf2);
        add(leaf3);
      }})
    );

    Node<String, String> verify = new BasicTree<String, String>("simple", new LinkedList<Node<String, String>>() {{
      add(leaf1);
      add(leaf2);
      add(leaf3);
    }});
    verify.accept(nc);

    //leaf.accept(nc);

    Node<String, String> diff1 = new BasicTree<String, String>("broken", new LinkedList<Node<String, String>>() {{
      add(leaf1);
      add(leaf2);
      add(leaf3);
    }});
    //diff1.accept(nc);

    Node<String, String> diff2 = new BasicTree<String, String>("simple", new LinkedList<Node<String, String>>() {{
      add(leaf1);
      add(leaf2);
      add(leaf3);
      add(leaf3);
    }});
    //diff2.accept(nc);

    Node<String, String> diff3 = new BasicTree<String, String>("simple", new LinkedList<Node<String, String>>() {{
      add(leaf1);
      add(leaf2);
    }});
    //diff3.accept(nc);

    Node<String, String> diff4 = new BasicTree<String, String>("simple", new LinkedList<Node<String, String>>() {{
      add(leaf1);
      add(leaf3);
      add(leaf2);
    }});
    //diff4.accept(nc);

  }
}

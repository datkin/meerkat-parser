package meerkat;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestInterfaces {

  @Test
  public void simple() {
    System.out.println((new ComplexToken("t")).accept(new TokenVisitor()));
  }

}

class TokenVisitor implements TreeVisitor<Token, String> {
  @Override
  public String visit(Result<Token> r) {
    StringBuilder sb = new StringBuilder();
    for (Node<Token> n : r.getNodes()) {
      sb.append(n.accept(this));
    }
    return sb.toString();
  }

  @Override
  public String visit(Token t) {
    return t.getValue();
  }
}

interface Token extends Node<Token> {
  public String getValue();
}

class SimpleToken implements Token {
  private final String value;

  public SimpleToken(String value) {
    this.value = value;
  }

  @Override
  public <V> V accept(TreeVisitor<Token, V> tv) {
    return tv.visit(this);
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && obj.getClass().equals(this.getClass())
      && ((SimpleToken)obj).getValue().equals(this.getValue());
  }

  public String getValue() {
    return this.value;
  }
}

class ComplexToken implements Token {
  private final String value;

  public ComplexToken(String value) {
    this.value = value;
  }

  @Override
  public <V> V accept(TreeVisitor<Token, V> tv) {
    return tv.visit(this);
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && obj.getClass().equals(this.getClass())
      && ((ComplexToken)obj).getValue().equals(this.getValue())
      && ((ComplexToken)obj).getName().equals(this.getName());
  }

  public String getValue() {
    return this.value;
  }

  public String getName() {
    return "Foo";
  }
}

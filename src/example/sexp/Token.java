package example.sexp;

public enum Token {
  Space,
  Letter,
  Open {
    @Override
    public String toString() { return "'('"; }
  },
  Close {
    @Override
    public String toString() { return "')'"; }
  }
}

package meerkat.parser;

interface EngineFactory<T> {
  public Engine<T> newEngine(Parser<T> parser);
}

class BaseEngineFactory<T> implements EngineFactory<T> {
  public Engine<T> newEngine(Parser<T> parser) {
    return new BaseEngine<T>(parser);
  }
}

class CachedEngineFactory<T> implements EngineFactory<T> {
  public Engine<T> newEngine(Parser<T> parser) {
    return new CachedEngine<T>(parser);
  }
}

package tyRuBa.util;

public interface Action {
  /** Excute action on an argument and return some kind of result */
  abstract public Object compute(Object arg);
}

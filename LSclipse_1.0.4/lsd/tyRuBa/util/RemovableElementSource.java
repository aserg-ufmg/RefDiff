package tyRuBa.util;

public abstract class RemovableElementSource extends ElementSource {

  /** Conveninet if you want to know beforehand what you will remove */
  public abstract Object peekNextElement();

  /** Remove the next element from the Collection these elements are
    coming from */
  public abstract void removeNextElement();

}

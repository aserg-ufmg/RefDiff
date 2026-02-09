package pkg;

public sealed interface Sealed permits SealedImpl, SealedChild {
    void doSomethingSealed();
}

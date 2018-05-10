package refdiff.parsers.java;

public enum Visibility {

    PUBLIC,
    PRIVATE,
    PROTECTED,
    PACKAGE;
    
    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
    
}

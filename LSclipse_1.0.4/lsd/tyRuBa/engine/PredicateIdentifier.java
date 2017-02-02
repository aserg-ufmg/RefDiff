package tyRuBa.engine;

/**
 * A PredicateIdentifier stores a predicate's name and its arity
 */
public class PredicateIdentifier extends Identifier {

    public PredicateIdentifier(String name, int arity) {
        super(name, arity);
    }
}

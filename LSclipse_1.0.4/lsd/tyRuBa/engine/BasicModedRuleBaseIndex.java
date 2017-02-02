package tyRuBa.engine;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import tyRuBa.modes.ConstructorType;
import tyRuBa.modes.CompositeType;
import tyRuBa.modes.Factory;
import tyRuBa.modes.Free;
import tyRuBa.modes.Mode;
import tyRuBa.modes.PredInfo;
import tyRuBa.modes.PredicateMode;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeConstructor;
import tyRuBa.modes.TypeMapping;
import tyRuBa.modes.TypeModeError;

/**
 * A BasicModedRuleBaseIndex stores ModedRuleBaseCollections in a hashmap.
 * The PredicateIdentifier of each ModedRuleBaseCollection is used as the key in the hashmap.
 */
public class BasicModedRuleBaseIndex extends ModedRuleBaseIndex {
	
	QueryEngine engine;
	String identifier;

	/** All the ground facts go in here, no separation based on modes! */
	TypeInfoBase typeInfoBase;

	/** Index of rulebases for particular predicates and modes.
	 * 	forms. Keys used for indexing are composed of the predicate name with arity
	 *  of an expression and its predicate mode */
	HashMap index = new HashMap();

	/**	
	 * @codegroup metadata
	 */
	public void enableMetaData() {
		typeInfoBase.enableMetaData(engine);
	}

	public BasicModedRuleBaseIndex(QueryEngine qe, String identifier) {     
		this.engine = qe;
		this.identifier = identifier;
		this.typeInfoBase = new TypeInfoBase(identifier);
	}

	/** return an array of ModedRuleBase corresponding to the predicate name */
	protected ModedRuleBaseCollection getModedRuleBases(PredicateIdentifier predID)
	throws TypeModeError {
		ModedRuleBaseCollection result = maybeGetModedRuleBases(predID);
		if (result == null) {
			throw new TypeModeError("Unknown predicate " + predID);
		} else {
			return result;
		}
	}

	public ModedRuleBaseCollection maybeGetModedRuleBases(PredicateIdentifier predID) {
		PredInfo pInfo = typeInfoBase.maybeGetPredInfo(predID);
		if (pInfo == null) {
			return null;
		}

		int numPredicateMode = pInfo.getNumPredicateMode();
		if (numPredicateMode == 0) {
			throw new Error("there are no mode declarations for " + predID);
		}
		return (ModedRuleBaseCollection) index.get(predID);
	}

	public void dumpFacts(PrintStream out) {
		for (Iterator iter = index.values().iterator(); iter.hasNext();) {
			ModedRuleBaseCollection element = (ModedRuleBaseCollection) iter.next();
			element.dumpFacts(out);
		}
	}

	public void insert(PredInfo p) throws TypeModeError {
		typeInfoBase.insert(p);
		ModedRuleBaseCollection rulebases = new ModedRuleBaseCollection(engine, p, identifier);
		index.put(p.getPredId(), rulebases);
	}
	
	public void addTypePredicate(TypeConstructor TypeConstructor, ArrayList subtypes) {
		RBRule typeRule = null;
		PredicateIdentifier typePred = new PredicateIdentifier(TypeConstructor.getName(), 1);
		RBTuple args = new RBTuple(RBVariable.make("?arg"));
		RBDisjunction cond = new RBDisjunction();
		PredInfo typePredInfo = Factory.makePredInfo(engine,TypeConstructor.getName(),
			Factory.makeTupleType(Factory.makeAtomicType(TypeConstructor)));
		typePredInfo.addPredicateMode(Factory.makeAllBoundMode(1));
		PredicateMode freeMode = Factory.makePredicateMode(
			Factory.makeBindingList(Free.the), Mode.makeNondet());

		boolean hasFreeMode = true;
		for (int i = 0; i < subtypes.size(); i++) {
			String currTypeConstructorName = ((TypeConstructor) subtypes.get(i)).getName();;
			try {
				PredInfo currSubTypePredInfo =
					getPredInfo(new PredicateIdentifier(currTypeConstructorName, 1));
				hasFreeMode = hasFreeMode && currSubTypePredInfo.getNumPredicateMode() > 1;
			} catch (TypeModeError e) {
				throw new Error("This should not happen");
			}
			RBExpression currExp = new RBPredicateExpression(
				new PredicateIdentifier(currTypeConstructorName, 1),
				new RBTuple(RBVariable.make("?arg")));
			cond.addSubexp(currExp);
		}
		if (hasFreeMode) {
			typePredInfo.addPredicateMode(freeMode);
		}

		if (subtypes.size() == 1) {
			typeRule = new RBRule(typePred, args, cond.getSubexp(0));
		} else if (subtypes.size() > 1) {
			typeRule = new RBRule(typePred, args, cond);
		}

		try {
			insert(typePredInfo);
			if (typeRule != null) {
				insert(typeRule);
			}
		} catch (TypeModeError e) {
			throw new Error("This should not happen",e);
		}
		
	}
	
	protected void basicAddTypeConst(TypeConstructor t) {
		typeInfoBase.addTypeConst(t);
	}
	
	public void addFunctorConst(Type repAs, CompositeType type) {
		typeInfoBase.addFunctorConst(repAs, type);
	}

	public void addTypeMapping(TypeMapping mapping, FunctorIdentifier id) throws TypeModeError {
		typeInfoBase.addTypeMapping(id, mapping);
	}

    public TypeMapping findTypeMapping(Class forWhat) {
        return typeInfoBase.findTypeMapping( forWhat) ;
    }

	public PredInfo maybeGetPredInfo(PredicateIdentifier predId) {
		return typeInfoBase.maybeGetPredInfo(predId);
	}

	public boolean contains(PredicateIdentifier p) {
		PredInfo result = typeInfoBase.maybeGetPredInfo(p);
		return result != null;
	}
	
	public TypeConstructor findType(String typeName) {
		return typeInfoBase.findType(typeName);
	}
	
	public TypeConstructor findTypeConst(String typeName, int arity) {
		return typeInfoBase.findTypeConst(typeName, arity);
	}
	
	public ConstructorType findConstructorType(FunctorIdentifier id) {
		return typeInfoBase.findConstructorType(id);
	}

	public void clear() {
		typeInfoBase.clear();
		index = new HashMap();
	}

    public void backup() {
        for (Iterator iter = index.values().iterator(); iter.hasNext();) {
            ModedRuleBaseCollection coll = (ModedRuleBaseCollection) iter.next();
            coll.backup();            
        }
    }
    
    public String toString() {
    		return getClass().getName()+"("+identifier+")";
    }

}

package tyRuBa.engine;

import java.io.PrintStream;
import java.util.ArrayList;

import tyRuBa.modes.CompositeType;
import tyRuBa.modes.ConstructorType;
import tyRuBa.modes.PredInfo;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeConstructor;
import tyRuBa.modes.TypeMapping;
import tyRuBa.modes.TypeModeError;

/**
 * A BucketModedRuleBaseIndex has a local and a global BasicModedRuleBaseIndex.
 * All facts and rules that are inserted into this rulebase index will first be
 * inserted into the local rulebase index. If that fails (the predicate identifier
 * is not declared locally index), then they are inserted into the global
 * rulebase index.
 */
public class BucketModedRuleBaseIndex extends ModedRuleBaseIndex {

	BasicModedRuleBaseIndex localRuleBase;
	BasicModedRuleBaseIndex globalRuleBase;
	
	/**	
	 * @codegroup metadata
	 */
	public void enableMetaData() {
		localRuleBase.enableMetaData();
		globalRuleBase.enableMetaData();
	}

	public BucketModedRuleBaseIndex(QueryEngine qe, String identifier, BasicModedRuleBaseIndex globalRuleBase) {
		this.localRuleBase = new BasicModedRuleBaseIndex(qe, identifier);
		this.globalRuleBase = globalRuleBase;
	}

	protected ModedRuleBaseCollection getModedRuleBases(PredicateIdentifier predID)
	throws TypeModeError {
		ModedRuleBaseCollection result = localRuleBase.maybeGetModedRuleBases(predID);
		if (result == null) {
			return globalRuleBase.getModedRuleBases(predID);
		} else {
			return result;
		}
	}

	public void insert(PredInfo p) throws TypeModeError {
		if (globalRuleBase.contains(p.getPredId()))
			throw new TypeModeError("Duplicate mode/type entries for predicate "
				+ p.getPredId());
		localRuleBase.insert(p);
	}

	public void dumpFacts(PrintStream out) {
		out.print("local facts: ");
		localRuleBase.dumpFacts(out);
		out.print("global facts: ");
		globalRuleBase.dumpFacts(out);
	}

	public PredInfo maybeGetPredInfo(PredicateIdentifier predId) {
		PredInfo result = localRuleBase.maybeGetPredInfo(predId);
		if (result == null) {
			return globalRuleBase.maybeGetPredInfo(predId);
		} else {
			return result;
		}
	}
	
	public void addTypePredicate(TypeConstructor TypeConstructor, ArrayList subtypes) {
		localRuleBase.addTypePredicate(TypeConstructor, subtypes);
	}
		
	protected void basicAddTypeConst(TypeConstructor t) {
		localRuleBase.basicAddTypeConst(t);
	}
	
	public void addFunctorConst(Type repAs, CompositeType type) {
		localRuleBase.addFunctorConst(repAs, type);
	}

	public void addTypeMapping(TypeMapping mapping, FunctorIdentifier id) throws TypeModeError {
	    localRuleBase.addTypeMapping(mapping, id);
	}	

	public TypeConstructor findType(String typeName) {
		TypeConstructor result;
		result = localRuleBase.findType(typeName);
		if (result == null) {
			result = globalRuleBase.findType(typeName);
		}
		return result;
	}

	public TypeConstructor findTypeConst(String typeName, int arity) {
		TypeConstructor result;
		result = localRuleBase.typeInfoBase.findTypeConst(typeName, arity);
		if (result != null) {
			return result;
		} else {
			return globalRuleBase.typeInfoBase.findTypeConst(typeName, arity);
		}
	}
	
	public ConstructorType findConstructorType(FunctorIdentifier id) {
		ConstructorType result;
		result = localRuleBase.typeInfoBase.findConstructorType(id);
		if (result != null) {
			return result;
		} else {
			return globalRuleBase.typeInfoBase.findConstructorType(id);
		}
	}

    public TypeMapping findTypeMapping(Class forWhat) {
        TypeMapping result;
		result = localRuleBase.typeInfoBase.findTypeMapping(forWhat);
		if (result != null) {
			return result;
		} else {
			return globalRuleBase.typeInfoBase.findTypeMapping(forWhat);
		}
	}
    
    public void clear() {
		localRuleBase.clear();
	}

    /**
     * @see tyRuBa.engine.ModedRuleBaseIndex#backup()
     */
    public void backup() {
        globalRuleBase.backup();
        localRuleBase.backup();
    }

//	public void update() {
//		globalRuleBase.update();
//		localRuleBase.update();
//	}
}

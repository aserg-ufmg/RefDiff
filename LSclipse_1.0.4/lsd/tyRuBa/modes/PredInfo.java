/*
 * Created on May 20, 2003
 */
package tyRuBa.modes;

import java.util.ArrayList;
import java.util.HashMap;

import tyRuBa.engine.PredicateIdentifier;
import tyRuBa.engine.QueryEngine;
import tyRuBa.engine.factbase.FactBase;
import tyRuBa.engine.factbase.SimpleArrayListFactBase;
import tyRuBa.engine.factbase.hashtable.HashTableFactBase;

/**
 * PredInfo stores information for a predicate.  It stores its name, its list of types, 
 * and its corresponding PredicateModes.
 * 
 * @category FactBase
 */
public class PredInfo {

    private QueryEngine engine; 
           // is null for Native predicates (not associated with a specific
           // engine
	private PredicateIdentifier predId;
	private TupleType tList;
	private ArrayList/*<PredicateMode>*/ predModes;
    private FactBase factbase;
    private boolean isPersistent;

	/** Constructor */
    public PredInfo(QueryEngine qe, String predName, TupleType tList, ArrayList/*<PredicateMode>*/ predModes, boolean isPersistent) {
		engine = qe;
	    predId = new PredicateIdentifier(predName, tList.size());
		this.tList = tList;
		this.predModes = predModes;
		this.isPersistent = isPersistent;
    }
    
	public PredInfo(QueryEngine qe, String predName, TupleType tList, ArrayList/*<PredicateMode>*/ predModes) {
	    this(qe, predName, tList, predModes, false);
	}
	
	public PredInfo(QueryEngine qe, String predName, TupleType tList) {
		this(qe, predName, tList, new ArrayList/*<PredicateMode>*/());
	}

	public void addPredicateMode(PredicateMode pm) {
		predModes.add(pm);
	}

	public PredicateIdentifier getPredId() {
		return predId;
	}

	public TupleType getTypeList() {
		return (TupleType) tList.clone(new HashMap());
	}

	public int getNumPredicateMode() {
		return predModes.size();
	}

	public PredicateMode getPredicateModeAt(int pos) {
		return (PredicateMode) predModes.get(pos);
	}

	public int hashCode() {
		return predId.hashCode();
	}

	public String toString() {
		StringBuffer result = new StringBuffer(
			predId.toString() + tList + "\nMODES\n");

		int size = predModes.size();
		for (int i = 0; i < size; i++) {
			result.append(predModes.get(i) + "\n");
		}
		return result.toString();
	}
	
	public FactBase getFactBase() {
	    if (factbase == null && (predId.getArity() == 0 || engine == null)) {
	        //use the memory factbase if there are no arguments to the predicate
	        factbase = new SimpleArrayListFactBase(this);
	    } else if (factbase == null) {
	        if (isPersistent) {
	            factbase = new HashTableFactBase(this);
	        } else {
	            factbase = new SimpleArrayListFactBase(this);
	    	}
	    }	        
	    return factbase;
	}
	
	public QueryEngine getQueryEngine() {
	    return engine;
	}

    public boolean isPersistent() {
        return isPersistent;
    }

    public int getArity() {
        return getPredId().getArity();
    }
}

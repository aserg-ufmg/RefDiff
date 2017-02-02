/*****************************************************************\
 * File:        Factory.java
 * Author:      TyRuBa
 * Meta author: Kris De Volder <kdvolder@cs.ubc.ca>
\*****************************************************************/
package tyRuBa.modes;

import java.util.ArrayList;

import tyRuBa.engine.ModedRuleBaseIndex;
import tyRuBa.engine.QueryEngine;
import tyRuBa.engine.RBExpression;
import tyRuBa.engine.RBPredicateExpression;
import tyRuBa.engine.RuleBase;

public class Factory {

	static public Bound makeBound() {
		return Bound.the;
	}

	static public Free makeFree() {
		return Free.the;
	}

	public static BindingMode makePartiallyBound() {
		return PatiallyBound.the;
	}

	static public Type makeAtomicType(TypeConstructor typeConstructor) {
		return typeConstructor.apply(Factory.makeTupleType(), false);
	}
	
	public static Type makeStrictAtomicType(TypeConstructor typeConstructor) {
        return typeConstructor.applyStrict(Factory.makeTupleType(), false);
	}
	
	public static Type makeSubAtomicType(TypeConstructor typeConstructor) {
		return typeConstructor.apply(Factory.makeTupleType(), true);
	}

	static public TVar makeTVar(String name) {
		return new TVar(name);
	}
	
	static public PredicateMode makePredicateMode(BindingList paramModes,
	Mode mode) {
		return makePredicateMode(paramModes, mode, true);
	}

	static public PredicateMode makePredicateMode(BindingList paramModes, Mode mode,
	boolean toBeChecked) {
		if (paramModes.isAllBound()) {
			return new PredicateMode(paramModes, Mode.makeSemidet(), false);
		} else {
			return new PredicateMode(paramModes, mode, toBeChecked);
		}
	}
	
	public static PredicateMode makeAllBoundMode(int size) {
		BindingList bounds = new BindingList();
		for (int i = 0; i < size; i++) {
			bounds.add(makeBound());
		}
		return new PredicateMode(bounds, Mode.makeSemidet(), false);
	}

	public static PredicateMode makePredicateMode(String paramModesString, 
	String modeString) {
		BindingList paramModes = Factory.makeBindingList();
		
		for (int i = 0; i < paramModesString.length(); i++) {
			char currChar = paramModesString.charAt(i);
			if (currChar == 'b' || currChar == 'B') {
				paramModes.add(Factory.makeBound());
			} else if (currChar == 'f' || currChar == 'F'){
				paramModes.add(Factory.makeFree());
			}
			else {
				throw new Error("unknown binding mode " + currChar);
			}
		}
		
		Mode mode = Mode.convertFromString(modeString);
		
		return makePredicateMode(paramModes, mode);
	}

	static public TupleType makeTupleType() {
		return new TupleType();
	}

	/** List with one element only */
	public static TupleType makeTupleType(Type t) {
		return new TupleType(new Type[] {t});
	}

	public static TupleType makeTupleType(Type t1, Type t2) {
		return new TupleType(new Type[] {t1, t2});
	}

	public static TupleType makeTupleType(Type t1, Type t2, Type t3) {
		return new TupleType(new Type[] {t1, t2, t3});
	}
	
	public static TupleType makeTupleType(Type t1, Type t2, Type t3, Type t4) {
		return new TupleType(new Type[] {t1, t2, t3, t4});
	}

	static public BindingList makeBindingList() {
		return new BindingList();
	}
	
	static public BindingList makeBindingList(BindingMode bm) {
		return new BindingList(bm);
	}

    public static BindingList makeBindingList(int repeat, BindingMode bm) {
        BindingList result = makeBindingList();
        for (int i = 0; i < repeat; i++) {
            result.add(bm);
        }
        return result;
    }

	static public PredInfo makePredInfo(QueryEngine engine, String predName, TupleType tList) {
		return new PredInfo(engine, predName, tList);
	}
	
	static public PredInfo makePredInfo(QueryEngine engine, String predName, TupleType tList, 
	ArrayList predModes, boolean isPersistent) {
		if (predModes.isEmpty()) {
			predModes.add(makeAllBoundMode(tList.size()));
		}
		return new PredInfo(engine,predName, tList, predModes, isPersistent);
	}
	
	static public PredInfo makePredInfo(QueryEngine engine, String predName, TupleType tList, 
	ArrayList predModes) {
	    return makePredInfo(engine, predName, tList, predModes, false);
	}

	public static Type makeListType(Type et) {
		return new ListType(et);
	}
	public static ListType makeEmptyListType() {
		return new ListType();
	}
	
	public static ModeCheckContext makeModeCheckContext(ModedRuleBaseIndex rulebases) {
		return new ModeCheckContext(new BindingEnv(), rulebases);
	}
	
//	public static ModeCheckContext makeModeCheckContext(
//	ModedRuleBaseIndex rulebases, ModedRuleBase rb) {
//		return new ModeCheckContext(new BindingEnv(), rulebases, rb);
//	}
	
	public static TypeEnv makeTypeEnv() {
		return new TypeEnv();
	}

	public static RBExpression makeModedExpression(RBExpression exp, Mode mode, 
	ModeCheckContext context) {
		return exp.makeModed(mode,context);
	}

    public static RBExpression makeModedExpression(RBPredicateExpression exp, Mode resultMode, ModeCheckContext resultContext, RuleBase bestRuleBase) {
        return exp.makeModed(resultMode, resultContext, bestRuleBase);
    }

    
//	public static ModedExpression makeModedExpression(RBExpression exp, Mode mode,
//	ModeCheckContext context, float percentFree) {
//		return new ModedExpression(exp, mode, context, percentFree);
//	}
//
	public static TypeConstructor makeTypeConstructor(Class javaclass) {
		return new JavaTypeConstructor(javaclass);
	}
	
	public static TypeConstructor makeTypeConstructor(String name, int arity) {
		return new UserDefinedTypeConstructor(name, arity);
	}


}

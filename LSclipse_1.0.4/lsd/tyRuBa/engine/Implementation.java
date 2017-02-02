package tyRuBa.engine;

import java.util.ArrayList;

import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.engine.compilation.SemiDetCompiled;
import tyRuBa.modes.BindingList;
import tyRuBa.modes.Factory;
import tyRuBa.modes.Mode;
import tyRuBa.modes.ModeCheckContext;
import tyRuBa.modes.Multiplicity;
import tyRuBa.modes.PredInfoProvider;
import tyRuBa.modes.PredicateMode;
import tyRuBa.modes.TupleType;
import tyRuBa.modes.TypeModeError;
import tyRuBa.util.ElementSource;

/**
 * An Implementation is used in defining NativePredicates; it implements
 * how results are evaluated in a certain predicate mode 
 */

public abstract class Implementation extends RBComponent {
	
	private PredicateMode mode;
	private ArrayList solutions = null;
	private ArrayList arguments;
	private ArrayList results;
	private RBTuple argsAndResults;
		
	/** calculates result and calls addSolution to store the results */
	public abstract void doit(RBTerm[] args);
	
	public Implementation(String paramModesString, String modeString) {
		mode = Factory.makePredicateMode(paramModesString, modeString);
		BindingList bindings = mode.getParamModes();
		ArrayList argsAndRes = new ArrayList();
		arguments = new ArrayList();
		results = new ArrayList();
		for (int i = 0; i < bindings.size(); i++) {
			RBTerm curr = FrontEnd.makeUniqueVar("arg" + i);
			argsAndRes.add(curr);
			if (bindings.get(i).isBound()) {
				arguments.add(curr); 
			} else {
				results.add(curr);
			}
		}		
		argsAndResults = RBTuple.make(argsAndRes);
	}
		
	public RBTerm getArgAt(int i) {
		return (RBTerm) arguments.get(i);
	}
	
	public int getNumArgs() {
		return arguments.size();
	}
	
	public RBTerm getResultAt(int i) {
		return (RBTerm) results.get(i);
	}
	
	public PredicateMode getPredicateMode() {
		return mode;
	}

	public Mode getMode() {
		return getPredicateMode().getMode();
	}

	public BindingList getBindingList() {
		return getPredicateMode().getParamModes();
	}
	
	public PredicateIdentifier getPredId() {
		throw new Error("This should not happen");
	}
	
	public RBTuple getArgs() {
		return argsAndResults;
	}
	
	public void addSolution() {
		solutions.add(new RBTerm[0]);
	}

	public void addSolution(Object o) {
		solutions.add(new RBTerm[] {RBCompoundTerm.makeJava(o)} );
	}

	public void addSolution(Object o1, Object o2) {
		solutions.add(new RBTerm[] {RBCompoundTerm.makeJava(o1), RBCompoundTerm.makeJava(o2)});
	}

	public void addTermSolution(RBTerm t) {
		solutions.add(new RBTerm[] {t} );
	}

	public void addTermSolution(RBTerm t1, RBTerm t2) {
		solutions.add(new RBTerm[] {t1,t2} );
	}
		
	public TupleType typecheck(PredInfoProvider predinfo) throws TypeModeError {
		throw new Error("This should not happen");
	}
	
	public RBComponent convertToMode(PredicateMode mode, ModeCheckContext context)
	throws TypeModeError {
		if (mode.equals(getPredicateMode())) {
			return this;
		} else {
			throw new Error("This should not happen");
		}
	}

	public ArrayList eval(RBContext rb, final Frame f, final Frame callFrame) {
		solutions = new ArrayList();
		RBTerm[] args = new RBTerm[getNumArgs()];
		for (int i = 0; i < getNumArgs(); i++) {
			args[i] = getArgAt(i).substitute(f);
		}
		doit(args);
		ArrayList results = new ArrayList();
		for (int i = 0; i < solutions.size(); i++) {
			Frame result = (Frame) f.clone();
			RBTerm[] sols = (RBTerm[]) solutions.get(i);
			for(int j = 0; j < sols.length; j++) {
				result = getResultAt(j).substitute(result).unify(sols[j], result);
				if (result == null) {
					j = sols.length;
				}
			}
			if (result != null) {
				results.add(callFrame.callResult(result));
			}
		}
		return results;
	}
	
	public String toString() {
		return "Implementation in mode: " + mode;
	}
	
	public Compiled compile(CompilationContext c) {
		if (getMode().hi.compareTo(Multiplicity.one) <= 0) {
			return new SemiDetCompiled() {
				public Frame runSemiDet(Object input, RBContext context) {
					Frame callFrame = new Frame();
					RBTuple goal = 
						(RBTuple) ((RBTuple)input).instantiate(callFrame);
					final Frame fc = goal.unify(getArgs(), new Frame());
					ArrayList results = eval(context, fc, callFrame);
					if (results.size() == 0) {
						return null;
					} else {
						return (Frame)results.get(0);
					}
				}
			};
		} else {
			return new Compiled(getMode()) {
				public ElementSource runNonDet(Object input, RBContext context) {
					final Frame callFrame = new Frame();
					final RBTuple goal = 
						(RBTuple) ((RBTuple)input).instantiate(callFrame);
					final Frame fc = goal.unify(getArgs(), new Frame());
					ArrayList results = eval(context, fc, callFrame);
					if (results == null) {
						return ElementSource.theEmpty;
					} else {
						return ElementSource.with(results);
					}
				}
			};
		}
	}

}

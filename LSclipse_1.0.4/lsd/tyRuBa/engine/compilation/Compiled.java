package tyRuBa.engine.compilation;

import tyRuBa.engine.*;
import tyRuBa.engine.Frame;
import tyRuBa.modes.Mode;
import tyRuBa.modes.Multiplicity;
import tyRuBa.util.Action;
import tyRuBa.util.ElementSource;

/**
 * This is what you get when an Expression is Compiled.
 * It is a little piece of machinery that takes an ElementSource
 * of Frames as its input and produces an ElementSource of Frames
 * as its output.
 * 
 * A number of additional abstract subclasses provide specific 
 * ways to implement the run method depending on the mode.
 */
public abstract class Compiled {
	
	private final Mode mode;
//	private final Action nondetAction = new Action() {
//		public Object compute(Object arg) {
//			return runNonDet(arg);
//		}
//	};
	
	public Compiled(Mode mode) {
		this.mode = mode;
	}
	
	public Mode getMode() {
		return mode;
	}

	public static Compiled succeed = new SemiDetCompiled(Mode.makeDet()) {
		public ElementSource run(ElementSource in) {
			return in;
		}
		public Frame runSemiDet(Object input, RBContext context) {
//			PoormansProfiler.countSingletonsFromSucceed++;
			return new Frame();
		}
		public Compiled conjoin(Compiled other) {
			return other;
		}
		public Compiled disjoin(Compiled other) {
			return this;
		}
		public Compiled negate() {
			return Compiled.fail;
		}
		public String toString() {
			return "SUCCEED";
		}
	};

	public static Compiled fail = new SemiDetCompiled(Mode.makeFail()) {
		public ElementSource run(ElementSource in) {
			return ElementSource.theEmpty;
		}
		public Frame runSemiDet(Object input, RBContext context) {
			return null;
		}
		public Compiled conjoin(Compiled other) {
			return this;
		}
		public Compiled disjoin(Compiled other) {
			return other;
		}
		public Compiled negate() {
			return Compiled.succeed;
		}
		public String toString() {
			return "FAIL";
		}
	};

	/** 
	 * All compiled Expressions must override at least one
	 * of run(ElementSource) or run(Object)
	 */
	public ElementSource run(ElementSource inputs, final RBContext context) {
		return inputs.map(new Action() {
			public Object compute(Object arg) {
				return runNonDet(arg, context);
			}
		}).flatten();
	}

	/** 
	 * All compiled Expressions must override at least one
	 * of run(ElementSource) or run(Object).
	 */
	public abstract ElementSource runNonDet(Object input, RBContext context);

	/** 
	 * Called by a client to start a compiled. Subclasses may overrride
	 * with an equivalent but more efficient version.
	 */
	public ElementSource start(Frame putMap) {
		return runNonDet(putMap, new RBContext());
	}

	/** Default implementation... can do better in subclasses for sure */
	public Compiled conjoin(Compiled other) {
		if (other instanceof SemiDetCompiled) {
			return new CompiledConjunction_Nondet_Semidet(
				this, (SemiDetCompiled)other);
		} else {
			return new CompiledConjunction(this, other);
		}
 	}
 	
	/** Default implementation... can do better in subclasses for sure */
	public Compiled disjoin(Compiled other) {
		if (other.equals(fail)) {
			return this;
		} else if (other instanceof SemiDetCompiled) {
			return other.disjoin(this);
		} else {
			return new CompiledDisjunction(this, other);
		}
	}
	
	/** Default implementation... can do better in subclasses for sure */
	public SemiDetCompiled first() {
//		if (this.mode.hi.compareTo(Multiplicity.one) <= 0)
//			return this;
//		else 
//		PoormansProfiler.firstIsCalled(this);
		return new CompiledFirst(this);
	}
	
	/** Default implementation... can do better in subclasses for sure */
	public Compiled negate() {
		return new CompiledNot(this);
	}
	
	public Compiled test() {
		return new CompiledTest(this);
	}

	public static Compiled makeCachedRuleBase(Compiled compiledRules) {
		if (compiledRules.getMode().hi.compareTo(Multiplicity.one)<=0) {
//			PoormansProfiler.countSemiDetCachedRB++;
			return new SemiDetCachedRuleBase((SemiDetCompiled)compiledRules);
		} else {
//			PoormansProfiler.countCachedRB++;
			return new CachedRuleBase(compiledRules);
		}
	}

//	/** So that a Compiled can be used as an Action for mapping on ElementSources */
//	public Object compute(Object arg) {
//		return runNonDet(arg);
//	}

}

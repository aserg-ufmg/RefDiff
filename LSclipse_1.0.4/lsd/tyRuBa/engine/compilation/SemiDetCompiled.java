package tyRuBa.engine.compilation;

import tyRuBa.engine.Frame;
import tyRuBa.engine.RBContext;
import tyRuBa.modes.Mode;
import tyRuBa.util.Action;
import tyRuBa.util.ElementSource;

/**
 * If some frame manipulating machinery is "SemiDetCompiled"
 * it means that it produces at most one frame of
 * output for each frame of input. Thus it has a specific
 * run method that takes just one frame and returns one
 * frame or null.
 */
public abstract class SemiDetCompiled extends Compiled {

	public SemiDetCompiled(Mode mode) {
		super(mode);
	}

	public SemiDetCompiled() {
		super(Mode.makeSemidet());
	}

	public ElementSource run(ElementSource inputs, final RBContext context) {
		return inputs.map(new Action() {
			public Object compute(Object arg) {
				return runSemiDet(arg, context);
			}
		});
	}

	public abstract Frame runSemiDet(Object input, RBContext context);

	/** 
	 * Note: this method is provided so that contexts which expect a nonDet or multi mode
	 * can use this.  However, it would be more efficient for compiled contexts to adapt
	 * when they are passing frames to a semiDetCompiled and use the runSemiDet instead.
	 */
	public ElementSource runNonDet(Object input, RBContext context) {
		Frame result = runSemiDet(input, context);
		if (result == null)
			return ElementSource.theEmpty;
		else {
//			PoormansProfiler.countSingletonsFromSemiDetCompiled++;
			return ElementSource.singleton(result);
		}
	}

	public Compiled conjoin(Compiled other) {
		if (other instanceof SemiDetCompiled)
			return new CompiledConjunction_SemiDet_SemiDet(
				this, (SemiDetCompiled)other);
		else
			return new CompiledConjunction_SemiDet_NonDet(this, other);
	}

	public Compiled disjoin(Compiled other) {
		if (other.equals(fail)) {
			return this;
		} else if (other instanceof SemiDetCompiled) {
			return new CompiledDisjunction_SemiDet_SemiDet(
				this, (SemiDetCompiled)other);
		} else {
			return new CompiledDisjunction_SemiDet_NonDet(this, other);
		}
	}

}

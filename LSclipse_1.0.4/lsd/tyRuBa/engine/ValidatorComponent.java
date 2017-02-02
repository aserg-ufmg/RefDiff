package tyRuBa.engine;

import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.modes.Mode;
import tyRuBa.modes.ModeCheckContext;
import tyRuBa.modes.PredInfoProvider;
import tyRuBa.modes.PredicateMode;
import tyRuBa.modes.TupleType;
import tyRuBa.modes.TypeModeError;

/**
 * A ValidatorComponent is an RBComponent wrapping an other RBComponent
 * "guarding" it with a Validator. The Validator may at any time become
 * invalidated. At this time the Validator itself becomes invalidated
 * and may be removed from wherever it happens to be stored.
 * 
 * @author kdvolder
 */

public class ValidatorComponent extends RBComponent {

	private Validator validator;
	private RBComponent comp;

	public ValidatorComponent(RBComponent c, Validator validator) {
		this.validator = validator;
		this.comp = c;
	}

	public boolean isValid() {
		return (validator != null && validator.isValid());
	}

	void checkValid() {
		if (!isValid()) {
			throw new Error("Internal Error: Using an invalidated component: "
					+this);
		}
	}

	public Validator getValidator() {
		return validator;
	}

	public RBTuple getArgs() {
		checkValid();
		return comp.getArgs();
	}
	
	public PredicateIdentifier getPredId() {
		checkValid();
		return comp.getPredId();
	}
	
	public TupleType typecheck(PredInfoProvider predinfos) throws TypeModeError {
		checkValid();
		return comp.typecheck(predinfos);
	}

	public RBComponent convertToNormalForm() {
		checkValid();
		return new ValidatorComponent(comp.convertToNormalForm(), validator);
	}

	public boolean isGroundFact() {
		checkValid();
		return comp.isGroundFact();
	}

	public String toString() {
		if (isValid())
			return comp.toString();
		else
			return "ValidatorComponent(INVALIDATED,"+comp+")";
	}

	public RBComponent convertToMode(PredicateMode mode, ModeCheckContext context) throws TypeModeError {
		checkValid();
		RBComponent converted = comp.convertToMode(mode, context);
		return new ValidatorComponent(converted, validator);
	}

	public Mode getMode() {
		checkValid();
		return comp.getMode();
	}

	public Compiled compile(final CompilationContext c) {
		checkValid();
		return comp.compile(c);
	}

}

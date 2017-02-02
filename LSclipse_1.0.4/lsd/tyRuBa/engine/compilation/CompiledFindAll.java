package tyRuBa.engine.compilation;

import tyRuBa.engine.Frame;
import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.RBContext;
import tyRuBa.engine.RBTerm;
import tyRuBa.modes.Mode;
import tyRuBa.util.Action;
import tyRuBa.util.ElementSource;

/**
 * @author kdvolder
 */
public class CompiledFindAll extends SemiDetCompiled {

	private final Compiled query;
	private final RBTerm extract;
	private final RBTerm result;

	public CompiledFindAll(Compiled query, RBTerm extract, RBTerm result) {
		super(Mode.makeDet());
		this.query = query;
		this.extract = extract;
		this.result = result;
	}

	public Frame runSemiDet(Object input, RBContext context) {
		ElementSource res = query.runNonDet(((Frame)input).clone(), context);
		res = res.map(new Action() {
			public Object compute(Object arg) {
				return extract.substitute((Frame) arg);
			}
		});
		RBTerm resultList = FrontEnd.makeList(res);
		return result.unify(resultList, (Frame)input);
	}

	public String toString() {
		return "COMPILED FINDALL(" + query + "," + extract + "," + result + ")";
	}

}

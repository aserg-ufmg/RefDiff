package tyRuBa.engine.compilation;

import java.util.HashSet;
import java.util.Set;

import tyRuBa.engine.Frame;
import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.RBContext;
import tyRuBa.engine.RBTerm;
import tyRuBa.modes.Mode;
import tyRuBa.util.ElementSource;

/**
 * @author kdvolder
 */
public class CompiledCount extends SemiDetCompiled {

	private final Compiled query;
	private final RBTerm extract;
	private final RBTerm result;

	public CompiledCount(Compiled query,RBTerm extract, RBTerm result) {
		super(Mode.makeDet());
		this.query = query;
		this.extract = extract;
		this.result = result;
	}

	public Frame runSemiDet(Object input, RBContext context) {
		ElementSource res = query.runNonDet(((Frame)input).clone(), context);
		Set results = new HashSet();
		while (res.hasMoreElements()) {
			Frame frame = (Frame)res.nextElement();
			results.add(extract.substitute(frame));
		}
		RBTerm resultCount = FrontEnd.makeInteger(results.size());
		return result.unify(resultCount, (Frame)input);
	}

	public String toString() {
		return "COMPILED FINDALL(" + query + "," + result + ")";
	}

}

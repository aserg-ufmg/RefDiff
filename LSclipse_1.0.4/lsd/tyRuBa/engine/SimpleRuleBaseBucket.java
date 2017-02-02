package tyRuBa.engine;
	
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;

public class SimpleRuleBaseBucket extends RuleBaseBucket {
	
	StringBuffer mystuff = null; // some stuff I should parse

	public SimpleRuleBaseBucket(FrontEnd frontEnd) {
		super(frontEnd, null);
	}
	
	public synchronized void addStuff(String toParse) {
		if (mystuff == null) 
			mystuff = new StringBuffer();
		mystuff.append(toParse + "\n");
		setOutdated();
	}
	
	public synchronized void clearStuff() {
		mystuff = null;
		setOutdated();
	}

	public synchronized void update() throws ParseException, TypeModeError {
		if (mystuff != null) {
			parse(mystuff.toString());
//			mystuff = null;
		}
	}

}

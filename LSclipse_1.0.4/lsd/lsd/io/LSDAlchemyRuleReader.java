package lsd.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import lsd.rule.*;

public class LSDAlchemyRuleReader {
	public static void main(String[] args) {
		LSDRule r = parseAlchemyRule("before_type(y) ^ before_fieldtype(y, X) => before_fieldtype(y, \"z\")");
		if (r!=null) { 
			System.out.println("LSDRule\n" +r.toString());
			System.out.println("Tyruba Query\n" +r.toTyrubaQuery(true));
		}
		r = parseAlchemyRule("before_type(x) ^ before_fieldtype( \"foo()\", x) => before_fieldtype(y, x)");
		if (r != null) {
			System.out.println("LSDRule\n" + r.toString());
			System.out.println("Tyruba Query\n" + r.toTyrubaQuery(true));
		}
		System.out.println("Parser tests completed.");
	}
	
	private ArrayList<LSDRule> rules = null;
	// (1) there's a weight in the beginning of the rule. 
	// (2) there'e no weight in the beginning of the rule. 
	
//	4.1 First-Order Logic
//	You can express an arbitrary first-order formula in an .mln file. The syntax for logical
//	connectives is as follows: ! (not), ^ (and), v (or), => (implies), <=> (if and only
//	if), FORALL/forall/Forall (universal quantification), and EXIST/exist/Exist (existential
//	quantification). Operator precedence is as follows: not > and > or > implies > if and
//	only if > forall = exists. Operators with the same precedence are evaluated left to right.
//	You can use parentheses to enforce a different precedence, or to make precedence explicit
//	(e.g., (A=>B)=>C as opposed to A=>(B=>C)). Universal quantifiers at the outermost level
//	can be omitted, i.e., free variables are interpreted as universally quantified at the outermost
//	level. Quantifiers can be applied to more than one variable at once (e.g., forall x,y). The
//	infix equality sign (e.g., x = y) can be used as a shorthand for the equality predicate (e.g.,
//	equals(x,y)).

	// Assumptions
	// (1) Disjunctive normal form is allowed. // !A(x) v !B(y) v C(x,y) 
	// (2) A horn clause with no nesting is allowed.  // A(x) ^ B(y) => C(x,y)
	// (3) There're no duplicate rules.  

	
	public LSDAlchemyRuleReader(File inputFile) {
		ArrayList<LSDRule> rs = new ArrayList<LSDRule>();
		try {
			if (inputFile.exists()) {
				BufferedReader in = new BufferedReader(
						new FileReader(inputFile));
				String line = null;
				while ((line=in.readLine())!= null){ 
					
					if (line.trim().equals("") || line.trim().charAt(0) == '#')
						continue;
					LSDRule rule = parseAlchemyRule(line); 
					rs.add(rule);
				}
				in.close();
			}
			this.rules= rs;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public ArrayList<LSDRule> getRules() {return rules;}
	
	static int quoteCount(String s) {
		int quoteCount = 0;
		for (char c : s.toCharArray()) {
			if (c == '"')
				quoteCount++;
		}
		return quoteCount;
	}
	
	public static LSDRule parseAlchemyRule(String line) { 
		line = line.replace("?","");
		if (line.lastIndexOf("\t")>0) {
		line = line.substring(0,line.lastIndexOf("\t"));
		}
		LSDRule rule = new LSDRule();
		// Only two forms allowed are:
		// (1) A ^ B ^ C ^ D ^ ... => E
		// (2) A v B v C v ...
		String ruleString = line.substring(line.indexOf('\t') + 1).trim(); // Assume '/t' iff it is the 1st char after junk at beginning of rule.
		//[!]predicate '(' args ')' {v,^,=>} (repeat)
		while (!ruleString.equals(""))
		{

			boolean negated = false;
			if (ruleString.charAt(0) == '!')
			{
				negated = true;
				ruleString = ruleString.substring(1);
			}
			String predicateName = ruleString.substring(0, ruleString.indexOf('(')).trim();
			ruleString = ruleString.substring(ruleString.indexOf('(') + 1).trim();
			int endOfArgs = ruleString.indexOf(')');
			int firstQuote = ruleString.indexOf('"');
			int secondQuote = ruleString.indexOf('"', firstQuote + 1);
			if (secondQuote == -1 && firstQuote != -1)
			{
				System.err.println("Mismatched quotes in the rule");
				System.err.println("Line: " + line);
				System.exit(-1);
			}
			while (quoteCount(ruleString.substring(0, endOfArgs)) % 2 != 0)//firstQuote != -1 && secondQuote != -1 && firstQuote < endOfArgs && endOfArgs < secondQuote)
			{
				endOfArgs = ruleString.indexOf(')', endOfArgs + 1);
				assert endOfArgs != -1;
			}
			String arguments = ruleString.substring(0, endOfArgs).trim();
			ruleString = ruleString.substring(endOfArgs + 1).trim();
			if (ruleString.equals(""))
			{
				// We're done, don't need to switch negation.
			}
			else if (ruleString.charAt(0) == 'v')
			{
				ruleString = ruleString.substring(1).trim();
			}
			else if (ruleString.charAt(0) == '^')
			{
				ruleString = ruleString.substring(1).trim();
				negated = !negated;
			} 
			else if (ruleString.charAt(0) == '=')
			{
				assert ruleString.charAt(1) == '>';
				ruleString = ruleString.substring(2).trim();
				negated = !negated;
			}
			else
			{
				System.err.println("Rule ill defined...");
				System.err.println("Line: " + line);
				System.err.println("Remaining: " + ruleString);
				System.exit(-1);				
			}
			LSDPredicate predicate = LSDPredicate.getPredicate(predicateName);
			if (predicate == null)
			{
				System.err.println("Predicate " + predicateName + " is not defined.");
				System.err.println("Line: " + line);
				System.exit(-1);
			}
			ArrayList<LSDBinding> bindings = new ArrayList<LSDBinding>();
			char[] types = predicate.getTypes();
			for (int i = 0; i < types.length; i++)
			{
				if (arguments.charAt(0) == '"')
				{
					String constant = arguments.substring(0, arguments.indexOf('"', 1) + 1);
					arguments = arguments.substring(arguments.indexOf('"', 1) + 1).trim();
					if (i != types.length - 1)
					{
						assert arguments.charAt(0) == ',';
						arguments = arguments.substring(1).trim();
					}
					bindings.add(new LSDBinding(constant));
				}
				else if (Character.isUpperCase(arguments.charAt(0)))
				{
					String constant = "";
					if (i != types.length - 1)
					{
						assert arguments.contains(",");
						constant = arguments.substring(0,arguments.indexOf(',', 1));
						arguments = arguments.substring(arguments.indexOf(',') + 1).trim();
					}
					else
					{
						assert !arguments.contains(",");
						constant = arguments;
					}
					bindings.add(new LSDBinding(constant));
				}
				else
				{
					String varName = "";
					if (i != types.length - 1)
					{
						assert arguments.contains(",");
						varName = arguments.substring(0,arguments.indexOf(',', 1));
						arguments = arguments.substring(arguments.indexOf(',') + 1).trim();
					}
					else
					{
						if (arguments.contains(","))
						{
							System.err.println("Error: we think '"+arguments+"' shouldn't contain a comma.");
							System.err.println("Line: " + line);
							System.exit(-1);	
						}
						varName = arguments;
					}
					bindings.add(new LSDBinding(new LSDVariable(varName, types[i])));
				}
			}
			try
			{
				boolean success = rule.addLiteral(new LSDLiteral(predicate, bindings, !negated));
				if (!success)
				{
					System.err.println("Error, rules cannot contain facts.");
					System.err.println("Line: " + line);
					System.exit(-1);					
				}
			}
			catch (LSDInvalidTypeException e)
			{
				e.printStackTrace();
				System.err.println("Line: " + line);
				System.exit(-1);
			}
		}
		if (!rule.isValid())
		{
			System.err
					.println("Rule skipped because it's not valid: isHornClause "
							+ rule.isHornClause()
							+ "\tdoesTypeChecks "
							+ rule.typeChecks()
							+ "\tMight also not be properly interrelated.");
			
			System.err.println("Rule parsed as: " + rule);
			System.err.println("Line: " + line);
			return null;
		}
		return rule;
	}
}

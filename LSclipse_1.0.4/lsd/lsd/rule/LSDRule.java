package lsd.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lsd.io.LSDAlchemyRuleReader;


public class LSDRule {
	private double score = 0;
	private int numMatches = 0;
	private double accuracy = 0;
	double numDeltaFacts = 0;
	
	private ArrayList<LSDLiteral> literals = new ArrayList<LSDLiteral>();

	private Set<LSDVariable> freeVars = new HashSet<LSDVariable>();
	
	private static HashMap<Character, Integer> penaltyLookup = new HashMap<Character, Integer>();
	
	static {
		penaltyLookup.put('p', 1);	// Package
		penaltyLookup.put('t', 2);	// Type
		penaltyLookup.put('m', 3);	// Method
		penaltyLookup.put('f', 3);	// Field
		penaltyLookup.put('a', 4);	// Name of type
		penaltyLookup.put('b', 4);	// Name of field
		penaltyLookup.put('c', 4);	// Name of method
	}
	
	public class LSDRuleComparator implements Comparator<LSDRule>  {

		public int compare(LSDRule r1, LSDRule r2) {
			return ((r2.score - r1.score)>0)?1:
				((r2.score - r1.score)==0)?0:-1;
		} 
	   
	} 
	
	public LSDRule() {
		
	}

	public LSDRule(LSDRule oldRule) {
		// create a copy of LSD Rule
		for (LSDLiteral literal: oldRule.literals) {
			this.addLiteral(literal);
		}
		
	}

	public LSDRule(LSDRule rule, boolean b) {
		for (LSDLiteral literal: rule.literals) {
			ArrayList<LSDBinding> newBindings = new ArrayList<LSDBinding>();
			for (LSDBinding binding : literal.getBindings()) {
				LSDBinding newBinding = new LSDBinding(new LSDVariable(binding.getVariable().getName(),binding.getType()));
				newBindings.add(newBinding);
			}
			try {
				LSDLiteral newLiteral = new LSDLiteral(literal.predicate,newBindings,!literal.isNegated());
				this.addLiteral(newLiteral);
			} catch (LSDInvalidTypeException e) {
				e.printStackTrace();
			}
			
		}
	}

	public ArrayList<LSDVariable> getFreeVariables() {
		ArrayList<LSDVariable> fv = new ArrayList<LSDVariable>(freeVars);
		return fv;
	}
	
	public boolean addLiteral(LSDLiteral newLiteral) {
		//if (newLiteral instanceof LSDFact)
		//	return false;
		Collection<LSDVariable> newFreeVars = newLiteral.freeVars();
		literals.add(newLiteral);
		freeVars.addAll(newFreeVars);
		return true;
	}

	public ArrayList<LSDLiteral> getLiterals() {
		return new ArrayList<LSDLiteral>(literals);
	}

	public LSDRule convertAllToAntecedents() {
		LSDRule antecedents = new LSDRule();
		for (LSDLiteral literal : literals) {
			if (literal.isNegated())
				antecedents.addLiteral(literal);
			else
				antecedents.addLiteral(literal.negatedCopy());
		}
		return antecedents;
	}
	
	public LSDRule getAntecedents() {
		LSDRule antecedents = new LSDRule();
		for (LSDLiteral literal : literals) {
			if (literal.isNegated())
				antecedents.addLiteral(literal);
		}
		return antecedents;
	}

	public LSDRule getConclusions() {
		LSDRule conclusions = new LSDRule();
		for (LSDLiteral literal : literals) {
			if (!literal.isNegated())
				conclusions.addLiteral(literal);
		}
		return conclusions;
	}

	public boolean literalsLinked()
	{
		// for every literal, there's at least one other literal that shares a
		// free variable.
		Map<LSDVariable, Integer> freeVarCount = new HashMap<LSDVariable, Integer>();
		for (LSDLiteral l : literals) {
			for (LSDVariable v : l.freeVars()) {
				if (freeVarCount.get(v) == null)
					freeVarCount.put(v, 1);
				else
					freeVarCount.put(v, freeVarCount.get(v) + 1);
			}
		}
		for (LSDLiteral l : literals) {
			boolean invalid = true;
			for (LSDVariable v : l.freeVars()) {
				if (freeVarCount.get(v) > 1) // This free variable also
				// occurs in another literal.
				{
					invalid = false; // If at least one FV is common, the
					// literal doesn't invalidate the rule.
					break;
				}
			}
			if (invalid) // If no FVs in this literal also occured in another...
				return false; // ..Then this rule is invalid.
		}
		return true;
	}
	//Added by fatemeh
	public boolean hasValidLinks() {
		// for every literal, the variable locations are legal
		for (LSDLiteral l : literals) {
			for (int i = 0; i < l.bindings.size(); i++) {
				LSDBinding temp = l.bindings.get(i);
				if (!temp.isBound()){
					for(int j = i+1; j < l.bindings.size(); j++)
						if (temp.getVariable() == l.bindings.get(j).getVariable())
							return false;
				}
			}
		}
		return true;
		
	}
	
	//Added by fatemeh
	public boolean isSamePreds() {
		ArrayList<LSDLiteral> conclusions = this.getConclusions().getLiterals();
		for (LSDLiteral conc : conclusions) {
			boolean allDuplicate = true;
			for (LSDLiteral literal : this.getAntecedents().getLiterals()) {
				if (!literal.getPredicate().getSuffix().equalsIgnoreCase(conc.getPredicate().getSuffix()))
					allDuplicate = false;
			}
			if (allDuplicate)
				return true;
		}
		return false;		
	}
	
	// If the rule is a horn clause, type checks, has all literals interconnected by at
	// least one variable, and has no literals in the conclusion that don't have a set of primary variables in the antecedents...
	public boolean isValid() {
		if (!isHornClause() || !typeChecks())
			return false;

		if (!literalsLinked())
			return false;
		// At least one set of primary variables in the conclusion will be matched by stuff in the antecedents
		Set<LSDVariable> antecedentVars = new HashSet<LSDVariable>(this.getAntecedents().getFreeVariables());
		for (LSDLiteral literal : this.getConclusions().getLiterals()) {
			boolean primaryMatched = false;
			for (List<LSDBinding> bindingSet : literal.getPrimaryBindings()) {
				boolean anyUnmatched = false;
				for (LSDBinding binding : bindingSet) {
					if (!binding.isBound() && !antecedentVars.contains(binding.getVariable())) {
						anyUnmatched = true;
						break;
					}
				}
				if (!anyUnmatched) {
					primaryMatched = true;
					break;
				}
			}
			if (!primaryMatched)
				return false;

		}
//		for (LSDVariable variable : this.getConclusions().getBindableFreeVariables()) {
//			if (!antecedentVars.contains(variable))
//				return false;
//		}
		
		return true;
	}
	

	public boolean containsFacts() {
		for (LSDLiteral literal : literals) {
			if (literal instanceof LSDFact)
				return true;
		}
		return false;
	}

	public LSDRule substitute(LSDVariable toReplace, LSDBinding replacement)
			throws LSDInvalidTypeException {
		// create a copy of LSD Rule
		LSDRule newRule = new LSDRule();
		for (LSDLiteral literal: this.literals) {
			newRule.addLiteral(literal.substitute(toReplace, replacement));
		}
		return newRule;
	}

	public boolean typeChecks() {
		// return true if freevariables with the same name have the same type.
		for (LSDVariable fv_i : freeVars) {
			for (LSDVariable fv_j : freeVars) {
				if (fv_i.typeConflicts(fv_j)) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean isHornClause() {
		int nonNegatedLiterals = 0;
		for (LSDLiteral literal : literals) {
			if (!literal.isNegated())
				nonNegatedLiterals++;
		}
		return nonNegatedLiterals == 1;
	}

	public String toString() {
		StringBuilder output = new StringBuilder();
		// Antecedents
		for (LSDLiteral literal : literals) {
			if (!literal.isNegated())
				continue;
			if (output.length() != 0)
				output.append(" ^ ");
			output.append(literal.nonNegatedCopy().toString());
		}
		output.append(" => ");
		boolean first = true;
		// Conclusion(s).
		for (LSDLiteral literal : literals) {
			if (literal.isNegated())
				continue;
			if (!first)
				output.append(" ^ ");
			output.append(literal.nonNegatedCopy().toString());
			first = false;
		}
		return output.toString();
	}

	public String toTyrubaQuery(boolean commandLine) {
		StringBuilder output = new StringBuilder();
		if (commandLine)
			output.append(":-");
		Hashtable<LSDVariable, Integer> freeVarCount = new Hashtable<LSDVariable, Integer>();
		for (LSDLiteral l : literals) {
			for (LSDVariable v : l.freeVars()) {
				if (freeVarCount.get(v) == null)
					freeVarCount.put(v, 1);
				else
					freeVarCount.put(v, freeVarCount.get(v) + 1);
			}
		}
		for (int i = 0; i < literals.size(); i++) {
			if (i > 0)
				output.append(",");

			output.append(literals.get(i).toTyrubaString(freeVarCount));
		}
		return output.toString() + (commandLine ? "." : "");
	}
	
	private String canonicalRepresentation() {
		return canonicalRepresentation(this.getLiterals(), new HashMap<LSDVariable, String>(), 0);
	}
	
	private String canonicalRepresentation(List<LSDLiteral> literals, Map<LSDVariable, String> varMap, int nextVarNum) {
		if (literals.size() == 0)
			return "";
		List<LSDPredicate> predicates = LSDPredicate.getPredicates();
		int firstPredicateIndex = predicates.size();
		List<Integer> firstPredicateList = null;
		for (int i = 0; i < literals.size(); i++) {
			LSDLiteral literal = literals.get(i);
			int thisIndex = predicates.indexOf(literal.getPredicate());
			if (thisIndex < firstPredicateIndex && thisIndex >= 0) {
				firstPredicateIndex = thisIndex;
				firstPredicateList = new ArrayList<Integer>();
				firstPredicateList.add(i);
			}
			else if (thisIndex == firstPredicateIndex)
				firstPredicateList.add(i);
		}
		String repr = null;
		for (int index : firstPredicateList) {

			StringBuilder thisRepr = new StringBuilder();
			Map<LSDVariable, String> thisVarMap = new HashMap<LSDVariable, String>(varMap);
			int thisNextVarNum = nextVarNum;
			LSDLiteral literal = literals.get(index);
			if (literal.isNegated())
				thisRepr.append("!");
			thisRepr.append(literal.getPredicate().getName());
			thisRepr.append("(");
			for (LSDBinding binding : literal.getBindings()) {
				if (binding.isBound())
					thisRepr.append(binding.toString());
				else {
					LSDVariable variable = binding.getVariable();
					if (!thisVarMap.containsKey(variable)) {
						thisVarMap.put(variable, "?x" + thisNextVarNum);
						thisNextVarNum += 1;
					}
					thisRepr.append(thisVarMap.get(variable));
				}
				thisRepr.append(",");
			}
			thisRepr.append(")");
			List<LSDLiteral> newLiterals= new ArrayList<LSDLiteral>(literals);
			newLiterals.remove(index);
			thisRepr.append(canonicalRepresentation(newLiterals, thisVarMap, thisNextVarNum));
			if (repr == null || thisRepr.toString().compareTo(repr) < 0)
				repr = thisRepr.toString();
		}
		return repr;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof LSDRule))
			return false;
		return canonicalRepresentation().equals(((LSDRule)o).canonicalRepresentation());
	}
	
	public int hashCode() {
		return canonicalRepresentation().hashCode();
	}

	// Negative when this is less general than r2
	// XXX  FIXME  XXX  I think this comparison is backwards.  Should make sure that fixing it to penalty1 - penalty2 is right.
	// FIXME XXX FIXME
	// XXX  FIXME  XXX
	public int generalityCompare(LSDRule r2) {
		int penalty1 = 0;
		int penalty2 = 0;
		for (LSDVariable var : this.getFreeVariables())
			penalty1 += penaltyLookup.get(var.getType());
		for (LSDVariable var : r2.getFreeVariables())
			penalty2 += penaltyLookup.get(var.getType());
		return penalty1 - penalty2;
	}
	
	public static void main(String[] args) {

		LSDPredicate foo = LSDPredicate.getPredicate("added_inheritedmethod");
		ArrayList<LSDBinding> bindings = new ArrayList<LSDBinding>();
		LSDVariable a = (new LSDVariable("a", 'm'));
		LSDBinding binding = new LSDBinding(a);
		bindings.add(binding);
		binding = new LSDBinding(new LSDVariable("b", 't'));
		bindings.add(binding);
		binding = new LSDBinding(new LSDVariable("c", 't'));
		bindings.add(binding);
		try {
			LSDLiteral bar = new LSDLiteral(foo, bindings, false);
			System.out.println(bar.freeVars());

			foo = LSDPredicate.getPredicate("deleted_accesses");
			bindings = new ArrayList<LSDBinding>();
			binding = new LSDBinding(new LSDVariable("d", 'f'));
			bindings.add(binding);
			binding = new LSDBinding(new LSDVariable("a", 'm'));
			bindings.add(binding);
			LSDLiteral baz = new LSDLiteral(foo, bindings, true);

			foo = LSDPredicate.getPredicate("deleted_accesses");
			bindings = new ArrayList<LSDBinding>();
			binding = new LSDBinding(new LSDVariable("r", 'f'));
			bindings.add(binding);
			binding = new LSDBinding(new LSDVariable("s", 'm'));
			bindings.add(binding);
			LSDLiteral quxx = new LSDLiteral(foo, bindings, true);

			LSDRule r = new LSDRule();
			assert r.addLiteral(bar);
			System.out.println(r);
			assert !r.isHornClause();
			assert r.freeVars.contains(a);
			LSDVariable b = new LSDVariable("a", 'm');
			assert a.hashCode() == b.hashCode();
			assert a.equals(b);
			assert a != b;
			assert r.freeVars.contains(b);
			assert a.equals(b);
			assert r.addLiteral(baz);
			System.out.println(r);
			assert r.isHornClause();
			assert r.isValid();
			assert r.addLiteral(quxx);
			System.out.println(r);
			assert !r.isValid();
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
		System.out.println(LSDAlchemyRuleReader.parseAlchemyRule("before_typeintype(z, x) ^ before_typeintype(x, z) => added_type(x)").canonicalRepresentation());
		
		System.out.println("Rule tests succeeded.");
	}


	public String[] getClassLevelGrounding() {
		HashSet<String> res = new HashSet<String>();
		ArrayList<LSDLiteral> temp = this.getLiterals();
		List<LSDBinding> bindings;
		for (LSDLiteral literal : temp) {
			bindings = literal.getBindings();
			for (LSDBinding binding : bindings) {
				if(binding.isBound())
					res.add(binding.getGroundConst());
			}
		}
		String[] results = new String[res.size()];
		int i = 0;
		for (String str : res) {
			results[i++] = str;
		}
		return results;
	}

	public double getScore() {
		return score;
	}
	
	public void setScore() {
		boolean hasLanguageBinding = false;
		int bindingScore = 0;
		for (LSDLiteral literal : literals) {
			for (LSDBinding binding : literal.getBindings()){
				if (binding.getGroundConst()!= null)
				{	if (binding.getGroundConst().startsWith("java") && !hasLanguageBinding)
						hasLanguageBinding = true;
					bindingScore += literal.getBindingScore(binding);
				}
			}
		}
		score = 2* bindingScore;
		score += accuracy;
		score += 2* ( numMatches / 250.0);
		if(hasLanguageBinding)
			score -= 2;
		
	}
	

	public void setNumMatches(int numMatches) {
		this.numMatches = numMatches;
	}

	public int getNumMatches() {
		return numMatches;
	}

	public void setAccuracy(double a) {
		accuracy = a;
	}

	public void removeFreeVar(LSDVariable variable) {
		freeVars.remove(variable);
		
	}

}

	

	


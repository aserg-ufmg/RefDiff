package lsd.io;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lsd.rule.LSDBinding;
import lsd.rule.LSDFact;
import lsd.rule.LSDLiteral;
import lsd.rule.LSDRule;
import lsd.rule.LSDVariable;
import metapackage.MetaInfo;
import tyRuBa.engine.Frame;
import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.RBExpression;
import tyRuBa.engine.RBTerm;
import tyRuBa.engine.RBVariable;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.util.ElementSource;



public class LSDTyrubaRuleChecker {
	private FrontEnd frontend = null;

	private File dbDir = null; // the default

	private boolean backgroundPageCleaning = false; // the default

	boolean loadInitFile = true; // the default

	int cachesize = FrontEnd.defaultPagerCacheSize; // the default

	
	public LSDTyrubaRuleChecker() {
		if (frontend == null) {
			if (dbDir == null)
				frontend = new FrontEnd(loadInitFile, MetaInfo.fdbDir, true,
						null, true, backgroundPageCleaning);
				
			else
				frontend = new FrontEnd(loadInitFile, dbDir, true, null, false,
						backgroundPageCleaning);
		}
		frontend.setCacheSize(this.cachesize);
	}
	
	public void loadAdditionalDB(File inputDBFile)
			throws tyRuBa.parser.ParseException, TypeModeError,
			java.io.IOException {
		//System.out.println("Kim:" + inputDBFile.getAbsolutePath());
		frontend.load(inputDBFile.getAbsolutePath());
	}
	
	public void loadAdditionalDB(String input)
			throws tyRuBa.parser.ParseException, TypeModeError,
			java.io.IOException {
		frontend.load(input);
	}
	
	public void loadPrimedAdditionalDB(File inputDBFile)
			throws tyRuBa.parser.ParseException, TypeModeError,
			java.io.IOException {
		List<LSDFact> facts = null;
		try {
			facts = new LSDTyrubaFactReader(inputDBFile).getFacts();
		} catch (Exception e) { }
		if (facts != null) {
			for (LSDFact fact : facts) {
				if (fact.getPredicate().isConclusionPredicate())
				{
					frontend.parse(fact.toString().replaceFirst("_", "_p_") + ".");
				}
			}
		}
	}
	
	

	public void loadFact(LSDFact fact)
			throws TypeModeError, ParseException {
		frontend.parse(fact.toString() + ".");
	}

	// Invokes a query in TyRuBa.
	// Returns all unique counter-examples of constants to substitute in for the free variables in the conclusions.
	// If the returned map.isEmpty(), then the rule is true (no counter-examples found).
	// (If the rule is A(x) ^ B(y) => C(x,y), return all sets of constants to bind to ?x and ?y
	// such that A(x) ^ B(y) ^ NOT C(x, y) is true 
	public List<Map<LSDVariable, String>> getCounterExamples(LSDRule rule) {
		return (List<Map<LSDVariable, String>>) invokeQuery(rule, false);
	}
	// Invokes the antecedents as a query in TyRuBa and does substitution on the conclusions.
	// Returns the conclusion of the rule for each set of substitutions which satisfy the antecedent and conclusion.
	// Only return unique conclusions.
	// (If the rule is A ^ B => C, returns all unique facts (actually, Rules composed of facts) C such that A ^ B ^ C.)
	public ArrayList<LSDRule> getTrueConclusions(LSDRule rule) {
		return (ArrayList<LSDRule>) invokeQuery(rule, true);
	}

	// See comments on getCounterExamples or getTrueConclusions for the two modes of behavior of this function.
	private Object invokeQuery(LSDRule rule, boolean returnConclusions) {
		LSDRule substitute = (returnConclusions ? rule.getConclusions() : rule);
		String query = (returnConclusions ? rule.convertAllToAntecedents() : rule).toTyrubaQuery(false);
		ArrayList<Map<LSDVariable, String>> exceptions = new ArrayList<Map<LSDVariable, String>>();
		ArrayList<LSDRule> newSubstitutedRules = new ArrayList<LSDRule>();
		ArrayList<LSDVariable> freeVars = rule.getConclusions().getFreeVariables();
		
		Set<Set<String>> exceptionMatches = new HashSet<Set<String>>();
		Set<String> foundConclusionMatches = new HashSet<String>();

		try {
			//System.out.println("Executing " + query);
			RBExpression exp = frontend.makeExpression(query);
			ElementSource es = frontend.frameQuery(exp);
			try {
				if (es.status() == ElementSource.NO_MORE_ELEMENTS) {
					//System.out.println("Failure --- which is success for us!");
					if (returnConclusions)
						return newSubstitutedRules;
					else
						return exceptions;
				}
				// iterate through all the frames in the result
				
				while (es.status() == ElementSource.ELEMENT_READY) {
					Frame frame = (Frame) es.nextElement();
					Set<String> exceptionMatchStrings = new HashSet<String>();
					LinkedHashMap<LSDVariable, String> exception = new LinkedHashMap<LSDVariable, String>();
					LSDRule newRule = null;
					for (RBVariable matchedVar : (Set<RBVariable>) frame.keySet()) {
						RBTerm term = frame.get(matchedVar);
						String constant = "\"" + term.toString() + "\"";
						LSDVariable toReplace = null;

						// find the free variable in our list that matches with tyruba free variable.
						for (LSDVariable freeVar : new LinkedHashSet<LSDVariable>(freeVars)) {
							if (freeVar == null)
								continue;
							if (freeVar.toString().equals(matchedVar.toString())) {
								exceptionMatchStrings.add(freeVar.toString() + constant);
								toReplace = freeVar;
							}
						}
						if (toReplace == null)
							continue;		// This var. on the frame isn't one of the ones we're interested in.
						exception.put(toReplace, constant);
						newRule = ((newRule == null)?substitute:newRule)
								.substitute(toReplace, new LSDBinding(constant));
					}
					//System.out.println("progress: " + newRule);
					
					if (!exceptionMatches.contains(exceptionMatchStrings)) {
							exceptions.add(exception);
							exceptionMatches.add(exceptionMatchStrings);
					}
					if (newRule != null) {
						if (!foundConclusionMatches.contains(newRule.toString())) {
							newSubstitutedRules.add(newRule);
							foundConclusionMatches.add(newRule.toString());
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} catch (Error e) {
				e.printStackTrace();
			} finally {
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (returnConclusions)
			return newSubstitutedRules;
		else
			return exceptions;
	}
	
	public List<LSDFact> get2KBMatches(LSDRule rule) {
		LSDRule substitute = rule;
		String query = rule.convertAllToAntecedents().toTyrubaQuery(false);
		ArrayList<LSDRule> newSubstitutedRules = new ArrayList<LSDRule>();
		ArrayList<LSDVariable> freeVars = rule.getFreeVariables();
		
		try {
			//System.out.println("Executing " + query);
			RBExpression exp = frontend.makeExpression(query);
			ElementSource es = frontend.frameQuery(exp);
			try {
				if (es.status() == ElementSource.NO_MORE_ELEMENTS) {
					//System.out.println("Failure --- which is success for us!");
					return new ArrayList<LSDFact>();
				}
				// iterate through all the frames in the result
				
				while (es.status() == ElementSource.ELEMENT_READY) {
					Frame frame = (Frame) es.nextElement();
					LSDRule newRule = null;
					for (RBVariable matchedVar : (Set<RBVariable>) frame.keySet()) {
						RBTerm term = frame.get(matchedVar);
						String constant = "\"" + term.toString() + "\"";
						LSDVariable toReplace = null;
						// find the free variable in our list that matches with tyruba free variable.
						for (LSDVariable freeVar : new LinkedHashSet<LSDVariable>(freeVars)) {
							if (freeVar == null)
								continue;
							if (freeVar.toString().equals(matchedVar.toString())) {
								toReplace = freeVar;
							}
						}
						if (toReplace == null)
							continue;		// This var. on the frame isn't one of the ones we're interested in.
						newRule = ((newRule == null)?substitute:newRule)
								.substitute(toReplace, new LSDBinding(constant));
					}
					
					if (newRule != null) {
						newSubstitutedRules.add(newRule);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} catch (Error e) {
				e.printStackTrace();
			} finally {
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<LSDFact> foundFacts = new ArrayList<LSDFact>();
		for (LSDRule r : newSubstitutedRules) {
			for (LSDLiteral literal : r.getLiterals()) {
				if (literal instanceof LSDFact && literal.getPredicate().is2KBPredicate() && !foundFacts.contains((LSDFact)literal))
					foundFacts.add((LSDFact)literal);
			}
		}
		return foundFacts;
	}
	
	// Count the number of distinct matches we have to the Tyruba query query.
	// Only consider matches which have a unique assignment of constants to the variables in freeVars.
	// If max > 0, only count at most max matches before returning.
	private int countMatches(String query, List<LSDVariable> freeVars, int max) {
		Set<Set<String>> matches = new HashSet<Set<String>>();
		try {
			RBExpression exp = frontend.makeExpression(query);
			ElementSource es = frontend.frameQuery(exp);
			if (es.status() == ElementSource.NO_MORE_ELEMENTS)
				return 0;
			while (es.status() == ElementSource.ELEMENT_READY && (max == 0  || matches.size() < max)) {
				Frame frame = (Frame) es.nextElement();
				Set<String> matchStrings = new HashSet<String>();
				for (RBVariable matchedVar : (Set<RBVariable>) frame.keySet()) {
					RBTerm term = frame.get(matchedVar);
					String constant = "\"" + term.toString() + "\"";
					// find the free variable in our list that matches with tyruba free variable.
					for (LSDVariable freeVar : new LinkedHashSet<LSDVariable>(freeVars)) {
						if (freeVar.toString().equals(matchedVar.toString()))
							matchStrings.add(freeVar.toString() + constant);
					}
				}
				matches.add(matchStrings);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return matches.size();
	}
	
	// Count the number of distinct facts which match the conclusion of rule.
	// (if rule is A ^ B -> C, return # matches for C s.t. A ^ B ^ C)
	// If optional arg. max > 0, stop counting at max and return.
	public int countTrueConclusions(LSDRule rule) {
		return countTrueConclusions(rule, 0);
	}
	public int countTrueConclusions(LSDRule rule, int max) {
		String query = rule.convertAllToAntecedents().toTyrubaQuery(false);
		List<LSDVariable> freeVars = rule.getConclusions().getFreeVariables();
		return countMatches(query, freeVars, max);
	}
	
	// Count the number of distinct false facts which violate the rule.
	// (if rule is A ^ B -> C, return # matches for C (the vars in C) s.t. A ^ B ^ !C)
	// If optional arg. max > 0, stop counting at max and return.
	public int countCounterExamples(LSDRule rule) {
		return countCounterExamples(rule, 0);
	}
	public int countCounterExamples(LSDRule rule, int max) {
		String query = rule.toTyrubaQuery(false);
		List<LSDVariable> freeVars = rule.getConclusions().getFreeVariables();
		return countMatches(query, freeVars, max);
	}
	
	// Return a set of the constants which could replace the given variable in the rule
	// and have it still have matches.
	public Set<String> getReplacementConstants(LSDRule rule, LSDVariable match) {
		assert rule.getFreeVariables().contains(match); 
		String query = rule.convertAllToAntecedents().toTyrubaQuery(false);
		Set<String> replacements = new LinkedHashSet<String>();
		try {
			//System.out.println("Executing " + query);
			RBExpression exp = frontend.makeExpression(query);
			ElementSource es = frontend.frameQuery(exp);
			try {
				if (es.status() == ElementSource.NO_MORE_ELEMENTS) {
					return replacements; 
				}
				// iterate through all the frames in the result
//				RBVariable RBVar = null;
				while (es.status() == ElementSource.ELEMENT_READY) {
					Frame frame = (Frame) es.nextElement();
					for (RBVariable matchedVar : (Set<RBVariable>) frame.keySet()) {
						if (matchedVar.toString().equals(match.toString())){
							RBTerm term = frame.get(matchedVar);
							if (term != null)
								replacements.add("\"" + term.toString() + "\"");
							break;
						}
					}
				}
			} catch (Exception e) {
			} catch (Error e) {
			} finally {
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return replacements;
	}
	
	
	public void shutdown() {
		frontend.shutdown();
		frontend.crash();
	}

	public void loadRelatedFacts(ArrayList<LSDFact> facts, ArrayList<String> typeNames) throws Exception {
		this.loadAdditionalDB(MetaInfo.included2kb);
		String line = null;
		for (LSDFact fact : facts){
			line = fact.toString() + ".";
			for (String str : typeNames) {
				if (line.contains(str))
					this.loadFact(LSDTyrubaFactReader.parseTyrubaFact(line));
			}
		}
	}
}

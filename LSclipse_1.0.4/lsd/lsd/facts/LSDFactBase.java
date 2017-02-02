package lsd.facts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import lsd.io.LSDTyrubaRuleChecker;
import lsd.rule.LSDFact;
import lsd.rule.LSDLiteral;
import lsd.rule.LSDRule;
import lsd.rule.LSDVariable;
import metapackage.MetaInfo;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;


public class LSDFactBase {
	public static final boolean deltaKB = true;
	public static final boolean twoKB = false;
	private LSDTyrubaRuleChecker ruleChecker = new LSDTyrubaRuleChecker();
	private LinkedHashSet<LSDFact> factsDeltaKB = new LinkedHashSet<LSDFact>();
	private LinkedHashSet<LSDFact> facts2KB = new LinkedHashSet<LSDFact>();
	private ArrayList<LSDRule> winnowingRules = new ArrayList<LSDRule>();
	private boolean winnowed = true;
	private HashSet<LSDFact> matched = new HashSet<LSDFact>();
	private HashMap<LSDRule, List<LSDFact>> ruleMatches = new HashMap<LSDRule, List<LSDFact>>();
	private HashMap<LSDRule, List<Map<LSDVariable, String>>> ruleExceptions =
												new HashMap<LSDRule, List<Map<LSDVariable, String>>>();
	
	public LinkedHashSet<LSDFact> get2KBFact () { 
		return facts2KB; 
	}
	public LinkedHashSet<LSDFact> getDeltaKBFact () { 
		return factsDeltaKB;
	}
	public List<LSDFact> getRemainingFacts(boolean deltaKB) {
		if (!winnowed)
			winnow();
		ArrayList<LSDFact> remainingFacts = new ArrayList<LSDFact>();
		for (LSDFact f : (deltaKB ? factsDeltaKB : facts2KB)) {
			if (!matched.contains(f))
				remainingFacts.add(f);
		}
		return remainingFacts;
	}

	public List<LSDFact> getRelevantFacts(LSDRule rule) {
		if (!winnowed)
			winnow();
		if (!ruleMatches.containsKey(rule)) {
			System.err.println("The requested rule (" + rule
					+ ") is not in the list.");
			return null;
		}
		ArrayList<LSDFact> relevantFacts = new ArrayList<LSDFact>();
		for (LSDFact f : ruleMatches.get(rule)) {
			if (factsDeltaKB.contains(f) || facts2KB.contains(f))
				relevantFacts.add(f);
		}
		return relevantFacts;
	}

	public List<Map<LSDVariable, String>> getExceptions(LSDRule rule) {
		if (!winnowed)
			winnow();
		if (!ruleExceptions.containsKey(rule)) {
			System.err.println("The requested rule (" + rule
					+ ") is not in the list.");
			return null;
		}
		return ruleExceptions.get(rule);
	}

	public void loadDeltaKBFactBase(ArrayList<LSDFact> facts) throws ParseException, TypeModeError, IOException{
//		ruleChecker.loadPrimedAdditionalDB(tyrubaFormattedFacts);
		ruleChecker.loadAdditionalDB(MetaInfo.includedDelta);
		for (LSDFact fact : facts) {
			ruleChecker.loadFact(fact);
			factsDeltaKB.add(fact);
		}
		resetWinnowing();
	}
	
	public void loadFilteredDeltaFactBase(ArrayList<LSDFact> facts,ArrayList<String> typeNames) throws Exception {
//		ruleChecker.loadPrimedAdditionalDB(orgDeltaFacts,typeNames);
		ruleChecker.loadAdditionalDB(MetaInfo.includedDelta);
		String line = null;
		for (LSDFact fact : facts) {
			line = fact.toString();
			for (String str : typeNames) {
				if (line.contains(str))
				{
					ruleChecker.loadFact(fact);
					factsDeltaKB.add(fact);
				}
			}
		}
		resetWinnowing();
	}
	
	public void loadFiltered2KBFactBase(ArrayList<LSDFact> facts,ArrayList<String> typeNames) throws Exception {
		ruleChecker.loadAdditionalDB(MetaInfo.included2kb);
		String line = null;
		for (LSDFact fact : facts) {
			line = fact.toString();
			for (String str : typeNames) {
				if (line.contains(str))
				{
					ruleChecker.loadFact(fact);
					facts2KB.add(fact);
				}
			}
		}
		resetWinnowing();
	}

	public void load2KBFactBase(ArrayList<LSDFact> facts) throws ParseException, TypeModeError, IOException{
		ruleChecker.loadAdditionalDB(MetaInfo.included2kb);
		for (LSDFact fact : facts) {
			ruleChecker.loadFact(fact);
			facts2KB.add(fact);
		}
		resetWinnowing();
	}
	
	// don't delete the facts but suppress them
	public void loadWinnowingRules(Collection<LSDRule> rules) {
		winnowingRules.addAll(rules);
		resetWinnowing();
		// winnow(); // Do it now to prevent unexpected delays later?
	}

	// FIXME What about rules that are false?  They're more complicated. XXX
	// FIXME Miryung: I changed this from public to package scope so that we can call this from a rule enumerator
	private void winnow() {
		ruleMatches = new HashMap<LSDRule, List<LSDFact>>();
		for (LSDRule rule : winnowingRules) {
			ArrayList<LSDFact> thisRuleMatches = new ArrayList<LSDFact>();
			List<Map<LSDVariable, String>> counterExamples = ruleChecker.getCounterExamples(rule);
			ruleExceptions.put(rule, counterExamples);
			ArrayList<LSDRule> resultingConclusions = ruleChecker.getTrueConclusions(rule);
			for (LSDRule matchedRule : resultingConclusions) {
				for (LSDLiteral generatedLiteral : matchedRule.getLiterals()) {
					if (!(generatedLiteral instanceof LSDFact))
					{
						System.out.println("Not a fact:" + generatedLiteral);
						continue;
					}
					LSDFact fact = ((LSDFact) generatedLiteral)
						.nonNegatedCopy();
					if (factsDeltaKB.contains(fact)) {
						thisRuleMatches.add(fact);
						matched.add(fact);
					}
					if (facts2KB.contains(fact)) {
						thisRuleMatches.add(fact);
						matched.add(fact);
					}
				}
			}
			
//			if (!counterExamples.isEmpty()) {
//				System.err.println("A rule entered with " + counterExamples.size() + " exceptions and "
//						+ thisRuleMatches.size() + " correct matches. ("
//						+ ((float) thisRuleMatches.size() / ((float) counterExamples.size() + thisRuleMatches.size())) + ")");
//				System.err.println("Rule: " + rule);
//			}

			ruleMatches.put(rule, thisRuleMatches);
		}
		winnowed = true;
	}
	
	void forceWinnowing() {
		winnow();
	}
	
	private void resetWinnowing() {
		winnowed = false;
	}
	public int num2KBFactSize() {
		return this.facts2KB.size();
	}
	public int numDeltaKBFactSize() {
		return this.factsDeltaKB.size();
	}
}

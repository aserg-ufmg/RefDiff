package lsd.facts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import lsd.io.LSDAlchemyRuleReader;
import lsd.io.LSDTyrubaFactReader;
import lsd.io.LSDTyrubaRuleChecker;
import lsd.rule.LSDBinding;
import lsd.rule.LSDFact;
import lsd.rule.LSDInvalidTypeException;
import lsd.rule.LSDLiteral;
import lsd.rule.LSDPredicate;
import lsd.rule.LSDRule;
import lsd.rule.LSDVariable;
import metapackage.MetaInfo;
import tyRuBa.engine.RuleBase;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;

public class LSDRuleEnumerator {
	private LSDTyrubaRuleChecker ruleChecker;

	private LSDTyrubaRuleChecker remainingRuleChecker;

	private int minMatches = 1;

	private int minMatchesPerLiteral = 0;

	private int maxExceptions = 10;

	private double minAccuracy = 0;

	private int beamSize = 100;

	private ArrayList<LSDFact> read2kbFacts = new ArrayList<LSDFact>();

	private ArrayList<LSDFact> readDeltaFacts = new ArrayList<LSDFact>();

	private ArrayList<LSDRule> winnowingRules = new ArrayList<LSDRule>();

	private ArrayList<LSDFact> workingSet2KB = new ArrayList<LSDFact>();

	private ArrayList<LSDFact> workingSetDeltaKB = new ArrayList<LSDFact>();


	public static ArrayList<LSDPredicate>  getUniquePredicates (Collection<LSDFact> facts, boolean antedecedent) { 
		TreeSet<String> predNames = new TreeSet<String>(); 
		for (LSDFact f : facts) { 
			LSDPredicate p = f.getPredicate(); 
			predNames.add(p.getName()); 
		}
		ArrayList<LSDPredicate> preds = new ArrayList<LSDPredicate>(); 
		
		for (String s: predNames) { 
			LSDPredicate p = LSDPredicate.getPredicate(s); 
			if (antedecedent && p.isAntecedentPredicate()) { 
				preds.add(p);
			} else {
				preds.add(p); 
			}
			
		}
		return preds;
	}
	private ArrayList<LSDRule> modifiedWinnowingRules = new ArrayList<LSDRule>();

	// MK: added for manipulating 2kb

	final LSdiffDistanceFactBase onDemand2KB;

	final LSdiffHierarchialDeltaKB onDemandDeltaKB;

	// Cumulative numbers
	// These are not in use, although they are not deleted
	public int statsGeneratedPartials = 0;

	public int statsEnqueuedPartials = 0;

	public int statsSavedPartials = 0;

	int statsGeneratedGroundings = 0;

	int statsEnqueuedGroundings = 0;

	int statsSavedGroundings = 0;

	int statsPartialValidQueryCount = 0;

	int statsGroundingConstantsQueryCount = 0;

	int statsGroundingValidQueryCount = 0;

	int statsGroundingExceptionsQueryCount = 0;

	double timeUngroundRuleGeneration;

	double timePartiallyGroundRuleGeneration;

	int numValidRules;

	int numRulesWithException;

	int num2KBSize;

	int numDeltaKBSize;

	int numWinnowDeltaKBSize;

	int numRemainingDeltaKBSize;

	int numFinalRules;

	private long enumerationTimestamp = 0;

	// ///////////

	private LSDFactBase fb;

	static int varNum = 0;

	public BufferedWriter output;

	private String resString;

	private int antecedantSize;

	private static final boolean isConclusion = true;

	private static final boolean isAntecedent = !isConclusion;

	long timer = 0, lastStart = 0;

	static {
		RuleBase.silent = true;
	}

	public LSDRuleEnumerator(File twoKBFile, File deltaKBFile,
			File winnowingRulesFile, File resultsFile, int minConcFact,
			double accuracy, int k, int beamSize2, int maxException,
			File modifiedWinnowingRulesFile, BufferedWriter output)
			throws Exception {
		setMinMatchesPerLiteral(0);
		setMaxExceptions(maxException);
		setBeamSize(beamSize);
		setMinMatches(minConcFact);
		setMinAccuracy(accuracy);
		setAntecedentSize(k);
		this.output = output;
		this.fb = new LSDFactBase();
		// reads input files and builds lists of facts
		startTimer();
		read2kbFacts = new LSDTyrubaFactReader(twoKBFile).getFacts();
		readDeltaFacts = new LSDTyrubaFactReader(deltaKBFile).getFacts();
		winnowingRules = new LSDAlchemyRuleReader(winnowingRulesFile)
				.getRules();

		// set onDemand database manipulators
		onDemand2KB = new LSdiffDistanceFactBase(read2kbFacts, readDeltaFacts);
		onDemandDeltaKB = new LSdiffHierarchialDeltaKB(readDeltaFacts);

		// set the modified winnowing rules
		modifiedWinnowingRules = new LSDAlchemyRuleReader(new File(
				MetaInfo.modifiedWinnowings)).getRules();
		stopTimer();
	}

	public LSDRuleEnumerator(ArrayList<LSDFact> input2kbFacts, 
			ArrayList<LSDFact> inputDeltaFacts,
			int minConcFact, double accuracy, int k, int beamSize2, int maxException,
			BufferedWriter output) throws Exception {

		setMinMatchesPerLiteral(0);
		setMaxExceptions(maxException);
		setBeamSize(beamSize);
		setMinMatches(minConcFact);
		setMinAccuracy(accuracy);
		setAntecedentSize(k);
		this.output = output;
		this.fb = new LSDFactBase();
		// reads input files and builds lists of facts
		startTimer();
		read2kbFacts = input2kbFacts;
		readDeltaFacts = inputDeltaFacts;
//		winnowingRules = inputWinnowingRules;

		// set onDemand database manipulators
		onDemand2KB = new LSdiffDistanceFactBase(read2kbFacts, readDeltaFacts);
		onDemandDeltaKB = new LSdiffHierarchialDeltaKB(readDeltaFacts);

		// set the modified winnowing rules
		modifiedWinnowingRules = new LSDAlchemyRuleReader(new File(
				MetaInfo.modifiedWinnowings)).getRules();
		stopTimer();
	}

	public void setAntecedentSize(int k) {
		this.antecedantSize = k;
	}

	public void setMinMatches(int minMatches) {
		this.minMatches = minMatches;
	}

	public void setMinMatchesPerLiteral(int minMatchesPerLiteral) {
		this.minMatchesPerLiteral = minMatchesPerLiteral;
	}

	public void setMaxExceptions(int maxExceptions) {
		this.maxExceptions = maxExceptions;
	}

	public void setMinAccuracy(double minAccuracy) {
		this.minAccuracy = minAccuracy;
	}

	public void setBeamSize(int beamSize) {
		this.beamSize = beamSize;
	}

	// Based on the level the function uses appropriate read lists and loads fb
	public void loadFactBases(int hopDistance2KB, LSdiffFilter filter)
			throws Exception {

		// use hop depth 1 for now to filter 2KB facts
		onDemand2KB.expand(hopDistance2KB);
		workingSet2KB = onDemand2KB.getWorking2KBFacts();

		TreeSet<LSDFact> workingDelta = new TreeSet<LSDFact>();
		onDemandDeltaKB.filterFacts(null, workingDelta, filter);
		workingSetDeltaKB = new ArrayList<LSDFact>(workingDelta);

		this.fb = new LSDFactBase(); 
		
		fb.load2KBFactBase(workingSet2KB);
		fb.loadDeltaKBFactBase(workingSetDeltaKB);
		fb.loadWinnowingRules(modifiedWinnowingRules);

		List<LSDFact> afterWinnowing = fb.getRemainingFacts(true);

		this.num2KBSize = fb.num2KBFactSize();
		this.numDeltaKBSize = fb.numDeltaKBFactSize();
		this.numWinnowDeltaKBSize = afterWinnowing.size();

		ruleChecker = createRuleChecker();
		remainingRuleChecker = createReducedRuleChecker(new ArrayList<LSDRule>());

		System.out.println("Number of 2kbFacts: " + num2KBSize);
		System.out.println("Number of deltaFacts: " + numDeltaKBSize);
	}

	private void swapFactBase(TreeSet<LSDFact> delta) throws Exception{ 
		LSDTyrubaRuleChecker newRuleChecker = new LSDTyrubaRuleChecker();
		ArrayList<LSDFact> twoKB = workingSet2KB;
		ArrayList<LSDFact> deltaKB = new ArrayList<LSDFact>(delta);
		workingSetDeltaKB = deltaKB; 
		newRuleChecker.loadAdditionalDB(MetaInfo.included2kb);
		for (LSDFact fact : twoKB)
			newRuleChecker.loadFact(fact);
		newRuleChecker.loadAdditionalDB(MetaInfo.includedDelta);
		for (LSDFact fact : deltaKB)
			newRuleChecker.loadFact(fact);
		ruleChecker = newRuleChecker;
		remainingRuleChecker= createReducedRuleChecker(new ArrayList<LSDRule>()); 
		System.out.println("[swapFactBase: Number of working 2kbFacts]\t: " + twoKB.size());
		System.out.println("[swapFactBase: Number of working deltaFacts]\t: " + delta.size());
	}
	// Primed fact bases are removed completely
	// Again here we should consider level
	// MK: make this rule checker always work only with respect to working 2KB
	// and working deltak
	private LSDTyrubaRuleChecker createRuleChecker() throws ParseException,
			TypeModeError, IOException {

		LSDTyrubaRuleChecker newRuleChecker = new LSDTyrubaRuleChecker();
		ArrayList<LSDFact> twoKB = workingSet2KB;
		ArrayList<LSDFact> deltaKB = workingSetDeltaKB;
		newRuleChecker.loadAdditionalDB(MetaInfo.included2kb);
		for (LSDFact fact : twoKB)
			newRuleChecker.loadFact(fact);
		newRuleChecker.loadAdditionalDB(MetaInfo.includedDelta);
		for (LSDFact fact : deltaKB)
			newRuleChecker.loadFact(fact);
		return newRuleChecker;
	}

	public LSDTyrubaRuleChecker createReducedRuleChecker(
			Collection<LSDRule> additionalRules) throws IOException,
			TypeModeError, ParseException {
		LSDTyrubaRuleChecker newRuleChecker = new LSDTyrubaRuleChecker();
		newRuleChecker.loadAdditionalDB(MetaInfo.included2kb);
		ArrayList<LSDFact> twoKB = workingSet2KB;
		ArrayList<LSDFact> deltaKB = workingSetDeltaKB;
		ArrayList<LSDRule> winnowing = modifiedWinnowingRules;

		for (LSDFact fact : twoKB)
			newRuleChecker.loadFact(fact);
		newRuleChecker.loadAdditionalDB(MetaInfo.includedDelta);
		LSDFactBase localFB = new LSDFactBase();
		localFB.load2KBFactBase(twoKB);
		localFB.loadDeltaKBFactBase(deltaKB);
		localFB.loadWinnowingRules(winnowing);
		localFB.loadWinnowingRules(additionalRules);

		List<LSDFact> afterWinnowing = localFB.getRemainingFacts(true);
		fb = localFB;
		this.num2KBSize = fb.num2KBFactSize();
		this.numDeltaKBSize = fb.numDeltaKBFactSize();
		this.numWinnowDeltaKBSize = afterWinnowing.size();

		for (LSDFact fact : afterWinnowing)
			newRuleChecker.loadFact(fact);
		return newRuleChecker;
	}

	// Creates a new ruleChecker with all deltaKB facts.
	public LSDTyrubaRuleChecker createRuleChecker(ArrayList<String> cluster)
			throws IOException, TypeModeError, ParseException {
		LSDTyrubaRuleChecker newRuleChecker = new LSDTyrubaRuleChecker();
		newRuleChecker.loadAdditionalDB(MetaInfo.included2kb);
		for (LSDFact fact : read2kbFacts)
			for (String str : cluster) {
				if (fact.toString().contains(str))
					newRuleChecker.loadFact(fact);
			}

		newRuleChecker.loadAdditionalDB(MetaInfo.includedDelta);
		for (LSDFact fact : readDeltaFacts)
			for (String str : cluster) {
				if (fact.toString().contains(str))
					newRuleChecker.loadFact(fact);
			}
		return newRuleChecker;
	}

	// The way timer works is changed
	// The filed timer is a continuous timer that is controlled with startTimer
	// and stopTimer
	private void startTimer() {
		lastStart = new Date().getTime();
	}

	private void stopTimer() {
		long temp = new Date().getTime() - lastStart;
		timer += temp;
	}

	private LSDVariable newFreeVariable(Collection<LSDVariable> variables,
			char type) {
		Set<String> varNames = new HashSet<String>();
		for (LSDVariable variable : variables)
			varNames.add(variable.getName());
		int i;
		for (i = 0; varNames.contains("x" + i); i++)
			;
		return new LSDVariable("x" + i, type);
	}

	private double nextEnumerationTiming() {
		long nowTime = new Date().getTime();
		double delta = (nowTime - enumerationTimestamp) / 1000.;
		enumerationTimestamp = nowTime;
		return delta;
	}

	// MK: copied from optimized diff
	// MK: I am going to skip java library constants. 
	private List<LSDRule> groundRule(LSDRule ungroundedRule) {
		ArrayList<LSDRule> rules = new ArrayList<LSDRule>();
		// Create a Stack of groundings.
		Stack<Grounding> groundings = new Stack<Grounding>();
		// Add the initial empty grounding with just a list of all vars.
		groundings.add(new Grounding(ungroundedRule));
		statsEnqueuedGroundings++;
		statsGeneratedGroundings++;
		// For each head of the stack:
		next: while (!groundings.isEmpty()) {
			// create all the possible groundings with the next var grounded (or
			// not)
			// add them to the queue.
			// Grounding grounding = groundings.remove();

			Grounding grounding = groundings.pop();
			LSDVariable variable = grounding.remainingVariables.iterator()
					.next();

			// (Assemble list of consts that could sub in for next var)
			startTimer();
			Set<String> constants = ruleChecker.getReplacementConstants(
					grounding.rule, variable);
			statsGroundingConstantsQueryCount++;
			constants.add(null);
			// For each constant of type of next var and for none:
			for (String constant : constants) {
				// Don't reuse constants. Ensures the generation of unique
				// rules.

				if (constant != null
						&& grounding.usedConstants.contains(constant))
					continue;
				
				// MK
				// FIXME: I decided to penalize constants with java library in learned rules. 
				if (constant !=null && constant.indexOf("java.")>0) 
					continue;
				// Substitute the constant in to a copy of the rule
				Grounding newGrounding = grounding.addGrounding(variable,
						constant);
				statsGeneratedGroundings++;
				// If rule is still valid and a query has results:
				// Do this in stages to avoid unecessary queries.
				if (newGrounding.rule.containsFacts())
					continue;
				int minMatchesByLength = minMatchesPerLiteral
						* (newGrounding.rule.getLiterals().size() - 1);
				startTimer();
				int numMatches = countRemainingMatches(newGrounding.rule);
				statsGroundingValidQueryCount++;
				if (numMatches < minMatches || numMatches < minMatchesByLength)
					continue;
				// add new grounding def. to the queue
				// If we can ground more variables, we'll repeat
				if (newGrounding.remainingVariables.size() > 0) {
					if (newGrounding.scanned) {
						if (newGrounding.isGrounded() && newGrounding.scanned
								&& newGrounding.rule.isValid()) {
							rules = addRule(rules, grounding,
									grounding.numMatches);
						} else if (grounding.scanned)
							continue next;
					}

					else {
						statsEnqueuedGroundings++;
						newGrounding.scanned = true;
						newGrounding.numMatches = numMatches;
						groundings.add(newGrounding);
					}
				} else if (newGrounding.rule.isValid()
						&& newGrounding.isGrounded())
					addRule(rules, newGrounding, numMatches);
			}
		}

		return rules;
	}

	// MK: copied from opitmized diff
	private List<LSDRule> groundRules(List<LSDRule> ungroundedRules) {
		// We now have a list of the fully-ungrounded rules. Now we'll add rules
		// to our list of partial groundings.
		ArrayList<LSDRule> rules = new ArrayList<LSDRule>();
		// For each ungrounded rule: create all (partial or full) groundings.
		int rulesGrounded = 0;
		for (LSDRule ungroundedRule : ungroundedRules) {
			if (rulesGrounded % 10 == 0) {
				System.err
						.println((((float) (rulesGrounded * 100)) / ungroundedRules
								.size())
								+ "% done.");
				System.err.flush();
			}
			rules.addAll(groundRule(ungroundedRule));
			rulesGrounded += 1;
		}
		return rules;

	}

	// MK: Copied from optimized diff.
	
	// return value: ungrounded rules 
	// 1st arg: old partial rules 
	// 2st arg; new partial rules
	private List<LSDRule> extendUngroundedRules(List<LSDRule> oldPartialRules,
			List<LSDRule> newPartialRules) {
		Set<LSDRule> ungroundedRules = new LinkedHashSet<LSDRule>();
		
		List<LSDPredicate> predicates = getUniquePredicates(workingSet2KB, true);
		System.out.println("[extendUngroundRules: predicates to add]\t"+predicates); 
		
		for (LSDRule partialRule : oldPartialRules) {
			List<LSDLiteral> previousLiterals = partialRule.getLiterals();
			LSDPredicate conclusionPredicate = partialRule.getConclusions()
					.getLiterals().get(0).getPredicate();
			Set<Character> currentTypes = new HashSet<Character>();
			for (LSDVariable variable : partialRule.getFreeVariables())
				currentTypes.add(variable.getType());
			// For each predicate:
			for (LSDPredicate predicate : predicates) {

				LSDPredicate antecedant = null;
				if (partialRule.getAntecedents() != null
						&& partialRule.getAntecedents().getLiterals().size() > 0) {
					antecedant = partialRule.getAntecedents().getLiterals()
							.get(0).getPredicate();
				}
				if (!predicate.allowedInSameRule(conclusionPredicate,
						antecedant))
					continue;

				// MK: check whether the predicate that we want to add has at least one type overlapping with previously learned rules
				if (!predicate.typeMatches(currentTypes))
					continue;
				
//				System.out.println("[extendedUngroundedRule]- trying adding a predicate to a partial rule:\t"+ predicate+"\t"+partialRule+ "\t"+currentTypes+"\t"+predicate.typeMatches(currentTypes) );
			
				// Create two lists of lists of bindings, one with [] in it.
				List<List<LSDBinding>> bindingsList = enumerateUngroundedBindings(
						partialRule, predicate);
				// For each now complete list of bindings:
				perBindings: for (List<LSDBinding> bindings : bindingsList) {
					statsGeneratedPartials++;
					LSDLiteral newLiteral = null;
					try {
						newLiteral = new LSDLiteral(predicate, bindings,
								isAntecedent);
					} catch (LSDInvalidTypeException e) {
						System.err
								.println("We're taking types directly from the predicates, so we should never have this type error.");
						System.exit(-7);
					}
					// If the new literal is identical to a previous one, skip
					// it and continue
					for (LSDLiteral oldLiteral : previousLiterals) {
						if (oldLiteral
								.identifiesSameIgnoringNegation(newLiteral))
							continue perBindings;
					}
					// Add that binding to the partial rule
					LSDRule newPartialRule = new LSDRule(partialRule);
					newPartialRule.addLiteral(newLiteral);
					// Add to the list of generated rules
					// Can be an invalid rule, but may become one with
					// grounding
					if (newPartialRule.literalsLinked()
							&& newPartialRule.hasValidLinks())// &&
																// !newPartialRule.isSamePreds())
					{
						int minMatchesByLength = minMatchesPerLiteral
								* (newPartialRule.getLiterals().size() - 1);
						startTimer();
						int numMatches = countRemainingMatches(newPartialRule,
								Math.max(minMatches, minMatchesByLength));
						statsPartialValidQueryCount++;
						if (numMatches >= minMatches
								&& numMatches >= minMatchesByLength) {
							// System.out.println(newPartialRule.toString());
							statsSavedPartials++;
							ungroundedRules.add(newPartialRule);
							statsEnqueuedPartials++;
							newPartialRules.add(newPartialRule);
						}
					}
				}
			}
		}
		return new ArrayList<LSDRule>(ungroundedRules);
	}

	// MK: copied from optimized diff
	private List<LSDRule> narrowSearch(List<LSDRule> partialRules,
			int currentLength) {
		ArrayList<LSDRule> chosenRules = new ArrayList<LSDRule>();
		ArrayList<LSDRule> sortedRules = sortRules(partialRules);
		int max = Math.min(beamSize, sortedRules.size());
		for (int i = 0; i < max; i++)
			chosenRules.add(sortedRules.get(i));
		return chosenRules;
	}

	public List<LSDFact> getRelevantFacts(LSDRule rule) {
		return fb.getRelevantFacts(rule);
	}
	public List<Map<LSDVariable, String>> getExceptions(LSDRule rule) {
		return fb.getExceptions(rule);
	}
	public List<LSDRule> levelIncrementLearning (PrintStream result) { 
		List<LSDRule> rules = null;
		try { 
			for (int level = 0; level <= LSdiffHierarchialDeltaKB.METHOD_LEVEL; level++) {
				// starting from top level
				if (level == LSdiffHierarchialDeltaKB.PACKAGE_LEVEL)
					loadFactBases(1, new LSdiffFilter(true,false,false,false,false));
				TreeSet<LSDFact> workingDeltaKB = onDemandDeltaKB
						.expandCluster(null, level);
				swapFactBase(workingDeltaKB);

				rules = enumerateRules(1);
				if (rules != null) {
					fb.loadWinnowingRules(rules);
					fb.forceWinnowing();
				}
				int cnt=0; 
				for (LSDRule r : rules) {
					result.println(r.toString());
					int matches = countMatches(r);
					int exceptions = countExceptions(r);
					if (exceptions>0) numRulesWithException++;
					result.println("#"+ cnt++ +"\t(" + matches + "/" + (matches+exceptions) + ")");
					result.println(r);
					for (LSDFact pfact: fb.getRelevantFacts(r)) 
						result.println("#P:\t"+ pfact);
					
				}
			}
		}catch (Exception e) {
			e.printStackTrace(); 
		}
		return rules; 
	}
	
	public List<LSDRule> levelIncrementLearning2 () {  
		List<LSDRule> packageLevelRules = null; 
		List<LSDRule> typeLevelRules = null;
		List<LSDRule> typeDependencyLevelRules = null;
		List<LSDRule> methodLevelRules = null;
		List<LSDRule> methodBodyLevelRules = null;
		List<LSDRule> fieldLevelRules = null;
			
		try { 
			List<LSDRule> previouslyLearnedRule = null; 
			for (int level = 0; level <= LSdiffHierarchialDeltaKB.BODY_LEVEL; level++) {
				// load fact bases
				if (level == LSdiffHierarchialDeltaKB.PACKAGE_LEVEL)
					loadFactBases(1, new LSdiffFilter(true,false,false,false,false));
				
				TreeSet<LSDFact> workingDeltaKB = onDemandDeltaKB
						.expandCluster(null, level);
				// expand previous rules 
				switch (level) { 
				case LSdiffHierarchialDeltaKB.PACKAGE_LEVEL:
					System.out.println("**PACKAGE_LEVEL**");
					packageLevelRules=  enumerateRules(1); 
					fb.loadWinnowingRules(packageLevelRules);
					break;
				case LSdiffHierarchialDeltaKB.TYPE_LEVEL:
					System.out.println("**TYPE_LEVEL**");
					assert (packageLevelRules!=null);
					typeLevelRules= extendPreviouslyLearnedRules(packageLevelRules); 
					fb.loadWinnowingRules(typeLevelRules);
					break;
				case LSdiffHierarchialDeltaKB.TYPE_DEPENDENCY_LEVEL: 
					System.out.println("**TYPE_DEP_LEVEL**");
					
					assert (typeLevelRules!=null);
					typeDependencyLevelRules = extendPreviouslyLearnedRules(typeLevelRules); 
					fb.loadWinnowingRules(typeDependencyLevelRules);
					break;
				case LSdiffHierarchialDeltaKB.METHOD_LEVEL: 
					System.out.println("**METHOD_LEVEL**");
					
					methodLevelRules = extendPreviouslyLearnedRules(typeLevelRules);
					fb.loadWinnowingRules(methodLevelRules);
					break;
				case LSdiffHierarchialDeltaKB.FIELD_LEVEL:
					System.out.println("**FIELD_LEVEL**");
					
					fieldLevelRules = extendPreviouslyLearnedRules(typeLevelRules); 
					fb.loadWinnowingRules(fieldLevelRules);
					break;
				case LSdiffHierarchialDeltaKB.BODY_LEVEL:  
					System.out.println("**BODY_LEVEL**");
					
					methodBodyLevelRules = extendPreviouslyLearnedRules(methodLevelRules); 
					fb.loadWinnowingRules(methodBodyLevelRules);
					break;
				default: 
					assert (false);
				}
				fb.forceWinnowing();
				
				swapFactBase(workingDeltaKB);
			}
		}catch (Exception e) {
			e.printStackTrace(); 
		}
		return null; 
	}	

	// MK; copied from optimized diff
	public List<LSDRule> onDemandLearning(List<LSDFact> cluster, int level) { 
		
		try {
			TreeSet<LSDFact> nextLevelWorkingDeltaKB = 	null; 
			
			if (level == LSdiffHierarchialDeltaKB.PACKAGE_LEVEL) { 
				onDemandDeltaKB.expandCluster(null, level); 
			}else if (level > LSdiffHierarchialDeltaKB.BODY_LEVEL){ 
				return null; 
			}else {
				onDemandDeltaKB.expandCluster(cluster, level);
			}
			List<LSDRule> rules;
			swapFactBase(nextLevelWorkingDeltaKB);
			rules = enumerateRules(1);
			
			if (rules != null) {
				fb.loadWinnowingRules(rules);
				fb.forceWinnowing();
			}
			
			System.err.println("Found Rules:" +rules.size()); 
			numValidRules = rules.size();
			List<LSDFact> factUncoveredByRules = fb.getRemainingFacts(true);
			numRemainingDeltaKBSize = factUncoveredByRules.size();

			int cnt=0; 
			for (LSDRule r : rules) {
				System.err.println(r.toString());
				int matches = countMatches(r);
				int exceptions = countExceptions(r);
				if (exceptions>0) numRulesWithException++;
				System.err.println("#"+ cnt++ +"\t(" + matches + "/" + (matches+exceptions) + ")");
				System.err.println(r);
				for (LSDFact pfact: fb.getRelevantFacts(r)) 
					System.err.println("#P:\t"+ pfact);
			}

		
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
		return null; 
	}

	public void bruteForceLearning(int hopDistance2KB,
			LSdiffFilter filter) {

		try {
			long st = new Date().getTime();
			List<LSDRule> rules;
			loadFactBases(hopDistance2KB, filter);

			rules = enumerateRules(antecedantSize);
			numValidRules = rules.size();

			if (rules != null) {
				fb.loadWinnowingRules(rules);
				fb.forceWinnowing();
			}

			List<LSDFact> remainingFacts = fb.getRemainingFacts(true);
			numRemainingDeltaKBSize = remainingFacts.size();

			System.err.println("Found Rules:" +rules.size()); 
			int cnt=1;
			for (LSDRule r : rules) {
				
				System.err.println(r.toString());
				int matches = countMatches(r);
				int exceptions = countExceptions(r);
				if (exceptions>0) numRulesWithException++;
				System.err.println("#"+ cnt+++"\t(" + matches + "/" + (matches+exceptions) + ")");
				System.err.println(r);
				for (LSDFact pfact: fb.getRelevantFacts(r)) 
					System.err.println("#P:\t"+ pfact);
			}

			Collection<LSDRule> selectedSubset;
			selectedSubset = coverSet(rules, true, null);
			
			System.err.println("Selected Rules:" +selectedSubset.size()); 
			
			for (LSDRule r : selectedSubset) {
				System.err.println(r.toString());
			}

			System.err.println("Remaining Facts:" +remainingFacts.size());  
			for (LSDFact f : remainingFacts) { 
				System.err.print(f); 
			}
			
			int cInfo = counttextual(selectedSubset);
			long en = new Date().getTime();
			output.write((Double.valueOf((en - st)) / 1000.00) + " \t "
					+ rules.size() + " \t " + selectedSubset.size() + " \t "
					+ numRemainingDeltaKBSize + " \t " + resString + cInfo);
			output.newLine();

			shutdown();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
		System.out.println("Done");

	}

	// MK: copied from optimized lsdiff
	Collection<LSDRule> coverSet(Collection<LSDRule> rules, boolean print,
			File rf) throws TypeModeError, FileNotFoundException,
			ParseException, IOException {
		List<LSDRule> chosenRules = new ArrayList<LSDRule>();
		List<LSDRule> remainingRules = new ArrayList<LSDRule>(rules);
		List<LSDFact> remainingFacts;
		HashMap<LSDRule, Integer> alreadyFoundExceptionCounts = new HashMap<LSDRule, Integer>();
		int startingNumFacts = -1;
		do {
			LSDFactBase fb = new LSDFactBase();
			LSDRule bestRule = null;
			int bestCount = 0;
			fb.load2KBFactBase(workingSet2KB);
			fb.loadDeltaKBFactBase(workingSetDeltaKB);
			fb.loadWinnowingRules(modifiedWinnowingRules);
			fb.loadWinnowingRules(chosenRules);
			remainingFacts = fb.getRemainingFacts(true);
			if (startingNumFacts == -1) // Only set this at the very beginning.
				startingNumFacts = remainingFacts.size();
			fb.loadWinnowingRules(remainingRules);
			for (Iterator<LSDRule> i = remainingRules.iterator(); i.hasNext();) {
				LSDRule rule = i.next();
				List<LSDFact> facts = fb.getRelevantFacts(rule);
				facts.retainAll(remainingFacts);
				int count = facts.size();
				if (count == 0) {
					i.remove();
					continue;
				}
				if (count > bestCount) {
					bestCount = count;
					bestRule = rule;
				} else if (count == bestCount) {
					// Compute exception counts, unless they already have been.
					// In that case, get the cached ones.
					Integer preFound = alreadyFoundExceptionCounts
							.get(bestRule);
					int exBestRule;
					if (preFound == null) {
						exBestRule = countExceptions(bestRule);
						alreadyFoundExceptionCounts.put(bestRule, exBestRule);
					} else {
						exBestRule = preFound;
					}
					preFound = alreadyFoundExceptionCounts.get(bestRule);
					int exRule;
					if (preFound == null) {
						exRule = countExceptions(rule);
						alreadyFoundExceptionCounts.put(rule, exRule);
					} else {
						exRule = preFound;
					}

					// Prefer rules with fewer exceptions
					if (exBestRule > exRule)
						bestRule = rule;
					// Given that, prefer more specific rules.
					else if (rule.generalityCompare(bestRule) < 0)
						bestRule = rule;
				}
			}
			if (bestRule != null) {
				chosenRules.add(bestRule);
				remainingRules.remove(bestRule);
				remainingFacts.removeAll(fb.getRelevantFacts(bestRule));
			}
		} while (!remainingFacts.isEmpty() && !remainingRules.isEmpty());

		LSDFactBase fb = new LSDFactBase();
		if (print) {
			fb.load2KBFactBase(workingSet2KB);
			fb.loadDeltaKBFactBase(workingSetDeltaKB);
			fb.loadWinnowingRules(modifiedWinnowingRules);
			fb.loadWinnowingRules(chosenRules);
			fb.forceWinnowing();
		}
		double coverage = Double.valueOf((startingNumFacts - remainingFacts
				.size()))
				/ Double.valueOf(startingNumFacts);
		double conciseness = Double.valueOf(startingNumFacts)
				/ Double.valueOf(chosenRules.size() + remainingFacts.size());
		this.resString = startingNumFacts + " \t " + coverage + " \t "
				+ conciseness + " \t ";
		// LogData(rf,remainingFacts,chosenRules,startingNumFacts);
		return chosenRules;
	}

	private ArrayList<LSDRule> addRule(ArrayList<LSDRule> rules,
			Grounding grounding, int numMatches) {
		double accuracy = measureAccuracy(grounding.rule, minAccuracy,
				maxExceptions, numMatches);
		statsGroundingExceptionsQueryCount++;
		if (accuracy >= minAccuracy) {
			statsSavedGroundings++;
			grounding.rule.setAccuracy(accuracy);
			grounding.rule.setNumMatches(numMatches);
			grounding.rule.setScore();
			rules.add(grounding.rule);
		}
		return rules;
	}

	List<List<LSDBinding>> enumerateUngroundedBindings(LSDRule partialRule,
			LSDPredicate predicate) {
		List<List<LSDBinding>> bindingsList = new ArrayList<List<LSDBinding>>();
		bindingsList.add(new ArrayList<LSDBinding>());
		Set<LSDVariable> ruleFreeVars = new HashSet<LSDVariable>(partialRule
				.getFreeVariables());
		// For each variable spot:
		for (char type : predicate.getTypes()) {
			List<List<LSDBinding>> newBindingsList = new ArrayList<List<LSDBinding>>();
			// For each partial binding pb
			for (List<LSDBinding> prevBindings : bindingsList) {
				// (Computing freevars-of-that-type union [new-var] for
				// below)
				Set<LSDVariable> freeVariables = new HashSet<LSDVariable>(
						ruleFreeVars);
				for (LSDBinding b : prevBindings)
					freeVariables.add(b.getVariable());
				List<LSDVariable> variableChoices = new ArrayList<LSDVariable>();
				for (LSDVariable v : freeVariables) {
					if (v.getType() == type)
						variableChoices.add(v);
				}
				variableChoices.add(newFreeVariable(freeVariables, type));
				// For each variable in freevars-of-that-type + new-var
				for (LSDVariable nextVariable : variableChoices) {
					// add pb + chosen-var to the 2nd queue of bindings
					ArrayList<LSDBinding> newBindings = new ArrayList<LSDBinding>(
							prevBindings);
					newBindings.add(new LSDBinding(nextVariable));
					newBindingsList.add(newBindings);
				}
			}
			// Swap the lists of partial bindings.
			bindingsList = newBindingsList;
		}
		for (Iterator<List<LSDBinding>> i = bindingsList.iterator(); i
				.hasNext();) {
			List<LSDBinding> bindings = i.next();
			boolean linked = false;
			for (LSDBinding b : bindings) {
				if (ruleFreeVars.contains(b.getVariable())) {
					linked = true;
					break;
				}
			}
			if (!linked)
				i.remove();
		}
		return bindingsList;
	}

	List<List<LSDBinding>> enumerateUngroundedBindings(LSDRule partialRule,
			LSDLiteral literal) {
		List<List<LSDBinding>> bindingsList = new ArrayList<List<LSDBinding>>();
		bindingsList.add(new ArrayList<LSDBinding>());
		Set<LSDVariable> ruleFreeVars = new HashSet<LSDVariable>(partialRule
				.getFreeVariables());
		// For each variable spot:
		for (LSDBinding binding : literal.getBindings()) {
			if (binding.isBound())
				continue;
			List<List<LSDBinding>> newBindingsList = new ArrayList<List<LSDBinding>>();
			// For each partial binding pb
			for (List<LSDBinding> prevBindings : bindingsList) {
				// (Computing freevars-of-that-type union [new-var] for
				// below)
				Set<LSDVariable> freeVariables = new HashSet<LSDVariable>(
						ruleFreeVars);
				for (LSDBinding b : prevBindings)
					freeVariables.add(b.getVariable());
				List<LSDVariable> variableChoices = new ArrayList<LSDVariable>();
				for (LSDVariable v : freeVariables) {
					if (v.getType() == binding.getType())
						variableChoices.add(v);
				}
				variableChoices.add(newFreeVariable(freeVariables, binding
						.getType()));
				// For each variable in freevars-of-that-type + new-var
				for (LSDVariable nextVariable : variableChoices) {
					// add pb + chosen-var to the 2nd queue of bindings
					ArrayList<LSDBinding> newBindings = new ArrayList<LSDBinding>(
							prevBindings);
					newBindings.add(new LSDBinding(nextVariable));
					newBindingsList.add(newBindings);
				}
			}
			// Swap the lists of partial bindings.
			bindingsList = newBindingsList;
		}
		for (Iterator<List<LSDBinding>> i = bindingsList.iterator(); i
				.hasNext();) {
			List<LSDBinding> bindings = i.next();
			boolean linked = false;
			for (LSDBinding b : bindings) {
				if (ruleFreeVars.contains(b.getVariable())) {
					linked = true;
					break;
				}
			}
			if (!linked)
				i.remove();
		}
		return bindingsList;
	}

	int countRemainingMatches(LSDRule rule) {
		// (Check if the rule has matches)
		return remainingRuleChecker.countTrueConclusions(rule);
	}

	int countRemainingMatches(LSDRule rule, int i) {
		// (Check if the rule has matches)
		return remainingRuleChecker.countTrueConclusions(rule, i);
	}

	public int countMatches(LSDRule rule) {
		// (Check if the rule has matches)
		return ruleChecker.countTrueConclusions(rule);
	}

	public int countExceptions(LSDRule rule) {
		// (Check if the rule has exceptions)
		return ruleChecker.countCounterExamples(rule);
	}

	int countExceptions(LSDRule rule, int max) {
		// (Check if the rule has exceptions)
		return ruleChecker.countCounterExamples(rule, max);
	}

	double measureAccuracy(LSDRule rule, double min, int maxExceptions,
			double matches) {
		int accuracyMaxExceptions = ((int) Math
				.floor((matches / min) - matches)) + 1;
		double exceptions = countExceptions(rule, Math.min(maxExceptions,
				accuracyMaxExceptions));
		if (exceptions >= maxExceptions)
			return 0;
		return matches / (matches + exceptions);
	}

	public void shutdown() {
		ruleChecker.shutdown();
		// FIXME XXX The following code shouldn't cause a null pointer
		// exception, should it? XXX
		// remainingRuleChecker.shutdown();
	}

	protected class Grounding {
		public int numMatches;

		public boolean scanned = false;

		public Set<LSDVariable> remainingVariables;

		public Set<String> usedConstants = new HashSet<String>();

		public LSDRule rule;

		public Grounding(LSDRule rule) {
			remainingVariables = new LinkedHashSet<LSDVariable>(rule
					.getFreeVariables());
			this.rule = rule;

		}

		public boolean isGrounded() {
			ArrayList<LSDLiteral> literalsList = rule.getLiterals();
			for (LSDLiteral literal : literalsList) {
				List<LSDBinding> bindingsList = literal.getBindings();
				for (LSDBinding binding : bindingsList) {
					if (binding.getGroundConst() != null)
						return true;
				}
			}
			return false;
		}

		public Grounding(Grounding oldGrounding) {
			this.remainingVariables = new HashSet<LSDVariable>(
					oldGrounding.remainingVariables);
			this.usedConstants = new HashSet<String>(oldGrounding.usedConstants);
			this.rule = oldGrounding.rule;
		}

		public Grounding addGrounding(LSDVariable variable, String constant) {
			Grounding newGrounding = new Grounding(this);

			assert remainingVariables.contains(variable) : ("Error: "
					+ remainingVariables + " doesn't contain " + variable);
			newGrounding.remainingVariables.remove(variable);
			if (constant != null) {
				assert !usedConstants.contains(constant);
				newGrounding.remainingVariables.remove(variable);
				newGrounding.usedConstants.add(constant);
				try {
					newGrounding.rule = rule.substitute(variable,
							new LSDBinding(constant));
				} catch (LSDInvalidTypeException e) {
					System.err
							.println("We're dealing with consts, so why type mismatch?");
					System.exit(-15);
				}
			}
			return newGrounding;
		}
	}



	private void LogData(File rf, List<LSDFact> remainingFacts,
			List<LSDRule> chosenRules, int startingNumFacts) throws IOException {
		BufferedWriter output = new BufferedWriter(new FileWriter(rf));
		if (!remainingFacts.isEmpty()) {
			output.write("The following facts (" + remainingFacts.size() + "/"
					+ startingNumFacts + ")were not matched by any rule:");
			output.newLine();
			for (LSDFact fact : remainingFacts) {
				output.write("\t" + fact);
				output.newLine();
			}
		} else
			output.write("Complete coverage.");
		output.newLine();
		for (LSDRule rule : chosenRules) {
			int matches = rule.getNumMatches();
			int exceptions = countExceptions(rule);
			output.newLine();
			output.write("\t" + rule + "\t(" + matches + "/"
					+ (matches + exceptions) + ")");
			output.newLine();
			for (LSDFact pfact : fb.getRelevantFacts(rule)) {
				output.write("\t#P:\t" + pfact);
				output.newLine();
			}

			if (exceptions > 0) {
				output.write("\t    Except:");
				output.newLine();
				for (Map<LSDVariable, String> exception : fb
						.getExceptions(rule)) {
					output.newLine();
					output.write("\t\t(");
					boolean first = true;
					for (LSDVariable var : exception.keySet()) {
						output.write((first ? "" : ", ") + var + "="
								+ exception.get(var));
						first = false;
					}
					output.write(")");
				}
				output.write("");
				output.newLine();
			}
		}
		output.close();
	}

	// counts number of contextual references
	private int counttextual(Collection<LSDRule> selectedSubset) {

		int count = 0;
		for (LSDRule rule : selectedSubset) {
			go: for (LSDLiteral literal : rule.getLiterals()) {
				for (LSDBinding bind : literal.getBindings()) {
					int i = 0;
					if (bind.isBound())
						for (LSDFact delta : readDeltaFacts) {
							if (delta.toString()
									.contains(bind.getGroundConst()))
								break;
							else
								i++;
						}
					if (i == readDeltaFacts.size()) {
						count++;
						break go;
					}
				}

			}
		}
		return count;
	}

	// Sorts rules based on score
	private ArrayList<LSDRule> sortRules(List<LSDRule> rules) {
		LSDRule[] temp = new LSDRule[rules.size()];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = rules.get(i);
		}
		Arrays.sort(temp, new LSDRule().new LSDRuleComparator());
		ArrayList<LSDRule> sortedList = new ArrayList<LSDRule>();
		for (LSDRule rule : temp) {
			sortedList.add(rule);
		}
		return sortedList;

	}

	// MK: copied from optimized diff
	public List<LSDRule> enumerateRules(int maxLiterals) {
		List<LSDRule> rules = new ArrayList<LSDRule>();
		List<LSDRule> partialRules = new ArrayList<LSDRule>(
				enumerateConclusions());
		statsGeneratedPartials += partialRules.size();
		statsEnqueuedPartials += partialRules.size();
		for (int currentLength = 1; currentLength <= maxLiterals; currentLength++) {

			System.out.println("Finding rules of length " + currentLength);
			List<LSDRule> newPartialRules = new ArrayList<LSDRule>();
			nextEnumerationTiming();
			List<LSDRule> ungroundedRules = extendUngroundedRules(partialRules,
					newPartialRules);
			double iterationTimeUngroundRuleGeneration = nextEnumerationTiming();
			timeUngroundRuleGeneration += iterationTimeUngroundRuleGeneration;
			
			System.out.println("Ungrounded rules, length " + currentLength
					+ ": " + iterationTimeUngroundRuleGeneration + " s");
			System.out.println("Total ungrounded rules generated: "+ungroundedRules.size());
			partialRules = newPartialRules;
			rules.addAll(groundRules(ungroundedRules));
			
			double iterationTimePartiallyGroundRuleGeneration = nextEnumerationTiming();
			timePartiallyGroundRuleGeneration += iterationTimePartiallyGroundRuleGeneration;
			System.out.println("Rule grounding, length " + currentLength + ": "
					+ iterationTimePartiallyGroundRuleGeneration + " s");
			System.out.println("Total grounded rules generated: "
					+ rules.size() + " rules");
			if (currentLength == maxLiterals)
				break;
			try {
				remainingRuleChecker.shutdown();
				remainingRuleChecker = createReducedRuleChecker(rules);
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
			System.out.println("Creating new rule checker: "
					+ nextEnumerationTiming() + " s");
			System.out.println("Enqueued partial rules: " + partialRules.size()
					+ " rules");

			partialRules = narrowSearch(newPartialRules, currentLength);
			System.out.println("Reduced enqueued partial rules: "
					+ partialRules.size() + " rules");
			System.out.println("Reducing partial rule set: "
					+ nextEnumerationTiming() + " s");
			if (partialRules.size() == 0)
				break;
		}
		return rules;
	}

	public List<LSDRule> extendPreviouslyLearnedRules(List<LSDRule> previouslyLearnedRules) {
		List<LSDRule> outcome = new ArrayList<LSDRule>();
		List<LSDRule> combineNewConsequentToPreviouslyLearnedRules = new ArrayList<LSDRule>();

		if (previouslyLearnedRules.size()>0) { 
			// create partial rules by setting a new consequent literal and previously learned antecedents
			for (LSDPredicate newConsequentPred : getUniquePredicates(workingSetDeltaKB, false)) {
//				System.out.println("[each new consequent]:\t"+newConsequentPred);
				for (LSDRule previousRule : previouslyLearnedRules) {

//					System.out.println("[previouslyLearnedRule]:\t" + previousRule);
					LSDRule previousAntecedents = previousRule.getAntecedents();
					// enumerate bindings for this new consequent literal based on previousAntecedent
					List<List<LSDBinding>> potentialBindingsForNewConsequentPredicate = enumerateUngroundedBindings(
							previousAntecedents, newConsequentPred);
					// For each now complete list of bindings:
//					System.out.println("[#potentialBindings]:\t" + potentialBindingsForNewConsequentPredicate.size());
					
					for (List<LSDBinding> bindingsForNewConsequent : potentialBindingsForNewConsequentPredicate) {
						LSDLiteral newConsequentLiteral = null;
						try {
							newConsequentLiteral = new LSDLiteral(newConsequentPred, bindingsForNewConsequent,
									isConclusion);
						} catch (LSDInvalidTypeException e) {
							System.err
							.println("We're taking types directly from the predicates, so we should never have this type error.");
							System.exit(-7);
						}

						// Add that binding to the partial rule
						LSDRule previousAntecedentsNewConsequent = new LSDRule(previousAntecedents);
						previousAntecedentsNewConsequent.addLiteral(newConsequentLiteral);

//						System.out
//						.println("[previousAntecedentsNewConsequent]:\t"
//								+ previousAntecedentsNewConsequent);
						combineNewConsequentToPreviouslyLearnedRules.add(previousAntecedentsNewConsequent);
					}
				}}
		}else { 
			// start from only conclusions 
			combineNewConsequentToPreviouslyLearnedRules = enumerateConclusions(); 
		}
		System.out.println("[# combineNewConsequentToPreviouslyLearnedRules]:\t"+combineNewConsequentToPreviouslyLearnedRules.size());
		
		List<LSDRule> newPartialRules = new ArrayList<LSDRule>();
		List<LSDRule> ungroundedRules = extendUngroundedRules(
				combineNewConsequentToPreviouslyLearnedRules, newPartialRules);
		
		System.out.println("[# ungroundedRules]:\t"+ungroundedRules.size());
//		for (LSDRule r:ungroundedRules) { 
//			System.out.println("\t\t"+r);
//		}
		List<LSDRule> groundRules = groundRules(ungroundedRules);
		System.out.println("[# groundRules]:\t"+groundRules.size()); 
//		for (LSDRule r:groundRules) { 
//			System.out.println("\t\t"+r);
//		}
		outcome.addAll(groundRules);

		try {
			remainingRuleChecker.shutdown();
			remainingRuleChecker = createReducedRuleChecker(outcome);
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return outcome;
	}

	// MK: copied from optimized diff
	private List<LSDRule> enumerateConclusions() {
		List<LSDRule> conclusions = new ArrayList<LSDRule>();
//		System.out.println("[enumerateConclusion: workingSetDeltaKB]:\t" +workingSetDeltaKB.size());
		System.out.println("[enumerateConclusion: getUniquePredicates]:\t" +getUniquePredicates(workingSetDeltaKB, false));
		
		for (LSDPredicate predicate : getUniquePredicates(workingSetDeltaKB, false)) {
			// Assign it all new free vars
			ArrayList<LSDBinding> bindings = new ArrayList<LSDBinding>();
			ArrayList<LSDVariable> variables = new ArrayList<LSDVariable>();
			for (char type : predicate.getTypes()) {
				LSDVariable nextVar = newFreeVariable(variables, type);
				variables.add(nextVar);
				bindings.add(new LSDBinding(nextVar));
			}
			// Add it to the queue of partial rules.
			LSDRule rule = new LSDRule();
			try {
				rule.addLiteral(new LSDLiteral(predicate, bindings,
						isConclusion));
			} catch (LSDInvalidTypeException e) {
				System.err
						.println("We're taking types directly from the predicates, so we should never have this type error.");
				System.exit(-7);
			}
			startTimer();
			int numMatches = countRemainingMatches(rule, minMatches);
			statsPartialValidQueryCount++;
			if (numMatches >= minMatches)
				conclusions.add(rule);
		}
		return conclusions;
	}
		
}
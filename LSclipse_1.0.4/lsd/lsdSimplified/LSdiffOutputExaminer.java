package lsdSimplified;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import lsd.facts.LSDFactBase;
import lsd.io.LSDAlchemyRuleReader;
import lsd.io.LSDTyrubaFactReader;
import lsd.rule.LSDFact;
import lsd.rule.LSDPredicate;
import lsd.rule.LSDRule;

public class LSdiffOutputExaminer {
	LSDFactBase localFB = new LSDFactBase();  
	ArrayList<LSDRule> lsdiffRules = new ArrayList<LSDRule>(); 
	File winnowingRulesFile= new File ("input/winnowingRules.rub"); 
	
	public static void main (String args[]){ 
		String project ="jfreechart"; 
		String oldVersion="0.9.10"; 
		String newVersion="0.9.11"; 
		File twoKBFile = new File(
				"/Volumes/gorillaHD2/LSdiff/Tyruba/lsd/jfreechart/"+oldVersion+"_"+newVersion+"2KB.rub");
		File deltaKBFile = new File ("/Volumes/gorillaHD2/LSdiff/Tyruba/lsd/jfreechart/"+oldVersion+"_"+newVersion+"delta.rub");
//		File lsdiffRuleFile= new File ("/Volumes/gorillaHD2/LSdiff/Tyruba/lsd/090"); 
		LSdiffOutputExaminer lsdiffExaminer = new LSdiffOutputExaminer(twoKBFile, deltaKBFile, null);  
		lsdiffExaminer.compute_LSdiff_FACTTYPE();
		lsdiffExaminer.print(System.out);
	}
	public LSdiffOutputExaminer(File twoKBFile, File deltaKBFile, File lsdiffRuleFile) { 
		try {
			// load factbases
			
			if (twoKBFile!=null) {
				ArrayList<LSDFact> twoKB = new LSDTyrubaFactReader(twoKBFile).getFacts(); 
				localFB.load2KBFactBase(twoKB);
			}
			if (deltaKBFile!=null) {
				ArrayList<LSDFact> deltaKB = new LSDTyrubaFactReader(deltaKBFile).getFacts();
				localFB.loadDeltaKBFactBase(deltaKB);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ArrayList<LSDRule> winnowingRules = new LSDAlchemyRuleReader(
				winnowingRulesFile).getRules();
		if (twoKBFile!=null && deltaKBFile!=null) localFB.loadWinnowingRules(winnowingRules);
		List<LSDFact> afterWinnowing = localFB.getRemainingFacts(true);
		if (lsdiffRuleFile!=null) 
			lsdiffRules = new LSDAlchemyRuleReader(lsdiffRuleFile).getRules();
	
	}
	// create a hash map where key is consequent type and value is a list of LSdiff facts 
	
	// lsdiff predicate is represented as a string. 
	private HashMap<String, ArrayList<LSDFact>> predicateToFacts = new HashMap<String, ArrayList<LSDFact>>(); 

	public void compute_LSdiff_FACTTYPE () { 
		 LinkedHashSet<LSDFact> deltaKBFacts = localFB.getDeltaKBFact(); 
		 for (LSDFact fact:deltaKBFacts) { 
			 LSDPredicate predicate = fact.getPredicate();
			 String predicateType = predicate.getName();
			 
			 ArrayList<LSDFact> facts = predicateToFacts.get(predicateType); 
			 if (facts==null) { 
				 facts = new ArrayList<LSDFact>(); 
				 predicateToFacts.put(predicateType,facts); 
			 }
			 facts.add(fact); 			 
		 }
	}
	public void print(PrintStream p) { 
		p.println("2KB Size:\t"+localFB.num2KBFactSize());
		p.println("DeltaKB Size:\t"+localFB.numDeltaKBFactSize());
		p.println("Categorization of Delta KB Facts");
		
		for (String predicateType: predicateToFacts.keySet()){ 
			 ArrayList<LSDFact> facts = predicateToFacts.get(predicateType); 
			 p.println(predicateType); 
			 p.println("# Facts:\t"+facts.size()); 	 
//			 for (LSDFact f :facts) { 
//				 p.println("\t"+f.toString());
//			 }
		}
	}
}

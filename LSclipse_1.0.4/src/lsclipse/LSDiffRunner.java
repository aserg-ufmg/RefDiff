package lsclipse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import lsclipse.dialogs.ProgressBarDialog;
import lsclipse.utils.StringCleaner;
import lsd.facts.LSDRuleEnumerator;
import lsd.rule.LSDFact;
import lsd.rule.LSDPredicate;
import lsd.rule.LSDRule;
import metapackage.MetaInfo;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

import changetypes.ASTVisitorAtomicChange;
import changetypes.AtomicChange;
import changetypes.AtomicChange.ChangeTypes;
import changetypes.ChangeSet;
import changetypes.Fact;
import changetypes.FactBase;

public class LSDiffRunner {
	private static final int NUM_THREADS = 4;
	private static final long TIMEOUT = 60; //wait 60 minutes for fact extraction
	private static Map<String, IJavaElement> oldTypeToFileMap_ = new ConcurrentHashMap<String, IJavaElement>();
	
	public static Map<String, IJavaElement> getOldTypeToFileMap() {
		return Collections.unmodifiableMap(oldTypeToFileMap_);
	}
	
	private static Map<String, IJavaElement> newTypeToFileMap_ = new ConcurrentHashMap<String, IJavaElement>();
	
	public static Map<String, IJavaElement> getNewTypeToFileMap() {
		return Collections.unmodifiableMap(newTypeToFileMap_);
	}
	
	public boolean doFactExtractionForRefFinder(String proj1, String proj2, ProgressBarDialog progbar) {
		if(!doFactExtraction(proj1, proj2, progbar))
			return false;
		installLSDiff();
		return true;
	}

	public List<LSDResult> doLSDiff(String proj1, String proj2, ProgressBarDialog progbar) {
		if(!doFactExtraction(proj1, proj2, progbar))
			return null;

		//somehow invoke LSDdiff
		progbar.setMessage("Invoking LSDiff... ");
		BufferedWriter output = null;
		LSDRuleEnumerator enumerator = null;
		List<LSDRule> rules = null;
		try {
			installLSDiff();	//install files if necessary

			File winnowingRulesFile = new File(MetaInfo.winnowings);
			File typeLevelWinnowingRulesFile = new File(MetaInfo.modifiedWinnowings);
			File resultsFile = new File(MetaInfo.resultsFile);
			File twoKBFile = new File(MetaInfo.lsclipse2KB);
			File deltaKBFile = new File(MetaInfo.lsclipseDelta);
			enumerator = new LSDRuleEnumerator(twoKBFile, deltaKBFile, winnowingRulesFile, resultsFile,
					MetaInfo.minConcFact,MetaInfo.accuracy, MetaInfo.k, MetaInfo.beamSize, MetaInfo.maxException, typeLevelWinnowingRulesFile, output);
			rules = enumerator.levelIncrementLearning(System.out);
		} catch (Exception e) {
			progbar.appendError("Unable to do LSDiff analysis");
			progbar.dispose();
			return null;
		}
		if (rules==null) {
			progbar.appendError("Unable to derive any rules!");
			progbar.dispose();
			return null;
		}
		progbar.appendLog("OK\n");
		progbar.appendLog("Found "+rules.size()+" rules\n");
		
		//return results
		List<LSDResult> res = new ArrayList<LSDResult>();
		for (LSDRule r : rules) {
			LSDResult result = new LSDResult();
			result.num_matches = enumerator.countMatches(r);
			result.num_counter = enumerator.countExceptions(r);
			result.desc = r.toString();
			result.examples = enumerator.getRelevantFacts(r);
			result.exceptions = enumerator.getExceptions(r);

			res.add(result);
		}

		progbar.setStep(5);
		progbar.setMessage("Cleaning up... ");
		progbar.appendLog("OK\n");

		progbar.dispose();
		
		
		return res;
	}

	private boolean doFactExtraction(String proj1, String proj2,
			ProgressBarDialog progbar) {
		Set<ICompilationUnit> allFiles = null;

		//Extraction for FB1
		progbar.setStep(1);
		progbar.setMessage("Retrieving facts for FB1... \n");
		FactBase fb1 = new FactBase();
		long beforefacts1 = System.currentTimeMillis();
		try {
			allFiles = getFiles(proj1);
		} catch (Exception e) {}
		if (allFiles == null)
			return false;
		progbar.appendLog("Scanning " + allFiles.size() + " files...");
		Iterator<ICompilationUnit> iter = allFiles.iterator();
		ExecutorService execService = Executors.newFixedThreadPool(NUM_THREADS);
		List<Future<FactBase>> futures = new LinkedList<Future<FactBase>>();
		while (iter.hasNext()) {
			ICompilationUnit file = iter.next();
			FactGetter fg = new FactGetter(file, oldTypeToFileMap_);
			futures.add(execService.submit(fg));
		}
		execService.shutdown();
		try {
			execService.awaitTermination(TIMEOUT, TimeUnit.MINUTES);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		for (Future<FactBase> f : futures) {
			try {
				fb1.addAll(f.get());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		int numFacts1 = fb1.size();
		progbar.appendLog("Extraction OK! Extracted "+numFacts1+" facts for FB1\n");
		@SuppressWarnings("unused")
		long afterfacts1 = System.currentTimeMillis();
		//Derived facts
		progbar.setMessage("Adding derived facts for FB1... \n");
		fb1.deriveFacts();
		progbar.appendLog("Derivation OK! Added "+(fb1.size()-numFacts1)+" facts to FB1\n");
		progbar.appendLog("All done! FB1 contains "+fb1.size()+" facts\n");

		long afterderivedfacts1 = System.currentTimeMillis();
		//Extraction for FB2
		progbar.setStep(2);
		progbar.setMessage("Retrieving facts for FB2... \n");
		FactBase fb2 = new FactBase();
		long beforefacts2 = System.currentTimeMillis();
		try {
			allFiles = getFiles(proj2);
		} catch (Exception e) {}
		if (allFiles == null)
			return false;
		progbar.appendLog("Scanning " + allFiles.size() + " files...");
		iter = allFiles.iterator();
		execService = Executors.newFixedThreadPool(NUM_THREADS);
		futures.clear();
		while (iter.hasNext()) {
			ICompilationUnit file = iter.next();
			FactGetter fg = new FactGetter(file, newTypeToFileMap_);
			futures.add(execService.submit(fg));
		}
		execService.shutdown();
		try {
			execService.awaitTermination(TIMEOUT, TimeUnit.MINUTES);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		for (Future<FactBase> f : futures) {
			try {
				fb2.addAll(f.get());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		int numFacts2 = fb2.size();
		progbar.appendLog("Extraction OK! Extracted "+numFacts2+" facts for FB2\n");
		@SuppressWarnings("unused")
		long afterfacts2 = System.currentTimeMillis();
		//Derived facts
		progbar.setMessage("Adding derived facts for FB2... \n");
		fb2.deriveFacts();
		progbar.appendLog("Derivation OK! Added "+(fb2.size()-numFacts2)+" facts to FB2\n");
		progbar.appendLog("All done! FB2 contains "+fb2.size()+" facts\n");
		long afterderivedfacts2 = System.currentTimeMillis();
		//Post processing
		progbar.setMessage("Doing post processing for FB2... ");
		progbar.appendLog("OK\n");

		//compute diff
		progbar.setStep(3);
		progbar.setMessage("Computing factbase differences... ");
		long beforediff = System.currentTimeMillis();
		ChangeSet cs = fb2.diff(fb1);
		progbar.appendLog("All done! "+cs.size()+" changes found\n");

		long afterdiff = System.currentTimeMillis();
		//LSDiff action
		progbar.setStep(4);
		//Convert into LSDFact/Rule format
		progbar.setMessage("Preparing to run LSDiff...\n");
		progbar.appendLog("Converting atomic change to LSDiff changes... ");
		long beforeconversion = System.currentTimeMillis();
		ArrayList<LSDFact> input2kbFacts = new ArrayList<LSDFact>();
		ArrayList<LSDFact> inputDeltaFacts = new ArrayList<LSDFact>();
		
		for (Fact f : fb1) {
			input2kbFacts.add(makeLSDFact(f, "before"));
		}
		System.out.println("***************************************");
		for (Fact f : fb2) {
			input2kbFacts.add(makeLSDFact(f, "after"));
		}
		
		
		for (AtomicChange ac : cs) {
		    LSDFact f = makeLSDFact(ac);
		   if (f != null)
				inputDeltaFacts.add(f);
		}
		
		progbar.appendLog("OK\n");
		long afterconversion = System.currentTimeMillis();

		//write to LSDiff input file
		progbar.appendLog("Writing to LSDiff input files... \n");
		BufferedWriter lsd2kbfile = null;
		long beforeoutput = System.currentTimeMillis();
		try {
			//First check if path exists -- if not, create it
			File f2KBfile = new File(MetaInfo.lsclipse2KB);
			File dir = f2KBfile.getParentFile();
			if (!dir.exists()) {
				dir.mkdirs();
			}
			//write away!
			progbar.appendLog("  Writing 2KB to "+MetaInfo.lsclipse2KB+"\n");
			lsd2kbfile = new BufferedWriter(new FileWriter(MetaInfo.lsclipse2KB));
			
			int counter = 0;
			for (LSDFact f : input2kbFacts) {
				counter = counter + 1;
				// System.out.println(counter);
				if (f != null)
					lsd2kbfile.append(f.toString()+".\n");
			}
			lsd2kbfile.close();
		} catch (IOException e) {
			progbar.appendError("Unable to create 2KB input file! Exiting...");
			progbar.dispose();
			return false;
		}
		BufferedWriter lsddeltafile = null;
		try {
			progbar.appendLog("  Writing deltas to "+MetaInfo.lsclipseDelta+"\n");
			lsddeltafile = new BufferedWriter(new FileWriter(MetaInfo.lsclipseDelta));
			lsddeltafile.close();
			for (LSDFact f : inputDeltaFacts) {
				lsddeltafile = new BufferedWriter(new FileWriter(MetaInfo.lsclipseDelta, true));
				lsddeltafile.append(f.toString()+".\n");
				lsddeltafile.close();
			}
		} catch (IOException e) {
			progbar.appendError("Unable to create delta KB input file! Exiting...");
			progbar.dispose();
			return false;
		}
		progbar.appendLog("OK\n");
		long afteroutput = System.currentTimeMillis();
		progbar.appendLog("\nTotal time for fb1 extraction(ms): " + (afterderivedfacts1 - beforefacts1));
		progbar.appendLog("\nTotal time for fb2 extraction(ms): " + (afterderivedfacts2 - beforefacts2));
		progbar.appendLog("\nTotal time for diff(ms): " + (afterdiff - beforediff));
		progbar.appendLog("\nTotal time for conversion to LSD(ms): " + (afterconversion - beforeconversion));
		progbar.appendLog("\nTotal time for write to file(ms): " + (afteroutput - beforeoutput));
		
		return true;
	}

	//if LSDiff is not installed, install the necessary files into metainfo directories 
	private static void installLSDiff() {	
		//first check if folders: input, output, and fdb are present
		File srcfile = (MetaInfo.srcDir);
		srcfile.mkdirs();
		File resfile = (MetaInfo.resDir);
		resfile.mkdirs();
		File fdbfile = (MetaInfo.fdbDir);
		fdbfile.mkdirs();
		//if 2KB_lsdPred not installed, install it now
		File included2KBFile = MetaInfo.included2kb;
		if (!included2KBFile.exists()) {
			InputStream is = lsclipse.LSclipse.getDefault().getClass().getResourceAsStream("/lib/"+included2KBFile.getName());
			writeStreamToFile(is, included2KBFile);
		}
		//if deltaKB_lsdPred not installed, install it now
		File includedDeltaKBFile = MetaInfo.includedDelta;
		if (!includedDeltaKBFile.exists()) {
			InputStream is = lsclipse.LSclipse.getDefault().getClass().getResourceAsStream("/lib/"+includedDeltaKBFile.getName());
			writeStreamToFile(is, includedDeltaKBFile);
		}
		//if winnowingrules not installed, install it now
		File winnowingRulesFile = new File(MetaInfo.winnowings);
		if (!winnowingRulesFile.exists()) {
			InputStream is = lsclipse.LSclipse.getDefault().getClass().getResourceAsStream("/lib/"+winnowingRulesFile.getName());
			writeStreamToFile(is, winnowingRulesFile);
		}
		//if newwinnowingrules not installed, install it now
		File typeLevelWinnowingRulesFile = new File(MetaInfo.winnowings);
		if (!typeLevelWinnowingRulesFile.exists()) {
			InputStream is = lsclipse.LSclipse.getDefault().getClass().getResourceAsStream("/lib/"+typeLevelWinnowingRulesFile.getName());
			writeStreamToFile(is, typeLevelWinnowingRulesFile);
		}
		//if deltaKB_primed_lsdPred not installed, install it now
		File includedPrimedDeltaKBFile = new File(MetaInfo.lsclipseRefactorDeltaPrimed);
		if (!includedPrimedDeltaKBFile.exists()) {
			InputStream is = lsclipse.LSclipse.getDefault().getClass().getResourceAsStream("/lib/"+includedPrimedDeltaKBFile.getName());
			writeStreamToFile(is, includedPrimedDeltaKBFile);
		}
		// if primed1 is not installed, install it now 
		File includedPred1File = new File(MetaInfo.lsclipseRefactorPred);
		if (!includedPred1File.exists()) {
			InputStream is = lsclipse.LSclipse.getDefault().getClass().getResourceAsStream("/lib/"+includedPred1File.getName());
			writeStreamToFile(is, includedPred1File);
		}
	}
	
	static class FactGetter implements Callable<FactBase> {
		Map<String, IJavaElement> typeToFileMap_;
		ICompilationUnit file_; 
		
		public FactGetter(ICompilationUnit file, Map<String, IJavaElement> typeToFileMap) {
			super();
			file_ = file;
			typeToFileMap_ = typeToFileMap;
		}

		@Override
		public FactBase call() throws Exception {
			//do some parsing
	        ASTParser parser = ASTParser.newParser(AST.JLS3);
	        parser.setResolveBindings(true);
	        parser.setSource(file_);
	        try {
		        parser.setUnitName(file_.getUnderlyingResource().getProjectRelativePath().toOSString());
	        } catch (JavaModelException e) {
				// This should not happen
				assert false;
			}
	        try {
	        	ASTVisitorAtomicChange acvisitor = new ASTVisitorAtomicChange();
		        ASTNode ast = parser.createAST(new NullProgressMonitor());
		        ast.accept(acvisitor);
		        typeToFileMap_.putAll(acvisitor.getTypeToFileMap());
		        
		        return acvisitor.facts;
	        } catch (Exception e){
	        	// TODO(kprete): figure out what the deal with these exceptions are and how to fix them
				System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
				System.out.println("Exception: " + e.getMessage());//Niki's edit
				//System.err.println("Error parsing "+file.getUnderlyingResource().getProjectRelativePath().toOSString()+" in project "+file.getUnderlyingResource().getProject().toString());
				return new FactBase();
			}
		}
		
		
		
	}

	private static LSDPredicate makeLSDPredicate(Fact.FactTypes type, String modifier) {
		switch (type) {
		case PACKAGE: 			return LSDPredicate.getPredicate(modifier+"_"+"package");
		case TYPE: 				return LSDPredicate.getPredicate(modifier+"_"+"type");
		case FIELD: 			return LSDPredicate.getPredicate(modifier+"_"+"field");
		case METHOD: 			return LSDPredicate.getPredicate(modifier+"_"+"method");

		case RETURN: 			return LSDPredicate.getPredicate(modifier+"_"+"return");
		case SUBTYPE:	 		return LSDPredicate.getPredicate(modifier+"_"+"subtype");

		case ACCESSES: 			return LSDPredicate.getPredicate(modifier+"_"+"accesses");
		case CALLS: 			return LSDPredicate.getPredicate(modifier+"_"+"calls");
		case INHERITEDFIELD: 	return LSDPredicate.getPredicate(modifier+"_"+"inheritedfield");
		case INHERITEDMETHOD: 	return LSDPredicate.getPredicate(modifier+"_"+"inheritedmethod");
		case FIELDOFTYPE: 		return LSDPredicate.getPredicate(modifier+"_"+"fieldoftype");
		case TYPEINTYPE:		return LSDPredicate.getPredicate(modifier+"_"+"typeintype");

		case EXTENDS: 			return LSDPredicate.getPredicate(modifier+"_"+"extends");
		case IMPLEMENTS: 		return LSDPredicate.getPredicate(modifier+"_"+"implements");
		case CONDITIONAL: 		return LSDPredicate.getPredicate(modifier+ "_"+"conditional");//Niki's edit
		case METHODBODY:		return LSDPredicate.getPredicate(modifier+"_"+"methodbody");
		case PARAMETER:			return LSDPredicate.getPredicate(modifier+ "_"+"parameter");
		case METHODMODIFIER: 	return LSDPredicate.getPredicate(modifier+ "_"+"methodmodifier");//Niki's edit
		case FIELDMODIFIER:		return LSDPredicate.getPredicate(modifier+ "_"+"fieldmodifier"); //Niki's edit
		
		// Kyle's edits
		case CAST:				return LSDPredicate.getPredicate(modifier+"_"+"cast");
		case TRYCATCH:			return LSDPredicate.getPredicate(modifier+"_"+"trycatch");
		case THROWN:			return LSDPredicate.getPredicate(modifier+"_"+"throws");
		case GETTER:			return LSDPredicate.getPredicate(modifier+"_"+"getter");
		case SETTER:			return LSDPredicate.getPredicate(modifier+"_"+"setter");
		case LOCALVAR:			return LSDPredicate.getPredicate(modifier+"_"+"localvar");
		
		default: return null;
		}
	}
	private static LSDFact makeLSDFact(Fact f, String modifier) {
		LSDPredicate pred = makeLSDPredicate(f.type, modifier);
		List<String> constants = new ArrayList<String>();
		int numparams = f.params.size();
		if (f.type==Fact.FactTypes.METHOD || f.type==Fact.FactTypes.FIELD || f.type==Fact.FactTypes.TYPE)
			numparams = numparams - 1; //for the 'visibility'/ 'typekind' field TODO: find a more elegant soln
		for (int i=0; i<numparams; ++i) {
			constants.add(StringCleaner.cleanupString(f.params.get(i)));
		}
		//Nikii's edit
		/*if (f.type == Fact.FactTypes.CONDITIONAL){
			System.out.println("************while making conditional fact******");
			System.out.println("The num of params is: " + numparams);
			System.out.println("The string is: " + constants + "and index of new line is " + constants.indexOf('\n') );
			constants = constants.replace('\n', ' ');
			System.out.println(constants);
		}*/
		//end of added code.
		return LSDFact.createLSDFact(pred, constants, true);
	}
	private static LSDPredicate makeLSDPredicate(AtomicChange ac) {
		String modifier = "";

		/*if (ac.type.ordinal()>=ChangeTypes.ADD_PACKAGE.ordinal() 
				&& ac.type.ordinal()<=ChangeTypes.ADD_TYPEINTYPE.ordinal()) { 
			modifier = "added";
		} else if (ac.type.ordinal()>=ChangeTypes.DEL_PACKAGE.ordinal() 
				&& ac.type.ordinal()<=ChangeTypes.DEL_TYPEINTYPE.ordinal()) {
			modifier = "deleted";
		} else {
			modifier = "modified";
		} Niki's edit
*/
		//Niki's edit
		if (ac.type.ordinal()>=ChangeTypes.ADD_PACKAGE.ordinal() 
				&& ac.type.ordinal()<=ChangeTypes.ADD_FIELDMODIFIER.ordinal()) { 
			modifier = "added";
		} else if (ac.type.ordinal()>=ChangeTypes.DEL_PACKAGE.ordinal() 
				&& ac.type.ordinal()<=ChangeTypes.DEL_FIELDMODIFIER.ordinal()) {
			modifier = "deleted";
		} else {
			modifier = "modified";
		}
		//end of added code.
		switch (ac.type) {
		case ADD_PACKAGE:
		case DEL_PACKAGE:
		case MOD_PACKAGE: 			return LSDPredicate.getPredicate(modifier+"_"+"package");
		case ADD_TYPE:
		case DEL_TYPE:
		case MOD_TYPE: 				return LSDPredicate.getPredicate(modifier+"_"+"type");
		case ADD_FIELD:
		case DEL_FIELD:
		case MOD_FIELD: 			return LSDPredicate.getPredicate(modifier+"_"+"field");
		case ADD_METHOD:
		case DEL_METHOD:
		case MOD_METHOD: 			return LSDPredicate.getPredicate(modifier+"_"+"method");

		case ADD_RETURN:
		case DEL_RETURN: 			return LSDPredicate.getPredicate(modifier+"_"+"return");
		case ADD_SUBTYPE:
		case DEL_SUBTYPE:	 		return LSDPredicate.getPredicate(modifier+"_"+"subtype");

		case ADD_ACCESSES:
		case DEL_ACCESSES:			return LSDPredicate.getPredicate(modifier+"_"+"accesses");
		case ADD_CALLS:
		case DEL_CALLS:				return LSDPredicate.getPredicate(modifier+"_"+"calls");
		case ADD_INHERITEDFIELD:
		case DEL_INHERITEDFIELD: 	return LSDPredicate.getPredicate(modifier+"_"+"inheritedfield");
		case ADD_INHERITEDMETHOD:
		case DEL_INHERITEDMETHOD: 	return LSDPredicate.getPredicate(modifier+"_"+"inheritedmethod");
		case ADD_FIELDOFTYPE:
		case DEL_FIELDOFTYPE: 		return LSDPredicate.getPredicate(modifier+"_"+"fieldoftype");
		case ADD_TYPEINTYPE:
		case DEL_TYPEINTYPE:		return LSDPredicate.getPredicate(modifier+"_"+"typeintype");

		case ADD_EXTENDS:
		case DEL_EXTENDS: 			return LSDPredicate.getPredicate(modifier+"_"+"extends");
		case ADD_IMPLEMENTS:
		case DEL_IMPLEMENTS: 		return LSDPredicate.getPredicate(modifier+"_"+"implements");
		
		case ADD_CONDITIONAL:	//Niki's edit
		case DEL_CONDITIONAL:	//Niki's edit
				return LSDPredicate.getPredicate(modifier+ "_" + "conditional"); //Niki's edit
				
		case ADD_METHODBODY:  //Niki's edit
		case DEL_METHODBODY:  //Niki's edit
				return LSDPredicate.getPredicate(modifier+ "_" + "methodbody");
				
		case ADD_PARAMETER: //Niki's edit
		case DEL_PARAMETER: //Niki's edit
				return LSDPredicate.getPredicate(modifier + "_" + "parameter");
				
		case ADD_METHODMODIFIER: //Niki's edit
		case DEL_METHODMODIFIER: //Niki's edit
				return LSDPredicate.getPredicate(modifier + "_" + "methodmodifier");
				
		case ADD_FIELDMODIFIER: //Niki's edit
		case DEL_FIELDMODIFIER: //Niki's edit
				return LSDPredicate.getPredicate(modifier + "_" + "fieldmodifier");
		//case MOD_CONDITIONAL:
		case ADD_CAST:
		case DEL_CAST:
				return LSDPredicate.getPredicate(modifier + "_" + "cast");
		case ADD_TRYCATCH:
		case DEL_TRYCATCH:
				return LSDPredicate.getPredicate(modifier + "_" + "trycatch");
		case ADD_THROWN:
		case DEL_THROWN:
				return LSDPredicate.getPredicate(modifier + "_" + "throws");
		case ADD_GETTER:
		case DEL_GETTER:
				return LSDPredicate.getPredicate(modifier + "_" + "getter");
		case ADD_SETTER:
		case DEL_SETTER:
				return LSDPredicate.getPredicate(modifier + "_" + "setter");
		case ADD_LOCALVAR:
		case DEL_LOCALVAR:
				return LSDPredicate.getPredicate(modifier + "_" + "localvar");
		default: return null;
		}
		
	}
	private static LSDFact makeLSDFact(AtomicChange ac) {
		LSDPredicate pred = makeLSDPredicate(ac);
		List<String> constants = new ArrayList<String>();
		for (String s : ac.params) {
			constants.add(StringCleaner.cleanupString(s));
		}
		// System.out.println("Niki......printing constants: " + constants);
		return LSDFact.createLSDFact(pred, constants, true);
	}
	/*
	private static FactBase collectFacts(String proj1, Set<String> changedFiles) {
		FactBase fb1 = new FactBase();
		FactBase fb;
		Iterator<String> iter = changedFiles.iterator();
		while (iter.hasNext()) {
			String filename = iter.next();
			System.out.print("  Now working on "+filename+" ... ");

			//collect facts for old revision
			String pathname1 = SVNNameToProjectName(filename, proj1);
			fb = getFacts(pathname1, proj1, ResourcesPlugin.getWorkspace().getRoot());
			fb1.addAll(fb);
			System.out.println("OK");
		}
		int numFacts1 = fb1.size();
		System.out.println("Deriving inheritance members for FB... ");
		fb1.deriveFacts();
		System.out.println("All done! Added "+(fb1.size()-numFacts1)+" facts to FB");
		return fb1;
	}*/
	private static Set<ICompilationUnit> getFiles(String projname) throws CoreException {
		IWorkspaceRoot ws = ResourcesPlugin.getWorkspace().getRoot();
		IProject proj = ws.getProject(projname);
		IJavaProject javaProject = JavaCore.create(proj);
		Set<ICompilationUnit> files = new HashSet<ICompilationUnit>();
		javaProject.open(new NullProgressMonitor());
		for( IPackageFragment packFrag : javaProject.getPackageFragments()) {
			for (ICompilationUnit icu : packFrag.getCompilationUnits()) {
				files.add(icu);
			}
		}
		javaProject.close();
		return files;
	}

	private static void writeStreamToFile(InputStream is, File file) {
		try {
			OutputStream out=new FileOutputStream(file);
		    byte buf[]=new byte[1024];
		    int len;
		    while((len=is.read(buf))>0)
		    out.write(buf,0,len);
		    out.close();
		    is.close();
		} catch (IOException e) {}
	}

}

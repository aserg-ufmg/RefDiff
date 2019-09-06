package refdiff.evaluation.performance;

import java.io.File;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.refactoringminer.api.Refactoring;

import gr.uom.java.xmi.UMLModel;
import gr.uom.java.xmi.UMLModelASTReader;
import gr.uom.java.xmi.diff.UMLModelDiff;
import refdiff.core.diff.CstComparator;
import refdiff.core.io.SourceFolder;
import refdiff.core.util.PairBeforeAfter;
import refdiff.evaluation.EvaluationUtils;
import refdiff.evaluation.RefactoringSet;
import refdiff.evaluation.RefactoringType;
import refdiff.evaluation.icse.IcseDataset;
import refdiff.parsers.java.JavaParser;

public class RunPerformanceComparison {
	
	public static EnumSet<RefactoringType> refactoringTypes = EnumSet.complementOf(EnumSet.of(RefactoringType.PULL_UP_ATTRIBUTE, RefactoringType.PUSH_DOWN_ATTRIBUTE, RefactoringType.MOVE_ATTRIBUTE));
	private EvaluationUtils evalUtils;
	
	public RunPerformanceComparison(String tempFolder) {
		evalUtils = new EvaluationUtils(new CstComparator(new JavaParser()), tempFolder);
	}
	
	public static void main(String[] args) throws Exception {
		new RunPerformanceComparison(args.length > 0 ? args[0] : "D:/refdiff/").run();
	}
	
	public void run() throws Exception {
		IcseDataset data = new IcseDataset();
		List<RefactoringSet> expected = data.getExpected();
		
//		ResultComparator rc = EvaluationCsvReader.buildResultComparator(data, EvaluationCsvReader.readEvalAll());
		
		for (int i = 0; i < expected.size(); i++) {
			RefactoringSet rs = expected.get(i);
			String project = rs.getProject();
			String commit = rs.getRevision();
			
			System.out.printf("%d/%d - ", i + 1, expected.size());
			evalUtils.prepareSourceCodeLightCheckout(project, commit);
			
			PairBeforeAfter<SourceFolder> sources = evalUtils.getSourceBeforeAfter(project, commit);
			PairBeforeAfter<Set<String>> folders = evalUtils.getRepositoryDirectoriesBeforeAfter(project, commit);
			
			List<Refactoring> refactorings = runRMiner(sources, folders);
			System.out.println(refactorings);
			break;
//			Map<KeyPair, String> explanations = new HashMap<>();
//			rc.compareWith("RefDiff", evalUtils.runRefDiff(project, commit, explanations, rs));
//			rc.addFnExplanations(project, commit, explanations);
			
		}
	}

	private List<Refactoring> runRMiner(PairBeforeAfter<SourceFolder> sources, PairBeforeAfter<Set<String>> folders) throws Exception {
		
		File rootFolder1 = sources.getBefore().getBasePath().get().toFile();
		File rootFolder2 = sources.getAfter().getBasePath().get().toFile();
		List<String> filePaths1 = sources.getBefore().getSourceFiles().stream().map(sf -> sf.getPath()).collect(Collectors.toList());
		List<String> filePaths2 = sources.getAfter().getSourceFiles().stream().map(sf -> sf.getPath()).collect(Collectors.toList());

		UMLModel model1 = new UMLModelASTReader(rootFolder1, filePaths1, folders.getBefore()).getUmlModel();
		UMLModel model2 = new UMLModelASTReader(rootFolder2, filePaths2, folders.getAfter()).getUmlModel();
		UMLModelDiff modelDiff = model1.diff(model2);
		return modelDiff.getRefactorings();
	}
	
}

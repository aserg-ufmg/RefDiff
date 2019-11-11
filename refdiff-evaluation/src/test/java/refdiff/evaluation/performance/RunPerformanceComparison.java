package refdiff.evaluation.performance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.glassfish.jersey.internal.util.Producer;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringMinerTimedOutException;

import gr.uom.java.xmi.UMLModel;
import gr.uom.java.xmi.UMLModelASTReader;
import gr.uom.java.xmi.diff.UMLModelDiff;
import refdiff.core.diff.CstComparator;
import refdiff.core.diff.CstDiff;
import refdiff.core.io.SourceFolder;
import refdiff.core.util.PairBeforeAfter;
import refdiff.evaluation.EvaluationUtils;
import refdiff.evaluation.RefactoringSet;
import refdiff.evaluation.RefactoringType;
import refdiff.evaluation.icse.IcseDataset;
import refdiff.parsers.java.JavaPlugin;

public class RunPerformanceComparison {
	
	public static EnumSet<RefactoringType> refactoringTypes = EnumSet.complementOf(EnumSet.of(RefactoringType.PULL_UP_ATTRIBUTE, RefactoringType.PUSH_DOWN_ATTRIBUTE, RefactoringType.MOVE_ATTRIBUTE));
	private EvaluationUtils evalUtils;
	
	public RunPerformanceComparison(String tempFolder) {
		evalUtils = new EvaluationUtils(new CstComparator(new JavaPlugin()), tempFolder);
	}
	
	public static void main(String[] args) throws Exception {
		new RunPerformanceComparison(args.length > 0 ? args[0] : "D:/refdiff/").run();
	}
	
	public void run() throws Exception {
		IcseDataset data = new IcseDataset();
		List<RefactoringSet> expected = data.getExpected();
		measureRefDiff(expected, "data/performance/refdiff1.txt");
		measureRMiner(expected, "data/performance/rminer1.txt");
		measureRefDiff(expected, "data/performance/refdiff2.txt");
		measureRMiner(expected, "data/performance/rminer2.txt");
		measureRefDiff(expected, "data/performance/refdiff3.txt");
		measureRMiner(expected, "data/performance/rminer3.txt");
	}

	private void measureRMiner(List<RefactoringSet> expected, String file) throws FileNotFoundException {
		try (PrintStream out = new PrintStream(file)) {
			out.printf("tool\tproject\tcommit\ttime\tfiles\n");
			for (int i = 0; i < expected.size(); i++) {
				RefactoringSet rs = expected.get(i);
				String project = rs.getProject();
				String projectName = project.substring("https://github.com/icse18-refactorings/".length(), project.length() - 4);
				String commit = rs.getRevision();
				
				System.out.printf("%d/%d - ", i + 1, expected.size());
				//evalUtils.prepareSourceCodeLightCheckout(project, commit);
				
				PairBeforeAfter<SourceFolder> sources = evalUtils.getSourceBeforeAfter(project, commit);
				PairBeforeAfter<Set<String>> folders = evalUtils.getRepositoryDirectoriesBeforeAfter(project, commit);
				int changedFiles = sources.getBefore().getSourceFiles().size() + sources.getAfter().getSourceFiles().size();
				
//				MeasuredResponse<CstDiff> measuredResponse = measureTime(() -> evalUtils.runRefDiff(sources));
				MeasuredResponse<List<Refactoring>> measuredResponse = measureTime(() -> runRMiner(sources, folders));
				printOutput(out, "RMiner", projectName, commit, measuredResponse.getTime(), changedFiles);
			}
		}
	}
	
	private void measureRefDiff(List<RefactoringSet> expected, String file) throws FileNotFoundException {
		try (PrintStream out = new PrintStream(file)) {
			out.printf("tool\tproject\tcommit\ttime\tfiles\n");
			for (int i = 0; i < expected.size(); i++) {
				RefactoringSet rs = expected.get(i);
				String project = rs.getProject();
				String projectName = project.substring("https://github.com/icse18-refactorings/".length(), project.length() - 4);
				String commit = rs.getRevision();
				
				System.out.printf("%d/%d - ", i + 1, expected.size());
				//evalUtils.prepareSourceCodeLightCheckout(project, commit);
				
				PairBeforeAfter<SourceFolder> sources = evalUtils.getSourceBeforeAfter(project, commit);
				int changedFiles = sources.getBefore().getSourceFiles().size() + sources.getAfter().getSourceFiles().size();
				
				MeasuredResponse<CstDiff> measuredResponse = measureTime(() -> evalUtils.runRefDiff(sources));
//				MeasuredResponse<List<Refactoring>> measuredResponse = measureTime(() -> runRMiner(sources, folders));
				printOutput(out, "RefDiff", projectName, commit, measuredResponse.getTime(), changedFiles);
			}
		}
	}
	
	private void printOutput(PrintStream out, String tool, String project, String commit, long time, int changedFiles) {
		out.printf("%s\t%s\t%s\t%d\t%d\n", tool, project, commit, time, changedFiles);
	}
	
	private List<Refactoring> runRMiner(PairBeforeAfter<SourceFolder> sources, PairBeforeAfter<Set<String>> folders) {
		File rootFolder1 = sources.getBefore().getBasePath().get().toFile();
		File rootFolder2 = sources.getAfter().getBasePath().get().toFile();
		List<String> filePaths1 = sources.getBefore().getSourceFiles().stream().map(sf -> sf.getPath()).collect(Collectors.toList());
		List<String> filePaths2 = sources.getAfter().getSourceFiles().stream().map(sf -> sf.getPath()).collect(Collectors.toList());
		
		UMLModel model1 = new UMLModelASTReader(rootFolder1, filePaths1, folders.getBefore()).getUmlModel();
		UMLModel model2 = new UMLModelASTReader(rootFolder2, filePaths2, folders.getAfter()).getUmlModel();
		try {
			UMLModelDiff modelDiff = model1.diff(model2);
			return modelDiff.getRefactorings();
		} catch (RefactoringMinerTimedOutException e) {
			throw new RuntimeException(e);
		}
	}
	
	private <T> MeasuredResponse<T> measureTime(Producer<T> producerFunction) {
		long timeBefore = System.currentTimeMillis();
		T response = producerFunction.call();
		long ellapsedTime = System.currentTimeMillis() - timeBefore;
		return new MeasuredResponse<T>(ellapsedTime, response);
	}
	
	private static class MeasuredResponse<T> {
		private final long time;
		private final T response;
		
		public MeasuredResponse(long time, T response) {
			super();
			this.time = time;
			this.response = response;
		}
		
		public long getTime() {
			return time;
		}
		
		public T getResponse() {
			return response;
		}
	}
}

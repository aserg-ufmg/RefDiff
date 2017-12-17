package refdiff.evaluation;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import refdiff.core.diff.RastComparator;
import refdiff.core.diff.RastComparatorThresholds;
import refdiff.core.diff.RastDiff;
import refdiff.core.diff.Relationship;
import refdiff.core.diff.RelationshipType;
import refdiff.core.diff.similarity.TfIdfSourceRepresentation;
import refdiff.core.diff.similarity.TfIdfSourceRepresentationBuilder;
import refdiff.core.io.FileSystemSourceFile;
import refdiff.core.io.GitHelper;
import refdiff.core.io.SourceFile;
import refdiff.parsers.java.JavaParser;
import refdiff.parsers.java.JavaSourceTokenizer;

public class RunCalibration {
	
	private EnumSet<RefactoringType> refactoringTypes = EnumSet.complementOf(EnumSet.of(RefactoringType.PULL_UP_ATTRIBUTE, RefactoringType.PUSH_DOWN_ATTRIBUTE, RefactoringType.MOVE_ATTRIBUTE));
	private JavaParser parser = new JavaParser();
	private JavaSourceTokenizer tokenizer = new JavaSourceTokenizer();
	private RastComparator<TfIdfSourceRepresentation> comparator = new RastComparator<>(parser, tokenizer, new TfIdfSourceRepresentationBuilder(), RastComparatorThresholds.DEFAULT);
	
	public static void main(String[] args) throws Exception {
		new RunCalibration().run();
	}
	
	public void run() throws Exception {
		CalibrationDataset cd = new CalibrationDataset();
		List<RefactoringSet> expected = cd.getExpected();
		
		ResultComparator rc = new ResultComparator();
		
		for (RefactoringSet rs : expected) {
			String project = rs.getProject();
			String commit = rs.getRevision();
			rc.expect(rs);
			rc.compareWith("RefDiff", runRefDiff(project, commit));
		}
		
		rc.printSummary(System.out, refactoringTypes);
	}

	private RefactoringSet runRefDiff(String project, String commit) throws Exception {
		RefactoringSet rs = new RefactoringSet(project, commit);
		
		String basePath = "D:/tmp/";
		String repoFolder = project.substring(project.lastIndexOf('/') + 1, project.lastIndexOf('.'));
		String checkoutFolder = repoFolder + "-" + commit.substring(0, 7) + "/";
		String checkoutFolderV0 = basePath + "v0/" + checkoutFolder;
		String checkoutFolderV1 = basePath + "v1/" + checkoutFolder;
		
		GitHelper gitHelper = new GitHelper();
		try (
			Repository repo = gitHelper.openRepository(basePath + repoFolder);
			RevWalk rw = new RevWalk(repo)) {
			
			RevCommit revCommit = rw.parseCommit(repo.resolve(commit));
			rw.parseCommit(revCommit.getParent(0));
			
			List<String> filesV0 = new ArrayList<>();
			List<String> filesV1 = new ArrayList<>();
			Map<String, String> renamedFilesHint = new HashMap<>();
			
			gitHelper.fileTreeDiff(repo, revCommit, filesV0, filesV1, renamedFilesHint, false);
			
			RastDiff diff = comparator.compare(getSourceFiles(checkoutFolderV0, filesV0), getSourceFiles(checkoutFolderV1, filesV1));
			
			System.out.println(project);
			for (Relationship rel : diff.getRelationships()) {
				if (rel.getType() != RelationshipType.SAME) {
					System.out.println(rel);
				}
			}
			System.out.println();
		}
		
		return rs;
	}

	private List<SourceFile> getSourceFiles(String checkoutFolder, List<String> files) {
		List<SourceFile> list = new ArrayList<>();
		for (String file : files) {
			list.add(new FileSystemSourceFile(Paths.get(checkoutFolder), Paths.get(file)));
		}
		return list;
	}
	
}

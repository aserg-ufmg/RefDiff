package refdiff.evaluation;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import refdiff.parsers.java.NodeTypes;

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
		
		int i = 0;
		for (RefactoringSet rs : expected) {
			String project = rs.getProject();
			String commit = rs.getRevision();
			rc.expect(rs);
			rc.compareWith("RefDiff", runRefDiff(project, commit));
			//if (i++ > 4) break;
		}
		
		rc.printSummary(System.out, refactoringTypes);
		rc.printDetails(System.out, refactoringTypes);
	}
	
	private RefactoringSet runRefDiff(String project, String commit) throws Exception {
		RefactoringSet rs = new RefactoringSet(project, commit);
		
		String basePath = "C:/tmp/";
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
			
			System.out.println(String.format("Computing diff for %s %s", project, commit));
			RastDiff diff = comparator.compare(getSourceFiles(checkoutFolderV0, filesV0), getSourceFiles(checkoutFolderV1, filesV1));
			
			for (Relationship rel : diff.getRelationships()) {
				RelationshipType relType = rel.getType();
				String nodeType = rel.getNodeAfter().getType();
				Optional<RefactoringType> refType = getRefactoringType(relType, nodeType);
				if (refType.isPresent()) {
					String keyN1 = JavaParser.getKey(rel.getNodeBefore());
					String keyN2 = JavaParser.getKey(rel.getNodeAfter());
					rs.add(refType.get(), keyN1, keyN2);
				}
			}
		}
		
		return rs;
	}
	
	private Optional<RefactoringType> getRefactoringType(RelationshipType relType, String nodeType) {
		boolean isType = nodeType.equals(NodeTypes.CLASS_DECLARATION) || nodeType.equals(NodeTypes.ENUM_DECLARATION) || nodeType.equals(NodeTypes.INTERFACE_DECLARATION);
		boolean isInterface = nodeType.equals(NodeTypes.INTERFACE_DECLARATION);
		boolean isMethod = nodeType.equals(NodeTypes.METHOD_DECLARATION);
		
		switch (relType) {
		case MOVE:
			if (isType) {
				return Optional.of(RefactoringType.MOVE_CLASS);
			} else if (isMethod) {
				return Optional.of(RefactoringType.MOVE_OPERATION);
			}
			break;
		case RENAME:
			if (isType) {
				return Optional.of(RefactoringType.RENAME_CLASS);
			} else if (isMethod) {
				return Optional.of(RefactoringType.RENAME_METHOD);
			}
			break;
		case EXTRACT:
			if (isMethod) {
				return Optional.of(RefactoringType.EXTRACT_OPERATION);
			}
			break;
		case INLINE:
			if (isMethod) {
				return Optional.of(RefactoringType.INLINE_OPERATION);
			}
			break;
		case PULL_UP:
			if (isMethod) {
				return Optional.of(RefactoringType.PULL_UP_OPERATION);
			}
			break;
		case PUSH_DOWN:
			if (isMethod) {
				return Optional.of(RefactoringType.PUSH_DOWN_OPERATION);
			}
			break;
		case EXTRACT_SUPER:
			if (isInterface) {
				return Optional.of(RefactoringType.EXTRACT_INTERFACE);
			} else if (isType) {
				return Optional.of(RefactoringType.EXTRACT_SUPERCLASS);
			}
			break;
		case SAME:
			return Optional.empty();
		}
		return Optional.empty();
		//throw new RuntimeException(String.format("Cannot convert to refactoring: %s %s", relType, nodeType));
	}
	
	private List<SourceFile> getSourceFiles(String checkoutFolder, List<String> files) {
		List<SourceFile> list = new ArrayList<>();
		for (String file : files) {
			list.add(new FileSystemSourceFile(Paths.get(checkoutFolder), Paths.get(file)));
		}
		return list;
	}
	
}

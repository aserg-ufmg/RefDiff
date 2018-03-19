package refdiff.evaluation;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import refdiff.core.diff.RastComparator;
import refdiff.core.diff.RastDiff;
import refdiff.core.diff.RastRootHelper;
import refdiff.core.diff.Relationship;
import refdiff.core.diff.RelationshipType;
import refdiff.core.io.FileSystemSourceFile;
import refdiff.core.io.GitHelper;
import refdiff.core.io.SourceFile;
import refdiff.parsers.java.JavaParser;
import refdiff.parsers.java.JavaSourceTokenizer;
import refdiff.parsers.java.NodeTypes;

public class EvaluationUtils {
	
	private JavaParser parser = new JavaParser();
	private JavaSourceTokenizer tokenizer = new JavaSourceTokenizer();
	private RastComparator comparator = new RastComparator(parser, tokenizer);
	private String tempFolder = "C:/tmp/";
	
	public EvaluationUtils(String tempFolder) {
		this.tempFolder = tempFolder;
	}

	public RefactoringSet runRefDiff(String project, String commit) throws Exception {
		RefactoringSet rs = new RefactoringSet(project, commit);
		
		File repoFolder = repoFolder(project);
		String checkoutFolderV0 = checkoutFolder(tempFolder, project, commit, "v0");
		String checkoutFolderV1 = checkoutFolder(tempFolder, project, commit, "v1");
		
		prepareSourceCode(project, commit);
		
		GitHelper gitHelper = new GitHelper();
		try (
			Repository repo = gitHelper.openRepository(repoFolder);
			RevWalk rw = new RevWalk(repo)) {
			
			RevCommit revCommit = rw.parseCommit(repo.resolve(commit));
			rw.parseCommit(revCommit.getParent(0));
			
			List<String> filesV0 = new ArrayList<>();
			List<String> filesV1 = new ArrayList<>();
			Map<String, String> renamedFilesHint = new HashMap<>();
			
			gitHelper.fileTreeDiff(repo, revCommit, filesV0, filesV1, renamedFilesHint, false);
			
			System.out.println(String.format("Computing diff for %s %s", project, commit));
			RastDiff diff = comparator.compare(getSourceFiles(checkoutFolderV0, filesV0), getSourceFiles(checkoutFolderV1, filesV1));
			
//			new RastRootHelper(diff.getAfter()).printRelationships(System.out);
			
			for (Relationship rel : diff.getRelationships()) {
				RelationshipType relType = rel.getType();
				String nodeType = rel.getNodeAfter().getType();
				Optional<RefactoringType> refType = getRefactoringType(relType, nodeType);
				if (refType.isPresent()) {
					if (refType.get().equals(RefactoringType.PULL_UP_OPERATION) && 
						diff.getRelationships().contains(new Relationship(RelationshipType.EXTRACT_SUPER, rel.getNodeBefore().getParent().get(), rel.getNodeAfter().getParent().get()))) {
						continue;
					}
					String keyN1 = JavaParser.getKey(rel.getNodeBefore());
					String keyN2 = JavaParser.getKey(rel.getNodeAfter());
					rs.add(refType.get(), keyN1, keyN2, rel.getSimilarity());
				}
			}
		}
		
		return rs;
	}

	private String checkoutFolder(String tempFolder, String project, String commit, String prefixFolder) {
		return tempFolder + prefixFolder + "/" + repoName(project) + "-" + commit.substring(0, 7) + "/";
	}

	private String repoName(String project) {
		return project.substring(project.lastIndexOf('/') + 1, project.lastIndexOf('.'));
	}

	public void prepareSourceCode2(String project, String commit) {
		System.out.println(String.format("Preparing %s %s", project, commit));
		String checkoutFolderV0 = checkoutFolder(tempFolder, project, commit, "v0");
		String checkoutFolderV1 = checkoutFolder(tempFolder, project, commit, "v1");
		File fRepoFolder = repoFolder(project);
		if (!fRepoFolder.exists()) {
			fRepoFolder.mkdirs();
			ExternalProcess.execute(fRepoFolder, "git", "init", "--bare");
			ExternalProcess.execute(fRepoFolder, "git", "remote", "add", "origin", project);
		}
		File fCheckoutFolderV0 = new File(checkoutFolderV0);
		File fCheckoutFolderV1 = new File(checkoutFolderV1);
		
		if (!fCheckoutFolderV0.exists() || !fCheckoutFolderV1.exists()) {
			String tagName = "refs/tags/r-" + commit.substring(0, 7);
			
			ExternalProcess.execute(fRepoFolder, "git", "fetch", "--depth", "2", "origin", tagName);
			
			if (fCheckoutFolderV0.mkdirs()) {
				ExternalProcess.execute(fRepoFolder, "git", "--work-tree=" + checkoutFolderV0, "checkout", commit + "~", "--", ".");
			}
			if (fCheckoutFolderV1.mkdirs()) {
				ExternalProcess.execute(fRepoFolder, "git", "--work-tree=" + checkoutFolderV1, "checkout", commit, "--", ".");
			}
		}
	}

	public void prepareSourceCode(String project, String commit) {
		System.out.println(String.format("Preparing %s %s", project, commit));
		String checkoutFolderV0 = checkoutFolder(tempFolder, project, commit, "v0");
		String checkoutFolderV1 = checkoutFolder(tempFolder, project, commit, "v1");
		File fRepoFolder = repoFolder(project);
		if (!fRepoFolder.exists()) {
			ExternalProcess.execute(new File(tempFolder), "git", "clone", project, "--bare", "--shallow-since=2015-05-01");
			// fRepoFolder.mkdirs();
			// ExternalProcess.execute(fRepoFolder, "git", "init", "--bare");
			// ExternalProcess.execute(fRepoFolder, "git", "remote", "add", "origin", project);
		}
		File fCheckoutFolderV0 = new File(checkoutFolderV0);
		File fCheckoutFolderV1 = new File(checkoutFolderV1);
		if (!fCheckoutFolderV0.exists() || !fCheckoutFolderV1.exists()) {
			// ExternalProcess.execute(fRepoFolder, "git", "fetch", "--depth", "2", "origin", commit);
			try {
				if (!fCheckoutFolderV0.exists() && fCheckoutFolderV0.mkdirs()) {
					ExternalProcess.execute(fRepoFolder, "git", "--work-tree=" + checkoutFolderV0, "checkout", commit + "~", "--", ".");
				}
				if (!fCheckoutFolderV1.exists() && fCheckoutFolderV1.mkdirs()) {
					ExternalProcess.execute(fRepoFolder, "git", "--work-tree=" + checkoutFolderV1, "checkout", commit, "--", ".");
				}
			} catch (RuntimeException e) {
				fCheckoutFolderV0.delete();
				fCheckoutFolderV1.delete();
				throw new RuntimeException(String.format("Error checking out %s %s:\n%s", project, commit, e.getMessage()), e);
			}
		}
	}
	
	private File repoFolder(String project) {
		return new File(tempFolder + repoName(project) + ".git");
	}
	
	private List<SourceFile> getSourceFiles(String checkoutFolder, List<String> files) {
		List<SourceFile> list = new ArrayList<>();
		for (String file : files) {
			list.add(new FileSystemSourceFile(Paths.get(checkoutFolder), Paths.get(file)));
		}
		return list;
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
		case PULL_UP_SIGNATURE:
			if (isMethod) {
				return Optional.of(RefactoringType.PULL_UP_OPERATION);
			}
			break;
		case PUSH_DOWN:
		case PUSH_DOWN_IMPL:
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
}

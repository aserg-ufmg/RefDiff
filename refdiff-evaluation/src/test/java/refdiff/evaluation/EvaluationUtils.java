package refdiff.evaluation;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import refdiff.core.cst.CstNode;
import refdiff.core.diff.CstComparator;
import refdiff.core.diff.CstComparator.DiffBuilder;
import refdiff.core.diff.CstComparatorMonitor;
import refdiff.core.diff.CstDiff;
import refdiff.core.diff.Relationship;
import refdiff.core.diff.RelationshipType;
import refdiff.core.io.FilePathFilter;
import refdiff.core.io.GitHelper;
import refdiff.core.io.SourceFileSet;
import refdiff.core.io.SourceFolder;
import refdiff.core.util.PairBeforeAfter;
import refdiff.parsers.java.JavaPlugin;
import refdiff.parsers.java.NodeTypes;

public class EvaluationUtils {
	
	private CstComparator comparator = new CstComparator(new JavaPlugin());
	private String tempFolder = "D:/tmp/";
	/**
	 * The ICSE dataset don't describe the qualified name of the extracted/inlined method.
	 */
	private boolean workAroundExtractAndInlineInconsistencies = true;
	
	public EvaluationUtils(String tempFolder) {
		this.tempFolder = tempFolder;
	}
	
	public EvaluationUtils(CstComparator comparator, String tempFolder) {
		this.comparator = comparator;
		this.tempFolder = tempFolder;
	}
	
	public RefactoringSet runRefDiff(String project, String commit) throws Exception {
		return runRefDiff(project, commit, new HashMap<>(), null);
	}
	
	public CstDiff computeDiff(String project, String commit) throws Exception {
		File repoFolder = repoFolder(project);
		String checkoutFolderV0 = checkoutFolder(tempFolder, project, commit, "v0");
		String checkoutFolderV1 = checkoutFolder(tempFolder, project, commit, "v1");
		
		prepareSourceCode(project, commit);
		
		GitHelper gitHelper = new GitHelper();
		try (
			Repository repo = GitHelper.openRepository(repoFolder);
			RevWalk rw = new RevWalk(repo)) {
			
			RevCommit revCommit = rw.parseCommit(repo.resolve(commit));
			rw.parseCommit(revCommit.getParent(0));
			
			List<String> filesV0 = new ArrayList<>();
			List<String> filesV1 = new ArrayList<>();
			Map<String, String> renamedFilesHint = new HashMap<>();
			
			gitHelper.fileTreeDiff(repo, revCommit, filesV0, filesV1, renamedFilesHint, false, comparator.getLanguagePlugin().getAllowedFilesFilter());
			
			System.out.println(String.format("Computing diff for %s %s", project, commit));
			
			CstDiff cstDiff = comparator.compare(getSourceFiles(checkoutFolderV0, filesV0), getSourceFiles(checkoutFolderV1, filesV1));
			
			System.out.println("Done computing");
			
			return cstDiff;
		}
	}
	
	public RefactoringSet runRefDiff(String project, String commit, Map<KeyPair, String> explanations, RefactoringSet expected) throws Exception {
		
		PairBeforeAfter<SourceFolder> sourceBeforeAfter = getSourceBeforeAfter(project, commit);
		
		RefactoringSetBuilder rsBuilder = new RefactoringSetBuilder(project, commit, expected, explanations);
		comparator.compare(sourceBeforeAfter.getBefore(), sourceBeforeAfter.getAfter(), rsBuilder);
		
		return rsBuilder.getRs();
	}
	
	public CstDiff runRefDiff(PairBeforeAfter<SourceFolder> sourceBeforeAfter) {
		return comparator.compare(sourceBeforeAfter.getBefore(), sourceBeforeAfter.getAfter());
	}

	public PairBeforeAfter<SourceFolder> getSourceBeforeAfter(String project, String commit) {
		String checkoutFolderV0 = checkoutFolder(tempFolder, project, commit, "v0");
		String checkoutFolderV1 = checkoutFolder(tempFolder, project, commit, "v1");
		
		if (!new File(checkoutFolderV0).exists() || !new File(checkoutFolderV1).exists()) {
			throw new RuntimeException(project + " " + commit + " not prepared");
		}
		
		System.out.println(String.format("Computing diff for %s %s", project, commit));
		
		SourceFolder sourceSetBefore = SourceFolder.from(Paths.get(checkoutFolderV0), ".java");
		SourceFolder sourceSetAfter = SourceFolder.from(Paths.get(checkoutFolderV1), ".java");
		PairBeforeAfter<SourceFolder> sourceBeforeAfter = new PairBeforeAfter<SourceFolder>(sourceSetBefore, sourceSetAfter);
		return sourceBeforeAfter;
	}

	public RefactoringSet runRefDiffGit(String project, String commit, Map<KeyPair, String> explanations) throws Exception {
		File repoFolder = repoFolder(project);
		try (Repository repo = GitHelper.openRepository(repoFolder)) {
			PairBeforeAfter<SourceFileSet> sourcesBeforeAfter = GitHelper.getSourcesBeforeAndAfterCommit(repo, commit, comparator.getLanguagePlugin().getAllowedFilesFilter());
			System.out.println(String.format("Computing diff for %s %s", project, commit));
			
			RefactoringSetBuilder rsBuilder = new RefactoringSetBuilder(project, commit, null, explanations);
			comparator.compare(sourcesBeforeAfter.getBefore(), sourcesBeforeAfter.getAfter(), rsBuilder);
			
			return rsBuilder.getRs();
		}
	}
	
	private class RefactoringSetBuilder implements CstComparatorMonitor {
		private final String project;
		private final String commit;
		private final RefactoringSet expected;
		private RefactoringSet rs;
		private final Map<KeyPair, String> explanation;
		
		public void reportDiscardedMatch(CstNode n1, CstNode n2, double score) {
			KeyPair keyPair = normalizeNodeKeys(n1, n2, false, false);
			explanation.put(keyPair, String.format("Threshold %.3f", score));
		}
		
		public void reportDiscardedConflictingMatch(CstNode n1, CstNode n2) {
			KeyPair keyPair = normalizeNodeKeys(n1, n2, false, false);
			explanation.put(keyPair, "Conflicting match");
		}
		
		public void reportDiscardedExtract(CstNode n1, CstNode n2, double score) {
			KeyPair keyPair = normalizeNodeKeys(n1, n2, true, false);
			explanation.put(keyPair, String.format("Threshold %.3f", score));
		}
		
		public void reportDiscardedInline(CstNode n1, CstNode n2, double score) {
			KeyPair keyPair = normalizeNodeKeys(n1, n2, false, true);
			explanation.put(keyPair, String.format("Threshold %.3f", score));
		}
		
		public RefactoringSetBuilder(String project, String commit, RefactoringSet expected, Map<KeyPair, String> explanation) {
			this.project = project;
			this.commit = commit;
			this.expected = expected;
			this.explanation = explanation;
		}

		@Override
		public void afterCompare(long elapsedTime, DiffBuilder<?> diffBuilder) {
			CstDiff diff = diffBuilder.getDiff();
			rs = new RefactoringSet(project, commit);
			for (Relationship rel : diff.getRelationships()) {
				RelationshipType relType = rel.getType();
				String nodeType = rel.getNodeAfter().getType();
				Optional<RefactoringType> optRefType = getRefactoringType(relType, nodeType);
				CstNode n1 = rel.getNodeBefore();
				CstNode n2 = rel.getNodeAfter();
				if (optRefType.isPresent()) {
					RefactoringType refType = optRefType.get();
					boolean copyN1Parent = refType.equals(RefactoringType.EXTRACT_OPERATION);
					boolean copyN2Parent = refType.equals(RefactoringType.INLINE_OPERATION) || refType.equals(RefactoringType.RENAME_METHOD);
					
					if (refType.equals(RefactoringType.EXTRACT_INTERFACE)) {
						n1 = diffBuilder.matchingNodeAfter(n1).get();
					}
					KeyPair keyPair = normalizeNodeKeys(n1, n2, copyN1Parent, copyN2Parent);
					
					RefactoringRelationship normalizedRefactoring = new RefactoringRelationship(refType, keyPair.getKey1(), keyPair.getKey2(), rel);
					
					if (refType.equals(RefactoringType.PULL_UP_OPERATION) &&
						diff.getRelationships().contains(new Relationship(RelationshipType.EXTRACT_SUPER, rel.getNodeBefore().getParent().get(), rel.getNodeAfter().getParent().get()))
						) {
						if (expected == null || !expected.getRefactorings().contains(normalizedRefactoring)) {
							continue;
						}
					}
					
					
					rs.add(normalizedRefactoring);
				} else {
					KeyPair keyPair = normalizeNodeKeys(n1, n2, false, false);
					if (relType.equals(RelationshipType.SAME) && n1.getParent().isPresent() && n2.getParent().isPresent() && expected != null) {
						CstNode n1Parent = rel.getNodeBefore().getParent().get();
						CstNode n2Parent = rel.getNodeAfter().getParent().get();
						
						KeyPair keyPairParent = normalizeNodeKeys(n1Parent, n2Parent, false, false);
						
						RefactoringRelationship moveOpRef = new RefactoringRelationship(RefactoringType.MOVE_OPERATION, keyPair.getKey1(), keyPair.getKey2());
						RefactoringRelationship moveClassRef = new RefactoringRelationship(RefactoringType.MOVE_CLASS, keyPairParent.getKey1(), keyPairParent.getKey2());
						RefactoringRelationship renameClassRef = new RefactoringRelationship(RefactoringType.RENAME_CLASS, keyPairParent.getKey1(), keyPairParent.getKey2());
						
						if (expected.getRefactorings().contains(moveOpRef) && (expected.getRefactorings().contains(moveClassRef) || expected.getRefactorings().contains(renameClassRef))) {
							//rs.add(new RefactoringRelationship(RefactoringType.MOVE_OPERATION, keyPair.getKey1(), keyPair.getKey2(), rel));
							//System.out.println("MACACO");
						}
					}
				}
			}
		}

		public RefactoringSet getRs() {
			return rs;
		}
	}
	
	private KeyPair normalizeNodeKeys(CstNode n1, CstNode n2, boolean copyN1Parent, boolean copyN2Parent) {
		String keyN1 = JavaPlugin.getKey(n1);
		String keyN2 = JavaPlugin.getKey(n2);
		
		if (workAroundExtractAndInlineInconsistencies) {
			if (copyN1Parent) {
				keyN2 = keyN1.substring(0, keyN1.lastIndexOf('.') + 1) + keyN2.substring(keyN2.lastIndexOf('.') + 1);
			} else if (copyN2Parent) {
				keyN1 = keyN2.substring(0, keyN2.lastIndexOf('.') + 1) + keyN1.substring(keyN1.lastIndexOf('.') + 1);
			}
		}
		return new KeyPair(keyN1, keyN2);
	}
	
	private String checkoutFolder(String tempFolder, String project, String commit, String prefixFolder) {
		return tempFolder + "checkout/" + repoName(project) + "-" + commit.substring(0, 7) + "/" + prefixFolder + "/";
	}
	
	private String repoName(String project) {
		return project.substring(project.lastIndexOf('/') + 1, project.lastIndexOf('.'));
	}
	
	public void prepareSourceCode2(String project, String commit) {
		System.out.print(String.format("Preparing %s %s ...", project, commit));
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
		System.out.println(" done.");
	}
	
	public void prepareSourceCodeNoCheckout(String project, String commit) {
		System.out.print(String.format("Preparing %s %s ...", project, commit));
		File fRepoFolder = repoFolder(project);
		if (!fRepoFolder.exists()) {
			fRepoFolder.mkdirs();
			ExternalProcess.execute(fRepoFolder, "git", "init", "--bare");
			ExternalProcess.execute(fRepoFolder, "git", "remote", "add", "origin", project);
		}
		String tagName = "refs/tags/r-" + commit.substring(0, 7);
		
		ExternalProcess.execute(fRepoFolder, "git", "fetch", "--depth", "2", "origin", tagName);
		System.out.println(" done.");
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
	
	public void prepareSourceCodeLightCheckout(String project, String commit) {
		System.out.println(String.format("Preparing %s %s", project, commit));
		String checkoutFolderV0 = checkoutFolder(tempFolder, project, commit, "v0");
		String checkoutFolderV1 = checkoutFolder(tempFolder, project, commit, "v1");
		File fRepoFolder = repoFolder(project);
		if (!fRepoFolder.exists()) {
			//ExternalProcess.execute(new File(tempFolder), "git", "clone", project, "--bare", "--shallow-since=2015-05-01");
			fRepoFolder.mkdirs();
			ExternalProcess.execute(fRepoFolder, "git", "init", "--bare");
			ExternalProcess.execute(fRepoFolder, "git", "remote", "add", "origin", project);
		}
		File fCheckoutFolderV0 = new File(checkoutFolderV0);
		File fCheckoutFolderV1 = new File(checkoutFolderV1);
		if (!fCheckoutFolderV0.exists() || !fCheckoutFolderV1.exists()) {
			// ExternalProcess.execute(fRepoFolder, "git", "fetch", "--depth", "2", "origin", commit);
			try (Repository repo = GitHelper.openRepository(fRepoFolder)) {
				PairBeforeAfter<SourceFileSet> sourcesPair = GitHelper.getSourcesBeforeAndAfterCommit(repo, commit, new FilePathFilter(Arrays.asList(".java")));
				if (!fCheckoutFolderV0.exists() && fCheckoutFolderV0.mkdirs()) {
					sourcesPair.getBefore().materializeAt(fCheckoutFolderV0.toPath());
				}
				if (!fCheckoutFolderV1.exists() && fCheckoutFolderV1.mkdirs()) {
					sourcesPair.getAfter().materializeAt(fCheckoutFolderV1.toPath());
				}
			} catch (Exception e) {
				throw new RuntimeException(String.format("Error checking out %s %s:\n%s", project, commit, e.getMessage()), e);
			}
		}
	}
	
	public PairBeforeAfter<Set<String>> getRepositoryDirectoriesBeforeAfter(String project, String commit) {
		File fRepoFolder = repoFolder(project);
		try (Repository repo = GitHelper.openRepository(fRepoFolder)) {
			try (RevWalk rw = new RevWalk(repo)) {
				RevCommit commitAfter = rw.parseCommit(repo.resolve(commit));
				if (commitAfter.getParentCount() != 1) {
					throw new RuntimeException("Commit should have one parent");
				}
				RevCommit commitBefore = rw.parseCommit(commitAfter.getParent(0));
				return new PairBeforeAfter<>(extractRepositoryDirectories(repo, commitBefore), extractRepositoryDirectories(repo, commitAfter));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private Set<String> extractRepositoryDirectories(Repository repository, RevCommit commit) throws Exception {
		Set<String> repositoryDirectories = new HashSet<>();
		RevTree parentTree = commit.getTree();
		try (TreeWalk treeWalk = new TreeWalk(repository)) {
			treeWalk.addTree(parentTree);
			treeWalk.setRecursive(true);
			while (treeWalk.next()) {
				String pathString = treeWalk.getPathString();
				if (pathString.endsWith(".java")) {
					String directory = pathString.substring(0, pathString.lastIndexOf("/"));
					repositoryDirectories.add(directory);
					// include sub-directories
					String subDirectory = new String(directory);
					while (subDirectory.contains("/")) {
						subDirectory = subDirectory.substring(0, subDirectory.lastIndexOf("/"));
						repositoryDirectories.add(subDirectory);
					}
				}
			}
		}
		return repositoryDirectories;
	}
	
	private File repoFolder(String project) {
		return new File(tempFolder + repoName(project) + ".git");
	}
	
	private SourceFolder getSourceFiles(String checkoutFolder, List<String> files) {
		return SourceFolder.from(Paths.get(checkoutFolder), files.stream().map(file -> Paths.get(file)).collect(Collectors.toList()));
	}
	
	private Optional<RefactoringType> getRefactoringType(RelationshipType relType, String nodeType) {
		boolean isType = nodeType.equals(NodeTypes.CLASS_DECLARATION) || nodeType.equals(NodeTypes.ENUM_DECLARATION) || nodeType.equals(NodeTypes.INTERFACE_DECLARATION);
		boolean isInterface = nodeType.equals(NodeTypes.INTERFACE_DECLARATION);
		boolean isMethod = nodeType.equals(NodeTypes.METHOD_DECLARATION);
		
		switch (relType) {
		case MOVE:
		case INTERNAL_MOVE:
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
		case MOVE_RENAME:
		case INTERNAL_MOVE_RENAME:
			if (isType) {
				return Optional.of(RefactoringType.RENAME_CLASS);
			} else if (isMethod) {
				return Optional.empty();
			}
			break;
		case EXTRACT:
		case EXTRACT_MOVE:
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
			if (isType) {
				return Optional.of(RefactoringType.MOVE_CLASS);
			} else if (isMethod) {
				return Optional.of(RefactoringType.PULL_UP_OPERATION);
			}
			break;
		case PUSH_DOWN:
		case PUSH_DOWN_IMPL:
			if (isType) {
				return Optional.of(RefactoringType.MOVE_CLASS);
			}
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
		default:
			//
		}
		return Optional.empty();
	}
	
}

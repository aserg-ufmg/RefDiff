package refdiff.evaluation.js;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.Repository;

import refdiff.core.diff.RastComparator;
import refdiff.core.diff.RastDiff;
import refdiff.core.diff.Relationship;
import refdiff.core.diff.RelationshipType;
import refdiff.core.io.GitHelper;
import refdiff.core.io.SourceFileSet;
import refdiff.core.rast.RastNode;
import refdiff.core.util.PairBeforeAfter;
import refdiff.evaluation.ExternalProcess;
import refdiff.parsers.js.JsParser;

public class ComputeRecallJs {
	
	private static final File tempFolder = new File("tmp");
	
	public static void main(String[] args) throws Exception {
		tempFolder.mkdirs();
		
		computeRecall(
			commit("https://github.com/webpack/webpack/commit/b50d4cf7c370dc0f9fa2c39ea0e73e28ca8918ac", RelationshipType.MOVE, node("lib/WebpackOptionsValidationError.js", "getSchemaPartText"), node("lib/util/getSchemaPartText.js", "getSchemaPartText")),
			commit("https://github.com/atom/atom/commit/fc620b9e80d67ca99f962431461b8fc4d085d9df", RelationshipType.MOVE, node("spec/tooltip-manager-spec.js", "createElement"), node("spec/tooltip-manager-spec.js", "createElement"))
		);
	}
	
	private static void computeRecall(boolean ...results) {
		int tp = 0;
		int fn = 0;
		for (boolean result : results) {
			if (result) {
				tp++;
			} else {
				fn++;
			} 
		}
		double recall = ((double) tp) / (tp + fn);
		System.out.println(String.format("TP: %d, FN: %d, Recall: %.3f", tp, fn, recall));
	}
	
	private static boolean commit(String commit, RelationshipType relType, String n1, String n2) throws Exception {
		RastDiff diff = diff(commit);
		Set<String> refactorings = diff.getRelationships().stream()
			.filter(r -> r.getType() != RelationshipType.SAME)
			.map(r -> format(r))
			.collect(Collectors.toSet());
		
		String refactoring = format(relType, n1, n2);
		if (refactorings.contains(refactoring)) {
			return true;
		} else {
			System.out.println("Found: ");
			for (String ref : refactorings) {
				System.out.println("  " + ref);
			}
			System.out.println("Not found: ");
			System.out.println("  " + refactoring);
			return false;
		}
	}

	private static String format(Relationship r) {
		return format(r.getType(), format(r.getNodeBefore()), format(r.getNodeAfter()));
	}
	
	private static String format(RelationshipType relType, String n1, String n2) {
		return String.format("%s\t%s\t%s", relType, n1, n2);
	}
	
	private static String format(RastNode node) {
		return node.getLocation().getFile() + ":" + node.getLocalName();
	}
	
	private static String node(String file, String localName) {
		return file + ":" + localName;
	}

	private static RastDiff diff(String commitUrl) throws Exception, IOException {
		String[] url = commitUrl.split("/commit/");
		String commit = url[1];
		String project = url[0].substring(url[0].lastIndexOf("/") + 1);
		File repoFolder = new File(tempFolder, project + ".git");
		String cloneUrl = "https://github.com/refdiff-data/" + project + ".git";
		
		if (!repoFolder.exists()) {
			ExternalProcess.execute(tempFolder, "git", "clone", cloneUrl, repoFolder.getPath(), "--bare", "--depth=1000");
		}
		//ExternalProcess.execute(repoFolder, "git", "fetch", "--depth=5000");
		
		GitHelper gh = new GitHelper();
		try (JsParser parser = new JsParser();
			Repository repo = gh.openRepository(repoFolder)) {
			RastComparator rastComparator = new RastComparator(parser);
			
			PairBeforeAfter<SourceFileSet> sources = gh.getSourcesBeforeAndAfterCommit(repo, commit, parser.getAllowedFilesFilter());
			return rastComparator.compare(sources.getBefore(), sources.getAfter());
		}
	}
	
}

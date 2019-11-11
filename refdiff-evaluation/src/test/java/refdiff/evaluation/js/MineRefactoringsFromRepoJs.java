package refdiff.evaluation.js;

import java.io.File;
import java.io.PrintStream;
import java.util.Random;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import refdiff.core.diff.CstComparator;
import refdiff.core.diff.CstDiff;
import refdiff.core.diff.Relationship;
import refdiff.core.diff.RelationshipType;
import refdiff.core.io.GitHelper;
import refdiff.core.io.SourceFileSet;
import refdiff.core.cst.CstNode;
import refdiff.core.util.PairBeforeAfter;
import refdiff.evaluation.ExternalProcess;
import refdiff.parsers.js.JsPlugin;

public class MineRefactoringsFromRepoJs {
	
	private static final int MAX_COMMITS = 500;

	private static Random random = new Random(44L);
	
	public static void main(String[] args) throws Exception {
		
		File tempFolder = new File("tmp");
		tempFolder.mkdirs();
		
		try (PrintStream csv = new PrintStream(new File(tempFolder, "data.csv"))) {			
			mineRepository(tempFolder, csv, "https://github.com/facebook/react.git");
			mineRepository(tempFolder, csv, "https://github.com/vuejs/vue.git");
			mineRepository(tempFolder, csv, "https://github.com/d3/d3.git");
			mineRepository(tempFolder, csv, "https://github.com/facebook/react-native.git");
			mineRepository(tempFolder, csv, "https://github.com/angular/angular.js.git");
			mineRepository(tempFolder, csv, "https://github.com/facebook/create-react-app.git");
			mineRepository(tempFolder, csv, "https://github.com/jquery/jquery.git");
			mineRepository(tempFolder, csv, "https://github.com/atom/atom.git");
			mineRepository(tempFolder, csv, "https://github.com/axios/axios.git");
			mineRepository(tempFolder, csv, "https://github.com/mrdoob/three.js.git");
			mineRepository(tempFolder, csv, "https://github.com/socketio/socket.io.git");
			mineRepository(tempFolder, csv, "https://github.com/reduxjs/redux.git");
			mineRepository(tempFolder, csv, "https://github.com/webpack/webpack.git");
			mineRepository(tempFolder, csv, "https://github.com/Semantic-Org/Semantic-UI.git");
			mineRepository(tempFolder, csv, "https://github.com/hakimel/reveal.js.git");
			mineRepository(tempFolder, csv, "https://github.com/meteor/meteor.git");
			mineRepository(tempFolder, csv, "https://github.com/expressjs/express.git");
			mineRepository(tempFolder, csv, "https://github.com/mui-org/material-ui.git");
			mineRepository(tempFolder, csv, "https://github.com/chartjs/Chart.js.git");
		}
		
	}

	private static void mineRepository(File tempFolder, PrintStream csv, String cloneUrl) throws Exception {
		String projectName = cloneUrl.substring(cloneUrl.lastIndexOf('/') + 1);
		File repoFolder = new File(tempFolder, projectName);
		
		
		if (!repoFolder.exists()) {
			System.out.println("Cloning " + cloneUrl);
			ExternalProcess.execute(tempFolder, "git", "clone", "https://github.com/refdiff-study/" + projectName, projectName, "--bare", "--depth=1000");
		}
		
		GitHelper gh = new GitHelper();
		
		System.out.println("Mining " + cloneUrl);
		try (
			JsPlugin parser = new JsPlugin();
			Repository repository = gh.openRepository(repoFolder)) {
			
			CstComparator cstComparator = new CstComparator(parser);
			
			gh.forEachNonMergeCommit(repository, MAX_COMMITS, (RevCommit commitBefore, RevCommit commitAfter) -> {
				String commitSha1 = commitAfter.getId().getName();
				System.out.println(commitSha1);
				
				try {
					PairBeforeAfter<SourceFileSet> sources = gh.getSourcesBeforeAndAfterCommit(repository, commitBefore, commitAfter, parser.getAllowedFilesFilter());
					CstDiff diff = cstComparator.compare(sources);
					
					for (Relationship relationship : diff.getRelationships()) {
						if (relationship.getType() != RelationshipType.SAME) {
							CstNode n1 = relationship.getNodeBefore();
							CstNode n2 = relationship.getNodeAfter();
							csv.printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%d\n", projectName, commitSha1, relationship.getType(), n1.getType(), n1.getLocation().format(), n1.getLocalName(), n2.getLocation().format(), n2.getLocalName(), random.nextInt());
						}
					}
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
				
			});
		}
	}
	
}

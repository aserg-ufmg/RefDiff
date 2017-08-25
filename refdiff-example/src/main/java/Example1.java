import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

import refdiff.core.RefDiff;
import refdiff.core.api.GitService;
import refdiff.core.rm2.model.refactoring.SDRefactoring;
import refdiff.core.util.GitServiceImpl;

public class Example1 {
	
	public static void main(String[] args) throws Exception {
		
		String[][] todo = new String[][] {
			{ "eclipse/che", "ca7612bfbce4cd15a2badb78d17a878253a30f11", "false" },
			{ "elastic/elasticsearch", "dea7989a0fda5c4e475788d250651bc8f72009f0", "false" },
			{ "eclipse/che", "d9f9e688c639c21687fc50a339c178b65c6e08f5", "false" },
			{ "spring-projects/spring-framework", "97917aa57d898e3e085beb0d17d26728813bb10c", "false" },
			{ "elastic/elasticsearch", "abcb4c8a97405a7ed5a019d4d9596f4d9e4ab055", "false" },
			{ "elastic/elasticsearch", "d11521318d37d54d13c8499a2b47bc5414310d4b", "false" },
			{ "elastic/elasticsearch", "549ca3178bb6151b080a97eac6093bf8af459a99", "false" },
			{ "elastic/elasticsearch", "867f056cf6370bfdc39790eac758ea0035644631", "false" },
			{ "square/okhttp", "7c94c808de0e946180fbcfa187aacd8fca83c489", "false" },
			{ "elastic/elasticsearch", "ddced5df1a924a90a3d8d549479941087859e111", "false" },
			{ "square/okhttp", "957537774b319bb0109819258a11af78a98bcb97", "false" }
		};
		
		RefDiff refDiff = new RefDiff();
		GitService gitService = new GitServiceImpl();
		
		File folder = new File("D:/tmp");
		try (PrintStream pw = new PrintStream(new File(folder.getParentFile(), "actual"))) {
			for (String[] i : todo) {
				String project = i[0];
				String sha1 = i[1];
				String projectName = project.substring(project.indexOf('/') + 1);
				File f = new File(folder, projectName);
				try (Repository repo = gitService.cloneIfNotExists(f.getPath(), "https://github.com/" + project + ".git")) {
					try {
						List<SDRefactoring> refactorings = refDiff.detectAtCommit(repo, sha1);
						System.out.println(String.format("Finished %s %s", project, sha1));
						for (SDRefactoring r : refactorings) {
							pw.printf("%s\t%s\t%s\t%s\t%s\n", projectName, sha1, r.getRefactoringType().getDisplayName(), r.getEntityBefore(), r.getEntityAfter());
						}
					} catch (Exception e) {
						System.out.println(String.format("Error %s %s: %s", project, sha1, e.getMessage()));
					}
				}
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Repository open(File folder) throws IOException {
		RepositoryBuilder builder = new RepositoryBuilder();
		return builder
			.setGitDir(new File(folder, ".git"))
			.readEnvironment()
			.findGitDir()
			.build();
	}
	
}

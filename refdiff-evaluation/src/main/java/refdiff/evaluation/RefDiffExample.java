package refdiff.evaluation;

import java.util.List;

import org.eclipse.jgit.lib.Repository;

import refdiff.core.RefDiff;
import refdiff.core.api.GitService;
import refdiff.core.rm2.model.refactoring.SDRefactoring;
import refdiff.core.util.GitServiceImpl;

public class RefDiffExample {

    public static void main(String[] args) throws Exception {
        RefDiff refDiff = new RefDiff();
        GitService gitService = new GitServiceImpl(); 
        try (Repository repository = gitService.openRepository("C:/tmp/clojure")) {
            List<SDRefactoring> refactorings = refDiff.detectAtCommit(repository, "17217a1");
            for (SDRefactoring refactoring : refactorings) {
                System.out.println(refactoring.toString());
            }
        }
    }

}

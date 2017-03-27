package refdiff.evaluation;

import java.util.Date;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import refdiff.core.api.GitService;
import refdiff.core.util.GitServiceImpl;
import refdiff.evaluation.db.model.DbCommitDao;
import refdiff.evaluation.db.model.DbCommitResultDao;
import refdiff.evaluation.db.model.DbRepositoryDao;

@SpringBootApplication
public class TestPerformance {

    public static void main(String[] args) {
        SpringApplication.run(TestPerformance.class);
    }

    @Component
    public static class TestPerformanceRunner implements CommandLineRunner {

        DbRepositoryDao repositoryDao;
        DbCommitDao commitDao;
        DbCommitResultDao commitResultDao;

        @Autowired
        public TestPerformanceRunner(DbRepositoryDao repositoryDao, DbCommitDao commitDao, DbCommitResultDao commitResultDao) {
            this.repositoryDao = repositoryDao;
            this.commitDao = commitDao;
            this.commitResultDao = commitResultDao;
        }

        @Override
        public void run(String... args) throws Exception {
            process("ReactiveX/RxJava", "2.x", "D:/Danilo/Workspaces/refdiff-eval-2/RxJava");
        }

        private void process(String fullName, String branch, String folder) {
            GitService gitService = new GitServiceImpl();
            try (
                Repository repository = gitService.cloneIfNotExists(folder, "https://github.com/" + fullName + ".git");
                RevWalk walk = gitService.createAllRevsWalk(repository, branch);) {

                int count = 0;
                for (RevCommit commit : walk) {
                    System.out.print(commit.getId().getName());
                    System.out.print("\t");
                    Date commitTime = new Date(((long) commit.getCommitTime()) * 1000L);
                    System.out.println(commitTime);
                    count++;
                }
                System.out.println("Commits: " + count);
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }
}

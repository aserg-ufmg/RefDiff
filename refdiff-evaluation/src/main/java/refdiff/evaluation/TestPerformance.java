package refdiff.evaluation;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import refdiff.core.api.GitHistoryRefactoringMiner;
import refdiff.core.api.GitService;
import refdiff.core.rm2.analysis.GitHistoryRefactoringMiner2;
import refdiff.core.util.GitServiceImpl;
import refdiff.evaluation.db.model.DbCommit;
import refdiff.evaluation.db.model.DbCommitDao;
import refdiff.evaluation.db.model.DbCommitResult;
import refdiff.evaluation.db.model.DbCommitResultDao;
import refdiff.evaluation.db.model.DbRefactoringRelationship;
import refdiff.evaluation.db.model.DbRepository;
import refdiff.evaluation.db.model.DbRepositoryDao;
import refdiff.evaluation.rm.RmAdapter;
import refdiff.evaluation.utils.RefactoringCollector;
import refdiff.evaluation.utils.RefactoringSet;

@SpringBootApplication
public class TestPerformance {

    public static void main(String[] args) {
        SpringApplication.run(TestPerformance.class);
    }

    @Component
    public static class TestPerformanceRunner implements CommandLineRunner {

        private final DbRepositoryDao repositoryDao;
        private final DbCommitDao commitDao;
        private final DbCommitResultDao commitResultDao;

        @Autowired
        public TestPerformanceRunner(DbRepositoryDao repositoryDao, DbCommitDao commitDao, DbCommitResultDao commitResultDao) {
            this.repositoryDao = repositoryDao;
            this.commitDao = commitDao;
            this.commitResultDao = commitResultDao;
        }

        @Override
        public void run(String... args) throws Exception {
            GitHistoryRefactoringMiner refdiff = new GitHistoryRefactoringMiner2();
            GitHistoryRefactoringMiner rm = new RmAdapter(new GitHistoryRefactoringMinerImpl());

            mine("ReactiveX/RxJava", "2.x", "D:/Danilo/Workspaces/refdiff-eval-2/RxJava", refdiff);
            mine("ReactiveX/RxJava", "2.x", "D:/Danilo/Workspaces/refdiff-eval-2/RxJava", rm);
        }

        private void mine(String fullName, String branch, String folder, GitHistoryRefactoringMiner algo) {
            process(fullName, branch, folder, (repository, commit, dbCommit) -> {
                if (commitResultDao.findOneByCommitAndTool(dbCommit, algo.getConfigId()) == null) {
                    String sha1 = commit.getId().getName();
                    RefactoringCollector rc = new RefactoringCollector(fullName, sha1);
                    long t0 = System.currentTimeMillis();
                    algo.detectAtCommit(repository, commit.getId().getName(), rc);
                    long tf = System.currentTimeMillis();
                    final DbCommitResult result = new DbCommitResult(dbCommit, algo.getConfigId());
                    result.setExecTime(tf - t0);
                    try {
                        RefactoringSet set = rc.assertAndGetResult();
                        result.setSuccess(true);
                        result.setRefactorings(set.getRefactorings().stream()
                            .map(r -> new DbRefactoringRelationship(result, r.getRefactoringType().getDisplayName(), r.getEntityBefore(), r.getEntityAfter()))
                            .collect(Collectors.toSet()));
                    } catch (Exception e) {
                        result.setSuccess(false);
                        result.setErrorLog(e.getMessage());
                    } finally {
                        commitResultDao.save(result);
                        System.out.println(String.format("%s %s %s", algo.getConfigId(), fullName, sha1));
                    }
                }
            });
        }
        
        private void process(String fullName, String branch, String folder, ProcessCommitFn processCommitFn) {
            GitService gitService = new GitServiceImpl();
            try (
                Repository repository = gitService.cloneIfNotExists(folder, "https://github.com/" + fullName + ".git");
                RevWalk walk = gitService.createAllRevsWalk(repository, branch);) {

                DbRepository dbRepository = repositoryDao.findOneByFullName(fullName);
                if (dbRepository == null) {
                    dbRepository = repositoryDao.save(new DbRepository(fullName));
                }
                for (RevCommit commit : walk) {
                    LocalDateTime ldt = LocalDateTime.ofEpochSecond(commit.getCommitTime(), 0, ZoneOffset.UTC);
                    int parentsCount = commit.getParents().length;
                    if (ldt.isAfter(LocalDateTime.of(2017, 1, 1, 0, 0)) && parentsCount == 1) {
                        String sha1 = commit.getId().getName();
                        DbCommit dbCommit = commitDao.findOneByRepositoryAndSha1(dbRepository, sha1);
                        if (dbCommit == null) {
                            dbCommit = commitDao.save(new DbCommit(dbRepository, sha1));
                        }
                        processCommitFn.processCommit(repository, commit, dbCommit);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }

    @FunctionalInterface
    interface ProcessCommitFn {
        void processCommit(Repository repository, RevCommit commit, DbCommit dbCommit);
    }
    
}


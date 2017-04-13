package refdiff.evaluation;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import refdiff.core.RefDiff;
import refdiff.core.api.GitRefactoringDetector;
import refdiff.core.api.GitService;
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
import refdiff.evaluation.utils.RefactoringRelationship;
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
            mine("ReactiveX/RxJava", "2.x", "D:/Danilo/Workspaces/refdiff-eval-2/RxJava");
            mine("elastic/elasticsearch", "master", "D:/Danilo/Workspaces/refdiff-eval-2/elasticsearch");
            mine("square/okhttp", "master", "D:/Danilo/Workspaces/refdiff-eval-2/okhttp");
            //mine("nostra13/Android-Universal-Image-Loader", "master", "D:/Danilo/Workspaces/refdiff-eval-2/Android-Universal-Image-Loader");
            mine("androidannotations/androidannotations", "develop", "D:/Danilo/Workspaces/refdiff-eval-2/androidannotations");
            mine("PhilJay/MPAndroidChart", "master", "D:/Danilo/Workspaces/refdiff-eval-2/MPAndroidChart");
            mine("bumptech/glide", "master", "D:/Danilo/Workspaces/refdiff-eval-2/glide");
            mine("zxing/zxing", "master", "D:/Danilo/Workspaces/refdiff-eval-2/zxing");
            mine("spring-projects/spring-framework", "master", "D:/Danilo/Workspaces/refdiff-eval-2/spring-framework");
            mine("libgdx/libgdx", "master", "D:/Danilo/Workspaces/refdiff-eval-2/libgdx");
            mine("netty/netty", "4.1", "D:/Danilo/Workspaces/refdiff-eval-2/netty");
        }

        private void mine(String fullName, String branch, String folder) {
            GitRefactoringDetector refdiff = new RefDiff();
            GitRefactoringDetector rm = new RmAdapter(new GitHistoryRefactoringMinerImpl());
            mine(fullName, branch, folder, rm);
            mine(fullName, branch, folder, refdiff);
        }

        private void mine(String fullName, String branch, String folder, GitRefactoringDetector algo) {
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
                        Set<RefactoringRelationship> refactoringsFound = set.getRefactorings();
                        result.setRefactorings(refactoringsFound.stream()
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
                        if (dbCommit.getAffectedFiles() == null) {
                            List<String> javaFiles = new ArrayList<>();
                            Map<String, String> renamed = new HashMap<>();
                            gitService.fileTreeDiff(repository, commit, javaFiles, javaFiles, renamed, false);
                            Set<String> distinctFiles = new HashSet<>(javaFiles);
                            dbCommit.setAffectedFiles(distinctFiles.size());
                            commitDao.save(dbCommit);
                        }
                        processCommitFn.processCommit(repository, commit, dbCommit);
                    }
                }
                gitService.checkout(repository, branch);
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


package refdiff.evaluation.db.model;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface DbCommitDao extends PagingAndSortingRepository<DbCommit, Integer> {

    DbCommit findOneByRepositoryAndSha1(DbRepository repository, String sha1);

}

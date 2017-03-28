package refdiff.evaluation.db.model;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface DbRepositoryDao extends PagingAndSortingRepository<DbRepository, Integer> {

    DbRepository findOneByFullName(String fullName);

}

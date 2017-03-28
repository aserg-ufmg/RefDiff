package refdiff.evaluation.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "repository")
public class DbRepository {

    @Id
    @SequenceGenerator(name = "seq_repository", sequenceName = "seq_repository", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_repository")
    private Integer id;

    @Column(unique = true, length = 200)
    private String fullName;

    public DbRepository(String fullName) {
        this.fullName = fullName;
    }

    public DbRepository() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

}

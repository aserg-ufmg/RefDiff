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
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq_repository")
    @SequenceGenerator(name="seq_repository", initialValue=1)
    private Integer id;

    @Column(unique = true)
    private String fullName;

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

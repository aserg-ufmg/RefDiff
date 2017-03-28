package refdiff.evaluation.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "refactoring_relationship")
public class DbRefactoringRelationship {

    @Id
    @SequenceGenerator(name = "seq_refactoring_relationship", sequenceName = "seq_refactoring_relationship", initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_refactoring_relationship")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "commitResult")
    private DbCommitResult commitResult;

    @Column(length = 200)
    private String refactoringType;

    @Column(length = 1000)
    private String entityBefore;

    @Column(length = 1000)
    private String entityAfter;

    public DbRefactoringRelationship(DbCommitResult commitResult, String refactoringType, String entityBefore, String entityAfter) {
        this.commitResult = commitResult;
        this.refactoringType = refactoringType;
        this.entityBefore = entityBefore;
        this.entityAfter = entityAfter;
    }

    public DbRefactoringRelationship() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DbCommitResult getCommitResult() {
        return commitResult;
    }

    public void setCommitResult(DbCommitResult commitResult) {
        this.commitResult = commitResult;
    }

    public String getRefactoringType() {
        return refactoringType;
    }

    public void setRefactoringType(String refactoringType) {
        this.refactoringType = refactoringType;
    }

    public String getEntityBefore() {
        return entityBefore;
    }

    public void setEntityBefore(String entityBefore) {
        this.entityBefore = entityBefore;
    }

    public String getEntityAfter() {
        return entityAfter;
    }

    public void setEntityAfter(String entityAfter) {
        this.entityAfter = entityAfter;
    }

}

package refdiff.evaluation.db.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "commit_result", uniqueConstraints = { @UniqueConstraint(columnNames = { "commit", "tool" }) })
public class DbCommitResult {

    @Id
    @SequenceGenerator(name = "seq_commit_result", sequenceName = "seq_commit_result", initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_commit_result")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "commit")
    private DbCommit commit;

    @Column(length = 200)
    private String tool;

    private Long execTime;

    private boolean success = true;

    @Column(length = 2000)
    private String errorLog;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "commitResult")
    private Set<DbRefactoringRelationship> refactorings;

    public DbCommitResult(DbCommit commit, String tool) {
        this.commit = commit;
        this.tool = tool;
    }

    public DbCommitResult() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DbCommit getCommit() {
        return commit;
    }

    public void setCommit(DbCommit commit) {
        this.commit = commit;
    }

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public Long getExecTime() {
        return execTime;
    }

    public void setExecTime(Long execTime) {
        this.execTime = execTime;
    }

    public Set<DbRefactoringRelationship> getRefactorings() {
        return refactorings;
    }

    public void setRefactorings(Set<DbRefactoringRelationship> refactorings) {
        this.refactorings = refactorings;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorLog() {
        return errorLog;
    }

    public void setErrorLog(String errorLog) {
        this.errorLog = errorLog;
    }

}

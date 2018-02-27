package refdiff.evaluation.icse;

import java.util.List;

public class IcseCommit {
	public int id;
	public String repository;
    public String sha1;
    public String url;
    public String author;
    public String time;
    public List<IcseRefactoring> refactorings;
}

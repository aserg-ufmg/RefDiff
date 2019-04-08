package refdiff.evaluation.icse;

import java.util.List;

public class IcseCommit {
	public int id;
	public String repository;
	public String mirrorRepository;
    public String sha1;
    public String url;
    public String author;
    public String time;
    public String comment;
    public boolean ignore;
    public List<IcseRefactoring> refactorings;
}

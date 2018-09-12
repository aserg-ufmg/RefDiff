package refdiff.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class RefactoringSet {
	
	private final String project;
	private final String revision;
	private final Set<RefactoringRelationship> refactorings;
	
	public RefactoringSet(String project, String revision) {
		super();
		this.project = project;
		this.revision = revision;
		this.refactorings = new HashSet<>();
	}
	
	public String getProject() {
		return project;
	}
	
	public String getRevision() {
		return revision;
	}
	
	public Set<RefactoringRelationship> getRefactorings() {
		return refactorings;
	}
	
	public RefactoringSet add(RefactoringRelationship r) {
		this.refactorings.add(r);
		return this;
	}
	
	public RefactoringSet remove(RefactoringRelationship r) {
		this.refactorings.remove(r);
		return this;
	}
	
	public RefactoringSet add(Iterable<RefactoringRelationship> rs) {
		for (RefactoringRelationship r : rs) {
			this.add(r);
		}
		return this;
	}
	
	public RefactoringSet ignoring(EnumSet<RefactoringType> refTypes) {
		RefactoringSet newSet = new RefactoringSet(project, revision);
		newSet.add(refactorings.stream()
			.filter(r -> !refTypes.contains(r.getRefactoringType()))
			.collect(Collectors.toList()));
		return newSet;
	}
	
	public void printSourceCode(PrintStream pw) {
		pw.printf("new RefactoringSet(\"%s\", \"%s\")", project, revision);
		for (RefactoringRelationship r : refactorings) {
			pw.printf("\n    .add(RefactoringType.%s, \"%s\", \"%s\")", r.getRefactoringType().toString(), r.getEntityBefore(), r.getEntityAfter());
		}
		pw.println(";");
	}
	
	public void printCsv(PrintStream pw) {
		ArrayList<RefactoringRelationship> list = new ArrayList<>();
		list.addAll(refactorings);
		Collections.sort(list);
		for (RefactoringRelationship r : list) {
			pw.printf("%s\t%s\t%s\n", r.getRefactoringType().getDisplayName(), r.getEntityBefore(), r.getEntityAfter());
		}
	}
	
	public void saveToFile(File file) {
		try (PrintStream pw = new PrintStream(file)) {
			for (RefactoringRelationship r : refactorings) {
				pw.printf("%s\t%s\t%s\n", r.getRefactoringType().getDisplayName(), r.getEntityBefore(), r.getEntityAfter());
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void readFromFile(File file) {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (!line.isEmpty()) {
					String[] array = line.split("\t");
					RefactoringType refactoringType = RefactoringType.fromName(array[0].trim());
					String entityBefore = array[1].trim();
					String entityAfter = array[2].trim();
					add(new RefactoringRelationship(refactoringType, entityBefore, entityAfter));
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}

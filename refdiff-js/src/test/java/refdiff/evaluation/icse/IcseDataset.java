package refdiff.evaluation.icse;

import java.io.FileReader;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import refdiff.evaluation.AbstractDataset;
import refdiff.evaluation.RefactoringDescriptionParser;
import refdiff.evaluation.RefactoringSet;

public class IcseDataset extends AbstractDataset {
	
	public IcseDataset() {
		RefactoringDescriptionParser parser = new RefactoringDescriptionParser();
		
		ObjectMapper om = new ObjectMapper();
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		ObjectReader reader = om.readerFor(IcseCommit[].class);
		try {
			IcseCommit[] commits = reader.readValue(new FileReader("data/icse/data.json"));
			for (IcseCommit commit : commits) {
				String repoUrl = commit.mirrorRepository;
				if (repoUrl == null) {
					repoUrl = "https://github.com/icse18-refactorings/" + commit.repository.substring(commit.repository.lastIndexOf('/') + 1);
				}
				
				RefactoringSet rs = new RefactoringSet(repoUrl, commit.sha1);
				RefactoringSet rsNotExpected = new RefactoringSet(repoUrl, commit.sha1);
				for (IcseRefactoring refactoring : commit.refactorings) {
					if (refactoring.validation.equals("TP") || refactoring.validation.equals("CTP")) {
						rs.add(parser.parse(refactoring.description));
					} else if (refactoring.validation.equals("FP")) {
						rsNotExpected.add(parser.parse(refactoring.description));
					}
				}
				add(rs, rsNotExpected);
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}

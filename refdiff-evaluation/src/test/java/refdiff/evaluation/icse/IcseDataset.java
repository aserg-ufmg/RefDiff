package refdiff.evaluation.icse;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import refdiff.evaluation.AbstractDataset;
import refdiff.evaluation.RefactoringDescriptionParser;
import refdiff.evaluation.RefactoringRelationship;
import refdiff.evaluation.RefactoringSet;

public class IcseDataset extends AbstractDataset {
	
	protected final List<RefactoringSet> rMinerRefactorings = new ArrayList<>();
	
	public IcseDataset() {
		RefactoringDescriptionParser parser = new RefactoringDescriptionParser();
		
		ObjectMapper om = new ObjectMapper();
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		ObjectReader reader = om.readerFor(IcseCommit[].class);
		try {
			IcseCommit[] commits = reader.readValue(new FileReader("data/icse/data.json"));
			int ignoredCount = 0;
			int addedCount = 0;
			int refCount = 0;
			for (IcseCommit commit : commits) {
				if (commit.ignore) {
					ignoredCount += commit.refactorings.size();
					continue;
				}
				String repoUrl = commit.mirrorRepository;
				if (repoUrl == null) {
					repoUrl = "https://github.com/icse18-refactorings/" + commit.repository.substring(commit.repository.lastIndexOf('/') + 1);
				}
				
				RefactoringSet rs = new RefactoringSet(repoUrl, commit.sha1);
				RefactoringSet rsRMiner = new RefactoringSet(repoUrl, commit.sha1);
				RefactoringSet rsNotExpected = new RefactoringSet(repoUrl, commit.sha1);
				for (IcseRefactoring refactoring : commit.refactorings) {
					if (refactoring.type.equals("Change Package")) {
						ignoredCount++;
						continue;
					} else {
						addedCount++;
					}
					List<RefactoringRelationship> refs = parser.parse(refactoring.description);
					refCount += refs.size();

					if (refactoring.validation.equals("TP") || refactoring.validation.equals("CTP")) {
						rs.add(refs);
					} else if (refactoring.validation.equals("FP")) {
						rsNotExpected.add(refs);
					}
					if (refactoring.detectionTools.contains("RefactoringMiner")) {
						rsRMiner.add(refs);
					}
				}
				add(rs, rsNotExpected);
				rMinerRefactorings.add(rsRMiner);
			}
			//System.out.println("Ignored: " + ignoredCount);
			//System.out.println("Added: " + addedCount);
			//System.out.println("Added refs: " + refCount);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<RefactoringSet> getrMinerRefactorings() {
		return rMinerRefactorings;
	}
}

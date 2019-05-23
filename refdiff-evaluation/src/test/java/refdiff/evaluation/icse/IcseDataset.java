package refdiff.evaluation.icse;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import refdiff.evaluation.AbstractDataset;
import refdiff.evaluation.RefactoringDescriptionParser;
import refdiff.evaluation.RefactoringRelationship;
import refdiff.evaluation.RefactoringSet;
import refdiff.evaluation.RefactoringType;

public class IcseDataset extends AbstractDataset {
	
	protected final List<RefactoringSet> rMinerRefactorings = new ArrayList<>();
	protected final List<RefactoringSet> refDiffRefactorings = new ArrayList<>();
	
	protected final List<String> ignore = Arrays.asList("Change Package", "Move Attribute", "Pull Up Attribute", "Push Down Attribute");
	
	public static EnumSet<RefactoringType> refactoringTypes = EnumSet.complementOf(EnumSet.of(RefactoringType.PULL_UP_ATTRIBUTE, RefactoringType.PUSH_DOWN_ATTRIBUTE, RefactoringType.MOVE_ATTRIBUTE));
	
	public IcseDataset() {
		RefactoringDescriptionParser parser = new RefactoringDescriptionParser();
		
		ObjectMapper om = new ObjectMapper();
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		ObjectReader reader = om.readerFor(IcseCommit[].class);
		try {
			IcseCommit[] commits = reader.readValue(new FileReader("data/icse/data.json"));
			int ignoredCount = 0;
			int addedCount = 0;
			int tpCount = 0;
			int tpCount2 = 0;
			int fpCount = 0;
			int fpCount2 = 0;
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
				RefactoringSet rsRefDiff = new RefactoringSet(repoUrl, commit.sha1);
				RefactoringSet rsNotExpected = new RefactoringSet(repoUrl, commit.sha1);
				for (IcseRefactoring refactoring : commit.refactorings) {
					if (ignore.contains(refactoring.type)) {
						ignoredCount++;
						continue;
					} else {
						addedCount++;
					}
					List<RefactoringRelationship> refs = parser.parse(refactoring.description).stream()
						.filter(r -> refactoringTypes.contains(r.getRefactoringType()))
						.collect(Collectors.toList());
					
					refCount += refs.size();

					if (refactoring.comment != null) {
						refs.forEach(r -> r.setComment(refactoring.comment));
					}
					
					if (refactoring.validation.equals("TP") || refactoring.validation.equals("CTP")) {
						rs.add(refs);
						tpCount += refs.size();
					} else if (refactoring.validation.equals("FP")) {
						rsNotExpected.add(refs);
						fpCount += refs.size();
					}
					if (refactoring.detectionTools.contains("RefactoringMiner")) {
						rsRMiner.add(refs);
					}
					if (refactoring.detectionTools.contains("RefDiff")) {
						rsRefDiff.add(refs);
					}
				}
				tpCount2 += rs.getRefactorings().size();
				fpCount2 += rsNotExpected.getRefactorings().size();
				add(rs, rsNotExpected);
				rMinerRefactorings.add(rsRMiner);
				refDiffRefactorings.add(rsRefDiff);
			}
//			System.out.println("Ignored: " + ignoredCount);
//			System.out.println("Added: " + addedCount);
//			System.out.println("Added refs: " + refCount);
//			System.out.println("TP: " + tpCount);
//			System.out.println("FP: " + fpCount);
//			System.out.println("TP2: " + tpCount2);
//			System.out.println("FP2: " + fpCount2);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<RefactoringSet> getrMinerRefactorings() {
		return rMinerRefactorings;
	}

	public List<RefactoringSet> getRefDiffRefactorings() {
		return refDiffRefactorings;
	}
	
}

package refdiff.evaluation.icse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import refdiff.evaluation.RefactoringRelationship;
import refdiff.evaluation.RefactoringSet;
import refdiff.evaluation.RefactoringType;
import refdiff.evaluation.ResultComparator;

public class FixExtract {
	
	public static void main(String[] args) throws IOException {
		IcseDataset data = new IcseDataset();
		List<RefactoringSet> expected = data.getExpected();
		
		Set<String> extractAndMove = new HashSet<>();
		for (RefactoringSet rs : expected) {
			for (RefactoringRelationship ref : rs.getRefactorings()) {
				if (ref.getRefactoringType().equals(RefactoringType.EXTRACT_AND_MOVE_OPERATION)) {
					String fRef = ResultComparator.format(ref).replace("Extract And Move Method\t", "Extract Method\t");
					extractAndMove.add(fRef.trim());
				}
			}
		}
		
		try (BufferedWriter bw = new BufferedWriter(new FileWriter("data/java-evaluation/FixExtractAfter.txt"));
			BufferedReader br = new BufferedReader(new FileReader("data/java-evaluation/FixExtractBefore.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				String oldRef = line.trim();
				if (extractAndMove.contains(oldRef)) {
					extractAndMove.remove(oldRef);
					bw.write(line.replace("Extract Method\t", "Extract And Move Method\t"));
				} else {
					bw.write(line);
				}
				bw.write('\n');
			}
		}
		
		for (String notFound : extractAndMove) {
			System.err.println(notFound);
		}
	}
	
}

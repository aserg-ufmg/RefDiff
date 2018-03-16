package refdiff.evaluation.icse;

import java.io.FileReader;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class IcseCreateTagGenerator {
	
	public static void main(String[] args) throws Exception {
		ObjectMapper om = new ObjectMapper();
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		ObjectReader reader = om.readerFor(IcseCommit[].class);
		IcseCommit[] commits = reader.readValue(new FileReader("data/icse/data.json"));
		for (IcseCommit commit : commits) {
			String sha1 = commit.sha1;
			String shortSha1 = sha1.substring(0, 7);
			String repoName = commit.repository.substring(commit.repository.lastIndexOf('/') + 1, commit.repository.lastIndexOf('.'));
			String mirrorRepoName = repoName;
			if (commit.mirrorRepository != null) {
				mirrorRepoName = commit.mirrorRepository.substring(commit.mirrorRepository.lastIndexOf('/') + 1, commit.mirrorRepository.lastIndexOf('.'));
			}
			System.out.println(String.format("curl -u \"danilofes:pwd\" -X POST -d '{\"ref\": \"refs/tags/r-%s\", \"sha\": \"%s\"}' https://api.github.com/repos/icse18-refactorings/%s/git/refs", shortSha1, sha1, mirrorRepoName));
		}
	}
	
}

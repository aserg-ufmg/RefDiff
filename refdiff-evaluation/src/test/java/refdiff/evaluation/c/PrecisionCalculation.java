package refdiff.evaluation.c;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.json.JsonObject;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;

import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Repos;
import com.jcabi.github.RtGithub;
import com.jcabi.http.response.JsonResponse;

import refdiff.core.diff.RastComparator;
import refdiff.core.diff.RastDiff;
import refdiff.core.diff.Relationship;
import refdiff.core.diff.RelationshipType;
import refdiff.core.rast.RastNode;
import refdiff.evaluation.EvaluationUtils;
import refdiff.evaluation.ExternalProcess;
import refdiff.parsers.c.CParser;

public class PrecisionCalculation {

	private static final String OAUTH_TOKEN = "";

	private static final String BASE_DIRECTORY = "data" + File.separator + "c-evaluation";
	private static final String CSV_DIRECTORY = BASE_DIRECTORY + File.separator + "csv";
	private static final String REPOSITORIES_FILE_NAME = CSV_DIRECTORY + File.separator + "1.1-repositories.csv";
	private static final String FORKS_FILE_NAME = CSV_DIRECTORY + File.separator + "1.2-forks.csv";
	private static final String RESULTS_DIRECTORY = CSV_DIRECTORY + File.separator + "results";

	private static final String COMMA = ",";
	private static final String QUOTES = "\"";
	private static final int REPOSITORIES_QUANTITY = 20;
	private static final int NUMBER_OF_COMMITS_TO_CONSIDER = 500;

	private static final String ORGANIZATION_NAME = "refdiff-study";
	
	public static void main(String[] args) throws IOException {
		if (OAUTH_TOKEN.equals("")) {
			System.err.println("You need to set the OAUTH_TOKEN (hard coded) on the code.");
			return;
		}
		
		final Github github = new RtGithub(OAUTH_TOKEN);
		final Repos repos = github.repos();

		List<String[]> originalRepositories = new ArrayList<String[]>();
		List<String[]> forkedRepositories = new ArrayList<String[]>();
		
		getTopRepositories(github, originalRepositories);
		createForks(repos, originalRepositories, forkedRepositories);
		findRefactorings(forkedRepositories);
	}

	private static void getTopRepositories(final Github github, List<String[]> originalRepositories) throws IOException {
		System.out.println();
		System.out.println("1.1) Top " + REPOSITORIES_QUANTITY + " repositories");

		System.out.println("Querying GitHub to get the top " + REPOSITORIES_QUANTITY + " repositories...");

		final JsonResponse response = github.entry()
				.uri().path("/search/repositories")
				.queryParam("q", "language:C")
				.queryParam("sort", "stars")
				.queryParam("order", "desc")
				.back()
				.fetch()
				.as(JsonResponse.class);

		final List<JsonObject> repositories = response.json().readObject()
				.getJsonArray("items")
				.getValuesAs(JsonObject.class)
				.subList(0, REPOSITORIES_QUANTITY);

		File directory = new File(CSV_DIRECTORY);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		
		System.out.println("Printing repositories to " + REPOSITORIES_FILE_NAME + "...");

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new File(REPOSITORIES_FILE_NAME));

			for (JsonObject repository : repositories) {
				String url = repository.getString("html_url");
				String owner = repository.getJsonObject("owner").getString("login");
				String repositoryName = repository.getString("name");
				
				writer.println(url + COMMA + owner + COMMA + repositoryName);
				
				String[] repositoryParts = new String[] {url, owner, repositoryName};
				originalRepositories.add(repositoryParts);
			}

			System.out.println("Done");
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		finally {
			closeWriter(writer);
		}
	}

	private static void createForks(final Repos repos, List<String[]> originalRepositories, List<String[]> forkedRepositories) {
		System.out.println();
		System.out.println("1.2) Fork repositories");

		System.out.println("Printing forks to " + FORKS_FILE_NAME + "...");
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new File(FORKS_FILE_NAME));
			
			for (String[] originalRepositoryParts : originalRepositories) {
				String oldUrl = originalRepositoryParts[0];
				String owner = originalRepositoryParts[1];
				String repositoryName = originalRepositoryParts[2];
				
				repos.get(new Coordinates.Simple(owner, repositoryName))
					.forks()
					.create(ORGANIZATION_NAME);
				
				String[] urlParts = oldUrl.split("/");
				urlParts[urlParts.length - 2] = ORGANIZATION_NAME;
				String newUrl = String.join("/", urlParts);
	
				writer.println(newUrl + COMMA + ORGANIZATION_NAME + COMMA + repositoryName);
				
				String[] forkedRepositoryParts = new String[] {
					newUrl, repositoryName
				};
				
				forkedRepositories.add(forkedRepositoryParts);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
		finally {
			closeWriter(writer);
		}

		System.out.println("Done");
	}

	private static void closeWriter(PrintWriter writer) {
		if (writer != null) {
			writer.close();	
		}
	}
	
	private static void findRefactorings(List<String[]> repositories) {
		System.out.println();
		System.out.println("1.3) Find refactorings on forks of top " + REPOSITORIES_QUANTITY + " repositories");

		File directory = new File(RESULTS_DIRECTORY);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		
		for (int i = 0; i < repositories.size(); i++) {
			String[] repositoryParts = repositories.get(i);
			
			String repositoryUrl = repositoryParts[0];
			String repositoryName = repositoryParts[1];
		
			findRefactoringsOnRepository(repositoryName, repositoryUrl, i + 1, repositories.size());
		}
	}
	
	private static void findRefactoringsOnRepository(String repositoryName, String repositoryUrl, int repositoryOrder, int totalRepositories) {
		String tempFolder = System.getProperty("java.io.tmpdir");
		String repoFolderPath = tempFolder + repositoryName + ".git";
		File repoFolder = new File(tempFolder, repositoryName + ".git");
		
		if (!repoFolder.exists()) {
			System.out.println("Cloning " + repositoryUrl + " into " + repoFolderPath);
			ExternalProcess.execute(new File(tempFolder), "git", "clone", repositoryUrl, "--bare", "--shallow-since=2017-01-01");
		}
		
		String fileName = RESULTS_DIRECTORY + File.separator + repositoryName + ".csv";
		
		System.out.println(repositoryOrder + "/" + totalRepositories + 
				" Analysing " + repositoryName + " and printing refactorings to " + fileName);
		
		int commitsCount = 0;
		Map<String, Integer> relationshipCounts = new HashMap<String, Integer>();
		
		try (
			PrintWriter writer = new PrintWriter(new File(fileName));
			Repository repository = new RepositoryBuilder()
				.setGitDir(repoFolder)
				.readEnvironment()
				.build();
			RevWalk revWalk = new RevWalk(repository)) {
			
			RevCommit head = revWalk.parseCommit(repository.resolve("HEAD"));
			revWalk.markStart(head);
			revWalk.setRevFilter(RevFilter.NO_MERGES);
			
			for (RevCommit commit : revWalk) {
				System.out.println("Commit " + (commitsCount + 1) + "/" + NUMBER_OF_COMMITS_TO_CONSIDER);
				
				String sha = commit.getName();
				
				Set<Relationship> relationships = getRelationships(tempFolder, repoFolderPath, sha);
				
				Map<String, Integer> thisCommitsRelationshipCounts = new HashMap<String, Integer>();
			    
			    for (Relationship relationship : relationships) {
			    	String relationshipString = relationship.getType().toString();
			    	
			    	String before = nodeRepresentation(relationship.getNodeBefore());
	    			String after = nodeRepresentation(relationship.getNodeAfter());
			    	
			    	writer.println(
			    			sha + COMMA + 
			    			relationshipString + COMMA + 
			    			before + COMMA + 
			    			after + COMMA + 
			    			QUOTES + commit.getShortMessage() + QUOTES);
			    	
			    	countRelationship(thisCommitsRelationshipCounts, relationshipString);
			    	countRelationship(relationshipCounts, relationshipString);
			    }
			    
			    System.out.print("Found " + relationships.size() + " relationships");
			    
			    String separator = " - ";
			    
			    for (Entry<String, Integer> relationshipCount : thisCommitsRelationshipCounts.entrySet()) {
			    	String relationship = relationshipCount.getKey();
			    	Integer count = relationshipCount.getValue();
			    	
			    	System.out.print(separator + count + " " + relationship);
			    	separator = ", ";
			    }
			    
			    System.out.println();
			    
			    commitsCount++;
			    if (commitsCount == NUMBER_OF_COMMITS_TO_CONSIDER) {
			    	break;
			    }
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
		finally {
			System.out.print("Done printing " + fileName + ". " + commitsCount + " commits considered");
			
			String separator = ". ";
			int countRelationships = 0;
			for (Entry<String, Integer> countsEntry : relationshipCounts.entrySet()) {
				String relationship = countsEntry.getKey();
				int count = countsEntry.getValue();
				
				countRelationships += count;
				
				System.out.print(separator + count + " " + relationship + " relationships");
				
				separator = "; ";
			}
			
			System.out.print(". Total relationships found " + countRelationships + ".");
			System.out.println();
		}
	}
	
	private static String nodeRepresentation(RastNode node) {
		return QUOTES + node.getLocation().getFile() + ":" + node.getLocalName() + ":" + 
				node.getLocation().getBegin() + "-" + node.getLocation().getEnd() + QUOTES;
	}
	
	private static void countRelationship(Map<String, Integer> counters, String relationship) {
		Integer counter = counters.get(relationship);
		if (counter == null) {
			counter = 0;
		}
		
		counters.put(relationship, counter + 1);
	}
	
	private static Set<Relationship> getRelationships(String tempFolder, String repositoryFolder, String sha) throws Exception {
		CParser parser = new CParser();
		RastComparator rastComparator = new RastComparator(parser, parser);
		EvaluationUtils evaluationUtils = new EvaluationUtils(rastComparator, tempFolder);
		
		RastDiff diff = evaluationUtils.computeDiff(repositoryFolder, sha);
		
		Set<Relationship> relationships = diff.getRelationships().stream()
				.filter(relationship -> !relationship.getType().equals(RelationshipType.SAME))
				.collect(Collectors.toSet());
		
		return relationships;
	}
}

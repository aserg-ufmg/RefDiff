package refdiff.evaluation.c;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.json.JsonObject;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Repos;
import com.jcabi.github.RtGithub;
import com.jcabi.http.response.JsonResponse;

import refdiff.core.diff.CstComparator;
import refdiff.core.diff.CstDiff;
import refdiff.core.diff.Relationship;
import refdiff.core.diff.RelationshipType;
import refdiff.core.io.GitHelper;
import refdiff.core.io.SourceFileSet;
import refdiff.core.cst.CstNode;
import refdiff.core.util.PairBeforeAfter;
import refdiff.evaluation.ExternalProcess;
import refdiff.parsers.c.CPlugin;

public class PrecisionCalculation {

	private static final String BASE_DIRECTORY = "data" + File.separator + "c-evaluation";
	private static final String CSV_DIRECTORY = BASE_DIRECTORY + File.separator + "csv";
	private static final String REPOSITORIES_FILE_NAME = CSV_DIRECTORY + File.separator + "1.1-repositories.csv";
	private static final String FORKS_FILE_NAME = CSV_DIRECTORY + File.separator + "1.2-forks.csv";
	private static final String RESULTS_DIRECTORY = CSV_DIRECTORY + File.separator + "results";

	private static final String COMMA = ",";
	private static final int REPOSITORIES_QUANTITY = 20;
	private static final int NUMBER_OF_COMMITS_TO_CONSIDER = 500;

	private static final String ORGANIZATION_NAME = "refdiff-study";
	
	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			System.err.println("You need enter at least an OAuth token as an argument to the app.");
			return;
		}
		else if (args.length > 1) {
			for (int i = 1; i < args.length; i++) {
				findRefactoringsOnRepository(args[i], "https://github.com/refdiff-study/" + args[i], i, args.length - 1);	
			}
			
			return;
		}
		
		String oAuthToken = args[0];
		
		final Github github = new RtGithub(oAuthToken);
		final Repos repos = github.repos();

		List<String[]> originalRepositories = new ArrayList<String[]>();
		List<String[]> forkedRepositories = new ArrayList<String[]>();
		
		getTopRepositories(github, originalRepositories);
		createForks(repos, originalRepositories, forkedRepositories);
		findRefactorings(forkedRepositories);
	}

	private static void getTopRepositories(final Github github, List<String[]> originalRepositories) throws IOException {
		Set<String> repositoriesToSkip = new HashSet<String>();
		repositoriesToSkip.add("The-Art-Of-Programming-By-July");
		repositoriesToSkip.add("How-to-Make-a-Computer-Operating-System");
		
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
				.subList(0, REPOSITORIES_QUANTITY + repositoriesToSkip.size());

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
				
				if (repositoriesToSkip.contains(repositoryName)) {
					continue;
				}
				
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
		long timeBefore = System.currentTimeMillis();
		
		String tempFolder = System.getProperty("java.io.tmpdir");
		String repoFolderPath = tempFolder + repositoryName + ".git";
		File repoFolder = new File(tempFolder, repositoryName + ".git");
		
		if (!repoFolder.exists()) {
			System.out.println("Cloning " + repositoryUrl + " into " + repoFolderPath);
			ExternalProcess.execute(new File(tempFolder), "git", "clone", repositoryUrl, repoFolder.getPath(), "--bare", "--depth=1000");
		}
		
		String fileName = RESULTS_DIRECTORY + File.separator + repositoryName + ".csv";
		
		System.out.println(repositoryOrder + "/" + totalRepositories + 
				" Analysing " + repositoryName + " and printing refactorings to " + fileName);
		
		AtomicInteger commitsCount = new AtomicInteger(0);
		Map<String, Integer> relationshipCounts = new HashMap<String, Integer>();
		
		GitHelper gh = new GitHelper();
		
		try (
			PrintWriter writer = new PrintWriter(new File(fileName));
			Repository repository = gh.openRepository(repoFolder);) 
		{
			gh.forEachNonMergeCommit(repository, NUMBER_OF_COMMITS_TO_CONSIDER, (RevCommit commitBefore, RevCommit commitAfter) -> {
				String sha = commitAfter.getName();
				
				System.out.println("Commit " + (commitsCount.intValue() + 1) + "/" + NUMBER_OF_COMMITS_TO_CONSIDER + " - " + sha);
				
				Set<Relationship> relationships;
				try {
					relationships = getRelationships(gh, repository, commitBefore, commitAfter);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				
				Map<String, Integer> thisCommitsRelationshipCounts = new HashMap<String, Integer>();
			    
			    for (Relationship relationship : relationships) {
			    	String relationshipString = getRelationshipType(relationship);
			    			
			    	String before = nodeRepresentation(relationship.getNodeBefore());
	    			String after = nodeRepresentation(relationship.getNodeAfter());
			    	
	    			String[] fields = new String[] {
	    				csvField(repositoryName),
    					csvField(sha), 
    					csvField(relationshipString), 
    					csvField(before), 
    					csvField(after), 
    					csvField(commitAfter.getShortMessage()), 
    					Double.toString(Math.random())
	    			};
	    			
			    	writer.println(String.join(COMMA, fields));
			    	
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
			    
			    int countTotal = 0;
				for (Entry<String, Integer> countsEntry : relationshipCounts.entrySet()) {
					countTotal += countsEntry.getValue();
				}
			    
			    System.out.println(" (total " + countTotal + ")");
			    
			    commitsCount.incrementAndGet();
			});
		} 
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
		finally {
			long timeAfter = System.currentTimeMillis();
			
			System.out.print("Done printing " + fileName + ". " + commitsCount.get() + " commits considered");
			
			String separator = ". ";
			int countRelationships = 0;
			for (Entry<String, Integer> countsEntry : relationshipCounts.entrySet()) {
				String relationship = countsEntry.getKey();
				int count = countsEntry.getValue();
				
				countRelationships += count;
				
				System.out.print(separator + count + " " + relationship + " relationships");
				
				separator = "; ";
			}
			
			System.out.println(". Total relationships found " + countRelationships + ".");
			System.out.println("Took " + ((timeAfter - timeBefore) / 1000) + " seconds");
		}
	}
	
	private static String getRelationshipType(Relationship relationship) {
		RelationshipType relationshipType = relationship.getType();
		
		String relationshipString = relationshipType.toString();
		
		if (relationshipType.equals(RelationshipType.MOVE)
				|| relationshipType.equals(RelationshipType.MOVE_RENAME) 
				|| relationshipType.equals(RelationshipType.RENAME)) {
			CstNode nodeBefore = relationship.getNodeBefore();
			CstNode nodeAfter = relationship.getNodeAfter();
			
			String nodeTypeBefore = nodeBefore.getType();
			String nodeTypeAfter = nodeAfter.getType();
			
			if (!nodeTypeBefore.equalsIgnoreCase(nodeTypeAfter)) {
				throw new RuntimeException("Before and After nodes should be of the same type");
			}
			
			if (nodeTypeBefore.equals("Program")) {
				relationshipString += "_FILE";
			}
			else {
				relationshipString += "_FUNCTION";
			}
		}
		
		return relationshipString;
	}

	private static String csvField(String field) {
		return StringEscapeUtils.escapeCsv(field);
	}
	
	private static String nodeRepresentation(CstNode node) {
		return node.getLocation().getFile() + ":" + node.getLocalName() + ":" + node.getLocation().getBegin() + "-" + node.getLocation().getEnd();
	}
	
	private static void countRelationship(Map<String, Integer> counters, String relationship) {
		Integer counter = counters.get(relationship);
		if (counter == null) {
			counter = 0;
		}
		
		counters.put(relationship, counter + 1);
	}
	
	private static Set<Relationship> getRelationships(GitHelper gh, Repository repository, RevCommit commitBefore, RevCommit commitAfter) 
			throws Exception {
		CPlugin parser = new CPlugin();
		CstComparator cstComparator = new CstComparator(parser);
		
		PairBeforeAfter<SourceFileSet> sources = gh.getSourcesBeforeAndAfterCommit(
				repository, commitBefore, commitAfter, parser.getAllowedFilesFilter());
		CstDiff diff = cstComparator.compare(sources);
		
		Set<Relationship> relationships = diff.getRelationships().stream()
				.filter(relationship -> !relationship.getType().equals(RelationshipType.SAME))
				.collect(Collectors.toSet());
		
		return relationships;
	}
}

package refdiff.evaluation.benchmark;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import refdiff.core.api.GitService;
import refdiff.core.util.GitServiceImpl;

public class BuildBenchmarkDataset {

    public static void main(String[] args) throws Exception {
        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.configure(SerializationFeature.INDENT_OUTPUT, true);
        String baseDir = "d:/tmp/";

        JsonCommit[] commits = om.readValue(new File("data/fse/refactorings.json"), JsonCommit[].class);
        List<JsonCommit> commits2 = new ArrayList<>();

        GitService gs = new GitServiceImpl();
        for (JsonCommit commit : commits) {
            String folderName = baseDir + commit.repository.substring(commit.repository.lastIndexOf('/'), commit.repository.lastIndexOf('.'));
            try (Repository repo = gs.cloneIfNotExists(folderName, commit.repository)) {
                gs.checkout(repo, commit.sha1);
                commits2.add(commit);
            } catch (MissingObjectException e) {
                // ignore
            }
        }
        om.writeValue(new File("data/fse/refactorings2.json"), commits2);
    }

}

class JsonCommit {
    public String repository;
    public String sha1;
    public List<JsonRefactoring> refactorings;
}

class JsonRefactoring {
    public String type;
    public String description;
}
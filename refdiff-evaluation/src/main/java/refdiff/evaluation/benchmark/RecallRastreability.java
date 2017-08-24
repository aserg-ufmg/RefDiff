package refdiff.evaluation.benchmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import refdiff.core.api.RefactoringType;
import refdiff.evaluation.utils.RefactoringSet;
import refdiff.evaluation.utils.ResultComparator;

public class RecallRastreability {

    public static void main(String[] args) {
        
        
        
    }
    
    private Map<String, RefactoringSet> mapActual = new HashMap<>(); 
    private Map<String, RefactoringSet> mapExpected = new HashMap<>(); 
    
    public void run() {
        File folder = new File("C:/tmp/RefDiff-data-icse");
        for (File f : folder.listFiles()) {
            readActual(f);
        }
        
    }
    
    private RefactoringSet getActualRs(String project, String sha1) {
        return mapActual.computeIfAbsent(project + "/" + sha1, k -> new RefactoringSet(project, sha1));
    }

    private RefactoringSet getExpectedRs(String project, String sha1) {
        return mapExpected.computeIfAbsent(project + "/" + sha1, k -> new RefactoringSet(project, sha1));
    }
    
    public void readActual(File file) {
        String project = file.getName().substring("model_".length());
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] array = line.split(";");
                    String refTypeString = array[0].trim();
                    String entityBefore = array[2].trim();
                    String entityAfter = array[3].trim();
                    String sha1 = array[4].trim();
                    RefactoringType refactoringType = findByNameCC(refTypeString);
                    RefactoringSet rs = getActualRs(project, sha1);
                    rs.add(refactoringType, entityBefore, entityAfter);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void readExpected(File file) {
        String project = file.getName().substring("model_".length());
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] array = line.split(";");
                    String refTypeString = array[0].trim();
                    String entityBefore = array[2].trim();
                    String entityAfter = array[3].trim();
                    String sha1 = array[4].trim();
                    RefactoringType refactoringType = findByNameCC(refTypeString);
                    RefactoringSet rs = getActualRs(project, sha1);
                    rs.add(refactoringType, entityBefore, entityAfter);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    private RefactoringType findByNameCC(String name) {
        for (RefactoringType r : RefactoringType.values()) {
            if (r.getDisplayName().replace(" ", "").equals(name)) {
                return r;
            }
        }
        throw new RuntimeException(name + " not found");
    }
}

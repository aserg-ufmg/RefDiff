package refdiff.evaluation.benchmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import refdiff.core.api.RefactoringType;
import refdiff.evaluation.utils.RefactoringSet;
import refdiff.evaluation.utils.ResultComparator;

public class RecallRastreability {

    public static void main(String[] args) {
        new RecallRastreability().run();
    }
    
    private Map<String, RefactoringSet> mapActual = new HashMap<>(); 
    private Map<String, RefactoringSet> mapExpected = new HashMap<>(); 
    
    public void run() {
    	readExpected(new File("C:/Users/m24063/RefDiff-data-icse/expected"));
    	
        File folder = new File("C:/Users/m24063/RefDiff-data-icse");
        for (File f : folder.listFiles()) {
            if (f.getName().startsWith("model_")) {
            	readActual(f);
            }
        }
        
        ResultComparator rc = new ResultComparator();
        rc.expect(mapExpected.values());
        rc.compareWith("RefDiff", mapActual.values());
        
        rc.printSummary(System.out, EnumSet.allOf(RefactoringType.class));
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
                    if (refTypeString.startsWith("Same")) continue;
                    
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
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] array = line.split("\t");
                    if (array.length != 4) continue;
                    String url = array[0].trim();
                    String refTypeString = array[1].trim();
                    String entityBefore = array[2].trim();
                    String entityAfter = array[3].trim();
                    if (refTypeString.isEmpty()) continue;
                    if (refTypeString.startsWith("Same")) continue;
                    
                    Matcher m = urlPattern.matcher(url);
                    if (!m.matches()) {
                    	throw new RuntimeException("Not matched: " + url);
                    }
                    String project = m.group(1);
                    String sha1 = m.group(2);
                    RefactoringType refactoringType = findByNameCC(refTypeString);
                    RefactoringSet rs = getExpectedRs(project, sha1);
                    rs.add(refactoringType, entityBefore, entityAfter);
                    //System.out.println(String.format("%s\t%s\t%s\t%s\t%s", project, sha1, refTypeString, entityBefore, entityAfter));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    //private Pattern urlPattern = Pattern.compile("https://github\\.com/[^/]+/([^/]+)/commit/([^\\?/])+(\\?.*)?");
    private Pattern urlPattern = Pattern.compile("https://github\\.com/[^/]+/([^/]+)/commit/([^?]+)(\\?.*)?");
    
    private RefactoringType findByNameCC(String name) {
    	if ("PullU Method".equals(name)) {
    		return RefactoringType.PULL_UP_OPERATION;
    	}
        for (RefactoringType r : RefactoringType.values()) {
            if (r.getDisplayName().replace(" ", "").equals(name.replace(" ", ""))) {
                return r;
            }
        }
        throw new RuntimeException(name + " not found");
    }
}

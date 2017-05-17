package refdiff.evaluation;

import refdiff.evaluation.utils.RefactoringSet;

public class PrintArqsoft16Dataset {

    public static void main(String[] args) {
        Arqsoft16Dataset oracle = new Arqsoft16Dataset();
        for (RefactoringSet rs : oracle.all()) {
            System.out.println(rs.getProject() + "/" + rs.getRevision());
            rs.printCsv(System.out);
        }
        
    }
}
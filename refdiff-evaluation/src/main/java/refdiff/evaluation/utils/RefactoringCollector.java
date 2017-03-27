package refdiff.evaluation.utils;

import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

import gr.uom.java.xmi.diff.ExtractOperationRefactoring;
import gr.uom.java.xmi.diff.ExtractSuperclassRefactoring;
import gr.uom.java.xmi.diff.InlineOperationRefactoring;
import gr.uom.java.xmi.diff.MoveAttributeRefactoring;
import gr.uom.java.xmi.diff.MoveClassRefactoring;
import gr.uom.java.xmi.diff.MoveOperationRefactoring;
import gr.uom.java.xmi.diff.RenameClassRefactoring;
import gr.uom.java.xmi.diff.RenameOperationRefactoring;
import refdiff.core.api.Refactoring;
import refdiff.core.api.RefactoringHandler;
import refdiff.core.api.RefactoringType;
import refdiff.core.rm2.model.refactoring.SDRefactoring;
import refdiff.evaluation.rm.RefactoringAdapter;

public class RefactoringCollector extends RefactoringHandler {
    private final RefactoringSet rs;
    private Exception ex = null;

    public RefactoringCollector(String cloneUrl, String commitId) {
        rs = new RefactoringSet(cloneUrl, commitId);
    }

    @Override
    public void handle(RevCommit commitData, List<Refactoring> refactorings) {
        for (Refactoring rx : refactorings) {
            if (rx instanceof SDRefactoring) {
                SDRefactoring sdr = (SDRefactoring) rx;
                rs.add(new RefactoringRelationship(rx.getRefactoringType(), sdr.getEntityBefore().toString(), sdr.getEntityAfter().toString()));
            } else if (rx instanceof RefactoringAdapter) {
                org.refactoringminer.api.Refactoring r = ((RefactoringAdapter) rx).getRefactoring();
                if (r instanceof MoveClassRefactoring) {
                    MoveClassRefactoring ref = (MoveClassRefactoring) r;
                    rs.add(new RefactoringRelationship(refactoringTypeOf(r), ref.getOriginalClassName(), ref.getMovedClassName()));
                } else if (r instanceof RenameClassRefactoring) {
                    RenameClassRefactoring ref = (RenameClassRefactoring) r;
                    rs.add(new RefactoringRelationship(refactoringTypeOf(r), ref.getOriginalClassName(), ref.getRenamedClassName()));
                } else if (r instanceof ExtractSuperclassRefactoring) {
                    ExtractSuperclassRefactoring ref = (ExtractSuperclassRefactoring) r;
                    for (String subclass : ref.getSubclassSet()) {
                        rs.add(new RefactoringRelationship(refactoringTypeOf(r), subclass, ref.getExtractedClass().getName()));
                    }
                } else if (r instanceof MoveOperationRefactoring) {
                    MoveOperationRefactoring ref = (MoveOperationRefactoring) r;
                    rs.add(new RefactoringRelationship(refactoringTypeOf(r), ref.getOriginalOperation().getKey(), ref.getMovedOperation().getKey()));
                } else if (r instanceof RenameOperationRefactoring) {
                    RenameOperationRefactoring ref = (RenameOperationRefactoring) r;
                    rs.add(new RefactoringRelationship(refactoringTypeOf(r), ref.getOriginalOperation().getKey(), ref.getRenamedOperation().getKey()));
                } else if (r instanceof ExtractOperationRefactoring) {
                    ExtractOperationRefactoring ref = (ExtractOperationRefactoring) r;
                    rs.add(new RefactoringRelationship(refactoringTypeOf(r), ref.getExtractedFromOperation().getKey(), ref.getExtractedOperation().getKey()));
                } else if (r instanceof InlineOperationRefactoring) {
                    InlineOperationRefactoring ref = (InlineOperationRefactoring) r;
                    rs.add(new RefactoringRelationship(refactoringTypeOf(r), ref.getInlinedOperation().getKey(), ref.getInlinedToOperation().getKey()));
                } else if (r instanceof MoveAttributeRefactoring) {
                    MoveAttributeRefactoring ref = (MoveAttributeRefactoring) r;
                    String attrName = ref.getMovedAttribute().getName();
                    rs.add(new RefactoringRelationship(refactoringTypeOf(r), ref.getSourceClassName() + "#" + attrName, ref.getTargetClassName() + "#" + attrName));
                } else {
                    throw new RuntimeException("refactoring not supported");
                }
            } 
        }
    }

    private static RefactoringType refactoringTypeOf(org.refactoringminer.api.Refactoring r) {
        return RefactoringType.valueOf(r.getRefactoringType().toString());
    }

    @Override
    public void handleException(String commitId, Exception e) {
        this.ex = e;
    }

    public RefactoringSet assertAndGetResult() {
        if (ex == null) {
            return rs;
        }
        throw new RuntimeException(ex);
    }
}
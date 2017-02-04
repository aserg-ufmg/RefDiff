package lsclipse.views;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import lsclipse.LSDiffRunner;
import lsclipse.dialogs.ProgressBarDialog;

public class TreeViewCommander {

    TreeView treeView;
    
    public TreeViewCommander() throws PartInitException {
        treeView = (TreeView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("lsclipse.views.TreeView");
    }

    public void detectRefactorings(IProject baseproj, IProject newproj) {
        long start = System.currentTimeMillis();

        // remember base project (and new project)
        treeView.baseproj = baseproj;
        treeView.newproj = newproj;

        // open new log box
        final ProgressBarDialog pbdiag = new ProgressBarDialog(treeView.parent.getShell());
        pbdiag.open();
        pbdiag.setStep(0);

        // do lsdiff (ish)
        if ((new LSDiffRunner()).doFactExtractionForRefFinder(baseproj.getName(), newproj.getName(), pbdiag)) {
            treeView.refreshTree();
        } else {
            System.out
                    .println("Something went wrong - fact extraction failed");
        }
        pbdiag.setStep(5);
        pbdiag.setMessage("Cleaning up... ");
        pbdiag.appendLog("OK\n");

        pbdiag.dispose();
        long end = System.currentTimeMillis();
        System.out.println("Total time: " + (end - start));
        
        for (Node node : treeView.nodeList) {
            //node.
            node.toString();
        }
    }

}

package refdiff.adapter.reffinder.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import lsclipse.LSDiffRunner;
import lsclipse.dialogs.ProgressBarDialog;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class RunHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

        final ProgressBarDialog pbdiag = new ProgressBarDialog(window.getShell());
        pbdiag.open();
        pbdiag.setStep(0);

        List<IProject> projects = new ArrayList<>();
        LSDiffRunner lsDiffRunner = new LSDiffRunner();
        for (IProject proj : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
            String name = proj.getName();
            if (name.endsWith("-v1")) {
                projects.add(proj);
            }
        }
        
        boolean success = true;
        for (IProject project : projects) {
            String name = project.getName();
            name.substring(0, name.length() - 3);
            String p0 = name + "-v0";
            String p1 = name + "-v1";
            success &= lsDiffRunner.doFactExtractionForRefFinder(p0, p1, pbdiag);
            File result = new File(project.getLocation().toFile(), "RefList.xml");
            if (result.exists()) {
                System.out.println("Analyzed project " + name);
            } else {
                success = false;
            }
        }

        if (success) {
            MessageDialog.openInformation(window.getShell(), "refdiff-adapter-ref-finder", "Success");
        } else {
            MessageDialog.openInformation(window.getShell(), "refdiff-adapter-ref-finder", "Error");
        }
        pbdiag.setStep(5);
        pbdiag.setMessage("Cleaning up... ");
        pbdiag.appendLog("OK\n");

        pbdiag.dispose();

        return null;
    }
}

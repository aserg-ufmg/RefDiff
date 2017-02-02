package refdiff.adapter.reffinder.handlers;

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

import lsclipse.LSDResult;
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

//        Application refFinder = new Application();
//
//        IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
//        try {
//            for (IProject project1 : allProjects) {
//                String name = project1.getName();
//                if (name.endsWith("-v1")) {
//                    String baseName = name.substring(0, name.length() - 3);
//                    IProject project0 = find(allProjects, baseName + "-v0");
//                    refFinder.myStart(project0, project1, null);
//                    
//                    File resultFile = new File(project1.getLocation().toFile(), "RefList.xml");
//                    if (resultFile.exists()) {
//                        System.out.println("Analyzed project " + baseName);
//                    } else {
//                        MessageDialog.openInformation(window.getShell(), "refdiff-adapter-ref-finder", "Error");
//                    }
//                }
//            }
//        } catch (Exception e) {
//            System.err.println(e.getMessage());
//            e.printStackTrace(System.err);
//        }

        IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        List<IProject> projects = new ArrayList<>();
        for (IProject project1 : allProjects) {
            String name = project1.getName();
            if (name.endsWith("-v1")) {
                projects.add(project1);
            }
        }

        boolean success = true;
        for (IProject project : projects) {
            String name = project.getName();
            name = name.substring(0, name.length() - 3);
            String p0 = name + "-v0";
            String p1 = name + "-v1";

            final ProgressBarDialog pbdiag = new ProgressBarDialog(window.getShell());
            pbdiag.open();
            pbdiag.setStep(0);
            List<LSDResult> results = (new LSDiffRunner()).doLSDiff(p0, p1, pbdiag);
            if (results != null) {
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

        return null;
    }
    
    private IProject find(IProject[] allProjects, String name) {
        for (IProject project : allProjects) {
            if (project.getName().equals(name)) {
                return project;
            }
        }
        return null;
    }
    
}

package refdiff.adapter.reffinder.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import lsclipse.views.TreeViewCommander;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class RunHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        //IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        try {
            IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
            TreeViewCommander tvc = new TreeViewCommander();
            
            for (IProject project1 : allProjects) {
                String name = project1.getName();
                if (name.endsWith("-v1")) {
                    String baseName = name.substring(0, name.length() - 3);
                    IProject project0 = find(allProjects, baseName + "-v0");
                    tvc.detectRefactorings(project0, project1);
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
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

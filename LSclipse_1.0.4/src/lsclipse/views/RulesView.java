package lsclipse.views;

import java.util.ArrayList;
import java.util.HashMap;

import lsclipse.LSDResult;
import lsclipse.LSDiffRunner;
import lsclipse.dialogs.ProgressBarDialog;
import lsclipse.dialogs.SelectProjectDialog;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

public class RulesView extends ViewPart {
	Action selectAction, explainAction, englishAction, filterAction, sortAction;
    TabFolder tabFolder;
    GridData layoutData1;
    GridData layoutHidden;
    Composite parent;
    Table rulesTable;
	TabItem tabItemExamples;
	TabItem tabItemExceptions;
	List examplesList;
	List exceptionsList;
	ProgressBarDialog progbar;
	java.util.List<LSDResult> rules = new ArrayList<LSDResult>();
	IProject baseproj = null;
	IProject newproj = null;

	public void createPartControl(Composite parent) {
		this.parent = parent;

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
	    parent.setLayout(layout);

	    //declare showing layout
		layoutData1 = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		layoutData1.grabExcessHorizontalSpace = true;
		layoutData1.grabExcessVerticalSpace = true;
		layoutData1.horizontalAlignment = GridData.FILL;
		layoutData1.verticalAlignment = GridData.FILL;
		layoutData1.exclude = false;

	    //declare 'hidden' layout
		layoutHidden = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		layoutHidden.grabExcessHorizontalSpace = true;
		layoutHidden.grabExcessVerticalSpace = true;
		layoutHidden.horizontalAlignment = GridData.FILL;
		layoutHidden.verticalAlignment = GridData.FILL;
		layoutHidden.exclude = true;

		//make rules table
	    rulesTable = new Table(parent, SWT.SINGLE | SWT.FULL_SELECTION);
		TableColumn col1 = new TableColumn(rulesTable, SWT.NULL);
		rulesTable.setHeaderVisible(true);
		col1.setText("Accuracy");
		col1.pack();
		TableColumn col2 = new TableColumn(rulesTable, SWT.NULL);
		col2.setText("Rule");
		col2.setWidth(430);
		rulesTable.setLayoutData(layoutData1);
		rulesTable.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event e) {
		          refreshExamples();
		        }
			});

		//make tabfolder and example lists
		tabFolder = new TabFolder (parent, 0);
		tabItemExamples = new TabItem (tabFolder, SWT.NONE);
		tabItemExamples.setText ("Changes");
		examplesList = new List(tabFolder, SWT.SINGLE);
		examplesList.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent arg0) {
				//IMPT!!! double click listener called AFTER the new line is selected
				//check if there is a
				MessageDialog.openError(examplesList.getShell(), "File selection error", examplesList.getSelection()[0]);
			}
			public void mouseDown(MouseEvent arg0) {
			}
			public void mouseUp(MouseEvent arg0) {
			}
		});
		tabItemExamples.setControl(examplesList);
		tabItemExceptions = new TabItem (tabFolder, SWT.NONE);
		tabItemExceptions.setText ("Exceptions");
		exceptionsList = new List(tabFolder, SWT.SINGLE);
		tabItemExceptions.setControl(exceptionsList);
		tabFolder.setLayoutData(layoutHidden);

		parent.layout();

		createActions();
        createMenu();
        createToolbar();
	}
    public void createActions() {
    	//Select Action
		selectAction = new Action("Select version...") {
			public void run() {
				
		    	//collect information from seldiag
				final SelectProjectDialog seldiag = new SelectProjectDialog(parent.getShell());
		    	final int returncode = seldiag.open();
		    	if (returncode>0) return;

		    	//remember base project (and new project)
				baseproj = ResourcesPlugin.getWorkspace().getRoot().getProject(seldiag.getProj1());
				newproj = ResourcesPlugin.getWorkspace().getRoot().getProject(seldiag.getProj2());

		    	//open new log box
				final ProgressBarDialog pbdiag = new ProgressBarDialog(parent.getShell());
				pbdiag.open();
				pbdiag.setStep(0);

				//do lsdiff
				rules = (new LSDiffRunner()).doLSDiff(seldiag.getProj1(), seldiag.getProj2(), pbdiag);

				//display results on view
				refreshRules();
				refreshExamples();
			}
		};
		selectAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_OBJ_FOLDER));

		//Explain Action
		explainAction = new Action("Explain") {
			public void run() {
				if (tabFolder.getLayoutData().equals(layoutHidden)) {
					showRulesList();
				} else {
					hideRulesList();
				}
			}
		};
		explainAction.setImageDescriptor(lsclipse.LSclipse.getImageDescriptor("icons/explain.gif"));

		//English Action
		englishAction = new Action("Translate to English") {
			public void run() {
				//Do something smart here
			}
		};
		englishAction.setImageDescriptor(lsclipse.LSclipse.getImageDescriptor("icons/english.gif"));

		//*	TODO: Currently do not have sort and filter functions
		//Sort Action
		sortAction = new Action("Sort") {
			public void run() {
			}
		};
		sortAction.setImageDescriptor(lsclipse.LSclipse.getImageDescriptor("icons/sort.gif"));

		//Filter Action
		filterAction = new Action("Filter") {
			public void run() {
			}
		};
		filterAction.setImageDescriptor(lsclipse.LSclipse.getImageDescriptor("icons/filter.gif"));
		//*/
	}
	private void createToolbar() {
        IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
        mgr.add(selectAction);
        mgr.add(explainAction);
        mgr.add(englishAction);
        mgr.add(sortAction);
        mgr.add(filterAction);
	}
	private void createMenu() {
//        IMenuManager mgr = getViewSite().getActionBars().getMenuManager();
//        mgr.add(selectAllAction);
	}

	public void setFocus() {
	}

	private void showRulesList() {
		tabFolder.setLayoutData(layoutData1);
		tabFolder.layout();
		parent.layout();
	}
    private void hideRulesList() {
		tabFolder.setLayoutData(layoutHidden);
		tabFolder.layout();
		parent.layout();
    }

    private void refreshRules() {
    	rulesTable.removeAll();
    	for (int i=0; i<rules.size(); ++i) {
    		LSDResult rule = rules.get(i);
    		TableItem ti = new TableItem(rulesTable, SWT.NULL);
    		ti.setText(new String[] { rule.num_matches+"/"+(rule.num_matches+rule.num_counter), rule.desc });
    	}
    	rulesTable.layout();
    }

    private void refreshExamples() {
    	refreshExamples(rulesTable.getSelectionIndex());
    }

    private void refreshExamples(int index) {

    	if (index<0 || index>=rules.size()) return; //array out of bounds

    	LSDResult rule = rules.get(index);

    	//refresh examples
    	tabItemExamples.setText("Changes ("+rule.examples.size()+")");
    	examplesList.removeAll();
    	for (String s : rule.getExampleStr()) examplesList.add(s);

    	//refresh exceptions
    	tabItemExceptions.setText("Exceptions ("+rule.exceptions.size()+")");
    	exceptionsList.removeAll();
    	for (String s : rule.getExceptionsString()) exceptionsList.add(s);
    }

    @SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	public static void openInEditor(IFile file, int startpos, int length) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		HashMap map = new HashMap();
		map.put(IMarker.CHAR_START, new Integer(startpos));
		map.put(IMarker.CHAR_END, new Integer(startpos+length));
		map.put(IWorkbenchPage.EDITOR_ID_ATTR, 
				"org.eclipse.ui.DefaultTextEditor");
		try {
			IMarker marker = file.createMarker(IMarker.TEXT);
			marker.setAttributes(map);

			IDE.openEditor(page, marker);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

}

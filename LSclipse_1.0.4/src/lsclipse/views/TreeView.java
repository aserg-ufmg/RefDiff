package lsclipse.views;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import lsclipse.LSDiffRunner;
import lsclipse.TopologicalSort;
import lsclipse.dialogs.ProgressBarDialog;
import lsclipse.dialogs.SelectProjectDialog;
import metapackage.MetaInfo;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class TreeView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "lsclipse.views.TreeView";

	private List viewer;
	private List list;
	private Action doubleClickTreeAction;
	private Action doubleClickListAction;
	private Action selectAction;
	private Composite parent;
	private Vector<Node> nodeList;
	private Map<String, Node> allNodes;
	private HashMap<String, Node> hashNode;
	HashMap<String, Node> strNodeRelation;
	GridData layoutData1;
	ArrayList<EditorInput> listDiffs = new ArrayList<EditorInput>();
	IProject baseproj = null;
	IProject newproj = null;

	/**
	 * The constructor.
	 */
	public TreeView() {
		nodeList = new Vector<Node>();
		hashNode = new HashMap<String, Node>();
		allNodes = new HashMap<String, Node>();
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		parent.setLayout(layout);

		layoutData1 = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		layoutData1.grabExcessHorizontalSpace = true;
		layoutData1.grabExcessVerticalSpace = true;
		layoutData1.horizontalAlignment = GridData.FILL;
		layoutData1.verticalAlignment = GridData.FILL;
		layoutData1.exclude = false;

		this.parent = parent;
		viewer = new List(this.parent, SWT.SINGLE | SWT.V_SCROLL);
		list = new List(this.parent, SWT.SINGLE | SWT.V_SCROLL);
		viewer.setLayoutData(layoutData1);
		list.setLayoutData(layoutData1);

		parent.layout();

		makeActions();
		hookDoubleClickAction();
		contributeToActionBars();

		IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
		mgr.add(selectAction);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		// manager.add(dummyNodeAction);
		// manager.add(new Separator());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
	}

	class EditorInput extends CompareEditorInput {
		public EditorInput(CompareConfiguration configuration) {
			super(configuration);
		}

		String base_;
		String mod_;

		public void setBase(InputStream inputStream) {
			try {
				base_ = convertToString(inputStream);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void setBase(String s) {
			base_ = s;
		}

		private String convertToString(InputStream is) throws IOException {
			final char[] buffer = new char[0x10000];
			StringBuilder out = new StringBuilder();
			Reader in = new InputStreamReader(is, "UTF-8");
			int read;
			while ((read = in.read(buffer, 0, buffer.length)) >= 0) {
				if (read > 0) {
					out.append(buffer, 0, read);
				}
			}
			return out.toString();
		}

		public void setMod(InputStream inputStream) {
			try {
				mod_ = convertToString(inputStream);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void setMod(String s) {
			mod_ = s;
		}

		@Override
		public Object prepareInput(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
			Differencer d = new Differencer();
			Object diff = d.findDifferences(false, new NullProgressMonitor(),
					null, null, new Input(base_), new Input(mod_));
			return diff;
		}

		class Input implements ITypedElement, IStreamContentAccessor {
			String fContent;

			public Input(String s) {
				fContent = s;
			}

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return "name";
			}

			@Override
			public String getType() {
				// TODO Auto-generated method stub
				return "java";
			}

			@Override
			public InputStream getContents() throws CoreException {
				// TODO Auto-generated method stub
				return new ByteArrayInputStream(fContent.getBytes());
			}

			@Override
			public Image getImage() {
				// TODO Auto-generated method stub
				return null;
			}
		}
	}

	private void makeActions() {
		// Double click a Node to open the associated file.
		doubleClickListAction = new Action() {
			public void run() {
				int selind = list.getSelectionIndex();
				if (selind >= listDiffs.size())
					return;
				EditorInput file = listDiffs.get(selind);
				if (file == null)
					return;

				CompareUI.openCompareEditor(file);
			}
		};

		// Double click a refactoring to display the associated information.
		doubleClickTreeAction = new Action() {
			public void run() {

				// clear list
				list.removeAll();
				listDiffs.clear();

				// Print out the list of code elements associated with this
				// refactoring
				int index = viewer.getSelectionIndex();
				if (index < 0 || index >= nodeList.size())
					return;

				Node node = nodeList.get(index);

				list.add(node.getName());
				listDiffs.add(null);
				list.add(node.params);
				listDiffs.add(null);
				// Seperator
				list.add(" ");
				listDiffs.add(null);

				int numtabs = 0;

				for (String statement : node.getDependents()) {
					StringBuilder output = new StringBuilder();
					if (statement.equals(")"))
						--numtabs;

					for (int i = 0; i < numtabs; ++i) {
						output.append("\t");
					}
					if (statement.equals("(")) {
						output.append("(");
						++numtabs;
					} else {
						output.append(statement);
					}
					list.add(output.toString());
					listDiffs.add(null);
				}

				if (!node.oldFacts.isEmpty()) {
					list.add("");
					listDiffs.add(null);
					list.add("Compare:");
					listDiffs.add(null);
					for (String display : node.oldFacts.keySet()) {
						IJavaElement filenameBase = node.oldFacts.get(display);
						IJavaElement filenameMod = node.newFacts.get(display);
						list.add(display);
						EditorInput ei = new EditorInput(
								new CompareConfiguration());
						try {
							ei.setBase(((IFile) filenameBase
									.getCorrespondingResource()).getContents());
						} catch (Exception ex) {
							ei.setBase("");
						}
						try {
							ei.setMod(((IFile) filenameMod
									.getCorrespondingResource()).getContents());
						} catch (Exception ex) {
							ei.setMod("");
						}
						listDiffs.add(ei);
					}
				}

			}
		};
		// Select Action
		selectAction = new Action("Select version...") {
			public void run() {
				// collect information from seldiag
				final SelectProjectDialog seldiag = new SelectProjectDialog(
						parent.getShell());
				final int returncode = seldiag.open();
				if (returncode > 0)
					return;

				long start = System.currentTimeMillis();

				// remember base project (and new project)
				baseproj = ResourcesPlugin.getWorkspace().getRoot()
						.getProject(seldiag.getProj1());
				newproj = ResourcesPlugin.getWorkspace().getRoot()
						.getProject(seldiag.getProj2());

				// open new log box
				final ProgressBarDialog pbdiag = new ProgressBarDialog(
						parent.getShell());
				pbdiag.open();
				pbdiag.setStep(0);

				// do lsdiff (ish)
				if ((new LSDiffRunner()).doFactExtractionForRefFinder(
						seldiag.getProj1(), seldiag.getProj2(), pbdiag)) {
					refreshTree();
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
			}
		};
		selectAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER));
	}

	public void refreshTree() {
		list.removeAll();
		nodeList.clear();
		viewer.removeAll();
		System.out.println(baseproj.toString());
		System.out.println(newproj.toString());

		TopologicalSort tSort = new TopologicalSort();

		long beforetime = System.currentTimeMillis();
		tSort.sort(MetaInfo.refsOnlyFile);
		long aftertime = System.currentTimeMillis();

		Map<String, java.util.List<String>> dependentMap = tSort.dependents;

		Set<String> allchildren = new HashSet<String>();

		for (java.util.List<String> s : dependentMap.values()) {
			allchildren.addAll(s);
		}

		strNodeRelation = new HashMap<String, Node>();
		Set<String> parents = new HashSet<String>();

		for (Entry<String, java.util.List<String>> queryEntry : dependentMap
				.entrySet()) {
			String filledQuery = queryEntry.getKey();
			Node temp = makeNode(filledQuery, queryEntry.getValue(), baseproj,
					newproj);
			hashNode.put("[" + temp.getName() + "]", temp);
			allNodes.put(filledQuery, temp);
			System.out.println(filledQuery);
			strNodeRelation.put(filledQuery, temp);

			nodeList.add(temp);
			viewer.add(temp.getName());
			parents.add(filledQuery);

		}

		// Print Summary Data
		System.out.println("\nTotal time for inference(ms): "
				+ (aftertime - beforetime));
		Set<lsclipse.Node> nodes = tSort.getGraph();
		int totalCount = 0;
		for (lsclipse.Node node : nodes) {
			if (node.numFound() > 0) {
				totalCount += node.numFound();
				System.out.print(node.toString() + ", ");
			}
		}
		System.out.println("\nFor a total of " + totalCount
				+ " refactorings found.");
	}

	private String getName(String filledQuery) {
		int parenthIndex = filledQuery.indexOf('(');
		return filledQuery.substring(0, parenthIndex);
	}

	private Node makeNode(String filledQuery, java.util.List<String> children,
			IProject baseProject, IProject newProject) {
		String name = getName(filledQuery);
		String nicename = name.replace('_', ' ');
		nicename = nicename.substring(0, 1).toUpperCase()
				+ nicename.substring(1);
		int nameIndex = filledQuery.indexOf(name);

		Node temp = new Node(nicename, null);
		temp.setDependents(children);
		temp.setFile("test.java.txt");// ?
		temp.setProjectName("LSclipse");// ?
		temp.params = filledQuery.substring(nameIndex + name.length());
		if (temp.params.length() > 4) {
			String[] params = temp.params
					.substring(2, temp.params.length() - 2).split("\",\"");
			ArrayList<String> fields = new ArrayList<String>();
			ArrayList<String> methods = new ArrayList<String>();
			ArrayList<String> classes = new ArrayList<String>();
			// guess what kind of parameter this is
			for (String s : params) {
				if (s.contains("{")
						|| s.contains("}")
						|| s.contains(";")
						|| s.contains("=")
						|| !s.contains("%")
						|| (s.contains("(") && (s.indexOf("(") < s.indexOf("%")))) { // looks
																						// like
																						// abody
					// body! ignore
				} else if (s.contains("(") && s.contains(")")) { // looks like
																	// method
					methods.add(s);
				} else if (s.contains("#")) { // looks like field
					fields.add(s);
				} else if (s.contains("%.")) { // looks like class
					classes.add(s);
				} else { // may be package or body
					// ignore
				}
			}
			for (String s : methods) {
				int indhex = s.indexOf("#");
				String qualifiedClassName = s.substring(0, indhex);
				temp.oldFacts.put(qualifiedClassName, LSDiffRunner
						.getOldTypeToFileMap().get(qualifiedClassName));
				temp.newFacts.put(qualifiedClassName, LSDiffRunner
						.getNewTypeToFileMap().get(qualifiedClassName));
			}
			for (String s : fields) {
				int indhex = s.indexOf("#");
				String qualifiedClassName = s.substring(0, indhex);
				temp.oldFacts.put(qualifiedClassName, LSDiffRunner
						.getOldTypeToFileMap().get(qualifiedClassName));
				temp.newFacts.put(qualifiedClassName, LSDiffRunner
						.getNewTypeToFileMap().get(qualifiedClassName));
			}
			for (String s : classes) {
				temp.oldFacts.put(s, LSDiffRunner.getOldTypeToFileMap().get(s));
				temp.newFacts.put(s, LSDiffRunner.getNewTypeToFileMap().get(s));
			}
		}
		return temp;
	}

	private void hookDoubleClickAction() {
		viewer.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent arg0) {
				doubleClickTreeAction.run();
			}

			public void mouseDown(MouseEvent arg0) {
			}

			public void mouseUp(MouseEvent arg0) {
			}
		});

		list.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent arg0) {
				doubleClickListAction.run();
			}

			public void mouseDown(MouseEvent arg0) {
			}

			public void mouseUp(MouseEvent arg0) {
			}
		});
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
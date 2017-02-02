package lsclipse.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class SelectProjectDialog extends Dialog {

	private String proj1 = "";
	private String proj2 = "";

	private Combo cmbProj1;
	private Combo cmbProj2;

	public SelectProjectDialog(Shell parentShell) {
		super(parentShell);
	}

	public String getProj1() {
		return proj1;
	}

	public String getProj2() {
		return proj2;
	}

	public void okPressed() {
		proj1 = cmbProj1.getText();
		proj2 = cmbProj2.getText();

		super.okPressed();
	}

	protected Control createDialogArea(Composite parent) {
		this.getShell().setText("Select Versions");

		// overall layout
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		parent.setLayout(layout);

		// declare some layouts
		GridData ldtDefault = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		ldtDefault.grabExcessHorizontalSpace = true;
		ldtDefault.grabExcessVerticalSpace = true;
		ldtDefault.horizontalAlignment = GridData.FILL;
		ldtDefault.verticalAlignment = GridData.FILL;
		ldtDefault.exclude = false;

		GridLayout panelLayout = new GridLayout();
		panelLayout.numColumns = 1;

		Composite leftPanel = new Composite(parent, 0);
		leftPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.FILL_VERTICAL | GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL));
		leftPanel.setLayout(panelLayout);

		Label base = new Label(leftPanel, 0);
		base.setText("Base Version:");

		// Project1 dropdown
		cmbProj1 = new Combo(leftPanel, SWT.READ_ONLY);
		cmbProj1.setLayoutData(ldtDefault);

		Label changed = new Label(leftPanel, 0);
		changed.setText("Changed Version:");

		// Diff options
		cmbProj2 = new Combo(leftPanel, SWT.READ_ONLY);
		cmbProj2.setLayoutData(ldtDefault);

		// Populate the combo boxes
		for (IProject proj : ResourcesPlugin.getWorkspace().getRoot()
				.getProjects()) {
			cmbProj1.add(proj.getName());
			cmbProj2.add(proj.getName());
		}

		return parent;
	}
}

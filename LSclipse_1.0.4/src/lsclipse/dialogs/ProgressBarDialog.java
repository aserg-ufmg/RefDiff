package lsclipse.dialogs;

import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ProgressBarDialog {

	private Shell shell;
	private Label lblStep;
	private Label lblMessage;
	private ProgressBar pbProg;
	private Text txtLog;
	
	public ProgressBarDialog(Shell parentShell, 
								String baseproj,
								int fetchmethod,	//0-manual, 1-workspace, 2-svn
								String proj2, 		//workspace
								String svnurl,		//svn
								int svnversion,	
								Set<String> changedFiles) {
		createDialogArea(parentShell);
	}
	public ProgressBarDialog(Shell parentShell) {
		shell = new Shell(parentShell, SWT.TITLE | SWT.MODELESS | SWT.CENTER);
		shell.setSize(520, 400);
        shell.setText("Running LSDiff...");
        createDialogArea(shell);
	}
	public synchronized void open() {
		shell.open();
	}
	public synchronized void dispose() {
		shell.dispose();
	}
	public synchronized void setStep(int phaseid) {
		final String[] phases = { "Preparation", 
			"Extract FB1: Base project facts",
			"Extract FB2: New project facts",
			"Compute Difference",
			"Perform LSDiff",
			"Cleanup"};
		if (phaseid<0 || phaseid>=phases.length) return;
		String step = "Step "+(phaseid+1)+" / "+phases.length+" : "+phases[phaseid] + "\n";
		try {
		lblStep.setText(step);
		} catch (Throwable t) {
			System.out.println(t.getMessage());
		}
		txtLog.append(step);
	}
	public synchronized void setMessage(String msg) {
		lblMessage.setText(msg);
		txtLog.append(msg);
	}
	public synchronized void appendLog(String log) {
		if (txtLog.getLineCount() > 300) {
			txtLog.getText().substring(1000);
		}
		txtLog.append(log);
		System.out.print(log);
	}
	public void appendError(String err) {
		txtLog.append(err);
		System.out.print(err);
	}
	public void setProgressMaxValue(int maxvalue) {
		this.pbProg.setMaximum(maxvalue);
	}
	public void setProgressCurrValue(int currvalue) {
		this.pbProg.setSelection(currvalue);
	}
	private Control createDialogArea(Composite shell) {
        GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		shell.setLayout(layout);

		lblStep = new Label(shell, SWT.NONE);
		lblStep.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		lblStep.setText("Step 1 / 999");

		lblMessage = new Label(shell, SWT.NONE);
		lblMessage.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		lblMessage.setText("Idle");

		pbProg = new ProgressBar(shell, SWT.SMOOTH | SWT.INDETERMINATE);
		pbProg.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		pbProg.setMaximum(1000);
		pbProg.setSelection(0);
		pbProg.setSelection(256);

		final Label lblSeparator = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		lblSeparator.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

		txtLog = new Text(shell, SWT.MULTI
		          | SWT.BORDER
		          | SWT.H_SCROLL
		          | SWT.V_SCROLL);
		txtLog.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		txtLog.setEditable(false);
		txtLog.setBackground(new Color(shell.getDisplay(), 10,10,10));
		txtLog.setForeground(new Color(shell.getDisplay(), 200,200,200));

		shell.layout();

		return shell;
	}
}

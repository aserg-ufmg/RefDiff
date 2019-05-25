package refdiff.parsers.java;

import org.eclipse.jdt.core.dom.MethodInvocation;

import refdiff.core.cst.CstNode;

public class MethodInvocationToResolve {
	private final CstNode caller;
	private final MethodInvocation invocation;
	
	public MethodInvocationToResolve(CstNode caller, MethodInvocation invocation) {
		this.caller = caller;
		this.invocation = invocation;
	}
	
	public CstNode getCaller() {
		return caller;
	}
	
	public MethodInvocation getInvocation() {
		return invocation;
	}
	
}

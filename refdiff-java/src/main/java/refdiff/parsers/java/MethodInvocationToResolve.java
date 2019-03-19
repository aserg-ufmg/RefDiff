package refdiff.parsers.java;

import org.eclipse.jdt.core.dom.MethodInvocation;

import refdiff.core.rast.RastNode;

public class MethodInvocationToResolve {
	private final RastNode caller;
	private final MethodInvocation invocation;
	
	public MethodInvocationToResolve(RastNode caller, MethodInvocation invocation) {
		this.caller = caller;
		this.invocation = invocation;
	}
	
	public RastNode getCaller() {
		return caller;
	}
	
	public MethodInvocation getInvocation() {
		return invocation;
	}
	
}

package refdiff.parsers.java;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;

import refdiff.core.rast.RastNode;

public class BindingResolver {

	public Map<RastNode, List<String>> getReferencesMap() {
		return null;
	}

	public Map<RastNode, List<String>> getSupertypesMap() {
		return null;
	}

	public void addSupertypeToResolve(RastNode type, Type superType) {
		
	}

	public void addMethodInvocationToResolve(RastNode method, MethodInvocation methodInvocation) {
		
	}

}

package refdiff.parsers.java;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;

import refdiff.core.cst.CstNode;

public class BindingResolver {
	
	private Map<String, Set<MethodInvocationToResolve>> invokedByIndex = new HashMap<>();
	private Map<String, Set<CstNode>> supertypeOfIndex = new HashMap<>();
	
	public void addSupertypeToResolve(CstNode type, Type superType) {
		String name = null;
		if (superType.isSimpleType()) {
			SimpleType simpleType = (SimpleType) superType;
			name = simpleType.getName().getFullyQualifiedName();
		} else if (superType.isNameQualifiedType()) {
			NameQualifiedType simpleType = (NameQualifiedType) superType;
			name = simpleType.getName().getFullyQualifiedName();
		} else if (superType.isQualifiedType()) {
			QualifiedType simpleType = (QualifiedType) superType;
			name = simpleType.getName().getFullyQualifiedName();
		}
		if (name != null) {
			supertypeOfIndex.computeIfAbsent(name, key -> new HashSet<>()).add(type);
		}
	}
	
	public void addMethodInvocationToResolve(CstNode method, MethodInvocation methodInvocation) {
		String methodName = methodInvocation.getName().getIdentifier();
		invokedByIndex.computeIfAbsent(methodName, key -> new HashSet<>()).add(new MethodInvocationToResolve(method, methodInvocation));
	}
	
	public void resolveBindings(SDModel model) {
		model.getRoot().forEachNode((node, depth) -> {
			if (node.getType().equals(NodeTypes.METHOD_DECLARATION)) {
				Set<MethodInvocationToResolve> invokers = invokedByIndex.getOrDefault(node.getSimpleName(), Collections.emptySet());
				for (MethodInvocationToResolve invoker : invokers) {
					if (isCompatible(invoker, node)) {
						model.addReference(invoker.getCaller(), node);
					}
				}
			} else if (node.getType().equals(NodeTypes.CLASS_DECLARATION) || node.getType().equals(NodeTypes.INTERFACE_DECLARATION)) {
				Set<CstNode> subtypes = supertypeOfIndex.getOrDefault(node.getSimpleName(), Collections.emptySet());
				for (CstNode subtype : subtypes) {
					model.addSubtype(node, subtype);
				}
			}
		});
	}

	private boolean isCompatible(MethodInvocationToResolve invocation, CstNode node) {
		MethodInvocation invocationNode = invocation.getInvocation();
		List arguments = invocationNode.arguments();
		return arguments.size() == node.getParameters().size();
	}
}

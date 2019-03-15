package refdiff.parsers.java;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;

import refdiff.core.rast.RastNode;

public class BindingResolver {
	
	private Map<String, Set<RastNode>> invokedByIndex = new HashMap<>();
	private Map<String, Set<RastNode>> supertypeOfIndex = new HashMap<>();
	
	public void addSupertypeToResolve(RastNode type, Type superType) {
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
	
	public void addMethodInvocationToResolve(RastNode method, MethodInvocation methodInvocation) {
		String methodName = methodInvocation.getName().getIdentifier();
		invokedByIndex.computeIfAbsent(methodName, key -> new HashSet<>()).add(method);
	}
	
	public void resolveBindings(SDModel model) {
		model.getRoot().forEachNode((node, depth) -> {
			if (node.getType().equals(NodeTypes.METHOD_DECLARATION)) {
				Set<RastNode> invokers = invokedByIndex.getOrDefault(node.getSimpleName(), Collections.emptySet());
				for (RastNode invoker : invokers) {
					model.addReference(invoker, node);
				}
			} else if (node.getType().equals(NodeTypes.CLASS_DECLARATION) || node.getType().equals(NodeTypes.INTERFACE_DECLARATION)) {
				Set<RastNode> subtypes = supertypeOfIndex.getOrDefault(node.getSimpleName(), Collections.emptySet());
				for (RastNode subtype : subtypes) {
					model.addSubtype(node, subtype);
				}
			}
		});
	}
}

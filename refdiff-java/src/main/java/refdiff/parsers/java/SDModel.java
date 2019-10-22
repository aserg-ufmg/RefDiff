package refdiff.parsers.java;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import refdiff.core.cst.HasChildrenNodes;
import refdiff.core.cst.Location;
import refdiff.core.cst.Parameter;
import refdiff.core.cst.CstNode;
import refdiff.core.cst.CstNodeRelationship;
import refdiff.core.cst.CstNodeRelationshipType;
import refdiff.core.cst.CstRoot;
import refdiff.core.cst.Stereotype;

public class SDModel {

	private int nodeCounter = 0;
	private CstRoot root = new CstRoot();
	private Map<String, CstNode> keyMap = new HashMap<>();
	
	public Optional<CstNode> findByKey(String referencedKey) {
		return Optional.ofNullable(keyMap.get(referencedKey));
	}

	public void addReference(CstNode caller, CstNode calleeNode) {
		root.getRelationships().add(new CstNodeRelationship(CstNodeRelationshipType.USE, caller.getId(), calleeNode.getId()));
	}

	public void addSubtype(CstNode supertype, CstNode type) {
		root.getRelationships().add(new CstNodeRelationship(CstNodeRelationshipType.SUBTYPE, type.getId(), supertype.getId()));
	}

	public CstNode createInnerType(String typeName, HasChildrenNodes parent, String sourceFilePath, CharSequence fileContent, AbstractTypeDeclaration ast, String nodeType) {
		return createType(typeName, "", parent, sourceFilePath, fileContent, ast, nodeType);
	}
	
	public CstNode createType(String typeName, String packageName, HasChildrenNodes parent, String sourceFilePath, CharSequence fileContent, AbstractTypeDeclaration ast, String nodeType) {
		if (typeName == null || typeName.isEmpty()) {
			throw new RuntimeException("Type should have a name");
		}
		String namespace = packageName.isEmpty() ? "" : packageName + "."; 
		CstNode cstNode = new CstNode(++nodeCounter);
		cstNode.setType(nodeType);
		cstNode.setLocation(Location.of(sourceFilePath, ast.getStartPosition(), ast.getStartPosition() + ast.getLength(), ast.getStartPosition(), ast.getStartPosition() + ast.getLength(), fileContent));
		cstNode.setLocalName(typeName);
		cstNode.setSimpleName(typeName);
		cstNode.setNamespace(namespace);
		parent.addNode(cstNode);
		ITypeBinding binding = ast.resolveBinding();
		if (binding != null) {
			keyMap.put(binding.getKey(), cstNode);
		}
		return cstNode;
	}

	public CstNode createMethod(String methodSignature, HasChildrenNodes parent, String sourceFilePath, CharSequence fileContent, boolean constructor, MethodDeclaration ast) {
		String methodName = ast.isConstructor() ? "new" : ast.getName().getIdentifier();
		CstNode cstNode = new CstNode(++nodeCounter);
		cstNode.setType(ast.getClass().getSimpleName());
		if (constructor) {
			cstNode.addStereotypes(Stereotype.TYPE_CONSTRUCTOR);
		}
		Block body = ast.getBody();
		int bodyStart;
		int bodyLength;
        if (body == null) {
            cstNode.addStereotypes(Stereotype.ABSTRACT);
            bodyStart = ast.getStartPosition() + ast.getLength();
            bodyLength = 0;
        } else {
        	// Remove open and close brackets
        	bodyStart = body.getStartPosition() + 1;
        	bodyLength = body.getLength() - 2;
        }
        cstNode.setLocation(Location.of(sourceFilePath, ast.getStartPosition(), ast.getStartPosition() + ast.getLength(), bodyStart, bodyStart + bodyLength, fileContent));
		cstNode.setLocalName(methodSignature);
		cstNode.setSimpleName(methodName);
		parent.addNode(cstNode);
		IMethodBinding binding = ast.resolveBinding();
		if (binding != null) {
			keyMap.put(binding.getKey(), cstNode);
		}
		return cstNode;
	}

	public void setReturnType(CstNode method, String normalizeTypeName) {
		// TODO Auto-generated method stub
		
	}

	public void addParameter(CstNode method, String identifier, String typeName) {
		method.getParameters().add(new Parameter(identifier));
	}

	public CstRoot getRoot() {
		return root;
	}
	
}

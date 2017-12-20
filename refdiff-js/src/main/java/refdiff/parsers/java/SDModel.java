package refdiff.parsers.java;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import refdiff.core.rast.HasChildrenNodes;
import refdiff.core.rast.Location;
import refdiff.core.rast.RastNode;
import refdiff.core.rast.RastNodeRelationship;
import refdiff.core.rast.RastNodeRelationshipType;
import refdiff.core.rast.RastRoot;
import refdiff.core.rast.Stereotype;

public class SDModel {

	private int nodeCounter = 0;
	private RastRoot root = new RastRoot();
	private Map<String, RastNode> keyMap = new HashMap<>();
	
	public Optional<RastNode> findByKey(String referencedKey) {
		return Optional.ofNullable(keyMap.get(referencedKey));
	}

	public void addReference(RastNode caller, RastNode calleeNode) {
		root.getRelationships().add(new RastNodeRelationship(RastNodeRelationshipType.USE, caller.getId(), calleeNode.getId()));
	}

	public void addSubtype(RastNode supertype, RastNode type) {
		root.getRelationships().add(new RastNodeRelationship(RastNodeRelationshipType.SUBTYPE, type.getId(), supertype.getId()));
	}

//	public RastNode createCompilationUnit(String packageName, String sourceFolder, String sourceFilePath, CompilationUnit compilationUnit) {
//		RastNode rastNode = new RastNode(++nodeCounter);
//		rastNode.setType("CompilationUnit");
//		rastNode.setLocation(new Location(sourceFilePath, 0, compilationUnit.getLength()));
//		rastNode.setLocalName(sourceFilePath);
//		root.getNodes().add(rastNode);
//		return rastNode;
//	}

//	public RastNode createAnonymousType(HasChildrenNodes parent, String sourceFilePath, String name, ASTNode ast) {
//		RastNode rastNode = new RastNode(++nodeCounter);
//		rastNode.setType(ast.getClass().getSimpleName());
//		rastNode.setLocation(new Location(sourceFilePath, ast.getStartPosition(), ast.getStartPosition() + ast.getLength()));
//		rastNode.setLocalName(name);
//		rastNode.setSimpleName("");
//		parent.addNode(rastNode);
//		keyMap.put(JavaParser.getKey(rastNode), rastNode);
//		return rastNode;
//	}

	public RastNode createInnerType(String typeName, HasChildrenNodes parent, String sourceFilePath, AbstractTypeDeclaration ast, String nodeType) {
		return createType(typeName, "", parent, sourceFilePath, ast, nodeType);
	}
	
	public RastNode createType(String typeName, String packageName, HasChildrenNodes parent, String sourceFilePath, AbstractTypeDeclaration ast, String nodeType) {
		if (typeName == null || typeName.isEmpty()) {
			throw new RuntimeException("Type should have a name");
		}
		String namespace = packageName.isEmpty() ? "" : packageName + "."; 
		RastNode rastNode = new RastNode(++nodeCounter);
		rastNode.setType(nodeType);
		rastNode.setLocation(new Location(sourceFilePath, ast.getStartPosition(), ast.getStartPosition() + ast.getLength()));
		rastNode.setLocalName(typeName);
		rastNode.setSimpleName(typeName);
		rastNode.setNamespace(namespace);
		parent.addNode(rastNode);
		keyMap.put(JavaParser.getKey(rastNode), rastNode);
		return rastNode;
	}

	public RastNode createMethod(String methodSignature, HasChildrenNodes parent, String sourceFilePath, boolean constructor, MethodDeclaration ast) {
		String methodName = ast.isConstructor() ? "" : ast.getName().getIdentifier();
		RastNode rastNode = new RastNode(++nodeCounter);
		rastNode.setType(ast.getClass().getSimpleName());
		
		Block body = ast.getBody();
		int bodyStart;
		int bodyLength;
        if (body == null) {
            rastNode.addStereotypes(Stereotype.ABSTRACT);
            bodyStart = ast.getStartPosition() + ast.getLength();
            bodyLength = 0;
        } else {
        	bodyStart = body.getStartPosition();
            bodyLength = body.getLength();
        }
        rastNode.setLocation(new Location(sourceFilePath, ast.getStartPosition(), ast.getStartPosition() + ast.getLength(), bodyStart, bodyStart + bodyLength));
		rastNode.setLocalName(methodSignature);
		rastNode.setSimpleName(methodName);
		parent.addNode(rastNode);
		keyMap.put(JavaParser.getKey(rastNode), rastNode);
		return rastNode;
	}

	public void setReturnType(RastNode method, String normalizeTypeName) {
		// TODO Auto-generated method stub
		
	}

	public void addParameter(RastNode method, String identifier, String typeName) {
		// TODO Auto-generated method stub
		
	}

	public RastRoot getRoot() {
		return root;
	}
	
}

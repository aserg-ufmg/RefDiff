package refdiff.parsers.java;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import refdiff.core.cst.HasChildrenNodes;
import refdiff.core.cst.CstNode;
import refdiff.core.cst.Stereotype;

public class BindingsRecoveryAstVisitor extends ASTVisitor {
	
	private final SDModel model;
	private final String sourceFilePath;
	private final CharSequence fileContent;
	private final String packageName;
	private final LinkedList<HasChildrenNodes> containerStack;
	private final Map<CstNode, List<String>> postProcessReferences;
	private final Map<CstNode, List<String>> postProcessSupertypes;
	
	public BindingsRecoveryAstVisitor(SDModel model, CompilationUnit compilationUnit, String sourceFilePath, char[] fileContent, String packageName, Map<CstNode, List<String>> postProcessReferences, Map<CstNode, List<String>> postProcessSupertypes) {
		this.model = model;
		this.sourceFilePath = sourceFilePath;
		this.fileContent = CharBuffer.wrap(fileContent);
		this.packageName = packageName;
		this.containerStack = new LinkedList<HasChildrenNodes>();
		this.containerStack.push(model.getRoot());
		this.postProcessReferences = postProcessReferences;
		this.postProcessSupertypes = postProcessSupertypes;
	}
	
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		return false;
	}
	
	/*
	 * @Override public boolean visit(AnonymousClassDeclaration node) { if (node.getParent() instanceof ClassInstanceCreation) { ClassInstanceCreation parent = (ClassInstanceCreation)
	 * node.getParent(); ITypeBinding typeBinding = parent.getType().resolveBinding(); if (typeBinding != null && typeBinding.isFromSource()) { int line =
	 * compilationUnit.getLineNumber(node.getStartPosition()); CstNode type = model.createAnonymousType(containerStack.peek(), sourceFilePath, "$" + line, node); containerStack.push(type);
	 * extractSupertypesForPostProcessing(type, typeBinding); return true; } } return false; }
	 * 
	 * @Override public void endVisit(AnonymousClassDeclaration node) { if (node.getParent() instanceof ClassInstanceCreation) { ClassInstanceCreation parent = (ClassInstanceCreation)
	 * node.getParent(); ITypeBinding typeBinding = parent.getType().resolveBinding(); if (typeBinding != null && typeBinding.isFromSource()) { containerStack.pop(); } } }
	 */
	
	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(EnumDeclaration node) {
		containerStack.push(visitTypeDeclaration(node, node.superInterfaceTypes(), NodeTypes.ENUM_DECLARATION));
		return true;
	}
	
	public void endVisit(EnumDeclaration node) {
		containerStack.pop();
	}
	
	@SuppressWarnings("unchecked")
	public boolean visit(TypeDeclaration typeDeclaration) {
		List<Type> supertypes = new ArrayList<Type>();
		Type superclass = typeDeclaration.getSuperclassType();
		if (superclass != null) {
			supertypes.add(superclass);
		}
		supertypes.addAll(typeDeclaration.superInterfaceTypes());
		CstNode sdType = visitTypeDeclaration(typeDeclaration, supertypes, typeDeclaration.isInterface() ? NodeTypes.INTERFACE_DECLARATION : NodeTypes.CLASS_DECLARATION);
		containerStack.push(sdType);
		if (typeDeclaration.isInterface()) {
			sdType.addStereotypes(Stereotype.ABSTRACT);
		}
		return true;
	}
	
	public void endVisit(TypeDeclaration node) {
		containerStack.pop();
	}
	
	private CstNode visitTypeDeclaration(AbstractTypeDeclaration node, List<Type> supertypes, String nodeType) {
		CstNode type;
		String typeName = node.getName().getIdentifier();
		if (node.isPackageMemberTypeDeclaration()) {
			type = model.createType(typeName, packageName, containerStack.peek(), sourceFilePath, fileContent, node, nodeType);
		} else {
			type = model.createInnerType(typeName, containerStack.peek(), sourceFilePath, fileContent, node, nodeType);
		}
		
		Set<String> annotations = extractAnnotationTypes(node.modifiers());
		if (annotations.contains("Deprecated")) {
			type.addStereotypes(Stereotype.DEPRECATED);
		}
		
		for (Type superType : supertypes) {
			ITypeBinding superTypeBinding = superType.resolveBinding();
			extractSupertypesForPostProcessing(type, superTypeBinding);
		}
		
		// final List<String> references = new ArrayList<String>();
		// node.accept(new DependenciesAstVisitor(true) {
		// @Override
		// protected void onTypeAccess(ASTNode node, ITypeBinding binding) {
		// String typeKey = AstUtils.getKeyFromTypeBinding(binding);
		// references.add(typeKey);
		// }
		// });
		// postProcessReferences.put(type, references);
		
		return type;
	}
	
	private void extractSupertypesForPostProcessing(CstNode type, ITypeBinding superTypeBinding) {
		List<String> supertypes = postProcessSupertypes.get(type);
		if (supertypes == null) {
			supertypes = new ArrayList<String>();
			postProcessSupertypes.put(type, supertypes);
		}
		while (superTypeBinding != null && superTypeBinding.isFromSource()) {
			String superTypeName = superTypeBinding.getErasure().getKey();
			supertypes.add(superTypeName);
			superTypeBinding = superTypeBinding.getSuperclass();
		}
	}
	
	public boolean visit(MethodDeclaration methodDeclaration) {
		// ASTNode parentNode = methodDeclaration.getParent();
		// if (!(parentNode instanceof TypeDeclaration)) {
		// // ignore methods from anonymous classes
		// return false;
		// }
		String methodSignature = AstUtils.getSignatureFromMethodDeclaration(methodDeclaration);
		// if
		// (methodDeclaration.getName().getIdentifier().equals("execMultiLineCommands"))
		// {
		// System.out.println("x");
		//
		// }
		
		final CstNode method = model.createMethod(methodSignature, containerStack.peek(), sourceFilePath, fileContent, methodDeclaration.isConstructor(), methodDeclaration);
		
		List<?> modifiers = methodDeclaration.modifiers();
		Set<String> annotations = extractAnnotationTypes(modifiers);
		boolean deprecated = annotations.contains("Deprecated") || AstUtils.containsDeprecatedTag(methodDeclaration.getJavadoc());
		if (deprecated) {
			method.addStereotypes(Stereotype.DEPRECATED);
		}
		
		// int methodModifiers = methodDeclaration.getModifiers();
		// Visibility visibility = getVisibility(methodModifiers);
		// boolean isStatic = (methodModifiers & Modifier.STATIC) != 0;
		
		if (!methodDeclaration.isConstructor()) {
			method.addStereotypes(Stereotype.TYPE_MEMBER);
		}
		
		// method.setVisibility(visibility);
		extractParametersAndReturnType(model, methodDeclaration, method);
		
		if (AstUtils.isGetter(methodDeclaration)) {
			method.addStereotypes(Stereotype.FIELD_ACCESSOR);
		} else if (AstUtils.isSetter(methodDeclaration)) {
			method.addStereotypes(Stereotype.FIELD_MUTATOR);
		}
		
		Block body = methodDeclaration.getBody();
		if (body == null) {
			method.addStereotypes(Stereotype.ABSTRACT);
		} else {
			final List<String> references = new ArrayList<String>();
			body.accept(new DependenciesAstVisitor(true) {
				@Override
				protected void onMethodAccess(ASTNode node, IMethodBinding binding) {
					String methodKey = binding.getKey();
					references.add(methodKey);
				}
				
			});
			postProcessReferences.put(method, references);
		}
		
		return true;
	}
	
	@SuppressWarnings("unused")
	private Visibility getVisibility(int methodModifiers) {
		Visibility visibility;
		if ((methodModifiers & Modifier.PUBLIC) != 0)
			visibility = Visibility.PUBLIC;
		else if ((methodModifiers & Modifier.PROTECTED) != 0)
			visibility = Visibility.PROTECTED;
		else if ((methodModifiers & Modifier.PRIVATE) != 0)
			visibility = Visibility.PRIVATE;
		else
			visibility = Visibility.PACKAGE;
		return visibility;
	}
	
	private static Set<String> extractAnnotationTypes(List<?> modifiers) {
		Set<String> annotations = new HashSet<String>();
		for (Object modifier : modifiers) {
			if (modifier instanceof Annotation) {
				Annotation a = (Annotation) modifier;
				annotations.add(a.getTypeName().toString());
			}
		}
		return annotations;
	}
	
	@SuppressWarnings("unchecked")
	public static void extractParametersAndReturnType(SDModel model, MethodDeclaration methodDeclaration, CstNode method) {
		Type returnType = methodDeclaration.getReturnType2();
		if (returnType != null) {
			model.setReturnType(method, AstUtils.normalizeTypeName(returnType, methodDeclaration.getExtraDimensions(), false));
		} else {
			model.setReturnType(method, null);
		}
		Iterator<SingleVariableDeclaration> parameters = methodDeclaration.parameters().iterator();
		while (parameters.hasNext()) {
			SingleVariableDeclaration parameter = parameters.next();
			Type parameterType = parameter.getType();
			String typeName = AstUtils.normalizeTypeName(parameterType, parameter.getExtraDimensions(), parameter.isVarargs());
			model.addParameter(method, parameter.getName().getIdentifier(), typeName);
		}
	}
	
}

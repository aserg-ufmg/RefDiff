package refdiff.parsers.java;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.Type;

public class AstUtils {
	
	private AstUtils() {
		//
	}
	
	public static String getKeyFromFieldBinding(IVariableBinding binding) {
		StringBuilder sb = new StringBuilder();
		String className = binding.getDeclaringClass().getErasure().getQualifiedName();
		sb.append(className);
		sb.append('.');
		sb.append(binding.getName());
		return sb.toString();
	}
	
	public static String getSignatureFromMethodDeclaration(MethodDeclaration methodDeclaration) {
		String methodName = methodDeclaration.isConstructor() ? "new" : methodDeclaration.getName().getIdentifier();
		// if (methodName.equals("allObjectsSorted")) {
		// System.out.println();
		// }
		StringBuilder sb = new StringBuilder();
		sb.append(methodName);
		sb.append('(');
		@SuppressWarnings("unchecked")
		Iterator<SingleVariableDeclaration> parameters = methodDeclaration.parameters().iterator();
		while (parameters.hasNext()) {
			SingleVariableDeclaration parameter = parameters.next();
			Type parameterType = parameter.getType();
			String typeName = normalizeTypeName(parameterType, parameter.getExtraDimensions(), parameter.isVarargs());
			sb.append(typeName);
			if (parameters.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append(')');
		String methodSignature = sb.toString();
		return methodSignature;
	}
	
	public static String normalizeTypeName(Type type, int extraDimensions, boolean varargs) {
		StringBuilder sb = new StringBuilder();
		// String rawTypeName = stripQualifiedTypeName(stripTypeParamsFromTypeName(type.toString()));
		String rawTypeName = stripTypeParamsFromTypeName(type.toString());
		sb.append(rawTypeName);
		for (int i = extraDimensions; i > 0; i--) {
			sb.append("[]");
		}
		if (varargs) {
			sb.append("[]");
		}
		return sb.toString();
	}
	
	private static String stripTypeParamsFromTypeName(String typeNameWithGenerics) {
		String rawTypeName = typeNameWithGenerics;
		int startOfTypeParams = typeNameWithGenerics.indexOf('<');
		if (startOfTypeParams >= 0) {
			rawTypeName = typeNameWithGenerics.substring(0, startOfTypeParams);
			int endOfTypeParams = typeNameWithGenerics.lastIndexOf('>');
			if (endOfTypeParams > startOfTypeParams && endOfTypeParams < typeNameWithGenerics.length() - 1) {
				rawTypeName = rawTypeName + typeNameWithGenerics.substring(endOfTypeParams + 1);
			}
		}
		return rawTypeName;
	}
	
	private static String stripTypeArguments(String entity) {
		StringBuilder sb = new StringBuilder();
		int openGenerics = 0;
		for (int i = 0; i < entity.length(); i++) {
			char c = entity.charAt(i);
			if (c == '<') {
				openGenerics++;
			}
			if (openGenerics == 0) {
				sb.append(c);
			}
			if (c == '>') {
				openGenerics--;
			}
		}
		return sb.toString();
	}
	
	private static String stripQualifiedTypeName(String qualifiedTypeName) {
		int dotPos = qualifiedTypeName.lastIndexOf('.');
		if (dotPos >= 0) {
			return qualifiedTypeName.substring(dotPos + 1);
		}
		return qualifiedTypeName;
	}
	
	public static String normalizeMethodSignature(String methodSignature) {
		StringBuilder sb = new StringBuilder();
		int start = 0;
		int openPar = methodSignature.indexOf('(');
		int closePar = methodSignature.lastIndexOf(')');
		if (openPar == -1 || closePar == -1) {
			throw new IllegalArgumentException("Invalid method signature: " + methodSignature);
		}
		int lastSpace = methodSignature.lastIndexOf(' ', openPar);
		if (lastSpace != -1) {
			start = lastSpace + 1;
		}
		sb.append(methodSignature, start, openPar);
		sb.append('(');
		
		String[] parameters;
		String parametersStr = stripTypeArguments(methodSignature.substring(openPar + 1, closePar));
		if (parametersStr.length() > 0) {
			parameters = parametersStr.split(" *, *");
		} else {
			parameters = new String[0];
		}
		for (int i = 0; i < parameters.length; i++) {
			String parameter = parameters[i];
			int space = parameter.lastIndexOf(' ');
			String parameterType;
			if (space == -1) {
				parameterType = parameter;
			} else {
				parameterType = parameter.substring(space + 1);
			}
			sb.append(stripQualifiedTypeName(parameterType));
			if (i < parameters.length - 1) {
				sb.append(',');
			}
		}
		sb.append(')');
		return sb.toString();
	}
	
	public static boolean containsDeprecatedTag(Javadoc javadoc) {
		if (javadoc == null) {
			return false;
		}
		@SuppressWarnings("unchecked")
		List<TagElement> javadocTags = (List<TagElement>) javadoc.tags();
		for (TagElement tag : javadocTags) {
			if ("@deprecated".equals(tag.getTagName())) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isGetter(MethodDeclaration methodDeclaration) {
		String identifier = methodDeclaration.getName().getIdentifier();
		if (!(identifier.startsWith("get") || identifier.startsWith("is"))) {
			return false;
		}
		if (!methodDeclaration.parameters().isEmpty()) {
			return false;
		}
		Block body = methodDeclaration.getBody();
		if (body != null) {
			List<?> statements = body.statements();
			if (statements.size() > 1) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean isSetter(MethodDeclaration methodDeclaration) {
		String identifier = methodDeclaration.getName().getIdentifier();
		if (!identifier.startsWith("set")) {
			return false;
		}
		if (methodDeclaration.parameters().size() != 1) {
			return false;
		}
		Block body = methodDeclaration.getBody();
		if (body != null) {
			List<?> statements = body.statements();
			if (statements.size() > 1) {
				return false;
			}
		}
		return true;
	}
	
}

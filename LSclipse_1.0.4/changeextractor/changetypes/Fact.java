package changetypes;

import java.util.Vector;

public class Fact {

	public enum FactTypes {
		//LSD facts
		PACKAGE,
		TYPE,
		METHOD,
		FIELD,
		RETURN,
		FIELDOFTYPE,
		ACCESSES,
		CALLS,
		SUBTYPE,
		EXTENDS,
		IMPLEMENTS,
		INHERITEDFIELD,
		INHERITEDMETHOD,
		TYPEINTYPE,
		//Non-LSD facts
		METHODBODY,		//(methodFullName, methodBody)
		METHODSIGNATURE,	//(methodFullName, methodSignature) Method args is encoded as "type1:arg1,type2:arg2,...->return"
		CONDITIONAL, //Niki's added code
		PARAMETER, // Niki's added code
		METHODMODIFIER, // Niki's added code\
		FIELDMODIFIER, //Niki's added code
		// Below are Kyle's edits
		CAST,
		TRYCATCH, 
		THROWN, 
		GETTER, 
		SETTER, 
		LOCALVAR
	}

	public final static String PRIVATE = "private"; 
	public final static String PROTECTED = "protected"; 
	public final static String PUBLIC = "public";
	public final static String PACKAGE = "package";
	public final static String INTERFACE = "interface";
	public final static String CLASS = "class";

	public FactTypes type;
	public Vector<String> params;
	public int params_length;

	//physical location of node in source file
	public String filename = "";
	public int startposition;
	public int length;

	private Fact(FactTypes mytype, Vector<String> myparams) {
		type = mytype;
		params = new Vector<String>(myparams);
	}

	public Fact(Fact f) {
		type = f.type;
		params = f.params;
	}

	public int hashCode() {
		return params.hashCode()+type.ordinal()*1000;
//		return type.ordinal();
	}

	public boolean equals(Object o) {
		if (o.getClass()!=this.getClass()) return false;
		Fact f = (Fact)o;
		if (!type.equals(f.type)) return false;
		for (int i=0; i<params.size(); ++i) {
			if (!params.get(i).equals(f.params.get(i)) &&
					!params.get(i).equals("*") && !f.params.get(i).equals("*"))
				return false;
		}
		return true;
	}

	public String toString() {
		StringBuilder res = new StringBuilder();
		res.append(type.toString());
		res.append("(");
		boolean first = true;
		for (String arg : params) {
			if (!first) res.append(", ");
			res.append(arg);
			first = false;
		}
		res.append(")");
		
		return res.toString();
	}

	//Niki's added code
	public static Fact makeModifierMethodFact(String mFullName, String modifier){
		Vector<String> params = new Vector<String>();
		params.add(mFullName);
		params.add(modifier);
		return new Fact(FactTypes.METHODMODIFIER, params);
	}
	public static Fact makeFieldModifierFact(String fFullName, String modifier){
		Vector<String> params = new Vector<String>();
		params.add(fFullName);
		params.add(modifier);
		return new Fact(FactTypes.FIELDMODIFIER, params);
	}


	public static Fact makeConditionalFact(String condition, String ifBlockName, String elseBlockName, String typeFullName){
		Vector<String> params = new Vector<String>();
		params.add(condition);
		params.add(ifBlockName);
		params.add(elseBlockName);
		params.add(typeFullName);
		return new Fact(FactTypes.CONDITIONAL, params);
	}
	
	public static Fact makeParameterFact(String methodFullName, String paramList, String paramChange){
		Vector<String> params = new Vector<String>();
		params.add(methodFullName);
		params.add(paramList);
		params.add(paramChange);
		return new Fact(FactTypes.PARAMETER, params);
	}
	// end of Niki's added code
	public static Fact makePackageFact(String packageFullName) {
		Vector<String> params = new Vector<String>();
		params.add(packageFullName);
		return new Fact(FactTypes.PACKAGE, params);
	}

	public static Fact makeTypeFact(String typeFullName, String typeShortName, String packageFullName, String typeKind) {
		Vector<String> params = new Vector<String>();
		params.add(typeFullName);
		params.add(typeShortName);
		params.add(packageFullName);
		params.add(typeKind);
		return new Fact(FactTypes.TYPE, params);
	}

	public static Fact makeFieldFact(String fieldFullName, String fieldShortName, String typeFullName, String visibility) {
		Vector<String> params = new Vector<String>();
		params.add(fieldFullName);
		params.add(fieldShortName);
		params.add(typeFullName);
		params.add(visibility);
		return new Fact(FactTypes.FIELD, params);
	}

	public static Fact makeMethodFact(String methodFullName, String methodShortName, String typeFullName, String visibility) {
		Vector<String> params = new Vector<String>();
		params.add(methodFullName);
		params.add(methodShortName);
		params.add(typeFullName);
		params.add(visibility);
		return new Fact(FactTypes.METHOD, params);
	}

	public static Fact makeFieldTypeFact(String fieldFullName, String declaredTypeFullName) {
		Vector<String> params = new Vector<String>();
		params.add(fieldFullName);
		params.add(declaredTypeFullName);
		return new Fact(FactTypes.FIELDOFTYPE, params);
	}

	public static Fact makeTypeInTypeFact(String innerTypeFullName, String outerTypeFullName) {
		Vector<String> params = new Vector<String>();
		params.add(innerTypeFullName);
		params.add(outerTypeFullName);
		return new Fact(FactTypes.TYPEINTYPE, params);
	}

	public static Fact makeReturnsFact(String methodFullName, String returnTypeFullName) {
		Vector<String> params = new Vector<String>();
		params.add(methodFullName);
		params.add(returnTypeFullName);
		return new Fact(FactTypes.RETURN, params);
	}

	public static Fact makeSubtypeFact(String superTypeFullName, String subTypeFullName) {
		Vector<String> params = new Vector<String>();
		params.add(superTypeFullName);
		params.add(subTypeFullName);
		return new Fact(FactTypes.SUBTYPE, params);
	}

	public static Fact makeImplementsFact(String superTypeFullName, String subTypeFullName) {
		Vector<String> params = new Vector<String>();
		params.add(superTypeFullName);
		params.add(subTypeFullName);
		return new Fact(FactTypes.IMPLEMENTS, params);
	}

	public static Fact makeExtendsFact(String superTypeFullName, String subTypeFullName) {
		Vector<String> params = new Vector<String>();
		params.add(superTypeFullName);
		params.add(subTypeFullName);
		return new Fact(FactTypes.EXTENDS, params);
	}

	public static Fact makeInheritedFieldFact(String fieldShortName, String superTypeFullName, String subTypeFullName) {
		Vector<String> params = new Vector<String>();
		params.add(fieldShortName);
		params.add(superTypeFullName);
		params.add(subTypeFullName);
		return new Fact(FactTypes.INHERITEDFIELD, params);
	}

	public static Fact makeInheritedMethodFact(String methodShortName, String superTypeFullName, String subTypeFullName) {
		Vector<String> params = new Vector<String>();
		params.add(methodShortName);
		params.add(superTypeFullName);
		params.add(subTypeFullName);
		return new Fact(FactTypes.INHERITEDMETHOD, params);
	}

	public static Fact makeMethodBodyFact(String methodFullName, String methodBody) {
		Vector<String> params = new Vector<String>();
		params.add(methodFullName);
		params.add(methodBody);
		return new Fact(FactTypes.METHODBODY, params);
	}

	public static Fact makeMethodArgsFact(String methodFullName, String methodSignature) {
		Vector<String> params = new Vector<String>();
		params.add(methodFullName);
		params.add(methodSignature);
		return new Fact(FactTypes.METHODSIGNATURE, params);
	}

	public static Fact makeCallsFact(String callerMethodFullName, String calleeMethodFullName) {
		Vector<String> params = new Vector<String>();
		params.add(callerMethodFullName);
		params.add(calleeMethodFullName);
		return new Fact(FactTypes.CALLS, params);
	}

	public static Fact makeAccessesFact(String fieldFullName, String accessorMethodFullName) {
		Vector<String> params = new Vector<String>();
		params.add(fieldFullName);
		params.add(accessorMethodFullName);
		return new Fact(FactTypes.ACCESSES, params);
	}
	
	public static Fact makeCastFact(String expression, String type, String methodName) {
		Vector<String> params = new Vector<String>();
		params.add(expression);
		params.add(type);
		params.add(methodName);
		return new Fact(FactTypes.CAST, params);
	}
	
	public static Fact makeTryCatchFact(String tryBlock, String catchClauses, String finallyBlock, String methodName) {
		Vector<String> params = new Vector<String>();
		params.add(tryBlock);
		params.add(catchClauses);
		params.add(finallyBlock);
		params.add(methodName);
		return new Fact(FactTypes.TRYCATCH, params);
	}

	public static Fact makeThrownExceptionFact(String methodQualifiedName,
			String exceptionQualifiedName) {
		Vector<String> params = new Vector<String>();
		params.add(methodQualifiedName);
		params.add(exceptionQualifiedName);
		return new Fact(FactTypes.THROWN, params);
	}

	public static Fact makeGetterFact(String methodQualifiedName,
			String fieldQualifiedName) {
		Vector<String> params = new Vector<String>();
		params.add(methodQualifiedName);
		params.add(fieldQualifiedName);
		return new Fact(FactTypes.GETTER, params);
	}

	public static Fact makeSetterFact(String methodQualifiedName,
			String fieldQualifiedName) {
		Vector<String> params = new Vector<String>();
		params.add(methodQualifiedName);
		params.add(fieldQualifiedName);
		return new Fact(FactTypes.SETTER, params);
	}

	public static Fact makeLocalVarFact(String methodQualifiedName,
			String typeQualifiedName, String identifier, String expression) {
		Vector<String> params = new Vector<String>();
		params.add(methodQualifiedName);
		params.add(typeQualifiedName);
		params.add(identifier);
		params.add(expression);
		return new Fact(FactTypes.LOCALVAR, params);
	}
}


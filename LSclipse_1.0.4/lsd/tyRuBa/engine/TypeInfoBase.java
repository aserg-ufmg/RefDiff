package tyRuBa.engine;

import java.util.HashMap;
import java.util.Iterator;

import tyRuBa.modes.CompositeType;
import tyRuBa.modes.ConstructorType;
import tyRuBa.modes.Factory;
import tyRuBa.modes.PredInfo;
import tyRuBa.modes.PredInfoProvider;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeConstructor;
import tyRuBa.modes.TypeMapping;
import tyRuBa.modes.TypeModeError;
import tyRuBa.modes.UserDefinedTypeConstructor;

/** 
 * A TypeInfoBase is a collection of predicate and type information.
 */
public class TypeInfoBase implements PredInfoProvider {

	/** 
	 * A database in which we will store facts that describe the type
	 * declarations so that user can write tyruba programs about their
	 * own types.
	 * 
	 * @codegroup metadata
	 */	
	MetaBase metaBase;
	
	HashMap predicateMap = new HashMap();
	HashMap typeConstructorMap = new HashMap();
	HashMap functorMap = new HashMap(); // map from constructor to type constructor
	HashMap toTyRuBaMappingMap = new HashMap();

	public TypeInfoBase(String identifier) {
		super();
		addTypeConst(TypeConstructor.theAny);
		metaBase = null;
	}

	/**
	 * After this methos is called, from this point forward in time, 
	 * metaData will be added to the metaBase
	 * 
	 * @codegroup metadata
	 */
	public void enableMetaData(QueryEngine qe) {
		metaBase = new MetaBase(qe);
		
		// Retroactively add facts about the types already declared.
		for (Iterator iter = typeConstructorMap.values().iterator(); iter.hasNext();) {
			metaBase.assertTypeConstructor((TypeConstructor) iter.next());
		}
	}
	
	public void insert(PredInfo pInfo) throws TypeModeError {
		PredInfo result = (PredInfo) predicateMap.get(pInfo.getPredId());
		if (result != null)
			throw new TypeModeError("Duplicate mode/type entries for predicate "
					+ pInfo.getPredId());
		predicateMap.put(pInfo.getPredId(), pInfo);
	}

	public PredInfo getPredInfo(PredicateIdentifier predId) throws TypeModeError {
		PredInfo result = maybeGetPredInfo(predId);
		if (result == null) {
			throw new TypeModeError("Unknown predicate " + predId);
		} else {
			return result;
		}
	}
	
	public PredInfo maybeGetPredInfo(PredicateIdentifier predId) {
		return (PredInfo) predicateMap.get(predId);
	}
		
	/**
	 * @codegroup metadata
	 */
	public void addTypeConst(TypeConstructor t) {
		typeConstructorMap.put(t.getName() + "/" + t.getTypeArity(), t);
		if (metaBase!=null) metaBase.assertTypeConstructor(t);
	}

	public void addFunctorConst(Type repAs, CompositeType type) {
		TypeConstructor tc = type.getTypeConstructor();
		FunctorIdentifier functorId = tc.getFunctorId();
		
		ConstructorType constrType = ConstructorType.makeUserDefined(functorId,repAs,type);
        functorMap.put(functorId, constrType);
		tc.setConstructorType(constrType);
	}

    public void addTypeMapping(FunctorIdentifier id, TypeMapping mapping) throws TypeModeError {
        TypeConstructor tc = findTypeConst(id.getName(),id.getArity());
		tc.getConstructorType();
		if (tc instanceof UserDefinedTypeConstructor)
			((UserDefinedTypeConstructor)tc).setMapping(mapping);
		else {
			throw new Error("The tyRuBa type "+id+" is not a mappable type. Only Userdefined types can be mapped.");
		}

        if (tc.hasRepresentation()) {
        		ConstructorType ct = tc.getConstructorType();
        		mapping.setFunctor(ct);
        }
		toTyRuBaMappingMap.put(mapping.getMappedClass(), mapping);
    }
	
	public String toString() {
		StringBuffer result = new StringBuffer(
			"/******** predicate info ********/\n");
		Iterator itr = predicateMap.values().iterator();
		while (itr.hasNext()) {
			PredInfo element = (PredInfo) itr.next();
			result.append(element.toString());
		}
		result.append("/******** user defined types ****/\n");
		itr = typeConstructorMap.values().iterator();
		while (itr.hasNext()) {
			result.append(itr.next() + "\n");
		}
		result.append("/********************************/\n");
		return result.toString();
	}
	
	public TypeConstructor findType(String typeName) {
		if (typeName.equals("String")
 		  ||typeName.equals("Integer")
		  ||typeName.equals("Number")
		  ||typeName.equals("Float"))
//		  ||typeName.equals("Object")) // see TypeConstructor.theAny
		    typeName = "java.lang."+typeName;
		if (typeName.equals("RegExp"))
			typeName = "org.apache.regexp.RE";
		TypeConstructor result =  (TypeConstructor)typeConstructorMap.get(typeName + /*arity*/ "/0"); 
		if (result==null) {
			if (typeName.indexOf('.')>=0) {
	            try {
	                Class cl = Class.forName(typeName);
	                result = Factory.makeTypeConstructor(cl);
	                addTypeConst(result);
	            } catch (ClassNotFoundException e1) {
	            	   // Yes, empty cathc block is intentional and OK here.
	            	   // Class.forName is called to attempt to find a Java
	            	   // class. This expected to fail sometimes.
	            }
	        }
		}
		return result;
	}
	
	public TypeConstructor findTypeConst(String typeName, int arity) {
		TypeConstructor result = (TypeConstructor) typeConstructorMap.get(typeName +"/" + arity);
		if (result == null) {
			result = Factory.makeTypeConstructor(typeName, arity);
			addTypeConst(result);
		}
		return result;
	}
	
	public ConstructorType findConstructorType(FunctorIdentifier id) {
		return (ConstructorType) functorMap.get(id);
	}
	
	public TypeMapping findTypeMapping(Class cls) {
	    return (TypeMapping) toTyRuBaMappingMap.get(cls);
	}
	
	public void clear() {
		predicateMap = new HashMap();
		typeConstructorMap = new HashMap();
	}

}

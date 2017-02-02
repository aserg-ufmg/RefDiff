package changetypes;

public interface IAtomicChange {

	public enum ChangeTypes {
		//LSD facts
		ADD_PACKAGE,
		ADD_TYPE,
		ADD_METHOD,
		ADD_FIELD,
		ADD_RETURN,
		ADD_FIELDOFTYPE,
		ADD_ACCESSES,
		ADD_CALLS,
		ADD_SUBTYPE,
		ADD_INHERITEDFIELD,
		ADD_INHERITEDMETHOD,
		ADD_PARAMETER, // Niki's edit
		
		DEL_PACKAGE,
		DEL_TYPE,
		DEL_METHOD,
		DEL_FIELD,
		DEL_RETURN,
		DEL_FIELDOFTYPE,
		DEL_ACCESSES,
		DEL_CALLS,
		DEL_SUBTYPE,
		DEL_INHERITEDFIELD,
		DEL_INHERITEDMETHOD,
		//Non-LSD facts
		CHANGE_METHODBODY,		//(methodFullName, methodBody)
		CHANGE_METHODSIGNATURE,	//(methodFullName, methodSignature) Method args is encoded as "type1:arg1,type2:arg2,...->return"
		ADD_CONDITIONAL, // Niki's edit
		DEL_CONDITIONAL, // Niki's edit
		DEL_PARAMETER // Niki's edit
	}

}

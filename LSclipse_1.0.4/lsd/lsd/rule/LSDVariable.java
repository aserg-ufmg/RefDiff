package lsd.rule;


public class LSDVariable {
	public static boolean isValidType(char type) {
//		Is type one of package, class, method, or field?
//		return "ptmf".contains(Character.toString(type));

		// Is type one of package, class, method, field, name of
		// type, name of field, or name of method?
		return "hieptmfabc".contains(Character.toString(type));
	}     
	/**
	 * @param args
	 */
	private char type; 
	private String variableName;
	public LSDVariable(String variableName, char type)
	{
		this.variableName = variableName;
		this.type = type;
	}
	
	public String getName() { return variableName;}
	
	public boolean typeChecks(char type) { 
		return (this.type==type);
	}
	public boolean typeChecks(LSDVariable match) { 
		return (this.type==match.type);
	}
	
	public boolean typeConflicts(LSDVariable toBeMatched) {
		return (this.variableName.equals(toBeMatched.variableName) &&
				!typeChecks(toBeMatched));
	}
	public String toString() {return "?"+variableName;}
	
	public boolean equals(LSDVariable other){ return this.variableName.equals(other.variableName) && this.type == other.type;} 
	public boolean equals(Object other) {
		if (other instanceof LSDVariable)
			return equals((LSDVariable) other);
		else
			return false;
	}
	@Override
	public int hashCode() {
		String identity = variableName + type;
		return identity.hashCode();
	}

	public int compareTo(Object o) {
		return o.hashCode() - this.hashCode();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}
	public char getType() { 
		// FIXME kinda violates information hiding. I need to know the type of variable in LSDBruteForceRuleEnumerator though.
		return type;
	}

}

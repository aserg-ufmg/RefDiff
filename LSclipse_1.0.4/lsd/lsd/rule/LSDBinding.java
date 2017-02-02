package lsd.rule;


public class LSDBinding implements Comparable<LSDBinding>{
	
	private char type;
	
	private LSDVariable variable;

	private String groundConst = null;

	public boolean isBound() {
		return (groundConst != null);
	}
	
	public String getGroundConst(){
		String temp = groundConst;
		if (temp != null)
			temp = temp.substring(1,temp.length()-1);
		return temp;
	}

	public boolean equals(Object other) {
		if (!(other instanceof LSDBinding))
			return false;
		LSDBinding otherBinding = (LSDBinding) other;
		if (groundConst == null || otherBinding.groundConst == null){
			if (groundConst != otherBinding.groundConst)
				return false;
		} else {
			if (!groundConst.equals(otherBinding.groundConst))
				return false;
		}
		if (variable == null || otherBinding.variable == null){
			if (variable != otherBinding.variable)
				return false;
		} else {
			if (!variable.equals(otherBinding.variable))
				return false;
		}
		return true;
	}
	
	public LSDBinding(LSDVariable var) {
		this.variable = var;
		this.type = var.getType();

	}

	public LSDBinding(String cst) {
		this.variable = null;
		this.groundConst = cst;
	}

	public void ground(String cst) {
		type = variable.getType();
		this.variable = null;
		this.groundConst = cst;
	}

	private LSDBinding(LSDBinding toCopyFrom) {
		this.variable = toCopyFrom.variable;
		this.type = toCopyFrom.type;
		this.groundConst = toCopyFrom.groundConst;

	}

	public boolean typeChecks(char type) {
		return (variable == null || variable.typeChecks(type));
	}

	public LSDBinding substitute(LSDVariable toReplace, LSDBinding replacement)
			throws LSDInvalidTypeException {
		// always create a new copy and then return the copy after substitution
		LSDBinding nb = new LSDBinding(this);
		if (this.variable == null)
			return nb;
		if (this.variable.equals(toReplace)) {
			if (!this.variable.typeChecks(toReplace))
				throw new LSDInvalidTypeException();
			if (replacement.variable != null
					&& !this.variable.typeChecks(replacement.variable))
				throw new LSDInvalidTypeException();
			nb.variable = replacement.variable;
			nb.type = variable.getType();
			nb.groundConst = replacement.groundConst;
		}
		return nb;
	}

	public LSDVariable getVariable() {
		return variable;
	}

	public String toString() {
		if (variable != null)
			return variable.toString();
		else
			return groundConst.toString();
	}

	public void replaceVar(LSDVariable newVar) {
		variable = newVar;
		type = newVar.getType();
		
	}
	public char getType() {
		return type;
	}

	public int compareTo(LSDBinding o) {
		// FIXME this is generated for LSdiffDistanceFactBasse
		String os = o.getType()+":" +o.groundConst; 
		String ts = this.type +":"+this.groundConst; 
		return os.compareTo(ts);
	}
	public void setType (char c) { 
		this.type = c; 
	}
}

package tyRuBa.engine;

/**
 * @author kdvolder
 */
public abstract class RBSubstitutable extends RBTerm {
	
	protected String name;
	
	RBSubstitutable(String name) {
		this.name = name;
	}

	public String name() {
		return name;
	}

	public int hashCode() {
		return name.hashCode();
	}

	public String toString() {
		return name;
	}

	public boolean equals(Object obj) {
		return (obj instanceof RBSubstitutable)
			&& ((RBSubstitutable) obj).name == this.name;
	}	

}

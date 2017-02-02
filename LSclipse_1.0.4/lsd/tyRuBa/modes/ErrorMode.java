package tyRuBa.modes;

public class ErrorMode extends Mode {
	
	String msg;
		
	public ErrorMode(String msg) {
		super(Multiplicity.zero, Multiplicity.infinite);
		this.msg = msg;
	}
			
	public String toString() {
		return "ERROR: " + msg;
	}
	
	public boolean equals(Object other) {
		if (other instanceof ErrorMode) {
			return msg.equals(((ErrorMode)other).msg);
		} else {
			return false;
		}
	}
	
	public int hashCode() {
		return 122 + msg.hashCode();
	}

	public Mode add(Mode other) {
		return this;
	}

}

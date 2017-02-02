package tyRuBa.engine;

import java.io.Serializable;


public class Validator implements Serializable {
    
    private boolean isOutdated = true;
	private boolean isValid = true;
	
    private long handle = -1;
    	
    public Validator() {}

    public long handle() {
		return handle;
	}
	
	public void setHandle(long handle) {
		this.handle = handle;
	}

	/**
	 * Method isValid.
	 * @return boolean
	 */
	public boolean isValid() {
		return isValid;
	}
    
    public void invalidate() {
        isValid = false;
    }

	public String toString() {
		return "Validator("+handle+","
		  + (isOutdated ? "OUTDATED" : "UPTODATE") +"," 
		  + (isValid ? "VALID" : "INALIDATED") + ")";
	}

	public boolean isOutdated() {
		return isOutdated;
	}

	public void setOutdated(boolean flag) {
		isOutdated = flag;
	}
	
	private boolean hasAssociatedFacts = false;
	
	public boolean hasAssociatedFacts() {
	    return hasAssociatedFacts;
	}
	
	public void setHasAssociatedFacts(boolean flag) {
	    hasAssociatedFacts = flag;
	}

}

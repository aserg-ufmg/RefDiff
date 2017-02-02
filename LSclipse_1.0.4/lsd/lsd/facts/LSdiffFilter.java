package lsd.facts;

public class LSdiffFilter {

	final boolean packageLevel; 
	final boolean typeLevel; 
	final boolean methodLevel; 
	final boolean fieldLevel; 
	final boolean bodyLevel;
	
	public LSdiffFilter( boolean packageL, boolean classL, boolean methodL, boolean fieldL, boolean bodyL) { 
		packageLevel = packageL; 
		typeLevel = classL;
		methodLevel = methodL;
		fieldLevel = fieldL; 
		bodyLevel = bodyL;
	}
	
	
	
}

package lsd.rule;

import java.util.ArrayList;
import java.util.List;


public class LSDConst {
	
	public static void main (String args[]) { 
		String field = "\"ca.ubc.jquery%.ActionsLog#append\"";
		System.out.println(getContainerPackage(field));
		System.out.println(getContainerType(field));
		System.out.println(getShortMethodOrFieldName(field));
	
		LSDFact mf = createModifiedField(field);
		System.out.println(mf.toString());
		String method= "\"ca.ubc.jquery%.ActionsLog#logGeneric(java.lang%.String)\"";
		LSDFact mm = createModifiedMethod(method);
		System.out.println(mm.toString());
	
		String type = "\"org.jfree.chart.util%.LineUtilities\"";
		LSDFact mt = createModifiedType(type);
		System.out.println(mt.toString());
		
		String shortName= "\"drawDomainMarker(java.awt%.Graphics2D,org.jfree.chart.plot%.CategoryPlot,org.jfree.chart.axis%.CategoryAxis,org.jfree.chart.plot%.CategoryMarker,java.awt.geom%.Rectangle2D)\""; 
		String typeName= "\"org.jfree.chart.renderer.category%.IntervalBarRenderer\"";
		String fullName = createFullMethodOrFieldName(shortName, typeName);
		LSDFact mm2 = createModifiedMethod(fullName);
		System.out.println(mm2.toString());
	}
	private static String getContainerType (String methodOrField) { 
		int beginIndex = methodOrField.indexOf("\"")+1;
		int endIndex = methodOrField.indexOf("#"); 
		assert (beginIndex>=1); 
		assert (endIndex>beginIndex);
		return methodOrField.substring(beginIndex,endIndex);	
	}
	private static String getShortMethodOrFieldName(String methodOrField) { 
		int beginIndex = methodOrField.indexOf("#")+1;
		int endIndex = methodOrField.length(); 
		assert (beginIndex>=1); 
		assert (endIndex>beginIndex);
		return methodOrField.substring(beginIndex,endIndex);
		
	}
	private static String getShortTypeName (String typeOrInnerType) { 
		 int beginIndex = typeOrInnerType.indexOf("%")+2; 
		 int endIndex = typeOrInnerType.length(); 
		 assert (beginIndex>=1); 
		 assert (endIndex> beginIndex); 
		 return typeOrInnerType.substring(beginIndex, endIndex); 
	}
	private static String getContainerPackage (String typeOrMethodOrField) { 
		int beginIndex = typeOrMethodOrField.indexOf("\"")+1;
		int endIndex = typeOrMethodOrField.indexOf("%"); 
		assert (beginIndex>=2); 
		assert (endIndex>beginIndex);
		try {
			return typeOrMethodOrField.substring(beginIndex,endIndex);
		}catch (StringIndexOutOfBoundsException e) { 
			System.err.println(typeOrMethodOrField); 
			System.exit(0);
		}
		return null;
	}
	
	private static String stripDoubleQuote (String s) { 
		return s.replaceAll("\"", "");
	}
	public static LSDFact createModifiedField(String fullFieldName) { 
		String shortFieldName = LSDConst.getShortMethodOrFieldName(fullFieldName);
		String containerType = LSDConst.getContainerType(fullFieldName);

		LSDPredicate pred = LSDPredicate.getPredicate("modified_field"); // fbt
		fullFieldName =stripDoubleQuote(fullFieldName);
		List<String> constants = new ArrayList<String>();
		constants.add(fullFieldName);
		constants.add(shortFieldName);
		constants.add(containerType);

		LSDFact mf = LSDFact.createLSDFact(pred, constants, true);
		return mf;
	
	} 
	public static LSDFact createModifiedMethod(String fullMethodName) { 
		String shortMethodName = LSDConst.getShortMethodOrFieldName(fullMethodName);
		String containerType = LSDConst.getContainerType(fullMethodName);
		LSDPredicate pred = LSDPredicate.getPredicate("modified_method"); // mat
		List<String> constants = new ArrayList<String>();
		constants.add(stripDoubleQuote(fullMethodName));
		constants.add(shortMethodName);
		constants.add(containerType);

		LSDFact mf = LSDFact.createLSDFact(pred, constants, true);
		return mf;
		
	}
	
	public static LSDFact createModifiedType(String fullTypeName) { 
		String shortTypeName = LSDConst.getShortTypeName(fullTypeName);
		String containerPackage = LSDConst.getContainerPackage(fullTypeName); 
		LSDPredicate pred = LSDPredicate.getPredicate("modified_type"); // tmp 
		List<String> constants = new ArrayList<String>();
		constants.add(stripDoubleQuote(fullTypeName));
		constants.add(shortTypeName);
		constants.add(containerPackage);
		
		LSDFact mt = LSDFact.createLSDFact(pred, constants, true); 
		return mt; 
	}
	public static LSDFact createModifiedPackage(String fullPackageName) { 
		LSDPredicate pred = LSDPredicate.getPredicate("modified_package");
		List<String> constants = new ArrayList<String>();
		constants.add(fullPackageName);
		
		LSDFact mp = LSDFact.createLSDFact(pred,constants,true); 
		return mp;
	}
	public static String createFullMethodOrFieldName (String shortName, String containerName) { 
		return containerName.substring(0, containerName.length()) + "#"+shortName; 
	}
	public static LSDFact convertModifiedToAdded(LSDFact mf) { 
		LSDPredicate pred = LSDPredicate.getPredicate(mf.getPredicate().getName().replace("modified", "added"));
		
		return LSDFact.createLSDFact(pred, (ArrayList<LSDBinding>)mf.getBindings()); 
	}
	public static LSDFact convertModifiedToDeleted(LSDFact mf) { 
		LSDPredicate pred = LSDPredicate.getPredicate(mf.getPredicate().getName().replace("modified", "deleted"));
		
		return LSDFact.createLSDFact(pred, (ArrayList<LSDBinding>)mf.getBindings()); 
	}
}

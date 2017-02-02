package lsd.facts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import lsd.io.LSDTyrubaFactReader;
import lsd.rule.LSDBinding;
import lsd.rule.LSDConst;
import lsd.rule.LSDFact;

public class LSdiffHierarchialDeltaKB {

	public String ADDED="ADD"; 
	public String DELETED = "DELETE"; 
	public String MODIFIED = "MODIFY"; 

	public static final int PACKAGE_LEVEL = 0;
	public static final int TYPE_LEVEL = 1;
	public static final int TYPE_DEPENDENCY_LEVEL = 2; 
	public static final int METHOD_LEVEL = 3;
	public static final int FIELD_LEVEL = 4; 
	public static final int BODY_LEVEL = 5; 
	
	HashMap<String, TreeSet<LSDFact>> packageLevel= new HashMap<String, TreeSet<LSDFact>>(); 
	HashMap<String, TreeSet<LSDFact>> typeLevel =new HashMap<String, TreeSet<LSDFact>>(); 
	HashMap<String, TreeSet<LSDFact>> methodLevel= new HashMap<String, TreeSet<LSDFact>>(); 
	HashMap<String, TreeSet<LSDFact>> fieldLevel = new HashMap<String, TreeSet<LSDFact>>(); 

	private LSdiffFilter filter = new LSdiffFilter(true,true,true,true,true);
	private ArrayList<LSDFact> originalDeltaKB; 
	
	public static void main (String args[]) { 
//		File deltaKBFile = new File ("input/jfreechart/0.9.10_0.9.11delta.rub");
		File deltaKBFile = new File ("input/jfreechart/1.0.12_1.0.13delta.rub");
		ArrayList<LSDFact> deltaKB= new LSDTyrubaFactReader(deltaKBFile).getFacts();
		LSdiffHierarchialDeltaKB modifiedFB = new LSdiffHierarchialDeltaKB(deltaKB); 
		TreeSet<LSDFact> ontheflyDeltaKB = new TreeSet<LSDFact>();
		TreeSet<LSDFact> ontheflyDeltaKB2 = new TreeSet<LSDFact>();
		
		File temp = new File("temp-fileterdDelta"); 
		File temp2 = new File("temp-hFilteredDelta");
		PrintStream p;
		PrintStream p2; 
		try {
			p = new PrintStream(temp);
			p2 = new PrintStream(temp2);

			modifiedFB.filterFacts(p, ontheflyDeltaKB); 
			modifiedFB.topDownTraversal2(p2, ontheflyDeltaKB2);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		boolean result1 = modifiedFB.checkEquivalence(deltaKB, ontheflyDeltaKB);

		boolean result2 = modifiedFB.checkEquivalence(deltaKB, ontheflyDeltaKB2);
		System.err.println(result1);
		System.err.println(result2);
	}
	public LSdiffHierarchialDeltaKB(ArrayList<LSDFact> deltaKB) {
		this.originalDeltaKB = deltaKB;
		constructFieldLevel(); 
		constructMethodLevel(); 
		constructTypeLevel(); 
		constructPackageLevel();
	}
	
	public TreeSet<LSDFact> expandCluster (List<LSDFact> cluster, int level) { 
		switch (level) {
		case PACKAGE_LEVEL:
			return getPackageLevelFacts(null);
		case TYPE_LEVEL:
			return expandPackageLevelCluster2TypeElements(null, cluster);
		case TYPE_DEPENDENCY_LEVEL:
			return expandTypeLevelCluster2TypeDependencies(null, cluster);
		case METHOD_LEVEL:
			return expandTypeLevelCluster2Methods(null, cluster);
		case FIELD_LEVEL:
			return expandTypeLevelCluster2Fields(null, cluster);
		case BODY_LEVEL:
			return expandMethodLevelCluster2Bodies(null, cluster);
		}
		return null;
	}
	private boolean checkEquivalence(ArrayList<LSDFact> deltaKB, TreeSet<LSDFact> ontheflyDeltaKB) { 
		ArrayList<LSDFact> tempOriginal = new ArrayList<LSDFact>(deltaKB); 
		System.out.println("original:\t"+tempOriginal.size()); 
		tempOriginal.removeAll(ontheflyDeltaKB); 
		ArrayList<LSDFact> tempOntheFly = new ArrayList<LSDFact>(); 
		// add non modified facts
		for (LSDFact f: ontheflyDeltaKB) { 
			if (f.getPredicate().getName().indexOf("modified_")<0){ 
				tempOntheFly.add(f);
			}
		}
		System.out.println("onthefly:\t"+tempOntheFly.size());
		tempOntheFly.removeAll(deltaKB); 
		
		System.out.println("onthefly - original:\t" + tempOntheFly.size()
				); 
		for (LSDFact f :tempOntheFly) { 
			System.out.println(f); 
		}
		System.out.println("original - onthefly:\t" + tempOriginal.size()); 
		for (LSDFact f: tempOriginal ) { 
			System.out.println(f);
		}
		return (tempOriginal.size()==0 && tempOntheFly.size()==0);
	}
	

	private void filterFacts (PrintStream p, TreeSet<LSDFact> output ) { 
		// topDownTraversal(p, output); 
		output.addAll(getPackageLevelFacts(p));
		output.addAll(expandPackageLevelCluster2TypeElements(p, null)) ;
		output.addAll(expandTypeLevelCluster2TypeDependencies(p, null)); 
		output.addAll(expandTypeLevelCluster2Methods(p,null)); 
		output.addAll(expandTypeLevelCluster2Fields(p, null));
		output.addAll(expandMethodLevelCluster2Bodies(p, null));
	}

	public void filterFacts (PrintStream p, TreeSet<LSDFact> output, LSdiffFilter filter ) { 
		assert (filter!=null); 
		if (filter.packageLevel) output.addAll(getPackageLevelFacts(p));
		if (filter.typeLevel) output.addAll(expandPackageLevelCluster2TypeElements(p, null)) ;
		if (filter.typeLevel) output.addAll(expandTypeLevelCluster2TypeDependencies(p, null)); 
		if (filter.methodLevel) output.addAll(expandTypeLevelCluster2Methods(p,null)); 
		if (filter.fieldLevel) output.addAll(expandTypeLevelCluster2Fields(p, null));
		if (filter.bodyLevel) output.addAll(expandMethodLevelCluster2Bodies(p, null));
	}
	
	private TreeSet<LSDFact> getPackageLevelFacts (PrintStream p) {  
		TreeSet<LSDFact> ontheflyDeltaKB = new TreeSet<LSDFact>(); 
		for (String kind: packageLevel.keySet()) {
			for (LSDFact packageF: packageLevel.get(kind)) { 
				if (p!=null) p.println(packageF);
				ontheflyDeltaKB.add(packageF);
			}
		}
		return ontheflyDeltaKB;
	}
	
	private TreeSet<LSDFact> expandPackageLevelCluster2TypeElements(PrintStream p, List<LSDFact> packageLevelCluster) { 
		TreeSet<LSDFact> ontheflyDeltaKB = new TreeSet<LSDFact>(); 
		TreeSet<String> packageConstants = null;
		// collect a list of package name constants
		if (packageLevelCluster !=null) { 
			packageConstants = new TreeSet<String>(); 
			for (LSDFact packageF: packageLevelCluster) { 
				packageConstants.add(packageF.getBindings().get(0).getGroundConst());
			}
		} 

		for (String kind: typeLevel.keySet()) { 
			for (LSDFact typeF: typeLevel.get(kind)) { 
				String containerPackage= typeF.getBindings().get(2).getGroundConst(); 
				if (packageConstants==null || packageConstants.contains(containerPackage)) {  
					if (p!=null) p.println("\t" + typeF);
					ontheflyDeltaKB.add(typeF);
				}
			}
		}
		return ontheflyDeltaKB; 
	}
	// return: output, relevant delta kb facts 
	// p: output to print stream 
	// typeLevelFacts: typelevel facts, either modified_types, added_types, deleted_types,
	private TreeSet<LSDFact> expandTypeLevelCluster2TypeDependencies (PrintStream p, List<LSDFact> typeLevelCluster) { 
		// print subtypes
		// print typeintype
		// print inherited methods
		// print inherited fields
		TreeSet<LSDFact> ontheflyDeltaKB = new TreeSet<LSDFact>();
		TreeSet<String> typeConstants = null; 
		if (typeLevelCluster!=null) { 
			typeConstants= new TreeSet<String>(); 
			for (LSDFact typeF: typeLevelCluster) { 
				typeConstants.add(typeF.getBindings().get(0).getGroundConst());
			}
		}
		for (LSDFact fact:originalDeltaKB) {
			String involvedType = null; 
			if (fact.getPredicate().getName().indexOf("_typeintype")>0) { 
				involvedType= fact.getBindings().get(1).getGroundConst(); 
			}
			// extends (Super, Sub)  => Sub was modified 
			else if (fact.getPredicate().getName().indexOf("_extends")>0) { 
				involvedType = fact.getBindings().get(1).getGroundConst(); 
				// implements (Super, Sub) => Sub was modified
			}else if (fact.getPredicate().getName().indexOf("_implements")>0) { 
				involvedType= fact.getBindings().get(1).getGroundConst(); 
			// FIXME: I want to move inherited files and inherited methods to method level or field level change.  
			}else if (fact.getPredicate().getName().indexOf("_inheritedfield")>0) { 
				involvedType = fact.getBindings().get(2).getGroundConst(); 
			}else if (fact.getPredicate().getName().indexOf("_inheritedmethod")>0) { 
				involvedType= fact.getBindings().get(2).getGroundConst();
			}
			if (involvedType!=null && (typeConstants==null || typeConstants.contains(involvedType))) {
				if (p!=null) p.println("\t\t\t"+ fact); 
				ontheflyDeltaKB.add(fact);
			} 
		}
		return ontheflyDeltaKB;
	}

	private TreeSet<LSDFact> expandTypeLevelCluster2Methods (PrintStream p, List<LSDFact> typeLevelCluster) { 	
		TreeSet<LSDFact> ontheflyDeltaKB = new TreeSet<LSDFact>(); 
		TreeSet<String> typeConstants = null; 
		TreeSet<String> methodConstants = new TreeSet<String>(); 
		if (typeLevelCluster!=null) { 
			typeConstants= new TreeSet<String>(); 
			for (LSDFact typeF: typeLevelCluster) { 
				typeConstants.add(typeF.getBindings().get(0).getGroundConst());
			}
		}
		for (String kind: methodLevel.keySet()) { 
			for (LSDFact methodF: methodLevel.get(kind)) { 
				String containerType = methodF.getBindings().get(2).getGroundConst(); 
				if (typeConstants==null || typeConstants.contains(containerType)){
					if (p!=null) p.println("\t\t"+methodF);
					ontheflyDeltaKB.add(methodF);	
					methodConstants.add(methodF.getBindings().get(0).getGroundConst());
				}
			}
		}
		// add _return facts that are related to added method level facts. 
		for (LSDFact fact : originalDeltaKB) { 
			if (fact.getPredicate().getName().indexOf("_return") > 0) { 
				String involvedMethod =  fact.getBindings().get(0).getGroundConst();
				if (methodConstants.contains(involvedMethod)) { 
					ontheflyDeltaKB.add(fact);

				}
			}
		}	
		return ontheflyDeltaKB; 
	}

	private TreeSet<LSDFact> expandTypeLevelCluster2Fields (PrintStream p, List<LSDFact> typeLevelCluster) { 
		TreeSet<LSDFact> ontheflyDeltaKB = new TreeSet<LSDFact>(); 
		TreeSet<String> typeConstants = null; 
		TreeSet<String> fieldConstants = new TreeSet<String>(); 
		if (typeLevelCluster!=null) { 
			typeConstants= new TreeSet<String>(); 
			for (LSDFact typeF: typeLevelCluster) { 
				typeConstants.add(typeF.getBindings().get(0).getGroundConst());
			}
		}
		for (String kind: fieldLevel.keySet()) { 
			for (LSDFact fieldF: fieldLevel.get(kind)) { 
				String containerType = fieldF.getBindings().get(2)
				.getGroundConst();

				if (typeConstants == null
						|| typeConstants.contains(containerType)) {
					if (p != null)
						p.println("\t\t" + fieldF);
					ontheflyDeltaKB.add(fieldF);
					fieldConstants.add(fieldF.getBindings().get(0)
							.getGroundConst());
				}
			}
		}

		for (LSDFact fact : originalDeltaKB) {
			if (fact.getPredicate().getName().indexOf("_fieldoftype") > 0) { 
				String involvedField = fact.getBindings().get(0).getGroundConst();
				if (fieldConstants.contains(involvedField)) { 
					ontheflyDeltaKB.add(fact); 

				}
			}
		}
		return ontheflyDeltaKB;
	}
	private TreeSet<LSDFact> expandMethodLevelCluster2Bodies (PrintStream p, List<LSDFact> methodLevelCluster) { 
		TreeSet<LSDFact> ontheflyDeltaKB = new TreeSet<LSDFact>(); 
		TreeSet<String> methodConstants = null; 
		if (methodLevelCluster!=null) { 
			methodConstants= new TreeSet<String>(); 
			for (LSDFact methodF: methodLevelCluster) { 
				methodConstants.add(methodF.getBindings().get(0).getGroundConst());
			}
		}

		for (LSDFact fact:originalDeltaKB) {
			String involvedMethod =null;  
			if (fact.getPredicate().getName().indexOf("_calls")>0) { 
				involvedMethod= fact.getBindings().get(0).getGroundConst(); 
			}
			else if (fact.getPredicate().getName().indexOf("_accesses")>0) { 
				involvedMethod = fact.getBindings().get(1).getGroundConst(); 
			}
			if (involvedMethod!=null && (methodConstants==null || methodConstants.contains(involvedMethod))){
				if (p!=null) p.println("\t\t\t"+ fact); 
				ontheflyDeltaKB.add(fact);
			}
		}
		return ontheflyDeltaKB;
	}


	private void printPakcageLevelFactStat(PrintStream p) { 
		if (p!=null) p.println("# added_package:\t"+packageLevel.get(ADDED).size()); 
		if (p!=null) p.println("# deleted_package:\t"+packageLevel.get(DELETED).size());  
		if (p!=null) p.println("# modified_package:\t"+packageLevel.get(MODIFIED).size()); 
		
	}
	private void printTypeLevelFactStat(PrintStream p) { 
		if (p!=null) p.println("# added_type:\t"+typeLevel.get(ADDED).size()); 
		if (p!=null) p.println("# deleted_type:\t"+typeLevel.get(DELETED).size());  
		if (p!=null) p.println("# modified_type:\t"+typeLevel.get(MODIFIED).size()); 	
	}
	private void printMethodLevelFactStat(PrintStream p) { 
		if (p!=null) p.println("# added_method:\t"+methodLevel.get(ADDED).size()); 
		if (p!=null) p.println("# deleted_method:\t"+methodLevel.get(DELETED).size());  
		if (p!=null) p.println("# modified_method:\t"+methodLevel.get(MODIFIED).size()); 
	}
	private void printFieldLevelFactStat(PrintStream p) { 
		if (p!=null) p.println("# added_field:\t"+fieldLevel.get(ADDED).size()); 
		if (p!=null) p.println("# deleted_field:\t"+fieldLevel.get(DELETED).size());  
		if (p!=null) p.println("# modified_field:\t"+fieldLevel.get(MODIFIED).size()); 
		
	}
	
	
	private void constructFieldLevel() {
		TreeSet<LSDFact> addedField = new TreeSet<LSDFact>();
		TreeSet<LSDFact> deletedField = new TreeSet<LSDFact>();
		TreeSet<LSDFact> modifiedField = new TreeSet<LSDFact>();
		for (LSDFact fact : originalDeltaKB) {
			String predName = fact.getPredicate().getName();
			if (predName.equals("added_field")) {
				// for all added_field (fieldFullName, ... ,...) => added_field
				// (fieldFullName)
				addedField.add(fact);
			} else if (predName.equals("deleted_field")) {
				// for all deleted_field (fieldFullName, ..., ...) =>
				// deleted_field (fieldFullName)
				deletedField.add(fact);
			}
		}
		int counter = 0;
		for (LSDFact fact : originalDeltaKB) {
			String predName = fact.getPredicate().getName();
			counter++;
			System.out.println(counter+". \""+predName+"\":"+fact);
			if (predName.equals("added_fieldoftype")
					|| predName.equals("deleted_fieldoftype")) {
				// for all fieldoftype (fieldFullName, ..., ...) =>
				// changed_field (fieldFullName)
				List<LSDBinding> bindings = fact.getBindings();
				LSDBinding firstBinding = bindings.get(0);
				LSDFact mfield = LSDConst.createModifiedField(firstBinding
						.getGroundConst());
				if (!containsTheSameFact(addedField, deletedField, mfield))
					modifiedField.add(mfield);

				// add only if it is not already in added or deleted fields. 			
			} 
		}
		fieldLevel.put(ADDED, addedField);
		fieldLevel.put(DELETED, deletedField); 
		fieldLevel.put(MODIFIED, modifiedField); 
	}
	
	private void constructMethodLevel() { 
		TreeSet<LSDFact> addedMethod = new TreeSet<LSDFact>(); 
		TreeSet<LSDFact> deletedMethod = new TreeSet<LSDFact>();
		TreeSet<LSDFact> modifiedMethod = new TreeSet<LSDFact>(); 
		
		for (LSDFact fact : originalDeltaKB) {
			String predName = fact.getPredicate().getName();
			if (predName.equals("added_method")) {
				// for all added_method => added_method
				addedMethod.add(fact);
			} else if (predName.equals("deleted_method")) {
				// for all deleted_method => deleted_method
				deletedMethod.add(fact);
			}
		}
		for (LSDFact fact : originalDeltaKB) {
			String predName = fact.getPredicate().getName();
			if (predName.equals("added_return")
					|| predName.equals("deleted_return")) {
				List<LSDBinding> bindings = fact.getBindings();
				LSDBinding firstBinding = bindings.get(0);
				LSDFact mmethod = LSDConst.createModifiedMethod(firstBinding
						.getGroundConst());
				if (!containsTheSameFact(addedMethod, deletedMethod, mmethod))
					modifiedMethod.add(mmethod);

			} else if (predName.equals("deleted_calls")
					|| predName.equals("added_calls")) {
				// for all added_/deleted_calls(caller, calllee) =>
				// changed_method (caller)
				List<LSDBinding> bindings = fact.getBindings();
				LSDBinding firstBinding = bindings.get(0);
				LSDFact mmethod = LSDConst.createModifiedMethod(firstBinding
						.getGroundConst());
				if (!containsTheSameFact(addedMethod, deletedMethod, mmethod))
					modifiedMethod.add(mmethod);
			} else if (predName.equals("added_accesses")
					|| predName.equals("deleted_accesses")) {
				// for all added_/deleted_accesses( field, accessor) =>
				// changed_method
				// (accessor)
				List<LSDBinding> bindings = fact.getBindings();
				LSDBinding secondBinding = bindings.get(1);
				LSDFact mmethod = LSDConst.createModifiedMethod(secondBinding
						.getGroundConst());
				if (!containsTheSameFact(addedMethod, deletedMethod, mmethod))
					modifiedMethod.add(mmethod);
			}
		}
		methodLevel.put(ADDED, addedMethod);
		methodLevel.put(DELETED, deletedMethod); 
		methodLevel.put(MODIFIED, modifiedMethod);
		
	}
	private void constructTypeLevel() { 
		TreeSet<LSDFact> addedType = new TreeSet<LSDFact>(); 
		TreeSet<LSDFact> deletedType = new TreeSet<LSDFact>(); 
		TreeSet<LSDFact> modifiedType = new TreeSet<LSDFact>(); 
		
		for (LSDFact fact : originalDeltaKB) {
			String predName = fact.getPredicate().getName();
			if (predName.equals("added_type")) {
				// for all added_type => added_type
				addedType.add(fact);
			} else if (predName.equals("deleted_type")) {
				// for all deleted_type => deleted_type
				deletedType.add(fact);  
			}
		}
		
		for (LSDFact fact : originalDeltaKB) {
			String predName = fact.getPredicate().getName();
			if (predName.endsWith("_typeintype")) { 
				// for all *typeintype(A,B) => changed_type(B) 
				List<LSDBinding> bindings = fact.getBindings(); 
				LSDBinding secondBinding = bindings.get(1);
				LSDFact mtype = LSDConst.createModifiedType(secondBinding.getGroundConst()); 
				if (!containsTheSameFact(addedType, deletedType, mtype)) modifiedType.add(mtype);
			} else if (predName.endsWith("_extends") || predName.endsWith("_implements")) { 
				// for all *subtype(A,B) => changed_type(B) 
				List<LSDBinding> bindings = fact.getBindings(); 
				LSDBinding secondBinding = bindings.get(1); 
				LSDFact mtype = LSDConst.createModifiedType(secondBinding.getGroundConst()); 
				if (!containsTheSameFact(addedType, deletedType, mtype)) modifiedType.add(mtype);
				
			} else if (predName.endsWith("_inheritedmethod")) {
				// for now *inheritedmethod(..., A, B) => changed_type(A)
				List<LSDBinding> bindings = fact.getBindings(); 
				LSDBinding secondBinding = bindings.get(1); 
				LSDFact mtype = LSDConst.createModifiedType(secondBinding.getGroundConst()); 
				modifiedType.add(mtype); 
				
				// for now *inheritedmethod(..., A, B) => changed_type(B)
				LSDBinding thirdBinding = bindings.get(2); 
				LSDFact m2type = LSDConst.createModifiedType(thirdBinding.getGroundConst()); 
				if (!containsTheSameFact(addedType, deletedType, m2type)) modifiedType.add(m2type);
				
			} else if (predName.endsWith("_inheritedfield")) { 
                // for now *inheritedfield(..., A, B) => changed_type(A)
				// for now *inheritedfield(..., A, B) => changed_type(B)
//				 for now *inheritedmethod(..., A, B) => changed_type(A)
				List<LSDBinding> bindings = fact.getBindings(); 
				LSDBinding secondBinding = bindings.get(1); 
				LSDFact mtype = LSDConst.createModifiedType(secondBinding.getGroundConst()); 
				if (!containsTheSameFact(addedType, deletedType, mtype)) modifiedType.add(mtype);
				
				// for now *inheritedmethod(..., A, B) => changed_type(B)
				LSDBinding thirdBinding = bindings.get(2); 
				LSDFact m2type = LSDConst.createModifiedType(thirdBinding.getGroundConst()); 
				if (!containsTheSameFact(addedType, deletedType, m2type)) modifiedType.add(m2type);
			}
		}
		
		// for all added/changed/deleted_method => changed_type 
		for (String kind: methodLevel.keySet()) { 
			for (LSDFact fact: methodLevel.get(kind)) { 
				// get container Type. 
				List<LSDBinding> bindings = fact.getBindings(); 
				LSDBinding thirdBinding = bindings.get(2); 
				LSDFact mtype = LSDConst.createModifiedType(thirdBinding.getGroundConst()); 
				if (!containsTheSameFact(addedType, deletedType, mtype)) modifiedType.add(mtype);
			}
		}
		// for all added/changed/deleted_field =>changed_type 
		for (String kind: fieldLevel.keySet()) { 
			for (LSDFact fact: fieldLevel.get(kind)) { 
				// get container Type. 
				List<LSDBinding> bindings = fact.getBindings(); 
				LSDBinding thirdBinding = bindings.get(2); 
				LSDFact mtype = LSDConst.createModifiedType(thirdBinding
						.getGroundConst());
				if (!containsTheSameFact(addedType, deletedType, mtype)) modifiedType.add(mtype);
			}
		}
		typeLevel.put(ADDED, addedType);
		typeLevel.put(DELETED, deletedType);
		typeLevel.put(MODIFIED, modifiedType);
	}
	
	private void constructPackageLevel() { 
		TreeSet<LSDFact> addedPackage = new TreeSet<LSDFact>(); 
		TreeSet<LSDFact> deletedPackage = new TreeSet<LSDFact>(); 
		TreeSet<LSDFact> modifiedPackage = new TreeSet<LSDFact>(); 
		
		for (LSDFact fact : originalDeltaKB) {
			String predName = fact.getPredicate().getName();
			if (predName.equals("added_package")) {
				// for all added_package => added_package 		
				addedPackage.add(fact);
			} else if (predName.equals("deleted_package")) {
				// for all deleted_package => deleted_package 
				deletedPackage.add(fact);
			}
		}
		// for all added/changed/deleted_type => changed_package 	
		for (String kind: typeLevel.keySet()) { 
			for (LSDFact fact: typeLevel.get(kind)) { 
				// get container Type. 
				List<LSDBinding> bindings = fact.getBindings(); 
				LSDBinding thirdBinding = bindings.get(2); 
				LSDFact mpackage = LSDConst.createModifiedPackage(thirdBinding
						.getGroundConst());
				
				if (!containsTheSameFact(addedPackage, deletedPackage, mpackage)) modifiedPackage.add(mpackage);
			}
		}
		packageLevel.put(ADDED, addedPackage);
		packageLevel.put(DELETED, deletedPackage);
		packageLevel.put(MODIFIED, modifiedPackage);
	}
	
	private boolean containsTheSameFact(TreeSet<LSDFact> addSet, TreeSet<LSDFact> deletedSet, LSDFact mf) { 
		LSDFact add = LSDConst.convertModifiedToAdded(mf); 
		LSDFact del = LSDConst.convertModifiedToDeleted(mf); 
		return (addSet.contains(add) || deletedSet.contains(del) );
	}


private void filterFacts2 (PrintStream p, TreeSet<LSDFact> output, LSdiffFilter filter ) { 
	if (filter==null) return; 
	this.filter = filter;
	topDownTraversal(p, output); 
}

private void filterPerType (PrintStream p, TreeSet<LSDFact> ontheflyDeltaKB, LSDFact typeF) { 
	// print subtypes
	printSubtypes(p,ontheflyDeltaKB, typeF);
	// print typeintype
	printInnerTypes(p,ontheflyDeltaKB, typeF);
	// print inherited methods
	printInheritedMethods(p, ontheflyDeltaKB, typeF);
	// print inherited fields
	printInheritedFields(p, ontheflyDeltaKB, typeF); 
	printMethodsInType(p,ontheflyDeltaKB, typeF);
	printFieldsInType(p,ontheflyDeltaKB, typeF);

}
private void filterPerMethod (PrintStream p, TreeSet<LSDFact> ontheflyDeltaKB, LSDFact methodF) { 
	 printCallsInMethod(p,ontheflyDeltaKB, methodF); 
	 printAccessesInMethod(p, ontheflyDeltaKB, methodF);
	 printReturnInMethod(p,ontheflyDeltaKB, methodF);
}
private void filterPerField (PrintStream p, TreeSet<LSDFact> ontheflyDeltaKB, LSDFact fieldF) { 
	 printFieldOfType(p, ontheflyDeltaKB, fieldF);
}


private void topDownTraversal (PrintStream p, TreeSet<LSDFact> ontheflyDeltaKB) { 	
	//  start putting facts from all dirty package level elements 
	for (String kind: packageLevel.keySet()) {
		for (LSDFact packageF: packageLevel.get(kind)) { 
			if (filter.packageLevel && p!=null) p.println(packageF);
			if (filter.packageLevel) ontheflyDeltaKB.add(packageF);
			filterPerPackage(p, ontheflyDeltaKB, packageF);
		}
	}
}
public void filterPerPackage (PrintStream p, TreeSet<LSDFact> ontheflyDeltaKB, LSDFact packageF) { 
	// for a given package, start putting facts for all dirty elements within the package
	for (String kind: typeLevel.keySet()) { 
		for (LSDFact typeF: typeLevel.get(kind)) { 
			 if (typeF.getBindings().get(2).getGroundConst().equals(
					packageF.getBindings().get(0).getGroundConst())) {	
				if (filter.typeLevel && p!=null) p.println("\t" + typeF);
				if (filter.typeLevel) ontheflyDeltaKB.add(typeF);
				filterPerType(p, ontheflyDeltaKB, typeF);
					
			 }
		}
	}
}
private void printMethodsInType (PrintStream p, TreeSet<LSDFact> ontheflyDeltaKB, LSDFact typeF) { 
	//for a given type, start putting facts for all dirty elements within the type
	for (String kind: methodLevel.keySet()) { 
		for (LSDFact methodF: methodLevel.get(kind)) { 
			 if (methodF.getBindings().get(2).getGroundConst().equals(typeF.getBindings().get(0).getGroundConst())){
				 if (filter.methodLevel && p!=null) p.println("\t\t"+methodF);
				 if (filter.methodLevel) ontheflyDeltaKB.add(methodF);	
				 filterPerMethod(p, ontheflyDeltaKB, methodF);
			 }
		}
	}
}
private void printFieldsInType(PrintStream p, TreeSet<LSDFact> ontheflyDeltaKB, LSDFact typeF) {
	
	for (String kind: fieldLevel.keySet()) { 
		for (LSDFact fieldF: fieldLevel.get(kind)) { 
			 if (fieldF.getBindings().get(2).getGroundConst().equals(typeF.getBindings().get(0).getGroundConst())){
				 if (filter.fieldLevel && p!=null) p.println("\t\t"+fieldF);
				 if (filter.fieldLevel) ontheflyDeltaKB.add(fieldF);
				 filterPerField(p, ontheflyDeltaKB, fieldF);
			 }
		}
	}
}
private void printInnerTypes (PrintStream p, TreeSet<LSDFact> ontheflyDeltaKB, LSDFact typeF) { 
	for (LSDFact fact:originalDeltaKB) {
		if (fact.getPredicate().getName().indexOf("_typeintype")>0 && fact.getBindings().get(1).getGroundConst().equals(typeF.getBindings().get(0).getGroundConst())){ 
			if (filter.typeLevel) ontheflyDeltaKB.add(fact);
			if (filter.typeLevel&& p!=null) p.println("\t\t\t"+ fact); 
		}
	}
}
private void printSubtypes (PrintStream p, TreeSet<LSDFact> ontheflyDeltaKB, LSDFact typeF) { 
	for (LSDFact fact:originalDeltaKB) {
		// extends (Super, Sub)  => Sub was modified 
		if (fact.getPredicate().getName().indexOf("_extends")>0 && fact.getBindings().get(1).getGroundConst().equals(typeF.getBindings().get(0).getGroundConst())){ 
			if (filter.typeLevel) ontheflyDeltaKB.add(fact);
			if (filter.typeLevel&& p!=null) p.println("\t\t\t"+ fact); 
		// implements (Super, Sub) => Sub was modified
		}else if (fact.getPredicate().getName().indexOf("_implements")>0 && fact.getBindings().get(1).getGroundConst().equals(typeF.getBindings().get(0).getGroundConst())){ 
			if (filter.typeLevel) ontheflyDeltaKB.add(fact);
			if (filter.typeLevel&& p!=null) p.println("\t\t\t"+ fact); 
		}
	}
}

private void printCallsInMethod(PrintStream p, TreeSet<LSDFact> ontheflyDeltaKB, LSDFact methodF) { 		
	for (LSDFact fact:originalDeltaKB) {
		if (fact.getPredicate().getName().indexOf("_calls")>0 && fact.getBindings().get(0).getGroundConst().equals(methodF.getBindings().get(0).getGroundConst())){ 
			if (filter.bodyLevel) ontheflyDeltaKB.add(fact);
			if (filter.bodyLevel&& p!=null) p.println("\t\t\t"+ fact); 
		}
	}
}
private void printAccessesInMethod(PrintStream p, TreeSet<LSDFact> ontheflyDeltaKB, LSDFact methodF) { 
	for (LSDFact fact:originalDeltaKB) {
		if (fact.getPredicate().getName().indexOf("_accesses")>0 && fact.getBindings().get(1).getGroundConst().equals(methodF.getBindings().get(0).getGroundConst())){ 
			if (filter.bodyLevel) ontheflyDeltaKB.add(fact);
			if (filter.bodyLevel&& p!=null) p.println("\t\t\t"+ fact); 
		}
	}
}
private void printReturnInMethod(PrintStream p, TreeSet<LSDFact> ontheflyDeltaKB, LSDFact methodF) { 
	for (LSDFact fact:originalDeltaKB) {
		if (fact.getPredicate().getName().indexOf("_return")>0 && fact.getBindings().get(0).getGroundConst().equals(methodF.getBindings().get(0).getGroundConst())){ 
			if (filter.methodLevel) ontheflyDeltaKB.add(fact);
			if (filter.methodLevel&& p!=null) p.println("\t\t\t"+ fact); 
		}
	}
}
	
private void printInheritedFields (PrintStream p, TreeSet<LSDFact> ontheflyDeltaKB, LSDFact typeF){ 
	for (LSDFact fact:originalDeltaKB) {
		if (fact.getPredicate().getName().indexOf("_inheritedfield")>0 && fact.getBindings().get(2).getGroundConst().equals(typeF.getBindings().get(0).getGroundConst())){ 
			if (filter.fieldLevel) ontheflyDeltaKB.add(fact);
			if (filter.fieldLevel && p!=null) p.println("\t\t\t"+ fact); 
		}
	}
}
private void printInheritedMethods (PrintStream p,TreeSet<LSDFact> ontheflyDeltaKB,  LSDFact typeF) { 
	for (LSDFact fact:originalDeltaKB) {
		if (fact.getPredicate().getName().indexOf("_inheritedmethod")>0 && fact.getBindings().get(2).getGroundConst().equals(typeF.getBindings().get(0).getGroundConst())){ 
			if (filter.methodLevel) ontheflyDeltaKB.add(fact);
			if (filter.methodLevel&& p!=null) p.println("\t\t\t"+ fact); 
		}
	}
}

private void printFieldOfType (PrintStream p, TreeSet<LSDFact> ontheflyDeltaKB, LSDFact fieldF) { 
	for (LSDFact fact:originalDeltaKB) {
		if (fact.getPredicate().getName().indexOf("_fieldoftype")>0 && fact.getBindings().get(0).getGroundConst().equals(fieldF.getBindings().get(0).getGroundConst())){ 
			if (filter.fieldLevel) ontheflyDeltaKB.add(fact);
			if (filter.fieldLevel && p!=null) p.println("\t\t\t"+ fact); 
		}
	}
}


private void topDownTraversal2 (PrintStream p, TreeSet<LSDFact> ontheflyDeltaKB) { 	
	//  start putting facts from all dirty package level elements 
	for (String kind: packageLevel.keySet()) {
		for (LSDFact packageF: packageLevel.get(kind)) { 
			if (filter.packageLevel && p!=null) p.println(packageF);
			if (filter.packageLevel) ontheflyDeltaKB.add(packageF);
			filterPerPackage(p, ontheflyDeltaKB, packageF);
		}
	}
}
}


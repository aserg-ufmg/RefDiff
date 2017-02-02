package lsd.facts;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import lsd.io.LSDTyrubaFactReader;
import lsd.rule.LSDBinding;
import lsd.rule.LSDConst;
import lsd.rule.LSDFact;
import lsd.rule.LSDPredicate;

public class LSdiffDistanceFactBase {

	public static void main(String args[]) {
		String v [] = {"0.9.9_0.9.10", "0.9.10_0.9.11"}; 
		for (String v_i : v) { 
		
		File twoKBFile = new File(
				"input/jfreechart/"+v_i+"2KB.rub");
		ArrayList<LSDFact> twoKB = new LSDTyrubaFactReader(twoKBFile)
				.getFacts();
		File deltaKBFile = new File(
				"input/jfreechart/"+v_i+"delta.rub");
		ArrayList<LSDFact> deltaKB = new LSDTyrubaFactReader(deltaKBFile)
				.getFacts();
		
		System.out.println("Original 2KB Size:\t" + twoKB.size());
		System.out.println("Original Delta KB Size:\t" + deltaKB.size());
		
		LSdiffDistanceFactBase filter2KB = new LSdiffDistanceFactBase(twoKB,
				deltaKB);
		filter2KB.expand(1);
		System.out.println("Working Set Binding Size:" + filter2KB.workingSetBinding.size());		
		System.out.println("Working 2KB Size:" + filter2KB.working2KB.size());

		System.out.println("\n");
		}
	}

	private TreeSet<LSDFact> working2KB = new TreeSet<LSDFact>();

	private TreeSet<LSDBinding> workingSetBinding = new TreeSet<LSDBinding>();

	private final ArrayList<LSDFact> original2KB;

	private final ArrayList<LSDFact> originalDeltaKB;

	private final LSdiffHierarchialDeltaKB hdelta; 
	
	public LSdiffDistanceFactBase(ArrayList<LSDFact> twoKB,
			ArrayList<LSDFact> deltaKB) {	
		this.original2KB = twoKB;
		this.originalDeltaKB = deltaKB;
		this.hdelta = new LSdiffHierarchialDeltaKB(deltaKB);
	}

	public void expand(int depth) {
		initializedFromDirtyCodeElements(); 
		for (int i = 1; i <= depth; i++) {
			System.out.println("Iteration "+i);
			System.out.println("Working Set Binding Size:\t"+workingSetBinding.size());

			boolean stop = expandOneHopViaDependencies();
			System.out.println("Working 2KB Size:\t"+working2KB.size());
			
			if (stop) break;
		}
	}
	public ArrayList<LSDFact> getWorking2KBFacts() { 
		ArrayList<LSDFact> facts = new ArrayList<LSDFact>(working2KB); 
		return facts;
	}
	private void printWorkingSetBinding(PrintStream p) {
		
		for (LSDBinding b : workingSetBinding) {
			char c = b.getType();
			p.println(c + "\t:\t" + b.getGroundConst());
		}
	}

	private void printWorking2KBFact(PrintStream p) {
		
		for (LSDFact f : working2KB) {
			p.println(f);
		}
	}

	public void initializeFromDeltaKB() {
		// go over deltaKB facts and create an initial set of bindings to start
		// with
		for (LSDFact fact : originalDeltaKB) {
			addBindingsFromFact(workingSetBinding, fact); 
		}
		System.out.println("Initial Working Set Binding Size:\t" + workingSetBinding.size());
	}

	private void initializedFromDirtyCodeElements() {
		for (String kind : hdelta.packageLevel.keySet()) {
			for (LSDFact fact : hdelta.packageLevel.get(kind)) {
				addBindingsFromFact(workingSetBinding, fact);
			}
		}
		System.out.println("Initial Working Set Binding Size:\t"
				+ workingSetBinding.size());
		for (String kind : hdelta.typeLevel.keySet()) {
			for (LSDFact fact : hdelta.typeLevel.get(kind)) {
				addBindingsFromFact(workingSetBinding, fact);
			}
		}
		System.out.println("After Type Level: Working Set Binding Size:\t"
				+ workingSetBinding.size());

		for (String kind : hdelta.methodLevel.keySet()) {
			for (LSDFact fact : hdelta.methodLevel.get(kind)) {
				addBindingsFromFact(workingSetBinding, fact);
			}
		}
		System.out.println("After Method Level: Working Set Binding Size:\t"
				+ workingSetBinding.size());
		for (String kind : hdelta.fieldLevel.keySet()) {
			for (LSDFact fact : hdelta.fieldLevel.get(kind)) {
				addBindingsFromFact(workingSetBinding, fact);
			}
		}
		System.out.println("After Field Level: Working Set Binding Size:\t"
				+ workingSetBinding.size());
	}

	private void addBindingsFromFact(TreeSet<LSDBinding> storage, LSDFact fact) {
		List<LSDBinding> bindings = fact.getBindings();
		char[] types = fact.getPredicate().getTypes();
		for (int i = 0; i < types.length; i++) {
			LSDBinding b = bindings.get(i);
			b.setType(types[i]);
			storage.add(b);
		}
	}

	private boolean expandOneHopViaDependencies() {
		// for each fact in 2KB, check whether its binding is in the working set
		// of expanding bindings.
		TreeSet<LSDBinding> temp = new TreeSet<LSDBinding>();
		for (LSDFact twoKBfact : original2KB) {
			LSDPredicate tk_pred = twoKBfact.getPredicate();
			String tk_predName = tk_pred.getName();
			if (tk_predName.endsWith("_accesses")
					|| tk_predName.endsWith("_calls")
					|| tk_predName.endsWith("_implements") 
					|| tk_predName.endsWith("_extends")) {
				char[] types = tk_pred.getTypes();
				java.util.List<LSDBinding> bindings = twoKBfact.getBindings();
				for (int i = 0; i < types.length; i++) {
					// for each binding
					LSDBinding tk_binding = bindings.get(i);
					tk_binding.setType(types[i]);
					// current we prevent connecting through a package constant
					// to
					// prevent every facts contained in the top package.
					if (workingSetBinding.contains(tk_binding)) {
						// expand the working set binding
						// expand the working set 2KB facts
						working2KB.add(twoKBfact);
						addBindingsFromFact(temp, twoKBfact);
					}
				}

			} else if (tk_predName.endsWith("_inheritedfield"))
			// (A, ? ,B) => use A+B
			{
				java.util.List<LSDBinding> bindings = twoKBfact.getBindings();

				LSDBinding tk_binding_A = bindings.get(0);
				LSDBinding tk_binding_B = bindings.get(2);
				String fullName = LSDConst.createFullMethodOrFieldName(
						tk_binding_A.getGroundConst(), tk_binding_B
								.getGroundConst());
				LSDBinding tk_binding = LSDConst.createModifiedField(fullName)
						.getBindings().get(0);// create a new binding
				char[] types = tk_pred.getTypes();
				tk_binding.setType(types[0]);
				
				// combining A and B
				if (workingSetBinding.contains(tk_binding)) {
					// expand the working set binding
					// expand the working set 2KB facts
					working2KB.add(twoKBfact);
					addBindingsFromFact(temp, twoKBfact);
				}

			} else if (tk_predName.endsWith("_inheritedmethod"))
			// (A, ? , B) => use A+B
			{
				java.util.List<LSDBinding> bindings = twoKBfact.getBindings();
				LSDBinding tk_binding_A = bindings.get(0);
				LSDBinding tk_binding_B = bindings.get(2);
				String fullName = LSDConst.createFullMethodOrFieldName(
						tk_binding_A.getGroundConst(), tk_binding_B
								.getGroundConst());
				LSDBinding tk_binding = LSDConst.createModifiedMethod(fullName)
						.getBindings().get(0);// create a new binding
				char[] types = tk_pred.getTypes();
				tk_binding.setType(types[0]);
				
				// combining A and B
				if (workingSetBinding.contains(tk_binding)) {
					// expand the working set binding
					// expand the working set 2KB facts
					working2KB.add(twoKBfact);
					addBindingsFromFact(temp, twoKBfact);
				}

			} else if (tk_predName.endsWith("_typeintype")
					|| tk_predName.endsWith("_fieldoftype")
					|| tk_predName.endsWith("_return")) {
				// (A, B) => use A
				java.util.List<LSDBinding> bindings = twoKBfact.getBindings();
				LSDBinding tk_binding_A = bindings.get(0);
				char[] types = tk_pred.getTypes();
				tk_binding_A.setType(types[0]);
				
				if (workingSetBinding.contains(tk_binding_A)) {
					// expand the working set binding
					// expand the working set 2KB facts
					working2KB.add(twoKBfact);
					addBindingsFromFact(temp, twoKBfact);
				}
			
			} else if (tk_predName.endsWith("_package")
					|| tk_predName.endsWith("_type")
					|| tk_predName.endsWith("_method")
					|| tk_predName.endsWith("_field")) {
//				java.util.List<LSDBinding> bindings = twoKBfact.getBindings();
//				LSDBinding tk_binding_A = bindings.get(0);
//				char[] types = tk_pred.getTypes();
//				tk_binding_A.setType(types[0]);
//				
//				if (workingSetBinding.contains(tk_binding_A)) {
//					// expand the working set binding
//					// expand the working set 2KB facts
//					working2KB.add(twoKBfact);
//					addBindingsFromFact(temp, twoKBfact);
//				}
			
			} else {
//				System.err.println(tk_predName + "\t" + twoKBfact);
//				assert (false);
			}

		}
		System.err.println("temp Size: " + temp.size());
		workingSetBinding.addAll(temp);
		System.err.println("workingSetBinding: " + workingSetBinding.size());
		if (temp.size() == 0)
			return true;
		return false;
	}

// public void printRelated2KBFacts (LSDFact fact) {
//		// of expanding bindings.
//		List<LSDBinding> fact_bindings = fact.getBindings();
//		for (LSDFact twoKBfact : original2KB) {
//			LSDPredicate tk_pred = twoKBfact.getPredicate();
//			String tk_predName = tk_pred.getName();
//			if (tk_predName.endsWith("_accesses")
//					|| tk_predName.endsWith("_calls")
//					|| tk_predName.endsWith("_subtype")) {
//
//				char[] types = tk_pred.getTypes();
//				java.util.List<LSDBinding> bindings = twoKBfact.getBindings();
//				for (int i = 0; i < types.length; i++) {
//					// for each binding
//					LSDBinding tk_binding = bindings.get(i);
//					// current we prevent connecting through a package constant
//					// to
//					// prevent every facts contained in the top package.
//					if (fact_bindings.contains(tk_binding)) {
//						System.out.println("\t\t"+twoKBfact);
//						
//					}
//
//				}
//
//			} else if (tk_predName.endsWith("_inheritedfield"))
//			// (A, ? , B) => use A+B
//			{
//				java.util.List<LSDBinding> bindings = twoKBfact.getBindings();
//
//				LSDBinding tk_binding_A = bindings.get(0);
//				LSDBinding tk_binding_B = bindings.get(2);
//				String fullName = LSDConst.createFullMethodOrFieldName(
//						tk_binding_A.getGroundConst(), tk_binding_B
//								.getGroundConst());
//				LSDBinding tk_binding = LSDConst.createModifiedField(fullName)
//						.getBindings().get(0);// create a new binding
//				// combining A and B
//				if (fact_bindings.contains(tk_binding)) {
//					System.out.println("\t\t" + twoKBfact);
//				}
//
//			} else if (tk_predName.endsWith("_inheritedmethod"))
//			// (A, ? , B) => use A+B
//			{
//				java.util.List<LSDBinding> bindings = twoKBfact.getBindings();
//
//				LSDBinding tk_binding_A = bindings.get(0);
//				LSDBinding tk_binding_B = bindings.get(2);
//				String fullName = LSDConst.createFullMethodOrFieldName(
//						tk_binding_A.getGroundConst(), tk_binding_B
//								.getGroundConst());
//				LSDBinding tk_binding = LSDConst.createModifiedMethod(fullName)
//						.getBindings().get(0);// create a new binding
//				// combining A and B
//				if (fact_bindings.contains(tk_binding)) {
//					System.out.println("\t\t" + twoKBfact);
//				}
//
//			} else if (tk_predName.endsWith("_typeintype")
//					|| tk_predName.endsWith("_fieldoftype")
//					|| tk_predName.endsWith("_return")) {
//				// (A, B) => use A
//				java.util.List<LSDBinding> bindings = twoKBfact.getBindings();
//
//				LSDBinding tk_binding_A = bindings.get(0);
//
//				if (fact_bindings.contains(tk_binding_A)) {
//					System.out.println("\t\t" + twoKBfact);
//				}
//			}
//		}
//	}
//	public void expandHop() {
//			// working2KB => 2KB
//			// type => subtype (type, type)
//			// => typeintype (type, ?)
//			// method => return (method, ?)
//			// => calls (method, method)
//			// => accesses (?, method)
//			// => inheritedmethod (A, ?, B) where A+B = method
//			// field => fieldoftype (field, ?)
//			// => accesses (field, ?)
//			// => inheritedfield (A, ?, B) where A+B = field
//	}
}

package lsd.rule;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

public class LSDLiteral {
	private boolean nonNegated;

	protected final LSDPredicate predicate;

	protected final ArrayList<LSDBinding> bindings;

	public LSDLiteral(LSDPredicate pred, List<LSDBinding> bindings,
			boolean nonNegated) throws LSDInvalidTypeException {
		//System.out.println("Niki....LSD Literal was called");
		this.nonNegated = nonNegated;
		//System.out.println("non Negated is " + nonNegated);
		//System.out.println("Niki....The pred arity is " + pred.arity() + "and the binding size is " + bindings.size());
		if (pred.arity() != bindings.size()) {
			this.predicate = null;
			this.bindings = null;
			return;
		}
		// type check whether variables in the binding is compatible with types
		// in the predicate definition.
		//System.out.println("I am now doing the type check");
		ArrayList<LSDBinding> ALBindings;
		if (bindings instanceof ArrayList)
			ALBindings = (ArrayList<LSDBinding>) bindings;
		else
			ALBindings = new ArrayList<LSDBinding>(bindings);
		if (!pred.typeChecks(ALBindings))
			throw new LSDInvalidTypeException();
		;
		this.predicate = pred;
		this.bindings = ALBindings;

	}
	public LSDLiteral nonNegatedCopy() {
		try {
			return new LSDLiteral(predicate, bindings, true);
		} catch (LSDInvalidTypeException e) {
			System.err.println("We're creating a non-negated copy of our valid self.  This can't happen..");
			System.exit(1);
			return null;
		}
	}
	public LSDLiteral negatedCopy() {
		try {
			return new LSDLiteral(predicate, bindings, false);
		} catch (LSDInvalidTypeException e) {
			System.err.println("We're creating a negated copy of our valid self.  This can't happen..");
			System.exit(1);
			return null;
		}
	}
	public String toString() {

		StringBuilder bs = new StringBuilder();
		for (int i = 0; i < bindings.size(); i++) {
			if (i >= 1) {
				bs.append( ",");
			}
			bs.append(bindings.get(i).toString());

		}
		return (nonNegated ? "" : "!") + this.predicate.getDisplayName() + "(" + bs.toString()
				+ ")";
	}

	public String toTyrubaString(Hashtable<LSDVariable, Integer> freeVarCount) {
		String output = "";
		for (int i = 0; i < bindings.size(); i++) {
			if (i >= 1) {
				output = output + ",";
			}
			output = output + bindings.get(i).toString();
		}
		output = this.predicate.getName() + "(" + output + ")";
		
		// if nonNegated add existential quantifiers and NOT()
		if (nonNegated) {
			boolean quantified = false;
			for (int i = 0; i < bindings.size(); i++) {
				LSDVariable var = bindings.get(i).getVariable();
				if (var != null && freeVarCount.get(var) == 1) {
					output = var.toString()
							+ (output.charAt(0) == '?' ? ", " : " : ") + output;
					quantified = true;
				}
			}
			if (quantified)
				output = "EXISTS " + output;
			output = "NOT(" + output + ")";
		}
		return output;
	}

	public LSDLiteral substitute(LSDVariable toReplace, LSDBinding replacement)
			throws LSDInvalidTypeException {
		// it returns a new copy in everycase (either fact or literal)
		ArrayList<LSDBinding> newbs = new ArrayList<LSDBinding>();
		boolean freeVariables = false;
		for (LSDBinding oldBinding : bindings) {
			LSDBinding nb = oldBinding.substitute(toReplace,
					replacement);
			newbs.add(nb);
			if (!nb.isBound())
				freeVariables = true;
		}
		if (freeVariables)
			return new LSDLiteral(this.predicate, newbs, this.nonNegated);
		else {
			List<String> binds = new ArrayList<String>();
			for (LSDBinding binding : newbs) {
				binds.add(binding.toString());
			}
			return LSDFact.createLSDFact(predicate, binds, this.nonNegated);
		}
	}

	public ArrayList<LSDVariable> freeVars() {
		ArrayList<LSDVariable> freeVars = new ArrayList<LSDVariable>();
		for (int i = 0; i < this.bindings.size(); i++) {
			if (!this.bindings.get(i).isBound())
				freeVars.add(this.bindings.get(i).getVariable());
		}
		return freeVars;
	}
	

	public boolean isNegated() {
		return !nonNegated;
	}
	
	public LSDPredicate getPredicate() {
		return predicate;
	}
	
	public List<LSDBinding> getBindings() {
		return new ArrayList<LSDBinding>(bindings);
	}
	
	public boolean equalsIgnoringNegation(Object other) {
		if (!(other instanceof LSDLiteral))
			return false;
		LSDLiteral otherLit = (LSDLiteral) other;
		if (!this.predicate.equalsIgnoringPrimes(otherLit.predicate))
			return false;
		if (this.bindings.size() != otherLit.bindings.size())
			return false;
		for (int i = 0; i < this.bindings.size(); i++){
			if (!this.bindings.get(i).equals(otherLit.bindings.get(i)))
				return false;
		}
		return true;
	}
	
	public boolean identifiesSameIgnoringNegation(Object other) {
		if (!(other instanceof LSDLiteral))
			return false;
		LSDLiteral otherLit = (LSDLiteral) other;
		if (!this.predicate.equalsIgnoringPrimes(otherLit.predicate))
			return false;
		List<List<LSDBinding>> thisBindingsLists  = this.getPrimaryBindings();
		List<List<LSDBinding>> otherBindingsLists = otherLit.getPrimaryBindings();
		
		if (thisBindingsLists.size() != otherBindingsLists.size())
			return false;
		boolean anyMatch = false;
		for (int i = 0; i < thisBindingsLists.size(); i++){
			boolean thisMatches = true;
			List<LSDBinding> thisBindings  = thisBindingsLists.get(i);
			List<LSDBinding> otherBindings = otherBindingsLists.get(i);
			if (thisBindings.size() != otherBindings.size())
				continue;
			for (int j = 0; j < thisBindings.size(); j++){
				if (!thisBindings.get(j).equals(otherBindings.get(j)))
					thisMatches = false;
			}
			if (thisMatches)
				anyMatch = true;
		}
		
		return anyMatch;
	}
		
	public boolean equals(Object other) {
		if (!(other instanceof LSDLiteral))
			return false;
		LSDLiteral otherLit = (LSDLiteral) other;
		if (this.nonNegated != otherLit.nonNegated)
			return false;
		return this.equalsIgnoringNegation(otherLit);
	}

	public static LSDLiteral createDefaultLiteral (LSDPredicate predicate, boolean nonNegated) {
		ArrayList<LSDBinding> bindings = new ArrayList<LSDBinding>();
		char types[] = predicate.getTypes();
		// create bindings with the variable names same as the types. 
		for (int i=0; i<types.length ; i++) { 
			String fvName = (types[i])+ ""+i;
			LSDVariable fv = new LSDVariable(fvName, types[i]);
			LSDBinding binding = new LSDBinding(fv);

			bindings.add(binding);
		}
		try {
			LSDLiteral literal = new LSDLiteral(predicate,bindings, nonNegated);
			return literal;
		}catch (LSDInvalidTypeException e) { 
			return null;
		}
	}
	
	// FIXME: Addition for refactoring support
	// Should be called hasSamePredicateSuffix?  Matches before_foo with added_p_foo.
	public boolean hasSamePred(LSDLiteral other) { 
		 return this.predicate.getSuffix().equals(other.predicate.getSuffix());
	}
	
	// FIXME:PREDICATE CONTENT DEPENT
	public List<List<LSDBinding>> getPrimaryBindings () {
		
		int[][] primaryArguments = this.predicate.getPrimaryArguments();
		List<List<LSDBinding>> primaryBindings = new ArrayList<List<LSDBinding>>();
		for (int[] argumentSet : primaryArguments) {
			List<LSDBinding> primaryBindingSet = new ArrayList<LSDBinding>();
				for (int argument : argumentSet) {
					assert argument < bindings.size();
					primaryBindingSet.add(bindings.get(argument));
				}
			primaryBindings.add(primaryBindingSet);
		}
		return primaryBindings;
	}
	public char[] getPrimaryTypes() {
		return this.getPredicate().getPrimaryTypes();
	}

	public static void main(String[] args) {

		LSDPredicate foo = LSDPredicate.getPredicate("added_inheritedMethod");
		ArrayList<LSDBinding> bindings = new ArrayList<LSDBinding>();
		LSDBinding binding = new LSDBinding(new LSDVariable("a", 'm'));
		bindings.add(binding);
		binding = new LSDBinding(new LSDVariable("b", 't'));
		bindings.add(binding);
		binding = new LSDBinding(new LSDVariable("c", 't'));
		bindings.add(binding);
		try {
			LSDLiteral bar = new LSDLiteral(foo, bindings, false);
			System.out.println(bar);
			bar = bar
					.substitute(new LSDVariable("a", 'm'), new LSDBinding("X"));
			bar = bar
					.substitute(new LSDVariable("b", 't'), new LSDBinding("X"));
			bar = bar
					.substitute(new LSDVariable("c", 't'), new LSDBinding("X"));
			bar.nonNegated = false;
			assert (bar instanceof LSDFact);
			System.out.println(bar);
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	//	System.out.println(LSDFact.createLSDFact(foo, " X,Y,   Z", true));
		System.out.println("Literal tests succeeded.");
	}
	
	
	public int getBindingScore(LSDBinding binding) {
		int i = 0;
		if (predicate.getSuffix().equalsIgnoreCase("type")){
			if (getLocation(binding) == 3)
				i++;
		}
		else if (predicate.getSuffix().equalsIgnoreCase("field")){
			if (getLocation(binding) == 3)
				i++;
		}
		else if (predicate.getSuffix().equalsIgnoreCase("method")){
			if (getLocation(binding) == 3)
				i++;
		}
		else if (predicate.getSuffix().equalsIgnoreCase("subtype")) 
		{
			i+=2;
			if (getLocation(binding) == 1)
				i++;
		}
		else if (predicate.getSuffix().equalsIgnoreCase("accesses"))
			i++;
		else if (predicate.getSuffix().equalsIgnoreCase("calls"))
			i++;
		else if (predicate.getSuffix().equalsIgnoreCase("dependency"))
			i+=2;
		return i;
	}
	private int getLocation(LSDBinding binding) {
		String literal = this.toString();
		literal = literal.substring(literal.indexOf("(")+1,literal.length()-1); 
		StringTokenizer tokenizer = new StringTokenizer(literal,",");
		int i = 1;
		String temp = null;
		while (tokenizer.hasMoreTokens()){
			temp = tokenizer.nextToken();
			if (!temp.startsWith("?"))
			{
				temp = temp.substring(1,temp.length()-1);
				if (binding.getGroundConst().equalsIgnoreCase(temp))
					return i;
			}
			i++;
		}
		return 0;
		
	}
	public boolean isConclusion() {
		return predicate.isConclusionPredicate();
	}
	
	public boolean isDependency() {
		return predicate.isDependencyPredicate();
	}
	
	public List<LSDLiteral> getCompatibles() {
		ArrayList<LSDLiteral> newliterals = new ArrayList<LSDLiteral>();
		for (LSDPredicate pred : this.predicate.getMethodLevelDependency()) {
			ArrayList<LSDBinding> bindings = new ArrayList<LSDBinding>();
			int i = 0;
			char[] types = pred.getTypes();
			for (LSDBinding oldBinding:getBindings()){
				bindings.add(new LSDBinding(new LSDVariable("t"+i,types[i])));
				i++;
			}
			pred.updateBindings(bindings);
			try {
				newliterals.add(new LSDLiteral(pred,bindings,!isNegated()));
			} catch (LSDInvalidTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return newliterals;
	}
	
}

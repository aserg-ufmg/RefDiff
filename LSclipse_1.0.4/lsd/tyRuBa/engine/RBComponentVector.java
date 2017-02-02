package tyRuBa.engine;

import java.util.ArrayList;
import java.util.Iterator;

import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;

/** This class was introduced into the TyRuBa impplementation
 to avoid long chains of nested RBComponents in case we are
 working with large collections of rules and facts.
*/

public class RBComponentVector {

	public ArrayList contents;

	/** Create an empty ComponentVector */
	public RBComponentVector() {
		super();
		contents = new ArrayList();
	}

	public void clear() {
		contents = new ArrayList();
//		this.update();
	}

	/** Create an empty ComponentVector, try to save some memory by predicting 
	 * its eventual size */
	public RBComponentVector(int predictedSize) {
		super();
		contents = new ArrayList(predictedSize);
	}

	/** Create a componentVector which is an "alias" of the given Vector */
	public RBComponentVector(ArrayList vect) {
		super();
		contents = vect;
	}

//	/** Unify this with other */
//	public ElementSource unify(RBTuple other, RBContext context) {
////		boolean dirty = false; 
//		CompositeElementSource result = new CompositeElementSource();
//		for (Iterator iter = contents.iterator(); iter.hasNext();) {
//			RBComponent element = (RBComponent) iter.next();
//			if (element.isValid())
//				result.add(element.unify(other, context));
//			else {
//				iter.remove();
////				dirty = true;
//			}
//		}
////		if (dirty)
////			update();
//		return result.simplify();
//	}

	/**Get rid of unnecesary RBComponentVector */
//	RuleBase simplify() {
//		if (contents.size() == 0)
//			return RBNullComponent.the;
//		else if (contents.size() == 1)
//			return (RuleBase) contents.firstElement();
//		else
//			return this;
//	}

	public void insert(RBComponent c) {
		if (c == null)
			throw new NullPointerException("Not allowed to insert null");
		contents.add(c);
//		update();
	}

	public String toString() {
		int len = contents.size();
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < len; i++) {
			result.append(contents.get(i) + "\n");
		}
		return result.toString();
	}

	public Compiled compile(CompilationContext context) {
		Compiled result = Compiled.fail;
		for (Iterator iter = iterator(); iter.hasNext();) {		
			RBComponent element = (RBComponent) iter.next();
			result = result.disjoin(element.compile(context));
		}
		return result;
	}

	private Iterator iterator() {
		return new Iterator() {
			int pos;
			
			{
				pos = 0;
				skipInvalids();
			}

			public boolean hasNext() {
				return pos<contents.size();
			}

			private void skipInvalids() {
				while (pos<contents.size() 
					&& !((RBComponent)contents.get(pos)).isValid())
					pos++;
			}

			public Object next() {
				Object result = contents.get(pos++);
				skipInvalids();
				return result;
			}

			public void remove() {
				throw new Error("This operation is not supported");
			}
		};
	}

}

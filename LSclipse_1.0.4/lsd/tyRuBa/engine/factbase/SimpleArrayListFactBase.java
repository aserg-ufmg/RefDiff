/*
 * Created on May 5, 2004
 */
package tyRuBa.engine.factbase;

import java.util.ArrayList;
import java.util.Iterator;

import tyRuBa.engine.Frame;
import tyRuBa.engine.RBComponent;
import tyRuBa.engine.RBContext;
import tyRuBa.engine.RBTuple;
import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.engine.compilation.SemiDetCompiled;
import tyRuBa.modes.Multiplicity;
import tyRuBa.modes.PredInfo;
import tyRuBa.modes.PredicateMode;
import tyRuBa.util.Action;
import tyRuBa.util.ArrayListSource;
import tyRuBa.util.ElementSource;

/**
 * A FactBase that uses an ArrayList to store facts. Inefficient, but simple.
 * @category FactBase
 * @author riecken
 */
public class SimpleArrayListFactBase extends FactBase {

    /** All of the facts in this FactBase. */
    ArrayList facts = new ArrayList();

    /**
     * Creates a new SimpleArrayListFactBase.
     */
    public SimpleArrayListFactBase(PredInfo info) {
    }

    /**
     * @see tyRuBa.engine.factbase.FactBase#isEmpty()
     */
    public boolean isEmpty() {
        return facts.isEmpty();
    }

    /**
     * @see tyRuBa.engine.factbase.FactBase#isPersistent()
     */
    public boolean isPersistent() {
        return false;
    }

    /**
     * @see tyRuBa.engine.factbase.FactBase#insert(tyRuBa.engine.RBComponent)
     */
    public void insert(RBComponent f) {
        facts.add(f);
    }

    /**
     * @see tyRuBa.engine.factbase.FactBase#compile(tyRuBa.modes.PredicateMode,
     * tyRuBa.engine.compilation.CompilationContext)
     */
    public Compiled basicCompile(PredicateMode mode, CompilationContext context) {
        if (mode.getMode().hi.compareTo(Multiplicity.one) <= 0) {
            return new SemiDetCompiled(mode.getMode()) {

                public Frame runSemiDet(Object input, RBContext context) {
                    final RBTuple goal = (RBTuple) input;
                    Frame result = null;
                    for (Iterator iter = facts.iterator(); result == null && iter.hasNext();) {
                        RBComponent fact = (RBComponent) iter.next();
                        if (!fact.isValid()) {
                            iter.remove();
                        } else
                            result = goal.unify(fact.getArgs(), new Frame());
                    }
                    return result;
                }

            };
        } else {
            return new Compiled(mode.getMode()) {

                public ElementSource runNonDet(Object input, RBContext context) {
                    final RBTuple goal = (RBTuple) input;
                    return new ArrayListSource(facts).map(new Action() {

                        public Object compute(Object arg) {
                            RBComponent fact = (RBComponent) arg;
                            if (!fact.isValid())
                                return null;
                            return goal.unify(fact.getArgs(), new Frame());
                        }
                    });
                }

            };
        }
    }

    /**
     * @see tyRuBa.engine.factbase.FactBase#backup()
     */
    public void backup() {
        //this factbase is not persistent
    }
}
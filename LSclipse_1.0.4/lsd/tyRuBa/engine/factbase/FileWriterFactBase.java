/*
 * Created on Jun 9, 2004
 */
package tyRuBa.engine.factbase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import tyRuBa.engine.PredicateIdentifier;
import tyRuBa.engine.RBComponent;
import tyRuBa.engine.RBCompoundTerm;
import tyRuBa.engine.RBTerm;
import tyRuBa.engine.RBTuple;
import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.modes.PredicateMode;

/**
 * Wrapper around another FactBase that records what is inserted to a file.
 * @category FactBase
 * @author riecken
 */
public class FileWriterFactBase extends FactBase {

    /** Predicate that this FactBase is for. */
    private String predicateName;

    /** Wrapped FactBase. */
    private FactBase containedFactBase;

    /** PrintWriter used to write out the file. */
    private static PrintWriter pw;

    /**
     * Creates a new FileWriterFactBase.
     * @param pid predicate that this FactBase is for.
     * @param fb FactBase to wrap around.
     * @param f file to store the logged inserts into.
     */
    public FileWriterFactBase(PredicateIdentifier pid, FactBase fb, File f) {
        predicateName = pid.getName();
        this.containedFactBase = fb;
        try {
            if (pw == null) {
                pw = new PrintWriter(new FileOutputStream(f));
            }
            ;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * @see tyRuBa.engine.factbase.FactBase#isEmpty()
     */
    public boolean isEmpty() {
        return containedFactBase.isEmpty();
    }

    /**
     * @see tyRuBa.engine.factbase.FactBase#isPersistent()
     */
    public boolean isPersistent() {
        return containedFactBase.isPersistent();
    }

    /**
     * @see tyRuBa.engine.factbase.FactBase#insert(tyRuBa.engine.RBComponent)
     */
    public synchronized void insert(RBComponent f) {
        if (f.isGroundFact()) {
            pw.print(predicateName + "(");
            RBTuple args = f.getArgs();
            printTuple(args);
            pw.println(").");
            containedFactBase.insert(f);
        }
    }

    /**
     * Prints a RBTuple to the log file.
     * @param args Tuple to log.
     */
    private void printTuple(RBTuple args) {
        for (int i = 0; i < args.getNumSubterms(); i++) {
            RBTerm subterm = args.getSubterm(i);
            if (i > 0) {
                pw.print(", ");
            }
            if (subterm instanceof RBCompoundTerm) {
                RBCompoundTerm compterm = (RBCompoundTerm) subterm;
                pw.print(compterm.getConstructorType().getFunctorId().getName() + "<");
                RBTerm[] terms = new RBTerm[compterm.getNumArgs()];
                for (int j = 0; j < compterm.getNumArgs(); j++) {
                    terms[j] = compterm.getArg(j);
                }
                printTuple(RBTuple.make(terms));
                pw.print(">");
            }
        }
    }

    /**
     * @see tyRuBa.engine.factbase.FactBase#compile(tyRuBa.modes.PredicateMode,
     * tyRuBa.engine.compilation.CompilationContext)
     */
    public Compiled basicCompile(PredicateMode mode, CompilationContext context) {
        pw.flush();
        return containedFactBase.compile(mode, context);

    }

    /**
     * @see tyRuBa.engine.factbase.FactBase#backup()
     */
    public void backup() {
        containedFactBase.backup();
    }

}
/*
 * Created on Jul 8, 2004
 */
package tyRuBa.engine.factbase;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;

import tyRuBa.engine.Frame;
import tyRuBa.engine.PredicateIdentifier;
import tyRuBa.engine.QueryEngine;
import tyRuBa.engine.RBContext;
import tyRuBa.engine.RBTuple;
import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.engine.compilation.SemiDetCompiled;
import tyRuBa.engine.factbase.hashtable.Index;
import tyRuBa.engine.factbase.hashtable.URLFactLibrary;
import tyRuBa.modes.Multiplicity;
import tyRuBa.modes.PredicateMode;
import tyRuBa.util.Action;
import tyRuBa.util.ElementSource;

/**
 * A FactLibraryManager manages the URLFactLibraries for a QueryEngine.
 * @category FactBase
 * @author riecken
 */
public class FactLibraryManager {

    /** The libraries. Indexed by base URL. */
    private HashMap libraries;

    /** The query engine this FactLibraryManager manages libraries for. */
    private QueryEngine qe;

    /** Creates a new FactLibraryManager. */
    public FactLibraryManager(QueryEngine qe) {
        this.qe = qe;
        this.removeAll();
    }

    /**
     * Adds a fact library for the specified jar file.
     * @param jarFileLocation the location of the jar file.
     */
    public void addLibraryJarFile(String jarFileLocation) {
        if (!libraries.containsKey(jarFileLocation)) {
            File jarFile = new File(jarFileLocation);
            try {
                libraries.put(jarFileLocation, new URLFactLibrary("jar:" + jarFile.toURL() + "!/", qe));
            } catch (MalformedURLException e) {
                throw new Error("Jar file location is not a valid location (it can't be turned into a URL)");
            }
        }
    }

    /**
     * Adds a fact library located at the specified base URL location.
     * @param baseURL the base location of the fact library.
     */
    public void addLibraryURLLocation(String baseURL) {
        if (!libraries.containsKey(baseURL)) {
            libraries.put(baseURL, new URLFactLibrary(baseURL, qe));
        }
    }

    /**
     * Retrieves a fact library for a given location.
     * @param location location of the library.
     */
    public URLFactLibrary getLibrary(String location) {
        return (URLFactLibrary) libraries.get(location);
    }

    /**
     * Removes a fact library from the system.
     * @param location location of the library.
     */
    public void removeLibrary(String location) {
        libraries.remove(location);
    }

    /** Removes all fact libraries. */
    public void removeAll() {
        libraries = new HashMap();
        //Quick and dirty to test something:
//        addLibraryURLLocation("file:///Users/kdvolder/Desktop/eclipse/eclipse-facts/");
    }

    /**
     * Compiles the fact libraries for a given predicate and mode.
     * @param pm the mode this compilation is for.
     * @param predId the predicate in the library to compile for.
     * @param context compilation context
     */
    public Compiled compile(PredicateMode pm, PredicateIdentifier predId, CompilationContext context) {
        if (libraries.size() == 0) {
            return Compiled.fail; //no libraries, no data.
        }

        final Index[] indexes = new Index[libraries.size()];
        int i = 0;
        for (Iterator iter = libraries.values().iterator(); iter.hasNext();) {
            URLFactLibrary element = (URLFactLibrary) iter.next();
            indexes[i] = element.getIndex(predId.getName(), predId.getArity(), pm);
            i++;
        }

        if (pm.getMode().hi.compareTo(Multiplicity.one) <= 0) {
            if (pm.getParamModes().getNumFree() != 0) {
                // Case 1: SemiDet and NOT all bound
                return new SemiDetCompiled(pm.getMode()) {

                    public Frame runSemiDet(Object input, RBContext context) {
                        final RBTuple goal = (RBTuple) input;
                        final RBTuple inputPars;
                        final RBTuple outputPars;
                        inputPars = indexes[0].extractBound(goal);
                        outputPars = indexes[0].extractFree(goal);
                        for (int i = 0; i < indexes.length; i++) {
                            RBTuple retrieved = indexes[i].getMatchSingle(inputPars);
                            if (retrieved != null) {
                                return retrieved.unify(outputPars, new Frame());
                            }
                        }
                        return null;
                    }
                };
            } else {
                // Case 2: SemiDet and all bound
                return new SemiDetCompiled(pm.getMode()) {

                    public Frame runSemiDet(Object input, RBContext context) {
                        final RBTuple goal = (RBTuple) input;
                        for (int i = 0; i < indexes.length; i++) {
                            RBTuple retrieved = indexes[i].getMatchSingle(goal);
                            if (retrieved != null) {
                                return new Frame();
                            }
                        }
                        return null;
                    }
                };
            }
        } else {
            if (pm.getParamModes().getNumFree() != 0) {
                //CASE 3: NonDet and NOT all bound
                return new Compiled(pm.getMode()) {

                    public ElementSource runNonDet(Object input, RBContext context) {
                        final RBTuple goal = (RBTuple) input;
                        final RBTuple inputPars;
                        final RBTuple outputPars;
                        inputPars = indexes[0].extractBound(goal);
                        outputPars = indexes[0].extractFree(goal);

                        ElementSource matches = indexes[0].getMatchElementSource(inputPars);
                        for (int i = 1; i < indexes.length; i++) {
                            matches = matches.append(indexes[i].getMatchElementSource(inputPars));
                        }
                        return matches.map(new Action() {

                            public Object compute(Object arg) {
                                RBTuple retrieved = (RBTuple) arg;
                                return retrieved.unify(outputPars, new Frame());
                            }
                        });
                    }
                };
            } else {
                //CASE 4: NonDet and all bound
                throw new Error("This case should not happen");
                // why not? all bound => SemiDet
            }
        }
    }

}
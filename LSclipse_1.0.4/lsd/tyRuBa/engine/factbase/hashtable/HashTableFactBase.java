/*
 * Created on Jun 9, 2004
 */
package tyRuBa.engine.factbase.hashtable;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.Assert;

import tyRuBa.engine.Frame;
import tyRuBa.engine.QueryEngine;
import tyRuBa.engine.RBComponent;
import tyRuBa.engine.RBContext;
import tyRuBa.engine.RBTuple;
import tyRuBa.engine.Validator;
import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.engine.compilation.SemiDetCompiled;
import tyRuBa.engine.factbase.FactBase;
import tyRuBa.modes.BindingList;
import tyRuBa.modes.Factory;
import tyRuBa.modes.Mode;
import tyRuBa.modes.Multiplicity;
import tyRuBa.modes.PredInfo;
import tyRuBa.modes.PredicateMode;
import tyRuBa.util.Action;
import tyRuBa.util.ElementSource;
import tyRuBa.util.pager.FileLocation;

/**
 * This FactBase implementation uses hash maps as the backing storage. Indexes
 * (hash tables indexed on the "bound" part of the fact) are created for each
 * declared predicate mode. These indexes are persisted by a paging mechanism
 * used by the global Pager. The paging mechanism also ensures that a reasonable
 * amount of data is stored in each index (see cache size in front end). New
 * indexes can be created on the fly, but it is not recommended as it will be
 * slow. It is best to declare all modes that will be used and have the overhead
 * occur during insertion rather than querying.
 * @category FactBase
 * @author riecken
 */
public class HashTableFactBase extends FactBase {

    /** The indexes on the facts. */
    private Map indexes;

    /** The arity of the predicate that this FactBase is for. */
    private int arity;

    /** The name of the predicate that this FactBase is for. */
    private String name;

    /** The "all free" index. It is special as it is used to create indexes on the fly. */
    private Index allFreeIndex;

    /** The QueryEngine that this FactBase resides in. */
    private QueryEngine engine;

    /** Whether this FactBase is empty. */
    private boolean isEmpty = true;

    /** Where this FactBase is persisted to. */
    private String storageLocation;

    /** Creates a new HashTableFactBase. */
    public HashTableFactBase(PredInfo info) {
        arity = info.getArity();
        name = info.getPredId().getName();
        engine = info.getQueryEngine();
        storageLocation = engine.getStoragePath() + "/" + engine.getFrontendNamePersistenceManager().getPersistentName(name) + "/" + arity + "/";
        initIndexes(info);
    }

    /**
     * Initializes the indexes. Indexes are always created for the all bound
     * mode and all free mode. In addition, any other modes defined in the
     * predicate declaration have indexes created for them. Any other modes will
     * have their indexes created on the fly.
     */
    private void initIndexes(PredInfo info) {
        indexes = new HashMap();

        //all free is special (used for creating new indexes)
        BindingList allFree = Factory.makeBindingList(arity, Factory.makeFree());
        PredicateMode freeMode = new PredicateMode(allFree, new Mode(Multiplicity.zero, Multiplicity.many), false);
        allFreeIndex = new Index(freeMode, new FileLocation(storageLocation + "/"
                + freeMode.getParamModes().getBFString() + "/"), engine, name + "/" + arity);
        indexes.put(freeMode.getParamModes(), allFreeIndex);

        //always want all bound NOTE: ***all bound and all free share the same
        // index***
        BindingList allBound = Factory.makeBindingList(arity, Factory.makeBound());
        PredicateMode boundMode = new PredicateMode(allBound, new Mode(Multiplicity.zero, Multiplicity.one), false);
        indexes.put(boundMode.getParamModes(), allFreeIndex);

        //At least make the indexes for modes that are defined..
        for (int i = 0; i < info.getNumPredicateMode(); i++) {
            PredicateMode pm = info.getPredicateModeAt(i);
            BindingList paramModes = pm.getParamModes();
            if (new File(storageLocation + "/" + paramModes.getBFString()).exists()) {
                isEmpty = false;
            }
            if (!(paramModes.getNumFree() == arity) && !(paramModes.getNumBound() == arity)) {
                indexes.put(pm.getParamModes(), new Index(pm, new FileLocation(storageLocation + "/"
                        + pm.getParamModes().getBFString() + "/"), engine, name + "/" + arity));
            }
        }

        //Try to reconnect to indexes if they're around
        int numIndexes = (int) (Math.pow(2, arity));
        for (int i = 0; i < numIndexes; i++) {
            BindingList blist = Factory.makeBindingList();
            int checkNum = 1;
            for (int j = 0; j < arity; j++) {
                if ((i & checkNum) == 0) {
                    blist.add(Factory.makeBound());
                } else {
                    blist.add(Factory.makeFree());
                }
                checkNum *= 2;
            }

            if (!(blist.getNumBound() == 0 || blist.getNumFree() == 0) && !indexes.containsKey(blist)) {
                if (new File(storageLocation + "/" + blist.getBFString()).exists()) {
                    isEmpty = false;
                    PredicateMode mode = new PredicateMode(blist, new Mode(Multiplicity.zero, Multiplicity.many), false);
                    Index idx = new Index(mode, new FileLocation(storageLocation + "/"
                            + mode.getParamModes().getBFString() + "/"), engine, name + "/" + arity);
                    indexes.put(mode.getParamModes(), idx);
                }
            }
        }

    }

    /**
     * Gets an index for a given mode.
     * @param mode mode to get the index for.
     */
    private Index getIndex(PredicateMode mode) {
        Index index = (Index) indexes.get(mode.getParamModes());
        if (index == null) {
            index = makeIndex(mode);
            indexes.put(mode.getParamModes(), index);
        }

        return index;
    }

    /**
     * Creates an index on the fly. 
     * WARNING: Creating indexes on the fly may be
     * very slow. This is because we need to load *all* of the facts from the
     * "allFree" index into memory, possibly doing many disk pages during the
     * process. Depending on how large the data set is, it may take several
     * minutes to create a new index. It is best if all modes that will be used
     * are defined in the rules files so that indexes will always be created for
     * them.
     */
    private Index makeIndex(PredicateMode mode) {
        Index index;

        index = new Index(mode, new FileLocation(storageLocation + "/" + mode.getParamModes().getBFString() + "/"),
                engine, name + "/" + arity);
        for (ElementSource iter = allFreeIndex.values(); iter.hasMoreElements();) {
            IndexValue fact = (IndexValue) iter.nextElement();
            index.addFact(fact);
        }
        return index;
    }

    /**
     * @see tyRuBa.engine.factbase.FactBase#isEmpty()
     */
    public boolean isEmpty() {
        return isEmpty;
    }

    /**
     * @see tyRuBa.engine.factbase.FactBase#isPersistent()
     */
    public boolean isPersistent() {
        return true;
    }

    /**
     * @see tyRuBa.engine.factbase.FactBase#insert(tyRuBa.engine.RBComponent)
     */
    public void insert(RBComponent f) {
        Assert.assertTrue("Only ground facts should be insterted in to FactBases", f.isGroundFact());
        isEmpty = false;

        Validator v = f.getValidator();

        for (Iterator iter = indexes.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            BindingList key = (BindingList) entry.getKey();
            if (key.getNumFree() != 0) { //skip for all bound
                Index index = (Index) entry.getValue();
                index.addFact(IndexValue.make(v, f.getArgs()));
            }
        }
    }

    /**
     * @see tyRuBa.engine.factbase.FactBase#compile(tyRuBa.modes.PredicateMode,
     * tyRuBa.engine.compilation.CompilationContext)
     */
    public Compiled basicCompile(final PredicateMode mode, CompilationContext context) {
        final Index index = getIndex(mode);

        if (mode.getMode().hi.compareTo(Multiplicity.one) <= 0) {
            if (mode.getParamModes().getNumFree() != 0) {
                // Case 1: SemiDet and NOT all bound
                return new SemiDetCompiled(mode.getMode()) {

                    public Frame runSemiDet(Object input, RBContext context) {
                        final RBTuple goal = (RBTuple) input;
                        final RBTuple inputPars;
                        final RBTuple outputPars;
                        inputPars = index.extractBound(goal);
                        outputPars = index.extractFree(goal);
                        RBTuple retrieved = index.getMatchSingle(inputPars);
                        if (retrieved == null) {
                            return null;
                        } else {
                            return retrieved.unify(outputPars, new Frame());
                        }
                    }
                };
            } else {
                // Case 2: SemiDet and all bound
                return new SemiDetCompiled(mode.getMode()) {

                    public Frame runSemiDet(Object input, RBContext context) {
                        final RBTuple goal = (RBTuple) input;
                        RBTuple retrieved = index.getMatchSingle(goal);
                        if (retrieved == null) {
                            return null;
                        } else {
                            return new Frame();
                        }
                    }
                };
            }
        } else {
            if (mode.getParamModes().getNumFree() != 0) {
                //CASE 3: NonDet and NOT all bound
                return new Compiled(mode.getMode()) {

                    public ElementSource runNonDet(Object input, RBContext context) {
                        final RBTuple goal = (RBTuple) input;
                        final RBTuple inputPars;
                        final RBTuple outputPars;
                        inputPars = index.extractBound(goal);
                        outputPars = index.extractFree(goal);
                        ElementSource matches = index.getMatchElementSource(inputPars);
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

    /**
     * @see tyRuBa.engine.factbase.FactBase#backup()
     */
    public void backup() {
        for (Iterator iter = indexes.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            BindingList key = (BindingList) entry.getKey();
            if (key.getNumFree() != 0) { //skip for all bound
                Index idx = (Index) entry.getValue();
                idx.backup();
            }
        }
    }
}
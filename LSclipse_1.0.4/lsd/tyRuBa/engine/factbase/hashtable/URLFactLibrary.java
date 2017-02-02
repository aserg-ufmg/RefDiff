/*
 * Created on Jul 8, 2004
 */
package tyRuBa.engine.factbase.hashtable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import junit.framework.Assert;

import tyRuBa.engine.QueryEngine;
import tyRuBa.engine.factbase.FileBasedValidatorManager;
import tyRuBa.engine.factbase.NamePersistenceManager;
import tyRuBa.engine.factbase.ValidatorManager;
import tyRuBa.modes.BindingList;
import tyRuBa.modes.Factory;
import tyRuBa.modes.PredicateMode;
import tyRuBa.util.pager.URLLocation;

/**
 * A URLFactLibrary is a collection of facts that are stored at some location.
 * They can be accessed and queried, but not modified.
 * @category FactBase
 * @author riecken
 */
public class URLFactLibrary {

    /** The base URL of the library. */
    private String baseURL;

    /** Map of indexes in the library. */
    private HashMap indexes;

    /** Manager to get filenames from a specified name. */
    private NamePersistenceManager nameManager;

    /** Manager to validate the facts. */
    private ValidatorManager validatorManager;

    /** Query engine that this library resides in. */
    private QueryEngine engine;

    /**
     * Creates a new URLFactLibrary.
     */
    public URLFactLibrary(String baseURL, QueryEngine qe) {
        indexes = new HashMap();
        this.baseURL = baseURL;
        this.engine = qe;
        try {
            nameManager = new NamePersistenceManager(new URL(baseURL + "names.data"));
            Assert.assertNotNull(nameManager);
            validatorManager = new FileBasedValidatorManager(new URL(baseURL + "validators.data"));
            Assert.assertNotNull(validatorManager);
        } catch (MalformedURLException e) {
            throw new Error("Library file does not exist");
        }

    }

    /**
     * Retrieves an index for a given mode of a predicate.
     * @param predicateName name of the predicate that the index is for.
     * @param arity arity of the predicate.
     * @param mode mode of the predicate.
     */
    public Index getIndex(String predicateName, int arity, PredicateMode mode) {

        BindingList bl = mode.getParamModes();

        if (bl.getNumFree() == 0) {
            bl = Factory.makeBindingList(arity, Factory.makeFree());
        }

        Index result = (Index) indexes.get(predicateName + arity + bl.getBFString());

        try {
            if (result == null) {
                result = new Index(mode, new URLLocation(baseURL + nameManager.getPersistentName(predicateName) + "/"
                        + arity + "/" + bl.getBFString() + "/"), engine, predicateName + "/" + arity + "/"
                        + bl.getBFString(), nameManager, validatorManager);
            }
        } catch (MalformedURLException e) {
            throw new Error("Malformed URL for Fact Library index: " + predicateName + "/" + arity + "/"
                    + bl.getBFString());
        }
        return result;
    }
}
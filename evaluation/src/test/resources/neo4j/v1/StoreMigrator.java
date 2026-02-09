package org.neo4j.kernel.impl.storemigration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Migrates a neo4j kernel database from one version to the next.
 * <p>
 * Since only one store migration is supported at any given version (migration
 * from the previous store version)
 * the migration code is specific for the current upgrade and changes with each
 * store format version.
 * <p>
 * Just one out of many potential participants in a {@link StoreUpgrader
 * migration}.
 *
 * @see StoreUpgrader
 */
public class StoreMigrator implements StoreMigrationParticipant {
  private static final String UTF8 = Charsets.UTF_8.name();

  private Map<Integer, Integer> dedupAndWritePropertyKeyTokenStore(
      PropertyStore propertyStore, Token[] tokens /* ordered ASC */ ) {
    PropertyKeyTokenStore keyTokenStore = propertyStore.getPropertyKeyTokenStore();
    Map<Integer/* duplicate */, Integer/* use this instead */> translations = new HashMap<>();
    return translations;
  }
}
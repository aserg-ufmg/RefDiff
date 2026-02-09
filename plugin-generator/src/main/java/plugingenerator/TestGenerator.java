package plugingenerator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map;

/**
 * Parses an NDJSON (Newline Delimited JSON) formatted string and generates test cases.
 */
public class TestGenerator {

  /**
   * Parses an NDJSON string and generates test cases.
   *
   * @param ndjsonString The NDJSON-formatted string to parse.
   */
  public static void generate(String ndjsonString) {
    String[] lines = ndjsonString.split("\n");

    for (String line : lines) {
      if (line.trim().isEmpty()) {
        continue; // Skip empty lines
      }

      // Parse to validate JSON structure but do not print to stdout.
      JsonObject jsonObject = JsonParser.parseString(line).getAsJsonObject();
    }
  }
}
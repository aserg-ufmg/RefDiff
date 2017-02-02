package lsd.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lsd.rule.*;

public class LSDTyrubaFactReader {

	private ArrayList<LSDFact> facts = null;

	public LSDTyrubaFactReader(File inputFile) {
		ArrayList<LSDFact> fs = new ArrayList<LSDFact>();
		try {
			if (inputFile.exists()) {
				BufferedReader in = new BufferedReader(
						new FileReader(inputFile));
				String line = null;
				while ((line = in.readLine()) != null) {
					if (line.trim().equals("") || line.trim().charAt(0) == '#'
							|| line.trim().startsWith("//"))
						continue;
					LSDFact fact = parseTyrubaFact(line);
					fs.add(fact);
				}
				in.close();
			}
			this.facts = fs;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<LSDFact> convertToClassLevel(
			ArrayList<LSDFact> readDeltaFacts) {

		LSDFact tempFact;
		ArrayList<LSDFact> facts = new ArrayList<LSDFact>();
		for (LSDFact fact : readDeltaFacts) {
			if (fact.getPredicate().isMethodLevel()) {
				tempFact = fact.convertToClassLevel();
				if (tempFact == null)
					continue;
				// if (!facts.contains(tempFact))
				facts.add(tempFact);
			} else
				facts.add(fact);
		}
		return facts;
	}

	public ArrayList<LSDFact> getFacts() {
		return facts;
	}

	public static LSDFact parseTyrubaFact(String line) {
		//System.out.println("Line being parse: " + line);//Niki's edit
		String factString = line.trim();
		// predicate '(' args ')''.'
		String predicateName = factString.substring(0, factString.indexOf('('))
				.trim();
		LSDPredicate predicate = LSDPredicate.getPredicate(predicateName);
		factString = factString.substring(factString.indexOf('(') + 1).trim();
		int endOfArgs = factString.lastIndexOf(')');
		String arguments = factString.substring(0, endOfArgs).trim();
		factString = factString.substring(endOfArgs + 1).trim();
		if (!factString.equals(".")) {
			System.err
					.println("Facts must be in the form 'predicate(const, const, ...).'");
			System.err.println("Line: " + line);
			System.exit(-3);
		}

		if (predicate == null) {
			System.err.println("Predicate " + predicateName
					+ " is not defined.");
			System.err.println("Line: " + line);
			System.exit(-1);
		}
		String[] params = arguments.split("\", \"");
		List<String> binds = new ArrayList<String>();
		for (String p : params) {
			if (p.startsWith("\"")) {
				binds.add(p.substring(1));
			} else if (p.endsWith("\"")) {
				binds.add(p.substring(0, p.length() - 2));
			} else {
				binds.add(p);
			}
		}
		return LSDFact.createLSDFact(predicate, binds, true);
	}
}

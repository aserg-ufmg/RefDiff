package refdiff.evaluation.icse;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MaybeFpCsvReader {
	public static void main(String[] args) throws IOException {
		Map<String, MaybeFpRow> map = readMaybeFp();
		
		try (BufferedReader br = new BufferedReader(new FileReader("data/java-evaluation/out3.txt"))) {
			String line;
			String commitUrl = "";
			while ((line = br.readLine()) != null) {
				String[] parts = line.split("\t", -1);
				String c0 = parts[0];
				if (c0.startsWith("https://")) {
					commitUrl = c0;
					System.out.printf("%s\n", commitUrl);
				} else {
					String refType = parts[0];
					String n1 = parts[1];
					String n2 = parts[2];
					String desc = parts[3];
					String result = parts[4];
					System.out.printf("%s\t%s\t%s\t%s\t%s", refType, n1, n2, desc, result);
					
					String key = commitUrl + " " + desc;
					MaybeFpRow row = map.get(key);
						if (row != null) {
							
							String fResult = row.resultA.equals(row.resultB) ? row.resultA : "FP?";
							System.out.printf("\t%s\t%s\t%s\t%s\t%s\t%s\t%s", row.resultA, row.commentA, row.resultB, row.commentB, fResult, row.resultC, row.commentC);
							
							/*
							boolean extractGetterSetter = refType.equals("Extract Method") && (n2.contains(".get") || n2.contains(".set")); 
							boolean inlineGetterSetter = refType.equals("Inline Method") && (n1.contains(".get") || n1.contains(".set"));
							if (extractGetterSetter) {
								System.out.print("\t\tExtract getter/setter");
							} else if (inlineGetterSetter) {
								System.out.print("\t\tInline getter/setter");
							}
							*/
						} else {
							System.out.printf("\t\t\t\t\t%s", result);
						}
					System.out.println();
				}
			}
		}
		
	}

	private static Map<String, MaybeFpRow> readMaybeFp() throws IOException, FileNotFoundException {
		Map<String, MaybeFpRow> map = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader("data/java-evaluation/maybe-fp.txt"))) {
			String line;
			String commitUrl = "";
			while ((line = br.readLine()) != null) {
				String[] parts = line.split("\t", -1);
				String c0 = parts[0];
				if (c0.startsWith("https://")) {
					commitUrl = c0;
				} else {
					//String refType = parts[0];
					//String n1 = parts[1];
					//String n2 = parts[2];
					String desc = parts[3];
					String result = parts[4];
					if (result.equals("FP?")) {
						MaybeFpRow row = new MaybeFpRow();
						row.commitUrl = commitUrl;
						row.description = desc;
						row.resultA = parts[5];
						row.commentA = parts[6];
						row.resultB = parts[7];
						row.commentB = parts[8];
						row.resultC = parts[10];
						row.commentC = parts[11];
						map.put(row.commitUrl + " " + row.description, row);
					}
				}
			}
		}
		return map;
	}
	
	public static class MaybeFpRow {
		public String commitUrl;
		public String description;
		public String resultA;
		public String commentA;
		public String resultB;
		public String commentB;
		public String resultC;
		public String commentC;
		
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return String.format("%s\t%s\t%s\t%s\t%s\t%s", commitUrl, description, resultA, commentA, resultB, commentB);
		}
	}
	
}

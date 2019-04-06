package refdiff.evaluation.icse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CsvReader {
	
	public static <T> List<T> readCsv(String path, Function<String[], T> rowReader) {
		List<T> list = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (!line.trim().isEmpty()) {
					String[] parts = line.split("\t", -1);
					list.add(rowReader.apply(parts));
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return list;
	}
	
}

package refdiff.evaluation.icse;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CsvReader {
	
	public static <T> List<T> readCsv(String path, Function<String[], T> rowReader) {
		List<T> list = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
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

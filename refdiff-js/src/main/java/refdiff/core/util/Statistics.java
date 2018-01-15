package refdiff.core.util;

import java.util.List;

public class Statistics {
	
	public static double median(List<? extends Number> values) {
		return median(values, 0, values.size());
	}
	
	public static double q1(List<? extends Number> values) {
		int length = values.size();
		if (length == 0) {
			throw new IllegalArgumentException("There are no values");
		} else {
			return median(values, 0, length / 2);
		}
	}
	
	public static double q3(List<? extends Number> values) {
		int length = values.size();
		if (length == 0) {
			throw new IllegalArgumentException("There are no values");
		} else if (length % 2 == 0) {
			return median(values, length / 2, length);
		} else {
			return median(values, (length / 2) + 1, length);
		}
	}
	
	private static double median(List<? extends Number> values, int from, int to) {
		int length = to - from;
		if (length <= 0) {
			throw new IllegalArgumentException("There are no values");
		} else if (length % 2 == 0) {
			int i1 = from + (length / 2) - 1;
			int i2 = from + (length / 2);
			return (values.get(i1).doubleValue() + values.get(i2).doubleValue()) / 2;
		} else {
			int i = from + length / 2;
			return values.get(i).doubleValue();
		}
	}
	
	public static double min(List<? extends Number> values) {
		int length = values.size();
		if (length == 0) {
			throw new IllegalArgumentException("There are no values");
		} else {
			return values.get(0).doubleValue();
		}
	}
	
	public static double max(List<? extends Number> values) {
		int length = values.size();
		if (length == 0) {
			throw new IllegalArgumentException("There are no values");
		} else {
			return values.get(values.size() - 1).doubleValue();
		}
	}
}

package refdiff.core.diff;

import java.util.ArrayList;

import refdiff.core.util.Statistics;

public class ThresholdsProvider {
	private double value = 0.5;
	
	public double getValue() {
		return value;
	}

	public void adjustTo(ArrayList<Double> similaritySame) {
		if (similaritySame.size() > 1) {
			double q1 = Statistics.min(similaritySame);
			//value = Math.min(value, q1);
		}
	}
	
}

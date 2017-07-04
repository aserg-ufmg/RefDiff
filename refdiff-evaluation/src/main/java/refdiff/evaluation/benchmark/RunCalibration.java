package refdiff.evaluation.benchmark;

import refdiff.evaluation.TestWithBenchmark;

public class RunCalibration {

	public static void main(String[] args) {
		new TestWithBenchmark(new CalibrationDataset()).calibrate();
	}

}

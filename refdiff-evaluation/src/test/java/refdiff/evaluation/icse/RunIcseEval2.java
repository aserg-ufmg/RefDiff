package refdiff.evaluation.icse;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import refdiff.core.diff.CstComparator;
import refdiff.evaluation.EvaluationUtils;
import refdiff.evaluation.KeyPair;
import refdiff.evaluation.RefactoringSet;
import refdiff.evaluation.RefactoringType;
import refdiff.evaluation.ResultComparator;
import refdiff.parsers.java.JavaPlugin;

public class RunIcseEval2 {
	
	public static EnumSet<RefactoringType> refactoringTypes = EnumSet.complementOf(EnumSet.of(RefactoringType.PULL_UP_ATTRIBUTE, RefactoringType.PUSH_DOWN_ATTRIBUTE, RefactoringType.MOVE_ATTRIBUTE));
	private EvaluationUtils evalUtils;
	
	public RunIcseEval2(String tempFolder) {
		evalUtils = new EvaluationUtils(new CstComparator(new JavaPlugin()), tempFolder);
	}

	public static void main(String[] args) throws Exception {
		new RunIcseEval2(args.length > 0 ? args[0] : "C:/refdiff/").run();
	}
	
	public void run() throws Exception {
		IcseDataset data = new IcseDataset();
		List<RefactoringSet> expected = data.getExpected();
		
		ResultComparator rc = EvaluationCsvReader.buildResultComparator(data, EvaluationCsvReader.readEvalAll());
		
		Set<String> whitelist = new HashSet<>(Arrays.asList(
//			"372f4ae6cebcd664e3b43cade356d1df233f6467",
//			"f77804dad35c13d9ff96456e85737883cf7ddd99",
//			"d5b2bb8cd1393f1c5a5bb623e3d8906cd57e53c4",
//			"1bf2875e9d73e2d1cd3b58200d5300485f890ff5",
//			"1c7c03dd9e6d5810ad22d37ecae59722c219ac35",
			"182f4d1174036417aad9d6db908ceaf64234fd5f"
//			"7f80425b6a0af9bdfef12c8a873676e39e0a04a6",
//			"bf5ee44b3b576e01ab09cae9f50300417b01dc07",
//			"881baed894540031bd55e402933bcad28b74ca88",
//			"5a853a60f93e09c446d458673bc7a2f6bb26742c",
//			"5a38d0bca0e48853c3f7c00a0f098bada64797df",
//			"1571049ec04b1e7e6f082ed5ec071584e7200c12",
//			"c8e09e2056c54ead97bce4386a25b222154223b1",
//			"bec15926deb49d2b3f7b979d4cfc819947a434ec",
//			"becced5f0b7bac8200df7a5706b568687b517b90",
//			"3d5e343df6a39ce3b41624b90974d83e9899541e",
//			"ec5230abc7500734d7b78a176c291378e100a927",
//			"99528dcc3b4a82b5e52a87d3e7aed5c6479028c7",
//			"d49765899cb9df6781fff9773ffc244b5167351c",
//			"f797bfa4da53315b49f8d97b784047f33ba1bf5f",
//			"00aa01fb90f3210d1e3027d7f759fb1085b814bd",
//			"4baf0a4de8d03022df48d696d210cc8b3117d38a",
//			"d3533c1a0716ca114d294b3ea183504c9725698f",
//			"c1b847acdc8cb90a1498b236b3bb5c81ca75c044",
//			"4b5b74b6467a28fb9b7712f8091e4aa61c2d64b6",
//			"e58c9c3eef4c6e44b21a97cfbd2862bb2eb4627a",
//			"51b8b0e1ad4be1b137d67774eab28dc0ef52cb0a",
//			"3fd77b419673ce6ec41e06cdc27558b1d8f4ca06",
//			"51f498a96b2fa1822e392027982c20e950535fd1",
//			"05bd8ecda456e0901ef7375b9ff7b120ae668eca",
//			"fa62b9bde224341e0c2d43c0694fc10c4df7336f",
//			"44a02e5efc39c6953ca6dd631669d91293ab67f6",
//			"76d7f5e3fe4eb41b383c1d884bc1217b9fa7192e",
//			"e29924b33ec0c0298ba4fc3f7a8c218c8e6cfa0c",
//			"b2b4085348de32f10903970dded99fdf0376a43c",
//			"7c59f2a4f9b03a9e48ca15554291a03477aa19c1",
//			"08b1b56e2cd5ad72126f4bbeb15a47d9b104dfff",
//			"b395127e733b33c27f344695ebf155ecf5edeeab",
//			"ec5ea36faa3dd74585bb339beabdba6149ed63be",
//			"8f446b6ddf540e1b1fefca34dd10f45ba7256095",
//			"30c4ae09745d6062077925a54f27205b7401d8df",
//			"d57b1401f874f96a53f1ec1c0f8a6089ae66a4ce",
//			"a5cdd8c4b10a738cb44819d7cc2fee5f5965d4a0",
//			"334dbc7cf3432e7c17b0ed98801e61b0b591b408",
//			"1eb3b624b288a4b1a054420d3efb05b8f1d28517",
//			"913704e835169255530c7408cad11ce9a714d4ec",
//			"543a9808a85619dbe5acc2373cb4fe5344442de7",
//			"5790b4a44ba85e7e8ece64613d9e6a1b737a6cde",
//			"2f7481ee4e20ae785298c31ec2f979752dd7eb03",
//			"03ade425dd5a65d3a713d5e7d85aa7605956fbd2",
//			"4aa2e8746b5492bbc1cf2b36af956cf3b01e40f5",
//			"deb8e5ca64fcf633edbd89523af472da813b6772",
//			"d31fa31cdcc5ea2fa96116e3b1265baa180df58a",
//			"3815f293ba9338f423315d93a373608c95002b15",
//			"54fa890a6af4ccf564fb481d3e1b6ad4d084de9e",
//			"1a2c1bcdc7267abec9b19d77726aedbb045d79a8",
//			"b08f28a10d050beaba6250e9e9c46efe13d9caaa",
//			"bf35b533f067b51d4c373c5e5124d88525db99f3",
//			"e0072aac53b3b88de787e7ca653c7e17f9499018",
//			"e3b84c8753a21b1b15cfc9aa90b5e0c56d290f41"
		));
		
		for (RefactoringSet rs : expected) {
			String project = rs.getProject();
			String commit = rs.getRevision();
			if (!whitelist.contains(commit)) {
				rc.remove(project, commit);
			} else {				
				try {
					//evalUtils.prepareSourceCodeLightCheckout(project, commit);
				} catch (RuntimeException e) {
					System.out.println(String.format("Skipped %s %s", project, commit));
					e.printStackTrace();
					continue;
				}
				Map<KeyPair, String> explanations = new HashMap<>();
				rc.compareWith("RefDiff", evalUtils.runRefDiff(project, commit, explanations, rs));
				//rc.addFnExplanations(project, commit, explanations);
			}
		}
		
		System.out.println("\n\n\n");
		rc.printDetails(System.out, refactoringTypes, "RefDiff", RunIcseEval::printDetails);
		rc.printSummary(System.out, refactoringTypes);
	}
}

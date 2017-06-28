package refdiff.evaluation.benchmark;

import java.util.ArrayList;
import java.util.List;

import refdiff.evaluation.utils.RefactoringSet;

public class CalibrationDataset {
	
	private final List<RefactoringSet> commits;

	public CalibrationDataset() {
		FseDataset dataset = new FseDataset();
		commits = new ArrayList<>();
		commits.add(dataset.remove("https://github.com/linkedin/rest.li.git", "54fa890a6af4ccf564fb481d3e1b6ad4d084de9e"));
		commits.add(dataset.remove("https://github.com/droolsjbpm/jbpm.git", "3815f293ba9338f423315d93a373608c95002b15"));
		commits.add(dataset.remove("https://github.com/gradle/gradle.git", "44aab6242f8c93059612c953af950eb1870e0774"));
		commits.add(dataset.remove("https://github.com/jenkinsci/workflow-plugin.git", "d0e374ce8ecb687b4dc046d1edea9e52da17706f"));
		commits.add(dataset.remove("https://github.com/spring-projects/spring-roo.git", "0bb4cca1105fc6eb86e7c4b75bfff3dbbd55f0c8"));
		commits.add(dataset.remove("https://github.com/BuildCraft/BuildCraft.git", "a5cdd8c4b10a738cb44819d7cc2fee5f5965d4a0"));
		commits.add(dataset.remove("https://github.com/droolsjbpm/drools.git", "1bf2875e9d73e2d1cd3b58200d5300485f890ff5"));
		commits.add(dataset.remove("https://github.com/jersey/jersey.git", "d94ca2b27c9e8a5fa9fe19483d58d2f2ef024606"));
		commits.add(dataset.remove("https://github.com/undertow-io/undertow.git", "d5b2bb8cd1393f1c5a5bb623e3d8906cd57e53c4"));
		commits.add(dataset.remove("https://github.com/kuujo/copycat.git", "19a49f8f36b2f6d82534dc13504d672e41a3a8d1"));
		commits.add(dataset.remove("https://github.com/facebook/facebook-android-sdk.git", "19d1936c3b07d97d88646aeae30de747715e3248"));
		commits.add(dataset.remove("https://github.com/apache/cassandra.git", "573a1d115b86abbe3fb53ff930464d7d8fd95600"));
		commits.add(dataset.remove("https://github.com/neo4j/neo4j.git", "77fab3caea4495798a248035f0e928f745c7c2db"));
		commits.add(dataset.remove("https://github.com/cwensel/cascading.git", "f9d3171f5020da5c359cdda28ef05172e858c464"));
		commits.add(dataset.remove("https://github.com/elastic/elasticsearch.git", "f77804dad35c13d9ff96456e85737883cf7ddd99"));
		commits.add(dataset.remove("https://github.com/facebook/facebook-android-sdk.git", "e813a0be86c87366157a0201e6c61662cadee586"));
		commits.add(dataset.remove("https://github.com/netty/netty.git", "303cb535239a6f07cbe24a033ef965e2f55758eb"));
		commits.add(dataset.remove("https://github.com/processing/processing.git", "8707194f003444a9fb8e00bffa2893ef0c2492c6"));
		commits.add(dataset.remove("https://github.com/JetBrains/intellij-community.git", "d12e1c16d1c73142334e689eb01f20abaeba84b0"));
		commits.add(dataset.remove("https://github.com/aws/aws-sdk-java.git", "4baf0a4de8d03022df48d696d210cc8b3117d38a"));
		commits.add(dataset.remove("https://github.com/checkstyle/checkstyle.git", "a07cae0aca9f9072256b3a5fd05779e8d69b9748"));
		commits.add(dataset.remove("https://github.com/jankotek/MapDB.git", "32dd05fc13b53873bf18c589622b55d12e3883c7"));
		commits.add(dataset.remove("https://github.com/MovingBlocks/Terasology.git", "dbd2d5048ae5e30fec98ddd969b6c1e91183fb65"));
		commits.add(dataset.remove("https://github.com/graphhopper/graphhopper.git", "7f80425b6a0af9bdfef12c8a873676e39e0a04a6"));
		commits.add(dataset.remove("https://github.com/hibernate/hibernate-orm.git", "2f1b67b03f6c48aa189d7478e16ed0dcf8d50af8"));
		commits.add(dataset.remove("https://github.com/neo4j/neo4j.git", "b83e6a535cbca21d5ea764b0c49bfca8a9ff9db4"));
		commits.add(dataset.remove("https://github.com/CyanogenMod/android_frameworks_base.git", "910397f2390d6821a006991ed6035c76cbc74897"));
		commits.add(dataset.remove("https://github.com/square/javapoet.git", "5a37c2aa596377cb4c9b6f916614407fd0a7d3db"));
		commits.add(dataset.remove("https://github.com/fabric8io/fabric8.git", "07807aed847e1d0589c094461544e54a2677cbf5"));
		commits.add(dataset.remove("https://github.com/apache/drill.git", "f8197cfe1bc3671aa6878ef9d1869b2fe8e57331"));
		commits.add(dataset.remove("https://github.com/eclipse/vert.x.git", "718782014519034b28f6d3182fd9d340b7b31a74"));
		commits.add(dataset.remove("https://github.com/apache/camel.git", "9f319029ecc031cf8bf1756ab8a0e9e4e52c2902"));
		commits.add(dataset.remove("https://github.com/JetBrains/intellij-community.git", "6905d569a1e39d0d7b1ec5ceee4f0bbe60b85947"));
		commits.add(dataset.remove("https://github.com/jersey/jersey.git", "d57b1401f874f96a53f1ec1c0f8a6089ae66a4ce"));
		commits.add(dataset.remove("https://github.com/redsolution/xabber-android.git", "faaf826e901f43d1b46105b18e655eb120f3ffef"));
		commits.add(dataset.remove("https://github.com/gradle/gradle.git", "04bcfe98dbe7b05e508559930c21379ece845732"));
		commits.add(dataset.remove("https://github.com/spring-projects/spring-framework.git", "dd4bc630c3de70204081ab196945d6b55ab03beb"));
		commits.add(dataset.remove("https://github.com/droolsjbpm/drools.git", "c8e09e2056c54ead97bce4386a25b222154223b1"));
		commits.add(dataset.remove("https://github.com/hazelcast/hazelcast.git", "c00275e7f85c8a9af5785f66cc0f75dc027b6cb6"));
		commits.add(dataset.remove("https://github.com/neo4j/neo4j.git", "4712de476aabe69cd762233c9641dd3cf9f8361b"));
		commits.add(dataset.remove("https://github.com/apache/drill.git", "c1b847acdc8cb90a1498b236b3bb5c81ca75c044"));
		commits.add(dataset.remove("https://github.com/facebook/presto.git", "364f50274d4b4b83d40930c0d2c4d0e57fb34589"));
		commits.add(dataset.remove("https://github.com/hazelcast/hazelcast.git", "4d05a3b1168441216dcaea8282c39338285182af"));
		commits.add(dataset.remove("https://github.com/killbill/killbill.git", "4b5b74b6467a28fb9b7712f8091e4aa61c2d64b6"));
		commits.add(dataset.remove("https://github.com/gradle/gradle.git", "b1fb1192daa1647b0bd525600dd41063765eca70"));
	}

	public List<RefactoringSet> getCommits() {
		return commits;
	}
}

package refdiff.evaluation;

public class CalibrationDataset extends AbstractDataset {
	
	public CalibrationDataset() {
		FseDataset dataset = new FseDataset();
		add(dataset.remove("https://github.com/linkedin/rest.li.git", "54fa890a6af4ccf564fb481d3e1b6ad4d084de9e"));
		add(dataset.remove("https://github.com/droolsjbpm/jbpm.git", "3815f293ba9338f423315d93a373608c95002b15"));
		add(dataset.remove("https://github.com/gradle/gradle.git", "44aab6242f8c93059612c953af950eb1870e0774"));
		add(dataset.remove("https://github.com/jenkinsci/workflow-plugin.git", "d0e374ce8ecb687b4dc046d1edea9e52da17706f"));
		add(dataset.remove("https://github.com/spring-projects/spring-roo.git", "0bb4cca1105fc6eb86e7c4b75bfff3dbbd55f0c8"));
		add(dataset.remove("https://github.com/BuildCraft/BuildCraft.git", "a5cdd8c4b10a738cb44819d7cc2fee5f5965d4a0"));
		add(dataset.remove("https://github.com/droolsjbpm/drools.git", "1bf2875e9d73e2d1cd3b58200d5300485f890ff5"));
		add(dataset.remove("https://github.com/jersey/jersey.git", "d94ca2b27c9e8a5fa9fe19483d58d2f2ef024606"));
		add(dataset.remove("https://github.com/undertow-io/undertow.git", "d5b2bb8cd1393f1c5a5bb623e3d8906cd57e53c4"));
		add(dataset.remove("https://github.com/kuujo/copycat.git", "19a49f8f36b2f6d82534dc13504d672e41a3a8d1"));
		add(dataset.remove("https://github.com/facebook/facebook-android-sdk.git", "19d1936c3b07d97d88646aeae30de747715e3248"));
		add(dataset.remove("https://github.com/apache/cassandra.git", "573a1d115b86abbe3fb53ff930464d7d8fd95600"));
		add(dataset.remove("https://github.com/neo4j/neo4j.git", "77fab3caea4495798a248035f0e928f745c7c2db"));
		add(dataset.remove("https://github.com/cwensel/cascading.git", "f9d3171f5020da5c359cdda28ef05172e858c464"));
		add(dataset.remove("https://github.com/elastic/elasticsearch.git", "f77804dad35c13d9ff96456e85737883cf7ddd99"));
		add(dataset.remove("https://github.com/facebook/facebook-android-sdk.git", "e813a0be86c87366157a0201e6c61662cadee586"));
		add(dataset.remove("https://github.com/netty/netty.git", "303cb535239a6f07cbe24a033ef965e2f55758eb"));
		add(dataset.remove("https://github.com/processing/processing.git", "8707194f003444a9fb8e00bffa2893ef0c2492c6"));
		add(dataset.remove("https://github.com/JetBrains/intellij-community.git", "d12e1c16d1c73142334e689eb01f20abaeba84b0"));
		//add(dataset.remove("https://github.com/aws/aws-sdk-java.git", "4baf0a4de8d03022df48d696d210cc8b3117d38a"));
		add(dataset.remove("https://github.com/checkstyle/checkstyle.git", "a07cae0aca9f9072256b3a5fd05779e8d69b9748"));
		add(dataset.remove("https://github.com/jankotek/MapDB.git", "32dd05fc13b53873bf18c589622b55d12e3883c7"));
		add(dataset.remove("https://github.com/MovingBlocks/Terasology.git", "dbd2d5048ae5e30fec98ddd969b6c1e91183fb65"));
		add(dataset.remove("https://github.com/graphhopper/graphhopper.git", "7f80425b6a0af9bdfef12c8a873676e39e0a04a6"));
		add(dataset.remove("https://github.com/hibernate/hibernate-orm.git", "2f1b67b03f6c48aa189d7478e16ed0dcf8d50af8"));
		add(dataset.remove("https://github.com/neo4j/neo4j.git", "b83e6a535cbca21d5ea764b0c49bfca8a9ff9db4"));
		add(dataset.remove("https://github.com/CyanogenMod/android_frameworks_base.git", "910397f2390d6821a006991ed6035c76cbc74897"));
		add(dataset.remove("https://github.com/square/javapoet.git", "5a37c2aa596377cb4c9b6f916614407fd0a7d3db"));
		add(dataset.remove("https://github.com/fabric8io/fabric8.git", "07807aed847e1d0589c094461544e54a2677cbf5"));
		add(dataset.remove("https://github.com/apache/drill.git", "f8197cfe1bc3671aa6878ef9d1869b2fe8e57331"));
		add(dataset.remove("https://github.com/eclipse/vert.x.git", "718782014519034b28f6d3182fd9d340b7b31a74"));
		add(dataset.remove("https://github.com/apache/camel.git", "9f319029ecc031cf8bf1756ab8a0e9e4e52c2902"));
		add(dataset.remove("https://github.com/JetBrains/intellij-community.git", "6905d569a1e39d0d7b1ec5ceee4f0bbe60b85947"));
		add(dataset.remove("https://github.com/jersey/jersey.git", "d57b1401f874f96a53f1ec1c0f8a6089ae66a4ce"));
		add(dataset.remove("https://github.com/redsolution/xabber-android.git", "faaf826e901f43d1b46105b18e655eb120f3ffef"));
		add(dataset.remove("https://github.com/gradle/gradle.git", "04bcfe98dbe7b05e508559930c21379ece845732"));
		add(dataset.remove("https://github.com/spring-projects/spring-framework.git", "dd4bc630c3de70204081ab196945d6b55ab03beb"));
		add(dataset.remove("https://github.com/droolsjbpm/drools.git", "c8e09e2056c54ead97bce4386a25b222154223b1"));
		add(dataset.remove("https://github.com/hazelcast/hazelcast.git", "c00275e7f85c8a9af5785f66cc0f75dc027b6cb6"));
		add(dataset.remove("https://github.com/neo4j/neo4j.git", "4712de476aabe69cd762233c9641dd3cf9f8361b"));
		add(dataset.remove("https://github.com/apache/drill.git", "c1b847acdc8cb90a1498b236b3bb5c81ca75c044"));
		add(dataset.remove("https://github.com/facebook/presto.git", "364f50274d4b4b83d40930c0d2c4d0e57fb34589"));
		add(dataset.remove("https://github.com/hazelcast/hazelcast.git", "4d05a3b1168441216dcaea8282c39338285182af"));
		add(dataset.remove("https://github.com/killbill/killbill.git", "4b5b74b6467a28fb9b7712f8091e4aa61c2d64b6"));
		add(dataset.remove("https://github.com/gradle/gradle.git", "b1fb1192daa1647b0bd525600dd41063765eca70"));
		
		augumentDataset();
	}
	
	private void augumentDataset() {
		commit("https://github.com/linkedin/rest.li.git", "54fa890a6af4ccf564fb481d3e1b6ad4d084de9e")
			.addTP("Extract Method", "com.linkedin.restli.examples.TestCompressionServer.test406Error(String)", "com.linkedin.restli.examples.TestCompressionServer.addCompressionHeaders(HttpGet,String)")
			.addTP("Extract Method", "com.linkedin.restli.examples.TestCompressionServer.testAcceptEncoding(String,String)", "com.linkedin.restli.examples.TestCompressionServer.addCompressionHeaders(HttpGet,String)")
			.addTP("Extract Method", "com.linkedin.restli.examples.TestCompressionServer.testCompatibleDefault(String,String)", "com.linkedin.restli.examples.TestCompressionServer.addCompressionHeaders(HttpGet,String)")
			.addTP("Extract Method", "com.linkedin.restli.examples.TestCompressionServer.testCompressionBetter(Compressor)", "com.linkedin.restli.examples.TestCompressionServer.addCompressionHeaders(HttpGet,String)")
			.addTP("Extract Method", "com.linkedin.restli.examples.TestCompressionServer.testCompressionWorse(Compressor)", "com.linkedin.restli.examples.TestCompressionServer.addCompressionHeaders(HttpGet,String)")
			.addTP("Inline Method", "com.linkedin.restli.examples.RestLiIntTestServer.createServer(Engine,int,String,boolean,int,List,List)", "com.linkedin.restli.examples.RestLiIntTestServer.createServer(Engine,int,String,boolean,int)")
			.addFP("Inline Method", "com.linkedin.restli.examples.RestLiIntTestServer.createServer(Engine,int,String,boolean,int,List,List)", "com.linkedin.restli.examples.RestLiIntegrationTest.init(List,List)")
			.addTP("Rename Method", "com.linkedin.r2.filter.compression.ClientCompressionFilter.shouldCompressResponse(String)", "com.linkedin.r2.filter.compression.ClientCompressionFilter.shouldCompressResponseForOperation(String)")
			.addTP("Rename Method", "com.linkedin.r2.filter.compression.TestClientCompressionFilter.provideRequestData()", "com.linkedin.r2.filter.compression.TestClientCompressionFilter.provideRequestCompressionData()")
			.addFP("Rename Method", "com.linkedin.r2.filter.compression.TestClientCompressionFilter.testCompressionOperations(String,String[],boolean)", "com.linkedin.r2.filter.compression.TestClientCompressionFilter.testResponseCompressionRules(CompressionConfig,CompressionOption,String,String)")
			.addTP("Rename Method", "com.linkedin.r2.transport.http.client.HttpClientFactory.getCompressionConfig(String,String)", "com.linkedin.r2.transport.http.client.HttpClientFactory.getRequestCompressionConfig(String,EncodingType)")
			.addTP("Rename Method", "com.linkedin.r2.transport.http.client.HttpClientFactory.getRequestContentEncodingName(List)", "com.linkedin.r2.transport.http.client.HttpClientFactory.getRequestContentEncoding(List)")
			.addTP("Rename Method", "com.linkedin.r2.transport.http.client.TestHttpClientFactory.testGetCompressionConfig(String,int,CompressionConfig)", "com.linkedin.r2.transport.http.client.TestHttpClientFactory.testGetRequestCompressionConfig(String,int,CompressionConfig)")
			.addFP("Rename Method", "com.linkedin.r2.filter.CompressionConfig.shouldCompressRequest(int,CompressionOption)", "com.linkedin.r2.filter.CompressionConfig.getCompressionThreshold()")
			.addTP("Rename Method", "com.linkedin.r2.transport.http.client.HttpClientFactory.buildAcceptEncodingSchemaNames()", "com.linkedin.r2.transport.http.client.HttpClientFactory.buildAcceptEncodingSchemas()");
		
		commit("https://github.com/droolsjbpm/jbpm.git", "3815f293ba9338f423315d93a373608c95002b15")
			.addTP("Move Method", "org.jbpm.process.audit.JPAAuditLogService.convertListToInterfaceList(List,Class)", "org.jbpm.query.jpa.impl.QueryCriteriaUtil.convertListToInterfaceList(List,Class)")
			.addFP("Extract Method", "org.jbpm.services.task.impl.TaskQueryBuilderImpl.TaskQueryBuilderImpl(String,CommandService)", "org.jbpm.query.jpa.data.QueryWhere.setAscending(String)")
			.addFP("Extract Method", "org.jbpm.services.task.impl.TaskQueryBuilderImpl.clear()", "org.jbpm.query.jpa.data.QueryWhere.setAscending(String)")
			.addFP("Extract Method", "org.jbpm.services.task.impl.TaskQueryServiceImpl.getTasksByVariousFields(String,Map,boolean)", "org.jbpm.query.jpa.data.QueryWhere.setAscending(String)")
			.addTP("Inline Method", "org.jbpm.query.jpa.data.QueryWhere.getAppropriateQueryCriteria(String,int)", "org.jbpm.query.jpa.data.QueryWhere.addParameter(String,T[])")
			.addTP("Inline Method", "org.jbpm.query.jpa.data.QueryWhere.getAppropriateQueryCriteria(String,int)", "org.jbpm.query.jpa.data.QueryWhere.addRangeParameter(String,T,boolean)")
			.addTP("Inline Method", "org.jbpm.query.jpa.data.QueryWhere.resetGroup()", "org.jbpm.query.jpa.data.QueryWhere.clear()")
			.addTP("Rename Class", "org.jbpm.query.jpa.data.QueryWhere.ParameterType", "org.jbpm.query.jpa.data.QueryWhere.QueryCriteriaType")
			.addTP("Rename Method", "org.jbpm.query.jpa.data.QueryWhere.addAppropriateParam(String,T[])", "org.jbpm.query.jpa.data.QueryWhere.addParameter(String,T[])")
			.addTP("Rename Method", "org.jbpm.services.task.impl.TaskQueryBuilderImpl.buildQuery()", "org.jbpm.services.task.impl.TaskQueryBuilderImpl.build()")
			.addTP("Rename Method", "org.jbpm.services.task.impl.TaskQueryBuilderImpl.initiator(String[])", "org.jbpm.services.task.impl.TaskQueryBuilderImpl.createdBy(String[])")
			.addFP("Rename Method", "org.jbpm.services.task.impl.TaskQueryBuilderImpl.orderBy(OrderBy)", "org.jbpm.services.task.impl.TaskQueryBuilderImpl.getOrderByListId(OrderBy)")
			.addTP("Rename Method", "org.jbpm.services.task.impl.TaskQueryBuilderImpl.taskOwner(String[])", "org.jbpm.services.task.impl.TaskQueryBuilderImpl.actualOwner(String[])")
			.addFP("Move Method", "org.jbpm.services.task.commands.TaskQueryDataCommand.execute(Context)", "org.jbpm.services.task.commands.TaskQueryWhereCommand.execute(Context)")
			.addTP("Rename Class", "org.jbpm.services.task.commands.TaskQueryDataCommand", "org.jbpm.services.task.commands.TaskQueryWhereCommand")
			.addFP("Move Method", "org.jbpm.services.task.impl.TaskQueryServiceImpl.addCriteria(String,String,Class)", "org.jbpm.query.jpa.impl.QueryCriteriaUtil.addCriteria(Map,String,Attribute)")
			.addFP("Move Method", "org.jbpm.services.task.impl.TaskQueryServiceImpl.addCriteria(String,String,Class,String)", "org.jbpm.query.jpa.impl.QueryCriteriaUtil.addCriteria(Map,String,Class,Attribute)")
			.addFP("Rename Method", "org.jbpm.process.audit.JPAAuditLogService.queryProcessInstanceLogs(QueryData)", "org.jbpm.process.audit.JPAAuditLogService.queryLogs(QueryWhere,Class,Class)")
			.addFP("Rename Method", "org.jbpm.query.jpa.builder.impl.AbstractQueryBuilderImpl.ascending()", "org.jbpm.query.jpa.builder.impl.AbstractQueryBuilderImpl.endGroup()")
			.addFP("Rename Method", "org.jbpm.query.jpa.builder.impl.AbstractQueryBuilderImpl.descending()", "org.jbpm.query.jpa.builder.impl.AbstractQueryBuilderImpl.newGroup()")
			.addFP("Rename Method", "org.jbpm.query.jpa.builder.impl.AbstractQueryBuilderImpl.getQueryData()", "org.jbpm.query.jpa.builder.impl.AbstractQueryBuilderImpl.getQueryWhere()")
			.addFP("Rename Method", "org.jbpm.query.jpa.data.QueryCriteria.QueryCriteria(String,boolean,ParameterType)", "org.jbpm.query.jpa.data.QueryCriteria.isGroupCriteria()")
			.addFP("Rename Method", "org.jbpm.query.jpa.data.QueryCriteria.setParameter(int,Object)", "org.jbpm.query.jpa.data.QueryCriteria.hasValues()")
			.addFP("Rename Method", "org.jbpm.query.jpa.data.QueryWhere.getAppropriateQueryCriteria(String,int)", "org.jbpm.query.jpa.data.QueryWhere.addRangeParameters(String,T,T)")
			.addFP("Rename Method", "org.jbpm.query.jpa.data.QueryWhere.resetGroup()", "org.jbpm.query.jpa.data.QueryWhere.getCriteriaType()")
			.addTP("Rename Method", "org.jbpm.query.jpa.data.QueryWhere.startGroup()", "org.jbpm.query.jpa.data.QueryWhere.newGroup()")
			.addFP("Rename Method", "org.jbpm.services.task.commands.TaskQueryDataCommand.TaskQueryDataCommand(QueryData)", "org.jbpm.services.task.commands.TaskQueryWhereCommand.setQueryWhere(QueryWhere)")
			.addFP("Rename Method", "org.jbpm.services.task.commands.TaskQueryDataCommand.getQueryData()", "org.jbpm.services.task.commands.TaskQueryWhereCommand.getQueryWhere()")
			.addFP("Rename Method", "org.jbpm.services.task.commands.TaskQueryDataCommand.setQueryData(QueryData)", "org.jbpm.services.task.commands.TaskQueryWhereCommand.TaskQueryWhereCommand(QueryWhere)")
			.addFP("Push Down Method", "org.jbpm.query.jpa.builder.impl.AbstractQueryBuilderImpl.ascending()", "org.jbpm.executor.impl.jpa.ErrorInfoQueryBuilderImpl.ascending(OrderBy)")
			.addFP("Push Down Method", "org.jbpm.query.jpa.builder.impl.AbstractQueryBuilderImpl.ascending()", "org.jbpm.executor.impl.jpa.RequestInfoQueryBuilderImpl.ascending(OrderBy)")
			.addFP("Push Down Method", "org.jbpm.query.jpa.builder.impl.AbstractQueryBuilderImpl.ascending()", "org.jbpm.process.audit.query.AbstractAuditQueryBuilderImpl.ascending(OrderBy)")
			.addFP("Push Down Method", "org.jbpm.query.jpa.builder.impl.AbstractQueryBuilderImpl.ascending()", "org.jbpm.services.task.impl.TaskQueryBuilderImpl.ascending(OrderBy)")
			.addFP("Push Down Method", "org.jbpm.query.jpa.builder.impl.AbstractQueryBuilderImpl.descending()", "org.jbpm.executor.impl.jpa.ErrorInfoQueryBuilderImpl.descending(OrderBy)")
			.addFP("Push Down Method", "org.jbpm.query.jpa.builder.impl.AbstractQueryBuilderImpl.descending()", "org.jbpm.executor.impl.jpa.RequestInfoQueryBuilderImpl.descending(OrderBy)")
			.addFP("Push Down Method", "org.jbpm.query.jpa.builder.impl.AbstractQueryBuilderImpl.descending()", "org.jbpm.process.audit.query.AbstractAuditQueryBuilderImpl.descending(OrderBy)")
			.addFP("Push Down Method", "org.jbpm.query.jpa.builder.impl.AbstractQueryBuilderImpl.descending()", "org.jbpm.services.task.impl.TaskQueryBuilderImpl.descending(OrderBy)")
			.addFP("Inline Method", "org.jbpm.query.jpa.data.QueryCriteria.convertObjectToSerializableVariant(Object)", "org.jbpm.query.jpa.data.QueryCriteria.addParameter(Object)");
		
		commit("https://github.com/BuildCraft/BuildCraft.git", "a5cdd8c4b10a738cb44819d7cc2fee5f5965d4a0")
			.addTP("Push Down Method", "buildcraft.api.robots.ResourceId.equals(Object)", "buildcraft.api.robots.ResourceIdRequest.equals(Object)")
			.addFP("Extract Method", "buildcraft.robotics.ai.AIRobotSearchStackRequest.getOrderFromRequestingStation(DockingStation,boolean)", "buildcraft.robotics.StackRequest.setStation(DockingStation)")
			.addFP("Extract Method", "buildcraft.robotics.boards.BoardRobotDelivery.delegateAIEnded(AIRobot)", "buildcraft.robotics.boards.BoardRobotDelivery.releaseCurrentRequest()")
			.addTP("Rename Method", "buildcraft.builders.TileBuilder.getAvailableRequest(int)", "buildcraft.builders.TileBuilder.getRequest(int)")
			.addTP("Rename Method", "buildcraft.builders.TileBuilder.getNumberOfRequests()", "buildcraft.builders.TileBuilder.getRequestsCount()")
			.addTP("Rename Method", "buildcraft.builders.TileBuilder.provideItemsForRequest(int,ItemStack)", "buildcraft.builders.TileBuilder.offerItem(int,ItemStack)")
			.addTP("Rename Method", "buildcraft.robotics.TileRequester.getNumberOfRequests()", "buildcraft.robotics.TileRequester.getRequestsCount()")
			.addTP("Rename Method", "buildcraft.robotics.TileRequester.provideItemsForRequest(int,ItemStack)", "buildcraft.robotics.TileRequester.offerItem(int,ItemStack)")
			.addFP("Rename Method", "buildcraft.api.robots.ResourceIdRequest.ResourceIdRequest(TileEntity,int)", "buildcraft.api.robots.ResourceIdRequest.readFromNBT(NBTTagCompound)")
			.addFP("Rename Method", "buildcraft.robotics.TileRequester.takeRequest(int,EntityRobotBase)", "buildcraft.robotics.TileRequester.getRequestTemplate(int)")
			.addFP("Rename Method", "buildcraft.robotics.ai.AIRobotSearchStackRequest.getOrderFromRequestingAction(DockingStation)", "buildcraft.robotics.ai.AIRobotSearchStackRequest.getAvailableRequests(DockingStation)")
			.addFP("Move Attribute", "buildcraft.api.robots.StackRequest.stack", "buildcraft.robotics.StackRequest.stack")
			.addFP("Move Attribute", "buildcraft.api.robots.StackRequest.station", "buildcraft.robotics.StackRequest.station")
			.addTP("Move Class", "buildcraft.api.robots.StackRequest", "buildcraft.robotics.StackRequest")
			
			.addTP("Push Down Method", "buildcraft.api.robots.ResourceId.hashCode()", "buildcraft.api.robots.ResourceIdBlock.hashCode()")
			.addTP("Push Down Method", "buildcraft.api.robots.ResourceId.hashCode()", "buildcraft.api.robots.ResourceIdRequest.hashCode()");
		
		commit("https://github.com/droolsjbpm/drools.git", "1bf2875e9d73e2d1cd3b58200d5300485f890ff5")
			.addFP("Extract Method", "org.drools.core.common.DefaultAgenda.fireUntilHalt(AgendaFilter)", "org.drools.core.common.DefaultAgenda.waitAndEnterExecutionState(ExecutionState)")
			.addFP("Extract Method", "org.drools.core.phreak.SynchronizedPropagationList.flush()", "org.drools.core.phreak.SynchronizedPropagationList.takeAll()")
			.addTP("Extract Method", "org.drools.core.rule.SlidingTimeWindow.assertFact(Object,InternalFactHandle,PropagationContext,InternalWorkingMemory)", "org.drools.core.rule.SlidingTimeWindow.SlidingTimeWindowContext.add(EventFactHandle)")
			.addTP("Extract Method", "org.drools.core.rule.SlidingTimeWindow.assertFact(Object,InternalFactHandle,PropagationContext,InternalWorkingMemory)", "org.drools.core.rule.SlidingTimeWindow.SlidingTimeWindowContext.peek()")
			.addTP("Extract Method", "org.drools.core.rule.SlidingTimeWindow.expireFacts(Object,PropagationContext,InternalWorkingMemory)", "org.drools.core.rule.SlidingTimeWindow.SlidingTimeWindowContext.peek()")
			.addTP("Extract Method", "org.drools.core.rule.SlidingTimeWindow.retractFact(Object,InternalFactHandle,PropagationContext,InternalWorkingMemory)", "org.drools.core.rule.SlidingTimeWindow.SlidingTimeWindowContext.peek()")
			.addTP("Extract Method", "org.drools.core.rule.SlidingTimeWindow.retractFact(Object,InternalFactHandle,PropagationContext,InternalWorkingMemory)", "org.drools.core.rule.SlidingTimeWindow.SlidingTimeWindowContext.poll()")
			.addTP("Extract Method", "org.drools.core.rule.SlidingTimeWindow.expireFacts(Object,PropagationContext,InternalWorkingMemory)", "org.drools.core.rule.SlidingTimeWindow.SlidingTimeWindowContext.remove()")
			.addTP("Extract Method", "org.drools.core.rule.SlidingTimeWindow.retractFact(Object,InternalFactHandle,PropagationContext,InternalWorkingMemory)", "org.drools.core.rule.SlidingTimeWindow.SlidingTimeWindowContext.remove(EventFactHandle)")
			.addTP("Rename Method", "org.drools.core.phreak.PhreakTimerNode.TimerAction.requiresImmediateFlushingIfNotFiring()", "org.drools.core.phreak.PhreakTimerNode.TimerAction.requiresImmediateFlushing()")
			.addTP("Rename Method", "org.drools.core.phreak.PropagationEntry.AbstractPropagationEntry.requiresImmediateFlushingIfNotFiring()", "org.drools.core.phreak.PropagationEntry.AbstractPropagationEntry.requiresImmediateFlushing()")
			.addTP("Rename Method", "org.drools.core.phreak.RuleExecutor.isHighestSalience(RuleAgendaItem)", "org.drools.core.phreak.RuleExecutor.isHigherSalience(RuleAgendaItem)")
			.addFP("Rename Method", "org.drools.core.phreak.SynchronizedPropagationList.internalFlush()", "org.drools.core.phreak.SynchronizedPropagationList.flush(InternalWorkingMemory,PropagationEntry)")
			.addTP("Rename Method", "org.drools.reteoo.common.ReteAgenda.executeIfNotFiring(Runnable)", "org.drools.reteoo.common.ReteAgenda.executeTask(ExecutableEntry)")
			.addFP("Move Method", "org.drools.core.common.TupleEntryQueueImpl.size()", "org.drools.reteoo.common.ReteAgendaGroupQueueImpl.SynchronizedBinaryHeapQueue.size()")
			.addFP("Move Method", "org.drools.core.common.TupleEntryQueueImpl.takeAll()", "org.drools.core.phreak.SynchronizedPropagationList.takeAll()")
			.addFP("Rename Method", "org.drools.core.common.DefaultAgenda.addActivation(AgendaItem,boolean)", "org.drools.core.common.DefaultAgenda.fireTimedActivation(Activation)")
			.addFP("Rename Method", "org.drools.core.common.DefaultAgenda.executeIfNotFiring(Runnable)", "org.drools.core.common.DefaultAgenda.executeTask(ExecutableEntry)")
			.addFP("Rename Method", "org.drools.core.common.DefaultAgenda.notifyHalt()", "org.drools.core.common.DefaultAgenda.immediateHalt()")
			;
		
		commit("https://github.com/jersey/jersey.git", "d94ca2b27c9e8a5fa9fe19483d58d2f2ef024606")
			.addTP("Extract Method", "org.glassfish.jersey.client.HttpUrlConnectorProvider.getConnector(Client,Configuration)", "org.glassfish.jersey.client.HttpUrlConnectorProvider.createHttpUrlConnector(Client,ConnectionFactory,int,boolean,boolean)")
			.addTP("Extract Method", "org.glassfish.jersey.client.HttpUrlConnector._apply(ClientRequest)", "org.glassfish.jersey.client.internal.HttpUrlConnector.secureConnection(Client,HttpURLConnection)");
		
		commit("https://github.com/undertow-io/undertow.git", "d5b2bb8cd1393f1c5a5bb623e3d8906cd57e53c4")
			.addTP("Move Method", "io.undertow.predicate.PredicateParser.coerceToType(String,Token,Class,ExchangeAttributeParser)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.coerceToType(String,Token,Class,ExchangeAttributeParser)")
			.addTP("Move Method", "io.undertow.predicate.PredicateParser.collapseOutput(Object,Deque)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.collapseOutput(Node,Deque)")
			.addTP("Move Method", "io.undertow.predicate.PredicateParser.handleSingleArrayValue(String,PredicateBuilder,Deque,Token,ExchangeAttributeParser,String)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.handleSingleArrayValue(String,Token,Deque,String)")
			.addTP("Move Method", "io.undertow.predicate.PredicateParser.isSpecialChar(String)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.isSpecialChar(String)")
			.addFP("Move Method", "io.undertow.predicate.PredicateParser.parse(String,Deque,Map,ExchangeAttributeParser)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.parse(String,Deque,boolean)")
			.addTP("Move Method", "io.undertow.predicate.PredicateParser.precedence(String)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.precedence(String)")
			.addTP("Move Method", "io.undertow.predicate.PredicateParser.readArrayType(String,Deque,Token,PredicateBuilder,ExchangeAttributeParser,String)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.readArrayType(String,String,ArrayNode,ExchangeAttributeParser,Class)")
			.addTP("Move Method", "io.undertow.server.handlers.builder.HandlerParser.coerceToType(String,Token,Class,ExchangeAttributeParser)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.coerceToType(String,Token,Class,ExchangeAttributeParser)")
			.addTP("Move Method", "io.undertow.server.handlers.builder.HandlerParser.handleSingleArrayValue(String,HandlerBuilder,Deque,Token,ExchangeAttributeParser,String,Token)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.handleSingleArrayValue(String,Token,Deque,String)")
			.addTP("Move Method", "io.undertow.server.handlers.builder.HandlerParser.isSpecialChar(String)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.isSpecialChar(String)")
			.addTP("Move Method", "io.undertow.server.handlers.builder.HandlerParser.precedence(String)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.precedence(String)")
			.addTP("Move Method", "io.undertow.server.handlers.builder.HandlerParser.readArrayType(String,Deque,Token,HandlerBuilder,ExchangeAttributeParser,String,Token)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.readArrayType(String,String,ArrayNode,ExchangeAttributeParser,Class)")
			.addTP("Move Method", "io.undertow.server.handlers.builder.HandlerParser.tokenize(String)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.tokenize(String)")
			.addTP("Move Method", "io.undertow.util.PredicateTokeniser.error(String,int,String)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.error(String,int,String)")
			.addTP("Move Method", "io.undertow.util.PredicateTokeniser.tokenize(String)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.tokenize(String)")
			.addFP("Extract Method", "io.undertow.predicate.PredicateParser.parse(String,Deque,Map,ExchangeAttributeParser)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.handleLineEnd(String,Deque,Deque,List)")
			.addFP("Extract Method", "io.undertow.server.handlers.builder.PredicatedHandlersParser.parse(String,ClassLoader)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.handleNode(String,Node,Map,Map,ExchangeAttributeParser)")
			.addFP("Extract Method", "io.undertow.server.handlers.builder.PredicatedHandlersParser.parse(String,ClassLoader)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.loadHandlerBuilders(ClassLoader)")
			.addFP("Extract Method", "io.undertow.server.handlers.builder.PredicatedHandlersParser.parse(String,ClassLoader)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.loadPredicateBuilders(ClassLoader)")
			.addFP("Extract Method", "io.undertow.server.handlers.builder.PredicatedHandlersParser.parse(String,ClassLoader)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.parse(String,Deque)")
			.addTP("Extract Method", "io.undertow.server.handlers.builder.HandlerParser.parse(String,ClassLoader)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.parseHandler(String,ClassLoader)")
			.addTP("Extract Method", "io.undertow.predicate.PredicateParser.parse(String,ClassLoader)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.parsePredicate(String,ClassLoader)")
			.addFP("Extract Method", "io.undertow.predicate.PredicateParser.handleSingleArrayValue(String,PredicateBuilder,Deque,Token,ExchangeAttributeParser,String)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.readArrayType(String,Deque,String)")
			.addFP("Inline Method", "io.undertow.predicate.PredicateParser.parse(String,Deque,ClassLoader)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.parse(String,ClassLoader)")
			.addFP("Inline Method", "io.undertow.server.handlers.builder.HandlerParser.parse(String,Deque,ClassLoader)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.parse(String,ClassLoader)")
			.addFP("Move Attribute", "io.undertow.predicate.PredicateParser.NotNode.node", "io.undertow.server.handlers.builder.PredicatedHandlersParser.NotNode.node")
			.addFP("Move Class", "io.undertow.predicate.PredicateParser.AndNode", "io.undertow.server.handlers.builder.PredicatedHandlersParser.AndNode")
			.addFP("Move Class", "io.undertow.predicate.PredicateParser.Node", "io.undertow.server.handlers.builder.PredicatedHandlersParser.Node")
			.addFP("Move Class", "io.undertow.predicate.PredicateParser.NotNode", "io.undertow.server.handlers.builder.PredicatedHandlersParser.NotNode")
			.addFP("Move Class", "io.undertow.predicate.PredicateParser.OrNode", "io.undertow.server.handlers.builder.PredicatedHandlersParser.OrNode")
			.addFP("Move Method", "io.undertow.predicate.PredicateParser.handleSingleValue(String,PredicateBuilder,Token,ExchangeAttributeParser)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.handleSingleValue(Token,Token)")
			.addFP("Move Method", "io.undertow.predicate.PredicateParser.handleSingleVarArgsValue(String,PredicateBuilder,Deque,Token,ExchangeAttributeParser,String)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.handleSingleVarArgsValue(String,Token,Deque,String)")
			.addFP("Move Method", "io.undertow.predicate.PredicateParser.parse(String,Deque,ClassLoader)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.parse(String,Deque)")
			.addFP("Move Method", "io.undertow.server.handlers.builder.HandlerParser.handleSingleValue(String,HandlerBuilder,Token,ExchangeAttributeParser)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.handleSingleValue(Token,Token)")
			.addFP("Move Method", "io.undertow.server.handlers.builder.HandlerParser.handleSingleVarArgsValue(String,HandlerBuilder,Deque,Token,ExchangeAttributeParser,String,Token)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.handleSingleVarArgsValue(String,Token,Deque,String)")
			.addFP("Move Method", "io.undertow.server.handlers.builder.HandlerParser.parse(String,Deque,ClassLoader)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.parse(String,Deque)")
			.addFP("Move Method", "io.undertow.server.handlers.builder.HandlerParser.parse(String,Deque,Map,ExchangeAttributeParser)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.parse(String,Deque)")
			.addFP("Move Method", "io.undertow.server.handlers.builder.HandlerParser.parse(String,Map,ExchangeAttributeParser)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.parse(String,Deque)")
			.addFP("Rename Method", "io.undertow.predicate.PredicateParser.NotNode.resolve()", "io.undertow.server.handlers.builder.PredicatedHandlersParser.NotNode.getNode()")
			.addFP("Inline Method", "io.undertow.predicate.PredicateParser.parsePredicate(String,Token,Deque,Map,ExchangeAttributeParser)", "io.undertow.server.handlers.builder.PredicatedHandlersParser.parse(String,Deque,boolean)");
		
		commit("https://github.com/facebook/facebook-android-sdk.git", "19d1936c3b07d97d88646aeae30de747715e3248")
			.addTP("Move Method", "com.facebook.share.internal.ShareInternalUtility.newUploadPhotoRequest(String,AccessToken,Bitmap,String,Bundle,Callback)", "com.facebook.GraphRequest.newUploadPhotoRequest(AccessToken,String,Bitmap,String,Bundle,Callback)")
			.addTP("Move Method", "com.facebook.share.internal.ShareInternalUtility.newUploadPhotoRequest(String,AccessToken,File,String,Bundle,Callback)", "com.facebook.GraphRequest.newUploadPhotoRequest(AccessToken,String,File,String,Bundle,Callback)")
			.addTP("Move Method", "com.facebook.share.internal.ShareInternalUtility.newUploadPhotoRequest(String,AccessToken,Uri,String,Bundle,Callback)", "com.facebook.GraphRequest.newUploadPhotoRequest(AccessToken,String,Uri,String,Bundle,Callback)")
			.addFP("Inline Method", "com.facebook.share.internal.ShareInternalUtility.newPostOpenGraphObjectRequest(AccessToken,String,String,String,String,String,JSONObject,Callback)", "com.facebook.RequestTests.executePostOpenGraphRequest()")
			.addFP("Inline Method", "com.facebook.share.internal.ShareInternalUtility.newUpdateOpenGraphObjectRequest(AccessToken,String,String,String,String,String,JSONObject,Callback)", "com.facebook.RequestTests.testUpdateOpenGraphObjectRequest()");
		
		commit("https://github.com/cwensel/cascading.git", "f9d3171f5020da5c359cdda28ef05172e858c464")
			.addTP("Pull Up Method", "cascading.stats.tez.TezNodeStats.getPrefix()", "cascading.stats.CascadingStats.getPrefix()")
			.addTP("Pull Up Method", "cascading.stats.tez.TezNodeStats.logDebug(String,Object[])", "cascading.stats.CascadingStats.logDebug(String,Object[])")
			.addTP("Pull Up Method", "cascading.stats.tez.TezNodeStats.logInfo(String,Object[])", "cascading.stats.CascadingStats.logInfo(String,Object[])")
			.addTP("Pull Up Method", "cascading.stats.tez.TezNodeStats.logWarn(String,Object[])", "cascading.stats.CascadingStats.logWarn(String,Object[])")
			.addFP("Move Method", "cascading.stats.tez.TezNodeStats.getPrefix()", "cascading.stats.CascadingStats.getPrefix()")
			.addFP("Move Method", "cascading.stats.tez.TezNodeStats.logDebug(String,Object[])", "cascading.stats.CascadingStats.logDebug(String,Object[])")
			.addFP("Move Method", "cascading.stats.tez.TezNodeStats.logInfo(String,Object[])", "cascading.stats.CascadingStats.logInfo(String,Object[])")
			.addFP("Move Method", "cascading.stats.tez.TezNodeStats.logWarn(String,Object[])", "cascading.stats.CascadingStats.logWarn(String,Object[])")
			.addTP("Pull Up Attribute", "cascading.stats.tez.TezNodeStats.prefixID", "cascading.stats.CascadingStats.prefixID")
			.addFP("Move Attribute", "cascading.stats.tez.TezNodeStats.prefixID", "cascading.stats.CascadingStats.prefixID")
			.addTP("Extract Method", "cascading.stats.hadoop.BaseHadoopNodeStats.recordChildStats()", "cascading.stats.CascadingStats.logError(String,Throwable)");
		
		commit("https://github.com/elastic/elasticsearch.git", "f77804dad35c13d9ff96456e85737883cf7ddd99")
			.addFP("Move And Rename Class", "org.elasticsearch.index.merge.policy.TieredMergePolicyProvider", "org.elasticsearch.index.shard.MergePolicyConfig")
			.addTP("Move Method", "org.elasticsearch.index.merge.policy.AbstractMergePolicyProvider.formatNoCFSRatio(double)", "org.elasticsearch.index.shard.MergePolicyConfig.formatNoCFSRatio(double)")
			.addTP("Move Method", "org.elasticsearch.index.merge.policy.AbstractMergePolicyProvider.parseNoCFSRatio(String)", "org.elasticsearch.index.shard.MergePolicyConfig.parseNoCFSRatio(String)")
			.addFP("Move Method", "org.elasticsearch.index.merge.policy.LogByteSizeMergePolicyProvider.getMergePolicy()", "org.elasticsearch.index.engine.EngineConfig.getMergePolicy()")
			.addFP("Move Method", "org.elasticsearch.index.merge.policy.LogDocMergePolicyProvider.getMergePolicy()", "org.elasticsearch.index.engine.EngineConfig.getMergePolicy()")
			.addTP("Move Class", "org.elasticsearch.index.merge.policy.MergePolicySettingsTest", "org.elasticsearch.index.shard.MergePolicySettingsTest")
			.addFP("Move Method", "org.elasticsearch.index.merge.policy.MergePolicySettingsTest.build(String)", "org.elasticsearch.index.shard.MergePolicySettingsTest.build(String)")
			.addFP("Move Method", "org.elasticsearch.index.merge.policy.MergePolicySettingsTest.build(boolean)", "org.elasticsearch.index.shard.MergePolicySettingsTest.build(String)")
			.addFP("Move Method", "org.elasticsearch.index.merge.policy.MergePolicySettingsTest.build(double)", "org.elasticsearch.index.shard.MergePolicySettingsTest.build(String)")
			.addFP("Move Method", "org.elasticsearch.index.merge.policy.MergePolicySettingsTest.build(int)", "org.elasticsearch.index.shard.MergePolicySettingsTest.build(String)")
			.addFP("Move Method", "org.elasticsearch.index.merge.policy.MergePolicySettingsTest.testTieredMergePolicySettingsUpdate()", "org.elasticsearch.index.shard.MergePolicySettingsTest.testTieredMergePolicySettingsUpdate()")
			.addTP("Move Method", "org.elasticsearch.index.merge.policy.TieredMergePolicyProvider.ApplySettings.onRefreshSettings(Settings)", "org.elasticsearch.index.shard.MergePolicyConfig.onRefreshSettings(Settings)")
			.addTP("Move Attribute", "org.elasticsearch.index.merge.policy.AbstractMergePolicyProvider.INDEX_COMPOUND_FORMAT", "org.elasticsearch.index.shard.MergePolicyConfig.INDEX_COMPOUND_FORMAT")
			.addFP("Move Method", "org.elasticsearch.index.merge.policy.LogByteSizeMergePolicyProvider.ApplySettings.onRefreshSettings(Settings)", "org.elasticsearch.index.shard.MergePolicyConfig.onRefreshSettings(Settings)")
			.addFP("Move Method", "org.elasticsearch.index.merge.policy.LogDocMergePolicyProvider.ApplySettings.onRefreshSettings(Settings)", "org.elasticsearch.index.shard.MergePolicyConfig.onRefreshSettings(Settings)")
			.addTP("Move Attribute", "org.elasticsearch.index.merge.policy.AbstractMergePolicyProvider.noCFSRatio", "org.elasticsearch.index.shard.MergePolicyConfig.noCFSRatio");
		
		commit("https://github.com/facebook/facebook-android-sdk.git", "e813a0be86c87366157a0201e6c61662cadee586")
			.addTP("Move Class", "com.facebook.iconicus.MainActivity", "com.example.iconicus.MainActivity")
			.addFP("Extract Method", "com.facebook.internal.AttributionIdentifiers.getAndroidId(Context)", "com.facebook.internal.AttributionIdentifiers.getAndroidIdViaService(Context)");
		/*
		commit("https://github.com/aws/aws-sdk-java.git", "4baf0a4de8d03022df48d696d210cc8b3117d38a")
			.addFP("Move Method", "com.amazonaws.services.elasticfilesystem.model.transform.BadRequestExceptionUnmarshaller.match(String,JSONObject)", "com.amazonaws.services.cognitoidentity.model.transform.ConcurrentModificationExceptionUnmarshaller.match(String,JSONObject)")
			.addFP("Move Method", "com.amazonaws.services.elasticfilesystem.model.transform.DependencyTimeoutExceptionUnmarshaller.match(String,JSONObject)", "com.amazonaws.services.cognitoidentity.model.transform.ConcurrentModificationExceptionUnmarshaller.match(String,JSONObject)")
			.addFP("Move Method", "com.amazonaws.services.elasticfilesystem.model.transform.FileSystemAlreadyExistsExceptionUnmarshaller.match(String,JSONObject)", "com.amazonaws.services.cognitoidentity.model.transform.ConcurrentModificationExceptionUnmarshaller.match(String,JSONObject)")
			.addFP("Move Method", "com.amazonaws.services.elasticfilesystem.model.transform.FileSystemInUseExceptionUnmarshaller.match(String,JSONObject)", "com.amazonaws.services.cognitoidentity.model.transform.ConcurrentModificationExceptionUnmarshaller.match(String,JSONObject)")
			.addFP("Move Method", "com.amazonaws.services.elasticfilesystem.model.transform.FileSystemLimitExceededExceptionUnmarshaller.match(String,JSONObject)", "com.amazonaws.services.cognitoidentity.model.transform.ConcurrentModificationExceptionUnmarshaller.match(String,JSONObject)")
			.addFP("Move Method", "com.amazonaws.services.elasticfilesystem.model.transform.FileSystemNotFoundExceptionUnmarshaller.match(String,JSONObject)", "com.amazonaws.services.cognitoidentity.model.transform.ConcurrentModificationExceptionUnmarshaller.match(String,JSONObject)")
			.addFP("Move Method", "com.amazonaws.services.elasticfilesystem.model.transform.IncorrectFileSystemLifeCycleStateExceptionUnmarshaller.match(String,JSONObject)", "com.amazonaws.services.cognitoidentity.model.transform.ConcurrentModificationExceptionUnmarshaller.match(String,JSONObject)")
			.addFP("Move Method", "com.amazonaws.services.elasticfilesystem.model.transform.IncorrectMountTargetStateExceptionUnmarshaller.match(String,JSONObject)", "com.amazonaws.services.cognitoidentity.model.transform.ConcurrentModificationExceptionUnmarshaller.match(String,JSONObject)")
			.addFP("Move Method", "com.amazonaws.services.elasticfilesystem.model.transform.InternalServerErrorExceptionUnmarshaller.match(String,JSONObject)", "com.amazonaws.services.cognitoidentity.model.transform.ConcurrentModificationExceptionUnmarshaller.match(String,JSONObject)")
			.addFP("Move Method", "com.amazonaws.services.elasticfilesystem.model.transform.IpAddressInUseExceptionUnmarshaller.match(String,JSONObject)", "com.amazonaws.services.cognitoidentity.model.transform.ConcurrentModificationExceptionUnmarshaller.match(String,JSONObject)")
			.addFP("Move Method", "com.amazonaws.services.elasticfilesystem.model.transform.MountTargetConflictExceptionUnmarshaller.match(String,JSONObject)", "com.amazonaws.services.cognitoidentity.model.transform.ConcurrentModificationExceptionUnmarshaller.match(String,JSONObject)")
			.addFP("Move Method", "com.amazonaws.services.elasticfilesystem.model.transform.MountTargetNotFoundExceptionUnmarshaller.match(String,JSONObject)", "com.amazonaws.services.cognitoidentity.model.transform.ConcurrentModificationExceptionUnmarshaller.match(String,JSONObject)")
			.addFP("Move Method", "com.amazonaws.services.elasticfilesystem.model.transform.NetworkInterfaceLimitExceededExceptionUnmarshaller.match(String,JSONObject)", "com.amazonaws.services.cognitoidentity.model.transform.ConcurrentModificationExceptionUnmarshaller.match(String,JSONObject)")
			.addFP("Move Method", "com.amazonaws.services.elasticfilesystem.model.transform.NoFreeAddressesInSubnetExceptionUnmarshaller.match(String,JSONObject)", "com.amazonaws.services.cognitoidentity.model.transform.ConcurrentModificationExceptionUnmarshaller.match(String,JSONObject)")
			.addFP("Move Method", "com.amazonaws.services.elasticfilesystem.model.transform.SecurityGroupLimitExceededExceptionUnmarshaller.match(String,JSONObject)", "com.amazonaws.services.cognitoidentity.model.transform.ConcurrentModificationExceptionUnmarshaller.match(String,JSONObject)")
			.addFP("Move Method", "com.amazonaws.services.elasticfilesystem.model.transform.SecurityGroupNotFoundExceptionUnmarshaller.match(String,JSONObject)", "com.amazonaws.services.cognitoidentity.model.transform.ConcurrentModificationExceptionUnmarshaller.match(String,JSONObject)")
			.addFP("Move Method", "com.amazonaws.services.elasticfilesystem.model.transform.SubnetNotFoundExceptionUnmarshaller.match(String,JSONObject)", "com.amazonaws.services.cognitoidentity.model.transform.ConcurrentModificationExceptionUnmarshaller.match(String,JSONObject)")
			.addFP("Move Method", "com.amazonaws.services.elasticfilesystem.model.transform.UnsupportedAvailabilityZoneExceptionUnmarshaller.match(String,JSONObject)", "com.amazonaws.services.cognitoidentity.model.transform.ConcurrentModificationExceptionUnmarshaller.match(String,JSONObject)")
			.addTP("Move Method", "com.amazonaws.util.EC2MetadataUtilsTest.handleConnection(BufferedReader,PrintWriter)", "com.amazonaws.util.EC2MetadataUtilsServer.handleConnection(BufferedReader,PrintWriter)")
			.addTP("Move Method", "com.amazonaws.util.EC2MetadataUtilsTest.ignoreRequest(BufferedReader)", "com.amazonaws.util.EC2MetadataUtilsServer.ignoreRequest(BufferedReader)")
			.addTP("Move Method", "com.amazonaws.util.EC2MetadataUtilsTest.outputIamCred(PrintWriter)", "com.amazonaws.util.EC2MetadataUtilsServer.outputIamCred(PrintWriter)")
			.addTP("Move Method", "com.amazonaws.util.EC2MetadataUtilsTest.outputIamCredList(PrintWriter)", "com.amazonaws.util.EC2MetadataUtilsServer.outputIamCredList(PrintWriter)")
			.addTP("Move Method", "com.amazonaws.util.EC2MetadataUtilsTest.outputIamInfo(PrintWriter)", "com.amazonaws.util.EC2MetadataUtilsServer.outputIamInfo(PrintWriter)")
			.addTP("Extract Method", "com.amazonaws.services.elasticfilesystem.model.transform.CreateTagsRequestMarshaller.marshall(CreateTagsRequest)", "com.amazonaws.services.elasticfilesystem.model.transform.TagJsonMarshaller.marshall(Tag,JSONWriter)")
			.addTP("Extract Method", "com.amazonaws.util.EC2MetadataUtilsTest.setUp()", "com.amazonaws.util.EC2MetadataUtilsServer.start()")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.CreateFileSystemRequestMarshaller.RESOURCE_PATH_TEMPLATE", "com.amazonaws.services.glacier.model.transform.AddTagsToVaultRequestMarshaller.RESOURCE_PATH_TEMPLATE")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.CreateFileSystemRequestMarshaller.RESOURCE_PATH_TEMPLATE", "com.amazonaws.services.glacier.model.transform.ListTagsForVaultRequestMarshaller.RESOURCE_PATH_TEMPLATE")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.CreateFileSystemRequestMarshaller.RESOURCE_PATH_TEMPLATE", "com.amazonaws.services.glacier.model.transform.RemoveTagsFromVaultRequestMarshaller.RESOURCE_PATH_TEMPLATE")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.CreateFileSystemRequestMarshaller.STATIC_QUERY_PARAMS", "com.amazonaws.services.glacier.model.transform.AddTagsToVaultRequestMarshaller.STATIC_QUERY_PARAMS")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.CreateFileSystemRequestMarshaller.STATIC_QUERY_PARAMS", "com.amazonaws.services.glacier.model.transform.ListTagsForVaultRequestMarshaller.STATIC_QUERY_PARAMS")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.CreateFileSystemRequestMarshaller.STATIC_QUERY_PARAMS", "com.amazonaws.services.glacier.model.transform.RemoveTagsFromVaultRequestMarshaller.STATIC_QUERY_PARAMS")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.CreateMountTargetRequestMarshaller.RESOURCE_PATH_TEMPLATE", "com.amazonaws.services.glacier.model.transform.AddTagsToVaultRequestMarshaller.RESOURCE_PATH_TEMPLATE")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.CreateMountTargetRequestMarshaller.STATIC_QUERY_PARAMS", "com.amazonaws.services.glacier.model.transform.AddTagsToVaultRequestMarshaller.STATIC_QUERY_PARAMS")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.CreateTagsRequestMarshaller.RESOURCE_PATH_TEMPLATE", "com.amazonaws.services.glacier.model.transform.AddTagsToVaultRequestMarshaller.RESOURCE_PATH_TEMPLATE")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.CreateTagsRequestMarshaller.STATIC_QUERY_PARAMS", "com.amazonaws.services.glacier.model.transform.AddTagsToVaultRequestMarshaller.STATIC_QUERY_PARAMS")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.DeleteFileSystemRequestMarshaller.RESOURCE_PATH_TEMPLATE", "com.amazonaws.services.glacier.model.transform.AddTagsToVaultRequestMarshaller.RESOURCE_PATH_TEMPLATE")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.DeleteFileSystemRequestMarshaller.STATIC_QUERY_PARAMS", "com.amazonaws.services.glacier.model.transform.AddTagsToVaultRequestMarshaller.STATIC_QUERY_PARAMS")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.DeleteMountTargetRequestMarshaller.RESOURCE_PATH_TEMPLATE", "com.amazonaws.services.glacier.model.transform.AddTagsToVaultRequestMarshaller.RESOURCE_PATH_TEMPLATE")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.DeleteMountTargetRequestMarshaller.STATIC_QUERY_PARAMS", "com.amazonaws.services.glacier.model.transform.AddTagsToVaultRequestMarshaller.STATIC_QUERY_PARAMS")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.DeleteTagsRequestMarshaller.RESOURCE_PATH_TEMPLATE", "com.amazonaws.services.glacier.model.transform.AddTagsToVaultRequestMarshaller.RESOURCE_PATH_TEMPLATE")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.DeleteTagsRequestMarshaller.STATIC_QUERY_PARAMS", "com.amazonaws.services.glacier.model.transform.AddTagsToVaultRequestMarshaller.STATIC_QUERY_PARAMS")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.DescribeFileSystemsRequestMarshaller.RESOURCE_PATH_TEMPLATE", "com.amazonaws.services.glacier.model.transform.AddTagsToVaultRequestMarshaller.RESOURCE_PATH_TEMPLATE")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.DescribeFileSystemsRequestMarshaller.STATIC_QUERY_PARAMS", "com.amazonaws.services.glacier.model.transform.AddTagsToVaultRequestMarshaller.STATIC_QUERY_PARAMS")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.DescribeMountTargetSecurityGroupsRequestMarshaller.RESOURCE_PATH_TEMPLATE", "com.amazonaws.services.glacier.model.transform.AddTagsToVaultRequestMarshaller.RESOURCE_PATH_TEMPLATE")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.DescribeMountTargetSecurityGroupsRequestMarshaller.STATIC_QUERY_PARAMS", "com.amazonaws.services.glacier.model.transform.AddTagsToVaultRequestMarshaller.STATIC_QUERY_PARAMS")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.DescribeMountTargetsRequestMarshaller.DYNAMIC_QUERY_PARAMS", "com.amazonaws.services.glacier.model.transform.AddTagsToVaultRequestMarshaller.DYNAMIC_QUERY_PARAMS")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.DescribeMountTargetsRequestMarshaller.DYNAMIC_QUERY_PARAMS", "com.amazonaws.services.glacier.model.transform.ListTagsForVaultRequestMarshaller.DYNAMIC_QUERY_PARAMS")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.DescribeMountTargetsRequestMarshaller.DYNAMIC_QUERY_PARAMS", "com.amazonaws.services.glacier.model.transform.RemoveTagsFromVaultRequestMarshaller.DYNAMIC_QUERY_PARAMS")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.DescribeMountTargetsRequestMarshaller.RESOURCE_PATH_TEMPLATE", "com.amazonaws.services.glacier.model.transform.AddTagsToVaultRequestMarshaller.RESOURCE_PATH_TEMPLATE")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.DescribeMountTargetsRequestMarshaller.STATIC_QUERY_PARAMS", "com.amazonaws.services.glacier.model.transform.AddTagsToVaultRequestMarshaller.STATIC_QUERY_PARAMS")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.DescribeTagsRequestMarshaller.DYNAMIC_QUERY_PARAMS", "com.amazonaws.services.glacier.model.transform.AddTagsToVaultRequestMarshaller.DYNAMIC_QUERY_PARAMS")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.DescribeTagsRequestMarshaller.RESOURCE_PATH_TEMPLATE", "com.amazonaws.services.glacier.model.transform.AddTagsToVaultRequestMarshaller.RESOURCE_PATH_TEMPLATE")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.DescribeTagsRequestMarshaller.STATIC_QUERY_PARAMS", "com.amazonaws.services.glacier.model.transform.AddTagsToVaultRequestMarshaller.STATIC_QUERY_PARAMS")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.ModifyMountTargetSecurityGroupsRequestMarshaller.RESOURCE_PATH_TEMPLATE", "com.amazonaws.services.glacier.model.transform.AddTagsToVaultRequestMarshaller.RESOURCE_PATH_TEMPLATE")
			.addFP("Move Attribute", "com.amazonaws.services.elasticfilesystem.model.transform.ModifyMountTargetSecurityGroupsRequestMarshaller.STATIC_QUERY_PARAMS", "com.amazonaws.services.glacier.model.transform.AddTagsToVaultRequestMarshaller.STATIC_QUERY_PARAMS")
			.addTP("Move Attribute", "com.amazonaws.util.EC2MetadataUtilsTest.server", "com.amazonaws.util.EC2MetadataUtilsServer.server")
			.addTP("Move Method", "com.amazonaws.util.EC2MetadataUtilsTest.outputInstanceInfo(PrintWriter)", "com.amazonaws.util.EC2MetadataUtilsServer.outputInstanceInfo(PrintWriter)")
			.addTP("Extract Method", "com.amazonaws.util.EC2MetadataUtilsTest.outputInstanceInfo(PrintWriter)", "com.amazonaws.util.EC2MetadataUtilsServer.constructInstanceInfo()")
			.addTP("Rename Method", "com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper.callUntilCompletion(Map)", "com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper.doBatchWriteItemWithRetry(Map,BatchWriteRetryStrategy)")
			.addFP("Extract Method", "com.amazonaws.services.elasticfilesystem.model.transform.CreateTagsRequestMarshaller.marshall(CreateTagsRequest)", "com.amazonaws.services.elasticfilesystem.model.transform.TagJsonMarshaller.getInstance()");
		*/
		commit("https://github.com/graphhopper/graphhopper.git", "7f80425b6a0af9bdfef12c8a873676e39e0a04a6")
			.addTP("Rename Class", "com.graphhopper.storage.GraphExtension.NoExtendedStorage", "com.graphhopper.storage.GraphExtension.NoOpExtension")
			.addTP("Rename Class", "com.graphhopper.storage.LevelGraphStorage", "com.graphhopper.storage.LevelGraphImpl")
			.addTP("Rename Class", "com.graphhopper.storage.LevelGraphStorageTest", "com.graphhopper.storage.GraphHopperStorageCHTest")
			.addTP("Move Class", "com.graphhopper.storage.GraphHopperStorage.AllEdgeIterator", "com.graphhopper.storage.BaseGraph.AllEdgeIterator")
			.addTP("Move Class", "com.graphhopper.storage.GraphHopperStorage.EdgeIterable", "com.graphhopper.storage.BaseGraph.EdgeIterable")
			.addTP("Move Class", "com.graphhopper.storage.GraphHopperStorage.SingleEdge", "com.graphhopper.storage.BaseGraph.SingleEdge")
			.addTP("Rename Method", "com.graphhopper.routing.AbstractRoutingAlgorithmTester.calcPathViaQuery(Graph,double,double,double,double)", "com.graphhopper.routing.AbstractRoutingAlgorithmTester.calcPathViaQuery_(GraphHopperStorage,double,double,double,double)")
			.addTP("Rename Method", "com.graphhopper.routing.AbstractRoutingAlgorithmTester.calcPathViaQuery(String,Graph,double,double,double,double)", "com.graphhopper.routing.AbstractRoutingAlgorithmTester.calcPathViaQuery_(String,GraphHopperStorage,double,double,double,double)")
			.addTP("Rename Method", "com.graphhopper.routing.AbstractRoutingAlgorithmTester.createGraph(EncodingManager,boolean)", "com.graphhopper.routing.AbstractRoutingAlgorithmTester.createGHStorage(EncodingManager,boolean)")
			.addTP("Rename Method", "com.graphhopper.routing.AbstractRoutingAlgorithmTester.createTestGraph()", "com.graphhopper.routing.AbstractRoutingAlgorithmTester.createTestStorage()")
			.addTP("Rename Method", "com.graphhopper.routing.EdgeBasedRoutingAlgorithmTest.createGraph(EncodingManager)", "com.graphhopper.routing.EdgeBasedRoutingAlgorithmTest.createStorage(EncodingManager)")
			.addTP("Rename Method", "com.graphhopper.routing.ch.DijkstraBidirectionCHTest.createGraph(EncodingManager,boolean)", "com.graphhopper.routing.ch.DijkstraBidirectionCHTest.createGHStorage(EncodingManager,boolean)")
			.addTP("Rename Method", "com.graphhopper.routing.util.PrepareRoutingSubnetworksTest.createDeadEndUnvisitedNetworkGraph(EncodingManager)", "com.graphhopper.routing.util.PrepareRoutingSubnetworksTest.createDeadEndUnvisitedNetworkStorage(EncodingManager)")
			.addTP("Rename Method", "com.graphhopper.routing.util.PrepareRoutingSubnetworksTest.createGraph(EncodingManager)", "com.graphhopper.routing.util.PrepareRoutingSubnetworksTest.createStorage(EncodingManager)")
			.addTP("Rename Method", "com.graphhopper.routing.util.PrepareRoutingSubnetworksTest.createSubnetworkTestGraph()", "com.graphhopper.routing.util.PrepareRoutingSubnetworksTest.createSubnetworkTestStorage()")
			.addTP("Rename Method", "com.graphhopper.routing.util.PrepareRoutingSubnetworksTest.createSubnetworkTestGraph2(EncodingManager)", "com.graphhopper.routing.util.PrepareRoutingSubnetworksTest.createSubnetworkTestStorage2(EncodingManager)")
			.addTP("Rename Method", "com.graphhopper.routing.util.PrepareRoutingSubnetworksTest.createTarjanTestGraph()", "com.graphhopper.routing.util.PrepareRoutingSubnetworksTest.createTarjanTestStorage()")
			.addTP("Rename Method", "com.graphhopper.storage.AbstractGraphStorageTester.createGraph()", "com.graphhopper.storage.AbstractGraphStorageTester.createGHStorage()")
			.addTP("Rename Method", "com.graphhopper.storage.AbstractGraphStorageTester.newRAMGraph()", "com.graphhopper.storage.AbstractGraphStorageTester.newRAMGHStorage()")
			.addTP("Rename Method", "com.graphhopper.storage.GraphHopperStorageTest.createGraph(String,boolean)", "com.graphhopper.storage.GraphHopperStorageTest.createGHStorage(String,boolean)")
			.addTP("Rename Method", "com.graphhopper.storage.GraphHopperStorageTest.newGraph(Directory,boolean)", "com.graphhopper.storage.GraphHopperStorageTest.newGHStorage(Directory,boolean)")
			.addTP("Rename Method", "com.graphhopper.storage.GraphHopperStorageWithTurnCostsTest.newGraph(Directory,boolean)", "com.graphhopper.storage.GraphHopperStorageWithTurnCostsTest.newGHStorage(Directory,boolean)")
			.addTP("Rename Method", "com.graphhopper.storage.GraphStorageViaMMapTest.createGraph(String,boolean)", "com.graphhopper.storage.GraphStorageViaMMapTest.createGHStorage(String,boolean)")
			.addTP("Rename Method", "com.graphhopper.storage.index.AbstractLocationIndexTester.createGraph(Directory,EncodingManager,boolean)", "com.graphhopper.storage.index.AbstractLocationIndexTester.createGHStorage(Directory,EncodingManager,boolean)")
			.addTP("Extract Method", "com.graphhopper.storage.GraphHopperStorage.close()", "com.graphhopper.storage.BaseGraph.close()")
			.addTP("Extract Method", "com.graphhopper.storage.GraphHopperStorage.create(long)", "com.graphhopper.storage.BaseGraph.create(long)")
			.addTP("Extract Method", "com.graphhopper.storage.GraphHopperStorage.flush()", "com.graphhopper.storage.BaseGraph.flush()")
			.addTP("Extract Method", "com.graphhopper.storage.GraphHopperStorage.loadExisting()", "com.graphhopper.storage.BaseGraph.loadExisting(String)")
			.addTP("Extract Method", "com.graphhopper.storage.GraphHopperStorage.setAdditionalEdgeField(long,int)", "com.graphhopper.storage.BaseGraph.setAdditionalEdgeField(long,int)")
			.addTP("Extract Method", "com.graphhopper.storage.GraphHopperStorage.setSegmentSize(int)", "com.graphhopper.storage.BaseGraph.setSegmentSize(int)")
			.addTP("Extract Method", "com.graphhopper.storage.GraphHopperStorage.toDetailsString()", "com.graphhopper.storage.BaseGraph.toDetailsString()")
			.addTP("Extract Method", "com.graphhopper.storage.GraphHopperStorage._copyTo(GraphHopperStorage)", "com.graphhopper.storage.GraphHopperStorage.isCHPossible()")
			.addFP("Extract Method", "com.graphhopper.storage.GraphHopperStorage.loadExisting()", "com.graphhopper.storage.LevelGraphImpl.loadExisting()")
			.addFP("Move Method", "com.graphhopper.storage.GraphHopperStorage.EdgeIterable.copyPropertiesTo(EdgeIteratorState)", "com.graphhopper.storage.BaseGraph.AllEdgeIterator.copyPropertiesTo(EdgeIteratorState)")
			.addFP("Move Method", "com.graphhopper.storage.GraphHopperStorage.EdgeIterable.getAdditionalField()", "com.graphhopper.storage.BaseGraph.AllEdgeIterator.getAdditionalField()")
			.addFP("Move Method", "com.graphhopper.storage.GraphHopperStorage.EdgeIterable.getDistance()", "com.graphhopper.storage.BaseGraph.AllEdgeIterator.getDistance()")
			.addFP("Move Method", "com.graphhopper.storage.GraphHopperStorage.EdgeIterable.getName()", "com.graphhopper.storage.BaseGraph.AllEdgeIterator.getName()")
			.addFP("Move Method", "com.graphhopper.storage.GraphHopperStorage.EdgeIterable.setDistance(double)", "com.graphhopper.storage.BaseGraph.AllEdgeIterator.setDistance(double)")
			.addFP("Move Method", "com.graphhopper.storage.GraphHopperStorage.EdgeIterable.setName(String)", "com.graphhopper.storage.BaseGraph.AllEdgeIterator.setName(String)")
			.addFP("Move Method", "com.graphhopper.storage.GraphHopperStorage.EdgeIterable.toString()", "com.graphhopper.storage.BaseGraph.AllEdgeIterator.toString()")
			.addFP("Move Class", "com.graphhopper.storage.LevelGraphStorage.AllEdgeSkipIterator", "com.graphhopper.storage.LevelGraphImpl.AllEdgeSkipIterator")
			.addFP("Move Class", "com.graphhopper.storage.LevelGraphStorage.EdgeSkipIteratorImpl", "com.graphhopper.storage.LevelGraphImpl.EdgeSkipIteratorImpl")
			.addFP("Move Class", "com.graphhopper.storage.LevelGraphStorage.SingleLevelEdge", "com.graphhopper.storage.LevelGraphImpl.SingleLevelEdge")
			.addFP("Move Method", "com.graphhopper.storage.LevelGraphStorage.getBaseGraph()", "com.graphhopper.routing.util.TestAlgoCollector.AlgoHelperEntry.getBaseGraph()")
			.addFP("Move Method", "com.graphhopper.GraphHopper.getGraph()", "com.graphhopper.storage.GraphHopperStorage.getGraph(Class)")
			.addTP("Rename Method", "com.graphhopper.GraphHopper.getGraph()", "com.graphhopper.GraphHopper.getGraphHopperStorage()")
			.addTP("Rename Method", "com.graphhopper.GraphHopper.setGraph(GraphStorage)", "com.graphhopper.GraphHopper.setGraphHopperStorage(GraphHopperStorage)")
			.addTP("Rename Method", "com.graphhopper.routing.AbstractRoutingAlgorithmTester.createGraph(boolean)", "com.graphhopper.routing.AbstractRoutingAlgorithmTester.createGHStorage(boolean)")
			.addTP("Rename Method", "com.graphhopper.routing.ch.PrepareContractionHierarchiesTest.createGraph()", "com.graphhopper.routing.ch.PrepareContractionHierarchiesTest.createGHStorage()")
			.addFP("Rename Method", "com.graphhopper.storage.GraphHopperStorage.checkInit()", "com.graphhopper.storage.GraphHopperStorage.getGraph(Class)")
			.addFP("Rename Method", "com.graphhopper.storage.LevelGraphStorage.LevelGraphStorage(Directory,EncodingManager,boolean)", "com.graphhopper.storage.LevelGraphImpl.edge(int,int,double,boolean)")
			.addTP("Rename Method", "com.graphhopper.storage.LevelGraphStorageTest.newGraph(Directory,boolean)", "com.graphhopper.storage.GraphHopperStorageCHTest.newGHStorage(Directory,boolean)")
			.addTP("Rename Method", "com.graphhopper.storage.index.AbstractLocationIndexTester.createGraph(EncodingManager)", "com.graphhopper.storage.index.AbstractLocationIndexTester.createGHStorage(EncodingManager)")
			.addTP("Rename Method", "com.graphhopper.storage.index.LocationIndexTreeForLevelGraphTest.createGraph(Directory,EncodingManager,boolean)", "com.graphhopper.storage.index.LocationIndexTreeForLevelGraphTest.createGHStorage(Directory,EncodingManager,boolean)")
			.addTP("Rename Method", "com.graphhopper.util.GHUtility.guessStorage(Graph,Directory,EncodingManager)", "com.graphhopper.util.GHUtility.newStorage(GraphHopperStorage)")
			.addFP("Extract Method", "com.graphhopper.routing.AbstractRoutingAlgorithmTester.testTwoWeightsPerEdge2()", "com.graphhopper.routing.AbstractRoutingAlgorithmTester.getGraph(GraphHopperStorage)")
			.addFP("Extract Method", "com.graphhopper.routing.AbstractRoutingAlgorithmTester.testWithCoordinates()", "com.graphhopper.routing.AbstractRoutingAlgorithmTester.getGraph(GraphHopperStorage)")
			.addFP("Extract Method", "com.graphhopper.routing.ch.DijkstraBidirectionCHTest.createFactory(Graph,AlgorithmOptions)", "com.graphhopper.routing.ch.DijkstraBidirectionCHTest.getGraph(GraphHopperStorage)")
			.addFP("Extract Method", "com.graphhopper.storage.GraphHopperStorage.toString()", "com.graphhopper.storage.GraphHopperStorage.isCHPossible()")
			.addFP("Extract Method", "com.graphhopper.storage.GraphHopperStorage.toDetailsString()", "com.graphhopper.storage.LevelGraphImpl.toDetailsString()")
			.addFP("Inline Method", "com.graphhopper.storage.LevelGraphStorageTest.createGraph()", "com.graphhopper.storage.GraphHopperStorageCHTest.testPriosWhileDeleting()");
		
		commit("https://github.com/neo4j/neo4j.git", "b83e6a535cbca21d5ea764b0c49bfca8a9ff9db4")
			.addTP("Rename Method", "org.neo4j.kernel.api.impl.index.LuceneDocumentStructure.newMatchAllQuery()", "org.neo4j.kernel.api.impl.index.LuceneDocumentStructure.newAllQuery()")
			.addTP("Rename Method", "org.neo4j.kernel.api.impl.index.LuceneDocumentStructure.newQuery(Object)", "org.neo4j.kernel.api.impl.index.LuceneDocumentStructure.newValueQuery(Object)")
			.addTP("Rename Method", "org.neo4j.kernel.api.impl.index.LuceneDocumentStructure.newQueryForChangeOrRemove(long)", "org.neo4j.kernel.api.impl.index.LuceneDocumentStructure.newTermForChangeOrRemove(long)")
			.addTP("Rename Method", "org.neo4j.kernel.api.index.IndexAccessorCompatibility.getAllNodes(String)", "org.neo4j.kernel.api.index.IndexAccessorCompatibility.getAllNodesWithProperty(String)")
			.addFP("Pull Up Attribute", "org.neo4j.kernel.api.impl.index.LuceneUniqueIndexAccessorReaderTest.reader", "org.neo4j.kernel.api.impl.index.AbstractLuceneIndexAccessorReaderTest.reader");
		
		commit("https://github.com/CyanogenMod/android_frameworks_base.git", "910397f2390d6821a006991ed6035c76cbc74897")
			.addTP("Move Class", "com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback", "com.android.systemui.statusbar.policy.BatteryStateRegistar.BatteryStateChangeCallback")
			.addTP("Rename Method", "com.android.internal.os.BatteryStatsHelper.load()", "com.android.internal.os.BatteryStatsHelper.loadStats()")
			.addTP("Rename Method", "com.android.systemui.BatteryLevelTextView.setBatteryController(BatteryController)", "com.android.systemui.BatteryLevelTextView.setBatteryStateRegistar(BatteryStateRegistar)");
		
		commit("https://github.com/jenkinsci/workflow-plugin.git", "d0e374ce8ecb687b4dc046d1edea9e52da17706f")
			.addFP("Move Method", "org.jenkinsci.plugins.workflow.multibranch.SCMBinder.configureShell(CpsFlowExecution,GroovyShell)", "org.jenkinsci.plugins.workflow.multibranch.SCMBinder.Decorator.configureShell(CpsFlowExecution,GroovyShell)")
			.addFP("Rename Method", "org.jenkinsci.plugins.workflow.multibranch.SCMBinder.configureShell(CpsFlowExecution,GroovyShell)", "org.jenkinsci.plugins.workflow.multibranch.SCMBinder.create(FlowExecutionOwner,TaskListener,List)");
		
		commit("https://github.com/jersey/jersey.git", "d57b1401f874f96a53f1ec1c0f8a6089ae66a4ce")
			.addTP("Rename Class", "org.glassfish.jersey.ext.cdi1x.internal.DefaultHk2LocatorManager", "org.glassfish.jersey.ext.cdi1x.internal.SingleHk2LocatorManager")
			.addTP("Rename Class", "org.glassfish.jersey.tests.cdi.bv.CdiTest", "org.glassfish.jersey.tests.cdi.bv.RawCdiTest")
			.addTP("Rename Class", "org.glassfish.jersey.tests.cdi.bv.Hk2Test", "org.glassfish.jersey.tests.cdi.bv.RawHk2Test")
			.addTP("Rename Class", "org.glassfish.jersey.tests.cdi.resources.MyApplication", "org.glassfish.jersey.tests.cdi.resources.MainApplication")
			.addTP("Rename Method", "org.glassfish.jersey.ext.cdi1x.internal.CdiComponentProvider.listTypes(StringBuilder,Collection)", "org.glassfish.jersey.ext.cdi1x.internal.CdiComponentProvider.listElements(StringBuilder,Collection)")
			.addFP("Move Method", "org.glassfish.jersey.tests.cdi.bv.CdiTest.setUp()", "org.glassfish.jersey.tests.cdi.resources.SecondJaxRsInjectedCdiBeanTest.setUp()")
			.addFP("Move Method", "org.glassfish.jersey.tests.cdi.bv.CdiTest.tearDown()", "org.glassfish.jersey.tests.cdi.resources.SecondJaxRsInjectedCdiBeanTest.tearDown()")
			.addFP("Move Method", "org.glassfish.jersey.tests.cdi.bv.CdiTest.setUp()", "org.glassfish.jersey.tests.cdi.resources.SecondJaxRsInjectedCdiBeanTest.setUp()")
			.addFP("Move Method", "org.glassfish.jersey.tests.cdi.bv.CdiTest.tearDown()", "org.glassfish.jersey.tests.cdi.resources.SecondJaxRsInjectedCdiBeanTest.tearDown()")
			.addFP("Move Attribute", "org.glassfish.jersey.ext.cdi1x.internal.DefaultHk2LocatorManager.locator", "org.glassfish.jersey.ext.cdi1x.internal.GenericHk2LocatorManager.locator")
			.addFP("Move Attribute", "org.glassfish.jersey.tests.cdi.bv.CdiTest.weld", "org.glassfish.jersey.tests.cdi.bv.CombinedTest.weld")
			.addFP("Move Attribute", "org.glassfish.jersey.tests.cdi.bv.CdiTest.weld", "org.glassfish.jersey.tests.cdi.resources.NonJaxRsBeanJaxRsInjectionTest.weld")
			.addFP("Move Attribute", "org.glassfish.jersey.tests.cdi.bv.CdiTest.weld", "org.glassfish.jersey.tests.cdi.resources.SecondJaxRsInjectedCdiBeanTest.weld")
			.addFP("Rename Method", "org.glassfish.jersey.ext.cdi1x.internal.CdiComponentProvider.Hk2InjectedCdiTarget.getInjectionTargetClassLoader()", "org.glassfish.jersey.ext.cdi1x.internal.CdiComponentProvider.Hk2InjectedCdiTarget.Hk2InjectedCdiTarget(Class,InjectionTarget)");
		
		commit("https://github.com/gradle/gradle.git", "04bcfe98dbe7b05e508559930c21379ece845732")
			.addTP("Move Class", "org.gradle.internal.component.model.ComponentArtifactIdentifier", "org.gradle.api.artifacts.component.ComponentArtifactIdentifier")
			.addTP("Push Down Method", "org.gradle.api.internal.artifacts.ResolverResults.assertHasArtifacts()", "org.gradle.api.internal.artifacts.DefaultResolverResults.assertHasArtifacts()")
			.addTP("Push Down Method", "org.gradle.api.internal.artifacts.ResolverResults.assertHasResult()", "org.gradle.api.internal.artifacts.DefaultResolverResults.assertHasResult()")
			.addTP("Push Down Method", "org.gradle.api.internal.artifacts.ResolverResults.eachResolvedProject(Action)", "org.gradle.api.internal.artifacts.DefaultResolverResults.eachResolvedProject(Action)")
			.addTP("Push Down Method", "org.gradle.api.internal.artifacts.ResolverResults.failed(ResolveException)", "org.gradle.api.internal.artifacts.DefaultResolverResults.failed(ResolveException)")
			.addTP("Push Down Method", "org.gradle.api.internal.artifacts.ResolverResults.getArtifactsBuilder()", "org.gradle.api.internal.artifacts.DefaultResolverResults.getArtifactsBuilder()")
			.addTP("Push Down Method", "org.gradle.api.internal.artifacts.ResolverResults.getGraphResults()", "org.gradle.api.internal.artifacts.DefaultResolverResults.getGraphResults()")
			.addTP("Push Down Method", "org.gradle.api.internal.artifacts.ResolverResults.getTransientConfigurationResultsBuilder()", "org.gradle.api.internal.artifacts.DefaultResolverResults.getTransientConfigurationResultsBuilder()")
			.addTP("Push Down Method", "org.gradle.api.internal.artifacts.ResolverResults.resolved(ResolutionResult,ResolvedLocalComponentsResult)", "org.gradle.api.internal.artifacts.DefaultResolverResults.resolved(ResolutionResult,ResolvedLocalComponentsResult)")
			.addTP("Push Down Method", "org.gradle.api.internal.artifacts.ResolverResults.retainState(ResolvedGraphResults,ResolvedArtifactsBuilder,TransientConfigurationResultsBuilder)", "org.gradle.api.internal.artifacts.DefaultResolverResults.retainState(ResolvedGraphResults,ResolvedArtifactsBuilder,TransientConfigurationResultsBuilder)")
			.addTP("Push Down Method", "org.gradle.api.internal.artifacts.ResolverResults.withResolvedConfiguration(ResolvedConfiguration)", "org.gradle.api.internal.artifacts.DefaultResolverResults.withResolvedConfiguration(ResolvedConfiguration)")
			.addTP("Push Down Attribute", "org.gradle.api.internal.artifacts.ResolverResults.artifactResults", "org.gradle.api.internal.artifacts.DefaultResolverResults.artifactResults")
			.addTP("Push Down Attribute", "org.gradle.api.internal.artifacts.ResolverResults.fatalFailure", "org.gradle.api.internal.artifacts.DefaultResolverResults.fatalFailure")
			.addTP("Push Down Attribute", "org.gradle.api.internal.artifacts.ResolverResults.graphResults", "org.gradle.api.internal.artifacts.DefaultResolverResults.graphResults")
			.addTP("Push Down Attribute", "org.gradle.api.internal.artifacts.ResolverResults.resolutionResult", "org.gradle.api.internal.artifacts.DefaultResolverResults.resolutionResult")
			.addTP("Push Down Attribute", "org.gradle.api.internal.artifacts.ResolverResults.resolvedConfiguration", "org.gradle.api.internal.artifacts.DefaultResolverResults.resolvedConfiguration")
			.addTP("Push Down Attribute", "org.gradle.api.internal.artifacts.ResolverResults.resolvedLocalComponentsResult", "org.gradle.api.internal.artifacts.DefaultResolverResults.resolvedLocalComponentsResult")
			.addTP("Push Down Attribute", "org.gradle.api.internal.artifacts.ResolverResults.transientConfigurationResultsBuilder", "org.gradle.api.internal.artifacts.DefaultResolverResults.transientConfigurationResultsBuilder");
		
		commit("https://github.com/spring-projects/spring-framework.git", "dd4bc630c3de70204081ab196945d6b55ab03beb")
			.addTP("Extract Method", "org.springframework.aop.interceptor.AsyncExecutionInterceptor.invoke(MethodInvocation)", "org.springframework.aop.interceptor.AsyncExecutionAspectSupport.doSubmit(Callable,AsyncTaskExecutor,Class)");
		
		commit("https://github.com/droolsjbpm/drools.git", "c8e09e2056c54ead97bce4386a25b222154223b1")
			.addTP("Rename Class", "org.drools.core.base.ClassFieldAccessorCache.ByteArrayClassLoader", "org.drools.core.base.ClassFieldAccessorCache.DefaultByteArrayClassLoader")
			.addTP("Push Down Method", "org.drools.core.common.ProjectClassLoader.InternalTypesClassLoader.loadClass(String,boolean)", "org.drools.core.common.ProjectClassLoader.DefaultInternalTypesClassLoader.loadClass(String,boolean)")
			.addTP("Push Down Attribute", "org.drools.core.common.ProjectClassLoader.InternalTypesClassLoader.projectClassLoader", "org.drools.core.common.ProjectClassLoader.DefaultInternalTypesClassLoader.projectClassLoader")
			.addFP("Move Class", "org.drools.core.base.ClassFieldAccessorCache.ByteArrayClassLoader", "org.drools.core.util.ByteArrayClassLoader")
			.addFP("Extract Method", "org.drools.core.common.ProjectClassLoader.defineType(String,byte[])", "org.drools.core.common.ProjectClassLoader.makeClassLoader()")
			.addTP("Extract Method", "org.drools.core.rule.JavaDialectRuntimeData.onAdd(DialectRuntimeRegistry,ClassLoader)", "org.drools.core.rule.JavaDialectRuntimeData.makeClassLoader()")
			.addTP("Extract Method", "org.drools.core.rule.JavaDialectRuntimeData.reload()", "org.drools.core.rule.JavaDialectRuntimeData.makeClassLoader()");
		
		commit("https://github.com/hazelcast/hazelcast.git", "c00275e7f85c8a9af5785f66cc0f75dc027b6cb6")
			.addTP("Push Down Attribute", "com.hazelcast.jca.AbstractDeploymentTest.connectionFactory", "com.hazelcast.jca.XATransactionTest.connectionFactory")
			.addFP("Rename Method", "com.hazelcast.jca.AbstractDeploymentTest.deploymentHzRar()", "com.hazelcast.jca.AbstractDeploymentTest.createResourceAdapterDeployment()")
			.addTP("Rename Method", "com.hazelcast.jca.XATransactionTest.Init()", "com.hazelcast.jca.XATransactionTest.init()")
			.addFP("Move Attribute", "com.hazelcast.jca.AbstractDeploymentTest.connectionFactory", "com.hazelcast.jca.ConnectionFactoryImplTest.connectionFactory");
		
		commit("https://github.com/neo4j/neo4j.git", "4712de476aabe69cd762233c9641dd3cf9f8361b")
			.addTP("Rename Method", "org.neo4j.graphalgo.impl.centrality.EigenvectorCentralityArnoldi.runInternalArnoldi(int)", "org.neo4j.graphalgo.impl.centrality.EigenvectorCentralityArnoldi.runInternalIteration()")
			.addTP("Extract Method", "org.neo4j.graphalgo.impl.centrality.EigenvectorCentralityArnoldi.runInternalArnoldi(int)", "org.neo4j.graphalgo.impl.centrality.EigenvectorCentralityBase.incrementTotalIterations()")
			.addFP("Rename Method", "org.neo4j.graphalgo.impl.centrality.EigenvectorCentralityPower.runIterations(int)", "org.neo4j.graphalgo.impl.centrality.EigenvectorCentralityPower.runInternalIteration()")
			.addFP("Extract Method", "org.neo4j.graphalgo.impl.centrality.EigenvectorCentralityPower.runIterations(int)", "org.neo4j.graphalgo.impl.centrality.EigenvectorCentralityBase.incrementTotalIterations()")
			.addFP("Extract Method", "org.neo4j.graphalgo.impl.centrality.EigenvectorCentralityArnoldi.runIterations(int)", "org.neo4j.graphalgo.impl.centrality.EigenvectorCentralityBase.changeSignBasedOnFirstNoneZeroValue()");
		
		commit("https://github.com/apache/drill.git", "c1b847acdc8cb90a1498b236b3bb5c81ca75c044")
			.addTP("Pull Up Method", "org.apache.drill.exec.impersonation.TestImpersonationDisabledWithMiniDFS.addMiniDfsBasedStorage()", "org.apache.drill.exec.impersonation.BaseTestImpersonation.addMiniDfsBasedStorage(Map)")
			.addTP("Pull Up Method", "org.apache.drill.exec.impersonation.TestImpersonationMetadata.addMiniDfsBasedStorage()", "org.apache.drill.exec.impersonation.BaseTestImpersonation.addMiniDfsBasedStorage(Map)")
			.addTP("Move Method", "org.apache.drill.exec.store.hive.HiveTestDataGenerator.executeQuery(Driver,String)", "org.apache.drill.exec.hive.HiveTestUtilities.executeQuery(Driver,String)")
			.addTP("Extract Method", "org.apache.drill.exec.store.hive.HiveTestDataGenerator.getInstance()", "org.apache.drill.BaseTestQuery.getTempDir(String)")
			.addTP("Move Attribute", "org.apache.drill.exec.store.hive.schema.HiveSchemaFactory.tableLoaders", "org.apache.drill.exec.store.hive.DrillHiveMetaStoreClient.NonCloseableHiveClientWithCaching.tableLoaders")
			.addTP("Move Attribute", "org.apache.drill.exec.store.hive.schema.HiveSchemaFactory.tableNameLoader", "org.apache.drill.exec.store.hive.DrillHiveMetaStoreClient.NonCloseableHiveClientWithCaching.tableNameLoader")
			.addTP("Extract Method", "org.apache.drill.exec.store.hive.schema.HiveSchemaFactory.DatabaseLoader.load(String)", "org.apache.drill.exec.store.hive.DrillHiveMetaStoreClient.getDatabasesHelper(IMetaStoreClient)")
			.addTP("Extract Method", "org.apache.drill.exec.store.hive.schema.HiveSchemaFactory.TableLoader.load(String)", "org.apache.drill.exec.store.hive.DrillHiveMetaStoreClient.getHiveReadEntryHelper(IMetaStoreClient,String,String,Map)")
			.addTP("Extract Method", "org.apache.drill.exec.store.hive.schema.HiveSchemaFactory.TableNameLoader.load(String)", "org.apache.drill.exec.store.hive.DrillHiveMetaStoreClient.getTableNamesHelper(IMetaStoreClient,String)")
			.addFP("Move Method", "org.apache.drill.exec.store.hive.schema.HiveSchemaFactory.HiveSchema.getTableNames(String)", "org.apache.drill.exec.store.hive.DrillHiveMetaStoreClient.NonCloseableHiveClientWithCaching.getTableNames(String)")
			.addFP("Rename Method", "org.apache.drill.exec.impersonation.TestImpersonationDisabledWithMiniDFS.addMiniDfsBasedStorage()", "org.apache.drill.exec.impersonation.TestImpersonationDisabledWithMiniDFS.setup()")
			.addFP("Rename Method", "org.apache.drill.exec.impersonation.TestImpersonationMetadata.addMiniDfsBasedStorage()", "org.apache.drill.exec.impersonation.TestImpersonationMetadata.setup()")
			.addFP("Rename Method", "org.apache.drill.exec.impersonation.TestImpersonationQueries.addMiniDfsBasedStorageAndGenerateTestData()", "org.apache.drill.exec.impersonation.TestImpersonationQueries.createTestData()")
			.addFP("Extract Method", "org.apache.drill.exec.impersonation.TestImpersonationDisabledWithMiniDFS.addMiniDfsBasedStorage()", "org.apache.drill.exec.impersonation.BaseTestImpersonation.addMiniDfsBasedStorage(Map)")
			.addFP("Extract Method", "org.apache.drill.exec.impersonation.TestImpersonationMetadata.addMiniDfsBasedStorage()", "org.apache.drill.exec.impersonation.BaseTestImpersonation.addMiniDfsBasedStorage(Map)")
			.addFP("Extract Method", "org.apache.drill.exec.impersonation.TestImpersonationDisabledWithMiniDFS.addMiniDfsBasedStorage()", "org.apache.drill.exec.impersonation.TestImpersonationDisabledWithMiniDFS.createTestData()")
			.addTP("Extract Method", "org.apache.drill.exec.store.hive.HiveTestDataGenerator.generateTestData()", "org.apache.drill.BaseTestQuery.getTempDir(String)")
			.addFP("Extract Method", "org.apache.drill.exec.store.hive.schema.HiveSchemaFactory.HiveSchemaFactory(HiveStoragePlugin,String,Map)", "org.apache.drill.exec.store.hive.DrillHiveMetaStoreClient.createNonCloseableClientWithCaching(HiveConf,Map)")
			.addFP("Inline Method", "org.apache.drill.exec.util.ImpersonationUtil.createFileSystem(String,Configuration,OperatorStats)", "org.apache.drill.exec.util.ImpersonationUtil.createFileSystem(String,Configuration)")
			.addTP("Move Attribute", "org.apache.drill.exec.store.hive.schema.HiveSchemaFactory.databases", "org.apache.drill.exec.store.hive.DrillHiveMetaStoreClient.NonCloseableHiveClientWithCaching.databases");
		
		commit("https://github.com/facebook/presto.git", "364f50274d4b4b83d40930c0d2c4d0e57fb34589")
			.addTP("Rename Method", "com.facebook.presto.operator.HashGenerator.getPartitionHashBucket(int,int,Page)", "com.facebook.presto.operator.HashGenerator.getPartitionBucket(int,int,Page)")
			.addTP("Rename Method", "com.facebook.presto.sql.planner.PlanFragmenter.FragmentProperties.setHashPartitionedOutput(List,Optional)", "com.facebook.presto.sql.planner.PlanFragmenter.FragmentProperties.setPartitionedOutput(Optional,Optional)");
		
		commit("https://github.com/killbill/killbill.git", "4b5b74b6467a28fb9b7712f8091e4aa61c2d64b6")
			.addTP("Extract Method", "org.killbill.billing.payment.core.PaymentProcessor.toPayment(PaymentModelDao,Iterable,Iterable)", "org.killbill.billing.payment.core.PaymentProcessor.findPaymentTransactionInfoPlugin(PaymentTransactionModelDao,Iterable)");
		
		commit("https://github.com/hazelcast/hazelcast.git", "4d05a3b1168441216dcaea8282c39338285182af")
			.addTP("Extract Superclass", "com.hazelcast.cache.impl.client.CacheCreateConfigRequest$1", "com.hazelcast.spi.impl.SimpleExecutionCallback")
			.addTP("Extract Superclass", "com.hazelcast.cache.impl.client.CacheDestroyRequest$1", "com.hazelcast.spi.impl.SimpleExecutionCallback")
			.addTP("Rename Method", "com.hazelcast.map.impl.proxy.MapProxySupport.MapExecutionCallbackAdapter.notify(Object)", "com.hazelcast.map.impl.proxy.MapProxySupport.MapExecutionCallbackAdapter.onResponse(Object)")
			.addFP("Extract Method", "com.hazelcast.spi.impl.operationservice.impl.InvocationBuilderImpl.invoke()", "com.hazelcast.spi.InvocationBuilder.getTargetExecutionCallback()");

		commit("https://github.com/JetBrains/intellij-community.git", "6905d569a1e39d0d7b1ec5ceee4f0bbe60b85947")
			.addFP("Move Method", "com.jetbrains.edu.coursecreator.actions.CCRenameLesson.processRename(Project,PsiDirectory,Course)", "com.jetbrains.edu.coursecreator.CCRenameHandler.processRename(Named,String,Project)")
			.addFP("Move Method", "com.jetbrains.edu.coursecreator.actions.CCRenameTask.processRename(Project,PsiDirectory,Course)", "com.jetbrains.edu.coursecreator.CCRenameHandler.processRename(Named,String,Project)");
		
		commit("https://github.com/neo4j/neo4j.git", "77fab3caea4495798a248035f0e928f745c7c2db")
			.addFP("Rename Method", "org.neo4j.kernel.ha.lock.SlaveLocksClient.acquireExclusive(ResourceType,long[])", "org.neo4j.kernel.ha.lock.SlaveLocksClient.acquireShared(ResourceType,long)")
			.addFP("Rename Method", "org.neo4j.kernel.ha.lock.SlaveLocksClient.acquireShared(ResourceType,long[])", "org.neo4j.kernel.ha.lock.SlaveLocksClient.acquireExclusive(ResourceType,long)");
		
		commit("https://github.com/square/javapoet.git", "5a37c2aa596377cb4c9b6f916614407fd0a7d3db")
			.addFP("Rename Method", "com.squareup.javapoet.TypesTest.getElement(Class)", "com.squareup.javapoet.TypesTest.getElements()")
			.addFP("Rename Method", "com.squareup.javapoet.TypesTest.getVoidTypeMirror()", "com.squareup.javapoet.TypesTest.getTypes()");
		
		commit("https://github.com/apache/cassandra.git", "573a1d115b86abbe3fb53ff930464d7d8fd95600")
			.addFP("Extract Method", "org.apache.cassandra.net.MessagingService.incrementRejectedMessages(Verb)", "org.apache.cassandra.net.MessagingService.incrementDroppedMessages(DroppedMessages,boolean)");
		
		commit("https://github.com/eclipse/vert.x.git", "718782014519034b28f6d3182fd9d340b7b31a74")
			.addTP("Push Down Attribute", "io.vertx.core.net.impl.VertxHandler.connectionMap", "io.vertx.core.net.impl.VertxNetHandler.connectionMap");
	}
	
}

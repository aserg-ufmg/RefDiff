package refdiff.evaluation.benchmark;

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
		add(dataset.remove("https://github.com/aws/aws-sdk-java.git", "4baf0a4de8d03022df48d696d210cc8b3117d38a"));
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
        ;
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
        ;
        commit("https://github.com/BuildCraft/BuildCraft.git", "a5cdd8c4b10a738cb44819d7cc2fee5f5965d4a0")
            .addTP("Push Down Method", "buildcraft.api.robots.ResourceId.equals(Object)", "buildcraft.api.robots.ResourceIdRequest.equals(Object)")
            .addFP("Extract Method", "buildcraft.robotics.ai.AIRobotSearchStackRequest.getOrderFromRequestingStation(DockingStation,boolean)", "buildcraft.robotics.StackRequest.setStation(DockingStation)")
            .addFP("Extract Method", "buildcraft.robotics.boards.BoardRobotDelivery.delegateAIEnded(AIRobot)", "buildcraft.robotics.boards.BoardRobotDelivery.releaseCurrentRequest()")
            .addTP("Rename Method", "buildcraft.builders.TileBuilder.getAvailableRequest(int)", "buildcraft.builders.TileBuilder.getRequest(int)")
            .addTP("Rename Method", "buildcraft.builders.TileBuilder.getNumberOfRequests()", "buildcraft.builders.TileBuilder.getRequestsCount()")
            .addTP("Rename Method", "buildcraft.builders.TileBuilder.provideItemsForRequest(int,ItemStack)", "buildcraft.builders.TileBuilder.offerItem(int,ItemStack)")
            .addTP("Rename Method", "buildcraft.robotics.TileRequester.getNumberOfRequests()", "buildcraft.robotics.TileRequester.getRequestsCount()")
            .addTP("Rename Method", "buildcraft.robotics.TileRequester.provideItemsForRequest(int,ItemStack)", "buildcraft.robotics.TileRequester.offerItem(int,ItemStack)")
        ;
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
        ;
        commit("https://github.com/jersey/jersey.git", "d94ca2b27c9e8a5fa9fe19483d58d2f2ef024606")
            .addTP("Extract Method", "org.glassfish.jersey.client.HttpUrlConnectorProvider.getConnector(Client,Configuration)", "org.glassfish.jersey.client.HttpUrlConnectorProvider.createHttpUrlConnector(Client,ConnectionFactory,int,boolean,boolean)")
            .addTP("Extract Method", "org.glassfish.jersey.client.HttpUrlConnector._apply(ClientRequest)", "org.glassfish.jersey.client.internal.HttpUrlConnector.secureConnection(Client,HttpURLConnection)")
        ;
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
        ;
        commit("https://github.com/facebook/facebook-android-sdk.git", "19d1936c3b07d97d88646aeae30de747715e3248")
            .addTP("Move Method", "com.facebook.share.internal.ShareInternalUtility.newUploadPhotoRequest(String,AccessToken,Bitmap,String,Bundle,Callback)", "com.facebook.GraphRequest.newUploadPhotoRequest(AccessToken,String,Bitmap,String,Bundle,Callback)")
            .addTP("Move Method", "com.facebook.share.internal.ShareInternalUtility.newUploadPhotoRequest(String,AccessToken,File,String,Bundle,Callback)", "com.facebook.GraphRequest.newUploadPhotoRequest(AccessToken,String,File,String,Bundle,Callback)")
            .addTP("Move Method", "com.facebook.share.internal.ShareInternalUtility.newUploadPhotoRequest(String,AccessToken,Uri,String,Bundle,Callback)", "com.facebook.GraphRequest.newUploadPhotoRequest(AccessToken,String,Uri,String,Bundle,Callback)")
        ;
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
        ;
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
        ;
        commit("https://github.com/facebook/facebook-android-sdk.git", "e813a0be86c87366157a0201e6c61662cadee586")
            .addTP("Move Class", "com.facebook.iconicus.MainActivity", "com.example.iconicus.MainActivity")
        ;
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
        ;
    }

}

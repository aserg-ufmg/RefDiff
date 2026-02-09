
package io.crate.action.job;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import io.crate.Streamer;
import io.crate.breaker.CrateCircuitBreakerService;
import io.crate.breaker.RamAccountingContext;
import io.crate.core.collections.Bucket;
import io.crate.executor.transport.distributed.SingleBucketBuilder;
import io.crate.jobs.CountContext;
import io.crate.jobs.JobExecutionContext;
import io.crate.jobs.NestedLoopContext;
import io.crate.jobs.PageDownstreamContext;
import io.crate.operation.PageDownstream;
import io.crate.operation.PageDownstreamFactory;
import io.crate.planner.node.dql.CollectNode;
import io.crate.planner.node.dql.CountNode;
import io.crate.planner.node.dql.MergeNode;
import io.crate.planner.node.dql.join.NestedLoopNode;
import io.crate.types.DataTypes;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.common.breaker.CircuitBreaker;

private class InnerPreparer extends ExecutionNodeVisitor<PreparerContext, Void> {

  @Override
  public Void visitCountNode(CountNode node, PreparerContext context) {
    Map<String, Map<String, List<Integer>>> locations = node.routing().locations();
    if (locations == null) {
      throw new IllegalArgumentException("locations are empty. Can't start count operation");
    }
    context.directResultFuture = singleBucketBuilder.result();
    context.contextBuilder.addSubContext(node.executionNodeId(), countContext);
    return null;
  }

  @Override
  public Void visitMergeNode(MergeNode node, PreparerContext context) {
    RamAccountingContext ramAccountingContext = RamAccountingContext.forExecutionNode(circuitBreaker, node);
    ResultProvider downstream = resultProviderFactory.createDownstream(node, node.jobId());
    if (flatProjectorChain != null) {
      flatProjectorChain.startProjections(pageDownstreamContext);
    }
    return null;
  }

  @Override
  public Void visitCollectNode(final CollectNode node, final PreparerContext context) {
    RamAccountingContext ramAccountingContext = RamAccountingContext.forExecutionNode(circuitBreaker, node);
    ResultProvider downstream = collectOperation.createDownstream(node);

    if (ExecutionNodes.hasDirectResponseDownstream(node.downstreamNodes())) {
      context.directResultFuture = downstream.result();
    }
    final JobCollectContext jobCollectContext = new JobCollectContext(
        context.jobId,
        node,
        collectOperation,
        ramAccountingContext,
        downstream);
    context.contextBuilder.addSubContext(node.executionNodeId(), jobCollectContext);
    return null;
  }

  @Override
  public Void visitNestedLoopNode(NestedLoopNode node, PreparerContext context) {
    RamAccountingContext ramAccountingContext = RamAccountingContext.forExecutionNode(circuitBreaker, node);

    ResultProvider downstream = resultProviderFactory.createDownstream(node, node.jobId());

    NestedLoopContext nestedLoopContext = new NestedLoopContext(
        node,
        downstream,
        ramAccountingContext,
        pageDownstreamFactory,
        threadPool,
        streamerVisitor);

    context.contextBuilder.addSubContext(node.executionNodeId(), nestedLoopContext);
    return null;
  }
}

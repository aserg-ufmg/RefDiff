
package io.crate.operation.collect;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.*;
import io.crate.analyze.EvaluatingNormalizer;
import io.crate.exceptions.TableUnknownException;
import io.crate.exceptions.UnhandledServerException;
import io.crate.executor.transport.TransportActionProvider;
import io.crate.metadata.Functions;
import io.crate.metadata.ReferenceResolver;
import io.crate.metadata.table.TableInfo;
import io.crate.operation.*;
import io.crate.operation.collect.files.FileCollectInputSymbolVisitor;
import io.crate.operation.collect.files.FileInputFactory;
import io.crate.operation.collect.files.FileReadingCollector;
import io.crate.operation.projectors.FlatProjectorChain;
import io.crate.operation.projectors.ProjectionToProjectorVisitor;
import io.crate.operation.projectors.ResultProvider;
import io.crate.operation.projectors.ResultProviderFactory;
import io.crate.operation.reference.file.FileLineReferenceResolver;
import io.crate.operation.reference.sys.node.NodeSysExpression;
import io.crate.operation.reference.sys.node.NodeSysReferenceResolver;
import io.crate.planner.RowGranularity;
import io.crate.planner.node.dql.CollectNode;
import io.crate.planner.node.dql.FileUriCollectNode;
import io.crate.planner.symbol.ValueSymbolVisitor;
import org.elasticsearch.action.bulk.BulkRetryCoordinatorPool;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.Singleton;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.IndexShardMissingException;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.threadpool.ThreadPool;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * collect local data from node/shards/docs on nodes where the data resides (aka
 * Mapper nodes)
 */
@Singleton
public class MapSideDataCollectOperation implements CollectOperation, RowUpstream {

  private static final ESLogger LOGGER = Loggers.getLogger(MapSideDataCollectOperation.class);

  private static class VoidFunction<Arg> implements Function<Arg, Void> {
    @Nullable
    @Override
    public Void apply(@Nullable Arg input) {
      return null;
    }
  }

  public ResultProvider createDownstream(CollectNode collectNode) {
        return resultProviderFactory.createDownstream(collectNode, collectNode.jobId());
    };

  }
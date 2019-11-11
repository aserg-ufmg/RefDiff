package refdiff.evaluation.js;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.Repository;

import refdiff.core.diff.CstComparator;
import refdiff.core.diff.CstComparatorMonitor;
import refdiff.core.diff.CstDiff;
import refdiff.core.diff.CstRootHelper;
import refdiff.core.diff.Relationship;
import refdiff.core.diff.RelationshipType;
import refdiff.core.io.GitHelper;
import refdiff.core.io.SourceFileSet;
import refdiff.core.cst.CstNode;
import refdiff.core.util.PairBeforeAfter;
import refdiff.evaluation.ExternalProcess;
import refdiff.parsers.js.JsPlugin;

public class ComputeRecallJs {
	
	private static final File tempFolder = new File("tmp");
	
	public static void main(String[] args) throws Exception {
		tempFolder.mkdirs();
		
		
		computeRecall("Move Function",
			commit("https://github.com/webpack/webpack/commit/b50d4cf7c370dc0f9fa2c39ea0e73e28ca8918ac", RelationshipType.MOVE, node("lib/WebpackOptionsValidationError.js", "getSchemaPartText"), node("lib/util/getSchemaPartText.js", "getSchemaPartText")),
			// SAME
			// commit("https://github.com/atom/atom/commit/fc620b9e80d67ca99f962431461b8fc4d085d9df", RelationshipType.MOVE, node("spec/tooltip-manager-spec.js", "createElement"), node("spec/tooltip-manager-spec.js", "createElement")),
			commit("https://github.com/webpack/webpack/commit/8b3772d47fc94fe3c3175602bba5eef6605fad86", RelationshipType.INTERNAL_MOVE, node("lib/EntryOptionPlugin.js", "itemToPlugin"), node("lib/EntryOptionPlugin.js", "itemToPlugin")),
			// SAME
			// commit("https://github.com/atom/atom/commit/44a2be7c9dc99a091fb3fb8c207b8c4bce7a609b", RelationshipType.MOVE, node("spec/workspace-spec.js", "simulateReload"), node("spec/workspace-spec.js", "simulateReload")),
			commit("https://github.com/facebook/create-react-app/commit/fbdff9d722d6ce669a090138022c4d3536ae95bb", RelationshipType.MOVE, node("packages/react-scripts/scripts/build.js", "getDifferenceLabel"), node("packages/react-dev-utils/FileSizeReporter.js", "getDifferenceLabel")),
			commit("https://github.com/facebook/react/commit/8fbcd499bd07c8a8206f23fe0b2a1cbbc1ffaf0a", RelationshipType.MOVE, node("src/renderers/shared/stack/reconciler/ReactUpdateQueue.js", "validateCallback"), node("src/renderers/shared/utils/validateCallback.js", "validateCallback")),
			commit("https://github.com/d3/d3/commit/959da21882ad7ea5f35f851adb629ae7a29d5a38", RelationshipType.INTERNAL_MOVE, node("d3.js", "start"), node("d3.js", "start")),
			//commit("https://github.com/mui-org/material-ui/commit/b869605290595bce27c50ee81ffa3f596a3b8b9b", RelationshipType.MOVE, node("src/mixins/controllable.js", "getValueLink"), node("src/menus/menu.jsx", "getValueLink")),
			commit("https://github.com/facebook/react/commit/63aa7259b9f48886af545afcc06c29acf225b05f", RelationshipType.MOVE, node("src/browser/ui/getReactRootElementInContainer.js", "getReactRootElementInContainer"), node("src/browser/ui/ReactMount.js", "getReactRootElementInContainer")),
			commit("https://github.com/chartjs/Chart.js/commit/ec7b87d69c6168d25166784796f8026b2cb5715e", RelationshipType.MOVE, node("src/charts/chart.bar.js", "calculateBarWidth"), node("src/scales/scale.category.js", "calculateBarWidth")),
			commit("https://github.com/angular/angular.js/commit/560951e9881b5f772262804384b4da9f673b925e", RelationshipType.INTERNAL_MOVE, node("src/ng/interpolate.js", "stringify"), node("src/ng/interpolate.js", "stringify")),
			commit("https://github.com/angular/angular.js/commit/2636105c5e363f14cda890f19ac9c3bc57556dd2", RelationshipType.MOVE, node("test/testabilityPatch.js", "toEqualError"), node("test/matchers.js", "toEqualError")),
			commit("https://github.com/mrdoob/three.js/commit/ca803d97c0e4fab6eadd3f745f3068443e8ca1f4", RelationshipType.MOVE, node("src/extras/core/NURBSCurve.js", "findSpan"), node("src/extras/core/NURBSUtils.js", "findSpan"))
		);
		
		computeRecall("Move And Rename Function",
			commit("https://github.com/meteor/meteor/commit/643a9f12da263044829f90ea06cbb1d6c1afe91e", RelationshipType.MOVE_RENAME, node("tools/cli/commands.js", "installDefaultNpmDeps"), node("tools/cli/default-npm-deps.js", "install")),
			commit("https://github.com/mui-org/material-ui/commit/7796fb2677b547bec838eeaf4c219ffb00825c49", RelationshipType.MOVE_RENAME, node("src/styles/theme-manager.js", "merge"), node("src/utils/extend.js", "extend")),
			commit("https://github.com/webpack/webpack/commit/5da9d8c7ef29f954a37f58f5138f116579c6efe8", RelationshipType.MOVE_RENAME, node("lib/Entrypoint.js", "getSize"), node("lib/SizeFormatHelpers.js", "getEntrypointSize")),
			commit("https://github.com/webpack/webpack/commit/86c00207bdc9cb1ef60441d1ec836624a162c9ab", RelationshipType.MOVE_RENAME, node("lib/ModuleParserHelpers.js", "addParsedVariable"), node("lib/ParserHelpers.js", "addParsedVariableToModule")),
			commit("https://github.com/meteor/meteor/commit/b5286b941a77a1bdd57abb1ea01385eeaa62f7ea", RelationshipType.MOVE_RENAME, node("packages/browser-policy/browser-policy.js", "disallowAllContent"), node("packages/browser-policy-content/browser-policy-content.js", "disallowAll")),
			commit("https://github.com/meteor/meteor/commit/91a4a46ea1d687de1f929e3b9f0bae9c2db0c83d", RelationshipType.INTERNAL_MOVE_RENAME, node("packages/liveui/liveui.js", "patch"), node("packages/liveui/liveui.js", "_patch")),
			// MOVE_RENAME browser-policy.js -> browser-policy-content.js induces SAME relationship on children
			commit("https://github.com/angular/angular.js/commit/af0ad6561c0d75c4f155b07e9cfc36a983af55bd", RelationshipType.MOVE_RENAME, node("src/JSON.js", "jsonReplacer"), node("src/Angular.js", "toJsonReplacer"))
		);
		
		computeRecall("Extract Function",
			//commit("https://github.com/facebook/react/commit/6f2ea73978168372f33a6dfad6c049afddc4aef3", RelationshipType.EXTRACT, node("packages/react-reconciler/src/ReactFiberScheduler.js", "resetStack"), node("packages/react-reconciler/src/ReactFiberScheduler.js", "rethrowOriginalError")),
			commit("https://github.com/atom/atom/commit/dec52a7384d9f241cae0cd2d0a51f440bafa7677", RelationshipType.EXTRACT, node("src/workspace.js", "toggle"), node("src/workspace.js", "hide")),
			commit("https://github.com/facebook/react/commit/24a83a5eeb1ccf4da1bdd97166d6c7c94d821bd8", RelationshipType.EXTRACT, node("src/renderers/shared/fiber/ReactFiberScheduler.js", "commitAllWork"), node("src/renderers/shared/fiber/ReactFiberScheduler.js", "commitAllHostEffects")),
			commit("https://github.com/chartjs/Chart.js/commit/b64cab004669a010025e4641eb7f359c4035f6b9", RelationshipType.EXTRACT, node("src/core/core.tooltip.js", "draw"), node("src/core/core.tooltip.js", "drawBackground")),
			commit("https://github.com/facebook/react/commit/b4b21486aa043b3c6260665930e7e638c908a5ec", RelationshipType.EXTRACT, node("src/renderers/shared/fiber/ReactFiberBeginWork.js", "beginWork"), node("src/renderers/shared/fiber/ReactFiberBeginWork.js", "updateHostRoot")),
			commit("https://github.com/angular/angular.js/commit/c39936ee26f00d8256c79df07096f03196811df5", RelationshipType.EXTRACT, node("src/ngRoute/route.js", "commitRoute"), node("src/ngRoute/route.js", "resolveLocalsFor")),
			commit("https://github.com/angular/angular.js/commit/4adc9a9117e7f7501d94c22c8cbbfeb77ad5d596", RelationshipType.EXTRACT, node("src/ngRoute/route.js", "commitRoute"), node("src/ngRoute/route.js", "getTemplateFor")),
			commit("https://github.com/meteor/meteor/commit/adc5e40fb74ad2f1b4bea172a1214d46d2e6686c", RelationshipType.EXTRACT, node("tools/files.js", "linkToMeteorScript"), node("tools/files.js", "_generateScriptLinkToMeteorScript")),
			commit("https://github.com/d3/d3/commit/0a39765f3cbcb940e6fb354cec010a7ffeba9289", RelationshipType.EXTRACT, node("d3.js", "d3_geo_circleClipPolygon"), node("d3.js", "d3_geo_circleLinkCircular")),
			// The function body has changed a lot`
			commit("https://github.com/d3/d3/commit/2eba0320407442e85be97b2cd84398fb6c626fe3", RelationshipType.EXTRACT, node("d3.js", "ticks"), node("d3.js", "d3_scale_linearTicks")),
			commit("https://github.com/angular/angular.js/commit/d9ca2459172a3ad62f0a19b8b1306d739c4b75b7", RelationshipType.EXTRACT, node("src/auto/injector.js", "anonFn"), node("src/auto/injector.js", "extractArgs"))
		);
		
		computeRecall("Inline Function",
			// Different file
			commit("https://github.com/vuejs/vue/commit/ae07fedf8ab00a000db56a155f2e2fdaa6daeff2", RelationshipType.INLINE, node("src/shared/util.js", "isFunction"), node("src/core/vdom/helpers/resolve-async-component.js", "resolveAsyncComponent")),
			commit("https://github.com/meteor/meteor/commit/ec3341e7adb89889deadc1d3ecd8d8a181b958f1", RelationshipType.INLINE, node("packages/dynamic-import/cache.js", "put"), node("packages/dynamic-import/cache.js", "flushSetMany")),
			commit("https://github.com/meteor/meteor/commit/d4d3df14285e559c92d5294b04be97ebb26517fd", RelationshipType.INLINE, node("tools/isobuild/resolver.js", "_resolvePkgJsonMain"), node("tools/isobuild/resolver.js", "_resolve")),
			// Threshold?
			commit("https://github.com/facebook/react/commit/47783e878d62ed96ea27290d1cdbb90b83c417c4", RelationshipType.INLINE, node("packages/react-dom/src/client/DOMPropertyOperations.js", "deleteValueForProperty"), node("packages/react-dom/src/client/DOMPropertyOperations.js", "setValueForProperty")),
			// Different file
			commit("https://github.com/facebook/react/commit/5c6a496d98b80d19d851c2ec3a56d760b7118a50", RelationshipType.INLINE, node("src/shared/utils/getIteratorFn.js", "getIteratorFn"), node("src/isomorphic/classic/element/ReactElementValidator.js", "getDeclarationErrorAddendum"))
		);
		
		computeRecall("Rename Function",
			// Score is low when function body is small and the name changes
			commit("https://github.com/mrdoob/three.js/commit/f0e7bdc1de54a1b896089d819872111a86aa4185", RelationshipType.RENAME, node("src/renderers/WebGLRenderer.js", "start"), node("src/renderers/WebGLRenderer.js", "startAnimation")),
			commit("https://github.com/mrdoob/three.js/commit/6d916aed6b31f4d860efb52f6b94e3d3ce49d1ba", RelationshipType.RENAME, node("src/loaders/ImageLoader.js", "loadListener"), node("src/loaders/ImageLoader.js", "onInternalLoad")),
			commit("https://github.com/facebook/react-native/commit/1d6ce2311f6a51821b33c5473414d70c8bd34425", RelationshipType.RENAME, node("Libraries/Image/AssetSourceResolver.js", "scaledAssetURLInScript"), node("Libraries/Image/AssetSourceResolver.js", "scaledAssetURLNearBundle")),
			commit("https://github.com/atom/atom/commit/50088b16c9a4e6fda78ea98430bb0705229883e8", RelationshipType.RENAME, node("src/text-editor-component.js", "isVerticalScrollbarVisible"), node("src/text-editor-component.js", "canScrollVertically")),
			commit("https://github.com/facebook/react/commit/71f591501b639c4adf329e1f586c7e04875dde7f", RelationshipType.RENAME, node("src/renderers/shared/fiber/ReactFiberScheduler.js", "findNextUnitOfWork"), node("src/renderers/shared/fiber/ReactFiberScheduler.js", "resetNextUnitOfWork")),
			commit("https://github.com/mrdoob/three.js/commit/8e8589c88105b4f9258867f61aadd70c9fccffaa", RelationshipType.RENAME, node("src/extras/core/Bezier.js", "b2p0"), node("src/extras/core/Bezier.js", "QuadraticBezierP0")),
			commit("https://github.com/facebook/react/commit/6144212a8634948faf18cce8211c71e6f9d0667e", RelationshipType.RENAME, node("src/renderers/shared/fiber/ReactFiberScheduler.js", "performLowPriWork"), node("src/renderers/shared/fiber/ReactFiberScheduler.js", "performDeferredWork")),
			commit("https://github.com/angular/angular.js/commit/e9bf93d510a6a0c105d8f5d036ec35c7ce08a588", RelationshipType.RENAME, node("src/Angular.js", "int"), node("src/Angular.js", "toInt")),
			commit("https://github.com/angular/angular.js/commit/097947fd3bd280fcf621e36154a9d4f82896ff01", RelationshipType.RENAME, node("src/Angular.js", "resumeBootstrapInternal"), node("src/Angular.js", "doBootstrap")),
			commit("https://github.com/meteor/meteor/commit/9d65f9269982e066de112ccf7e22dfeb2528ba58", RelationshipType.RENAME, node("packages/accounts-base/url_client.js", "onEnrollAccountLink"), node("packages/accounts-base/url_client.js", "onEnrollmentLink"))
		);
		
		computeRecall("Rename File",
			commit("https://github.com/facebook/react-native/commit/57daad98f01b59fce9cb9bf663fd0b191c56b232", RelationshipType.RENAME, node("Libraries/Components/StaticContainer.js", "StaticContainer.js"), node("Libraries/Components/StaticContainer.react.js", "StaticContainer.react.js")),
			commit("https://github.com/mui-org/material-ui/commit/c451323da71fa0ec5f7505d14f50d92f0c91f824", RelationshipType.RENAME, node("docs/src/app/components/pages/get-started/examples.jsx", "examples.jsx"), node("docs/src/app/components/pages/get-started/Examples.jsx", "Examples.jsx")),
			// A lot of added code
			commit("https://github.com/facebook/react/commit/7b2d9655da218f8311d1dab4ab1142c35c3eef3b", RelationshipType.RENAME, node("src/renderers/shared/stack/event/ReactSyntheticEvent.js", "ReactSyntheticEvent.js"), node("src/renderers/shared/stack/event/ReactSyntheticEventType.js", "ReactSyntheticEventType.js")),
			commit("https://github.com/meteor/meteor/commit/25bddd9a23dda6b309804e052232b09637250f42", RelationshipType.RENAME, node("packages/html5-tokenizer/html5-tokenizer.js", "html5-tokenizer.js"), node("packages/html5-tokenizer/html5_tokenizer.js", "html5_tokenizer.js")),
			commit("https://github.com/meteor/meteor/commit/48947a46bf7204367dcd3294c93d55e2f36e01eb", RelationshipType.RENAME, node("packages/test-helpers/test_events.js", "test_events.js"), node("packages/test-helpers/simulate_event.js", "simulate_event.js")),
			commit("https://github.com/facebook/react-native/commit/369e30f6859bc6af2a97670217ba142691bd49d9", RelationshipType.RENAME, node("Libraries/ReactNative/ReactNativeNativeComponent.js", "ReactNativeNativeComponent.js"), node("Libraries/ReactNative/ReactNativeBaseComponent.js", "ReactNativeBaseComponent.js")),
			commit("https://github.com/meteor/meteor/commit/e710338a9490adbcfd0519d8dbfe1d3b34cb4ab9", RelationshipType.RENAME, node("packages/livedata/stream_client.js", "stream_client.js"), node("packages/livedata/stream_client_sockjs.js", "stream_client_sockjs.js")),
			commit("https://github.com/facebook/react/commit/48e54da484bc3416572fb48bc22d748a60ecd54f", RelationshipType.RENAME, node("vendor/fbtransform/transforms/xjs.js", "xjs.js"), node("vendor/fbtransform/transforms/jsx.js", "jsx.js")),
			// Some added code. The only change is the rename, this makes the idf computation less effective
			commit("https://github.com/facebook/react/commit/366600d0b2b99ece8cd03d60e2a5454a02857502", RelationshipType.RENAME, node("packages/react-dom/src/client/__tests__/dangerouslySetInnerHTML-test.js", "dangerouslySetInnerHTML-test.js"), node("packages/react-dom/src/client/__tests__/setInnerHTML-test.js", "setInnerHTML-test.js")),
			commit("https://github.com/facebook/react-native/commit/c948ae81686e2345d155577b4ff9f43f50021e99", RelationshipType.RENAME, node("packager/src/node-haste/index.js", "index.js"), node("packager/src/node-haste/DependencyGraph.js", "DependencyGraph.js"))
		);
		
		computeRecall("Move File",
			commit("https://github.com/webpack/webpack/commit/756f2ca1779fd9836412041cbc9baa7912d490ae", RelationshipType.MOVE, node("test/configCases/plugins/dll-plugin/webpack.config.js", "webpack.config.js"), node("test/configCases/plugins/lib-manifest-plugin/webpack.config.js", "webpack.config.js")),
			commit("https://github.com/vuejs/vue/commit/9bded22a83b6fb9a89a32009e7f47f6201e167a3", RelationshipType.MOVE, node("test/weex/runtime/component/richtext.spec.js", "richtext.spec.js"), node("test/weex/runtime/components/richtext.spec.js", "richtext.spec.js")),
			commit("https://github.com/facebook/react/commit/08ff3d749d353a5ef5daf9fea78c456c5ebd048e", RelationshipType.MOVE, node("packages/react/src/classic/__tests__/ReactContextValidator-test.js", "ReactContextValidator-test.js"), node("packages/react/src/__tests__/ReactContextValidator-test.js", "ReactContextValidator-test.js")),
			commit("https://github.com/facebook/react/commit/313611572b6567d229367ed20ff63d1bca8610bb", RelationshipType.MOVE, node("packages/react-dom/src/shared/__tests__/CSSPropertyOperations-test.js", "CSSPropertyOperations-test.js"), node("packages/react-dom/src/__tests__/CSSPropertyOperations-test.js", "CSSPropertyOperations-test.js")),
			commit("https://github.com/meteor/meteor/commit/6b1bb038d820cbb4a6a066990da12e4c70d6e68d", RelationshipType.MOVE, node("tools/files.js", "files.js"), node("tools/fs/files.js", "files.js")),
			commit("https://github.com/meteor/meteor/commit/8a8db83d298a932ed72157ee0162792d9f592cd9", RelationshipType.MOVE, node("tools/mongo-exit-codes.js", "mongo-exit-codes.js"), node("tools/utils/mongo-exit-codes.js", "mongo-exit-codes.js")),
			commit("https://github.com/reduxjs/redux/commit/acc10fac4bd381cbf143f9488ac886ed1475b19a", RelationshipType.MOVE, node("src/decorators/root.js", "root.js"), node("src/addons/root.js", "root.js")),
			commit("https://github.com/meteor/meteor/commit/39d8aef3d96c3f744e926f87ee05d67a8e2f58d5", RelationshipType.MOVE, node("tools/selftest.js", "selftest.js"), node("tools/tool-testing/selftest.js", "selftest.js")),
			commit("https://github.com/facebook/react/commit/6c885d28c51ea30af0d8a4031dedcea98ef4114c", RelationshipType.MOVE, node("src/renderers/native/vendor/react/platform/NodeHandle.js", "NodeHandle.js"), node("src/renderers/native/NodeHandle/NodeHandle.js", "NodeHandle.js")),
			commit("https://github.com/meteor/meteor/commit/f49de5b9ac20ffed0144ad2bbbc249faa2c1fe91", RelationshipType.MOVE, node("tools/auth-client.js", "auth-client.js"), node("tools/meteor-services/auth-client.js", "auth-client.js"))
		);
		
		computeRecall("Move And Rename File",
			commit("https://github.com/webpack/webpack/commit/9156be961d890b9877ddef3a70964c9665662abb", RelationshipType.MOVE_RENAME, node("lib/BaseWasmMainTemplatePlugin.js", "BaseWasmMainTemplatePlugin.js"), node("lib/wasm/WasmMainTemplatePlugin.js", "WasmMainTemplatePlugin.js")),
			//commit("https://github.com/facebook/react/commit/b92f947af1b5d8804026cb0e1cfa59ead7484ca5", RelationshipType.MOVE_RENAME, node("packages/react-scheduler/npm/umd/react-scheduler-tracking.development.js", "react-scheduler-tracking.development.js"), node("packages/schedule/npm/umd/schedule-tracking.development.js", "schedule-tracking.development.js")),
			commit("https://github.com/facebook/react-native/commit/a50b4ea7b9ecc4c95a38b58f12a71b93ff3a3131", RelationshipType.MOVE_RENAME, node("Libraries/Animated/Animated.js", "Animated.js"), node("Libraries/Animated/src/AnimatedImplementation.js", "AnimatedImplementation.js")),
			commit("https://github.com/meteor/meteor/commit/4ad860ca47071c167281888e984f15ba987355bd", RelationshipType.MOVE_RENAME, node("tools/shell/server.js", "server.js"), node("tools/server/shell-server.js", "shell-server.js"))
		);
	}
	
	private static void computeRecall(String title, boolean ...results) {
		int tp = 0;
		int fn = 0;
		for (boolean result : results) {
			if (result) {
				tp++;
			} else {
				fn++;
			} 
		}
		double recall = ((double) tp) / (tp + fn);
		System.out.println(String.format("%s\tTP: %d, FN: %d, Recall: %.3f", title, tp, fn, recall));
	}
	
//	private static boolean debugCommit(String commit, RelationshipType relType, String n1, String n2) throws Exception {
//		return commit(true, commit, relType, n1, n2);
//	}
	private static boolean commit(String commit, RelationshipType relType, String n1, String n2) throws Exception {
		return commit(false, commit, relType, n1, n2);
	}
	private static boolean commit(boolean debug, String commit, RelationshipType relType, String n1, String n2) throws Exception {
		CstDiff diff = diff(commit, debug);
		
		Set<String> refactorings = diff.getRelationships().stream()
			.filter(r -> r.getType() != RelationshipType.SAME)
			.map(r -> format(r))
			.collect(Collectors.toSet());
		
		String refactoring = format(relType, n1, n2);
		if (refactorings.contains(refactoring)) {
			return true;
		} else {
			System.out.println(commit);
			System.out.println("  Found:");
			for (String ref : refactorings) {
				System.out.println("    " + ref);
			}
			System.out.println("  Not found:");
			System.out.println("    " + refactoring);
			return false;
		}
	}

	private static String format(Relationship r) {
		return format(r.getType(), format(r.getNodeBefore()), format(r.getNodeAfter()));
	}
	
	private static String format(RelationshipType relType, String n1, String n2) {
		return String.format("%s\t%s\t%s", relType, n1, n2);
	}
	
	private static String format(CstNode node) {
		return node.getLocation().getFile() + ":" + node.getLocalName();
	}
	
	private static String node(String file, String localName) {
		return file + ":" + localName;
	}

	private static CstDiff diff(String commitUrl, boolean debug) throws Exception, IOException {
		String[] url = commitUrl.split("/commit/");
		String commit = url[1];
		String project = url[0].substring(url[0].lastIndexOf("/") + 1);
		File repoFolder = new File(tempFolder, project + ".git");
		String cloneUrl = "https://github.com/refdiff-study/" + project + ".git";
		
		if (!repoFolder.exists()) {
			ExternalProcess.execute(tempFolder, "git", "clone", cloneUrl, repoFolder.getName(), "--bare");
		}
		//ExternalProcess.execute(repoFolder, "git", "fetch", "--depth=5000");
		
		GitHelper gh = new GitHelper();
		try (JsPlugin parser = new JsPlugin();
			Repository repo = gh.openRepository(repoFolder)) {
			CstComparator cstComparator = new CstComparator(parser);
			
			PairBeforeAfter<SourceFileSet> sources = gh.getSourcesBeforeAndAfterCommit(repo, commit, parser.getAllowedFilesFilter());
			if (debug) {
				return cstComparator.compare(sources.getBefore(), sources.getAfter(), new Monitor());
			} else {
				return cstComparator.compare(sources);
			}
		}
	}
	
	private static class Monitor implements CstComparatorMonitor {
		
		@Override
		public void beforeCompare(CstRootHelper<?> before, CstRootHelper<?> after) {
			after.printRelationships(System.out);
		}
		
		public void reportDiscardedMatch(CstNode n1, CstNode n2, double score) {
			System.out.println(String.format("Threshold %.3f\t%s%s", score, format(n1), format(n2)));
		}
		
		public void reportDiscardedConflictingMatch(CstNode n1, CstNode n2) {
			System.out.println(String.format("Conflicting match\t%s%s", format(n1), format(n2)));
		}
		
		public void reportDiscardedExtract(CstNode n1, CstNode n2, double score) {
			System.out.println(String.format("Extract threshold %.3f\t%s%s", format(n1), format(n2)));
		}
		
		public void reportDiscardedInline(CstNode n1, CstNode n2, double score) {
			System.out.println(String.format("Inline threshold %.3f\t%s%s", format(n1), format(n2)));
		}
	}
	
}

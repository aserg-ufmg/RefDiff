package lsclipse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lsclipse.rules.ChangeBidirectionalAssociationToUni;
import lsclipse.rules.ChangeUnidirectionalAssociationToBi;
import lsclipse.rules.ConsolidateConditionalExpression;
import lsclipse.rules.ConsolidateDuplicateConditionalFragment;
import lsclipse.rules.DecomposeConditional;
import lsclipse.rules.EncapsulateCollection;
import lsclipse.rules.ExtractMethod;
import lsclipse.rules.FormTemplateMethod;
import lsclipse.rules.InlineMethod;
import lsclipse.rules.InlineTemp;
import lsclipse.rules.IntroduceAssertion;
import lsclipse.rules.IntroduceExplainingVariable;
import lsclipse.rules.IntroduceNullObject;
import lsclipse.rules.IntroduceParamObject;
import lsclipse.rules.MoveMethod;
import lsclipse.rules.ParameterizeMethod;
import lsclipse.rules.PreserveWholeObject;
import lsclipse.rules.RemoveAssignmentToParameters;
import lsclipse.rules.RemoveControlFlag;
import lsclipse.rules.RenameMethod;
import lsclipse.rules.ReplaceArrayWithObject;
import lsclipse.rules.ReplaceConditionalWithPolymorphism;
import lsclipse.rules.ReplaceDataValueWithObject;
import lsclipse.rules.ReplaceExceptionWithTest;
import lsclipse.rules.ReplaceMethodWithMethodObject;
import lsclipse.rules.ReplaceNestedCondWithGuardClauses;
import lsclipse.rules.ReplaceParameterWithExplicitMethods;
import lsclipse.rules.ReplaceSubclassWithField;
import lsclipse.rules.ReplaceTypeCodeWithState;
import lsclipse.rules.ReplaceTypeCodeWithSubclasses;
import lsclipse.rules.Rule;
import lsclipse.rules.RuleFactory;
import lsclipse.rules.SeparateQueryFromModifier;
import metapackage.MetaInfo;
import tyRuBa.engine.FrontEnd;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.tdbc.Connection;
import tyRuBa.tdbc.PreparedQuery;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

public class TopologicalSort {
	FrontEnd frontend = null;
	private RuleFactory ruleFactory = new RuleFactory();
	boolean loadInitFile = true;
	File dbDir = null;
	int cachesize = FrontEnd.defaultPagerCacheSize;
	private boolean backgroundPageCleaning = false;

	public Map<String, List<String>> dependents = new HashMap<String, List<String>>();

	public Set<Node> getGraph() {
		return Collections.unmodifiableSet(graph);
	}

	private Set<Node> graph;
	private ArrayList<String> written_strs;

	public TopologicalSort() {
		graph = new HashSet<Node>();
		written_strs = new ArrayList<String>();

		RefactoringQuery move_field = new RefactoringQuery(
				"move_field",
				"deleted_field(?fFullName, ?fShortName, ?tFullName), "
						+ "added_field(?f1FullName, ?fShortName, ?t1FullName), "
						+ "deleted_accesses(?fFullName, ?mFullName), "
						+ "added_accesses(?f1FullName, ?nFullName), "
						+ "before_type(?tFullName, ?, ?package), "
						+ "after_type(?t1FullName, ?, ?package), "
						+ "NOT(equals(?tFullName, ?t1FullName))");
		move_field.addType("?fShortName");
		move_field.addType("?tFullName");
		move_field.addType("?t1FullName");
		Node move_field_node = new Node(move_field);
		graph.add(move_field_node);

		Node move_method_node = new Node(
				(new MoveMethod()).getRefactoringQuery());
		graph.add(move_method_node);

		Node rename_method_node = new Node(
				(new RenameMethod()).getRefactoringQuery());
		graph.add(rename_method_node);

		String push_down_field_query = "move_field(?fShortName, ?tParentFullName, ?tChildFullName), "
				+ "before_subtype(?tParentFullName, ?tChildFullName)";
		RefactoringQuery push_down_field = new RefactoringQuery(
				"push_down_field", push_down_field_query);
		push_down_field.addType("?fShortName");
		push_down_field.addType("?tParentFullName");
		push_down_field.addType("?tChildFullName");
		Node push_down_field_node = new Node(push_down_field);
		push_down_field_node.addChild(move_field_node);
		graph.add(push_down_field_node);

		String push_down_method_query = "move_method(?mShortName, ?tParentFullName, ?tChildFullName), "
				+ "before_subtype(?tParentFullName, ?tChildFullName)";
		RefactoringQuery push_down_method = new RefactoringQuery(
				"push_down_method", push_down_method_query);
		push_down_method.addType("?mShortName");
		push_down_method.addType("?tParentFullName");
		push_down_method.addType("?tChildFullName");
		Node push_down_method_node = new Node(push_down_method);
		push_down_method_node.addChild(move_method_node);
		graph.add(push_down_method_node);

		Node extract_method_node = new Node(
				(new ExtractMethod()).getRefactoringQuery());
		graph.add(extract_method_node);

		Node decompose_conditional_node = new Node(
				(new DecomposeConditional()).getRefactoringQuery());
		decompose_conditional_node.addChild(extract_method_node);
		graph.add(decompose_conditional_node);

		String pull_up_field_query = "move_field(?fShortName, ?tChildFullName, ?tParentFullName), "
				+ "before_subtype(?tParentFullName, ?tChildFullName)";
		RefactoringQuery pull_up_field = new RefactoringQuery("pull_up_field",
				pull_up_field_query);
		pull_up_field.addType("?fShortName");
		pull_up_field.addType("?tChildFullName");
		pull_up_field.addType("?tParentFullName");
		Node pull_up_field_node = new Node(pull_up_field);
		pull_up_field_node.addChild(move_field_node);
		graph.add(pull_up_field_node);

		String pull_up_method_query = "move_method(?mShortName, ?tChildFullName, ?tParentFullName), "
				+ "before_subtype(?tParentFullName, ?tChildFullName)";
		RefactoringQuery pull_up_method = new RefactoringQuery(
				"pull_up_method", pull_up_method_query);
		pull_up_method.addType("?mShortName");
		pull_up_method.addType("?tChildFullName");
		pull_up_method.addType("?tParentFullName");
		Node pull_up_method_node = new Node(pull_up_method);
		pull_up_method_node.addChild(move_method_node);
		graph.add(pull_up_method_node);

		// (deleted_subtype and (pull_up_field or _method)) or
		// (before_subtype and parent deleted and (push_down_field or _method))
		String collapse_hierarchy_query = "("
				+ "deleted_subtype(?tParentFullName, ?tChildFullName), "
				+ "("
				+ "pull_up_field(?fuShortName, ?tChildFullName, ?tParentFullName);"
				+ "pull_up_method(?muShortName, ?tChildFullName, ?tParentFullName)"
				+ "));"
				+ "("
				+ "before_subtype(?tParentFullName, ?tChildFullName),"
				+ "deleted_type(?tParentFullName, ?tParentShortName, ?),"
				+ "("
				+ "push_down_field(?fdShortName, ?tParentFullName, ?tChildFullName);"
				+ "push_down_method(?mdShortName, ?tParentFullName, ?tChildFullName)"
				+ "))";
		RefactoringQuery collapse_hierarchy = new RefactoringQuery(
				"collapse_hierarchy", collapse_hierarchy_query);
		collapse_hierarchy.addType("?tParentFullName");
		collapse_hierarchy.addType("?tChildFullName");
		Node collapse_hierarchy_node = new Node(collapse_hierarchy);
		collapse_hierarchy_node.addChild(push_down_method_node);
		collapse_hierarchy_node.addChild(push_down_field_node);
		collapse_hierarchy_node.addChild(pull_up_method_node);
		collapse_hierarchy_node.addChild(pull_up_field_node);
		graph.add(collapse_hierarchy_node);

		String remove_parameter_query = "deleted_parameter(?mFullName, ?paramList, ?delParam)";
		RefactoringQuery remove_parameter = new RefactoringQuery(
				"remove_parameter", remove_parameter_query);
		remove_parameter.addType("?mFullName");
		remove_parameter.addType("?delParam");
		Node remove_parameter_node = new Node(remove_parameter);
		graph.add(remove_parameter_node);

		Node inline_method_node = new Node(
				(new InlineMethod()).getRefactoringQuery());
		graph.add(inline_method_node);

		String add_parameter_query = "added_parameter(?mFullName, ?paramList, ?addParam)";
		RefactoringQuery add_parameter = new RefactoringQuery("add_parameter",
				add_parameter_query);
		add_parameter.addType("?mFullName");
		add_parameter.addType("?addParam");
		Node add_parameter_node = new Node(add_parameter);
		graph.add(add_parameter_node);

		Node replace_method_with_method_object_node = new Node(
				(new ReplaceMethodWithMethodObject()).getRefactoringQuery());
		graph.add(replace_method_with_method_object_node);

		String extract_class_query = "added_type(?newtFullName, ?newtShortName, ?),"
				+ "before_type(?tFullName, ?tShortName, ?pkg),"
				+ "after_type(?tFullName, ?tShortName, ?pkg),"
				+ "added_field(?fFullName, ?, ?tFullName),"
				+ "added_fieldoftype(?fFullName, ?newtFullName),"
				+ "("
				+ "move_field(?fShortName, ?tFullName, ?newtFullName);"
				+ "move_method(?mShortName, ?tFullName, ?newtFullName)" + ")";
		RefactoringQuery extract_class = new RefactoringQuery("extract_class",
				extract_class_query);
		extract_class.addType("?newtFullName");
		extract_class.addType("?tFullName");

		String inline_class_query = "deleted_type(?oldtFullName, ?oldtShortName, ?),"
				+ "before_type(?tFullName, ?tShortName, ?pkg),"
				+ "after_type(?tFullName, ?tShortName, ?pkg),"
				+ "deleted_field(?fFullName, ?, ?tFullName),"
				+ "deleted_fieldoftype(?fFullName, ?oldtFullName),"
				+ "("
				+ "move_field(?fShortName, ?oldtFullName, ?tFullName);"
				+ "move_method(?mShortname, ?oldtFullName, ?tFullName)" + ")";
		RefactoringQuery inline_class = new RefactoringQuery("inline_class",
				inline_class_query);
		inline_class.addType("?oldtFullName");
		inline_class.addType("?tFullName");
		Node inline_class_node = new Node(inline_class);
		inline_class_node.addChild(move_method_node);
		inline_class_node.addChild(move_field_node);
		graph.add(inline_class_node);

		String hide_delegate_query = "after_method(?clientmFullName, ?, ?clienttFullName),"
				+ "deleted_calls(?clientmFullName, ?delegatemFullName),"
				+ "added_calls(?clientmFullName, ?servermFullName),"
				+ "added_method(?servermFullName, ?, ?servertFullName),"
				+ "added_calls(?servermFullName, ?delegatemFullName),"
				+ "before_field(?delegatefFullName, ?, ?servertFullName),"
				+ "after_field(?delegatefFullName, ?, ?servertFullName),"
				+ "before_fieldoftype(?delegatefFullName, ?delegatetFullName),"
				+ "after_fieldoftype(?delegatefFullName, ?delegatetFullName),"
				+ "after_method(?delegatemFullName, ?, ?delegatetFullName),"
				+ "NOT(equals(?clienttFullName, ?servertFullName)),"
				+ "NOT(equals(?delegatetFullName, ?servertFullName))";
		RefactoringQuery hide_delegate = new RefactoringQuery("hide_delegate",
				hide_delegate_query);
		hide_delegate.addType("?delegatetFullName");
		hide_delegate.addType("?servertFullName");
		hide_delegate.addType("?clienttFullName");
		Node hide_delegate_node = new Node(hide_delegate);
		graph.add(hide_delegate_node);

		String remove_middle_man_query = "after_method(?clientmFullName, ?, ?clienttFullName),"
				+ "added_calls(?clientmFullName, ?delegatemFullName),"
				+ "deleted_calls(?clientmFullName, ?servermFullName),"
				+ "deleted_method(?servermFullName, ?, ?servertFullName),"
				+ "deleted_calls(?servermFullName, ?delegatemFullName),"
				+ "before_field(?delegatefFullName, ?, ?servertFullName),"
				+ "after_field(?delegatefFullName, ?, ?servertFullName),"
				+ "before_fieldoftype(?delegatefFullName, ?delegatetFullName),"
				+ "after_fieldoftype(?delegatefFullName, ?delegatetFullName),"
				+ "after_method(?delegatemFullName, ?, ?delegatetFullName),"
				+ "NOT(equals(?clienttFullName, ?servertFullName)),"
				+ "NOT(equals(?delegatetFullName, ?servertFullName))";
		RefactoringQuery remove_middle_man = new RefactoringQuery(
				"remove_middle_man", remove_middle_man_query);
		remove_middle_man.addType("?delegatetFullName");
		remove_middle_man.addType("?servertFullName");
		remove_middle_man.addType("?clienttFullName");
		Node remove_middle_man_node = new Node(remove_middle_man);
		graph.add(remove_middle_man_node);

		String introduce_local_extension_query = "("
				+ "added_subtype(?tSuperFullName,?tSubFullName);"
				+ "("
				+ "added_type(?tSubFullName, ?, ?),"
				+ "added_field(?superInstanceFieldFullName, ?, ?tSubFullName),"
				+ "added_fieldoftype(?superInstanceFieldFullName, ?tSuperFullName)"
				+ ")),"
				+ "added_method(?subCtorFullName, \"<init>()\", ?tSubFullName),"
				// +
				// "after_parameter(?subCtorFullName, ?tSuperParamFullName, ?),"
				// //TODO(kprete): make sure this is the right type (otherwise,
				// its useless)
				+ "move_method(?mShortName, ?tClientFullName, ?tSubFullName),"
				+ "before_method(?mBeforeFullName, ?mShortName, ?tClientFullName),"
				+ "after_method(?mAfterFullName, ?mShortName, ?tSubFullName),"
				+ "deleted_calls(?mClientFullName, ?mBeforeFullName),"
				+ "after_type(?tClientFullName, ?, ?),"
				+ "added_calls(?mClientFullName, ?mAfterFullName)";
		RefactoringQuery introduce_local_extension = new RefactoringQuery(
				"introduce_local_extension", introduce_local_extension_query);
		introduce_local_extension.addType("?tSubFullName");
		introduce_local_extension.addType("?tSuperFullName");
		Node introduce_local_extension_node = new Node(
				introduce_local_extension);
		introduce_local_extension_node.addChild(move_method_node);
		graph.add(introduce_local_extension_node);

		String replace_ctor_with_factory_method_query = "added_method(?mFactFullName, ?, ?tFullName),"
				+ "after_method(?mCtorFullName,\"<init>()\",?tFullName),"
				+ "added_calls(?mFactFullName, ?mCtorFullName),"
				// + "deleted_calls(?mClientFullName, ?mCtorFullName),"
				// + "added_calls(?mClientFullName, ?mFactFullName),"
				+ "added_methodmodifier(?mCtorFullName,\"private\")";
		RefactoringQuery replace_ctor_with_factory_method = new RefactoringQuery(
				"replace_constructor_with_factory_method",
				replace_ctor_with_factory_method_query);
		replace_ctor_with_factory_method.addType("?mCtorFullName");
		replace_ctor_with_factory_method.addType("?mFactFullName");
		Node replace_ctor_with_factory_method_node = new Node(
				replace_ctor_with_factory_method);
		graph.add(replace_ctor_with_factory_method_node);

		Node replace_data_value_with_object_node = new Node(
				(new ReplaceDataValueWithObject()).getRefactoringQuery());
		graph.add(replace_data_value_with_object_node);

		RefactoringQuery replace_magic_number_with_const = new RefactoringQuery(
				"replace_magic_number_with_constant",
				"added_field(?fFullName, ?, ?tFullName),"
						+ "added_fieldmodifier(?fFullName, \"final\"),"
						+ "before_method(?mFullName, ?, ?tFullName), "
						+ "added_accesses(?fFullName, ?mFullName)");
		replace_magic_number_with_const.addType("?mFullName");
		replace_magic_number_with_const.addType("?fFullName");
		Node replace_magic_number_with_const_node = new Node(
				replace_magic_number_with_const);
		graph.add(replace_magic_number_with_const_node);

		Node replace_array_with_object_node = new Node(
				(new ReplaceArrayWithObject()).getRefactoringQuery());
		graph.add(replace_array_with_object_node);

		Node change_uni_to_bi_node = new Node(
				(new ChangeUnidirectionalAssociationToBi())
						.getRefactoringQuery());
		graph.add(change_uni_to_bi_node);

		Node change_bi_to_uni_node = new Node(
				(new ChangeBidirectionalAssociationToUni())
						.getRefactoringQuery());
		graph.add(change_bi_to_uni_node);

		Node encapsulate_collection = new Node(
				(new EncapsulateCollection()).getRefactoringQuery());
		graph.add(encapsulate_collection);

		Node replace_nested_cond_guard_clauses = new Node(
				(new ReplaceNestedCondWithGuardClauses()).getRefactoringQuery());
		graph.add(replace_nested_cond_guard_clauses);

		Node separate_query_from_modifier_node = new Node(
				(new SeparateQueryFromModifier()).getRefactoringQuery());
		graph.add(separate_query_from_modifier_node);

		Node parameterize_method_node = new Node(
				(new ParameterizeMethod()).getRefactoringQuery());
		graph.add(parameterize_method_node);

		Node replace_parameter_with_methods_node = new Node(
				(new ReplaceParameterWithExplicitMethods())
						.getRefactoringQuery());
		graph.add(replace_parameter_with_methods_node);

		Node preserve_whole_object_node = new Node(
				(new PreserveWholeObject()).getRefactoringQuery());
		graph.add(preserve_whole_object_node);

		RefactoringQuery replace_param_with_method = new RefactoringQuery(
				"replace_param_with_method",
				"deleted_calls(?clientmFullName, ?othermFullName),"
						+ "before_calls(?clientmFullName, ?mFullName),"
						+ "after_calls(?clientmFullName, ?mFullName),"
						+ "added_calls(?mFullName, ?othermFullName),"
						+ "deleted_parameter(?mFullName, ?, ?paramName),"
						+ "NOT(added_parameter(?mFullName, ?, ?))");
		replace_param_with_method.addType("?mFullName");
		replace_param_with_method.addType("?paramName");
		replace_param_with_method.addType("?othermFullName");
		Node replace_param_with_method_node = new Node(
				replace_param_with_method);
		graph.add(replace_param_with_method_node);

		RefactoringQuery hide_method = new RefactoringQuery("hide_method",
				"deleted_methodmodifier(?mFullName, \"public\"),"
						+ "added_methodmodifier(?mFullName, \"private\")");
		hide_method.addType("?mFullName");
		Node hide_method_node = new Node(hide_method);
		graph.add(hide_method_node);

		RefactoringQuery pull_up_ctor_body = new RefactoringQuery(
				"pull_up_constructor_body",
				"added_method(?supermFullName, \"<init>()\", ?supertFullName),"
						+ "before_subtype(?supertFullName, ?subtFullName),"
						+ "after_subtype(?supertFullName, ?subtFullName),"
						+ "before_method(?submFullName, \"<init>()\", ?subtFullName),"
						+ "after_method(?submFullName, \"<init>()\", ?subtFullName),"
						+ "added_calls(?submFullName, ?supermFullName)");
		pull_up_ctor_body.addType("?supertFullName");
		Node pull_up_ctor_body_node = new Node(pull_up_ctor_body);
		graph.add(pull_up_ctor_body_node);

		RefactoringQuery extract_subclass = new RefactoringQuery(
				"extract_subclass",
				"added_subtype(?superTFullName, ?tFullName),"
						+ "NOT(before_type(?tFullName, ?, ?)),"
						+ "(move_field(?fShortName, ?superTFullName, ?tFullName);"
						+ "move_method(?mShortName, ?superTFullName, ?tFullName))");
		extract_subclass.addType("?superTFullName");
		extract_subclass.addType("?tFullName");
		Node extract_subclass_node = new Node(extract_subclass);
		extract_subclass_node.addChild(move_method_node);
		extract_subclass_node.addChild(move_field_node);
		graph.add(extract_subclass_node);

		RefactoringQuery extract_superclass = new RefactoringQuery(
				"extract_superclass",
				"added_subtype(?tFullName, ?subtFullName),"
						+ "NOT(before_type(?tFullName, ?, ?)),"
						+ "(move_field(?fShortName, ?subtFullName, ?tFullName);"
						+ "move_method(?mShortName, ?subtFullName, ?tFullName))");
		extract_superclass.addType("?subtFullName");
		extract_superclass.addType("?tFullName");
		Node extract_superclass_node = new Node(extract_superclass);
		extract_superclass_node.addChild(move_method_node);
		extract_superclass_node.addChild(move_field_node);
		graph.add(extract_superclass_node);

		Node consolidate_cond_expression_node = new Node(
				(new ConsolidateConditionalExpression()).getRefactoringQuery());
		consolidate_cond_expression_node.addChild(extract_method_node);
		graph.add(consolidate_cond_expression_node);

		RefactoringQuery extract_interface = new RefactoringQuery(
				"extract_interface",
				"added_type(?interfacetFullName, ?,?),"
						+ "added_implements(?interfacetFullName, ?othertFullName),"
						+ "before_type(?othertFullName, ?, ?)");
		extract_interface.addType("?interfacetFullName");
		extract_interface.addType("?othertFullName");
		Node extract_interface_node = new Node(extract_interface);
		graph.add(extract_interface_node);

		Node replace_exception_with_test_node = new Node(
				(new ReplaceExceptionWithTest()).getRefactoringQuery());
		graph.add(replace_exception_with_test_node);

		RefactoringQuery change_value_to_reference = new RefactoringQuery(
				"change_value_to_reference",
				"replace_constructor_with_factory_method(?mCtorFullName, ?mFactFullName),"
						+ "added_method(?mFactFullName, ?, ?tFullName),"
						+ "before_fieldoftype(?, ?tFullName),"
						+ "after_fieldoftype(?, ?tFullName),"
						+ "NOT(after_method(?, \"equals()\", ?tFullName);"
						+ "after_method(?, \"hashCode()\", ?tFullName))");
		change_value_to_reference.addType("?tFullName");
		Node change_value_to_reference_node = new Node(
				change_value_to_reference);
		change_value_to_reference_node
				.addChild(replace_ctor_with_factory_method_node);
		graph.add(change_value_to_reference_node);

		RefactoringQuery change_reference_to_value = new RefactoringQuery(
				"change_reference_to_value",
				"deleted_method(?mFactFullName, ?, ?tFullName),"
						+ "before_method(?mCtorFullName,\"<init>()\",?tFullName),"
						+ "deleted_calls(?mFactFullName, ?mCtorFullName),"
						+ "added_calls(?mClientFullName, ?mCtorFullName),"
						+ "deleted_calls(?mClientFullName, ?mFactFullName),"
						+ "deleted_methodmodifier(?mCtorFullName,\"private\"),"
						+ "after_fieldoftype(?, ?tFullName),"
						+ "before_fieldoftype(?, ?tFullName),"
						+ "after_method(?, \"equals()\", ?tFullName),"
						+ "after_method(?, \"hashCode()\", ?tFullName)");
		change_reference_to_value.addType("?tFullName");
		Node change_reference_to_value_node = new Node(
				change_reference_to_value);
		change_reference_to_value_node
				.addChild(replace_ctor_with_factory_method_node);
		graph.add(change_reference_to_value_node);

		Node consolidate_duplicate_cond_fragments_node = new Node(
				(new ConsolidateDuplicateConditionalFragment())
						.getRefactoringQuery());
		graph.add(consolidate_duplicate_cond_fragments_node);

		RefactoringQuery encapsulate_downcast = new RefactoringQuery(
				"encapsulate_downcast",
				"added_cast(?, ?tFullName, ?mFullName),"
						+ "added_return(?mFullName, ?tFullName),"
						+ "deleted_return(?mFullName, ?oldtFullName),"
						+ "(after_subtype(?oldtFullName, ?tFullName);"
						+ "(after_subtype(?oldtFullName, ?othertFullName),"
						+ "after_subtype(?othertFullName, ?tFullName)))");
		encapsulate_downcast.addType("?mFullName");
		encapsulate_downcast.addType("?tFullName");
		Node encapsulate_downcast_node = new Node(encapsulate_downcast);
		graph.add(encapsulate_downcast_node);

		Node introduce_assertion_node = new Node(
				(new IntroduceAssertion()).getRefactoringQuery());
		graph.add(introduce_assertion_node);

		RefactoringQuery tease_apart_inheritance = new RefactoringQuery(
				"tease_apart_inheritance", "before_type(?gptFullName, ?,?),"
						+ "after_type(?gptFullName, ?,?),"
						+ "before_subtype(?gptFullName, ?p1tFullName),"
						+ "after_subtype(?gptFullName, ?p1tFullName),"
						+ "before_subtype(?gptFullName, ?p2tFullName),"
						+ "after_subtype(?gptFullName, ?p2tFullName),"
						+ "NOT(equals(?p1tFullName, ?p2tFullName)),"
						+ "deleted_subtype(?p1tFullName, ?t1FullName),"
						+ "deleted_subtype(?p2tFullName, ?t2FullName),"
						+ "added_field(?fFullName, ?, ?gptFullName),"
						+ "added_fieldoftype(?fFullName, ?newptFullName),"
						+ "added_type(?newptFullName, ?, ?),"
						+ "added_subtype(?newptFullName, ?newt1FullName),"
						+ "added_subtype(?newptFullName, ?newt2FullName),"
						+ "(move_field(?, ?t1FullName, ?newt1FullName);"
						+ "move_method(?, ?t1FullName, ?newt1FullName);"
						+ "equals(?t1FullName, ?newt1FullName)),"
						+ "(move_field(?, ?t2FullName, ?newt2FullName);"
						+ "move_method(?, ?t2FullName, ?newt2FullName);"
						+ "equals(?t2FullName, ?newt2FullName))");
		tease_apart_inheritance.addType("?gptFullName");
		Node tease_apart_inheritance_node = new Node(tease_apart_inheritance);
		tease_apart_inheritance_node.addChild(move_method_node);
		tease_apart_inheritance_node.addChild(move_field_node);
		graph.add(tease_apart_inheritance_node);

		Node introduce_null_object_node = new Node(
				(new IntroduceNullObject()).getRefactoringQuery());
		graph.add(introduce_null_object_node);

		RefactoringQuery replace_inheritance_with_delegation = new RefactoringQuery(
				"replace_inheritance_with_delegation",
				"deleted_subtype(?delegate, ?delegatingObj),"
						+ "added_fieldoftype(?fFullName, ?delegate),"
						+ "added_field(?fFullName, ?, ?delegatingObj)");
		replace_inheritance_with_delegation.addType("?delegate");
		replace_inheritance_with_delegation.addType("?delegatingObj");
		Node replace_inheritance_with_delegation_node = new Node(
				replace_inheritance_with_delegation);
		graph.add(replace_inheritance_with_delegation_node);

		RefactoringQuery replace_delegation_with_inheritance = new RefactoringQuery(
				"replace_delegation_with_inheritance",
				"added_subtype(?delegate, ?delegatingObj), "
						+ "deleted_fieldoftype(?fFullName, ?delegate), "
						+ "deleted_field(?fFullName, ?, ?delegatingObj)");
		replace_delegation_with_inheritance.addType("?delegate");
		replace_delegation_with_inheritance.addType("?delegatingObj");
		Node replace_delegation_with_inheritance_node = new Node(
				replace_delegation_with_inheritance);
		graph.add(replace_delegation_with_inheritance_node);

		RefactoringQuery replace_type_code_with_class_query = new RefactoringQuery(
				"replace_type_code_with_class",
				"deleted_field(?old_fFullName1, ?fShortName1, ?tFullName), "
						+ "deleted_field(?old_fFullName2, ?fShortName2, ?tFullName), "
						+ "NOT(equals(?fShortName1, ?fShortName2)), "
						+ "added_field(?new_fFullName1, ?fShortName1, ?tCodeFullName), "
						+ "added_field(?new_fFullName2, ?fShortName2, ?tCodeFullName), "
						+ "added_type(?tCodeFullName, ?, ?), "
						+ "added_fieldmodifier(?new_fFullName1, \"static\"), "
						+ "added_fieldmodifier(?new_fFullName2, \"static\"), "
						+ "deleted_fieldmodifier(?old_fFullName1, \"static\"), "
						+ "deleted_fieldmodifier(?old_fFullName2, \"static\"), "
						+ "added_fieldoftype(?new_fFullName1, ?tCodeFullName), "
						+ "added_fieldoftype(?new_fFullName2, ?tCodeFullName), "
						+ "deleted_fieldoftype(?fFullName, ?), "
						+ "added_fieldoftype(?fFullName, ?tCodeFullName)");
		replace_type_code_with_class_query.addType("?tFullName");
		replace_type_code_with_class_query.addType("?tCodeFullName");
		Node replace_type_code_with_class_node = new Node(
				replace_type_code_with_class_query);
		// graph.add(replace_type_code_with_class_node);

		Node remove_assignment_to_parameters_node = new Node(
				(new RemoveAssignmentToParameters()).getRefactoringQuery());
		graph.add(remove_assignment_to_parameters_node);

		RefactoringQuery encapsulate_field = new RefactoringQuery(
				"encapsulate_field",
				"deleted_fieldmodifier(?fFullName,\"public\")," // TODO(kprete): I think we can remove this!
						+ "added_fieldmodifier(?fFullName,\"private\"),"
						+ "added_getter(?mGetFullName, ?fFullName),"
						+ "added_setter(?mSetFullName, ?fFullName)");
		encapsulate_field.addType("?fFullName");
		Node encapsulate_field_node = new Node(encapsulate_field);
		graph.add(encapsulate_field_node);

		RefactoringQuery remove_setting_method = new RefactoringQuery(
				"remove_setting_method",
				"added_fieldmodifier(?fFullName,\"final\"),"
						+ "deleted_setter(?mFullName,?fFullName)");
		remove_setting_method.addType("?mFullName");
		remove_setting_method.addType("?fFullName");
		Node remove_setting_method_node = new Node(remove_setting_method);
		graph.add(remove_setting_method_node);

		RefactoringQuery replace_error_code_with_exception = new RefactoringQuery(
				"replace_error_code_with_exception",
				"deleted_return(?mFullName, ?oldReturnType),"
						+ "added_return(?mFullName, \"void\"),"
						+ "added_throws(?mFullName, ?tFullName)");
		replace_error_code_with_exception.addType("?mFullName");
		replace_error_code_with_exception.addType("?oldReturnType");
		replace_error_code_with_exception.addType("?tFullName");
		Node replace_error_code_with_exception_node = new Node(
				replace_error_code_with_exception);
		graph.add(replace_error_code_with_exception_node);

		Node replace_type_code_with_subclasses_node = new Node(
				(new ReplaceTypeCodeWithSubclasses()).getRefactoringQuery());
		// graph.add(replace_type_code_with_subclasses_node);

		Node introduce_parameter_object_node = new Node(
				(new IntroduceParamObject()).getRefactoringQuery());
		introduce_parameter_object_node.addChild(remove_parameter_node);
		introduce_parameter_object_node.addChild(add_parameter_node);
		graph.add(introduce_parameter_object_node);

		Node replace_type_code_with_state_node = new Node(
				(new ReplaceTypeCodeWithState()).getRefactoringQuery());
		// graph.add(replace_type_code_with_state_node);

		Node replace_conditional_with_polymorphism_node = new Node(
				(new ReplaceConditionalWithPolymorphism())
						.getRefactoringQuery());
		graph.add(replace_conditional_with_polymorphism_node);

		RefactoringQuery self_encapsulate_field = new RefactoringQuery(
				"self_encapsulate_field",
				"added_getter(?mGetFullName, ?fFullName),"
						+ "before_field(?fFullName, ?, ?), "
						+ "added_setter(?mSetFullName, ?fFullName)");
		self_encapsulate_field.addType("?fFullName");
		Node self_encapsulate_field_node = new Node(self_encapsulate_field);
		self_encapsulate_field_node.addChild(encapsulate_field_node);
		graph.add(self_encapsulate_field_node);

		RefactoringQuery extract_hierarchy = new RefactoringQuery(
				"extract_hierarchy",
				"replace_type_code_with_subclasses(?tFullName);"
						+ "replace_type_code_with_state(?tFullName, ?);"
						+ "(replace_conditional_with_polymorphism(?mFullName, ?),"
						+ "before_method(?mFullName, ?, ?tFullName))");
		extract_hierarchy.addType("?tFullName");
		Node extract_hierarchy_node = new Node(extract_hierarchy);
		extract_hierarchy_node
				.addChild(replace_conditional_with_polymorphism_node);
		// extract_hierarchy_node.addChild(replace_type_code_with_state_node);
		// extract_hierarchy_node.addChild(replace_type_code_with_subclasses_node);
		graph.add(extract_hierarchy_node);

		Node form_template_method_node = new Node(
				(new FormTemplateMethod()).getRefactoringQuery());
		graph.add(form_template_method_node);

		Node replace_subclass_with_field_node = new Node(
				(new ReplaceSubclassWithField()).getRefactoringQuery());
		replace_subclass_with_field_node
				.addChild(replace_ctor_with_factory_method_node);
		graph.add(replace_subclass_with_field_node);

		// ////////////////////////////////
		// Below are local variable rules.
		//
		RefactoringQuery replace_temp_with_query = new RefactoringQuery(
				"replace_temp_with_query",
				(new ExtractMethod()).getRefactoringString()
						+ ", added_calls(?mFullName, ?newmFullName),"
						+ "deleted_localvar(?mFullName, ?type, ?, ?newmBody),"
						+ "added_return(?newmFullName, ?type)");
		replace_temp_with_query.addType("?mFullName");
		replace_temp_with_query.addType("?newmFullName");
		Node replace_temp_with_query_node = new Node(replace_temp_with_query);
		replace_temp_with_query_node.addChild(extract_method_node);
		graph.add(replace_temp_with_query_node);

		Node inline_temp_node = new Node(
				(new InlineTemp()).getRefactoringQuery());
		graph.add(inline_temp_node);

		Node introduce_explaining_variable_node = new Node(
				(new IntroduceExplainingVariable()).getRefactoringQuery());
		graph.add(introduce_explaining_variable_node);

		Node remove_control_flag_node = new Node(
				(new RemoveControlFlag()).getRefactoringQuery());
		graph.add(remove_control_flag_node);
		//
		// ////////////////////////////////
	}

	public void inferRefactoring(File fileName, Node curr) {
		if (curr.isVisited())
			return;

		curr.setVisited(true);
		for (Node child : curr.getChildren()) {
			inferRefactoring(fileName, child);
		}

		try {
			Connection con = new Connection(frontend);
			RefactoringQuery refactoring = curr.getRefQry();
			System.out.println("REF BEING CHECKED: " + refactoring.getName());
			PreparedQuery query = con.prepareQuery(refactoring.getQuery());
			ResultSet rs = query.executeQuery();

			while (rs.next()) {
				Rule currentRule = ruleFactory.returnRuleByName(refactoring
						.getName());
				if (currentRule != null) {
					String result = currentRule.checkAdherence(rs);
					storeResult(fileName, curr, rs, result);
				} else if (refactoring.getName().equals(
						"self_encapsulate_field")) {
					String fieldName = rs.getString("?fFullName");
					String checkForStragglers = "before_accesses(\""
							+ fieldName + "\", ?mFullName), after_accesses(\""
							+ fieldName + "\", ?mFullName)";
					PreparedQuery query2 = con.prepareQuery(checkForStragglers);
					ResultSet rs2 = query2.executeQuery();
					if (rs2.next())
						continue;

					checkForStragglers = "added_accesses(\""
							+ fieldName
							+ "\", ?mFullName), NOT(added_getter(?mFullName, \""
							+ fieldName + "\");added_setter(?mFullName, \""
							+ fieldName + "\"))";
					query2 = con.prepareQuery(checkForStragglers);
					rs2 = query2.executeQuery();
					if (rs2.next())
						continue;

					String writeTo = buildResult(refactoring, rs);
					storeResult(fileName, curr, rs, writeTo);
				} else {
					String writeTo = buildResult(refactoring, rs);
					storeResult(fileName, curr, rs, writeTo);
				}
			}
		} catch (IOException e) {
			System.out.println("IO Exception: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("Got exception: " + e.getMessage());
		}
	}

	private String buildResult(RefactoringQuery rq, ResultSet rs)
			throws TyrubaException {
		String result = rq.getName() + "(";
		Iterator<String> types_itr = rq.getTypes().iterator();
		int types_size = rq.getTypes().size();
		int types_count = 0;
		while (types_itr.hasNext()) {
			types_count++;
			String type = types_itr.next();
			String type_binding = rs.getString(type);
			result = result + "\"" + type_binding + "\"";
			if (types_count < types_size)
				result = result + ",";
		}
		return result + ")";
	}

	private void storeResult(File fileName, Node curr, ResultSet rs,
			String result) throws IOException, ParseException, TypeModeError {
		if (result != null && !written_strs.contains(result)) {
			written_strs.add(result);
			curr.incrementNumFound();
			String pred = "\n" + result + ".";
			List<String> vars1 = substituteInQueryString(curr.getRefQry()
					.getQuery(), rs);
			dependents.put(result, vars1);
			
			FileWriter fileWriter = new FileWriter(fileName, true);
			fileWriter.write(pred);
			fileWriter.flush();
			fileWriter.close();

			frontend.parse(pred);
		}
	}

	void ensureFrontEnd() throws java.io.IOException,
			tyRuBa.parser.ParseException, TypeModeError {
		if (frontend == null) {
			if (dbDir == null)
				frontend = new FrontEnd(loadInitFile, MetaInfo.fdbDir,
						false, null, true, backgroundPageCleaning);
			else
				frontend = new FrontEnd(loadInitFile, dbDir, true, null, false,
						backgroundPageCleaning);
		}
		frontend.setCacheSize(this.cachesize);
	}

	public void sort(String outputFile) {
		try {
			frontend = null;
			ensureFrontEnd();
			frontend.load(MetaInfo.lsclipseRefactorPred);
			frontend.load(MetaInfo.lsclipse2KB);
			frontend.load(MetaInfo.lsclipseDelta);
			// Delete the output file
			File f = new File(outputFile);
			if (f.exists()) {
				// delete if exists
				f.delete();
			}
			for (Node query : graph) {
				inferRefactoring(f, query);
			}
			frontend.shutdown();
			//frontend.finalize();
			frontend = null;
			written_strs = null;
		} catch (Exception e) {
			System.out.println("Something REALLY BAD has happened!");
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void printTree() {
		for (Node query : graph)
			printTree(query, 0);
	}

	public void printTree(Node curr, int indentCount) {
		for (int i = 0; i < indentCount; i++) {
			System.out.print(" ");
		}
		System.out.println(curr.getRefQry().getName());

		if (curr.getChildren() != null) {
			for (Node child : curr.getChildren()) {
				if (child != null)
					printTree(child, indentCount + 1);
			}
		}
	}

	public String findIntInString(String str) {
		Pattern intsOnly = Pattern.compile("\\d+");
		Matcher makeMatch = intsOnly.matcher(str);// "dadsad14 19.8dssaf jfdkasl;fj");
		if (makeMatch.find()) {
			String inputInt = makeMatch.group();
			System.out.println(inputInt);
			return inputInt;
		}
		return "";
	}

	public String findDecimalInString(String str) {
		Pattern intsOnly = Pattern.compile("[\\d]+\\.[\\d]+");
		Matcher makeMatch = intsOnly.matcher(str);// "dadsad14 19.8dssaf jfdkasl;fj");
		if (makeMatch.find()) {
			String inputInt = makeMatch.group();
			System.out.println(inputInt);
			return inputInt;
		}
		return "";
	}

	private static List<String> substituteInQueryString(String queryString,
			ResultSet rs) {
		List<String> res = new ArrayList<String>();
		// break up this query string
		List<String> queries = new ArrayList<String>();
		// preprocess by removing all spaces
		queryString = queryString.replace(" ", "");
		int current = 0;
		// Find start of first name, store nested parens
		while (queryString.charAt(current) == '(') {
			queries.add("(");
			++current;
		}
		while (current < queryString.length()) {
			if (queryString.startsWith("NOT(", current)) {
				queries.add("NOT");
				queries.add("(");
				current += 4;
				continue;
			}
			int start = current;
			// Find end of predicate
			while (queryString.charAt(current) != ')') {
				// Ignore stuff in quotation marks
				if (queryString.charAt(current) == '\"') {
					++current;
					while (queryString.charAt(current) != '\"') {
						++current;
					}
				}
				++current;
			}
			++current;
			queries.add(queryString.substring(start, current));
			// Find start of next name, adding parens, ;'s and ,'s
			while (current < queryString.length()
					&& !Character.isLetter(queryString.charAt(current))) {
				queries.add(String.valueOf(queryString.charAt(current)));
				++current;
			}
		}

		// substitue each elemt in queries
		for (String q : queries) {
			// This is a paren or punctuation
			if (q.length() < 4) {
				if (q.equals(","))
					res.add("AND");
				else if (q.equals(";"))
					res.add("OR");
				else
					res.add(q); // paren or NOT
				continue;
			}
			String newq = new String(q);
			// find each variable by looking for '?'
			int begin = 0;
			int end = 0;
			boolean addNewQ = true;
			while (true) {
				char endchar = ',';
				begin = q.indexOf('?', end);
				if (begin < 0)
					break;
				end = q.indexOf(endchar, begin);
				if (end < 0) {
					endchar = ')';
					end = q.indexOf(endchar, begin);
				}
				if (end < 0)
					break;
				String var = q.substring(begin, end);
				if (var.equals("?")) {
					// TODO(kprete): replace with "dont care"?
					// this doesnt work:
					// newq.replaceFirst("\\?", "X");
					continue;
				}
				try {
					String type_binding = rs.getString(var);
					newq = newq.replace(var, "\"" + type_binding + "\"");
				} catch (TyrubaException te) {
					addNewQ = false;
					break;
					// Should not get here because if we don't find the
					// string...
				} catch (NullPointerException npe) {
					addNewQ = false;
					break;
					// a null pointer exception is throw by
					// ResultSet.wrongType()
					// which tries to get the class of the null ptr.
				}
			}
			if (addNewQ)
				res.add(newq);
			else
				res.add(q);
		}
		return res;
	}
}

package refdiff.core.cst;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * A node in the Code Structure Tree (CST).
 * It represents a code element in the target programming language. For example, 
 * in Java we represent classes, interfaces, enums, and methods as CST nodes.
 */
public class CstNode implements HasChildrenNodes {
	private final int id;
	private String type;
	private Location location;
	private String simpleName;
	private String localName;
	private String namespace;
	private Optional<CstNode> parent = Optional.empty();
	private List<CstNode> nodes = new ArrayList<>();
	private Set<Stereotype> stereotypes = new HashSet<>();
	private List<Parameter> parameters = new ArrayList<>();
	
	public CstNode(int id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return String.format("%s %s %s", location.toString(), type, localName);
	}
	
	/**
	 * @return An unique id of the node in the CST.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return The type of the code element in the target programming language.
	 */
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * @return The location of the CST node in the source code.
	 */
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	/**
	 * @return The declared name and signature of the code element that uniquely identifies it whithin its parent node.
	 * For example, the local name of {@code m1} may be encoded as {@code "m1(int,String)"} in the Java example below.
	 * 
	 * <pre>
	 * class A {
	 *   void m1(int n, String message) {
	 *     // body
	 *   }
	 * }
	 * </pre>
	 */
	public String getLocalName() {
		return localName;
	}
	
	public void setLocalName(String logicalName) {
		this.localName = logicalName;
	}
	
	/**
	 * 
	 * @return The declared name of the code element.
	 * For example, the simple name of {@code m1} is {@code "m1"} in the Java example below.
	 * 
	 * <pre>
	 * class A {
	 *   void m1(int n, String message) {
	 *     // body
	 *   }
	 * }
	 * </pre>
	 */
	public String getSimpleName() {
		return simpleName;
	}
	
	public void setSimpleName(String simpleName) {
		this.simpleName = simpleName;
	}

	/**
	 * @return The children nodes of this node.
	 */
	public List<CstNode> getNodes() {
		return nodes;
	}
	
	@Override
	public void addNode(CstNode node) {
		nodes.add(node);
		node.setParent(this);
	}
	
	public void setNodes(List<CstNode> nodes) {
		this.nodes = nodes;
	}
	
	public Set<Stereotype> getStereotypes() {
		return stereotypes;
	}
	
	public void setStereotypes(Set<Stereotype> stereotypes) {
		this.stereotypes = stereotypes;
	}
	
	public void addStereotypes(Stereotype stereotype) {
		this.stereotypes.add(stereotype);
	}
	
	public boolean hasStereotype(Stereotype stereotype) {
		return this.stereotypes.contains(stereotype);
	}
	
	public void setParent(CstNode node) {
		this.parent = Optional.ofNullable(node);
	}

	/**
	 * @return The parent node of this node. Top-level declarations dos not have a parent node.
	 */
	@JsonIgnore
	public Optional<CstNode> getParent() {
		return parent;
	}

	/**
	 * @return The top-level ancestor node of this node.
	 */
	@JsonIgnore
	public Optional<CstNode> getRootParent() {
		if (!parent.isPresent()) {
			return Optional.empty();
		} else {
			CstNode parentNode = parent.get();
			if (parentNode.getParent().isPresent()) {
				return parentNode.getRootParent();
			} else {
				return parent;
			}
		}
	}
	
	/**
	 * @return The prefix that should be appended to {#code getLocalName} to uniquely identify this node.
	 * Nodes that have a parent should have a null namespace, i.e., only top-level declarations should have a namespace.
	 * 
	 * <p>In the example below, class {@code A} should have {@code "foo."} as namespace and "A" as local name.
	 * 
	 * <pre>
	 * package foo;
	 * 
	 * public class A {
	 *   void m1(int n, String message) {
	 *     // body
	 *   }
	 * }
	 * </pre>
	 */
	@JsonInclude(Include.NON_NULL)
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}
	
}

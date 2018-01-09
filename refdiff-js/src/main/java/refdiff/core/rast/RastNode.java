package refdiff.core.rast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RastNode implements HasChildrenNodes {
	private final int id;
	private String type;
	private Location location;
	private String simpleName;
	private String localName;
	private String namespace;
	private Optional<RastNode> parent = Optional.empty();
	private List<RastNode> nodes = new ArrayList<>();
	private Set<Stereotype> stereotypes = new HashSet<>();
	
	public RastNode(int id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return String.format("%s %s %s", location.toString(), type, localName);
	}
	
	public int getId() {
		return id;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public String getLocalName() {
		return localName;
	}
	
	public void setLocalName(String logicalName) {
		this.localName = logicalName;
	}
	
	public String getSimpleName() {
		return simpleName;
	}
	
	public void setSimpleName(String simpleName) {
		this.simpleName = simpleName;
	}
	
	public List<RastNode> getNodes() {
		return nodes;
	}
	
	@Override
	public void addNode(RastNode node) {
		nodes.add(node);
		node.setParent(this);
	}
	
	public void setNodes(List<RastNode> nodes) {
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
	
	public void setParent(RastNode node) {
		this.parent = Optional.ofNullable(node);
	}

	public Optional<RastNode> getParent() {
		return parent;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
}

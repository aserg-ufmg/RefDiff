package lsclipse;

import java.util.ArrayList;
import java.util.List;

public class Node {
	private List<Node> children;
	private boolean visited;
	private RefactoringQuery refQry;
	private int numFound_;
	
	public Node(){
		this.children = new ArrayList<Node>(); 
		this.visited = false;
		this.refQry = null;
		numFound_ = 0;
	}
	
	public Node(RefactoringQuery refQry){
		this.children = new ArrayList<Node>();
		this.visited = false;
		this.refQry = refQry;
		numFound_ = 0;
	}

	public Node(List<Node> children, boolean visited, RefactoringQuery refQry) {
		super();
		this.children = children;
		this.visited = visited;
		this.refQry = refQry;
	}
	
	public void incrementNumFound() {
		++numFound_;
	}
	
	public int numFound() {
		return numFound_;
	}
	
	@Override
	public String toString() {
		return refQry.getName() + "("+ numFound_ +")";
	}
	
	public List<Node> getChildren() {
		return children;
	}
	
	public void addChild(Node child) {
		children.add(child);
	}

	public void setChildren(List<Node> children) {
		this.children = children;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public RefactoringQuery getRefQry() {
		return refQry;
	}

	public void setRefQry(RefactoringQuery refQry) {
		this.refQry = refQry;
	}
}

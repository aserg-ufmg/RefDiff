package lsclipse.views;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.jdt.core.IJavaElement;

public class Node
{
	private String nodeName;
	private Vector<Node> children;
	private Node parent;
	private String message;
	private String fileName;
	private String projectName;
	private String baseProjectName;
	private String newProjectName;
	private String basePath;
	private String newPath;
	private String basePackageName;
	private String newPackageName;
	private String refactoring;
	private boolean isParent;
	private List<String> dependents_;

	public String params;
	public Map<String,IJavaElement> oldFacts = new HashMap<String,IJavaElement>();
	public Map<String,IJavaElement> newFacts = new HashMap<String,IJavaElement>();

	public Node(String name, Node p)
	{
		children = new Vector<Node>();
		nodeName = name;
		isParent = false;
		basePackageName = "";
		newPackageName = "";
		if(p != null)
			p.addChild(this);
	}
	
	public void setDependents(List<String> dependents_) {
		this.dependents_ = dependents_;
	}

	public List<String> getDependents() {
		return dependents_;
	}

	public void setParentStatus(boolean item)
	{
		isParent = true;
	}
	
	public boolean isParent()
	{
		return isParent;
	}
	
	public void setProjectName(String item)
	{ projectName = item; }
	
	public String getProjectName()
	{ return projectName; }
	
	public void setBasePackageName(String item)
	{ basePackageName = item; }
	
	public String getBasePackageName()
	{ return basePackageName; }
	
	public void setRefactoring(String item)
	{ refactoring = item; }
	
	public String getRefactoring()
	{ return refactoring; }
	
	public void setNewPackageName(String item)
	{ newPackageName = item; }
	
	public String getNewPackageName()
	{ return newPackageName; }
	
	public void setBaseProjectName(String item)
	{ baseProjectName = item; }
	
	public String getBaseProjectName()
	{ return baseProjectName; }
	
	public void setNewProjectName(String item)
	{ newProjectName = item; }
	
	public String getNewProjectName()
	{ return newProjectName; }
	
	public void setBasePath(String basePath1)
	{
		basePath = basePath1;
	}
	
	public String getBasePath()
	{
		return basePath;
	}
	
	public void setNewPath(String newPath1)
	{
		newPath = newPath1;
	}
	
	public String getNewPath()
	{
		return newPath;
	}
	
	public void setFile(String item)
	{ fileName = item; }
	
	public String getFile()
	{ return fileName; }
	
	public String getName()
	{ return nodeName; }
	
	public void setMessage(String s)
	{ message = s; }
	
	public String getMessage()
	{ return message; }
	
	public String toString()
	{ return getName(); }
	
	public void setParent(Node p)
	{ parent = p; }
	
	public Node getParent()
	{ return parent; }
	
	public void addChild(Node child)
	{
		children.add(child);
		if(child.getParent() == null)
		{
			child.setParent(this);
		}
	}
	
	public Node getChild(int index)
	{ return (Node)children.get(index); }
	
	public Vector<Node> getChildren()
	{ return children; }
	
	public boolean hasChildren()
	{ return !children.isEmpty(); }	
}
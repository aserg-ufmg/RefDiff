package lsclipse;
import java.util.*;
public class RefactoringQuery {
	private String name;
	private String query;
	private ArrayList<String> types;
	
	public RefactoringQuery(String name, String query) {
		super();
		this.name = name;
		this.query = query;
		this.types = new ArrayList<String>();
	}
	public ArrayList<String> getTypes() {
		return types;
	}
	public void setTypes(ArrayList<String> types) {
		this.types = types;
	}
	
	public void addType(String type){
		types.add(type);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
}

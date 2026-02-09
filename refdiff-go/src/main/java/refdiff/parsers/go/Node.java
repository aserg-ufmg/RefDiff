package refdiff.parsers.go;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.gson.annotations.SerializedName;
import refdiff.core.cst.TokenPosition;

//@JsonIgnoreProperties(ignoreUnknown = true)
public class Node {
	private int id;
	private int start;
	private int end;
	private int line;
	private String name;
	private String type;
	private String parent;
	private String receiver;
	private String namespace;

	@SerializedName("has_body")
	private boolean hasBody;

	@SerializedName("tokens")
	ArrayList<String> tokens = new ArrayList<>();

	@SerializedName("parameter_names")
	ArrayList<String> parametersNames = new ArrayList<>();

	@SerializedName("parameter_types")
	ArrayList<String> parameterTypes = new ArrayList<>();

	@SerializedName("function_calls")
	ArrayList<String> functionCalls = new ArrayList<>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getLine() {
		return line;
	}

	public String getReceiver() {
		return receiver;
	}

	@JsonSetter("receiver")
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	@JsonSetter("line")
	public void setLine(int line) {
		this.line = line;
	}

	public String getParent() {
		return parent;
	}

	@JsonSetter("parent")
	public void setParent(String parent) {
		this.parent = parent;
	}

	public boolean hasBody() {
		return hasBody;
	}

	@JsonSetter("has_body")
	public void setHasBody(boolean hasBody) {
		this.hasBody = hasBody;
	}

	public ArrayList<String> getTokens() {
		return tokens;
	}

	@JsonSetter("tokens")
	public void setTokens(ArrayList<String> tokens) {
		this.tokens = tokens;
	}

	public ArrayList<String> getParametersNames() {
		return parametersNames;
	}

	@JsonSetter("parameter_names")
	public void setParametersNames(ArrayList<String> parametersNames) {
		this.parametersNames = parametersNames;
	}

	public ArrayList<String> getParameterTypes() {
		return parameterTypes;
	}

	@JsonSetter("parameter_types")
	public void setParameterTypes(ArrayList<String> parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public ArrayList<String> getFunctionCalls() {
		return functionCalls;
	}

	@JsonSetter("function_calls")
	public void setFunctionCalls(ArrayList<String> functionCalls) {
		this.functionCalls = functionCalls;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getNamespace() {
		return namespace;
	}

	@JsonSetter("start")
	public void setStart(int start) {
		this.start = start;
	}

	@JsonSetter("end")
	public void setEnd(int end) {
		this.end = end;
	}

	@JsonSetter("name")
	public void setName(String name) {
		this.name = name;
	}

	@JsonSetter("type")
	public void setType(String type) {
		this.type = type;
	}

	@JsonSetter("namespace")
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public ArrayList<TokenPosition> getTokenPositions(String content) {
		ArrayList<TokenPosition> positions = new ArrayList<>();
		if (this.tokens == null) {
			return positions;
		}

//		Map<Integer, Integer> linePositionOffset = new HashMap<>();
//		linePositionOffset.put(0, 0);
//		int lineNumber = 1;
//		String[] lines = content.split(System.lineSeparator());
//		for (String line: lines) {
//			linePositionOffset.put(lineNumber, linePositionOffset.get(lineNumber-1) + line.length() + System.lineSeparator().length());
//			lineNumber++;
//		}


		for(String token: this.tokens) {
			String[] parts = token.split("-");

			int start = Integer.parseInt(parts[0]);
			int end = Integer.parseInt(parts[1]);

//			int line = Integer.parseInt(parts[0]);
//			int column = Integer.parseInt(parts[1]);
//			int size = Integer.parseInt(parts[2]);
//
//			if (line >= lines.length){
//				System.out.println("eita!");
//			}
//
//			int startPosition = linePositionOffset.get(line) + column;
//			int endPosition = startPosition + size;

			positions.add(new TokenPosition(start, end));
		}

		return positions;
	}

	public String getAddress() {
		return this.namespace+this.name;
	}
}
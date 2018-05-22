package refdiff.parsers.c;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTGenericVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTArrayDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTArrayModifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTArraySubscriptExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTEqualsInitializer;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTReturnStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTUnaryExpression;

import refdiff.core.rast.HasChildrenNodes;
import refdiff.core.rast.Location;
import refdiff.core.rast.Parameter;
import refdiff.core.rast.RastNode;
import refdiff.core.rast.RastNodeRelationship;
import refdiff.core.rast.RastNodeRelationshipType;
import refdiff.core.rast.RastRoot;

public class CRastVisitor extends ASTGenericVisitor {

	private int id = 1;
	private RastNode currentNode;
	private RastNode currentRelationshipN1;
	private Map<Integer, RastNode> nodesHash = new HashMap<Integer, RastNode>();
	private RastRoot rastRoot;
	private RastNode programNode;
	private String waitingName;
	private String waitingBodyLocation;
	private String waitingType;
	private String maybeWaitingArray;
	private String fileName;

	public CRastVisitor(RastRoot rastRoot, String fileName) {
		super(true);
		this.rastRoot = rastRoot;
		this.fileName = fileName;
	}
	
	@Override
	protected int genericVisit(IASTNode iastNode) {
		if (!this.shouldSkip(iastNode)) {
			if (iastNode instanceof CASTFunctionCallExpression) {
				this.currentRelationshipN1 = (RastNode) this.getRASTParent(iastNode);
				this.waitingName = "Relationship";
			}
			else if (iastNode instanceof CASTParameterDeclaration) {
				this.waitingName = "Parameter";
				this.waitingType = "Parameter";
			}
			else if (iastNode instanceof CASTCompoundStatement) {
				if (this.waitingBodyLocation.equals("FunctionDeclaration")) {
					int offset = ((CASTCompoundStatement) iastNode).getOffset();
					int length = ((CASTCompoundStatement) iastNode).getLength();
					
					Location location = this.currentNode.getLocation();
					location.setBodyBegin(offset);
					location.setBodyEnd(offset + length);
					
					this.waitingBodyLocation = null;
				}
			}
			else if (iastNode instanceof CASTSimpleDeclSpecifier) {
				if (this.waitingType != null && this.waitingType.equals("Parameter")) {
					String type = this.getType((CASTSimpleDeclSpecifier) iastNode);
					
					String localName = this.currentNode.getLocalName();
					
					int closingParenthesisIndex = localName.indexOf(")");
					
					if (localName.charAt(closingParenthesisIndex - 1) != '(') {
						type = ", " + type;
					}
					
					StringBuilder newLocalNameBuilder = new StringBuilder();
					newLocalNameBuilder.append(localName.substring(0, closingParenthesisIndex));
					newLocalNameBuilder.append(type);
					newLocalNameBuilder.append(")");
					
					this.currentNode.setLocalName(newLocalNameBuilder.toString());
					
					this.waitingType = null;
					this.maybeWaitingArray = null;
				}
			}
			else if (iastNode instanceof CASTArrayModifier) {
				if (this.maybeWaitingArray != null && this.maybeWaitingArray.equals("Parameter")) {
					RastNode parentNode = (RastNode) this.getRASTParent(iastNode);
					
					List<Parameter> parameters = parentNode.getParameters();
					Parameter lastParameter = parameters.get(parameters.size() - 1);
					
					lastParameter.setName(lastParameter.getName() + "[]");
				}
				
				this.maybeWaitingArray = null;
			}
			else if (iastNode instanceof IASTName) {
				if (this.waitingName != null) {
					String name = ((IASTName) iastNode).toString();
					
					if (this.waitingName.equals("FunctionDeclaration")) {
						this.currentNode.setSimpleName(name);
						this.currentNode.setLocalName(name + "()");	
					}
					else if (this.waitingName.equals("Parameter")) {
						Parameter parameter = new Parameter();
						parameter.setName(name);
						
						RastNode parentNode = (RastNode) this.getRASTParent(iastNode);
						parentNode.getParameters().add(parameter);
						
						this.maybeWaitingArray = "Parameter";
					}
					else if (this.waitingName.equals("Relationship")) {
						RastNode n2 = this.getBySimpleName(name);
						if (n2 != null) {
							if (!this.hasRelationship(
									this.rastRoot, this.currentRelationshipN1, n2, RastNodeRelationshipType.USE)) {
								RastNodeRelationship relationship = new RastNodeRelationship(
										RastNodeRelationshipType.USE, 
										this.currentRelationshipN1.getId(), 
										n2.getId());
								this.rastRoot.getRelationships().add(relationship);								
							}
						}
						
						this.currentRelationshipN1 = null;
					}
					
					this.waitingName = null;
				}
			}
			else {
				if (iastNode instanceof CASTFunctionDefinition) {
					this.waitingName = "FunctionDeclaration";
					this.waitingBodyLocation = "FunctionDeclaration";
				}
				
				RastNode node = this.createNode((ASTNode) iastNode);

				HasChildrenNodes parentNode = this.getRASTParent(iastNode);
				
				parentNode.getNodes().add(node);

				this.nodesHash.put(iastNode.hashCode(), node);
				
				this.currentNode = node;
				
				if (iastNode instanceof CASTTranslationUnit) {
					this.programNode = node;
				}
			}				
		}

		return PROCESS_CONTINUE;
	}
	
	private RastNode createNode(ASTNode astNode) {
		RastNode rastNode = new RastNode(this.id);
		
		if (astNode instanceof CASTTranslationUnit) {
			rastNode.setSimpleName(this.fileName);
			rastNode.setLocalName(this.fileName);	
		}
		
		rastNode.setType(this.getRASTType(astNode));
		
		int offset = astNode.getOffset();
		int length = astNode.getLength();

		Location location = new Location();
		location.setBegin(offset);
		location.setEnd(offset + length);
		location.setFile(this.fileName);
		
		if (astNode instanceof CASTTranslationUnit) {
			location.setBodyBegin(offset);
			location.setBodyEnd(offset + length);
		}
		
		rastNode.setLocation(location);
		
		this.id++;
		
		return rastNode;
	}
	
	private String getRASTType(ASTNode astNode) {
		if (astNode instanceof CASTTranslationUnit) {
			return "Program";
		}
		else if (astNode instanceof CASTFunctionDefinition) {
			return "FunctionDeclaration";
		}
		else if (astNode instanceof CASTParameterDeclaration) {
			return "Parameter";
		}
		
		return astNode.getClass().getName();
	}
	
	private boolean shouldSkip(IASTNode iastNode) {
		return iastNode instanceof CASTFunctionDeclarator
				|| iastNode instanceof CASTExpressionStatement
				|| iastNode instanceof CASTIdExpression 
				|| iastNode instanceof CASTLiteralExpression
				|| iastNode instanceof CASTDeclarator
				|| iastNode instanceof CASTReturnStatement
				|| iastNode instanceof CASTDeclarationStatement
				|| iastNode instanceof CASTSimpleDeclaration
				|| iastNode instanceof CASTUnaryExpression
				|| iastNode instanceof CASTBinaryExpression
				|| iastNode instanceof CASTArrayDeclarator 
				|| iastNode instanceof CASTArraySubscriptExpression
				|| iastNode instanceof CASTEqualsInitializer;
	}
	
	private HasChildrenNodes getRASTParent(IASTNode iastNode) {
		if (iastNode instanceof CASTTranslationUnit) {
			return this.rastRoot;
		}
		
		RastNode rastParent = this.nodesHash.get(iastNode.getParent().hashCode());
		
		if (rastParent == null) {
			rastParent = this.currentNode;
		}
		
		return rastParent;
	}
	
	private RastNode getBySimpleName(String simpleName) {
		return this.searchBySimpleName(this.programNode, simpleName);
	}
	
	private String getType(IASTSimpleDeclSpecifier node) {
		int type = node.getType();
		
		if (type == IASTSimpleDeclSpecifier.t_auto) { 
			return "auto";
		}
		else if (type == IASTSimpleDeclSpecifier.t_bool) {
			return "Bool";
		}
		else if (type == IASTSimpleDeclSpecifier.t_char) {
			return "char";
		}
		else if (type == IASTSimpleDeclSpecifier.t_char16_t) {
			return "char16_t";
		}
		else if (type == IASTSimpleDeclSpecifier.t_char32_t) {
			return "char32_t";
		}
		else if (type == IASTSimpleDeclSpecifier.t_decltype) {
			return "decltype";
		}
		else if (type == IASTSimpleDeclSpecifier.t_double) {
			return "double";
		}
		else if (type == IASTSimpleDeclSpecifier.t_float) {
			return "float";
		}
		else if (type == IASTSimpleDeclSpecifier.t_int) {
			return "int";
		}
		else if (type == IASTSimpleDeclSpecifier.t_typeof) {
			return "typeof";
		}
		else if (type == IASTSimpleDeclSpecifier.t_void) {
			return "void";
		}
		else if (type == IASTSimpleDeclSpecifier.t_wchar_t) {
			return "wchar_t";
		}
		
		return null;
	}
	
	private boolean hasRelationship(RastRoot root, RastNode n1, RastNode n2, RastNodeRelationshipType type) {
		for (RastNodeRelationship relationship : root.getRelationships()) {
			if (relationship.getType().equals(type)
					&& ((relationship.getN1() == n1.getId() && relationship.getN2() == n2.getId()) 
							|| (relationship.getN2() == n1.getId() && relationship.getN1() == n2.getId()))) {
				return true;
			}
		}
		
		return false;
	}
	
	private RastNode searchBySimpleName(RastNode node, String simpleName) {
		if (node.getSimpleName() != null && node.getSimpleName().equals(simpleName)) {
			return node;
		}

		for (RastNode child : node.getNodes()) {
			RastNode found = this.searchBySimpleName(child, simpleName);
			if (found != null) {
				return found;
			}
		}

		return null;
	}
	
}

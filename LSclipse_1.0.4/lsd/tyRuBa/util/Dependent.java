package tyRuBa.util;

/**
 * For the dependency tracking mechanism of RuleBases. RuleBase
 * will notify its dependents by calling reset.
 * 
 * @author kdvolder
 */
public interface Dependent {
	void update();
}

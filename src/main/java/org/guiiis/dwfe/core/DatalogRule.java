package org.guiiis.dwfe.core;

import java.util.Map;
import java.util.Set;

import fr.lirmm.graphik.graal.api.core.Constant;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.Variable;
import fr.lirmm.graphik.util.string.AppendableToStringBuilder;

/**
 * This interface represents a datalog rule whose expression is
 * ∀X,Y(B[X,Y] -> H[X]).
 * 
 * @author Peng Xiao {sharpen70@gmail.com}
 */
public interface DatalogRule extends Comparable<DatalogRule>, AppendableToStringBuilder {
	/**
	 * Get the label (the name) of this rule.
	 * 
	 * @return the label of this rule.
	 */
	String getLabel();

	/**
	 * Set the label (the name) of this rule.
	 * 
	 * @param label
	 */
	void setLabel(String label);

	/**
	 * Get the body (the hypothesis) of this rule.
	 * 
	 * @return the body of this rule.
	 */
	InMemoryAtomSet getBody();

	/**
	 * Get the head (the conclusion) of this rule.
	 * 
	 * @return the head of this rule.
	 */
	InMemoryAtomSet getHead();

//	/**
//	 * Compute and return the set of frontier variables of this rule.
//	 * 
//	 * @return a Set containing the frontier variables of this rule.
//	 */
//	Set<Variable> getFrontier();

	/**
	 * Get all variables of this rule.
	 * 
	 * @return a Set of all variables related to this Rule.
	 */
	Set<Variable> getVariables();
	
	/**
	 * Get all constants of this rule.
	 * 
	 * @return a Set of all constants related to this Rule.
	 */
	Set<Constant> getConstants();
	
	/**
	 * Get the relative map of the variables in this rule
	 * 
	 * @return a map records the relative coding of variables
	 */
	Map<Variable, Integer> getVarMap();
	
	/**
	 *  Get all free variables of this rule.
	 *  
	 *  @return a Set of all variables related to this Rule.
	 */
	Set<Variable> getFree();
	
	/**
	 * Output the rule in RDFox syntax
	 */
	String toRDFox();
}

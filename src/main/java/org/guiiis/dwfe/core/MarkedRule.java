package org.guiiis.dwfe.core;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.guiiis.dwfe.utils.Utils;

import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.Variable;
import fr.lirmm.graphik.graal.core.DefaultRule;
import fr.lirmm.graphik.graal.rulesetanalyser.util.PredicatePosition;
import fr.lirmm.graphik.util.stream.IteratorException;

public class MarkedRule extends DefaultRule {
	private boolean isMarked = false;
	private Map<PredicatePosition, Boolean> markmap;
	private Set<PredicatePosition> markset;
	private Boolean isShy = null;
	
	public MarkedRule(Rule r) {
		super(r);
		
		this.markmap = new TreeMap<>();
		this.markset = new TreeSet<>();
	}
	
	/**
	 * @param position the position used the marked the rule body
	 * @return  0  the rule is firstly marked
	 *          1  the rule is marked with the new position
	 *         -1  the rule is not marked with any position
	 */
	public int mark(PredicatePosition position) {
		if(!this.markset.contains(position)) {
			this.markset.add(position);
			if(isMarked) return 1;
			else {
				isMarked = true;
				return 0;
			}
		}
		return -1;
	}
	
	public boolean isShy() throws IteratorException {
		Set<Variable> markvariable = new HashSet<>();
		
		for(PredicatePosition pp : markset) {
			markvariable.addAll(Utils.getVarByPosition(this.getBody(), pp));
		}
		
		for(Variable v : markvariable) {
			
		}
		
		return true;
	}
	
	public void clearMark() {
		this.isMarked = false;
		this.markmap.clear();
		this.markset.clear();
	}
}

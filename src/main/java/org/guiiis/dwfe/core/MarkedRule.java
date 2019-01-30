package org.guiiis.dwfe.core;

import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.core.DefaultRule;
import fr.lirmm.graphik.graal.rulesetanalyser.util.PredicatePosition;

public class MarkedRule extends DefaultRule {
	
	
	public MarkedRule(Rule r) {
		super(r);
	}
	
	/**
	 * @param position the position used the marked the rule body
	 * @return  0  the rule is firstly marked
	 *          1  the rule is marked with new position
	 *         -1  the rule is not marked with any position
	 */
	public int mark(PredicatePosition position) {
		return 0;
	}
	
	public boolean isShy() {
		
		return true;
	}
	
	public void clearMark() {
		
	}
}

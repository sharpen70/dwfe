package org.guiiis.dwfe.checker;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.guiiis.dwfe.core.MarkedRule;
import org.guiiis.dwfe.utils.Utils;

import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.core.Variable;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByBodyPredicatesRuleSet;
import fr.lirmm.graphik.graal.core.ruleset.LinkedListRuleSet;
import fr.lirmm.graphik.graal.rulesetanalyser.util.PredicatePosition;
import fr.lirmm.graphik.util.stream.IteratorException;

public class ComplibilityChecker {
	private RuleSet markedruleset;
	private IndexedByBodyPredicatesRuleSet indexedmarkedruleset;
	private List<MarkedRule> affectedRule = null;
	
	public ComplibilityChecker(RuleSet rs) {
		this.markedruleset = new LinkedListRuleSet();
		for(Rule r : rs) this.markedruleset.add(new MarkedRule(r));
		this.indexedmarkedruleset = new IndexedByBodyPredicatesRuleSet(this.markedruleset);
		
		affectedRule = new ArrayList<>();
	}
	
	public void checkAndNote() {
		
	}
	
	private boolean check(Rule r) throws IteratorException, AtomSetException {	
		Set<Variable> e = r.getExistentials();
		Queue<PredicatePosition> queue = new LinkedList<>();
		
		for(Variable v : e) {
			clearMark();
			queue.clear();
			List<PredicatePosition> pos = Utils.getPositionByVar(r.getHead(), v);
			queue.addAll(pos);
			
			while(!queue.isEmpty()) {
				PredicatePosition pp = queue.poll();

				Iterable<Rule> mks = this.indexedmarkedruleset.getRulesByBodyPredicate(pp.predicate);
				for(Rule mk : mks) {
					MarkedRule _mk = (MarkedRule)mk;
					int flag = _mk.mark(pp);
					if(flag >= 0) queue.addAll(Utils.getAffectedPosition(_mk, pp));
					if(flag == 0) affectedRule.add(_mk);
				}
			}
			
			for(MarkedRule _r : affectedRule) {
				if(!_r.isShy()) return false;
			}
		}
		
		return true;
	}
	
	private void clearMark() {
		for(MarkedRule r : affectedRule) {
			r.clearMark();
		}
		affectedRule.clear();
	}
}

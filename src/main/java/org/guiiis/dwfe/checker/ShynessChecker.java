package org.guiiis.dwfe.checker;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import fr.lirmm.graphik.graal.api.core.GraphOfRuleDependencies;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.core.Variable;

public class ShynessChecker {
	private GraphOfRuleDependencies graph;
	private RuleSet markedRule;
	private boolean isMarked = false;
	
	public ShynessChecker(GraphOfRuleDependencies g) {
		this.graph = g;
	}
	
	public boolean check(Rule r) {
		
		return false;
	}
	
	public void checkAndNote() {
		
	}
	
	private void mark(Rule r) {
		Set<Variable> e = r.getExistentials();
		
		for(Variable v : e) {
			Set<Rule> marked = new HashSet<>();
			Queue<Pair<Rule, Substitution>> queue = new LinkedList<>();
			queue.addAll(this.graph.getTriggeredRulesWithUnifiers(r));
			while(!queue.isEmpty()) {
				Pair<Rule, Substitution> cr = queue.poll();
				
			}
		}
	}
	
}

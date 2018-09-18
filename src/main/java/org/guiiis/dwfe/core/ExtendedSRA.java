package org.guiiis.dwfe.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RulesCompilation;
import fr.lirmm.graphik.graal.backward_chaining.pure.AbstractRewritingOperator;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByHeadPredicatesRuleSet;
import fr.lirmm.graphik.graal.core.unifier.QueryUnifier;

public class ExtendedSRA extends AbstractRewritingOperator {
	private Map<ConjunctiveQuery, QueryUnifier> info;
	
	@Override
	public Collection<ConjunctiveQuery> getRewritesFrom(ConjunctiveQuery q, IndexedByHeadPredicatesRuleSet ruleSet, RulesCompilation compilation) {
		LinkedList<ConjunctiveQuery> rewriteSet = new LinkedList<ConjunctiveQuery>();
		Collection<QueryUnifier> unifiers = new LinkedList<QueryUnifier>();
		for (Rule r : getUnifiableRules(q.getAtomSet().predicatesIterator(),
				ruleSet, compilation)) {
			unifiers.addAll(getSRUnifier(q, r, compilation));
		}
		
		/** info is initialized every time rewriting is called **/
		info = new HashMap<>();
		
		/** compute the rewrite from the unifier **/
		ConjunctiveQuery a;
		for (QueryUnifier u : unifiers) {
			a = Utils.rewrite(q, u);
			if(a != null) {
				info.put(a, u);
				rewriteSet.add(a);
			}
		}

		return rewriteSet;
	}
	 
	public QueryUnifier getUnificationInfo(ConjunctiveQuery q) {
		return info.get(q);
	}
}

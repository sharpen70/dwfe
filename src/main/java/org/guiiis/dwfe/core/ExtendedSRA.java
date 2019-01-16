package org.guiiis.dwfe.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.guiiis.dwfe.core.graal.Utils;

import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RulesCompilation;
import fr.lirmm.graphik.graal.backward_chaining.pure.AbstractRewritingOperator;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByHeadPredicatesRuleSet;
import fr.lirmm.graphik.graal.core.unifier.QueryUnifier;

public class ExtendedSRA extends AbstractRewritingOperator {
	private Map<ConjunctiveQuery, ExtendedQueryUnifier> info;
	
	@Override
	public Collection<ConjunctiveQuery> getRewritesFrom(ConjunctiveQuery q, IndexedByHeadPredicatesRuleSet ruleSet, RulesCompilation compilation) {
		LinkedList<ConjunctiveQuery> rewriteSet = new LinkedList<ConjunctiveQuery>();
		Collection<ExtendedQueryUnifier> unifiers = new LinkedList<ExtendedQueryUnifier>();
		for (Rule r : getUnifiableRules(q.getAtomSet().predicatesIterator(),
				ruleSet, compilation)) {
			for (ExtendedQueryUnifier u : getESRUnifier(q, r, compilation)) {
				unifiers.add(u);
			}
		}
		
		/** info is initialized every time rewriting is called **/
		info = new HashMap<>();
		
		/** compute the rewrite from the unifier **/
		ConjunctiveQuery a;
		
		for (ExtendedQueryUnifier u : unifiers) {
			a = Utils.rewrite(q, u.getUnifier());
			if(a != null) {
				info.put(a, u);
				rewriteSet.add(a);
			}
		}

		return rewriteSet;
	}
	 
	public ExtendedQueryUnifier getUnificationInfo(ConjunctiveQuery q) {
		return info.get(q);
	}
	
	protected List<ExtendedQueryUnifier> getESRUnifier(ConjunctiveQuery q, Rule r, RulesCompilation compilation) {
		/** compute the simple unifiers **/
		List<QueryUnifier> unifiers =  getSinglePieceUnifiers(q, r, compilation);
		
		//if(unifiers.isEmpty()) System.out.println("empty");
		
		LinkedList<ExtendedQueryUnifier> unifAggregated = new LinkedList<ExtendedQueryUnifier>();
		
		/** compute the aggregated unifier by rule **/
		if (!unifiers.isEmpty()) {
			LinkedList<QueryUnifier> restOfUnifToAggregate = new LinkedList<QueryUnifier>(unifiers);
			Iterator<QueryUnifier> itr = unifiers.iterator();
			QueryUnifier u;
			while (itr.hasNext()) {
				u = itr.next();
				restOfUnifToAggregate.remove(itr);
				
				for(ExtendedQueryUnifier _u : aggregate(u, restOfUnifToAggregate, 1)) {
					_u.setLabel(r.getLabel());
					unifAggregated.add(_u);
				}
			}
		}
		
		return unifAggregated;		
	}
	
	/**
	 * Returns the list of all the aggregated unifiers that can be build from u
	 * and others unifiers of l. recursive function
	 * 
	 * @param u the unifier whose we want aggregate with the unifiers of l
	 * @param l list of unifiers
	 * @param aggNum the current number of aggregated unifiers
	 * @return the list of all aggregated unifier build from u and unifiers of
	 *         l
	 */
	@SuppressWarnings("unchecked")
	private LinkedList<ExtendedQueryUnifier> aggregate(QueryUnifier u,
			LinkedList<QueryUnifier> l, int aggNum) {
		LinkedList<QueryUnifier> lu = (LinkedList<QueryUnifier>) l.clone();
		// if there is no more unifier to aggregate
		if (lu.isEmpty()) {
			LinkedList<ExtendedQueryUnifier> res = new LinkedList<ExtendedQueryUnifier>();
			res.add(new ExtendedQueryUnifier(u, aggNum));
			return res;
		} else { 
			QueryUnifier first = lu.getFirst(); // take the first one
			lu.removeFirst(); // remove first one from lu
			// if first can be aggregated with u
			LinkedList<ExtendedQueryUnifier> res = aggregate(u, lu, aggNum);
			if (u.isCompatible(first)) {
				// System.out.println("oui");
				// compute the others aggregation from the aggregation of u and
				// first and the rest of lu
				res.addAll(aggregate(u.aggregate(first), lu, aggNum + 1));
				// concatenate this result and the others aggregations from u
				// and the rest of lu

			}
			return res;
		}
	}
}

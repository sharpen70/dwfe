package org.guiiis.dwfe.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.RulesCompilation;
import fr.lirmm.graphik.graal.core.compilation.NoCompilation;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByHeadPredicatesRuleSet;
import fr.lirmm.graphik.graal.core.unifier.QueryUnifier;
import fr.lirmm.graphik.util.profiler.Profilable;
import fr.lirmm.graphik.util.profiler.Profiler;

public class DatalogRewritingAlgorithm implements Profilable{
	private boolean verbose = true;
	private Profiler profiler;
	
	private ExtendedSRA op;
	private DatalogRewritingOperator dp;
	
	private Map<ConjunctiveQuery, RuleRewPair> rtd;
	private RewTree rewtree;
	
	public DatalogRewritingAlgorithm(DatalogRewritingOperator dp, ExtendedSRA op) {
		this.dp = dp;
		this.op = op;
	
		this.rtd = new HashMap<>();
		this.rewtree = new RewTree();
	}
	
	public Set<DatalogRule> exec(ConjunctiveQuery query, IndexedByHeadPredicatesRuleSet ruleSet) {
		RulesCompilation compilation = NoCompilation.instance();
		
		if(this.verbose) {
			this.profiler.trace(query.toString());
			this.profiler.put("CONFIG", op.getClass().getSimpleName());
		}
		LinkedList<ConjunctiveQuery> finalRewritingSet = new LinkedList<ConjunctiveQuery>();
		Queue<ConjunctiveQuery> rewriteSetToExplore = new LinkedList<ConjunctiveQuery>();
		Collection<ConjunctiveQuery> currentRewriteSet;
		
		Set<DatalogRule> finalDatalog = new HashSet<DatalogRule>();
		
		int exploredRewrites = 0;
		int generatedRewrites = 0;

		if(this.verbose) {
			this.profiler.clear("Rewriting time");
			this.profiler.start("Rewriting time");
		}
	
		// remove some basic redundancy
		PureQuery pquery = new PureQuery(compilation.getIrredondant(query.getAtomSet()), query.getAnswerVariables());

		pquery.addAnswerPredicate();
		rewriteSetToExplore.add(pquery);
		finalRewritingSet.add(pquery);

		ConjunctiveQuery q;
		
		while (!Thread.currentThread().isInterrupted() && !rewriteSetToExplore.isEmpty()) {

			/* take the first query to rewrite */
			q = rewriteSetToExplore.poll();
			++exploredRewrites; // stats

			/* compute all the rewrite from it */
			currentRewriteSet = this.op.getRewritesFrom(q, ruleSet, compilation);
			generatedRewrites += currentRewriteSet.size(); // stats

			/* keep only the most general among query just computed */
			Utils.computeCover(currentRewriteSet, compilation);

			/*
			 * keep only the query just computed that are more general than
			 * query already compute
			 */
			selectMostGeneralFromRelativeTo(currentRewriteSet,
					finalRewritingSet, compilation);

			
			// keep to explore only most general query
			selectMostGeneralFromRelativeTo(rewriteSetToExplore,
					currentRewriteSet, compilation);
			
			// build rewriting tree according to the current rewriting
			this.rewtree.add(q, currentRewriteSet);
			
			for(ConjunctiveQuery _q: currentRewriteSet) {
				QueryUnifier u = op.getUnificationInfo(_q);
				DatalogRule r = findRep(u.getPiece(), _q, null);
				RuleRewPair p = this.dp.getRewriteFrom(r, op.getUnificationInfo(_q));
				rtd.put(_q, p);
				finalDatalog.addAll(p.getRules());
			}
			
			
			// add to explore the query just computed that we keep
			rewriteSetToExplore.addAll(currentRewriteSet);
			
			/*
			 * keep in final rewrite set only query more general than query just
			 * computed
			 */
			selectMostGeneralFromRelativeTo(finalRewritingSet,
					currentRewriteSet, compilation);
			
			// add in final rewrite set the query just compute that we keep
			finalRewritingSet.addAll(currentRewriteSet);

		}

		/* clean the rewrites to return */
		Utils.computeCover(finalRewritingSet);

		if(this.verbose) {
			this.profiler.stop("Rewriting time");
			this.profiler.put("Generated rewritings", generatedRewrites);
			this.profiler.put("Explored rewritings", exploredRewrites);
			this.profiler.put("Pivotal rewritings", finalRewritingSet.size());
		}

		return finalDatalog;
	}
	
	/**
	 * @return The representative of the current rewriting
	 */
	public DatalogRule findRep(InMemoryAtomSet B, ConjunctiveQuery q, DatalogRule r) {
		RuleRewPair rp = rtd.get(q);
		
		if(r != null) {
			rp.replace(r);
		}
		else {
			DatalogRule c = rp.contains(B);
			if(c != null) return c;
		}
		
		DatalogRule uc = rp.unfold();
		
		if(B.isSubSetOf(uc.getBody())) return uc;
		else {
			return findRep(B, rewtree.getParent(q), uc);
		}
	}
	
	/**
	 * (From fr.lirmm.graphik.graal.backward_chaining.pure.RewritingAlgorithm)
	 * Remove from toSelect the Fact that are not more general than all the fact
	 * of relativeTo
	 * 
	 * @param toSelect
	 * @param rewritingSet
	 */
	public void selectMostGeneralFromRelativeTo(
			Collection<ConjunctiveQuery> toSelect,
			Collection<ConjunctiveQuery> rewritingSet, RulesCompilation compilation) {
		Iterator<? extends ConjunctiveQuery> i = toSelect.iterator();
		while (i.hasNext()) {
			InMemoryAtomSet f = i.next().getAtomSet();
			if (containMoreGeneral(f, rewritingSet, compilation))
				i.remove();
		}
	}
	
	/**
	 * (From fr.lirmm.graphik.graal.backward_chaining.pure.RewritingAlgorithm)
	 * Returns true if rewriteSet contains a fact more general than f, else
	 * returns false
	 * 
	 * @param f
	 * @param rewriteSet
	 * @param compilation
	 * @return true if rewriteSet contains a fact more general than f, false otherwise.
	 */
	public boolean containMoreGeneral(InMemoryAtomSet f,
			Collection<ConjunctiveQuery> rewriteSet, RulesCompilation compilation) {
		for(ConjunctiveQuery q : rewriteSet) {
			InMemoryAtomSet a = q.getAtomSet();
			if (Utils.isMoreGeneralThan(a, f, compilation))
				return true;
		}
		return false;
	}
	
	public Profiler getProfiler() {
		return this.profiler;
	}

	public void setProfiler(Profiler profiler) {
		this.profiler = profiler;
	}
}

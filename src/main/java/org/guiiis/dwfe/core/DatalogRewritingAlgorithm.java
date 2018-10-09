package org.guiiis.dwfe.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.RulesCompilation;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.core.atomset.AtomSetUtils;
import fr.lirmm.graphik.graal.core.compilation.NoCompilation;
import fr.lirmm.graphik.graal.core.factory.DefaultAtomFactory;
import fr.lirmm.graphik.graal.core.factory.DefaultAtomSetFactory;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByHeadPredicatesRuleSet;
import fr.lirmm.graphik.graal.core.unifier.QueryUnifier;
import fr.lirmm.graphik.util.profiler.Profilable;
import fr.lirmm.graphik.util.profiler.Profiler;

public class DatalogRewritingAlgorithm implements Profilable{
	private boolean verbose = true;
	private Profiler profiler;
	
	private ExtendedSRA op;
	private DatalogRewritingOperator dp;
	
	private Rtd rtd;
	private RewTree rewtree;
	
	private boolean test = false;
	
	public DatalogRewritingAlgorithm(DatalogRewritingOperator dp, ExtendedSRA op) {
		this.dp = dp;
		this.op = op;
	
		this.rtd = new Rtd();
		this.rewtree = new RewTree();
	}
	
	public Set<DatalogRule> exec(ConjunctiveQuery query, IndexedByHeadPredicatesRuleSet ruleSet) {
		RulesCompilation compilation = NoCompilation.instance();
		
		if(this.verbose) {
			this.profiler.trace(query.toString());
		//	this.profiler.put("CONFIG", op.getClass().getSimpleName());
		}
		LinkedList<ConjunctiveQuery> finalRewritingSet = new LinkedList<ConjunctiveQuery>();
		Queue<ConjunctiveQuery> rewriteSetToExplore = new LinkedList<ConjunctiveQuery>();
		Collection<ConjunctiveQuery> currentRewriteSet;
		
		Set<ConjunctiveQuery> rmSet = new HashSet<>();
		
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
		
		// Construct the initial DatalogRule
		List<Term> ansVar = query.getAnswerVariables();
		Predicate G = new Predicate("ANS", ansVar.size());
		Atom Ghead = DefaultAtomFactory.instance().create(G, ansVar);
		DefaultAtomSetFactory.instance().create(Ghead);
		DatalogRule H = new DefaultDatalogRule(query.getAtomSet(), DefaultAtomSetFactory.instance().create(Ghead));
		
		rtd.add(pquery, new RuleRewPair(H, null, true));
		
		finalDatalog.add(H);
		
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
				ExtendedQueryUnifier eu = op.getUnificationInfo(_q);
				QueryUnifier u = eu.getUnifier();
				
				if(test) {
					this.profiler.trace("rewrites: " + _q.toString());
					this.profiler.trace("Piece: " + u.getPiece().toString() + "\n");					
				}
				DatalogRule r = findRep(u, u.getQuery(), null);
				
				RuleRewPair p = this.dp.getRewriteFrom(r, eu);
				rtd.add(_q, p);
				finalDatalog.addAll(p.getRules());
			}
			
			if(test) this.profiler.trace("===");
			
			// add to explore the query just computed that we keep
			rewriteSetToExplore.addAll(currentRewriteSet);
			
			/*
			 * keep in final rewrite set only query more general than query just
			 * computed
			 */
			rmSet.addAll(selectMostGeneralFromRelativeTo(finalRewritingSet,
					currentRewriteSet, compilation));
			
			// add in final rewrite set the query just compute that we keep
			finalRewritingSet.addAll(currentRewriteSet);

		}

//		/* clean the rewrites to return */
//		Utils.computeCover(finalRewritingSet);
		
		/* clean the datalog rule */
		for(ConjunctiveQuery rq : rmSet) rtd.rm(rq);
		
		clean(finalDatalog);
		
		if(this.verbose) {
			this.profiler.stop("Rewriting time");
			this.profiler.put("Generated rewritings", generatedRewrites);
			this.profiler.put("Explored rewritings", exploredRewrites);
			this.profiler.put("Pivotal rewritings", finalRewritingSet.size());
			this.profiler.put("Datalog rewritings", finalDatalog.size());
		}
		
		return finalDatalog;
	}
	
	
	/**
	 * Clean the redundant datalog rules according to the 
	 * deletion in UCQ rewriting
	 */
	private void clean(Set<DatalogRule> dlg) {
		Iterator<DatalogRule> it = dlg.iterator();
		
		while(it.hasNext()) {
			DatalogRule r = it.next();
			if(!this.rtd.exists(r)) it.remove();
		}
	}
	
	/**
	 * @return The representative of the current rewriting
	 */
	public DatalogRule findRep(QueryUnifier u, ConjunctiveQuery q, DatalogRule r) {
		RuleRewPair rp = rtd.get(q);
		
		if(test) {
			this.profiler.trace("From " + q.toString() + "\n");
		//	this.profiler.trace("Previous: " + (r != null ? r.toString() : "null") + "\n");
			this.profiler.trace("Rtd: " + rp.toString() + "\n");
		}
			
		if(r != null) {
			rp.setTail(r);
		}
		
		DatalogRule tail = rp.getTail();
		DatalogRule unfolding = rp.getUnfold();
		InMemoryAtomSet b = u.getImageOf(u.getPiece());
		
		if(AtomSetUtils.contains(u.getImageOf(tail.getBody()), b)) return tail;
		else {
	//		System.out.println("unfold: " + unfolding.toString() + "\n");
	//		System.out.println("unfold body: " + u.getImageOf(unfolding.getBody()) + " piece: " + b + "\n");
			if(AtomSetUtils.contains(u.getImageOf(unfolding.getBody()), b)) return unfolding;
			else return findRep(u, rewtree.getParent(q), unfolding);
		}
	}
	
	/**
	 * (From fr.lirmm.graphik.graal.backward_chaining.pure.RewritingAlgorithm)
	 * Remove from toSelect the Fact that are not more general than all the fact
	 * of relativeTo
	 * 
	 * @param toSelect
	 * @param rewritingSet
	 * @return the set of rewritings are redundant
	 */
	public Set<ConjunctiveQuery> selectMostGeneralFromRelativeTo(
			Collection<ConjunctiveQuery> toSelect,
			Collection<ConjunctiveQuery> rewritingSet, RulesCompilation compilation) {
		Iterator<? extends ConjunctiveQuery> i = toSelect.iterator();
		Set<ConjunctiveQuery> rm = new HashSet<>();
		
		while (i.hasNext()) {
			ConjunctiveQuery q = i.next();
			InMemoryAtomSet f = q.getAtomSet();
			if (containMoreGeneral(f, rewritingSet, compilation)) {
				i.remove();
				rm.add(q);
			}
		}
		
		return rm;
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
	
	/**
	 * A recording for the mapping from rewritings
	 * to datalog rules
	 * 
	 * @author sharpen
	 */
	private class Rtd {
		private Map<ConjunctiveQuery, RuleRewPair> rtd;
		private Map<DatalogRule, Integer> count;
		
		public Rtd() {
			rtd = new HashMap<>();
			count = new HashMap<>();
		}
		
		public RuleRewPair get(ConjunctiveQuery q) {
			return this.rtd.get(q);
		}
		
		public void add(ConjunctiveQuery q, RuleRewPair rp) {
			Collection<DatalogRule> rs = rp.getRules();
			rtd.put(q, rp);
			for(DatalogRule r : rs) {
				try {
					count.merge(r, 1, (old, one) -> old + one);
				}
				catch(Exception e) {
					System.out.println("Merge Error " + r.toString());
				}
			}
		}
		
		public void rm(ConjunctiveQuery q) {
			RuleRewPair rp = rtd.get(q);
			if(rp == null) return;
			
			rtd.remove(q);
			
			Collection<DatalogRule> rs = rp.getRules();
			for(DatalogRule r : rs) {
				count.merge(r, 1, (old, one) -> old - one);
			}
		}
		
		public boolean exists(DatalogRule r) {
			return this.count.get(r) != 0;
		}
	}
}

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
		
		int i = 0;
		
		while (!Thread.currentThread().isInterrupted() && !rewriteSetToExplore.isEmpty()) {

			/* take the first query to rewrite */
			q = rewriteSetToExplore.poll();
			
			++exploredRewrites; // stats

			/* compute all the rewrite from it */
			currentRewriteSet = this.op.getRewritesFrom(q, ruleSet, compilation);
			generatedRewrites += currentRewriteSet.size(); // stats
			
			int s1 = currentRewriteSet.size();
			
			if(test) {
				System.out.println("\nIteration " + (i++) + "\n");
				System.out.println("Current: " + q + "\n");
	//			for(ConjunctiveQuery _q : currentRewriteSet) System.out.println(_q + "\n");
			}
			
			/* keep only the most general among query just computed */
			Utils.computeCover(currentRewriteSet, compilation);
			
			int s2 = currentRewriteSet.size();
			
			if(test) {
				System.out.println("\nCover remove Current rewriting set\n" + s1 + " " + s2);
			}
			/*
			 * keep only the query just computed that are more general than
			 * query already compute
			 */
			selectMostGeneralFromRelativeTo(currentRewriteSet,
					finalRewritingSet, compilation);
			
			int s3 = currentRewriteSet.size();
			
			if(test) {
				System.out.println("\nCover remove Current rewriting set from final \n" + s2  + " " + s3);
			}
			
			// keep to explore only most general query
			selectMostGeneralFromRelativeTo(rewriteSetToExplore,
					currentRewriteSet, compilation);
			
			// build rewriting tree according to the current rewriting
			this.rewtree.add(q, currentRewriteSet);
			
			for(ConjunctiveQuery _q: currentRewriteSet) {
				ExtendedQueryUnifier eu = op.getUnificationInfo(_q);
				QueryUnifier u = eu.getUnifier();
				
//				if(test) {
//					this.profiler.trace("rewrites: " + _q.toString());
//					this.profiler.trace("Piece: " + u.getPiece().toString() + "\n");					
//				}
				DatalogRule r = findRep(u, u.getQuery(), null);
				
				RuleRewPair p = this.dp.getRewriteFrom(r, eu);
				
				rtd.add(_q, p);
				
				finalDatalog.addAll(p.getRules());
				
				if(test) System.out.println("\n Query to add:\n" + p.getRules());
			}
			
			// add to explore the query just computed that we keep
			rewriteSetToExplore.addAll(currentRewriteSet);
			
			int fs = finalRewritingSet.size();
			/*
			 * keep in final rewrite set only query more general than query just
			 * computed
			 */
			Set<ConjunctiveQuery> toremove = selectMostGeneralFromRelativeTo(finalRewritingSet,
					currentRewriteSet, compilation);
			
			if(test) {
				for(ConjunctiveQuery _q : toremove) {
					System.out.println("\nQuery to be removed:\n" + _q + "\n\nRelated DatalogRule:\n" + rtd.get(_q));
				}
			}
			
//			for(ConjunctiveQuery _q : toremove) rtd.rm(_q);
			
			if(test) rtd.show();
			
			rmSet.addAll(toremove);
			
			if(test) {
				System.out.println("\nFinal rewriting size \n" + fs + " " + finalRewritingSet.size());
			}
			
			// add in final rewrite set the query just compute that we keep
			finalRewritingSet.addAll(currentRewriteSet);

		}

//		/* clean the rewrites to return */
//		Utils.computeCover(finalRewritingSet);
		
		/* clean the datalog rule */
		for(ConjunctiveQuery rq : rmSet) rtd.rm(rq);
		
		if(test) {
			rtd.show();
			System.out.println("\nFinal size before remove:" + finalDatalog.size() + "\n");
			this.profiler.trace("Rule cleaned:\n");
		}
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
			if(!this.rtd.exists(r)) {
				if(test) this.profiler.trace(r.toString() + "\n");
				it.remove();
			}
		}
	}
	
	/**
	 * @return The representative of the current rewriting
	 */
	public DatalogRule findRep(QueryUnifier u, ConjunctiveQuery q, DatalogRule r) {
		RuleRewPair rp = rtd.get(q);
		
//		if(test) {
//			this.profiler.trace("From " + q.toString() + "\n");
//		//	this.profiler.trace("Previous: " + (r != null ? r.toString() : "null") + "\n");
//			this.profiler.trace("Rtd: " + rp.toString() + "\n");
//		}
		
//		if(r != null) {
//			rp.setTail(r);
//		}
//		
//		DatalogRule tail = rp.getTail();
//		DatalogRule unfolding = rp.getUnfold();
		DatalogRule unfolding;
		
		if(r == null) unfolding = rp.getUnfold();
		else unfolding = rp.unfold(r);
		
		InMemoryAtomSet b = u.getImageOf(u.getPiece());
		
		if(AtomSetUtils.contains(u.getImageOf(rp.getTail().getBody()), b)) return rp.getTail();
		else {
//			System.out.println("Pair: " + rp);
//			System.out.println("unfold: " + unfolding.toString() + "\n");
//			System.out.println("unfold body: " + u.getImageOf(unfolding.getBody()) + " piece: " + b + "\n");
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
			rtd.put(q, rp);
			
			backtrack(q, 1);
		}
		
		public void rm(ConjunctiveQuery q) {
			backtrack(q, -1);
		}
		
		public boolean exists(DatalogRule r) {
			return this.count.get(r) > 0;
		}
		
		public void show() {
			System.out.println("\nRtd Count:\n");
			for(DatalogRule r: this.count.keySet()) {
				System.out.println(r + " : " + count.get(r));
			}
		}
		
		private void backtrack(ConjunctiveQuery q, int v) {
			while(q != null) {
				RuleRewPair rp = rtd.get(q);
				
				if(!rp.isOrigin()) changeCount(rp.getUp(), v);
				changeCount(rp.getTail(), v);
				q = rewtree.getParent(q);
			}
		}
		
		private void changeCount(DatalogRule r, int v) {
			int c = count.getOrDefault(r, 0);
			count.put(r, c + v);
		}
	}
}

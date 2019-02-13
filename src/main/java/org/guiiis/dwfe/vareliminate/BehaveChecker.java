package org.guiiis.dwfe.vareliminate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.guiiis.dwfe.core.MarkedRule;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.core.Variable;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByBodyPredicatesRuleSet;
import fr.lirmm.graphik.graal.core.ruleset.LinkedListRuleSet;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.IteratorException;

public class BehaveChecker {
	private RuleSet markedruleset;
	private IndexedByBodyPredicatesRuleSet indexedmarkedruleset;
	private List<MarkedRule> affectedRule = null;
	private Map<Rule, Boolean> checked = null;
	
	public BehaveChecker(RuleSet rs) throws IteratorException {
		this.markedruleset = new LinkedListRuleSet();
		for(Rule r : rs) this.markedruleset.add(new MarkedRule(r));
		this.indexedmarkedruleset = new IndexedByBodyPredicatesRuleSet(this.markedruleset);
		checked = new HashMap<>();
		
		affectedRule = new ArrayList<>();
	}
	
	private List<Pair<Predicate, Set<Integer>>> getHV(Rule r, Variable v) throws IteratorException {
		AtomSet head = r.getHead();
		
		List<Pair<Predicate, Set<Integer>>> re = new LinkedList<>();
		
		CloseableIterator<Atom> it = head.iterator();
		
		while(it.hasNext()) {
			Atom a = it.next();
			List<Term> ts = a.getTerms();
			Set<Integer> indice = new HashSet<>();
			
			for(int i = 0; i < ts.size(); i++) {
				if(ts.get(i).equals(v)) indice.add(i);
			}
			
			if(!indice.isEmpty()) re.add(Pair.of(a.getPredicate(), indice));
		}
		
		return re;
	}
	
	private boolean check(Rule r, Variable v) throws IteratorException, AtomSetException {	
		Queue<Pair<Predicate, Set<Integer>>> queue = new LinkedList<>();
		
		clearMark();
		queue.clear();
		queue.addAll(getHV(r, v));
		
		while(!queue.isEmpty()) {
			Pair<Predicate, Set<Integer>> pp = queue.poll();

			Iterable<Rule> mks = this.indexedmarkedruleset.getRulesByBodyPredicate(pp.getLeft());
			for(Rule mk : mks) {
				MarkedRule _mk = (MarkedRule)mk;
				
				if(!_mk.isMarked()) affectedRule.add(_mk);
				
				List<Pair<Predicate, Set<Integer>>> fhead = _mk.mark(pp.getLeft(), pp.getRight());
				if(_mk.isDominated()) return false;
				
				queue.addAll(fhead);
			}
		}
		
		for(MarkedRule _r : affectedRule) {
			if(!_r.isDominated()) return false;
		}
		
		return true;
	}
	
	public boolean isBehaveRule(Rule r) throws IteratorException, AtomSetException {
		if(checked.containsKey(r)) return checked.get(r);
		
		for(Variable v : r.getExistentials()) {
			if(!check(r, v)) {
				checked.put(r, false);
				return false;
			}
		}
		
		return true;
	}
	
	private void clearMark() throws IteratorException {
		for(MarkedRule r : affectedRule) {
			r.reset();
		}
		affectedRule.clear();
	}
}

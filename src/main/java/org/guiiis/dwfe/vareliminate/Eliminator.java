package org.guiiis.dwfe.vareliminate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.core.factory.DefaultAtomFactory;
import fr.lirmm.graphik.graal.core.factory.DefaultAtomSetFactory;
import fr.lirmm.graphik.graal.core.factory.DefaultRuleFactory;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByBodyPredicatesRuleSet;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;
import fr.lirmm.graphik.util.stream.IteratorException;

public class Eliminator {
	private RuleSet rs = null;
	private IndexedByBodyPredicatesRuleSet ruleset = null;
	private BehaveChecker checker = null;
	
	public Eliminator(RuleSet _rs) throws IteratorException {
		this.rs = _rs;
		this.ruleset = new IndexedByBodyPredicatesRuleSet(rs);
		this.checker = new BehaveChecker(_rs);
	}
	
	public void elim() throws IteratorException, AtomSetException {
		for(Rule r : this.ruleset) {
			if(this.checker.isBehaveRule(r)) elim(r);
		}
	}
	
	public void elim(Rule r) throws IteratorException {
		List<Term> terms = new LinkedList<>();	
		InMemoryAtomSet head = r.getHead();
		
		for(Term t : head.getTerms()) {
			if(!r.getExistentials().contains(t)) terms.add(t);
		}
		
		String id = "P_" + r.getLabel();
		Predicate p = new Predicate(id, terms.size());
		Atom newhead = DefaultAtomFactory.instance().create(p, terms);

		
		CloseableIterator<Atom> it = head.iterator();
		
		while(it.hasNext()) {
			Atom a = it.next();
			List<Integer> reserved = new LinkedList<>(); 
			List<Term> _terms = a.getTerms();
			
			for(int i = 0; i < _terms.size(); i++) {
				Term t = _terms.get(i);
				if(terms.contains(t)) reserved.add(i);
			}
			
			Iterable<Rule> next = ruleset.getRulesByBodyPredicate(a.getPredicate());
			for(Rule rule : next) {
				_elim(rule, p, a, reserved);
			}
		}
		
		head.removeAll(head);
		head.add(newhead);
	}
	
	private void _elim(Rule r, Predicate np, Atom origin_head, List<Integer> reserved) {
		Predicate p = origin_head.getPredicate();
		CloseableIteratorWithoutException<Atom> it = r.getBody().atomsByPredicate(p);
		InMemoryAtomSet body = r.getBody();
		InMemoryAtomSet newbody = DefaultAtomSetFactory.instance().create();
		Set<Term> rmd = new HashSet<>();
		boolean replaced = false;
		
		while(it.hasNext()) {
			Atom a = it.next();
			
			if(a.getPredicate().equals(p)) {				
				List<Term> terms = new LinkedList<>();
				
				List<Term> ori = a.getTerms();
				int c = 0;
				boolean replacable = true;
				
				for(int i = 0; i < ori.size(); i++) {
					Term t = ori.get(i);
					if(i == reserved.get(c)) {
						c++;
						if(t.isConstant() && !t.equals(origin_head.getTerm(i))) {
							replacable = false;
							break;
						}
						else terms.add(t);
					}
					else {
						if(t.isVariable()) rmd.add(t);
					}
				}
				
				if(!replacable) continue;
				
				replaced = true;
				Atom na = DefaultAtomFactory.instance().create(np, terms);
				newbody.add(na);
			}
			else {
				newbody.add(a);
			}
		}
		
		if(!replaced) return;
		
		InMemoryAtomSet newhead = DefaultAtomSetFactory.instance().create();
		
		CloseableIteratorWithoutException<Atom> nit = r.getHead().iterator();
		
		List<Pair<Atom, Pair<Predicate, List<Integer>>>> candidate = new ArrayList<>();
		
		while(nit.hasNext()) {
			Atom a = nit.next();
			List<Term> terms = a.getTerms();
			List<Term> newterms = new LinkedList<>();
			List<Integer> _reserved = new LinkedList<>();
			
			String aid = a.getPredicate().getIdentifier().toString();
			aid = aid.substring(aid.indexOf(":") + 1);
			
			String id = "DWFE:P_" + r.getLabel() + "_" + aid;
			boolean convert = false;
			
			for(int i = 0; i < terms.size(); i++) {
				Term t = terms.get(i);
				if(rmd.contains(t)) {
					id += "_" + i;
					convert = true;
				}
				else {
					_reserved.add(i);
					newterms.add(t);
				}
			}
			
			if(convert) {
				Predicate _np = new Predicate(id, newterms.size());
				Atom newatom = DefaultAtomFactory.instance().create(_np, newterms);
				newhead.add(newatom);
				candidate.add(Pair.of(a, Pair.of(_np, _reserved)));
			}
			else {
				newhead.add(a);
			}
		}
		
		Rule newrule = DefaultRuleFactory.instance().create(newbody, newhead);
		
		this.ruleset.add(newrule);
		
		for(Pair<Atom, Pair<Predicate, List<Integer>>> pair : candidate) {
			for(Rule _r : this.ruleset.getRulesByBodyPredicate(pair.getLeft().getPredicate()))
					_elim(_r, pair.getRight().getLeft(), pair.getLeft(), pair.getRight().getRight());
		}
	}
	
	public RuleSet getRuleSet() {
		return this.ruleset;
	}
}

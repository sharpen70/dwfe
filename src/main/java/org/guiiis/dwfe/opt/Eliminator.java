package org.guiiis.dwfe.opt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.core.Variable;
import fr.lirmm.graphik.graal.core.factory.DefaultAtomFactory;
import fr.lirmm.graphik.graal.core.factory.DefaultAtomSetFactory;
import fr.lirmm.graphik.graal.core.factory.DefaultRuleFactory;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByBodyPredicatesRuleSet;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;
import fr.lirmm.graphik.util.stream.IteratorException;

public class Eliminator {
	private IndexedByBodyPredicatesRuleSet ruleset = null;
	private BehaveChecker checker = null;
	private List<Rule> tmpruleset;
	private int label;
	private Set<String> gen;
	
	public Eliminator(RuleSet _rs) throws IteratorException {
		this.tmpruleset = new ArrayList<>();
		for(Rule r : _rs) this.tmpruleset.add(r);
		
		this.ruleset = new IndexedByBodyPredicatesRuleSet(_rs);
		this.checker = new BehaveChecker(_rs);
		
		label = this.tmpruleset.size();
		this.gen = new HashSet<>();
	}
	
	public void elim() throws IteratorException, AtomSetException {
		for(Rule r : this.tmpruleset) {
			if(!r.getExistentials().isEmpty() && this.checker.isBehaveRule(r)) elim(r);
		}
	}
	
	public void elim(Rule r) throws IteratorException {
		InMemoryAtomSet head = r.getHead();
		Set<Variable> existentials = r.getExistentials();
		
		
		CloseableIterator<Atom> it = head.iterator();
		InMemoryAtomSet newhead = DefaultAtomSetFactory.instance().create();
//		List<Atom> newhead = new LinkedList<>();
		List<Tuple4<Rule, Predicate, Predicate, List<Integer>>> next = new LinkedList<>();
		
		while(it.hasNext()) {
			Atom a = it.next();
			List<Integer> reserved = new ArrayList<>(); 
			List<Term> terms = a.getTerms();
			List<Term> newterms = new LinkedList<>();
			
			String id = a.getPredicate().getIdentifier().toString();
			
			for(int i = 0; i < terms.size(); i++) {
				Term t = terms.get(i);
				if(!existentials.contains(t)) {
					reserved.add(i);
					newterms.add(t);
				}
				else id += "_" + String.valueOf(i);
			}		

			Predicate p = new Predicate(id, newterms.size());
			newhead.add(DefaultAtomFactory.instance().create(p, newterms));
			
			gen.add(id);
			
			Iterable<Rule> n1 = ruleset.getRulesByBodyPredicate(a.getPredicate());
			
			for(Rule rule : n1) {
				Tuple4<Rule, Predicate, Predicate, List<Integer>> tuple = new Tuple4<>(rule, p, a.getPredicate(), reserved);
				next.add(tuple);		
			}
		}
		
		Rule newrule = DefaultRuleFactory.instance().create(r.getLabel(), r.getBody(), newhead);
		this.ruleset.remove(r);
		this.ruleset.add(newrule);
		
		for(Tuple4<Rule, Predicate, Predicate, List<Integer>> tuple : next) {
			_elim(tuple.a, tuple.b, tuple.c, tuple.d);
		}
	}
	
	private boolean join(AtomSet atomset, Atom a, Variable v) throws IteratorException {
		CloseableIterator<Atom> it = atomset.iterator();
		
		while(it.hasNext()) {
			Atom _a = it.next();
			
			if(!_a.equals(a)) {
				if(_a.getTerms().contains(v)) return true;
			}
		}
		
		return false;
	}
		
	private void _elim(Rule r, Predicate np, Predicate p, List<Integer> reserved) throws IteratorException {
		CloseableIteratorWithoutException<Atom> it = r.getBody().iterator();
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
					if(c < reserved.size() && i == reserved.get(c)) {
						c++;
						terms.add(t);
					}
					else {
						if(t.isVariable()) {
							if(join(r.getBody(), a, (Variable)t)) {
								replacable = false;
								break;
							}
							rmd.add(t);
						}
						else {
							replacable = false;
							break;
						}
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
		
		List<Tuple3<Predicate, Predicate, List<Integer>>> candidate = new ArrayList<>();
		
		while(nit.hasNext()) {
			Atom a = nit.next();
			List<Term> terms = a.getTerms();
			List<Term> newterms = new LinkedList<>();
			List<Integer> _reserved = new LinkedList<>();
			
			String id = a.getPredicate().getIdentifier().toString();
			boolean convert = false;
			
			for(int i = 0; i < terms.size(); i++) {
				Term t = terms.get(i);
				if(rmd.contains(t)) {
					id += "_" + String.valueOf(i);
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
				if(this.gen.add(id))
					candidate.add(new Tuple3<Predicate, Predicate, List<Integer>>(a.getPredicate(), _np, _reserved));
			}
			else {
				newhead.add(a);
			}
		}
		
		Rule newrule = DefaultRuleFactory.instance().create(newbody, newhead);
		newrule.setLabel(String.valueOf(++this.label));
		this.ruleset.add(newrule);
		
		for(Tuple3<Predicate, Predicate, List<Integer>> tuple : candidate) {
			List<Rule> avoid = new LinkedList<>();
			for(Rule _r : this.ruleset.getRulesByBodyPredicate(tuple.a))
				avoid.add(_r);
			
			for(Rule _r : avoid)
				_elim(_r, tuple.b, tuple.a, tuple.c);
		}
	}
	
	public RuleSet getRuleSet() {
		return this.ruleset;
	}
}

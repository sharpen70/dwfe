package org.guiiis.dwfe.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.tuple.Pair;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.core.DefaultRule;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.IteratorException;

public class MarkedRule extends DefaultRule {
	private boolean isMarked;
	
	private List<Map<Atom, Set<Integer>>> bodymap;
	
	private boolean dominated;   
	
	public MarkedRule(Rule r) throws IteratorException {
		super(r);
		
		reset();
	}
	
	public void reset() throws IteratorException {
		this.isMarked = false;
		this.dominated = false;
		
		bodymap = new ArrayList<>();
		bodymap.add(newbodymap());
	}
	
	/**
	 * @param position the position used the marked the rule body
	 * @return  true     no marking is changed in this rule
	 * 	        false    otherwise
	 * @throws IteratorException 
	 */
	public List<Pair<Predicate, Set<Integer>>> mark(Predicate pred, Set<Integer> indice) throws IteratorException {
		AtomSet body = this.getBody();
		
		int size = this.bodymap.size();
		int add = 0;
		List<Integer> changed = new ArrayList<>();
		
		List<Integer> newcopy = new ArrayList<>();
		boolean fixed = false;
		
		//mark body
		for(int i = 0; i < size; i++) {
			Map<Atom, Set<Integer>> m = this.bodymap.get(i);
						
			CloseableIterator<Atom> it = body.iterator();
			
			boolean same = true;
			
			while(it.hasNext()) {
				Atom a = it.next();
				
				if(a.getPredicate().equals(pred)) {
					Set<Integer> spp = m.get(a);
					
					if(spp.containsAll(indice)) continue;
					else same = false;
						
					if(indice.containsAll(spp)) {
						m.put(a, indice);
						changed.add(i);
					}
					else newcopy.add(i);
				}
			}
			
			if(same) fixed = true;
		}
		
		if(!fixed) {
			for(int i : newcopy) {
				Map<Atom, Set<Integer>> m = this.bodymap.get(i);
				Map<Atom, Set<Integer>> _m = newbodymap();
				for(Atom a : m.keySet()) {
					if(a.getPredicate().equals(pred)) {
						_m.put(a, indice);
					}
					else {
						_m.put(a, m.get(a));
					}
				}
				this.bodymap.add(_m);
				changed.add(size + (add++));
			}
		}
		
		List<Pair<Predicate, Set<Integer>>> fhead = new LinkedList<>();
		
		//mark head
		for(Integer i : changed) {
			Map<Atom, Set<Integer>> m = bodymap.get(i);
			AtomSet head = this.getHead();
			Set<Term> markedv = new HashSet<>();
			
			for(Entry<Atom, Set<Integer>> entry : m.entrySet()) {
				Set<Integer> idx = entry.getValue();
//				System.out.println(entry.getKey() + " " + idx);
				for(Integer j : idx) {
					Term t = entry.getKey().getTerm(j);
					if(t.isVariable()) markedv.add(t);
					else m.remove(entry.getKey());
				}
			}
			
//			System.out.println(this.getLabel() + " " + markedv);
			
			boolean through = true;
			
			CloseableIterator<Atom> it = body.iterator();
			Map<Term, Atom> va = new HashMap<>();
			
			while(it.hasNext()) {
				Atom a = it.next();
				List<Term> lt = a.getTerms();
				
				for(int ti = 0; ti < lt.size(); ti++) {
					Term t = lt.get(ti);
					if(markedv.contains(t)) {
						if(!m.get(a).contains(ti)) through = false;
						else {
							Atom ta = va.get(t);
							
//							System.out.println(this.getLabel() + " " +  t + " " + ta + " " + a);
							if(ta != null && !ta.equals(a)) this.dominated = true;
							else va.put(t, a);
						}
					}
				}
			}
			
			if(!through) continue;
			
			it = head.iterator();
			
			List<Pair<Predicate, Set<Integer>>> _fhead = new LinkedList<>();
			
			while(it.hasNext()) {
				Atom a = it.next();
				Set<Integer> hid = new HashSet<>();
				
				for(Term v : markedv) {
					List<Term> terms = a.getTerms();
					for(int ai = 0; ai < terms.size(); ai++) {
						if(terms.get(ai).equals(v)) hid.add(ai);
					}
				}
				_fhead.add(Pair.of(a.getPredicate(), hid));
			}
			
			fhead.addAll(_fhead);
		}
		
		return fhead;
	}
	
	private Map<Atom, Set<Integer>> newbodymap() throws IteratorException {
		CloseableIterator<Atom> it = this.getBody().iterator();
		
		Map<Atom, Set<Integer>> re = new HashMap<>();
		
		while(it.hasNext()) {
			Atom a = it.next();
			
			re.put(a, SetUtils.<Integer>emptySet());
		}
		
		return re;
	}
	
	public boolean isDominated() {
		return this.dominated;
	}
	
	public boolean isMarked() {
		return this.isMarked;
	}
	
	public void printMark() {
		System.out.print("[" + this.getLabel() + "] ");
		for(Map<Atom, Set<Integer>> m : this.bodymap) {
			for(Entry<Atom, Set<Integer>> entry : m.entrySet()) {
				System.out.print(entry.getKey() + ": " + entry.getValue() + "  ");
			}
			
			System.out.print(" | ");
		}
		System.out.println("");
	}
}

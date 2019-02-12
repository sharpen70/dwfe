package org.guiiis.dwfe.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.guiiis.dwfe.utils.Utils;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.core.Variable;
import fr.lirmm.graphik.graal.core.DefaultRule;
import fr.lirmm.graphik.graal.rulesetanalyser.util.PredicatePosition;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.IteratorException;

public class MarkedRule extends DefaultRule {
	private boolean isMarked = false;
	
	private List<Map<Atom, Set<Integer>>> bodymap;
	private ArrayList<Map<Atom, Set<Integer>>> headmap;
	private Set<PredicatePosition> markset;
	private Map<Variable, Boolean> shymap;
	
	public MarkedRule(Rule r) {
		super(r);
		
		bodymap = new ArrayList<>();
		Map<Atom, Set<Integer>> first = new HashMap<>();
		bodymap.add(first);
		
		headmap = new ArrayList<>();
	}
	
	/**
	 * @param position the position used the marked the rule body
	 * @return  true     no marking is changed in this rule
	 * 	        false    otherwise
	 * @throws IteratorException 
	 */
	public boolean mark(Predicate pred, Set<Integer> indice) throws IteratorException {
		AtomSet body = this.getBody();
		
		int size = this.bodymap.size();
		int add = 0;
		List<Integer> changed = new ArrayList<>();
		
		//mark body
		for(int i = 0; i < size; i++) {
			Map<Atom, Set<Integer>> m = this.bodymap.get(i);
			
			boolean newcopy = false;			
			CloseableIterator<Atom> it = body.iterator();
			
			while(it.hasNext()) {
				Atom a = it.next();
				
				if(a.getPredicate().equals(pred)) {
					Set<Integer> spp = m.getOrDefault(a, new TreeSet<>());
					
					if(spp.containsAll(indice)) continue;
					
					if(indice.containsAll(spp)) {
						m.put(a, indice);
						changed.add(i);
					}
					else newcopy = true;
				}
			}
			
			if(newcopy) {
				Map<Atom, Set<Integer>> _m = new HashMap<>();
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
		
		boolean fixed = true;
		
		//mark head
		for(Integer i : changed) {
			Map<Atom, Set<Integer>> m = bodymap.get(i);
			AtomSet head = this.getHead();
			Set<Variable> markedv = new HashSet<>();
			
			for(Atom a : m.keySet()) {
				Set<Integer> idx = m.get(a);
				for(Integer j : idx) {
					Term t = a.getTerm(j);
					if(t.isVariable()) markedv.add((Variable)t);
				}
			}
			
			if(fixed && !markedv.isEmpty()) fixed = false;
			
			CloseableIterator<Atom> it = head.iterator();
			
			Map<Atom, Set<Integer>> _m = new TreeMap<>();
			
			while(it.hasNext()) {
				Atom a = it.next();
				Set<Integer> hid = new HashSet<>();
				
				for(Variable v : markedv) {
					int vid = a.indexOf(v);
					if(vid != -1) hid.add(vid);
				}
				_m.put(a, hid);
			}
			
			headmap.set(i, _m);
		}
		
		return fixed;
	}
	
	public boolean isShy() throws IteratorException {
		Set<Variable> markvariable = new HashSet<>();
		
		for(PredicatePosition pp : markset) {
			markvariable.addAll(Utils.getVarByPosition(this.getBody(), pp));
		}
		
		for(Variable v : markvariable) {
			
		}
		
		return true;
	}
	
	public void clearMark() {
		this.isMarked = false;

	}
}

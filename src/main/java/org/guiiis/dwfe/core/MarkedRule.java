package org.guiiis.dwfe.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.guiiis.dwfe.utils.Utils;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.Variable;
import fr.lirmm.graphik.graal.core.DefaultRule;
import fr.lirmm.graphik.graal.rulesetanalyser.util.PredicatePosition;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.IteratorException;

public class MarkedRule extends DefaultRule {
	private boolean isMarked = false;
	
	private List<Map<Atom, Set<Integer>>> bodymap;
	private Set<PredicatePosition> markset;
	private Map<Variable, Boolean> shymap;
	
	public MarkedRule(Rule r) {
		super(r);
		
		bodymap = new ArrayList<>();
		Map<Atom, Set<Integer>> first = new HashMap<>();
		bodymap.add(first);
	}
	
	/**
	 * @param position the position used the marked the rule body
	 * @return  0  the rule is firstly marked
	 *          1  the rule is marked with the new position
	 *         -1  the rule is not marked with any position
	 * @throws IteratorException 
	 */
	public int mark(Predicate pred, Set<Integer> indice) throws IteratorException {
		AtomSet body = this.getBody();
		
		for(int i = 0; i < this.bodymap.size(); i++) {
			Map<Atom, Set<Integer>> m = this.bodymap.get(i);
			
			boolean newcopy = false;			
			CloseableIterator<Atom> it = body.iterator();
			
			while(it.hasNext()) {
				Atom a = it.next();

				if(a.getPredicate().equals(pred)) {
					Set<Integer> spp = m.getOrDefault(a, new TreeSet<>());
					if(indice.containsAll(spp)) {
						m.put(a, indice);
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
			}
		}


		return -1;
	}
	
	private void markhead() {
		
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

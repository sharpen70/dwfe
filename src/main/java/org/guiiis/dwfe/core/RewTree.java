package org.guiiis.dwfe.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;

/**
 * The tree constructed by rewritings obtained from
 * the generic algorithm.
 * 
 * @author Peng Xiao {sharpen70@gmail.com}
 */
public class RewTree {
	private Map<ConjunctiveQuery, Set<ConjunctiveQuery>> children;
	private Map<ConjunctiveQuery, ConjunctiveQuery> parent;
	
	public RewTree() {
		children = new HashMap<>();
		parent = new HashMap<>();
	}
	
	public void add(ConjunctiveQuery p, ConjunctiveQuery c) {
		Set<ConjunctiveQuery> _chlidren = children.get(p);
		
		if(_chlidren == null) {
			_chlidren = new HashSet<>();
			children.put(p, _chlidren);
		}
		
		_chlidren.add(c);
		
		parent.put(c, p);
	}
	
	public void add(ConjunctiveQuery p, Collection<ConjunctiveQuery> c) {
		Set<ConjunctiveQuery> _chlidren = children.get(p);
		
		if(_chlidren == null) {
			_chlidren = new HashSet<>();
			children.put(p, _chlidren);
		}
		
		for(ConjunctiveQuery q : c) {
			_chlidren.add(q);
			parent.put(q, p);
		}
	}
	
	public Set<ConjunctiveQuery> getChildren(ConjunctiveQuery q) {
		return children.get(q);
	}
	
	public ConjunctiveQuery getParent(ConjunctiveQuery q) {
		return parent.get(q);
	}
	
	
	/**
	 * Print the tree to check the correctness
	 */
	public void print() {
		
	}
	
}

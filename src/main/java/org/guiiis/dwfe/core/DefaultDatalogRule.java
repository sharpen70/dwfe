package org.guiiis.dwfe.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.Constant;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.core.Variable;
import fr.lirmm.graphik.graal.core.DefaultAtom;
import fr.lirmm.graphik.graal.core.factory.DefaultAtomSetFactory;
import fr.lirmm.graphik.graal.core.term.DefaultTermFactory;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;

/**
 * The class for datalog rule
 * Without loss of generality, all rule are assumed to
 * have one single head.
 * 
 * @author Peng Xiao {sharpen70@gmail.com}
 */
public class DefaultDatalogRule implements DatalogRule {
	final private static String defaultNullPrefix = "@";
	private static int NullStart = 0;
	
	private String label = "";
	private InMemoryAtomSet body;
	private InMemoryAtomSet head;
//	private Atom head;
	
	private Map<Variable, Integer> varMap = null;
	private Set<Variable> variables = null;
	private Set<Constant> constants = null;
//	private Set<Variable> frontier = null;
	private Set<Variable> free = null;
	
	/**
	 * By default, we assume all datalog rules have atomic head
	 */
	public DefaultDatalogRule(Rule r) {
		this.label = r.getLabel();
		this.body = r.getBody();
		this.head = r.getHead();
	}
	/**
	 * By default, we assume all datalog rules have atomic head
	 * 
	 * @param body
	 * @param head
	 */
	public DefaultDatalogRule(InMemoryAtomSet body, InMemoryAtomSet head) {
		this.body = body;
		this.head = head;
	}
	
	/**
	 * If required, create a safe datalog rule by replace free variables with fresh constants
	 * 
	 * @param body
	 * @param head
	 * @param safe require safe or not
	 */
	public DefaultDatalogRule(InMemoryAtomSet body, InMemoryAtomSet head, boolean safe) {
		this.body = body;
		this.head = head;
		
		if(safe) this.makeSafe();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(super.equals(obj)) return true;
		
		String t1 = "";
		String t2 = "";
		
		if(!(obj instanceof DatalogRule)) return false;
		
		DatalogRule r = (DatalogRule)obj;

		if(!checkAtomSet(this.head, r.getHead(), r.getVarMap(), t1, t2)) return false;
		if(!checkAtomSet(this.body, r.getBody(), r.getVarMap(), t1, t2)) return false;
		
		if(t1 != t2) return false;
		
		return true;
	}
	
	private boolean checkAtomSet(InMemoryAtomSet as1, InMemoryAtomSet as2, Map<Variable, Integer> m, String t1, String t2) {
		CloseableIteratorWithoutException<Atom> h1 = as1.iterator();
		CloseableIteratorWithoutException<Atom> h2 = as2.iterator();
		
		while(h1.hasNext() && h2.hasNext()) {
			Atom a1 = h1.next();
			Atom a2 = h2.next();
			
			if(a1.getPredicate()!= a2.getPredicate()) return false;
			
			for(Term t : a1.getTerms()) {
				if(t.isConstant()) t1.concat(t.getLabel());
				else t1.concat((this.getVarMap().get(t)).toString());
			}
			
			for(Term t : a2.getTerms()) {
				if(t.isConstant()) t2.concat(t.getLabel());
				else t2.concat((m.get(t)).toString());
			}
		}
		if(h1.hasNext() || h2.hasNext()) return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
	
		CloseableIteratorWithoutException<Atom> hit = head.iterator();
		CloseableIteratorWithoutException<Atom> bit = body.iterator();
		
		while(hit.hasNext()) {
			Atom a = hit.next();
			result = prime * result + a.getPredicate().hashCode();
		}
		while(bit.hasNext()) {
			Atom a = bit.next();
			result = prime * result + a.getPredicate().hashCode();
		}
		
		return result;
	}
	
	@Override
	public int compareTo(DatalogRule o) {
		return 0;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		this.appendTo(builder);
		return builder.toString();
	}

	@Override
	public void appendTo(StringBuilder builder) {
		if (!this.getLabel().isEmpty()) {
			builder.append('[');
			builder.append(this.getLabel());
			builder.append("] ");
		}
		builder.append(this.getBody().toString());
		builder.append(" -> ");
		builder.append(this.getHead());
	}

	@Override
	public String getLabel() {
		return this.label;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public InMemoryAtomSet getBody() {
		return this.body;
	}

	@Override
	public InMemoryAtomSet getHead() {
		return this.head;
	}
	
	@Override
	public Set<Variable> getFree() {
		if(this.free == null) setFree();
		return this.free;
	}	
	
	private void setFree() {
		this.free = new HashSet<>();
		this.free.addAll(this.head.getVariables());
		this.free.removeAll(this.body.getVariables());
	}
	
//	@Override
//	public Set<Variable> getFrontier() {
//		if(this.frontier == null) {
//			this.frontier = new HashSet<>();
//			this.frontier.addAll(this.head.getVariables());
//			this.frontier.removeAll(this.getFree());
//		}
//		return this.frontier;
//	}

	@Override
	public Set<Variable> getVariables() {
		if(this.variables == null) setVariables();
		return this.variables;
	}
	
	private void setVariables() {
		this.variables = new HashSet<>();
		this.variables.addAll(this.head.getVariables());
		this.variables.addAll(this.body.getVariables());
	}
	
	@Override
	public Set<Constant> getConstants() {
		if(this.constants == null) setConstants();
		return this.constants;
	}

	private void setConstants() {
		this.constants = new HashSet<>();
		this.constants.addAll(this.head.getConstants());
		this.constants.addAll(this.body.getConstants());
	}
	
	@Override
	public Map<Variable, Integer> getVarMap() {
		if(this.varMap == null) setVarMap();
		return this.varMap;
	}
	
	private void setVarMap() {
		int i = 0;
		Set<Variable> tt = new HashSet<>();
		tt.addAll(this.head.getVariables());
		tt.addAll(this.body.getVariables());
		
		this.varMap = new HashMap<>();
		
		for(Variable t: tt) {
			this.varMap.put(t, i);
			i++;
		}
	}
	
	private void makeSafe() {
		Atom ahead = this.head.iterator().next();
		
		if(!this.getFree().isEmpty()) {
		//	System.out.println("free exits");
			List<Term> newterms = new ArrayList<>();
			
			for(Term t : ahead.getTerms()) {
				if(this.free.contains(t)) {
					newterms.add(DefaultTermFactory.instance().createVariable(defaultNullPrefix + NullStart));
					NullStart++;
				}
				else {
					newterms.add(t);
				}
			}
			
			this.head = DefaultAtomSetFactory.instance().create(new DefaultAtom(ahead.getPredicate(), newterms)); 
		}
	}
}

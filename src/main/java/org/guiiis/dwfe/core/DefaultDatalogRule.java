package org.guiiis.dwfe.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
	
	private String label;
	private InMemoryAtomSet body;
	private InMemoryAtomSet head;
//	private Atom head;
	
	private Set<Term> terms = null;
	private Set<Variable> variables = null;
	private Set<Constant> constants = null;
	private Set<Variable> frontier = null;
	private Set<Variable> free = null;
	
	/**
	 * By default, we assume all datalog rules have atomic head
	 */
	public DefaultDatalogRule(Rule r) {
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
	
	@Override
	public int compareTo(DatalogRule o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void appendTo(StringBuilder sb) {
		// TODO Auto-generated method stub
		
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
		if(this.free == null) {
			this.free = new HashSet<>();
			this.free.addAll(this.head.getVariables());
			this.free.removeAll(this.getVariables());
		}
			
		return this.free;
	}	
	
	@Override
	public Set<Variable> getFrontier() {
		if(this.frontier == null) {
			this.frontier = new HashSet<>();
			this.frontier.addAll(this.head.getVariables());
			this.frontier.removeAll(this.getFree());
		}
		return this.frontier;
	}

	@Override
	public Set<Variable> getVariables() {
		if(this.variables == null) {
			this.variables = new HashSet<>();
			this.variables.addAll(this.head.getVariables());
			this.variables.addAll(this.body.getVariables());
		}
		return this.variables;
	}

	@Override
	public Set<Constant> getConstants() {
		if(this.constants == null) {
			this.constants = new HashSet<>();
			this.constants.addAll(this.head.getConstants());
			this.constants.addAll(this.body.getConstants());
		}
		return this.constants;
	}

	@Override
	public Set<Term> getTerms() {
		if(this.terms == null) {
			this.terms = new HashSet<>();
			this.terms.addAll(this.head.getTerms());
			this.terms.addAll(this.body.getTerms());
		}
		
		return this.terms;
	}

	@Override
	public void makeSafe() {
		Atom ahead = this.head.iterator().next();
		
		if(!this.getFree().isEmpty()) {
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
			
			InMemoryAtomSet _head = DefaultAtomSetFactory.instance().create(new DefaultAtom(ahead.getPredicate(), newterms)); 
			this.head = _head;
		}
	}
}

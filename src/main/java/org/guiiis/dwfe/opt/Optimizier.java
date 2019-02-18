package org.guiiis.dwfe.opt;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.guiiis.dwfe.core.graal.PureQuery;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.GraphOfRuleDependencies;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.core.factory.DefaultAtomFactory;
import fr.lirmm.graphik.graal.core.factory.DefaultAtomSetFactory;
import fr.lirmm.graphik.graal.core.factory.DefaultRuleFactory;
import fr.lirmm.graphik.graal.core.grd.DefaultGraphOfRuleDependencies;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByHeadPredicatesRuleSet;
import fr.lirmm.graphik.util.graph.scc.StronglyConnectedComponentsGraph;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;
import fr.lirmm.graphik.util.stream.IteratorException;

public class Optimizier {
	public final static String ANSPredicateIdentifier = "guiiis.dwfe#ANS";
	
	private RuleSet rs;
	private Eliminator elim;
	private PureQuery q;
	private Rule H;
	private GraphOfRuleDependencies grd;
	private IndexedByHeadPredicatesRuleSet unsolved;
	private IndexedByHeadPredicatesRuleSet solved;
	
	public Optimizier(PureQuery _q, RuleSet _rs) throws IteratorException {
		this.rs = _rs;
		this.q = _q;
		
		List<Term> ansVar = new LinkedList<>();
		Predicate G = new Predicate(ANSPredicateIdentifier, 0);
		Atom Ghead = DefaultAtomFactory.instance().create(G, ansVar);
		this.H = DefaultRuleFactory.instance().create(q.getAtomSet(), DefaultAtomSetFactory.instance().create(Ghead));
		this.rs.add(this.H);
		
		this.elim = new Eliminator(rs);
		
		this.unsolved = new IndexedByHeadPredicatesRuleSet();
		this.solved = new IndexedByHeadPredicatesRuleSet();
	}
	
	public IndexedByHeadPredicatesRuleSet getUnsolved() throws Exception {
		if(this.unsolved == null) sep();
		return this.unsolved;
	}
	
	public IndexedByHeadPredicatesRuleSet getSolved() throws Exception {
		if(this.solved == null) sep();
		return this.solved;
	}
	
	private void sep() throws Exception {	
		this.elim.elim();
		RuleSet ers = this.elim.getRuleSet();
		
		grd = new DefaultGraphOfRuleDependencies(ers);
		StronglyConnectedComponentsGraph<Rule> sccg = grd.getStronglyConnectedComponentsGraph();
		
		Set<Integer> roots = sccg.getSinks();
		List<Rule> tmp = new LinkedList<>();
		
		for(int i : roots) {
			dfs(i, sccg, tmp);
		}
	}
	
	private void dfs(int i, StronglyConnectedComponentsGraph<Rule> sccg, List<Rule> tmp) {
		Set<Rule> scc = sccg.getComponent(i);
		
		boolean exfree = true;
		
		for(Rule r : scc) {
			if(!r.getExistentials().isEmpty()) exfree = false;
		}
		
		tmp.addAll(scc);
		
		if(!exfree) {
			this.unsolved.addAll(tmp);
			tmp.clear();
		}
		
		Set<Integer> next = sccg.incomingEdgesOf(i);
		
		if(next.isEmpty()) {
			this.solved.addAll(tmp);
			tmp.clear();
		}
		else {
			for(int n : next) dfs(n, sccg, tmp);
		}
	}
}

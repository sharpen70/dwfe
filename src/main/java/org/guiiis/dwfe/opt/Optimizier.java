package org.guiiis.dwfe.opt;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import fr.lirmm.graphik.graal.api.core.GraphOfRuleDependencies;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByHeadPredicatesRuleSet;
import fr.lirmm.graphik.util.graph.scc.StronglyConnectedComponentsGraph;
import fr.lirmm.graphik.util.stream.IteratorException;

public class Optimizier {
	private final static int maxrulesize = 50000;
	
	private RuleSet rs;
	private Eliminator elim;
	private GraphOfRuleDependencies grd;
	private IndexedByHeadPredicatesRuleSet unsolved = null;
	private List<Rule> solved = null;
	
	private boolean[] visited = new boolean[maxrulesize];
	private boolean[] exfree = new boolean[maxrulesize];
	
	public Optimizier(RuleSet _rs) throws IteratorException {
		this.rs = _rs;
		
		this.elim = new Eliminator(rs);	
	}
	
	public IndexedByHeadPredicatesRuleSet getUnsolved() throws Exception {
		if(this.unsolved == null) {
			this.unsolved = new IndexedByHeadPredicatesRuleSet();
			this.solved = new LinkedList<>();
			sep();
		}
		return this.unsolved;
	}
	
	public List<Rule> getSolved() throws Exception {
		if(this.solved == null) {
			this.unsolved = new IndexedByHeadPredicatesRuleSet();
			this.solved = new LinkedList<>();
			sep();
		}
		return this.solved;
	}
	
	private void sep() throws Exception {	
		this.elim.elim();
		RuleSet ers = this.elim.getRuleSet();
		
		System.out.println("elim size:" + ers.size());
		
		grd = new SimpleGraphOfRuleDependencies(ers);
		StronglyConnectedComponentsGraph<Rule> sccg = grd.getStronglyConnectedComponentsGraph();
		
//		for(int i : sccg.vertexSet()) System.out.println(i + " : " + sccg.getComponent(i));
//		
//		for(int i : sccg.incomingEdgesOf(4)) System.out.println(sccg.getEdgeSource(i));
		
		Set<Integer> roots = sccg.getSinks();
		
		for(int i : roots) {
			dfs(i, sccg);
		}
	}
	
	private boolean dfs(int i, StronglyConnectedComponentsGraph<Rule> sccg) {
		if(this.visited[i]) return this.exfree[i];
		
		this.visited[i] = true;
		
		Set<Rule> scc = sccg.getComponent(i);
		
		boolean free = true;
		
		Set<Integer> next = sccg.incomingEdgesOf(i);	

		for(int n : next) free = dfs(sccg.getEdgeSource(n), sccg) && free;		

		for(Rule r : scc) {
			if(!r.getExistentials().isEmpty()) free = false;
		}
		
		this.exfree[i] = free;
		
		for(Rule r : scc) {
			if(free) this.solved.add(r);
			else this.unsolved.add(r);
		}
		
		return free;
	}
}

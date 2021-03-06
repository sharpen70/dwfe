package org.guiiis.dwfe.utils;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.core.ruleset.LinkedListRuleSet;
import fr.lirmm.graphik.graal.rulesetanalyser.RuleSetPropertyHierarchy;
import fr.lirmm.graphik.graal.rulesetanalyser.property.FUSProperty;
import fr.lirmm.graphik.graal.rulesetanalyser.property.RuleSetProperty;
import fr.lirmm.graphik.graal.rulesetanalyser.util.AnalyserRuleSet;
import fr.lirmm.graphik.util.graph.scc.StronglyConnectedComponentsGraph;

public class FUSAnalyser {
	protected AnalyserRuleSet analyserRuleset;
	
	public FUSAnalyser(AnalyserRuleSet rs) {
		this.analyserRuleset = rs;
	}
	
	public RuleSet maximalFusComponent() {
		RuleSet ret = new LinkedListRuleSet();
		
		List<AnalyserRuleSet> sccs = this.analyserRuleset.getSCC();
		StronglyConnectedComponentsGraph<Rule> sccG = this.analyserRuleset.getStronglyConnectedComponentsGraph();
		
		FUSProperty p = FUSProperty.instance();
		Map<String, RuleSetProperty> properties = RuleSetPropertyHierarchy.generatePropertyMapSpecializationOf(p);
	    
//	    RuleSetPropertyHierarchy hierarchy = new RuleSetPropertyHierarchy(ps);
//	    
//	    Iterable<RuleSetProperty> properties = hierarchy.getOrderedProperties();
//	    
//	    for(RuleSetProperty _p : properties.values()) System.out.println(_p.getLabel());
	    
		Set<Integer> sinks = sccG.getSinks();
		
		Deque<Integer> queue_bfs = new LinkedList<>();
		
		queue_bfs.addAll(sinks);
		
		while(!queue_bfs.isEmpty()) {
			Integer i = queue_bfs.pollFirst();
			AnalyserRuleSet rs = sccs.get(i.intValue());
			
			for(RuleSetProperty _p : properties.values()) {
				if(_p.check(rs) > 0) {
					ret.addAll(rs.iterator());
					break;
				}
			}
			for(int e : sccG.incomingEdgesOf(i)) {
				queue_bfs.add(sccG.getEdgeSource(e));
			}
		}
		
		return ret;
	}
}

package org.guiiis.dwfe.core;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.guiiis.dwfe.DatalogRewriting;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Ontology;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.io.Parser;
import fr.lirmm.graphik.graal.core.atomset.graph.DefaultInMemoryGraphStore;
import fr.lirmm.graphik.graal.core.ruleset.DefaultOntology;
import fr.lirmm.graphik.graal.core.ruleset.LinkedListRuleSet;
import fr.lirmm.graphik.graal.rulesetanalyser.Analyser;
import fr.lirmm.graphik.graal.rulesetanalyser.RuleSetPropertyHierarchy;
import fr.lirmm.graphik.graal.rulesetanalyser.property.FUSProperty;
import fr.lirmm.graphik.graal.rulesetanalyser.property.RuleSetProperty;
import fr.lirmm.graphik.graal.rulesetanalyser.util.AnalyserRuleSet;
import fr.lirmm.graphik.util.profiler.RealTimeProfiler;
import fr.lirmm.graphik.util.stream.IteratorException;


public class DlgKnowledgeBase {
	private PrintStream profileSteam = System.out;
	private Boolean decidable = null;
	
	private final Ontology ruleset;
	private final AtomSet store;
	
	private RuleSet fusRuleSet = null;
	private AnalyserRuleSet alyrs = null;
	private Analyser analyzer = null;
	
	private boolean force_rewriting = true;
	
	public DlgKnowledgeBase(Parser<Object> parser) throws AtomSetException {
		this.ruleset = new DefaultOntology();
		this.store = new DefaultInMemoryGraphStore();
		
		Object o;
		try {
			while (parser.hasNext()) {
				o = parser.next();
				if (o instanceof Rule) {
					this.ruleset.add((Rule) o);
				} else if (o instanceof Atom) {
					this.store.add((Atom) o);
				}
			}
		} catch (IteratorException e) {
			throw new AtomSetException(e);
		}
		
		this.analyze();
	}
	
	public Collection<DatalogRule> rewriteToDlg(ConjunctiveQuery q) {
		DatalogRewriting dr = new DatalogRewriting();			
		dr.setProfiler(new RealTimeProfiler(profileSteam));
		
		if(this.force_rewriting) {
			return dr.exec(q, this.getFUSRuleset());
		}
		else if (this.isDecidable()) {

			return dr.exec(q, this.ruleset);	
		}
		else {
			System.out.println("The ontology is not fus, not suitable for current rewritng approach.");
			return null;
		}
		
	}
	
	public RuleSet getFUSRuleset() {
		if (this.fusRuleSet == null) {
			this.fusRuleSet = new LinkedListRuleSet();

			int[] combine = this.analyzer.combineFUS();
			List<AnalyserRuleSet> scc = this.alyrs.getSCC();
			for (int i = 0; i < combine.length; ++i) {
				if ((combine[i] & Analyser.COMBINE_FUS) != 0) {
					this.fusRuleSet.addAll(scc.get(i).iterator());
				}
			}
		}
		return this.fusRuleSet;		
	}
	
	protected Boolean isDecidable() {
		if(this.decidable != null) return decidable;
		else {					
			this.decidable = this.analyzer.isDecidable();			
			return decidable;
		}
	}
	
	private void analyze() {
		this.alyrs = new AnalyserRuleSet(this.ruleset);
		this.analyzer = new Analyser(this.alyrs);
		
		Map<String, RuleSetProperty> properties = new HashMap<>();
		properties.putAll(RuleSetPropertyHierarchy.generatePropertyMapSpecializationOf(FUSProperty.instance()));
		this.analyzer.setProperties(properties.values());
	}
	
	public void setProfiling(PrintStream o) {
		profileSteam = o;
	}
}

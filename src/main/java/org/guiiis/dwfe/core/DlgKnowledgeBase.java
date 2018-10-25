package org.guiiis.dwfe.core;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.guiiis.dwfe.utils.FUSAnalyser;

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
import fr.lirmm.graphik.graal.rulesetanalyser.Analyser;
import fr.lirmm.graphik.graal.rulesetanalyser.util.AnalyserRuleSet;
import fr.lirmm.graphik.util.profiler.RealTimeProfiler;
import fr.lirmm.graphik.util.stream.IteratorException;


public class DlgKnowledgeBase {
	private PrintStream profileSteam = System.out;
	private Boolean decidable = null;
	
	private final Ontology ruleset;
	private final AtomSet store;
	
	private AnalyserRuleSet analyzerRuleSet = null;
	private Analyser analyzer = null;
	private FUSAnalyser fusAnalyzer = null;
	
	private RuleSet fusComponent = null;
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
		
		this.analyzerRuleSet = new AnalyserRuleSet(this.ruleset);
		this.analyzer = new Analyser(this.analyzerRuleSet);
		this.fusAnalyzer = new FUSAnalyser(this.analyzerRuleSet);
	}
	
	public Collection<DatalogRule> rewriteToDlg(ConjunctiveQuery q) {
		DatalogRewriting dr = new DatalogRewriting();			
		dr.setProfiler(new RealTimeProfiler(profileSteam));
		
		if(this.force_rewriting) {
			if(this.fusComponent == null) {
				this.fusComponent = this.fusAnalyzer.maximalFusComponent();
				System.out.println(this.fusComponent);
			}
			return dr.exec(q, this.fusComponent);
		}
		else if (this.isDecidable()) {

			return dr.exec(q, this.ruleset);	
		}
		else {
			System.out.println("The ontology is not fus, not suitable for current rewritng approach.");
			return null;
		}
		
	}
	
	protected Boolean isDecidable() {
		if(this.decidable != null) return decidable;
		else {					
			this.decidable = this.analyzer.isDecidable();			
			return decidable;
		}
	}
	
	public Ontology getOntology() {
		return this.ruleset;
	}
	
	public void setProfiling(PrintStream o) {
		profileSteam = o;
	}
}

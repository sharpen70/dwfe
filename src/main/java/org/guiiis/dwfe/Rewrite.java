package org.guiiis.dwfe;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import fr.lirmm.graphik.graal.api.backward_chaining.QueryRewriter;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Ontology;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.backward_chaining.pure.AggregSingleRuleOperator;
import fr.lirmm.graphik.graal.backward_chaining.pure.PureRewriter;
import fr.lirmm.graphik.graal.core.ruleset.DefaultOntology;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.rulesetanalyser.Analyser;
import fr.lirmm.graphik.graal.rulesetanalyser.RuleSetPropertyHierarchy;
import fr.lirmm.graphik.graal.rulesetanalyser.property.FUSProperty;
import fr.lirmm.graphik.graal.rulesetanalyser.property.RuleSetProperty;
import fr.lirmm.graphik.graal.rulesetanalyser.util.AnalyserRuleSet;
import fr.lirmm.graphik.util.profiler.RealTimeProfiler;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;

public class Rewrite {
	public static void main(String[] args) throws Exception {
		String dlp = args[0];
		String queries = args[1];
		
		PrintStream outstream = new PrintStream(System.out);
			
		File f = new File(dlp);
	    Ontology onto = new DefaultOntology(new DlgpParser(f));
    	
		AnalyserRuleSet analyserruleset = new AnalyserRuleSet(onto);
		Analyser analyser = new Analyser(analyserruleset);
		
		Map<String, RuleSetProperty> properties = new HashMap<>();
		properties.putAll(RuleSetPropertyHierarchy.generatePropertyMapSpecializationOf(FUSProperty.instance()));
		analyser.setProperties(properties.values());
		if(!analyser.isDecidable())	{
			System.out.println("Not Rewritable!");
			return;
		}
		
		Scanner scanner = new Scanner(new File(queries));
		
    	while(scanner.hasNextLine()) {
    		String qs = scanner.nextLine();
    		ConjunctiveQuery query = DlgpParser.parseQuery(qs);
        	
        	QueryRewriter rewriter = new PureRewriter(new AggregSingleRuleOperator(), false);
        	rewriter.setProfiler(new RealTimeProfiler(outstream));
            CloseableIteratorWithoutException it = rewriter.execute(query, onto);
        	
            while(it.hasNext()) {
            	it.next();
            }
    	}
    	
    	scanner.close();
    	outstream.close();
	}
}

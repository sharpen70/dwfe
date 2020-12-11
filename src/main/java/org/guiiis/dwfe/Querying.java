package org.guiiis.dwfe;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.guiiis.dwfe.core.DlgKnowledgeBase;
import org.guiiis.dwfe.utils.GraalConjunctiveQueryToRule;
import org.guiiis.dwfe.utils.GraalToVlogConverter;
import org.semanticweb.vlog4j.core.model.api.PositiveLiteral;
import org.semanticweb.vlog4j.core.model.api.QueryResult;
import org.semanticweb.vlog4j.core.reasoner.KnowledgeBase;
import org.semanticweb.vlog4j.core.reasoner.QueryResultIterator;
import org.semanticweb.vlog4j.core.reasoner.implementation.VLogReasoner;
import org.semanticweb.vlog4j.parser.ParsingException;
import org.semanticweb.vlog4j.parser.RuleParser;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.core.ruleset.IndexedByHeadPredicatesRuleSet;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;
import fr.lirmm.graphik.util.stream.IteratorException;

public class Querying {
	public static void main(String[] args) throws Exception {		
		String ontologyfile = null;		
		String queriesfile = null;
		String datafile = null;
		
		DlgKnowledgeBase kb;
		
		boolean verbose = false;
		
		for(int i = 0; i < args.length; i++) {
			if(args[i].startsWith("-")) {
				String flag = args[i].substring(1);
				if(flag.equals("i")) {
					ontologyfile = args[++i];
				}
				if(flag.equals("d")) {
					datafile = args[++i];
				}
				if(flag.equals("q")) {
					queriesfile = args[++i];
				}
				if(flag.equals("v")) {
					verbose = true;
				}
			}
		}
		
		if(ontologyfile == null || queriesfile == null) {
			System.out.println("Missing input !");
			return;
		}
		
		Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.OFF);
		
		long start, end, tstart;
		
		tstart = System.currentTimeMillis();
//		start = System.currentTimeMillis();
		File ontofile = new File(ontologyfile);
//		
////		if(!kb.isDecidable()) {
////			System.out.println("Not concrete class in FUS !");
////			return;
////		}
//		end = System.currentTimeMillis();
//		
//
//		System.out.println("Finish compling and checking the decidablility of input ontology, cost " + (end - start) + "ms");
		
		Scanner scn = new Scanner(new File(queriesfile));
 
		if(scn.hasNextLine()) {
			String s = scn.nextLine();
			ConjunctiveQuery query = DlgpParser.parseQuery(s);
			if(verbose) System.out.println("Answering query " + query);
			
			long estart = System.currentTimeMillis();
			kb = new DlgKnowledgeBase(new DlgpParser(ontofile));
			kb.complileRule();
			
			start = System.currentTimeMillis();
			Collection<ConjunctiveQuery> rewriting = kb.rewriteToUCQWithComp(query);
			List<Rule> related = getRelatedCompliedRule(rewriting, kb.getCompliedRule());
			end = System.currentTimeMillis();			
			
			long rew_t = end - start;
			if(verbose) System.out.println("Finish rewriting the query, rewriting size " + rewriting.size() + "(" + related.size() + ") "+ " cost " + (end - start) + " ms");
			else System.out.println(rewriting.size() + " + " + related.size() + ", " + (end - start));
			
			if(datafile != null) {			
				long dstart = System.currentTimeMillis();
				KnowledgeBase vlogbase = new KnowledgeBase();
				
				List<QueryResult> result = new LinkedList<>();				

				try (VLogReasoner reasoner = new VLogReasoner(vlogbase)) {	

					vlogbase.addStatements(GraalToVlogConverter.convertRules(related));
					loadData(vlogbase, datafile);
					
					PositiveLiteral pl = null;
					
					for(ConjunctiveQuery cq : rewriting) {
						GraalConjunctiveQueryToRule qtr = GraalToVlogConverter.convertQuery("ANS", cq);
						vlogbase.addStatement(qtr.getRule());
						if(pl == null) pl = qtr.getQuery();
					}
					reasoner.load();
					long dend = System.currentTimeMillis();
					if(verbose) System.out.println("Vlog Loading time cost " + (dend - dstart) + " ms");
					
					start = System.currentTimeMillis();
					reasoner.reason();					
					QueryResultIterator answers = reasoner.answerQuery(pl, false);
					answers.forEachRemaining(answer -> result.add(answer));
					end = System.currentTimeMillis();
					

					if(verbose) System.out.println("Finish answering queries, answers " + result.size() + " cost " + (end - start + rew_t) + " ms.");
					if(verbose) System.out.println("Total time cost " + (end - tstart) + " ms.");
				}
			}
		}
		
		scn.close();
	}
	
	public static List<Rule> getRelatedCompliedRule(Collection<ConjunctiveQuery> rewritings, IndexedByHeadPredicatesRuleSet compiled_rule) throws IteratorException {
		List<Rule> related = new LinkedList<>();
		
		Queue<CloseableIteratorWithoutException<Atom>> queue = new LinkedList<>();
		Set<Rule> labels = new HashSet<>();
		
		for(ConjunctiveQuery q : rewritings) 
			queue.add(q.getAtomSet().iterator());
		
		while(!queue.isEmpty()) {
			CloseableIteratorWithoutException<Atom> _it = queue.poll();
			
			while(_it.hasNext()) {
				Atom a = _it.next();
				for(Rule r : compiled_rule.getRulesByHeadPredicate(a.getPredicate())) {
					if(labels.add(r)) {
						related.add(r);
						queue.add(r.getBody().iterator());
					}
				}
			}
		}
		
		return related;
	}
	
	public static void loadData(KnowledgeBase kb, String dataDir) throws FileNotFoundException, ParsingException {
		File dir = new File(dataDir);
		for(File csv : dir.listFiles()) {
			String fname = csv.getName();
			String pname = fname.substring(0, fname.indexOf("."));
			Scanner scanner = new Scanner(csv);
			
			String line;
			int arity = 0;
			
			if(scanner.hasNextLine()) {
				line = scanner.nextLine();
				
				Pattern p = Pattern.compile("[^,\"]+|\"([^\"]*)\"");
				Matcher m = p.matcher(line);
				
				while(m.find()) {
					m.group();
					arity++;
				}
				scanner.close();
			}
			
			String import_str = "@source <" + pname + ">[" + arity + "] : load-csv(\"" + csv.getAbsolutePath() + "\") .";
			RuleParser.parseInto(kb, import_str);
		}
	}	
}

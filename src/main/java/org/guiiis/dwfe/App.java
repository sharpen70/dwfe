package org.guiiis.dwfe;

import java.io.File;
import java.util.Collection;

import org.guiiis.dwfe.core.DatalogRule;
import org.guiiis.dwfe.core.DlgRewritingCloseableIterator;
import org.guiiis.dwfe.core.DlgpEWriter;

import fr.lirmm.graphik.dlgp2.parser.DLGP2Parser;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.kb.KBBuilder;

/**
 * Hello world!
 *
 */
public class App 
{
	private static String rootDir = "./input/";
	private static String q1 = "?():-p(Y,Z),p(Z,Y)";
	public static void main(String[] args) throws Exception {
//		// 0 - Create a KBBuilder
//		KBBuilder kbb = new KBBuilder();
//		// 1 - Add a rule
//		Rule r1 = DlgpParser.parseRule("mortal(X) :- human(X, Y).");
//		
//		kbb.add(r1);
////		// 2 - Add a fact
////		kbb.add(DlgpParser.parseAtom("human(socrate)."));
//		// 3 - Generate the KB
//		KnowledgeBase kb = kbb.build();
//		// 4 - Create a DLGP writer to print data
//		DlgpEWriter writer = new DlgpEWriter();
//		
////		// 5 - Parse a query from a Java String
//		ConjunctiveQuery query = DlgpParser.parseQuery("?() :- mortal(X).");
////		// 6 - Query the KB
////		CloseableIterator resultIterator = kb.query(query);
//		
//		DatalogRewriting dr = new DatalogRewriting();
//		
//		DlgRewritingCloseableIterator it = dr.exec(query, kb);
//		
//		// 7 - Iterate and print results
//		writer.write("\n= Rewriting results =\n");
//		if (it.hasNext()) {
//			do {
//				writer.write(it.next());
//				writer.write("\n");
//			} while (it.hasNext());
//		} else {
//			writer.write("Not Rewritable.\n");
//		}
//		// 8 - Close resources
//		kb.close();
//		writer.close();
		// 0 - Create a KBBuilder
//		KBBuilder kbb = new KBBuilder();
//		// 1 - Add a rule
//		kbb.add(DlgpParser.parseRule("mortal(X) :- human(X)."));
//		// 2 - Add a fact
//		kbb.add(DlgpParser.parseAtom("human(socrate)."));
//		// 3 - Generate the KB
//		KnowledgeBase kb = kbb.build();
//		// 4 - Create a DLGP writer to print data
//		DlgpWriter writer = new DlgpWriter();
//		// 5 - Parse a query from a Java String
//		ConjunctiveQuery query = DlgpParser.parseQuery("?(X) :- mortal(X).");
//		// 6 - Query the KB
//		CloseableIterator resultIterator = kb.query(query);
//		// 7 - Iterate and print results
//		writer.write("\n= Answers =\n");
//		if (resultIterator.hasNext()) {
//			do {
//				writer.write(resultIterator.next());
//				writer.write("\n");
//			} while (resultIterator.hasNext());
//		} else {
//			writer.write("No answers.\n");
//		}
//		// 8 - Close resources
//		kb.close();
//		writer.close();
		
		// 0 - Create a KBBuilder
		KBBuilder kbb = new KBBuilder();
	
		kbb.addAll(new DlgpParser(new File(rootDir, "dlg1")));

		KnowledgeBase kb = kbb.build();

		DlgpEWriter writer = new DlgpEWriter();
		
		ConjunctiveQuery query = DlgpParser.parseQuery("?() :- a(X).");
		
		DatalogRewriting dr = new DatalogRewriting();
		
		Collection<DatalogRule> result = dr.exec(query, kb);
		
		writer.write("\n= Rewriting results =\n");
		
		for(DatalogRule r : result) {
			writer.write(r);
			writer.write("\n");
		}
		// 8 - Close resources
		kb.close();
		writer.close();
	}
}

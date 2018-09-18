package org.guiiis.dwfe;

import org.guiiis.dwfe.core.DlgRewritingCloseableIterator;

import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.io.dlp.DlgpWriter;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.util.stream.CloseableIterator;

/**
 * Hello world!
 *
 */
public class App 
{
	public static void main(String[] args) throws Exception {
		// 0 - Create a KBBuilder
		KBBuilder kbb = new KBBuilder();
		// 1 - Add a rule
		kbb.add(DlgpParser.parseRule("mortal(X) :- human(X)."));
		// 2 - Add a fact
		kbb.add(DlgpParser.parseAtom("human(socrate)."));
		// 3 - Generate the KB
		KnowledgeBase kb = kbb.build();
		// 4 - Create a DLGP writer to print data
		DlgpWriter writer = new DlgpWriter();
		
//		// 5 - Parse a query from a Java String
		ConjunctiveQuery query = DlgpParser.parseQuery("?(X) :- mortal(X).");
//		// 6 - Query the KB
//		CloseableIterator resultIterator = kb.query(query);
		
		DatalogRewriting dr = new DatalogRewriting();
		
		DlgRewritingCloseableIterator it = dr.exec(query, kb);
		
		// 7 - Iterate and print results
		writer.write("\n= Rewriting results =\n");
		if (it.hasNext()) {
			do {
				writer.write(it.next());
				writer.write("\n");
			} while (it.hasNext());
		} else {
			writer.write("Not Rewritable.\n");
		}
		// 8 - Close resources
		kb.close();
		writer.close();
	}
}

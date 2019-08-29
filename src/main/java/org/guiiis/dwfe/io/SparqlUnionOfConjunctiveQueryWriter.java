package org.guiiis.dwfe.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.core.UnionOfConjunctiveQueries;
import fr.lirmm.graphik.graal.api.io.WriterException;
import fr.lirmm.graphik.graal.io.sparql.SparqlConjunctiveQueryWriter;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;

public class SparqlUnionOfConjunctiveQueryWriter extends SparqlConjunctiveQueryWriter {
	public SparqlUnionOfConjunctiveQueryWriter(OutputStream o) {
		super(o);
	}
	
//	public SparqlUnionOfConjunctiveQueryWriter write(UnionOfConjunctiveQueries ucq) {
//		this.write("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
//		
//		this.write("SELECT DISTINCT ");
//		
//		for(Term t : ucq.getAnswerVariables()) {
//			this.write(t);
//			this.write(' ');
//		}
//
//		this.write("\nWHERE {\n");
//		
//		List<ConjunctiveQuery> uit = ucq.getConjunctiveQueries();
//		
//		boolean ufirst = true;
//		
//		for(ConjunctiveQuery query : uit) {			
//			if(!ufirst) this.write("\nUNION\n");
//			else ufirst = false;
//			
//			this.write("{ ");
//			
//			boolean isFirst = true;
//			CloseableIteratorWithoutException<Atom> it = query.getAtomSet().iterator();
//			
//			while (it.hasNext()) {
//				Atom a = it.next();
//				if(!isFirst) this.write(" . ");
//				else isFirst = false;
//
//				this.writeAtom(a);
//			}
//			this.write(" }");
//		}
//	
//		this.write("\n}\n");
//
//		return this;
//	}
	
	@Override
	protected void writeAtom(Atom a) throws IOException {
		this.write("\t");
		this.write(a.getTerm(0));
		this.write(' ');

		if (a.getPredicate().getArity() == 1) {
			this.write("rdf:type ");
			this.write(a.getPredicate());
		} else if (a.getPredicate().getArity() == 2) {
			this.write(a.getPredicate());
			this.write(' ');
			this.write(a.getTerm(1));
		} else {
			throw new WriterException("Unsupported predicate arity");
		}
	}
}

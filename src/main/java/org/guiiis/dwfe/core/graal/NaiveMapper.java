package org.guiiis.dwfe.core.graal;

import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.core.mapper.AbstractMapper;
import fr.lirmm.graphik.util.URI;

public class NaiveMapper extends AbstractMapper {
	
	@Override
	public Predicate map(Predicate predicate) {
		return predicate;
	}

	@Override
	public Predicate unmap(Predicate predicate) {
		Object identifier = predicate.getIdentifier();
		
		String id = null;
		if (identifier instanceof String) {
			id = (String) identifier;
		} else if (identifier instanceof URI) {
			id = ((URI) identifier).toString();
		} else {
			return predicate;
		}
		
		int index = id.indexOf("#");
		
		if (index != -1) {
			return new Predicate(id.substring(index + 1), predicate.getArity());
		} else {
			return predicate;
		}
	}
}

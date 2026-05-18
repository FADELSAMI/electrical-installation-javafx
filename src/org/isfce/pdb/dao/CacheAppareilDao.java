package org.isfce.pdb.dao;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.isfce.pdb.model.Appareil;

public class CacheAppareilDao implements IAppareilDao {
	private IAppareilDao dao;
	private Map<String, Appareil> cache = new HashMap<String, Appareil>();

	public CacheAppareilDao(IAppareilDao dao) {
		this.dao = dao;
	}

	@Override
	public Optional<Appareil> getFromID(String id) {
		if (cache.containsKey(id))
			return Optional.of(cache.get(id));

		var oObj = dao.getFromID(id);

		oObj.ifPresent((obj) -> cache.put(id, obj));

		return oObj;
	}
}
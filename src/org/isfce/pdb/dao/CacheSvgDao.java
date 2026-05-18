package org.isfce.pdb.dao;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.isfce.pdb.model.Svg;

public class CacheSvgDao implements ISvgDao {
	private ISvgDao dao;
	private Map<String, Svg> cache = new HashMap<String, Svg>();

	public CacheSvgDao(ISvgDao dao) {
		this.dao = dao;
	}

	@Override
	public Optional<Svg> getFromID(String id) {
		if (cache.containsKey(id))
			return Optional.of(cache.get(id));

		var oObj = dao.getFromID(id);

		oObj.ifPresent((obj) -> cache.put(id, obj));

		return oObj;
	}
}
package org.isfce.pdb.dao;

import org.isfce.pdb.model.Element;
import java.util.List;

public interface IElementDao extends IDAO<Element, Integer> {
	List<Element> getListeFromInstallation(int installation);

}

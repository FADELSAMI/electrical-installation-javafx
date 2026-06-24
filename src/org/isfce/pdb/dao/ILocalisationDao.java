package org.isfce.pdb.dao;

import org.isfce.pdb.model.Localisation;

public interface ILocalisationDao extends IDAO<Localisation, Integer> {

	Localisation insert(int idElement, Localisation localisation) throws Exception;

	boolean update(int idElement, Localisation localisation) throws Exception;

	boolean delete(int idElement) throws Exception;
}
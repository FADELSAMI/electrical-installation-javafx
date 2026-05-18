package org.isfce.pdb.dao;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.isfce.pdb.dao.DAOFactory.TypePersistance;
import org.isfce.pdb.databases.connexion.ConnexionFromFile;
import org.isfce.pdb.databases.connexion.ConnexionSingleton;
import org.isfce.pdb.databases.uri.Databases;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestDaoSvg {

	private static ISvgDao dao;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		ConnexionSingleton.setInfoConnexion(
				new ConnexionFromFile("./ressources/connexionPDB2526_test.properties", Databases.FIREBIRD));

		var factory = DAOFactory.getDAOFactory(
				TypePersistance.FIREBIRD,
				ConnexionSingleton.getConnexion());

		dao = factory.getSvgDAO();
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		ConnexionSingleton.liberationConnexion();
	}

	@Test
	void testGetFromId() {
		var oObj = dao.getFromID("PIP2");
		assertTrue(oObj.isPresent());
	}
}

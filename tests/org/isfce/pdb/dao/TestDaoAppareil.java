package org.isfce.pdb.dao;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.isfce.pdb.dao.DAOFactory.TypePersistance;
import org.isfce.pdb.databases.connexion.ConnexionFromFile;
import org.isfce.pdb.databases.connexion.ConnexionSingleton;
import org.isfce.pdb.databases.uri.Databases;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestDaoAppareil {

	private static IAppareilDao dao;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		ConnexionSingleton.setInfoConnexion(
				new ConnexionFromFile("./ressources/connexionPDB2526_test.properties", Databases.FIREBIRD));

		var factory = DAOFactory.getDAOFactory(
				TypePersistance.FIREBIRD,
				ConnexionSingleton.getConnexion());

		dao = factory.getAppareilDAO();
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		ConnexionSingleton.liberationConnexion();
	}

	@Test
	void testGetFromId() {
		var oObj = dao.getFromID("IC1");
		assertTrue(oObj.isPresent());
	}
}

package org.isfce.pdb.dao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.isfce.pdb.dao.DAOFactory.TypePersistance;
import org.isfce.pdb.databases.connexion.ConnexionFromFile;
import org.isfce.pdb.databases.connexion.ConnexionSingleton;
import org.isfce.pdb.databases.uri.Databases;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestDaoInstallation {

	private static IInstallationDao dao;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		ConnexionSingleton.setInfoConnexion(
				new ConnexionFromFile("./ressources/connexionPDB2526_test.properties", Databases.FIREBIRD));

		var factory = DAOFactory.getDAOFactory(
				TypePersistance.FIREBIRD,
				ConnexionSingleton.getConnexion());

		dao = factory.getInstallationDAO();
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		ConnexionSingleton.liberationConnexion();
	}

	@Test
	void testGetFromId() {
		var oObj = dao.getFromID(3);
		assertTrue(oObj.isPresent());
	}

	@Test
	void testGetListe() {
		var liste = dao.getListe(null);
		assertFalse(liste.isEmpty());
	}
}

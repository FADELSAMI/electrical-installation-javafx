package org.isfce.pdb.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import org.isfce.pdb.dao.DAOFactory.TypePersistance;
import org.isfce.pdb.databases.connexion.ConnexionFromFile;
import org.isfce.pdb.databases.connexion.ConnexionSingleton;
import org.isfce.pdb.databases.uri.Databases;
import org.isfce.pdb.model.Installation;
import org.isfce.pdb.model.Plan;
import org.isfce.pdb.util.DatabaseUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestDaoPlan {

	private static IPlanDao dao;
	private static IInstallationDao daoInstallation;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		ConnexionSingleton.setInfoConnexion(
				new ConnexionFromFile("./ressources/connexionPDB2526_test.properties", Databases.FIREBIRD));

		DatabaseUtil.executeScriptSQL(ConnexionSingleton.getConnexion(), "./ressources/scriptInitDBTest.sql");

		var factory = DAOFactory.getDAOFactory(TypePersistance.FIREBIRD, ConnexionSingleton.getConnexion());
		dao = factory.getPlanDAO();
		daoInstallation = factory.getInstallationDAO();
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		DatabaseUtil.executeScriptSQL(ConnexionSingleton.getConnexion(), "./ressources/scriptInitDBTest.sql");
		ConnexionSingleton.liberationConnexion();
	}

	@Test
	void testGetFromId() {
		var oObj = dao.getFromID(1);

		assertTrue(oObj.isPresent());
		assertEquals("Rez-de-chaussée", oObj.get().getNom());
		assertEquals("rez.png", oObj.get().getFichier());
	}

	@Test
	void testGetListePlanFromInstallation() {
		var liste = dao.getListePlanFromInstallation(3);

		assertEquals(2, liste.size());
		assertEquals("Etage", liste.get(0).getNom());
		assertEquals("Rez-de-chaussée", liste.get(1).getNom());
	}

	@Test
	void testInsert() throws Exception {
		Installation installation = daoInstallation.getFromID(3).get();

		Plan plan = new Plan(
				0,
				"Grenier",
				"grenier.png",
				BigDecimal.valueOf(2),
				installation
		);

		Plan retour = dao.insert(plan);

		assertTrue(retour.getId() > 0);
		assertEquals("Grenier", retour.getNom());
		assertEquals("grenier.png", retour.getFichier());
		assertEquals(BigDecimal.valueOf(2), retour.getEtage());
		assertEquals(3, retour.getInstallation().getId());
	}
}
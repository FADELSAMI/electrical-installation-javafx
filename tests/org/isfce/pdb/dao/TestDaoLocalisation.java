package org.isfce.pdb.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.isfce.pdb.dao.DAOFactory.TypePersistance;
import org.isfce.pdb.databases.connexion.ConnexionFromFile;
import org.isfce.pdb.databases.connexion.ConnexionSingleton;
import org.isfce.pdb.databases.uri.Databases;
import org.isfce.pdb.model.Localisation;
import org.isfce.pdb.model.Piece;
import org.isfce.pdb.util.DatabaseUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestDaoLocalisation {

	private static ILocalisationDao dao;
	private static IPieceDao daoPiece;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		ConnexionSingleton.setInfoConnexion(
				new ConnexionFromFile("./ressources/connexionPDB2526_test.properties", Databases.FIREBIRD));

		DatabaseUtil.executeScriptSQL(ConnexionSingleton.getConnexion(), "./ressources/scriptInitDBTest.sql");

		var factory = DAOFactory.getDAOFactory(TypePersistance.FIREBIRD, ConnexionSingleton.getConnexion());
		dao = factory.getLocalisationDAO();
		daoPiece = factory.getPieceDAO();
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		DatabaseUtil.executeScriptSQL(ConnexionSingleton.getConnexion(), "./ressources/scriptInitDBTest.sql");
		ConnexionSingleton.liberationConnexion();
	}

	@Test
	void testInsertEtGetFromId() throws Exception {
		Piece piece = daoPiece.getFromID(1).get();

		Localisation loc = Localisation.builder()
				.piece(piece)
				.x(100)
				.y(200)
				.angle(90)
				.place(true)
				.build();

		dao.insert(1, loc);

		var oObj = dao.getFromID(1);

		assertTrue(oObj.isPresent());
		assertEquals(piece, oObj.get().getPiece());
		assertEquals(100, oObj.get().getX());
		assertEquals(200, oObj.get().getY());
		assertEquals(90, oObj.get().getAngle());
		assertTrue(oObj.get().isPlace());
	}

	@Test
	void testUpdate() throws Exception {
		Piece piece = daoPiece.getFromID(2).get();

		Localisation loc = Localisation.builder()
				.piece(piece)
				.x(10)
				.y(20)
				.angle(0)
				.place(true)
				.build();

		dao.insert(2, loc);

		Localisation locModifiee = Localisation.builder()
				.piece(piece)
				.x(300)
				.y(400)
				.angle(180)
				.place(false)
				.build();

		assertTrue(dao.update(2, locModifiee));

		var oObj = dao.getFromID(2);

		assertTrue(oObj.isPresent());
		assertEquals(300, oObj.get().getX());
		assertEquals(400, oObj.get().getY());
		assertEquals(180, oObj.get().getAngle());
		assertEquals(false, oObj.get().isPlace());
	}

	@Test
	void testDelete() throws Exception {
		Piece piece = daoPiece.getFromID(1).get();

		Localisation loc = Localisation.builder()
				.piece(piece)
				.x(50)
				.y(60)
				.angle(45)
				.place(true)
				.build();

		dao.insert(3, loc);

		assertTrue(dao.delete(3));
		assertTrue(dao.getFromID(3).isEmpty());
	}
}
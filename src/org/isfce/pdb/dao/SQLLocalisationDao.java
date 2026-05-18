package org.isfce.pdb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.isfce.pdb.model.Element;
import org.isfce.pdb.model.Localisation;
import org.isfce.pdb.model.Piece;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SQLLocalisationDao implements ILocalisationDao {
	private static String SQL_GET_FROM_ID = """
			SELECT FKELEMENT_LOC, FKPIECE_LOC, X_LOC, Y_LOC, A_LOC
			FROM TLOCALISATION
			WHERE FKELEMENT_LOC = ?
			""";

	private static String SQL_INSERT = """
			INSERT INTO TLOCALISATION (FKELEMENT_LOC, FKPIECE_LOC, X_LOC, Y_LOC, A_LOC)
			VALUES (?, ?, ?, ?, ?)
			""";

	private static String SQL_UPDATE = """
			UPDATE TLOCALISATION
			SET FKPIECE_LOC = ?, X_LOC = ?, Y_LOC = ?, A_LOC = ?
			WHERE FKELEMENT_LOC = ?
			""";

	private static String SQL_DELETE = """
			DELETE FROM TLOCALISATION
			WHERE FKELEMENT_LOC = ?
			""";

	private DAOFactory factory;
	private Connection connexion;

	public SQLLocalisationDao(DAOFactory factory) {
		this.factory = factory;
		this.connexion = factory.getConnection();
	}
	
	@Override
	public Optional<Localisation> getFromID(Integer id) {
		Localisation obj = null;

		try (PreparedStatement ps = connexion.prepareStatement(SQL_GET_FROM_ID)) {
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				Element element = factory.getElementDAO()
						.getFromID(rs.getInt("FKELEMENT_LOC"))
						.get();

				Piece piece = factory.getPieceDAO()
						.getFromID(rs.getInt("FKPIECE_LOC"))
						.get();

				obj = Localisation.builder()
						.element(element)
						.piece(piece)
						.x(rs.getBigDecimal("X_LOC"))
						.y(rs.getBigDecimal("Y_LOC"))
						.angle(rs.getBigDecimal("A_LOC"))
						.build();

				log.debug("Localisation chargée: " + obj);
			}

		} catch (SQLException e) {
			log.error(e.getMessage());
		}

		return Optional.ofNullable(obj);
	}
	
	@Override
	public Localisation insert(Localisation obj) throws Exception {
		assert obj != null : "L'objet doit exister";

		try (PreparedStatement ps = connexion.prepareStatement(SQL_INSERT)) {
			ps.setInt(1, obj.getElement().getId());
			ps.setInt(2, obj.getPiece().getId());
			ps.setBigDecimal(3, obj.getX());
			ps.setBigDecimal(4, obj.getY());
			ps.setBigDecimal(5, obj.getAngle());

			ps.executeUpdate();

			if (!connexion.getAutoCommit())
				connexion.commit();

			log.debug("Localisation ajoutée: " + obj);

		} catch (SQLException e) {
			log.error("Insertion non validée: " + e);

			if (!connexion.getAutoCommit())
				connexion.rollback();

			this.factory.dispatchException(e, "TLOCALISATION");
		}

		return obj;
	}
	
	@Override
	public boolean update(Localisation obj) throws Exception {
		try (PreparedStatement ps = connexion.prepareStatement(SQL_UPDATE)) {
			ps.setInt(1, obj.getPiece().getId());
			ps.setBigDecimal(2, obj.getX());
			ps.setBigDecimal(3, obj.getY());
			ps.setBigDecimal(4, obj.getAngle());
			ps.setInt(5, obj.getElement().getId());

			int nb = ps.executeUpdate();

			if (!connexion.getAutoCommit())
				connexion.commit();

			return nb == 1;

		} catch (SQLException e) {
			log.error("Update non validé: " + e);

			if (!connexion.getAutoCommit())
				connexion.rollback();

			this.factory.dispatchException(e, "TLOCALISATION");
		}

		return false;
	}
	
	@Override
	public boolean delete(Localisation obj) throws Exception {
		try (PreparedStatement ps = connexion.prepareStatement(SQL_DELETE)) {
			ps.setInt(1, obj.getElement().getId());

			int nb = ps.executeUpdate();

			if (!connexion.getAutoCommit())
				connexion.commit();

			return nb == 1;

		} catch (SQLException e) {
			log.error("Delete non validé: " + e);

			if (!connexion.getAutoCommit())
				connexion.rollback();

			this.factory.dispatchException(e, "TLOCALISATION");
		}

		return false;
	}
}

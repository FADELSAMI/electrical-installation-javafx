package org.isfce.pdb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.isfce.pdb.model.Localisation;
import org.isfce.pdb.model.Piece;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SQLLocalisationDao implements ILocalisationDao {
	private static String SQL_GET_FROM_ID = """
			SELECT FKELEMENT_LOC, FKPIECE_LOC, X_LOC, Y_LOC, A_LOC, PLACE_LOC
			FROM TLOCALISATION
			WHERE FKELEMENT_LOC = ?
			""";

	private static String SQL_INSERT = """
			INSERT INTO TLOCALISATION (FKELEMENT_LOC, FKPIECE_LOC, X_LOC, Y_LOC, A_LOC, PLACE_LOC)
			VALUES (?, ?, ?, ?, ?, ?)
			""";

	private static String SQL_UPDATE = """
			UPDATE TLOCALISATION
			SET FKPIECE_LOC = ?, X_LOC = ?, Y_LOC = ?, A_LOC = ?, PLACE_LOC = ?
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
	public Optional<Localisation> getFromID(Integer idElement) {
		Localisation obj = null;

		try (PreparedStatement ps = connexion.prepareStatement(SQL_GET_FROM_ID)) {
			ps.setInt(1, idElement);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				Piece piece = factory.getPieceDAO()
						.getFromID(rs.getInt("FKPIECE_LOC"))
						.get();

				obj = Localisation.builder()
						.piece(piece)
						.x(rs.getDouble("X_LOC"))
						.y(rs.getDouble("Y_LOC"))
						.angle(rs.getDouble("A_LOC"))
						.place(rs.getBoolean("PLACE_LOC"))
						.build();

				log.debug("Localisation chargée: " + obj);
			}
		} catch (SQLException e) {
			log.error(e.getMessage());
		}

		return Optional.ofNullable(obj);
	}

	@Override
	public Localisation insert(int idElement, Localisation obj) throws Exception {
		assert obj != null : "L'objet localisation doit exister";

		try (PreparedStatement ps = connexion.prepareStatement(SQL_INSERT)) {
			ps.setInt(1, idElement);
			ps.setInt(2, obj.getPiece().getId());
			ps.setDouble(3, obj.getX());
			ps.setDouble(4, obj.getY());
			ps.setDouble(5, obj.getAngle());
			ps.setBoolean(6, obj.isPlace());

			ps.executeUpdate();

			if (!connexion.getAutoCommit())
				connexion.commit();

			log.debug("Localisation ajoutée: " + obj);
		} catch (SQLException e) {
			log.error("Insertion localisation non validée: " + e);

			if (!connexion.getAutoCommit())
				connexion.rollback();

			this.factory.dispatchException(e, "[INS] TLOCALISATION");
		}

		return obj;
	}

	@Override
	public boolean update(int idElement, Localisation obj) throws Exception {
		boolean ok = false;

		try (PreparedStatement ps = connexion.prepareStatement(SQL_UPDATE)) {
			ps.setInt(1, obj.getPiece().getId());
			ps.setDouble(2, obj.getX());
			ps.setDouble(3, obj.getY());
			ps.setDouble(4, obj.getAngle());
			ps.setBoolean(5, obj.isPlace());
			ps.setInt(6, idElement);

			int nb = ps.executeUpdate();

			if (nb == 1) {
				if (!connexion.getAutoCommit())
					connexion.commit();
				ok = true;
			}
		} catch (SQLException e) {
			log.error("Update localisation non validé: " + e);

			if (!connexion.getAutoCommit())
				connexion.rollback();

			this.factory.dispatchException(e, "[UPD] TLOCALISATION");
		}

		return ok;
	}

	@Override
	public boolean delete(int idElement) throws Exception {
		boolean ok = false;

		try (PreparedStatement ps = connexion.prepareStatement(SQL_DELETE)) {
			ps.setInt(1, idElement);

			int nb = ps.executeUpdate();

			if (nb == 1) {
				if (!connexion.getAutoCommit())
					connexion.commit();
				ok = true;
			}
		} catch (SQLException e) {
			log.error("Delete localisation non validé: " + e);

			if (!connexion.getAutoCommit())
				connexion.rollback();

			this.factory.dispatchException(e, "[DEL] TLOCALISATION");
		}

		return ok;
	}
}

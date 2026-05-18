package org.isfce.pdb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.isfce.pdb.model.Plan;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SQLPlanDao implements IPlanDao {
	private static String SQL_GET_LISTE_FROM_INSTALLATION = """
			SELECT ID_PLA, NOM_PLA
			FROM TPLAN
			WHERE FKINSTALLATION_PLA = ?
			ORDER BY NOM_PLA
			""";

	private static String SQL_INSERT = """
			INSERT INTO TPLAN (NOM_PLA, FKINSTALLATION_PLA)
			VALUES (?, ?)
			""";

	private DAOFactory factory;
	private Connection connexion;

	public SQLPlanDao(DAOFactory factory) {
		this.factory = factory;
		this.connexion = factory.getConnection();
	}

	@Override
	public List<Plan> getListePlanFromInstallation(int installation) {
		List<Plan> liste = new ArrayList<>();

		try (PreparedStatement ps = connexion.prepareStatement(SQL_GET_LISTE_FROM_INSTALLATION)) {
			ps.setInt(1, installation);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				liste.add(new Plan(
						rs.getInt("ID_PLA"),
						rs.getString("NOM_PLA"),
						installation
				));
			}

			log.debug("Liste des plans chargée pour l'installation: " + installation);

		} catch (SQLException e) {
			log.error(e.getMessage());
		}

		return liste;
	}

	@Override
	public Plan insert(Plan obj) throws Exception {
		assert obj != null && obj.id() == null : "L'objet doit exister sans ID ";

		try (PreparedStatement ps = connexion.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, obj.nom().trim());
			ps.setInt(2, obj.installation());

			int nb = ps.executeUpdate();

			if (nb == 1) {
				ResultSet rs = ps.getGeneratedKeys();

				if (rs.next()) {
					obj = new Plan(rs.getInt(1), obj.nom(), obj.installation());

					if (!connexion.getAutoCommit())
						connexion.commit();
				}
			}

		} catch (SQLException e) {
			log.error("Insertion non validée: " + e);

			if (!connexion.getAutoCommit())
				connexion.rollback();

			this.factory.dispatchException(e, "TPLAN");
		}

		return obj;
	}
}

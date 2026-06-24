package org.isfce.pdb.dao;

import java.sql.Connection; 
import java.util.Optional;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.isfce.pdb.model.Installation;
import org.isfce.pdb.model.Plan;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SQLPlanDao implements IPlanDao {
	private static String SQL_GET_LISTE_FROM_INSTALLATION = """
			SELECT ID_PLA, NOM_PLA, FICHIER_PLA
			FROM TPLAN
			WHERE FKINSTALLATION_PLA = ?
			ORDER BY NOM_PLA
			""";
	
	private static String SQL_GET_FROM_ID = """
			SELECT ID_PLA, NOM_PLA, FICHIER_PLA, FKINSTALLATION_PLA
			FROM TPLAN
			WHERE ID_PLA = ?
			""";

	private static String SQL_INSERT = """
			INSERT INTO TPLAN (NOM_PLA, FICHIER_PLA, FKINSTALLATION_PLA)
			VALUES (?, ?, ?)
			""";

	private DAOFactory factory;
	private Connection connexion;

	public SQLPlanDao(DAOFactory factory) {
		this.factory = factory;
		this.connexion = factory.getConnection();
	}
	
	@Override
	public Optional<Plan> getFromID(Integer id) {
		Plan obj = null;

		try (PreparedStatement ps = connexion.prepareStatement(SQL_GET_FROM_ID)) {
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				Installation inst = factory.getInstallationDAO()
						.getFromID(rs.getInt("FKINSTALLATION_PLA"))
						.orElse(null);

				obj = new Plan(
						rs.getInt("ID_PLA"),
						rs.getString("NOM_PLA"),
						rs.getString("FICHIER_PLA"),
						inst
				);
			}

		} catch (SQLException e) {
			log.error(e.getMessage());
		}

		return Optional.ofNullable(obj);
	}

	@Override
	public List<Plan> getListePlanFromInstallation(int installation) {
		List<Plan> liste = new ArrayList<>();

		try (PreparedStatement ps = connexion.prepareStatement(SQL_GET_LISTE_FROM_INSTALLATION)) {
			ps.setInt(1, installation);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				Installation inst = factory.getInstallationDAO()
						.getFromID(installation)
						.orElse(null);

				liste.add(new Plan(
						rs.getInt("ID_PLA"),
						rs.getString("NOM_PLA"),
						rs.getString("FICHIER_PLA"),
						inst
				));
			}
		} catch (SQLException e) {
			log.error(e.getMessage());
		}

		return liste;
	}

	@Override
	public Plan insert(Plan obj) throws Exception {
		assert obj != null : "L'objet doit exister";

		try (PreparedStatement ps = connexion.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, obj.getNom().trim());
			ps.setString(2, obj.getFichier().trim());
			ps.setInt(3, obj.getInstallation().getId());

			int nb = ps.executeUpdate();

			if (nb == 1) {
				ResultSet rs = ps.getGeneratedKeys();

				if (rs.next()) {
					obj.setId(rs.getInt(1));

					if (!connexion.getAutoCommit())
						connexion.commit();
				}
			}
		} catch (SQLException e) {
			log.error("Insertion non validée: " + e);

			if (!connexion.getAutoCommit())
				connexion.rollback();

			this.factory.dispatchException(e, "[INS] TPLAN");
		}

		return obj;
	}
}
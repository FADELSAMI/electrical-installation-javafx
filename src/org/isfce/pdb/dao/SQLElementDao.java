package org.isfce.pdb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.isfce.pdb.model.Appareil;
import org.isfce.pdb.model.Element;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SQLElementDao implements IElementDao {
	private static String SQL_GET_LISTE_FROM_INSTALLATION = """
			SELECT ID_ELE, FKAPPAREIL_ELE, QT_ELE, CODE_ELE, INFO_ELE, ORDRE_ELE
			FROM TELEMENT
			WHERE FKINSTALLATION_ELE = ?
			ORDER BY ORDRE_ELE
			""";
	
	private static String SQL_GET_FROM_ID = """
			SELECT ID_ELE, FKAPPAREIL_ELE, QT_ELE, CODE_ELE, INFO_ELE, ORDRE_ELE
			FROM TELEMENT
			WHERE ID_ELE = ?
			""";

	private DAOFactory factory;
	private Connection connexion;

	public SQLElementDao(DAOFactory factory) {
		this.factory = factory;
		this.connexion = factory.getConnection();
	}

	@Override
	public List<Element> getListeFromInstallation(int installation) {
		List<Element> liste = new ArrayList<>();

		try (PreparedStatement ps = connexion.prepareStatement(SQL_GET_LISTE_FROM_INSTALLATION)) {
			ps.setInt(1, installation);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				Appareil appareil = factory.getAppareilDAO()
						.getFromID(rs.getString("FKAPPAREIL_ELE"))
						.get();

				Element obj = Element.builder()
						.id(rs.getInt("ID_ELE"))
						.appareil(appareil)
						.qt(rs.getInt("QT_ELE"))
						.code(rs.getString("CODE_ELE"))
						.info(rs.getString("INFO_ELE"))
						.ordre(rs.getInt("ORDRE_ELE"))
						.build();

				liste.add(obj);
			}

			log.debug("Liste des éléments chargée pour l'installation: " + installation);

		} catch (SQLException e) {
			log.error(e.getMessage());
		}

		return liste;
	}
	
	@Override
	public Optional<Element> getFromID(Integer id) {
		Element obj = null;

		try (PreparedStatement ps = connexion.prepareStatement(SQL_GET_FROM_ID)) {
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				Appareil appareil = factory.getAppareilDAO()
						.getFromID(rs.getString("FKAPPAREIL_ELE"))
						.get();

				obj = Element.builder()
						.id(rs.getInt("ID_ELE"))
						.appareil(appareil)
						.qt(rs.getInt("QT_ELE"))
						.code(rs.getString("CODE_ELE"))
						.info(rs.getString("INFO_ELE"))
						.ordre(rs.getInt("ORDRE_ELE"))
						.build();

				log.debug("Un élément est chargé: " + obj);
			}

		} catch (SQLException e) {
			log.error(e.getMessage());
		}

		return Optional.ofNullable(obj);
	}
}

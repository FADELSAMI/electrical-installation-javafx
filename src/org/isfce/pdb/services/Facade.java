package org.isfce.pdb.services;

import org.isfce.pdb.model.Localisation;
import java.math.BigDecimal;
import java.io.BufferedReader; 
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Flow.Subscriber;

import org.isfce.pdb.dao.DAOFactory;
import org.isfce.pdb.exceptions.InstallationException;
import org.isfce.pdb.model.Element;
import org.isfce.pdb.model.Installation;
import org.isfce.pdb.model.Piece;
import org.isfce.pdb.model.Plan;
import org.isfce.pdb.model.TypePiece;
import org.isfce.pdb.services.ListMessages.Classe;
import org.isfce.pdb.services.ListMessages.Evenement;
import org.isfce.pdb.view.bundle.I18N;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Facade {
	private DAOFactory factory;
	private Installation installation;
	// Le publisher
	private PublisherEvent publisher;

	// liste des plans de l'installation FAKE
	private List<Plan> plans = new ArrayList<Plan>();
	// pièces fake des plans
	private List<Piece> pieces = new ArrayList<Piece>();
	private List<Element> elements = new ArrayList<Element>();

	// propriétés du projet
	@Getter
	private Properties properties = new Properties();

	public Facade(DAOFactory factory) {
		this.factory = factory;
		// Le Publisher pour publier les évènements
		publisher = new PublisherEvent();
		// charges le fichier de propriétés
		chargerProperties();
	}

	/**
	 * Charge une installation via son ID fake: (ici je limite à l'installation 1)
	 * 
	 * @param id
	 * @throws InstallationException
	 */
	public void chargeInstallation(int id) throws InstallationException {
		installation = factory.getInstallationDAO()
				.getFromID(id)
				.orElseThrow(() -> new InstallationException(I18N.getString("err.install.inconnue")));

		plans.clear();
		pieces.clear();
		elements.clear();

		plans.addAll(factory.getPlanDAO().getListePlanFromInstallation(id));
		System.out.println("NB PLANS = " + plans.size());

		pieces.addAll(factory.getPieceDAO().getListeFromInstallation(id));
		System.out.println("NB PIECES = " + pieces.size());
		
		elements.addAll(factory.getElementDAO().getListeFromInstallation(id));
		System.out.println("NB ELEMENTS = " + elements.size());

		for (Element element : elements) {
			factory.getLocalisationDAO()
					.getFromID(element.getId())
					.ifPresent(element::setLocalisation);
		}
	}

	/**
	 * Obtenir l'installation courante
	 * 
	 * @return
	 * @throws InstallationException
	 */
	public Installation getCurrentInstallation() throws InstallationException {
		if (installation == null)
			throw new InstallationException(I18N.getString("err.noInstall"));
		return installation;
	}
	
	public List<Element> getListeElements() {
		return elements;
	}

	/**
	 * retourne tous les types de pièces
	 * 
	 * @return
	 */
	public List<TypePiece> getTypePiece() {
		return factory.getTypePieceDAO().getListe(null);
	}

	/**
	 * Ajout d'une nouvelle pièce
	 */
	public void insertPiece(Piece piece) throws InstallationException {
		try {
			factory.getPieceDAO().insert(piece);
			// publie l'évènement
			Map<Classe, Evenement> messages = new HashMap<>();
			messages.put(Classe.PIECE, new Evenement(TypeOperation.INSERT, piece));
			publisher.submit(new ListMessages(messages));
		} catch (Exception e) {
			if (e instanceof InstallationException exception)
				throw exception;
		}

	}

	public List<Piece> getListePieces() {
		if (installation != null)
			return factory.getPieceDAO().getListeFromInstallation(installation.getId());
		else
			return List.of();
	}

	/**
	 * Suppression d'une pièce
	 * 
	 * @param piece
	 * @return
	 * @throws InstallationException
	 */
	public boolean deletePiece(Piece piece) throws InstallationException {
		// tente de supprimer la pièce mais elle ne doit pas encore être utilisée
		// (Localisation)
		// ici je fais juste une suppression et vérifie si je n'ai pas une exception
		boolean ok = false;
		try {
			ok = factory.getPieceDAO().delete(piece);
			// publie l'évènement
			Map<Classe, Evenement> messages = new HashMap<>();
			messages.put(Classe.PIECE, new Evenement(TypeOperation.DELETE, piece));
			publisher.submit(new ListMessages(messages));
		} catch (Exception e) {
			if (e instanceof InstallationException exc)
				throw exc;
		}
		return ok;
	}

	/**
	 * Mise à jour d'une pièce
	 * 
	 * @param piece
	 * @return
	 * @throws InstallationException
	 */
	public boolean updatePiece(Piece piece) throws InstallationException {
		boolean ok = false;
		try {
			ok = factory.getPieceDAO().update(piece);
			// publie l'évènement
			Map<Classe, Evenement> messages = new HashMap<>();
			messages.put(Classe.PIECE, new Evenement(TypeOperation.UPDATE, piece));
			publisher.submit(new ListMessages(messages));
		} catch (Exception e) {
			if (e instanceof InstallationException exc)
				throw exc;
		}
		return ok;
	}

	/**
	 * charge une pièce à partir de son id
	 * 
	 * @param id
	 * @return
	 */
	public Optional<Piece> getPiece(Integer id) {
		return factory.getPieceDAO().getFromID(id);
	}

	/**
	 * Retourne la liste des plans de l'installation
	 * 
	 * @return
	 * @throws InstallationException
	 */
	public List<Plan> getListePlans() {
		return plans;
	}

	/**
	 * Retourne les pièces associées au plan
	 * 
	 * @param plan
	 * @return
	 */
	public List<Piece> getPiecesPlan(Plan plan) {
		return pieces.stream().filter(p -> plan.equals(p.getPlan())).toList();
	}

	/**
	 * retourne les éléments d'une pièce
	 * 
	 * @param piece
	 * @return
	 */
	public List<Element> getElementPiece(Piece piece) {
		return elements.stream()
				.filter(e -> piece.equals(e.getLocalisation() == null ? null : e.getLocalisation().getPiece()))
				.toList();
	}

	/***
	 * Retourne la liste des éléments déjà placés sur le plan
	 * 
	 * @param plan
	 * @return
	 */
	public List<Element> getElementsPlacesDansPlanBOL(Plan plan) {
		assert plan != null : " Le plan est à null";

		return elements.stream()
				.filter(element -> element.getLocalisation() != null)
				.filter(element -> element.getLocalisation().isPlace())
				.filter(element -> element.getLocalisation().getPiece() != null)
				.filter(element -> element.getLocalisation().getPiece().getPlan() != null)
				.filter(element -> plan.getId() == element.getLocalisation().getPiece().getPlan().getId())
				.toList();
	}

	/**
	 * Retourne tous les éléments qui sont ou doivent apparaître sur le plan
	 * 
	 * @param plan
	 */
	public List<Element> getElementsPlan(Plan plan) {
		assert plan != null : " Le plan est à null";

		return elements.stream()
				.filter(element -> element.getLocalisation() != null)
				.filter(element -> element.getLocalisation().getPiece() != null)
				.filter(element -> element.getLocalisation().getPiece().getPlan() != null)
				.filter(element -> plan.getId() == element.getLocalisation().getPiece().getPlan().getId())
				.toList();
	}

	/**
	 * Permet de s'abonner aux évènements sur le modèle
	 */

	public void addObserver(Subscriber<ListMessages> obs) {
		publisher.addObserver(obs);
	}

	private void chargerProperties() {
		String fichier = "./ressources/installation.properties";
		// charge les propriétes de l'application
		try (BufferedReader br = new BufferedReader(new FileReader(fichier))) {
			properties.load(br);// charge toutes les propriétés du fichier dans la map
		} catch (IOException e) {
			log.error("Problème de chargement du fichier " + fichier + " : " + e.getMessage());
		}
	}
	
	public void sauverLocalisations() throws InstallationException {
		try {
			for (Element element : elements) {
				if (element.getLocalisation() != null) {
					if (factory.getLocalisationDAO().getFromID(element.getId()).isPresent()) {
						factory.getLocalisationDAO().update(element.getId(), element.getLocalisation());
					} else {
						factory.getLocalisationDAO().insert(element.getId(), element.getLocalisation());
					}
				}
			}
		} catch (Exception e) {
			if (e instanceof InstallationException exc)
				throw exc;
			throw new InstallationException(I18N.getString("err.save"));
		}
	}
	
	public void sauverLocalisation(Element element) throws InstallationException {
		try {
			if (element.getLocalisation() != null) {
				if (factory.getLocalisationDAO().getFromID(element.getId()).isPresent())
					factory.getLocalisationDAO().update(element.getId(), element.getLocalisation());
				else
					factory.getLocalisationDAO().insert(element.getId(), element.getLocalisation());
			}
		} catch (Exception e) {
			throw new InstallationException(I18N.getString("err.save"));
		}
	}
	
	public List<Installation> getListeInstallations() {
		return factory.getInstallationDAO().getListe(null);
	}
	
	
	public void associerElementPiece(Element element, Piece piece) throws InstallationException {
		element.setLocalisation(new Localisation(piece, 0, 0, 0, false));
		sauverLocalisation(element);
	}
	
	public void ajouterPlan(String nom, String fichier, BigDecimal etage) throws InstallationException {
		try {
			if (installation == null)
				throw new InstallationException(I18N.getString("err.noInstall"));

			Plan plan = new Plan(
					0,
					nom.trim(),
					fichier.trim(),
					etage,
					installation
			);

			plan = factory.getPlanDAO().insert(plan);
			plans.add(plan);

		} catch (Exception e) {
			throw new InstallationException(I18N.getString("err.save"));
		}
	}

}
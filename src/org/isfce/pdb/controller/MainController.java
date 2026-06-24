package org.isfce.pdb.controller;

import org.isfce.pdb.view.element.VueListeElementsController;
import java.io.IOException;
import java.sql.Connection;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import org.isfce.pdb.view.installation.VueChargeInstallationController;
import org.isfce.pdb.view.plan.VuePlanController;

import org.isfce.pdb.dao.DAOFactory;
import org.isfce.pdb.dao.DAOFactory.TypePersistance;
import org.isfce.pdb.databases.connexion.ConnexionFromFile;
import org.isfce.pdb.databases.connexion.ConnexionSingleton;
import org.isfce.pdb.databases.uri.Databases;
//import org.isfce.pdb.exceptions.InstallationException;
import org.isfce.pdb.services.Facade;
import org.isfce.pdb.view.bundle.I18N;
import org.isfce.pdb.view.piece.VueListePiecesController;
import org.isfce.pdb.view.piece.VuePieceController;
import org.isfce.pdb.view.plan.VueImplantationController;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainController extends Application {
//Facade
	private Facade facade;
//Factory
	private DAOFactory factory;

//MainStage
	private Stage mainStage;

	private Stage vueImplantation;

//property pour savoir si une installation est chargée
	private BooleanProperty installationChargee = new SimpleBooleanProperty(false);

//Lancement de l'application
	@Override
	public void start(Stage mainStage) {
		// Locale
		Locale.setDefault(Locale.FRENCH);
		// mémorise la fénêtre principale
		this.mainStage = mainStage;

		/*
		 * connexion à la base de données création de la fabrique
		 */
		factory = connexionToDatabase();

		// facade
		facade = new Facade(factory);

		// conteneur principal
		BorderPane cp = new BorderPane();

		// Liste de boutons
		VBox leftPane = new VBox(10);
		leftPane.setFillWidth(true);
		leftPane.setPadding(new Insets(10));
		Button bt1 = new Button(I18N.getString("bt.load"));
		leftPane.getChildren().add(bt1);
		// bt1
		bt1.setOnAction(this::actionChargeInstallation);
		bt1.setMaxWidth(Double.MAX_VALUE);

		// bt2
		Button bt2 = new Button(I18N.getString("bt.cree.piece"));
		leftPane.getChildren().add(bt2);
		bt2.setOnAction(this::actionCreePiece);
		bt2.setMaxWidth(Double.MAX_VALUE);
		bt2.disableProperty().bind(installationChargee.not());

		// bt3
		Button bt3 = new Button(I18N.getString("bt.liste.piece"));
		leftPane.getChildren().add(bt3);
		bt3.setOnAction(this::actionListePieces);
		bt3.setMaxWidth(Double.MAX_VALUE);
		bt3.disableProperty().bind(installationChargee.not());

		// bt4
		Button bt4 = new Button(I18N.getString("bt.implantation"));
		leftPane.getChildren().add(bt4);
		bt4.setOnAction(this::actionImplantation);
		bt4.setMaxWidth(Double.MAX_VALUE);
		bt4.disableProperty().bind(installationChargee.not());
		
		Button bt5 = new Button("Eléments");
		leftPane.getChildren().add(bt5);
		bt5.setOnAction(this::actionListeElements);
		bt5.setMaxWidth(Double.MAX_VALUE);
		bt5.disableProperty().bind(installationChargee.not());
		
		Button bt6 = new Button("Ajouter Plan");
		leftPane.getChildren().add(bt6);
		bt6.setOnAction(this::actionAjouterPlan);
		bt6.setMaxWidth(Double.MAX_VALUE);
		bt6.disableProperty().bind(installationChargee.not());

		cp.setLeft(leftPane);

		Scene scene = new Scene(cp, 500, 400);
		mainStage.setScene(scene);
		mainStage.setTitle("Projet PDB 2526");
		mainStage.show();

	}

	/**
	 * 
	 * 
	 */
	public void showAddPiece() {
		// bundle
		ResourceBundle bundle;
		// Crée une stage
		Stage stage = new Stage();
		// indique sa stage parent
		stage.initOwner(mainStage);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setX(100);
		stage.setY(50);

		// Crée un loader pour charger la vue FXML
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/isfce/pdb/view/piece/VuePiece.fxml"));

		try {
			bundle = I18N.getInstance().getGlobalBundle();
			loader.setResources(bundle);
			// Obtenir la traduction du titre dans la locale
			stage.setTitle(bundle.getString("piece.titre"));
		} catch (Exception e) {
			log.error("Imposible de charger le buddle pour la vue Piece" + e.getMessage());
			showErreur("Impossible de charger le buddle ");
			stage.setTitle("Vue Piece");
		}
		// Charge la vue à partir du Loader
		// et initialise son contenu en appelant la méthode setUp du controleur
		AnchorPane root;
		try {
			root = loader.load();

			// récupère le ctrl (après l'initialisation)
			VuePieceController ctrl = loader.getController();
			// fourni au controleur l'accès à la fabrique et sa stage
			ctrl.setUp(this, stage);

			// charge le Pane dans la Stage
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/org/isfce/pdb/view/css/pdb2526.css").toExternalForm());
			stage.setScene(scene);
			stage.showAndWait();
		} catch (IOException e) {
			log.error("Imposible de charger la vue Piece");
			showErreur("Impossible de charger la vue Piece: " + e.getMessage());
		}
		stage = null;
	}
	
	private void showAjoutPlan() {
		// bundle
		ResourceBundle bundle;
		// Crée une stage
		Stage stage = new Stage();
		// indique sa stage parent
		stage.initOwner(mainStage);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setX(100);
		stage.setY(50);

		// Crée un loader pour charger la vue FXML
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/isfce/pdb/view/plan/VuePlan.fxml"));

		try {
			bundle = I18N.getInstance().getGlobalBundle();
			loader.setResources(bundle);
			// Obtenir la traduction du titre dans la locale
			stage.setTitle(bundle.getString("piece.titre"));
		} catch (Exception e) {
			log.error("Imposible de charger le buddle pour la vue Plan" + e.getMessage());
			showErreur("Impossible de charger le buddle ");
			stage.setTitle("Vue Plan");
		}
		// Charge la vue à partir du Loader
		// et initialise son contenu en appelant la méthode setUp du controleur
		AnchorPane root;
		try {
			root = loader.load();

			// récupère le ctrl (après l'initialisation)
			VuePlanController ctrl = loader.getController();
			// fourni au controleur l'accès à la fabrique et sa stage
			ctrl.setUp(this, stage);

			// charge le Pane dans la Stage
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/org/isfce/pdb/view/css/pdb2526.css").toExternalForm());
			stage.setScene(scene);
			stage.showAndWait();
		} catch (IOException e) {
			log.error("Imposible de charger la vue Piece");
			showErreur("Impossible de charger la vue Piece: " + e.getMessage());
		}
		stage = null;
	}

	private void showListePieces() {
		// bundle
		ResourceBundle bundle;
		// Crée une stage
		Stage stage = new Stage();
		// indique sa stage parent
		stage.initOwner(mainStage);
		// stage.initModality(Modality.APPLICATION_MODAL);
		stage.setX(100);
		stage.setY(50);

		// Crée un loader pour charger la vue FXML
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/isfce/pdb/view/piece/VueListePieces.fxml"));

		try {
			bundle = I18N.getInstance().getGlobalBundle();
			loader.setResources(bundle);
			// Obtenir la traduction du titre dans la locale
			stage.setTitle(bundle.getString("piece.liste.titre"));
		} catch (Exception e) {
			log.error("Imposible de charger le buddle pour la vue liste pieces" + e.getMessage());
			showErreur("Impossible de charger le buddle ");
			stage.setTitle("Vue Liste Pieces");
		}
		// Charge la vue à partir du Loader
		// et initialise son contenu en appelant la méthode setUp du controleur
		AnchorPane root;
		try {
			root = loader.load();

			// récupère le ctrl (après l'initialisation)
			VueListePiecesController ctrl = loader.getController();
			// fourni au controleur l'accès à la fabrique et sa stage
			ctrl.setUp(this, stage);

			// charge le Pane dans la Stage
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/org/isfce/pdb/view/css/pdb2526.css").toExternalForm());
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			log.error("Imposible de charger la vue ListePieces");
			showErreur("Impossible de charger la vue ListePieces: " + e.getMessage());
		}
		stage = null;
	}
	
	private void showListeElements() {
		// bundle
		ResourceBundle bundle;
		// Crée une stage
		Stage stage = new Stage();
		// indique sa stage parent
		stage.initOwner(mainStage);
		// stage.initModality(Modality.APPLICATION_MODAL);
		stage.setX(100);
		stage.setY(50);

		// Crée un loader pour charger la vue FXML
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/isfce/pdb/view/element/VueListeElements.fxml"));

		try {
			bundle = I18N.getInstance().getGlobalBundle();
			loader.setResources(bundle);
			// Obtenir la traduction du titre dans la locale
			stage.setTitle("Liste des éléments");
		} catch (Exception e) {
			log.error("Imposible de charger le buddle pour la vue liste pieces" + e.getMessage());
			showErreur("Impossible de charger le buddle ");
			stage.setTitle("Vue Liste Elements");
		}
		// Charge la vue à partir du Loader
		// et initialise son contenu en appelant la méthode setUp du controleur
		AnchorPane root;
		try {
			root = loader.load();

			// récupère le ctrl (après l'initialisation)
			VueListeElementsController ctrl = loader.getController();
			// fourni au controleur l'accès à la fabrique et sa stage
			ctrl.setUp(this, stage);

			// charge le Pane dans la Stage
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/org/isfce/pdb/view/css/pdb2526.css").toExternalForm());
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
			log.error("Impossible de charger la vue ListeElements", e);
			showErreur("Impossible de charger la vue ListeElements: " + e.getMessage());
		}
		stage = null;
	}
	
	private void showChargeInstallation() {
		// bundle
		ResourceBundle bundle;
		// Crée une stage
		Stage stage = new Stage();
		// indique sa stage parent
		stage.initOwner(mainStage);
		// stage.initModality(Modality.APPLICATION_MODAL);
		stage.setX(100);
		stage.setY(50);

		// Crée un loader pour charger la vue FXML
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/isfce/pdb/view/installation/VueChargeInstallation.fxml"));

		try {
			bundle = I18N.getInstance().getGlobalBundle();
			loader.setResources(bundle);
			// Obtenir la traduction du titre dans la locale
			stage.setTitle(bundle.getString("piece.liste.titre"));
		} catch (Exception e) {
			log.error("Imposible de charger le buddle pour la vue liste pieces" + e.getMessage());
			showErreur("Impossible de charger le buddle ");
			stage.setTitle("Vue Charge Installation");
		}
		// Charge la vue à partir du Loader
		// et initialise son contenu en appelant la méthode setUp du controleur
		AnchorPane root;
		try {
			root = loader.load();

			// récupère le ctrl (après l'initialisation)
			VueChargeInstallationController ctrl = loader.getController();
			ctrl.setUp(this, stage);

			// charge le Pane dans la Stage
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/org/isfce/pdb/view/css/pdb2526.css").toExternalForm());
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
			log.error("Impossible de charger l''installtion", e);
			showErreur("Impossible de charger l''installation: " + e.getMessage());
		}
		stage = null;
	}

	private void showImplantation() {
		if (vueImplantation != null) {
			vueImplantation.show();
			return;
		}
		// bundle
		ResourceBundle bundle;
		// Crée une stage
		Stage stage = new Stage();
		// indique sa stage parent
		stage.initOwner(mainStage);
		// stage.initModality(Modality.APPLICATION_MODAL);
		stage.setX(0);
		stage.setY(0);

		// Crée un loader pour charger la vue FXML
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/isfce/pdb/view/plan/VueImplantation.fxml"));

		try {
			bundle = I18N.getInstance().getGlobalBundle();
			loader.setResources(bundle);
			// Obtenir la traduction du titre dans la locale
			stage.setTitle(bundle.getString("implantation.titre"));
		} catch (Exception e) {
			log.error("Imposible de charger le buddle pour la vue implantation" + e.getMessage());
			showErreur("Impossible de charger le buddle ");
			stage.setTitle("Vue Implantation");
		}
		// Charge la vue à partir du Loader
		// et initialise son contenu en appelant la méthode setUp du controleur
		BorderPane root;
		try {
			root = loader.load();

			// récupère le ctrl (après l'initialisation)
			VueImplantationController ctrl = loader.getController();
			
			// charge le Pane dans la Stage
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/org/isfce/pdb/view/css/pdb2526.css").toExternalForm());
			stage.setScene(scene);
			// fourni au controleur l'accès à la fabrique et sa stage
			ctrl.setUp(this, stage);
			vueImplantation = stage;
			stage.show();
		} catch (IOException e) {
			log.error("Imposible de charger la vue Implantation");
			showErreur("Impossible de charger la vue Implantation: " + e.getMessage());
		}
		stage = null;
	}

	/**
	 * Boite de confirmation
	 * 
	 * @param message
	 * @return
	 */
	public boolean showConfirmation(String message) {
		Alert a = new Alert(AlertType.CONFIRMATION, message);
		Optional<ButtonType> result = a.showAndWait();
		return result.get() == ButtonType.OK;
	}

	/**
	 * Vue pour afficher les messages d'erreur
	 * 
	 * @param message
	 */
	public void showErreur(String message) {
		Alert a = new Alert(AlertType.ERROR, message);
		a.showAndWait();
	}

	/**
	 * Point d'entrée principal de l'application
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}

	public Facade getFacade() {
		return facade;
	}

	/**
	 * Connexion à la base de données sur base du contenu du fichier
	 * "connexionPDB2526.properties"
	 */
	private DAOFactory connexionToDatabase() {
		DAOFactory factory = null;
		// Connexion à la BD
		try {
			ConnexionSingleton.setInfoConnexion(
					new ConnexionFromFile("./ressources/connexionPDB2526.properties", Databases.FIREBIRD));
			Connection connect = ConnexionSingleton.getConnexion();
			log.info("Connexion établie");
			// Crée la factory Firebird
			factory = DAOFactory.getDAOFactory(TypePersistance.FIREBIRD, connect);
		} catch (Exception e) {
			showErreur("Problème de connexion");
			// Quitte l'application
			Platform.exit();
		}
		return factory;
	}

	/***************************************************************
	 * Liste d'actions
	 **************************************************************/
	public void actionChargeInstallation(ActionEvent event) {
		showChargeInstallation();
	}

	public void actionCreePiece(ActionEvent event) {
		showAddPiece();
	}

	public void actionListePieces(ActionEvent event) {
		showListePieces();
	}

	public void actionImplantation(ActionEvent event) {
		showImplantation();
	}
	
	public void actionListeElements(ActionEvent event) {
		showListeElements();
	}
	
	public void actionAjouterPlan(ActionEvent event) {
		showAjoutPlan();
	}
	
	public void setInstallationChargee(boolean valeur) {
		installationChargee.set(valeur);
	}
}

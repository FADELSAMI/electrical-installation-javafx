package org.isfce.pdb.view.plan;

import org.isfce.pdb.controller.MainController;
import org.isfce.pdb.exceptions.InstallationException;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class VuePlanController {

	private MainController ctrl;
	private Stage stage;

	@FXML
	private TextField txtNom;

	@FXML
	private TextField txtFichier;
	
	@FXML
	private TextField txtEtage;

	public void setUp(MainController ctrl, Stage stage) {
		this.ctrl = ctrl;
		this.stage = stage;
	}

	@FXML
	void actionAjouter() {
		try {
			ctrl.getFacade().ajouterPlan(
					txtNom.getText(),
					txtFichier.getText(),
					new java.math.BigDecimal(txtEtage.getText()));

			stage.close();
		} catch (InstallationException e) {
			ctrl.showErreur(e.getMessage());
		}
	}

	@FXML
	void actionAnnuler() {
		stage.close();
	}
}
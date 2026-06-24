package org.isfce.pdb.view.installation;

import org.isfce.pdb.controller.MainController;
import org.isfce.pdb.exceptions.InstallationException;
import org.isfce.pdb.model.Installation;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

public class VueChargeInstallationController {

	private MainController ctrl;
	private Stage stage;

	@FXML
	private ComboBox<Installation> cbInstallations;

	public void setUp(MainController ctrl, Stage stage) {
		this.ctrl = ctrl;
		this.stage = stage;

		cbInstallations.setItems(
				FXCollections.observableArrayList(
						ctrl.getFacade().getListeInstallations()));

		if (!cbInstallations.getItems().isEmpty())
			cbInstallations.getSelectionModel().selectFirst();
	}

	@FXML
	void actionCharger() {
		Installation installation = cbInstallations.getValue();

		if (installation != null) {
			try {
				ctrl.getFacade().chargeInstallation(installation.getId());
				ctrl.setInstallationChargee(true);
				stage.close();
			} catch (InstallationException e) {
				ctrl.showErreur(e.getMessage());
			}
		}
	}

	@FXML
	void actionAnnuler() {
		stage.close();
	}
}
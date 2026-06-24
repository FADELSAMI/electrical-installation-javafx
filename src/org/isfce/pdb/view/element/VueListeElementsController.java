package org.isfce.pdb.view.element;

import org.isfce.pdb.controller.MainController;
import org.isfce.pdb.model.Element;
import org.isfce.pdb.model.Piece;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;

public class VueListeElementsController {

	private MainController ctrl;
	private Stage stage;

	@FXML
	private TableColumn<Element, String> colPiece;
	
	@FXML
	private TableView<Element> tblElements;

	@FXML
	private TableColumn<Element, String> colCode;

	@FXML
	private TableColumn<Element, String> colAppareil;

	public void setUp(MainController ctrl, Stage stage) {
		this.ctrl = ctrl;
		this.stage = stage;

		colCode.setCellValueFactory(data ->
				new SimpleStringProperty(data.getValue().getCode()));

		colAppareil.setCellValueFactory(data ->
				new SimpleStringProperty(
						data.getValue().getAppareil().getNom()));

		tblElements.setItems(
				FXCollections.observableArrayList(
						ctrl.getFacade().getListeElements()));
		
		colPiece.setCellValueFactory(data -> {
			Piece piece = data.getValue().getLocalisation() == null ? null : data.getValue().getLocalisation().getPiece();
			return new SimpleStringProperty(piece == null ? "" : piece.getNom());
		});
		
		colPiece.setCellFactory(col -> new TableCell<>() {
			private final ComboBox<Piece> combo = new ComboBox<>();

			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);

				if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
					setGraphic(null);
					return;
				}

				Element element = getTableView().getItems().get(getIndex());

				combo.setOnAction(null);

				combo.setItems(FXCollections.observableArrayList(ctrl.getFacade().getListePieces()));

				Piece pieceActuelle = null;
				if (element.getLocalisation() != null)
					pieceActuelle = element.getLocalisation().getPiece();

				combo.setValue(pieceActuelle);

				combo.setOnAction(e -> {
					Piece nouvellePiece = combo.getValue();

					if (nouvellePiece != null) {
						try {
							ctrl.getFacade().associerElementPiece(element, nouvellePiece);

							if (element.getLocalisation() != null)
								element.getLocalisation().setPiece(nouvellePiece);

							colPiece.setCellValueFactory(data -> {
								Piece p = data.getValue().getLocalisation() == null ? null
										: data.getValue().getLocalisation().getPiece();
								return new SimpleStringProperty(p == null ? "" : p.getNom());
							});

							getTableView().refresh();

						} catch (Exception ex) {
							ctrl.showErreur("Erreur lors de l'association");
						}
					}
				});

				setGraphic(combo);
			}
		});
	}
}
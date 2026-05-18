package org.isfce.pdb.services;

import java.time.LocalDate;
import java.util.List;

import org.isfce.pdb.dao.DAOFactory;
import org.isfce.pdb.exceptions.InstallationException;
import org.isfce.pdb.model.Adresse;
import org.isfce.pdb.model.Element;
import org.isfce.pdb.model.Installation;
import org.isfce.pdb.model.Piece;

public class Facade {

	private DAOFactory factory;
	private Installation installation;
	//pièces de l'installation
	private List<Piece> pieces;
	//élements de l'installation
	private List<Element> elements;

	public Facade(DAOFactory factory) {
		this.factory=factory;
	}
	
	public void chargeInstallation(int id) throws InstallationException {
	//	ResourceBundle bundle = I18N.getInstance().getBundle("view.Installation.bundle.VueInstallation");
		if (id==1) {
		Installation inst=Installation.builder()
				.adresse(new Adresse("Rue J buedts",1040,"Etterbeek"))
				.date(LocalDate.of(2026, 5, 12))
				.id(1)
				.Installateur("moi")
				.proprietaire("truc").build();
		}
		else throw new InstallationException("Installation inconnue");
				
		
		
		
	}
	
	

}

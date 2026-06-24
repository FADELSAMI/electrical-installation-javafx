package org.isfce.pdb.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Plan {
	private int id;
	private String nom;
	private String fichier;
	private Installation installation;
	
	@Override
	public String toString() {
	    return nom;
	}
}


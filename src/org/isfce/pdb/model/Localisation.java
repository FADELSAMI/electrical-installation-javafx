package org.isfce.pdb.model;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Localisation {
	private Element element;
	private Piece piece;
	private BigDecimal x;
	private BigDecimal y;
	private BigDecimal angle;
}

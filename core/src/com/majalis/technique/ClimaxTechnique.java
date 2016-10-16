package com.majalis.technique;

import com.majalis.character.AbstractCharacter.Stance;

public class ClimaxTechnique extends TechniquePrototype {

	public ClimaxTechnique(Stance usableStance, Stance resultingStance, String name, Stance forceStance, ClimaxType type) {
		super(usableStance, resultingStance, name);
		this.climaxType = type;
		this.forceStance = forceStance;
	}
	
	public enum ClimaxType {
		ANAL,
		ORAL,
		FACIAL,
		BACKWASH
	}
}
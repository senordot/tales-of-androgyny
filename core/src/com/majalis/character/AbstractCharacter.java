package com.majalis.character;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.majalis.battle.Attack;

import java.lang.reflect.Field;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.IntArray;
/*
 * Abstract character class, both enemies and player characters extend this class
 */
public abstract class AbstractCharacter extends Group implements Json.Serializable {
	
	// some of these ints will be enumerators or objects in time
	/* permanent stats */
	public String label;
	public boolean secondPerson;
	
	/* rigid stats */	
	public int level;
	public int baseStrength;
	public int baseVitality;
	public int baseAgility;
	public int basePerception;
	public int baseMagic;
	public int baseCharisma;
	public int baseLuck; // 0 for most classes, can go negative
	
	public int baseEvade;
	public int baseBlock;
	public int baseParry;
	public int baseCounter;
	
	public IntArray healthTiers; // total these to receive maxHealth, maybe cache it when this changes
	public IntArray staminaTiers; // total these to receive maxStamina, maybe cache it when this changes
	public IntArray manaTiers; // total these to receive maxMana, maybe cache it when this changes
	
	/* morphic stats */
	public int currentHealth;
	public int currentStamina;
	public int currentMana; // mana might be replaced with spell slots that get refreshed
	
	public int stability;
	public int focus;
	public int fortune;
	
	// public Weapon weapon;
	// public Shield shield;
	// public Armor armor;
	// public Gauntlet gauntlet;
	// public Sabaton sabaton;
	// public Accessory firstAccessory;
	// public Accessory secondAccessory;
	
	public Stance stance;
	// public ObjectMap<StatusTypes, Status>; // status effects will be represented by a map of Enum to Status object
	/* Constructors */
	protected AbstractCharacter(){}
	public AbstractCharacter(boolean defaultValues){
		if (defaultValues){
			secondPerson = false;
			level = 1;
			baseStrength = baseVitality = baseAgility = basePerception = baseMagic = baseCharisma = 3;
			baseLuck = 0;
			baseEvade = 0;
			baseBlock = 0;
			baseParry = 0;
			baseCounter = 0;
			healthTiers = new IntArray(new int[]{5});
			staminaTiers = new IntArray(new int[]{5});
			manaTiers = new IntArray(new int[]{0});
			currentHealth = getMaxHealth();
			currentStamina = getMaxStamina();
			currentMana = getMaxMana();
			stability = focus = fortune = 10;
			stance = Stance.BALANCED;			
		}
	}
	
	protected abstract Technique getTechnique(AbstractCharacter target);
	
	protected int getMaxHealth() { return getMax(healthTiers); }
	protected int getMaxStamina() { return getMax(staminaTiers); }
	protected int getMaxMana() { return getMax(manaTiers); }
	protected int getMax(IntArray tiers){
		int max = 0;
		for (int ii = 0; ii < tiers.size; ii++){
			max += tiers.get(ii);
		}
		return max;
	}
	protected int getStaminaRegen() { return 2; }
	
	public String getLabel (){
		return label;
	}
	
	public Boolean getSecondPerson(){
		return secondPerson;
	}
	
	public int getCurrentHealth(){
		return currentHealth;
	}
	
	public int getCurrentStamina(){
		return currentStamina;
	}
	
	protected void setStaminaToMax() {
		currentStamina = getMaxStamina();
	}
	
	public int getStability(){
		return stability;
	}
	
	protected void setStabilityToMax(){
		stability = 10;
	}
	
	public void setStability(int stability){
		this.stability = stability;
	}
	
	
	public Stance getStance(){
		return stance;
	}
	
	public void setStance(Stance stance){
		this.stance = stance;
	}
	
	public int getStrength(){
		return baseStrength;
	}
	
	public int getVitality(){
		return baseVitality;
	}

	/* Serialization methods */
	@Override
	public void write(Json json) {
		writeFields(json, AbstractCharacter.class.getDeclaredFields());		
	}
	
	protected void writeFields(Json json, Field[] fields){
		for (Field field : fields){
			try {
				if (!field.isSynthetic()){
					json.writeValue(field.getName(), field.get(this));
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}
	
	@Override
	public void read(Json json, JsonValue jsonData) {
		for (JsonValue jsonValue : jsonData){
			try {
				Class<?> thisClass = this.getClass();
				switch (jsonValue.type()){
					case booleanValue: thisClass.getField(jsonValue.name).set(this, jsonValue.asBoolean()); break;
					case doubleValue: thisClass.getField(jsonValue.name).set(this, jsonValue.asInt()); break;
					case longValue: thisClass.getField(jsonValue.name).set(this, jsonValue.asInt()); break;
					case stringValue: 
						if (jsonValue.name.equals("stance")) stance = Stance.valueOf(jsonValue.asString());
						else thisClass.getField(jsonValue.name).set(this, jsonValue.asString()); 
						break;
					case array:
					case object: // this would need to somehow deserialize the object and place it into the field
					case nullValue:
					default:
				}
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}

	public String doAttack(String technique, AbstractCharacter secondCharacter, Attack attack){
		return label + (secondPerson ? " use " : " uses ") + technique + " against " + secondCharacter.receiveAttack(attack);
	}
	
	public String receiveAttack(Attack attack){
		String result = (secondPerson ? label.toLowerCase() : label) + ". ";

		int damage = attack.getDamage();
		damage -= getVitality();
		if (damage > 0){	
			currentHealth -= damage;
			result += "The blow strikes for " + damage + " damage! ";
		}
		
		Stance forcedStance = attack.getForceStance();
		if (forcedStance != null){
			result += "\n" + label + (secondPerson ? " are " : " is ") + "forced into " + stance.toString() + " stance!";
		}
		
		return result+"\n";
	}
	
	public Technique extractCosts(Technique technique){
		currentStamina += getStaminaRegen();
		currentStamina -= technique.getStaminaCost();
		stability -= technique.getStabilityCost();	
		if (stance != Stance.PRONE && stance != Stance.SUPINE && stance != Stance.DOGGY){
			if (stability <= 0){
				technique = new Technique(Techniques.TRIP, 0);
			}
			else if (currentStamina <= 0){
				technique = new Technique(Techniques.FALL_DOWN, 0);
			}
		}
		
		return technique;
	}

	public enum Stance {
		BALANCED,
		DEFENSIVE,
		OFFENSIVE,
		PRONE,
		SUPINE,
		KNEELING,
		JUMPING, 
		DOGGY, 
		KNOTTED
	}	
}

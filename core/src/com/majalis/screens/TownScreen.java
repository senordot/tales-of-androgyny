package com.majalis.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.majalis.asset.AssetEnum;
import com.majalis.encounter.Background;
import com.majalis.encounter.Background.BackgroundBuilder;
import com.majalis.encounter.EncounterBuilder.Branch;
import com.majalis.encounter.EncounterCode;
import com.majalis.save.MutationResult;
import com.majalis.save.SaveEnum;
import com.majalis.save.SaveManager;
import com.majalis.save.SaveService;

public class TownScreen extends AbstractScreen {

	private static final Array<AssetDescriptor<?>> resourceRequirements = new Array<AssetDescriptor<?>>();
	private static Array<AssetDescriptor<?>> requirementsToDispose = new Array<AssetDescriptor<?>>();
	static {
		resourceRequirements.add(AssetEnum.UI_SKIN.getSkin());
		resourceRequirements.add(AssetEnum.TOWN_BG.getTexture());
		resourceRequirements.add(AssetEnum.STANCE_ARROW.getTexture());
		resourceRequirements.add(AssetEnum.DEFAULT_BACKGROUND.getTexture());
		resourceRequirements.add(AssetEnum.BATTLE_HOVER.getTexture());
		resourceRequirements.add(AssetEnum.BATTLE_TEXTBOX.getTexture());
		resourceRequirements.add(AssetEnum.TEXT_BOX.getTexture());
		resourceRequirements.add(AssetEnum.SHOPKEEP.getTexture());
		resourceRequirements.add(AssetEnum.TRAINER.getTexture());
		resourceRequirements.add(AssetEnum.SHOP_MUSIC.getMusic());
		resourceRequirements.add(AssetEnum.BUTTON_SOUND.getSound());
		resourceRequirements.add(AssetEnum.EQUIP.getSound());
		resourceRequirements.add(AssetEnum.ENCOUNTER_MUSIC.getMusic());
	}
	private final SaveService saveService;
	private final Skin skin;
	private final Background background;
	private final Image arrow;
	private final Sound buttonSound;
	private final Array<TextButton> buttons;
	private final boolean story;
	private final Label console;
	private int time;
	private int selection;
	
	protected TownScreen(ScreenFactory screenFactory, ScreenElements elements, SaveService saveService, int time, boolean story) {
		super(screenFactory, elements, AssetEnum.SHOP_MUSIC);
		this.saveService = saveService;
		this.story = story;
		this.time = time;
		skin = assetManager.get(AssetEnum.UI_SKIN.getSkin());
		background = new BackgroundBuilder(assetManager.get(AssetEnum.TOWN_BG.getTexture()), true).build();
		background.setColor(getTimeColor(time));
		arrow = new Image(assetManager.get(AssetEnum.STANCE_ARROW.getTexture()));
		buttonSound = assetManager.get(AssetEnum.BUTTON_SOUND.getSound());
		
		
		buttons = new Array<TextButton>();
		selection = 0;
		console = new Label("", skin);
	}

	private Color getTimeColor(int time) { return TimeOfDay.getTime(time).getColor(); }
	
	private ClickListener getListener(final EncounterCode code) {
		return new ClickListener() {
	        @Override
	        public void clicked(InputEvent event, float x, float y) {
	        	buttonSound.play(Gdx.app.getPreferences("tales-of-androgyny-preferences").getFloat("volume") *.5f);
	        	saveService.saveDataValue(SaveEnum.CONTEXT, SaveManager.GameContext.ENCOUNTER);
	        	saveService.saveDataValue(SaveEnum.RETURN_CONTEXT, SaveManager.GameContext.TOWN);
	        	saveService.saveDataValue(SaveEnum.ENCOUNTER_CODE, code);
	        	showScreen(ScreenEnum.CONTINUE);    
	        }
	    };
	}
	
	@Override
	public void buildStage() {
		Table table = new Table();
		table.align(Align.topLeft);
        table.setPosition(1200, 860);
		
		Array<String> buttonLabels = new Array<String>();
		buttonLabels.addAll("General Store", "Blacksmith", "Inn", "Bank", "Brothel", "Town Square", "Rest", "Depart");
		
		for (int ii = 0; ii < buttonLabels.size; ii++) {
			if (story && buttonLabels.get(ii).equals("Town Square")) continue;
			TextButton button = new TextButton(buttonLabels.get(ii), skin);
			buttons.add(button);
			
			table.add(button).size(300, 60).row();
		}
		int ii = 0;
		for (TextButton button : buttons) {
			button.addListener(getListener(ii++));
		}
		
		buttons.get(0).addListener(getListener(EncounterCode.SHOP));
		buttons.get(1).addListener(getListener(EncounterCode.WEAPON_SHOP));
		buttons.get(2).addListener(getListener(EncounterCode.INN));  	
		buttons.get(3).addListener(getListener(EncounterCode.BANK));  	
		buttons.get(4).addListener(getListener(EncounterCode.BROTHEL));  	
		if (!story) buttons.get(5).addListener(getListener(EncounterCode.TOWN_CRIER)); 
		buttons.get(story ? 5 : 6).addListener(new ClickListener() {
	        @Override
	        public void clicked(InputEvent event, float x, float y) {
	        	passTime(1);
	        }
	    });
		buttons.get(story ? 6 : 7).addListener(new ClickListener() {
	        @Override
	        public void clicked(InputEvent event, float x, float y) {
	        	buttonSound.play(Gdx.app.getPreferences("tales-of-androgyny-preferences").getFloat("volume") *.5f);
	        	saveService.saveDataValue(SaveEnum.CONTEXT, SaveManager.GameContext.WORLD_MAP);
	        	showScreen(ScreenEnum.CONTINUE);    
	        }
	    });
		
        this.addActor(background);
        this.addActor(table);
        this.addActor(arrow);
        
        arrow.setSize(45, 75);
        setArrowPosition();
        arrow.setPosition(arrow.getX(), arrow.getY() + 60 * (buttons.size-1));
        
        this.addActor(console);
		console.setPosition(900, 150);
		console.setAlignment(Align.top);
	}
	
	private void passTime(int timePass) {
		buttonSound.play(Gdx.app.getPreferences("tales-of-androgyny-preferences").getFloat("volume") *.5f);
		time += timePass;
		Array<MutationResult> temp = saveService.saveDataValue(SaveEnum.HEALTH, 10 * timePass);
		Array<MutationResult> results = saveService.saveDataValue(SaveEnum.TIME, timePass);
		results.addAll(temp);
		console.setText(getResults(results));
		background.setColor(getTimeColor(time));
	}
	
	private String getResults(Array<MutationResult> results) {
		String result = "";
		for (MutationResult mr : results) {
			result += mr.getText() + "\n";
		}
		return result.trim();
	}
	
	@Override
	public void render(float delta) {
		super.render(delta);
		OrthographicCamera camera = (OrthographicCamera) getCamera();
        batch.setTransformMatrix(camera.view);
        
        if(Gdx.input.isKeyJustPressed(Keys.UP)) {
        	if (selection > 0) selection--;
        	else selection = buttons.size-1;
        	setArrowPosition();
        }
        else if(Gdx.input.isKeyJustPressed(Keys.DOWN)) {
        	if (selection < buttons.size- 1) selection++;
        	else selection = 0;
        	setArrowPosition();
        }
        else if(Gdx.input.isKeyJustPressed(Keys.ENTER)) {
        	InputEvent event1 = new InputEvent();
            event1.setType(InputEvent.Type.touchDown);
            buttons.get(selection).fire(event1);

            InputEvent event2 = new InputEvent();
            event2.setType(InputEvent.Type.touchUp);
            buttons.get(selection).fire(event2);
        }
        else if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			showScreen(ScreenEnum.MAIN_MENU);
		}
        
		camera.update();
		batch.setProjectionMatrix(camera.combined);
	}
	
	private void setArrowPosition() {
		Vector2 buttonPosition = buttons.get(selection).localToStageCoordinates(new Vector2(0,0));
		arrow.setPosition(buttonPosition.x-43, buttonPosition.y-8);
	}
	
	private ClickListener getListener(final int index) {
		return new ClickListener() {
	        @Override
	        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				selection = index;
				setArrowPosition();
	        }
	    };
	}

	@Override
	public void dispose() {
		for (AssetDescriptor<?> path : requirementsToDispose) {
			if (path.fileName.equals(AssetEnum.BUTTON_SOUND.getSound().fileName) || path.type == Music.class)
				continue;
			assetManager.unload(path.fileName);
		}
		requirementsToDispose = new Array<AssetDescriptor<?>>();
	}
	
	public static Array<AssetDescriptor<?>> getRequirements(Branch encounter) {
		Array<AssetDescriptor<?>> requirements = new Array<AssetDescriptor<?>>(resourceRequirements);
		requirements.addAll(encounter.getRequirements());
		requirementsToDispose = requirements;
		return requirements;
	}
}

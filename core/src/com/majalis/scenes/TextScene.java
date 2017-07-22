package com.majalis.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.majalis.character.PlayerCharacter;
import com.majalis.encounter.Background;
import com.majalis.encounter.LogDisplay;
import com.majalis.save.MutationResult;
import com.majalis.save.SaveEnum;
import com.majalis.save.SaveService;
import com.majalis.screens.EncounterScreen;
import com.majalis.screens.TimeOfDay;

public class TextScene extends AbstractTextScene  {
	
	private final Array<Mutation> mutations;
	private final Background background;
	private final AssetManager assetManager;
	private final PlayerCharacter character;
	private final AssetDescriptor<Music> music;
	private final AssetDescriptor<Sound> sound;
	
	public TextScene(OrderedMap<Integer, Scene> sceneBranches, int sceneCode, AssetManager assetManager, BitmapFont font, SaveService saveService, Background background, String toDisplay, Array<Mutation> mutations, PlayerCharacter character, LogDisplay log, AssetDescriptor<Music> music, AssetDescriptor<Sound> sound) {
		super(sceneBranches, sceneCode, assetManager, font, character, saveService, background);
		this.assetManager = assetManager;
		this.character = character;
		display.setText(toDisplay);
		this.mutations = mutations != null ? mutations : new Array<Mutation>();
		this.background = background;
		this.music = music;
		this.sound = sound;
		ScrollPane pane = new ScrollPane(log);
		log.setAlignment(Align.topLeft);
		log.setWrap(true);
		pane.setScrollingDisabled(true, false);
		pane.setOverscroll(false, false);
		pane.setBounds(325, 350, 1300, 700);
		this.addActor(pane);	
		log.setColor(Color.DARK_GRAY);
		log.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				background.toggleDialogBox(display);
			}
		});
	}	

	@Override
	public String getText() {
		return display.getText().toString();
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		if (Gdx.input.isKeyJustPressed(Keys.TAB)) {
			background.toggleDialogBox(display);
		}
	}
	
	// careful!  This calls mutate() on all the mutations, which does not flush to the save file! Causing a flush to the save file in this method will cause mutations to replay every time this scene is loaded
	@Override
	public void setActive() {
		super.setActive();
		if (music != null) {
			EncounterScreen.setMusic(music);
			saveService.saveDataValue(SaveEnum.MUSIC, music.fileName);
		};
		if (sound != null) {
			EncounterScreen.play(sound);
		}
		background.initAnimation();
		Array<MutationResult> results = new Array<MutationResult>();
		for (Mutation mutator: mutations) {
			Array<MutationResult> result = mutator.mutate();
			if (result != null) results.addAll(result);
		}
		
		// save the results to the result array in the save file
		saveService.saveDataValue(SaveEnum.RESULT, results, false);		
		
		for (MutationResult result : results) {
			statusResults.add(new MutationActor(result, assetManager.get(result.getTexture()), skin)).fillY().align(Align.right).row();
		}
		
		if (character.isLoaded()) {
			characterPortrait.addAction(Actions.show());
			characterPortrait.setDrawable(new TextureRegionDrawable(new TextureRegion(assetManager.get(character.getPortraitPath()))));
			masculinityIcon.addAction(Actions.show());
			masculinityIcon.setDrawable(new TextureRegionDrawable(new TextureRegion(assetManager.get(character.getMasculinityPath()))));
			fullnessIcon.addAction(Actions.show());
			fullnessIcon.setDrawable(new TextureRegionDrawable(new TextureRegion(assetManager.get(character.getCumInflationPath()))));
		}
		background.setColor(TimeOfDay.getTime(character.getTime()).getColor());
		if (display.getText().toString().equals("")) nextScene();
	}
	
	@Override
	protected void nextScene() {
		sceneBranches.get(sceneBranches.orderedKeys().get(0)).setActive();
		isActive = false;
		addAction(Actions.hide());
	}
}

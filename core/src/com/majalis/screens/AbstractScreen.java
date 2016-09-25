package com.majalis.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
/*
 * Abstract class which all Screens inherit from; each screen has a a single master Stage.  Allows a Screen to switch to a different screen via an enum.
 */
public abstract class AbstractScreen extends Stage implements Screen {
	private final Game game;
	private final ScreenFactory screenFactory;

	protected final SpriteBatch batch;
	protected final BitmapFont font;
	
    protected AbstractScreen(ScreenFactory screenFactory, ScreenElements elements) {
        super(elements.getViewport());
        this.game = screenFactory.getGame();
        this.screenFactory = screenFactory;
        this.batch = elements.getBatch();
        this.font = elements.getFont();
    }
 
    // Subclasses must load actors in this method
    public abstract void buildStage();
    
    public void showScreen(ScreenEnum screenRequest) {
    	AbstractScreen newScreen = screenFactory.getScreen(screenRequest);
        // Get current screen to dispose it
        Screen currentScreen = game.getScreen();
    	
        // Dispose previous screen
        if (currentScreen != null) {
            currentScreen.dispose();
        }
    	// Show new screen
    	newScreen.buildStage();
        game.setScreen(newScreen);
    }  
    
    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Calling to Stage methods
        super.act(delta);
        super.draw();
    }
 
    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
        font.setUseIntegerPositions(false);
    }
 
    @Override
    public void resize(int width, int height) {
        getViewport().update(width, height, false);
    }
 
    @Override public void hide() {
    	font.dispose();
    }
    @Override public void pause() {}
    @Override public void resume() {}
    
}
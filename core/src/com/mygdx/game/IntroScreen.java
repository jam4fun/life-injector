
package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

public class IntroScreen implements Screen {

	private static final boolean DEBUG_UI = true;
	private static final float scrollSpeed = 0.65f; // unit per second
	private static final float maxScrollTime = 100; // in seconds

	private SpriteBatch spriteBatch;
	private BitmapFont bitmapFont;
	private PerspectiveCamera camera3d;
	private int screenWidth;
	private int screenHeight;
	private Stage stage;
	private Skin skin;
	private TextureRegion skyBox;

	private String text = "L I F E   I N J E C T O R\n\nby the Jam4Fun Team\n\n\nThankfully it has been a\nlong period of peace\nin the universe\nand life is thriving everywhere.\n\nHowever a huge fearsome comet,\nknown as The Big Mower,\nis destroying every life form\nalong its winding path\nacross the universe.\n\nYour mission, as the hero\nof this story, is to follow\nthe tail of the comet\nat the maximum speed,\navoiding obstacles and restoring\nas much life as you can\nbefore it's too late to do so.\n\nIt is a desperate run,\na necessary sacrifice...\n\nMay the force be with you!!!";

	float elapsed;
	WorldScreen worldScreen;
	boolean worldScreenFinishedLoading;

	public IntroScreen (int reqWidth, int reqHeight) {
		screenWidth = reqWidth;
		screenHeight = reqHeight;
		spriteBatch = new SpriteBatch();

		skin = new Skin(Gdx.files.internal("skins/uiskin.json"));

		skyBox = new TextureRegion(new Texture("models/g3db/skybox.jpg"));

		stage = new Stage();
		stage.setDebugAll(DEBUG_UI);
// stageWidth = stage.getWidth();
// stageHeight = stage.getHeight();

		// Add background
		final Image background = new Image(skyBox);
		background.setSize(stage.getWidth(), stage.getHeight());
		stage.addActor(background);

		// Create UI layout
		Table rootTable = new Table(skin);
		rootTable.center().top();
		rootTable.row().pad(15, 15, 15, 15);
		rootTable.add(new TextButton("Info Button", skin)).left();
		rootTable.add(new Label("Game Logo", skin)).height(stage.getHeight() * .3f).expandX();
		final Button playButton = new TextButton("Play Button", skin);
		playButton.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				if (worldScreenFinishedLoading) {
					SpaceGame.game.setScreen(worldScreen);
				}
			}
		});
		rootTable.add(playButton).right();
		rootTable.setFillParent(true);
		stage.addActor(rootTable);

		Gdx.input.setInputProcessor(stage);

		bitmapFont = new BitmapFont();

		bitmapFont.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		bitmapFont.setUseIntegerPositions(false);
		bitmapFont.getData().setScale(.035f);
		bitmapFont.setColor(Color.YELLOW);

		camera3d = new PerspectiveCamera();
	}

	@Override
	public void show () {
		elapsed = 0;
		worldScreenFinishedLoading = false;
		worldScreen = new WorldScreen(screenWidth, screenHeight);
	}

	@Override
	public void render (float delta) {
		elapsed += delta;

		if (!worldScreenFinishedLoading) {
			worldScreenFinishedLoading = worldScreen.assets.update();
		}

		boolean repeatText = elapsed > maxScrollTime;
		if (repeatText) {
			camera3d.translate(0.0f, elapsed * scrollSpeed, 0.0f);
			elapsed = 0;
		} else {
			camera3d.translate(0.0f, -delta * scrollSpeed, 0.0f);
		}
		camera3d.update(false);

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.act(delta);
		stage.draw();

		spriteBatch.setProjectionMatrix(camera3d.combined);
		spriteBatch.begin();
		bitmapFont.draw(spriteBatch, text, -camera3d.viewportWidth / 2f, -camera3d.viewportHeight, camera3d.viewportWidth,
			Align.center, true);
		spriteBatch.end();
	}

	@Override
	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
		screenWidth = width;
		screenHeight = height;

		// Define the perspective camera 10 unit wide with height depending on aspect ratio
		float camWidth = 10f;
		float camHeight = camWidth * (float)screenWidth / (float)screenHeight;
		camera3d.fieldOfView = 90.0f;
		camera3d.viewportWidth = camWidth;
		camera3d.viewportHeight = camHeight;
		camera3d.update();
		camera3d.translate(0.0f, -7.5f, 3.0f);
		camera3d.lookAt(0.0f, 0.0f, 0.0f);
		camera3d.update(true);
	}

	@Override
	public void pause () {
	}

	@Override
	public void resume () {
	}

	@Override
	public void hide () {
		dispose();
	}

	@Override
	public void dispose () {
		stage.dispose();
		skin.dispose();
		skyBox.getTexture().dispose();

		spriteBatch.dispose();
		bitmapFont.dispose();
	}

}

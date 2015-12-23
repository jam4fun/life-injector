
package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;

public class IntroScreen implements Screen {

	private SpriteBatch spriteBatch;
	private BitmapFont bitmapFont;
	private OrthographicCamera camera2d;
	private PerspectiveCamera camera3d;
	private int screenWidth;
	private int screenHeight;

	private final float scrollSpeed = 0.65f; // unit per second
	private final float maxScrollTime = 100; // in seconds

	private String text = "L I F E   I N J E C T O R\n\nby the Jam4Fun Team\n\n\nThankfully it has been a\nlong period of peace\nin the universe\nand life is thriving everywhere.\n\nHowever a huge fearsome comet,\nknown as The Big Mower,\nis destroying every life form\nalong its winding path\nacross the universe.\n\nYour mission, as the hero\nof this story, is to follow\nthe tail of the comet\nat the maximum speed,\navoiding obstacles and restoring\nas much life as you can\nbefore it's too late to do so.\n\nIt is a desperate run,\na necessary sacrifice...\n\nMay the force be with you!!!";

	float elapsed;
	WorldScreen worldScreen;
	boolean worldScreenFinishedLoading;

	public IntroScreen (int reqWidth, int reqHeight) {
		screenWidth = reqWidth;
		screenHeight = reqHeight;
		spriteBatch = new SpriteBatch();
		bitmapFont = new BitmapFont();
		bitmapFont.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		bitmapFont.setUseIntegerPositions(false);
		bitmapFont.getData().setScale(.035f);
		bitmapFont.setColor(Color.YELLOW);

		camera2d = new OrthographicCamera();
		camera3d = new PerspectiveCamera();

		Gdx.input.setInputProcessor(new InputAdapter() {
			@Override
			public boolean touchDown (int screenX, int screenY, int pointer, int button) {
				if (worldScreenFinishedLoading) {
					SpaceGame.game.setScreen(worldScreen);
					return true;
				}
				return false;
			}
		});
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
		}
		else {
			camera3d.translate(0.0f, -delta * scrollSpeed, 0.0f);
		}
		camera3d.update(false);

		GL20 gl = Gdx.graphics.getGL20();
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		spriteBatch.setProjectionMatrix(camera3d.combined);
		spriteBatch.begin();
		bitmapFont.draw(spriteBatch, text, -camera3d.viewportWidth / 2f, -camera3d.viewportHeight, camera3d.viewportWidth,
			Align.center, true);
		spriteBatch.end();
	}

	@Override
	public void resize (int width, int height) {
		screenWidth = width;
		screenHeight = height;

		// Define an ortho camera 10 unit wide with height depending on aspect ratio
		float camWidth = 10.0f;
		float camHeight = camWidth * (float)screenWidth / (float)screenHeight;
		camera2d.setToOrtho(true, camWidth, camHeight);
		camera2d.position.set(camWidth / 2.0f, camHeight / 2.0f, 0.0f);
		camera2d.update();

		// Define the perspective camera
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
		spriteBatch.dispose();
		bitmapFont.dispose();
	}

}

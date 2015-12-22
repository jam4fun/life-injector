
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

	private final float scrollSpeed = 1.0f; // unit per second

	private String text = "Episode IV\n\nA NEW HOPE\n\nIt is a period of civil war.\nRebel spaceships, striking\nfrom a hidden base, have\nwon their first victory\nagainst the evil Galactic\nEmpire.\n\nDuring the battle, Rebel\nspies managed to steal\nsecret plans to the Empire’s\nultimate weapon, the\nDEATH STAR, an armored\nspace station with enough\npower to destroy an entire\nplanet.\n\nPursued by the Empire's\nsinister agents, Princess\nLeia races home aboard her\nstarship, custodian of the\nstolen plans that can save\nher people and restore\nfreedom to the galaxy....";

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
				if (worldScreen != null && worldScreenFinishedLoading) {
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
		System.out.println(elapsed);
		if (elapsed > 0) {
			if (!worldScreenFinishedLoading) {
				worldScreenFinishedLoading = worldScreen.assets.update();
			} else if (elapsed > 25) {
				SpaceGame.game.setScreen(worldScreen);
				return;
			}
		}

		camera3d.translate(0.0f, -delta * scrollSpeed, 0.0f);
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
		camera3d.translate(0.0f, -7.0f, 3.0f);
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

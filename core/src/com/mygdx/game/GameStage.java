package com.mygdx.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameStage extends Stage {

	private final Camera cameraUI;

	private final FirstPersonCameraController camCtrl;

	private final SpriteBatch batch;
	private final ShapeRenderer shapeRenderer;
	private final Table rootTable;
	private final Vector3 tmp = new Vector3();
	private final Skin skin;

	public GameStage(Viewport viewport) {
		super(viewport);

		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();
		shapeRenderer.setAutoShapeType(true);

		cameraUI = new OrthographicCamera(viewport.getScreenWidth(), viewport.getScreenHeight());
		cameraUI.position.set(viewport.getScreenWidth() / 2, viewport.getScreenHeight() / 2, 0);
		cameraUI.update();

		addActor(rootTable = new Table());

		skin = new Skin(Gdx.files.internal("skins/uiskin.json"));
		rootTable.addActor(new Label("Drag mouse to look, press WASD keys to move", skin));


		InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(this);
		multiplexer.addProcessor(camCtrl = new FirstPersonCameraController(viewport.getCamera()));
		Gdx.input.setInputProcessor(multiplexer);
	}


	@Override
	public Vector2 screenToStageCoordinates(Vector2 screenCoords) {
		Viewport viewport = getViewport();
		tmp.set(screenCoords.x, screenCoords.y, 1);
		cameraUI.unproject(tmp, viewport.getScreenX(), viewport.getScreenY(),
				viewport.getScreenWidth(), viewport.getScreenHeight());
		screenCoords.set(tmp.x, tmp.y);
		return screenCoords;
	}


	public void resize(int width, int height) {
		Viewport viewport = getViewport();
		viewport.update(width, height, false);
		cameraUI.viewportWidth = viewport.getScreenWidth();
		cameraUI.viewportHeight = viewport.getScreenHeight();
		cameraUI.position.set(viewport.getScreenWidth() / 2, viewport.getScreenHeight() / 2, 0);
		cameraUI.update();
		batch.setProjectionMatrix(cameraUI.combined);
		shapeRenderer.setProjectionMatrix(cameraUI.combined);

		// Resize the root table that will auto-scale if needed
		rootTable.setSize(viewport.getScreenWidth(), viewport.getScreenHeight());
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		camCtrl.update(delta);
	}

	@Override
	public void dispose() {
		super.dispose();
		batch.dispose();
		shapeRenderer.dispose();
		skin.dispose();
	}

	@Override
	public void draw() {
		batch.begin();
		for (Actor actor : getActors()) {
			if (actor.isVisible()) {
				actor.draw(batch, 1);
			}
		}
		batch.end();

		shapeRenderer.begin();
		rootTable.drawDebug(shapeRenderer);
		shapeRenderer.end();
	}
}

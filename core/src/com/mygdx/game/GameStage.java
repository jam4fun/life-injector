package com.mygdx.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.game.utils.EndlessCatmullRomSpline;

public class GameStage extends Stage {

	private final Camera cameraUI;

	private final FirstPersonCameraController camCtrl;

	private final SpriteBatch batch;
	private final ShapeRenderer uiShapeRenderer;
	private final Table rootTable;
	private final Vector3 tmp = new Vector3();
	private final Skin skin;

	private final ShapeRenderer shapeRenderer;
   int k;
   Vector3[] points;
   EndlessCatmullRomSpline spline;
   float splineAdvanceDelay;

	public GameStage(Viewport viewport) {
		super(viewport);

		batch = new SpriteBatch();
		uiShapeRenderer = new ShapeRenderer();
		uiShapeRenderer.setAutoShapeType(true);

		shapeRenderer = new ShapeRenderer();
	   k = 100; //increase k for more fidelity to the spline
	   points = new Vector3[k];
	   for (int i = 0; i < k; i++)
	   	points[i] = new Vector3();
	   spline = new EndlessCatmullRomSpline(6, new Vector3());
	   splineAdvanceDelay = 0;
		
		
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
		uiShapeRenderer.setProjectionMatrix(cameraUI.combined);
		shapeRenderer.setProjectionMatrix(getViewport().getCamera().combined);

		// Resize the root table that will auto-scale if needed
		rootTable.setSize(viewport.getScreenWidth(), viewport.getScreenHeight());
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		camCtrl.update(delta);
		
		splineAdvanceDelay += delta;
		if (splineAdvanceDelay > 1) {
			splineAdvanceDelay -= 1;
			spline.advance();
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		batch.dispose();
		uiShapeRenderer.dispose();
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

		uiShapeRenderer.begin();
		rootTable.drawDebug(uiShapeRenderer);
		uiShapeRenderer.end();
		
		shapeRenderer.setProjectionMatrix(getViewport().getCamera().combined);
		shapeRenderer.begin(ShapeType.Line);
		int n = k - 1;
		float denom = n;
		for (int i = 0; i < n; ++i) {
			shapeRenderer.line(spline.valueAt(points[i], i / denom), spline.valueAt(points[i + 1], (i + 1) / denom));
		}
		shapeRenderer.end();

	}
}

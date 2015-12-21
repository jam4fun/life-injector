package com.mygdx.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
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
	private final Camera cameraWorld;

	private final FirstPersonCameraController camCtrl;

	private final SpriteBatch batch;
	private final ShapeRenderer uiShapeRenderer;
	private final Table rootTable;
	private final Vector3 tmpV1 = new Vector3();
	private final Vector3 tmpV2 = new Vector3();
	private final Skin skin;

	private final ShapeRenderer shapeRenderer;
	int k;
	Vector3[] points;
	EndlessCatmullRomSpline spline;
	float splineFrac;

	Vector3 out = new Vector3();
	int nextCPIdx;
	float speed = .7f;

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
		int len = 7;
		spline = new EndlessCatmullRomSpline(len, new Vector3());
		nextCPIdx = len / 2;
		spline.advance();
		splineFrac = spline.locate(spline.controlPoints[1]);

		cameraWorld = viewport.getCamera();
		cameraWorld.position.set(spline.controlPoints[0]);
		cameraWorld.direction.set(spline.controlPoints[1]).sub(spline.controlPoints[0]).nor();
		cameraWorld.up.set(Vector3.Y);
		cameraWorld.update();

		cameraUI = new OrthographicCamera(viewport.getScreenWidth(), viewport.getScreenHeight());
		cameraUI.position.set(viewport.getScreenWidth() / 2, viewport.getScreenHeight() / 2, 0);
		cameraUI.update();

		addActor(rootTable = new Table());

		skin = new Skin(Gdx.files.internal("skins/uiskin.json"));
		rootTable.addActor(new Label("Drag mouse to look, press WASD keys to move", skin));


		InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(this);
		multiplexer.addProcessor(camCtrl = new FirstPersonCameraController(cameraWorld));
		Gdx.input.setInputProcessor(multiplexer);
	}


	@Override
	public Vector2 screenToStageCoordinates(Vector2 screenCoords) {
		Viewport viewport = getViewport();
		tmpV1.set(screenCoords.x, screenCoords.y, 1);
		cameraUI.unproject(tmpV1, viewport.getScreenX(), viewport.getScreenY(),
				viewport.getScreenWidth(), viewport.getScreenHeight());
		screenCoords.set(tmpV1.x, tmpV1.y);
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

		// Resize the root table that will auto-scale if needed
		rootTable.setSize(viewport.getScreenWidth(), viewport.getScreenHeight());
	}

	// Alpha (smoothness) of linear interpolation for camera positioning/targeting
	private float splineCameraLerpAlpha = 0.2f;
	// Camera targeting/positioning variables
	private final Vector3 splineCamPos = new Vector3();
	private final Vector3 splineCamDir = new Vector3();

	private void advancePath () {
		// Advance and re-cache the spline
		spline.advance();
		float denom = k - 1;
		for (int i = 0; i < k; ++i) {
			spline.valueAt(points[i], i / denom);
		}
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
		camCtrl.update(delta);

		spline.derivativeAt(out, splineFrac);
		float offset = (delta * speed) / out.len();
		splineFrac += offset;


		// Calculate how close we are to the next advance
		spline.valueAt(splineCamPos, splineFrac);
//		System.out.println(Gdx.graphics.getFrameId() + " splineAdvanceDelay = " + splineFrac + " dst = " + splineCamPos.dst2(spline.controlPoints[nextCPIdx]));
		if (MathUtils.isZero(splineCamPos.dst2(spline.controlPoints[nextCPIdx]), .1f)) {
			spline.advance();
			splineFrac = spline.locate(spline.controlPoints[nextCPIdx-2]);
//			System.out.println(Gdx.graphics.getFrameId() + " Advance spline -> splineAdvanceDelay = " + splineFrac);
		}
		spline.valueAt(splineCamDir, splineFrac + offset);
		splineCamDir.sub(splineCamPos).nor();

		cameraWorld.position.lerp(splineCamPos, splineCameraLerpAlpha);
		cameraWorld.direction.lerp(splineCamDir, splineCameraLerpAlpha);
		// Not sure what camera up vector works best, but this looks ok for now...
		cameraWorld.up.set(Vector3.Y);
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

//		uiShapeRenderer.setProjectionMatrix(cameraUI.combined);
//		uiShapeRenderer.begin();
//		rootTable.drawDebug(uiShapeRenderer);
//		uiShapeRenderer.end();

		shapeRenderer.setProjectionMatrix(cameraWorld.combined);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.WHITE);
		int n = k - 1;
		float denom = n;
		for (int i = 0; i < n; ++i) {
			shapeRenderer.line(spline.valueAt(points[i], i / denom), spline.valueAt(points[i + 1], (i + 1) / denom));
		}
		// For debugging camera position/direction, can be removed later
		float s = 1e-3f;
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.box(splineCamPos.x - s / 2, splineCamPos.y - s / 2, splineCamPos.z + s / 2, s, s, s);
		shapeRenderer.setColor(Color.GREEN);
		shapeRenderer.line(splineCamPos.x, splineCamPos.y, splineCamPos.z,
				splineCamPos.x + splineCamDir.x, splineCamPos.y + splineCamDir.y, splineCamPos.z + splineCamDir.z);
		shapeRenderer.end();

	}
}

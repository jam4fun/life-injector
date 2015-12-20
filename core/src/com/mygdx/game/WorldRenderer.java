package com.mygdx.game;


import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;

public class WorldRenderer implements Disposable {

	private final Vector3 tmp = new Vector3();

	private final ModelBatch modelBatch;
	private final Environment environment;

	private final Viewport viewport;
	private final Camera camera;
	private final PhysicsEngine engine;

	public WorldRenderer(Viewport viewport, Camera camera, PhysicsEngine engine) {
		this.viewport = viewport;
		this.camera = camera;
		this.engine = engine;

		modelBatch = new ModelBatch();

		environment = new Environment();
		float ambVal = 0.3f;
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight,
				ambVal, ambVal, ambVal, 1));
		environment.add(new PointLight().set(Color.WHITE,new Vector3(),5000f));

	}

	private boolean isVisible(final Camera camera, final GameModel gameModel) {
		gameModel.modelInstance.transform.getTranslation(tmp);
		tmp.add(gameModel.center);
		return camera.frustum.sphereInFrustum(tmp, gameModel.boundingBoxRadius);
	}


	public void update() {
		viewport.apply();
		modelBatch.begin(camera);
		for (GameModel mdl : engine.getDynamicModels()) {
			if (isVisible(camera, mdl)) {
				modelBatch.render(mdl.modelInstance, environment);
			}
		}
		modelBatch.end();
	}

	@Override
	public void dispose() {
		modelBatch.dispose();
	}
}

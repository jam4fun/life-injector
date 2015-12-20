package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.glutils.MipMapGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class WorldScreen implements Screen {

	private final ModelLoader.ModelParameters modelParameters;
	private final TextureLoader.TextureParameter textureParameter;

	private final PhysicsEngine engine;
	private final WorldRenderer worldRenderer;
	private final PerspectiveCamera camera;
	private final Viewport viewport;
	private final GameStage stage;
	private final ShapeRenderer shapeRenderer;
	private final AssetManager assets;

	private GameModel skybox;
	private Model sphereModel;

	public WorldScreen(int reqWidth, int reqHeight) {
		Bullet.init();
		MipMapGenerator.setUseHardwareMipMap(true);
		modelParameters = new ModelLoader.ModelParameters();
		modelParameters.textureParameter.genMipMaps = true;
		modelParameters.textureParameter.minFilter = Texture.TextureFilter.MipMap;
		modelParameters.textureParameter.magFilter = Texture.TextureFilter.Linear;

		textureParameter = new TextureLoader.TextureParameter();
		textureParameter.genMipMaps = true;
		textureParameter.minFilter = Texture.TextureFilter.MipMap;
		textureParameter.magFilter = Texture.TextureFilter.Linear;


		assets = new AssetManager();
		shapeRenderer = new ShapeRenderer();

		camera = new PerspectiveCamera(67, reqWidth, reqHeight);
		viewport = new FitViewport(reqWidth, reqHeight, camera);


		engine = new PhysicsEngine();
		engine.setDebugMode(btIDebugDraw.DebugDrawModes.DBG_DrawWireframe);

		worldRenderer = new WorldRenderer(viewport, camera, engine);
		stage = new GameStage(viewport);

		camera.near = 1f;
		camera.far = 1e5f;
		camera.position.set(50, 3, -5);
		camera.up.set(Vector3.Y);
		camera.lookAt(Vector3.Zero);

		// Load assets
		assets.load("nasa_sun.png", Texture.class, textureParameter);
		assets.load("planet_Dank_1182.png", Texture.class, textureParameter);
		assets.load("planet_Quom_2449.png", Texture.class, textureParameter);
		assets.load("Planet_New_Aruba_5128.png", Texture.class, textureParameter);
		assets.load("planet_Muunilinst_1406.png", Texture.class, textureParameter);
		assets.load("skybox.g3db", Model.class, modelParameters);
		assets.finishLoading();

		// Planets
		Planet sun = new Planet("nasa_sun.png", assets.get("nasa_sun.png", Texture.class),
				20, 1, new Vector3(0, 0, 0), new Vector3(0, 0, 0), new Vector3(0, 0.05f, 0));
		sun.object.modelInstance.materials.first().set(new ColorAttribute(ColorAttribute.AmbientLight, Color.WHITE));

		Planet planet0 = new Planet("Planet_New_Aruba_5128.png", assets.get("Planet_New_Aruba_5128.png", Texture.class),
				0.5f, 1, new Vector3(80, 0, 5), new Vector3(0, 0, 1), new Vector3(0, 0.1f, 0));

		Planet planet1 = new Planet("planet_Muunilinst_1406.png", assets.get("planet_Muunilinst_1406.png", Texture.class),
				4, 1, new Vector3(100, 0, -20), new Vector3(0, 0, 1), new Vector3(0, 0.1f, 0));

		Planet planet2 = new Planet("planet_Dank_1182.png", assets.get("planet_Dank_1182.png", Texture.class),
				2, 1, new Vector3(120, 0, 10), new Vector3(0, 0, 1), new Vector3(0, 0.1f, 0));

		Planet planet3 = new Planet("planet_Quom_2449.png", assets.get("planet_Quom_2449.png", Texture.class),
				1, 1, new Vector3(150, 0, 0), new Vector3(0, 0, 1), new Vector3(0, 0.1f, 0));

		PlanetarySystem system = new PlanetarySystem("uncharted system");
		system.planets.add(sun);
		system.planets.add(planet0);
		system.planets.add(planet1);
		system.planets.add(planet2);
		system.planets.add(planet3);

		engine.addEntity(system);

		for (Planet planet : system.planets)
			engine.addEntity(planet.object);

		// Skybox
		assets.finishLoadingAsset("skybox.g3db");
		Model skyboxModel = assets.get("skybox.g3db");
		skyboxModel.materials.first().set(new ColorAttribute(ColorAttribute.AmbientLight, Color.LIGHT_GRAY));
		Vector3 skyboxScale = new Vector3(1, 1, 1).scl(1000);
		skybox = new GameModel(skyboxModel, "skybox",
				new Vector3(), new Vector3(), skyboxScale);
		engine.addEntity(skybox);
	}

	@Override
	public void show() {

	}

	@Override
	public void render(float delta) {
		stage.act(delta);
		engine.update(delta);

		// Move skybox with camera so we never reach the edge of it
		skybox.modelTransform.setTranslation(camera.position);

		Gdx.gl.glClearColor(0, 0, 0, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(Color.DARK_GRAY);
		shapeRenderer.rect(0, 0, viewport.getScreenWidth(), viewport.getScreenHeight());
		shapeRenderer.end();

		worldRenderer.update();
//		engine.debugDrawWorld(camera); // Bullet debug draw
		stage.draw();

	}

	@Override
	public void resize(int width, int height) {
		stage.resize(width, height);
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {

	}

	@Override
	public void dispose() {
		sphereModel.dispose();

		shapeRenderer.dispose();
		worldRenderer.dispose();
		engine.dispose();
		stage.dispose();
		assets.dispose();
	}
}

package com.mygdx.game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.LongMap;


public class PhysicsEngine extends Engine implements Disposable {

	public final static short NONE_FLAG = 0;
	public final static short ALL_FLAG = -1;
	public final static short PC_FLAG = 1 << 10;
	public final static short GROUND_FLAG = 1 << 8;
	public final static short OBJECT_FLAG = 1 << 9;

	// Bullet classes
	public final btDynamicsWorld dynamicsWorld;
	private final btDispatcher dispatcher;
	private final btConstraintSolver constraintSolver;
	private final btDbvtBroadphase broadphase;
	private final btCollisionConfiguration collisionConfig;
	private final DebugDrawer debugDrawer;

	private final LongMap<GameObject> objectsById = new LongMap<GameObject>();
	private Array<GameModel> dynamicModels = new Array<GameModel>();
	private boolean modelCacheDirty = true;

	public PhysicsEngine() {

		collisionConfig = new btDefaultCollisionConfiguration();
		dispatcher = new btCollisionDispatcher(collisionConfig);
		broadphase = new btDbvtBroadphase();
		constraintSolver = new btSequentialImpulseConstraintSolver();
		dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase,
				constraintSolver, collisionConfig);

		debugDrawer = new DebugDrawer();
		dynamicsWorld.setDebugDrawer(debugDrawer);
		debugDrawer.setDebugMode(btIDebugDraw.DebugDrawModes.DBG_DrawWireframe);

		dynamicsWorld.setGravity(Vector3.Zero);
	}


	public void update(float deltaTime) {
		// Update Bullet simulation
		dynamicsWorld.stepSimulation(deltaTime, 5, 1f / 60f);

		for (GameObject object : objectsById.values()) {
			object.update(deltaTime);
		}
	}

	public void debugDrawWorld(Camera camera) {
		debugDrawer.begin(camera);
		dynamicsWorld.debugDrawWorld();
		debugDrawer.end();
	}

	public void setDebugMode(int mode) {
		debugDrawer.setDebugMode(mode);
	}

	@Override
	public void addEntity(Entity entity) {
		super.addEntity(entity);
		if (entity instanceof GameModelBody) {
			GameModelBody gameObj = (GameModelBody) entity;
			gameObj.body.setUserPointer(entity.getId());
			dynamicsWorld.addRigidBody(gameObj.body, gameObj.belongsToFlag, gameObj.collidesWithFlag);
		}

		if (entity instanceof GameObject) {
			GameObject gameObj = (GameObject) entity;
			objectsById.put(entity.getId(), gameObj);
		}
		modelCacheDirty = true;
	}

	public Array<GameModel> getDynamicModels() {
		if (modelCacheDirty) {
			// Not sure if this project will use ModelCache?
			modelCacheDirty = false;
			dynamicModels.clear();
			for (GameObject obj : objectsById.values()) {
				if (obj instanceof GameModel) {
					GameModel model = (GameModel) obj;
					dynamicModels.add(model);
				}
			}
		}

		return dynamicModels;
	}


	@Override
	public void dispose() {
		collisionConfig.dispose();
		dispatcher.dispose();
		dynamicsWorld.dispose();
		broadphase.dispose();
		constraintSolver.dispose();
		debugDrawer.dispose();

		dynamicModels.clear();
		for (GameObject obj : objectsById.values())
			obj.dispose();
	}
}

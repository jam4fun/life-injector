package com.mygdx.game;


import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;

import java.nio.FloatBuffer;

public class Planet implements Disposable, Pool.Poolable {

	public final Model model;
	private final btCollisionShape shape;
	public final float mass;
	public final GameModelBody object;

	private final Vector3 tmpV1 = new Vector3();
	private final Vector3 tmpV2 = new Vector3();

	/**
	 * Creates a sphere shaped planet model and collision body.
	 */
	public Planet(String name, Texture texture, float radius, float density,
				  Vector3 position, Vector3 linearVelocity, Vector3 angularVelocity) {
		Material material = new Material(
				TextureAttribute.createDiffuse(texture),
				ColorAttribute.createSpecular(1, 1, 1, 1),
				FloatAttribute.createShininess(1f));
		long attributes = VertexAttributes.Usage.Position
				| VertexAttributes.Usage.Normal
				| VertexAttributes.Usage.TextureCoordinates;
		ModelBuilder modelBuilder = new ModelBuilder();
		Vector3 modelScale = new Vector3(1, 1, 1);
		int subDivs = 24;
		model = modelBuilder.createSphere(modelScale.x, modelScale.y, modelScale.z,
				subDivs, subDivs, material, attributes);
		mass = 4f / 3f * MathUtils.PI * radius * radius * radius * density;
		Vector3 dim = new Vector3(1, 1, 1).scl(radius * 2f);
		shape = new btSphereShape(dim.x * 0.5f);
		object = new GameModelBody(model, name,
				position, new Vector3(), dim, shape, 1,
				PhysicsEngine.GROUND_FLAG, PhysicsEngine.ALL_FLAG,
				false, true);
		object.body.setLinearVelocity(linearVelocity);
		object.body.setAngularVelocity(angularVelocity);
	}

	/**
	 * Creates a convex hull collision shape from a model
	 */
	public Planet(String name, Model model, float density, float scale,
				  Vector3 position, Vector3 linearVelocity, Vector3 angularVelocity) {
		this.model = model;
		shape = createConvexHull(model, new Vector3(), new Vector3(), new Vector3(1, 1, 1).scl(scale));
		BoundingBox bb = new BoundingBox();
		Vector3 dimension = model.calculateBoundingBox(bb).getDimensions(tmpV1).scl(scale);
		mass = dimension.x * dimension.y * dimension.z * density;
		object = new GameModelBody(model, name,
				position, new Vector3(), new Vector3(1, 1, 1).scl(scale), shape, 1,
				PhysicsEngine.GROUND_FLAG, PhysicsEngine.ALL_FLAG,
				false, true);
		object.body.setLinearVelocity(linearVelocity);
		object.body.setAngularVelocity(angularVelocity);
	}

	public Vector3 getPosition(Vector3 out) {
		return object.body.getWorldTransform().getTranslation(out);
	}

	public Vector3 forceFrom(Planet other) {
		float G = 6.674E-11f; // N*m^2/kg^2
		Vector3 dr = other.getPosition(tmpV1).sub(this.getPosition(tmpV2));
		float l = dr.len();
		return dr.scl(G * this.mass * other.mass / (l * l * l));
	}

	@Override
	public void dispose() {
		model.dispose();
		object.dispose();
		shape.dispose();
	}

	@Override
	public void reset() {
		object.motionState.transform.idt();
		object.modelTransform.idt();
		object.body.setWorldTransform(object.modelTransform);
		object.body.setAngularVelocity(Vector3.Zero);
		object.body.setLinearVelocity(Vector3.Zero);
	}

	public static btCollisionShape createConvexHull(Model model, Vector3 position, Vector3 rotation, Vector3 scale) {
		// We need a model instance with the correct scale
		ModelInstance modelInstance = new ModelInstance(model);
		GameModel.applyTransform(position, rotation, scale, modelInstance);
		// Copy the vertices to a work buffer, where we apply the model global transform to them
		Matrix4 transform = new Matrix4(modelInstance.nodes.get(0).globalTransform);
		Mesh mesh = modelInstance.model.meshes.get(0);
		FloatBuffer workBuffer = BufferUtils.newFloatBuffer(mesh.getVerticesBuffer().capacity());
		BufferUtils.copy(mesh.getVerticesBuffer(), workBuffer, mesh.getNumVertices() * mesh.getVertexSize() / 4);
		BufferUtils.transform(workBuffer, 3, mesh.getVertexSize(), mesh.getNumVertices(), transform);
		// First create a shape using all the vertices, then use the built in tool to reduce
		// the number of vertices to a manageable amount.
		btConvexShape convexShape = new btConvexHullShape(workBuffer, mesh.getNumVertices(), mesh.getVertexSize());
		btShapeHull hull = new btShapeHull(convexShape);
		hull.buildHull(convexShape.getMargin());
		btCollisionShape shape = new btConvexHullShape(hull);
		convexShape.dispose();
		hull.dispose();
		return shape;
	}
}

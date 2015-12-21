package com.mygdx.game;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;

public class Planet {

	public final Model model;
	public final GameModelBody object;

	public final float mass;

	public Planet(String name, Texture texture, float radius, float density,
				  Vector3 position, Vector3 linearVelocity, Vector3 angularVelocity) {

		this.position.set(position);
		this.velocity.set(linearVelocity);

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

		mass = 4f / 3f * MathUtils.PI * radius * radius * radius * density*500000000;
		Vector3 dim = new Vector3(1, 1, 1).scl(radius * 2f);

		btSphereShape sphereShape = new btSphereShape(dim.x * 0.5f);
		object = new GameModelBody(model, name,
				position, new Vector3(), dim,
				sphereShape, 1,
				PhysicsEngine.GROUND_FLAG, PhysicsEngine.ALL_FLAG,
				false, true);

		object.body.setLinearVelocity(linearVelocity);
		object.body.setAngularVelocity(angularVelocity);
	}

	public Vector3 forceFrom(Planet other) {
		float G = 6.674E-11f; // N*m^2/kg^2
		float m1 = this.mass;
		float m2 = other.mass;
		Vector3 dr = tmp.set(other.position).sub(this.position);
		float l = dr.len();
		return dr.scl(G * m1 * m2 / (l * l * l));
	}

	private Vector3 acceleration = new Vector3();
	private Vector3 velocity = new Vector3();
	private Vector3 tmp = new Vector3();
	private Vector3 position = new Vector3();

	public void applyForce(Vector3 f, float dt) {
		acceleration.set(f).scl(1 / mass);

		velocity.set(object.body.getLinearVelocity());
		velocity.add(tmp.set(acceleration).scl(dt));
		object.body.setLinearVelocity(velocity);
	}
}

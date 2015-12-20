package com.mygdx.game;

public abstract class GameObject extends Entity {
	public final String name;

	public GameObject(String name) {
		this.name = name;
	}

	public abstract void update(float deltaTime);

	public abstract void dispose();


}

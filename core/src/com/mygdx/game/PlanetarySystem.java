package com.mygdx.game;

import com.badlogic.gdx.utils.Array;

public class PlanetarySystem extends GameObject {

	public Array<Planet> planets = new Array<Planet>();

	public PlanetarySystem(String name) {
		super(name);
	}

	@Override
	public void update(float deltaTime) {
		for (int i = 0; i < planets.size; i++) {
			Planet a = planets.get(i);
			for (int j = 0; j < planets.size; j++) {
				if (i == j) {
					continue;
				}
				Planet b = planets.get(j);
				a.object.body.applyCentralForce(a.forceFrom(b));
			}
		}
	}

	@Override
	public void dispose() {
		planets.clear();
	}
}

package com.mygdx.game.utils;

import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class EndlessCatmullRomSpline extends CatmullRomSpline<Vector3> {
	
	public EndlessCatmullRomSpline (int n, Vector3 start) {
		super(createControlPoints(n + 2, start), false);
	}

	public void advance() {
		Vector3 firstPoint = controlPoints[0];
		int last = controlPoints.length - 1;
		for(int i = 0; i < last; i++) {
			controlPoints[i] = controlPoints[i + 1];
		}
		controlPoints[last] = firstPoint;
		generateControlPoint(controlPoints[last - 2], controlPoints[last - 1]);
		controlPoints[last].set(controlPoints[last - 1]);
	}

	private static Vector3[] createControlPoints(int n, Vector3 start) {
		Vector3[] ctrlPoints = new Vector3[n];
		ctrlPoints[0] = new Vector3(start);
		ctrlPoints[1] = new Vector3(start);
		for(int i = 2; i < n; i++) {
			ctrlPoints[i] = generateControlPoint(ctrlPoints[i-1], new Vector3());
		}
		return ctrlPoints;
	}

	private static float randomSigned(float s, float e) {
		float f = MathUtils.random(s, e);
		return MathUtils.randomBoolean() ? f : -f;
		
	}

	private static Vector3 generateControlPoint(Vector3 from, Vector3 to) {
		float x = randomSigned(1f, 6f);
		float y = randomSigned(1f, 6f);
		float z = randomSigned(1f, 6f);
		return to.set(from).add(x, y, z);
	}
}

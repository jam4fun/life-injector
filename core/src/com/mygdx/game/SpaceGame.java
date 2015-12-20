package com.mygdx.game;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class SpaceGame extends Game {

	public static int reqWidth = 1280;
	public static int reqHeight = 720;

	@Override
	public void create() {
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		Screen currentScreen = new WorldScreen(reqWidth, reqHeight);
		setScreen(currentScreen);
	}
}

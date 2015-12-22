package com.mygdx.game;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

public class SpaceGame extends Game {

	public static int reqWidth = 1280;
	public static int reqHeight = 720;

	public static SpaceGame game;
	
	@Override
	public void create() {
		game = this;
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		Screen currentScreen = new IntroScreen(reqWidth, reqHeight);
		setScreen(currentScreen);
	}
}

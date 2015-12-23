package com.mygdx.game;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;

public class PlanetPool extends Pool<Planet> implements Disposable{


	private final ModelLoader.ModelParameters modelParameters;
	private final TextureLoader.TextureParameter textureParameter;
	private final AssetManager assets = new AssetManager();

	public PlanetPool() {
		modelParameters = new ModelLoader.ModelParameters();
		modelParameters.textureParameter.genMipMaps = true;
		modelParameters.textureParameter.minFilter = Texture.TextureFilter.MipMap;
		modelParameters.textureParameter.magFilter = Texture.TextureFilter.Linear;

		textureParameter = new TextureLoader.TextureParameter();
		textureParameter.genMipMaps = true;
		textureParameter.minFilter = Texture.TextureFilter.MipMap;
		textureParameter.magFilter = Texture.TextureFilter.Linear;
	}



	@Override
	protected Planet newObject() {
		return null;
	}


	@Override
	public void dispose() {
		assets.dispose();
	}
}

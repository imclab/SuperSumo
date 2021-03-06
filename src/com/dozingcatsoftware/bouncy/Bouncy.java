
package com.dozingcatsoftware.bouncy;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.utils.TimeUtils;
import com.dozingcatsoftware.bouncy.elements.FieldElement;

public class Bouncy extends InputAdapter implements ApplicationListener {
	OrthographicCamera cam;
	static GLFieldRenderer renderer;
	Field field;
	int level = 1;
	WindowedMean physicsMean = new WindowedMean(10);
	WindowedMean renderMean = new WindowedMean(10);
	long startTime = TimeUtils.nanoTime();
	private String scoreText = "Score : ";
	private String score;
	private float textWidth;
	private float textHeight;
	private BitmapFont font;
	private SpriteBatch spriteBatch;

	@Override
	public void create () {
		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		renderer = new GLFieldRenderer();
		field = new Field(cam);
		field.resetForLevel(level);
		Gdx.input.setInputProcessor(this);
		// Gdx.graphics.setDisplayMode(1280, 720, true);
		spriteBatch = new SpriteBatch();
		font = new BitmapFont();
		textWidth = font.getBounds(scoreText).width;
		textHeight = font.getBounds(scoreText).height;

	}

	@Override
	public void resume () {

	}

	@Override
	public void render () {
		GLCommon gl = Gdx.gl;

		long startPhysics = TimeUtils.nanoTime();
		field.tick((long)(Gdx.graphics.getDeltaTime() * 3000), 4);
		physicsMean.addValue((TimeUtils.nanoTime() - startPhysics) / 1000000000.0f);

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		cam.viewportWidth = field.getWidth();
		cam.viewportHeight = field.getHeight();
		cam.position.set(field.getWidth() / 2, field.getHeight() / 2, 0);
		cam.update();
		renderer.setProjectionMatrix(cam.combined);

		long startRender = TimeUtils.nanoTime();

		renderer.begin();
		int len = field.getFieldElements().size();
		for (int i = 0; i < len; i++) {
			FieldElement element = field.getFieldElements().get(i);
			element.draw(renderer);
		}
		renderer.end();
		field.flock.removeBalls();

		renderer.begin();
		field.flock.run(renderer);
		field.player.update(Gdx.graphics.getDeltaTime());
		field.player.render();

		renderer.end();

		renderMean.addValue((TimeUtils.nanoTime() - startRender) / 1000000000.0f);
		score = String.valueOf(field.player.score);
		spriteBatch.begin();
		font.draw(spriteBatch, scoreText, 0, Gdx.graphics.getHeight());
		font.draw(spriteBatch, score, 0 + font.getBounds(scoreText).width + 5, Gdx.graphics.getHeight());
		spriteBatch.end();

		if (TimeUtils.nanoTime() - startTime > 1000000000) {
			Gdx.app.log("Bouncy", "fps: " + Gdx.graphics.getFramesPerSecond() + ", physics: " + physicsMean.getMean() * 1000
				+ ", rendering: " + renderMean.getMean() * 1000);
			startTime = TimeUtils.nanoTime();
		}

	}

	@Override
	public void resize (int width, int height) {

	}

	@Override
	public void pause () {

	}

	@Override
	public void dispose () {

	}

}

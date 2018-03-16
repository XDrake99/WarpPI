package org.warp.picalculator.gui.screens;

import org.warp.picalculator.device.KeyboardEventListener;
import org.warp.picalculator.gui.DisplayManager;
import org.warp.picalculator.gui.GraphicalInterface;

public abstract class Screen implements KeyboardEventListener, GraphicalInterface {
	public DisplayManager d;
	public boolean created = false;
	public boolean initialized = false;
	public boolean canBeInHistory = false;

	public Screen() {}

	public void initialize() throws InterruptedException {
		if (!initialized) {
			initialized = true;
			initialized();
		}
	}

	public void create() throws InterruptedException {
		if (!created) {
			created = true;
			created();
		}
	}

	public abstract void created() throws InterruptedException;

	public abstract void initialized() throws InterruptedException;

	public abstract void render();

	public void renderTopmost() {

	}

	public abstract void beforeRender(float dt);

	public abstract boolean mustBeRefreshed();
}

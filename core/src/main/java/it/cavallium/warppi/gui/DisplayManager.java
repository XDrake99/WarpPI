package it.cavallium.warppi.gui;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.Platform.ConsoleUtils;
import it.cavallium.warppi.Platform.Semaphore;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.device.Keyboard;
import it.cavallium.warppi.flow.Observable;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.gui.graphicengine.RenderingLoop;
import it.cavallium.warppi.gui.graphicengine.Skin;
import it.cavallium.warppi.gui.graphicengine.nogui.NoGuiEngine;
import it.cavallium.warppi.gui.screens.Screen;
import it.cavallium.warppi.util.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class DisplayManager implements RenderingLoop {
	private static final int tickDuration = 50;

	private float brightness;

	public final GraphicEngine engine;
	public final HardwareDisplay monitor;
	public final boolean supportsPauses;
	public Renderer renderer;

	public Skin guiSkin;
	public BinaryFont[] fonts;

	public String error;
	public String[] errorStackTrace;
	public final int[] glyphsHeight;

	private Screen screen;
	private final HUD hud;
	private final String initialTitle;
	private Screen initialScreen;
	public Semaphore screenChange;
	public String displayDebugString;
	public ObjectArrayList<GUIErrorMessage> errorMessages;
	/**
	 * Set to true when an event is fired
	 */
	public boolean forceRefresh;

	public DisplayManager(HardwareDisplay monitor, HUD hud, Screen screen, String title) {
		this.monitor = monitor;
		this.hud = hud;
		this.initialTitle = title;
		this.initialScreen = screen;
		
		screenChange = Engine.getPlatform().newSemaphore();
		engine = chooseGraphicEngine();
		supportsPauses = engine.doesRefreshPauses();

		glyphsHeight = new int[] { 9, 6, 12, 9 };
		displayDebugString = "";
		errorMessages = new ObjectArrayList<>();
	}

	public void initialize() {
		monitor.initialize();

		try {
			hud.d = this;
			hud.create();
			if (!hud.initialized) {
				hud.initialize();
			}
		} catch (final Exception e) {
			e.printStackTrace();
			Engine.getPlatform().exit(0);
		}

		try {
			engine.create();
			renderer = engine.getRenderer();
			engine.setTitle(initialTitle);
			loop();
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		monitor.shutdown();
	}

	/*
	 * private void load_skin() {
	 * try {
	 * skin_tex = glGenTextures();
	 * glBindTexture(GL_TEXTURE_2D, skin_tex);
	 * glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
	 * 
	 * InputStream in = new FileInputStream("skin.png");
	 * PNGDecoder decoder = new PNGDecoder(in);
	 * 
	 * System.out.println("width="+decoder.getWidth());
	 * System.out.println("height="+decoder.getHeight());
	 * 
	 * ByteBuffer buf =
	 * ByteBuffer.allocateDirect(4*decoder.getWidth()*decoder.getHeight());
	 * decoder.decode(buf, decoder.getWidth()*4, Format.RGBA);
	 * buf.flip();
	 * 
	 * skin = buf;
	 * skin_w = decoder.getWidth();
	 * skin_h = decoder.getHeight();
	 * glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, skin_w,
	 * skin_h, 0, GL_RGBA, GL_UNSIGNED_BYTE, skin);
	 * } catch (IOException ex) {
	 * ex.printStackTrace();
	 * }
	 * }
	 */

	private GraphicEngine chooseGraphicEngine() {
		GraphicEngine d;
		d = Utils.getOrDefault(Engine.getPlatform().getEnginesList(), "framebuffer engine", null);
		if (d != null && d.isSupported()) {
			Engine.getPlatform().getConsoleUtils().out().println(1, "Using FB Graphic Engine");
			return d;
		}
		d = Utils.getOrDefault(Engine.getPlatform().getEnginesList(), "GPU engine", null);
		if (d != null && d.isSupported()) {
			Engine.getPlatform().getConsoleUtils().out().println(1, "Using GPU Graphic Engine");
			return d;
		}
		d = Utils.getOrDefault(Engine.getPlatform().getEnginesList(), "CPU engine", null);
		if (d != null && d.isSupported()) {
			Engine.getPlatform().getConsoleUtils().out().println(1, "Using CPU Graphic Engine");
			return d;
		}
		d = Utils.getOrDefault(Engine.getPlatform().getEnginesList(), "headless 24 bit engine", null);
		if (d != null && d.isSupported()) {
			System.err.println("Using Headless 24 bit Engine! This is a problem! No other graphic engines are available.");
			return d;
		}
		d = Utils.getOrDefault(Engine.getPlatform().getEnginesList(), "headless 256 colors engine", null);
		if (d != null && d.isSupported()) {
			System.err.println("Using Headless 256 Engine! This is a problem! No other graphic engines are available.");
			return d;
		}
		d = Utils.getOrDefault(Engine.getPlatform().getEnginesList(), "headless 8 colors engine", null);
		if (d != null && d.isSupported()) {
			System.err.println("Using Headless basic Engine! This is a problem! No other graphic engines are available.");
			return d;
		}
		d = Utils.getOrDefault(Engine.getPlatform().getEnginesList(), "HTML5 engine", null);
		if (d != null && d.isSupported()) {
			Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_NODEBUG, "Using Html Graphic Engine");
			return d;
		}
		d = new NoGuiEngine();
		if (d != null && d.isSupported()) {
			Engine.getPlatform().getConsoleUtils().out().println(1, "Using NoGui Graphic Engine");
			return d;
		}
		throw new UnsupportedOperationException("No graphic engines available.");
	}

	public void setScreen(Screen screen) {
		if (screen.initialized == false) {
			if (screen.canBeInHistory) {
				if (this.currentSession > 0) {
					final int sl = this.sessions.length + 5; //TODO: I don't know why if i don't add +5 or more some items disappear
					this.sessions = Arrays.copyOfRange(this.sessions, this.currentSession, sl);
				}
				this.currentSession = 0;
				for (int i = this.sessions.length - 1; i >= 1; i--) {
					this.sessions[i] = this.sessions[i - 1];
				}
				this.sessions[0] = screen;
			} else {
				this.currentSession = -1;
			}
		}
		screen.d = this;
		try {
			screen.create();
			this.screen = screen;
			screenChange.release();
			if (screen.initialized == false) {
				screen.initialize();
			}
		} catch (final Exception e) {
			e.printStackTrace();
			Engine.getPlatform().exit(0);
		}
	}

	public void replaceScreen(Screen screen) {
		if (screen.initialized == false) {
			if (screen.canBeInHistory) {
				this.sessions[this.currentSession] = screen;
			} else {
				this.currentSession = -1;
				for (int i = 0; i < this.sessions.length - 2; i++) {
					this.sessions[i] = this.sessions[i + 1];
				}
			}
		}
		screen.d = this;
		try {
			screen.create();
			this.screen = screen;
			screenChange.release();
			if (screen.initialized == false) {
				screen.initialize();
			}
		} catch (final Exception e) {
			e.printStackTrace();
			Engine.getPlatform().exit(0);
		}
	}

	public boolean canGoBack() {
		if (this.currentSession == -1) {
			return this.sessions[0] != null;
		}
		if (this.screen != this.sessions[this.currentSession]) {

		} else if (this.currentSession + 1 < this.sessions.length) {
			if (this.sessions[this.currentSession + 1] != null) {

			} else {
				return false;
			}
		} else {
			return false;
		}
		if (this.sessions[this.currentSession] != null) {
			return true;
		}
		return false;
	}

	public void goBack() {
		if (canGoBack()) {
			if (this.currentSession >= 0 && this.screen != this.sessions[this.currentSession]) {} else {
				this.currentSession += 1;
			}
			this.screen = this.sessions[this.currentSession];
			screenChange.release();
		}
	}

	public boolean canGoForward() {
		if (this.currentSession <= 0) { // -1 e 0
			return false;
		}
		if (this.screen != this.sessions[this.currentSession]) {

		} else if (this.currentSession > 0) {
			if (this.sessions[this.currentSession - 1] != null) {

			} else {
				return false;
			}
		} else {
			return false;
		}
		if (this.sessions[this.currentSession] != null) {
			return true;
		}
		return false;
	}

	public void goForward() {
		if (canGoForward()) {
			if (this.screen != this.sessions[this.currentSession]) {

			} else {
				this.currentSession -= 1;
			}
			this.screen = this.sessions[this.currentSession];
			screenChange.release();
		}
	}

	public Screen getScreen() {
		return screen;
	}

	public HUD getHUD() {
		return hud;
	}

	private void load_skin() throws IOException {
		guiSkin = engine.loadSkin("skin.png");
	}

	private void load_fonts() throws IOException {
		fonts = new BinaryFont[7];
		fonts[0] = engine.loadFont("smal");
		fonts[1] = engine.loadFont("smallest");
		fonts[2] = engine.loadFont("norm");
		fonts[3] = engine.loadFont("smal");
		//4
		//fonts[5] = engine.loadFont("square");
	}

	private void draw_init() {
		if (engine.supportsFontRegistering()) {
			final List<BinaryFont> fontsIterator = engine.getRegisteredFonts();
			for (final BinaryFont f : fontsIterator) {
				if (!f.isInitialized()) {
					f.initialize(engine);
				}
			}
		}
		renderer.glClear(engine.getWidth(), engine.getHeight());
	}

	private void draw_world() {
		renderer.glColor3i(255, 255, 255);

		if (error != null) {
			final BinaryFont fnt = Utils.getFont(false, false);
			if (fnt != null && fnt != engine.getRenderer().getCurrentFont()) {
				fnt.use(engine);
			}
			renderer.glColor3i(129, 28, 22);
			renderer.glDrawStringRight(StaticVars.screenSize[0] - 2, StaticVars.screenSize[1] - (fnt.getCharacterHeight() + 2), Engine.getPlatform().getSettings().getCalculatorNameUppercase() + " CALCULATOR");
			renderer.glColor3i(149, 32, 26);
			renderer.glDrawStringCenter((StaticVars.screenSize[0] / 2), 22, error);
			renderer.glColor3i(164, 34, 28);
			int i = 22;
			for (final String stackPart : errorStackTrace) {
				renderer.glDrawStringLeft(2, 22 + i, stackPart);
				i += 11;
			}
			if (fonts[0] != null && fonts[0] != engine.getRenderer().getCurrentFont()) {
				fonts[0].use(engine);
			}
			renderer.glColor3i(129, 28, 22);
			renderer.glDrawStringCenter((StaticVars.screenSize[0] / 2), 11, "UNEXPECTED EXCEPTION");
		} else {
			if (fonts[0] != null && fonts[0] != engine.getRenderer().getCurrentFont()) {
				fonts[0].use(engine);
			}
			hud.renderBackground();
			screen.render();
			hud.render();
			hud.renderTopmostBackground();
			screen.renderTopmost();
			hud.renderTopmost();
		}
	}

	private void draw() {
		draw_init();
		draw_world();
	}

	private long precTime = -1;

	@Override
	public void refresh() {
		if (supportsPauses == false || (Keyboard.popRefreshRequest() || forceRefresh || screen.mustBeRefreshed())) {
			forceRefresh = false;
			draw();
		}

	}

	public void loop() {
		try {
			load_skin();
			load_fonts();

			try {
				if (initialScreen != null) {
					setScreen(initialScreen);
					initialScreen = null;
				}
				screen.initialize();
			} catch (final Exception e) {
				e.printStackTrace();
				Engine.getPlatform().exit(0);
			}

			Observable<Long> workTimer = Observable.interval(tickDuration);

			Observable<Integer[]> onResizeObservable = engine.onResize();
			Observable<Pair<Long, Integer[]>> refreshObservable;
			if (onResizeObservable == null) {
				refreshObservable = workTimer.map((l) -> Pair.of(l, null));
			} else {
				refreshObservable = Observable.combineChanged(workTimer, engine.onResize());
			}

			refreshObservable.subscribe((pair) -> {
				double dt = 0;
				final long newtime = System.nanoTime();
				if (precTime == -1) {
					dt = tickDuration;
				} else {
					dt = (newtime - precTime) / 1000d / 1000d;
				}
				precTime = newtime;

				if (pair.getRight() != null) {
					Integer[] windowSize = pair.getRight();
					StaticVars.screenSize[0] = windowSize[0];
					StaticVars.screenSize[1] = windowSize[1];
				}

				screen.beforeRender((float) (dt / 1000d));
			});

			engine.start(getDrawable());
		} catch (final Exception ex) {
			ex.printStackTrace();
		} finally {}
	}

	public void changeBrightness(float change) {
		setBrightness(brightness + change);
	}

	public void setBrightness(float newval) {
		if (newval >= 0 && newval <= 1) {
			brightness = newval;
			monitor.setBrightness(brightness);
		}
	}

	public void cycleBrightness(boolean reverse) {
		final float step = reverse ? -0.1f : 0.1f;
		if (brightness + step > 1f) {
			setBrightness(0f);
		} else if (brightness + step <= 0f) {
			setBrightness(1.0f);
		} else {
			changeBrightness(step);
		}
	}

	public float getBrightness() {
		return brightness;
	}

	public int currentSession = 0;
	public Screen[] sessions = new Screen[5];

	@Deprecated
	public void colore(float f1, float f2, float f3, float f4) {
		renderer.glColor4f(f1, f2, f3, f4);
	}

	public RenderingLoop getDrawable() {
		return this;
	}

	@Deprecated
	public void drawSkinPart(int x, int y, int uvX, int uvY, int uvX2, int uvY2) {
		renderer.glFillRect(x, y, uvX2 - uvX, uvY2 - uvY, uvX, uvY, uvX2 - uvX, uvY2 - uvY);
	}

	public void waitForExit() {
		engine.waitForExit();
	}
}
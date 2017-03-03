package org.warp.picalculator.gui.expression;

import org.warp.picalculator.gui.graphicengine.GraphicEngine;
import org.warp.picalculator.gui.graphicengine.Renderer;
import org.warp.picalculator.math.parser.features.FeatureChar;
import org.warp.picalculator.math.parser.features.interfaces.Feature;

public class BlockChar extends Block {

	private final char ch;
	
	public BlockChar(char ch) {
		this.ch = ch;
		recomputeDimensions();
	}

	@Override
	public void draw(GraphicEngine ge, Renderer r, int x, int y, Caret caret) {
		BlockContainer.getDefaultFont(small).use(ge);
		r.glColor(BlockContainer.getDefaultColor());
		r.glDrawCharLeft(x, y, ch);
	}

	@Override
	public void recomputeDimensions() {
		width = BlockContainer.getDefaultCharWidth(small);
		height = BlockContainer.getDefaultCharHeight(small);
		line = height/2;
	}

	@Override
	public void setSmall(boolean small) {
		this.small = small;
		recomputeDimensions();
	}
	
	public char getChar() {
		return ch;
	}

}

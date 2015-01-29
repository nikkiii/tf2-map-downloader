package org.nikkii.mapdownloader.util.ui;

import javax.swing.JTextField;
import javax.swing.text.Document;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * A class which adds a textfield with a placeholder value.
 *
 * @see {@link http://stackoverflow.com/a/16229082}
 */
public class PlaceholderTextField extends JTextField {

	private String placeholder;

	public PlaceholderTextField() {
	}

	public PlaceholderTextField(Document pDoc, String pText, int pColumns) {
		super(pDoc, pText, pColumns);
	}

	public PlaceholderTextField(int pColumns) {
		super(pColumns);
	}

	public PlaceholderTextField(String pText) {
		super(pText);
	}

	public PlaceholderTextField(String pText, int pColumns) {
		super(pText, pColumns);
	}

	public String getPlaceholder() {
		return placeholder;
	}

	@Override
	protected void paintComponent(Graphics pG) {
		super.paintComponent(pG);

		if (placeholder.length() == 0 || getText().length() > 0) {
			return;
		}

		final Graphics2D g = (Graphics2D) pG;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(getDisabledTextColor());
		g.drawString(placeholder, getInsets().left, pG.getFontMetrics().getMaxAscent() + getInsets().top);
	}

	public void setPlaceholder(String s) {
		placeholder = s;
	}
}
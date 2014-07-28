package de.onyxbits.raccoon.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JEditorPane;

/**
 * A non-opaque, non-editable text pane that is able to display anti-aliased
 * hypertextcontent.
 * 
 * @author patrick
 * 
 */
public class HypertextPane extends JEditorPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HypertextPane(String txt) {
		super("text/html", txt);
		setEditable(false);
		setOpaque(false);
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D graphics2d = (Graphics2D) g;
		graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		super.paintComponent(g);
	}

}

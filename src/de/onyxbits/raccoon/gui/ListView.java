package de.onyxbits.raccoon.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Scrollable;

/**
 * A simple container that will scroll more gracefully when plugged into a
 * scrollpane. It also automatically alternates the background colors of added
 * panels (if those panels contain other panels, make sure that the children are
 * not opaque).
 * 
 * @author patrick
 * 
 */
class ListView extends JPanel implements Scrollable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean zebra;
	private GridBagConstraints gbc;

	public ListView() {
		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(0, 0, 10, 0);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx=GridBagConstraints.REMAINDER;
	}

	public void add(JComponent comp) {
		comp.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2, false));
		comp.getInsets().bottom = 10;
		comp.getInsets().top = 10;
		comp.getInsets().right = 10;
		comp.getInsets().left = 10;
		if (zebra) {
			comp.setBackground(Color.GRAY);
		}
		else {
			comp.setBackground(Color.LIGHT_GRAY);
		}
		zebra = !zebra;
		gbc.gridy++;
		super.add(comp, gbc);
	}

	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		Component c = getComponent(0);
		if (c != null) {
			return c.getHeight() / 2;
		}
		else {
			return 1;
		}
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	public boolean getScrollableTracksViewportWidth() {
		// FIXME: Setting this to true is desirable to force the entries to stretch
		// to full width. Unfortunately this also disables the horizontal scroller
		// and the layout breaks if an entry is too wide to fit into the scrollpane.
		// So for the time being let's stick with arguably less pretty instead of
		// potentially broken.
		return false;
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return getScrollableBlockIncrement(visibleRect, orientation, direction);
	}

}

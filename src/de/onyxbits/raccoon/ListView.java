package de.onyxbits.raccoon;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.BoxLayout;
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

	public void add(JComponent comp) {
		if (getComponentCount()==0) {
			setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		}
		if (zebra) {
			comp.setBackground(Color.GRAY);
		}
		else {
			comp.setBackground(Color.LIGHT_GRAY);
		}
		zebra = !zebra;
		super.add(comp);
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
		return true;
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return getScrollableBlockIncrement(visibleRect, orientation, direction);
	}

}

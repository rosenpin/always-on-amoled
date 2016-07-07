package info.staticfree.android.twentyfourhour.overlay;

import java.util.Calendar;

import android.graphics.Canvas;

/**
 *
 * An overlay for a clock dial.
 *
 * @author <a href="mailto:steve@staticfree.info">Steve Pomeroy</a>
 *
 */
public interface DialOverlay {

	/**
	 * Subclasses should implement this to draw the overlay.
	 *
	 * @param canvas
	 *            the canvas onto which you must draw
	 * @param cX
	 *            the x coordinate of the center
	 * @param cY
	 *            the y coordinate of the center
	 * @param w
	 *            the width of the canvas
	 * @param h
	 *            the height of the canvas
	 * @param calendar
	 *            the desired date/time
	 */
	public abstract void onDraw(Canvas canvas, int cX, int cY, int w, int h, Calendar calendar,
			boolean sizeChanged);

}
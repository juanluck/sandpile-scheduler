package org.graphics;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.*;
import fang2.attributes.*;
import fang2.core.*;
import fang2.sprites.*;
import fang2.transformers.*;

/**This example shows how to snap the
 * user mouse to a 5 x 5 grid.
 * @author Jam Jenkins 
 */
public class SnapToGrid
			extends Game
{
	/**this is the square which moves
	 * around with the mouse*/
	private Sprite pointer;

	/**adds the grid to the background
	 * and the circle in front*/
	public void setup()
	{
		Grid grid=new Grid(20, 10, 0.01);
		grid.setScale(1);
		grid.setLocation(0.5, 0.5);

		pointer=new RectangleSprite(0.1,0.05);
		pointer.setLocation(0.05,0.025);
		pointer.setColor(getColor("lightgreen"));

		addSprite(pointer);
		addSprite(grid);

		setHelp("resources/SnapToGridHelp.txt");
	}

	/**gets the snapped to grid location
	 * @param point the horizontal and vertical
	 * screen location
	 * @return the snapped to grid column
	 * and row.  These are numbered 0...4.
	 */
	public Point getCell(Location2D point)
	{
		int x=(int)Math.min(4, point.x*5);
		int y=(int)Math.min(4, point.y*5);
		return new Point(x, y);
	}

	/**gets the screen coordinate for the
	 * given row and column
	 * @param point the grid column and row.
	 * The x value represents the column and
	 * the y value represents the row.
	 * @return the snapped to grid position
	 * on the screen
	 */
	public Location2D getScreenLocation(Point point)
	{
		return new Location2D(
		           (point.x+0.5)/5,
		           (point.y+0.5)/5);
	}

	/**snaps the screen location to the
	 * center of the nearest cell
	 * @param screen the screen location
	 * @return the center of the nearest cell
	 */
	public Location2D getSnappedLocation(Location2D screen)
	{
		return getScreenLocation(getCell(screen));
	}

	/**moves the circle with the mouse.  When
	 * the user clicks, a circle is deposited
	 * in the nearest cell.
	 * @param timePassed not used
	 */
	public void advance()
	{
		//makes the circle follow the mouse
		pointer.setLocation(getSnappedLocation(getMouse2D()));
	}

	/**runs SnapToGrid as an application
	 * @param args not used
	 */
	public static void main(String[] args)
	{
		new SnapToGrid().runAsApplication();
	}

}

//Uploaded on Sat Sep 19 13:58:12 EDT 2009


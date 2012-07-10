package org.graphics;

import java.awt.geom.*;

import fang2.attributes.*;
import fang2.core.*;
import fang2.sprites.*;
import fang2.transformers.*;

/**Makes a grid shaped sprite.
 * @author Jam Jenkins
 */
public class Grid
			extends Sprite
{
	/**makes a sprite with a given number of
	 * rows and columns
	 * @param rows the number of vertical cells
	 * @param columns the number of horizontal cells
	 * @param margin the percent of the cell reserved
	 * for the line between cells
	 */
	public Grid(int rows, int columns,
	            double margin)
	{
		Area area=new Area();
		area.add(new Area(new Rectangle2D.Double(0, 0, 1, 1)));
		double w=(1.0-margin)/columns;
		double h=(1.0-margin)/rows;
		for(int r=0; r<rows; r++)
		{
			for(int c=0; c<columns; c++)
			{
				double x=(c+0.5*margin)/columns;
				double y=(r+0.5*margin)/rows;
				Rectangle2D.Double cell=
				    new Rectangle2D.Double(x, y, w, h);
				area.subtract(new Area(cell));
			}
		}
		setShape(area);
	}
}


//Uploaded on Sat Sep 19 13:58:08 EDT 2009


/*
 * MergeHero: Differing and Merging Folders & Files
 *
 * Copyright © 2004, Dynamsoft, Inc. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.opensource.org/licenses/gpl-3.0.html.
 */

package MergeHeroLib;
/*
 * Created on 2005-4-18
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author Falcon Young
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CPoint
{
    public static void main(String[] args)
    {
    }
    
    public CPoint(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    
    public void copy(CPoint pt)
    {
        this.x = pt.x;
        this.y = pt.y;
    }
    
    public void zero()
    {
        this.x = 0;
        this.y = 0;
    }
    
    public boolean equals(CPoint pt)
    {
        if (this.x == pt.x && this.y == pt.y)
        {
            return true;
        }
        
        return false;
    }
    
    public int x;
    public int y;
}

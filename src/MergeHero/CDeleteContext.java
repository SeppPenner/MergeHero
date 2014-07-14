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

package MergeHero;

import MergeHeroLib.CPoint;

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
public class CDeleteContext extends CInsertContext
{
    public static void main(String[] args)
    {
    }
	public CPoint m_ptStart = new CPoint(0, 0);
    public CPoint m_ptEnd = new CPoint(0, 0);

    public void RecalculatePoint(CPoint ptPoint)
    {
    	if (ptPoint.y < m_ptStart.y)
    	{
    		return;
    	}

    	if (ptPoint.y > m_ptEnd.y)
        {
    		ptPoint.y -= (m_ptEnd.y - m_ptStart.y);
    		return;
        }

    	if (ptPoint.y == m_ptEnd.y && ptPoint.x >= m_ptEnd.x)
        {
    		ptPoint.y = m_ptStart.y;
    		ptPoint.x = m_ptStart.x + (ptPoint.x - m_ptEnd.x);
    		return;
        }

    	if (ptPoint.y == m_ptStart.y)
        {
    		if (ptPoint.x > m_ptStart.x)
    		{
    			ptPoint.x = m_ptStart.x;
    		}
    		return;
        }

    	ptPoint.copy(m_ptStart);
    }
}

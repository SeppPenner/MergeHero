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

/*
 * Created on 2005-1-13
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.dynamsoft.sourceanywhere;

import java.util.ArrayList;

/**
 * @author Falcon Young
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StringArray extends ArrayList
{
	public void add(int iIndex, Object strToAdd) //insert
	{
		if (strToAdd instanceof String)
			super.add(iIndex,strToAdd);
	}
	
	public boolean add(Object strToAdd)
	{
		if (strToAdd instanceof String)
			return super.add(strToAdd);
		else
			return false;
	}
	
	public Object set(int iIndex,Object strToSet)
	{
		if (strToSet instanceof String)
			return (String)super.set(iIndex,strToSet);
		else
			return (String)null;
	}
}

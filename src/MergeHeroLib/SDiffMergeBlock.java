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
 * Created on 2006-4-28
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package MergeHeroLib;

/**
 * @author Falcon Young
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SDiffMergeBlock
{

    public static void main(String[] args)
    {
    }
    
 	public int m_nLeftStartLine;   // record the left view corresponding start line
	public int m_nLeftEndLine;
	public int m_nRightStartLine;  // record the right view corresponding start line
	public int m_nRightEndLine;

	public boolean m_bLeftIsGhost;	// record the left view is ghost line
	public boolean m_bRightIsGhost;	// record the right view is ghost line
	public boolean m_bLeftIsNormal;	// record the left view is normal line
	public boolean m_bRightIsNormal;	// record the right view is normal line

	// The following members only for merge
	// If current is diff result, ignore them
	public int m_nMergeStartLine;	// record the merge view original start line, 
	public int m_nMergeEndLine;

	public boolean m_bRightAdded; // record which view's lines are added for conflict blcok
	public boolean m_bLeftAdded;	
	public boolean m_bConflict;	// record the block is conflict or not
	public boolean m_bHidden;		// record the block is hidden or not
	
	public SDiffMergeBlock()
	{
		m_nLeftStartLine = -1;
		m_nLeftEndLine = -1;
		m_nRightStartLine = -1;
		m_nRightEndLine = -1;
		m_nMergeStartLine = -1;
		m_nMergeEndLine = -1;
		m_bRightAdded = false;
		m_bLeftAdded = false;
		m_bConflict = false;
		m_bHidden = false;
		m_bLeftIsGhost = false;
		m_bRightIsGhost = false;
		m_bLeftIsNormal = false;
		m_bRightIsNormal = false;
	}    
}

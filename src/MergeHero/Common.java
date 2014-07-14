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
 * Created on 2005-4-13
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
package MergeHero;
import java.awt.*;
import java.util.*;

public interface Common 
{ 
  //define return value
   public static final int DIFF_HELP = 0x0005;
    public static final int DIFF_PARAMS_ERROR = 0x0006;
    public static final int DIFF_PARAMS_OK = 0x0007;
    public static final int DIFF_BINARY_OK = 0x0008;
    public static final int UNDO_DESCRIP_BUF = 30;
    public static final String TITLE_CAPTION = "MergeHero";

    public static final Color COLOR_INSERT = new Color(142, 199, 255);
    public static final Color COLOR_DELETE = new Color(239, 223, 123);
    public static final Color COLOR_CONFLICT = new Color(255, 0, 0);
    public static final Color COLOR_GHOST = new Color(221, 219, 221);
    public static final Color COLOR_TEXT = new Color(0, 0, 0);
    public static final Color COLOR_TRIVAL = new Color(255, 255, 255);
    public static final Color COLOR_ADD = new Color(156, 203, 181);
    public static final Color COLOR_MOVE = new Color(0, 128, 128);
    public static String LEFT_LABEL = "Theirs -- ";
    public static String RIGHT_LABEL = "Yours -- "; 
    public static int MAX_PATH = 260;
    
    public static final int LEFT_VIEW = 0;
    public static final int RIGHT_VIEW = 1;
    public static final int MERGE_VIEW = 2;
}

interface EnumLoadResult
{
  public static final int Load_Error = 0;
  public static final int Load_Ok = 1;
  public static final int Load_Ok_Impure = 2;
  public static final int Load_Binary = 3;
}

interface EnumSaveResult
{
  public static final int Save_Ok = 0;   
  public static final int Save_Fail = 1;
  public static final int Save_No_FileName = 2;  
  public static final int Save_Cancel = 3;
}

interface EnumMainStatusIndex
{
  public static final int PANE_MAIN_MERGE_LINE = 0;
  public static final int PANE_TOTAL_ITEMS = 0;
  public static final int PANE_FILE_DIFFS = 0;
  public static final int PANE_MAIN_CONFLICTS = 1;
  public static final int PANE_TOTAL_DIFFS = 1;
  public static final int PANE_MAIN_MERGED = 2;
  public static final int PANE_TOTAL_SAMES = 2;  
}

interface EnumLineFlags
{
  public static final long LF_EXECUTION = 0x00010000L;
}

interface EnumMergeType
{
  public static final int Two_Way_Merge = 0x0001;
  public static final int Three_Way_Merge = 0x0002;
  public static final int Auto_Merge = 0x0100;
  public static final int Manual_Merge = 0x0200;
}

interface EnumBufferType
{
  public static final int LOCAL_BUFFER = 0;
  public static final int SERVER_BUFFER = 1;
  public static final int MERGE_BUFFER = 2;
  public static final int ORIGINAL_BUFFER = 3;
}

interface EnumDrawColor
{
  public static final int COLORINDEX_WHITESPACE = 0;
  public static final int COLORINDEX_BKGND = 1;
  public static final int COLORINDEX_NORMALTEXT = 2;
  public static final int COLORINDEX_SELMARGIN = 3;
  public static final int COLORINDEX_SELBKGND = 4;
  public static final int COLORINDEX_SELTEXT = 5;
};

interface EnumFILETYPE
{
  public static final int TYPE_TEXT = 0;
  public static final int TYPE_BINARY = 1;
}

interface EnumUndo
{
  public static final int UNDO_INSERT = 0x0001;
  public static final int UNDO_BLOCK = 0x0002;
  public static final int UNDO_BEGINGROUP = 0x0100;
}

interface EnumFindText
{
    public static final int FIND_MATCH_CASE = 0x0001;
    public static final int FIND_WHOLE_WORD = 0x0002;
    public static final int FIND_REGEXP = 0x0004;
    public static final int FIND_DIRECTION_UP = 0x0010;
    public static final int REPLACE_SELECTION = 0x0100; 
    public static final int REPLACE_NO_WRAP = 0x200;
}

interface enumAcvion 
{
  public static final int CE_ACTION_UNKNOWN = 0;
  public static final int CE_ACTION_PASTE = 1;
  public static final int CE_ACTION_DELSEL = 2;
  public static final int CE_ACTION_CUT = 3;
  public static final int CE_ACTION_TYPING = 4;
  public static final int CE_ACTION_BACKSPACE = 5;
  public static final int CE_ACTION_INDENT = 6;
  public static final int CE_ACTION_DRAGDROP = 7;
  public static final int CE_ACTION_REPLACE = 8;
  public static final int CE_ACTION_DELETE = 9;
  public static final int CE_ACTION_AUTOINDENT = 10;
  public static final int CE_ACTION_AUTOCOMPLETE = 11;
  public static final int CE_ACTION_AUTOEXPAND = 12;
  public static final int CE_ACTION_LOWERCASE = 13;
  public static final int CE_ACTION_UPPERCASE = 14;
  public static final int CE_ACTION_SWAPCASE = 15;
  public static final int CE_ACTION_CAPITALIZE = 16;
  public static final int CE_ACTION_SENTENCIZE = 17;
  public static final int CE_ACTION_RECODE  = 18; 
  public static final int CE_ACTION_SPELL = 19;
  public static final int CE_ACTION_MERGE = 20;
}

class SColorSettings
{
	public Color m_clrInserted;		
	public Color m_clrDeleted;	
	public Color m_clrConflict;		 
	public Color m_clrTrival;
	public Color m_clrGhost;
	public Color m_clrText;
	public Color m_clrAdded;
	public Color m_clrMoved;
}

//Infos about the last search settings
//Is also used in the replace dialog
class SLastSearchInfos
{
	public int m_nDirection;       // only for search
	public boolean m_bReplaceNoWrap;  // only for replace
	public boolean m_bMatchCase;
	String m_strText;
	public boolean m_bWholeWord;
	public boolean m_bRegExp;

	SLastSearchInfos()
	{
		m_nDirection = 1;
		m_bReplaceNoWrap = false;
		m_bMatchCase = false;
		m_strText = "";
		m_bWholeWord = false;
		m_bRegExp = false;
	}
}

class CRect
{
    public int    left;
    public int    top;
    public int    right;
    public int    bottom;
    
	// uninitialized rectangle
	CRect()
	{
	   left = top = right = bottom = 0; 
	}
	// from left, top, right, and bottom
	CRect(int l, int t, int r, int b)
	{
	    left = l; top = t; right = r; bottom = b;
	}
	
	public int Width()
	{ return right - left; }
	
	public int Height() 
	{ return bottom - top; }
	
	public void setRect(int l, int t, int r, int b)
	{
	    left = l; top = t; right = r; bottom = b;
	}
	
	public void OffsetRect(int x, int y)
	{
	    left += x;
	    top += y;
	    right += x;
	    bottom += y;
	}
	
	public void copy(CRect rc)
	{
	    this.left = rc.left;
	    this.top = rc.top;
	    this.right = rc.right;
	    this.bottom =rc.bottom;
	}
	
	public boolean equals(CRect rc)
	{
	    if (this.left == rc.left && this.top == rc.top &&
	            this.right == rc.right && this.bottom == rc.bottom)
	    {
	        return true;
	    }
	    
	    return false;
	}
}
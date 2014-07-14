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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;
import javax.swing.JScrollBar;

import MergeHeroLib.CDynamicTextBuffer;
import MergeHeroLib.CMemoryOperator;
import MergeHeroLib.CPoint;
import MergeHeroLib.CUpdateContext;

import com.dynamsoft.sourceanywhere.BaseDataObject;
import com.dynamsoft.sourceanywhere.IntegerArray;

/*
 * Created on 2005-4-19
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
public class CTextView extends JPanel implements MouseListener, MouseMotionListener, 
										KeyListener//, ClipboardOwner 
{
    protected CTextBuffer m_pTextBuffer = null;
    private SColorSettings m_colorsSetting = new SColorSettings();
    protected boolean m_bMergeView = false;
    private Font f = null;
    private FontMetrics fm = null;
    public int m_nLineHeight = -1, m_nCharWidth = -1;
    CPoint m_ptCursorPos = new CPoint(0, 0), m_ptCursorLast = new CPoint(0, 0);
    CPoint m_ptSelStart = new CPoint(0, 0), m_ptSelEnd = new CPoint(0, 0);
    CPoint m_ptDrawSelStart = new CPoint(0, 0), m_ptDrawSelEnd = new CPoint(0, 0);
	CPoint ptStart = new CPoint(0, 0);
	CPoint ptEnd = new CPoint(0, 0);
	CPoint pos = new CPoint(0, 0);
	CPoint m_ptAnchor = new CPoint(0, 0);
	CPoint m_charPoint = new CPoint(0, 0);
	CPoint pointCursor = new CPoint(0, 0), ptCurrentPos = new CPoint(0, 0);
	CPoint subLinePos = new CPoint(0, 0), subLinePosEnd = new CPoint(0, 0);
    private CRect rcLine = new CRect();
    CRect rcCacheMargin = new CRect();
    CRect rcCacheLine = new CRect();
    CRect rcClient = new CRect();
    boolean m_bSelMargin = false;
    int m_nTopLine = 0, m_nOffsetChar = 0;
    int m_nMaxLineLength;
    int m_nIdealCharPos; 
    //  Amount of lines/characters that completely fits the client area
    int m_nScreenLines, m_nScreenChars;
    IntegerArray m_parySubLines = new IntegerArray();
    int[] anBreaks = new int[50];
    boolean m_bWordWrap = false;
    boolean m_bViewTabs = false;
    boolean m_bViewEols = false;
    boolean m_bDistinguishEols = false;
    boolean m_bWordSelection = false, m_bLineSelection = false;
    boolean m_bDisableDragAndDrop = true;
    Color CLR_NONE = null;
    Color m_crBkgnd = null;
    Graphics pdc = null;
    BaseDataObject	m_nBreaks = new BaseDataObject();
    BaseDataObject m_strText = new BaseDataObject();
    BaseDataObject	nLine = new BaseDataObject(), nSubLine = new BaseDataObject();
	BaseDataObject nSubLineOffset = new BaseDataObject();
    Clipboard textClipboard = getToolkit().getSystemClipboard();
    long m_dwFlags = 0;
    boolean m_bCursorHidden = false;
    CRect rcOldCaret = new CRect(), rcNewCaret = new CRect();
    private final int LEFT_BEGIN = 1;
    int dx = 0, dy = 0;
    public int iRealX = 0, iRealY = 0;
	int m_nLineIndex = 0;
	int m_nLastFindWhatLen = 0;
 
    // The index of the subline that is the first visible line on the screen.
    int m_nTopSubLine = 0;
    boolean  m_bSmoothScroll;  
    private ScrollFrame scrollFrame = null;
    Pattern  pattern = null;
    Matcher matcher = null;
    MainJFrame mainFrame = null;

    public static void main(String[] args) throws IOException
    {
 
    }
    
    // Syntax coloring overrides
    class STextBlock
	{
        int m_nCharPos;
        int m_nColorIndex;

	}
    
    CTextView()
    {
    	m_colorsSetting.m_clrConflict = Common.COLOR_CONFLICT;
    	m_colorsSetting.m_clrDeleted = Common.COLOR_DELETE;
    	m_colorsSetting.m_clrGhost = Common.COLOR_GHOST;
    	m_colorsSetting.m_clrInserted = Common.COLOR_INSERT;
    	m_colorsSetting.m_clrText = Common.COLOR_TEXT;
    	m_colorsSetting.m_clrTrival = Common.COLOR_TRIVAL;   
    	m_colorsSetting.m_clrAdded = Common.COLOR_ADD;
    	m_colorsSetting.m_clrMoved = Common.COLOR_MOVE;
    	
    	//setPreferredSize(new Dimension(300, 200));

    	m_bSelMargin = false;
    	addKeyListener(this);
    	addMouseListener(this);
    	addMouseMotionListener(this);
    }
    
    public void setScrollFrame(ScrollFrame scrollFrame)
    {
        this.scrollFrame = scrollFrame;
    }
    
    public void translate(int x, int y)
    {
        dx = x;
        dy = y;
        repaint();
    }
    
    public void showText(Graphics g)
    {
        if (m_pTextBuffer == null || !m_pTextBuffer.m_bInit)
        {
            return;
        }
        
 	    setFonts(g);
	    g.setFont(f);
	   	boolean bDeleted = false;
    	long dwLineFlags = 0;
    	int nLineCount = GetLineCount();
    	int nLineHeight = GetLineHeight();
    	int iWeight = getSize().width, iHeight = getSize().height;
    	PrepareSelBounds();
    	
    	iRealX = GetLineMaxWidth() > iWeight ? GetLineMaxWidth() : iWeight;
    	iRealY = nLineHeight * nLineCount > iHeight ? nLineHeight * nLineCount : iHeight;
    	rcClient.setRect(0, 0, iRealX, iRealY);
    	rcLine.setRect(0, 0, rcClient.Width(), rcClient.Height());
    	rcLine.bottom += rcLine.top + nLineHeight;
    	rcCacheMargin.setRect(0, 0, GetMarginWidth(), nLineHeight);
    	rcCacheLine.setRect(GetMarginWidth(), 0, rcLine.Width(), nLineHeight);
    	
    	// initialize rects
    	int	nSubLineOffset = GetSubLineIndex(m_nTopLine) - m_nTopSubLine;
    	if(nSubLineOffset < 0)
    	{
    		rcCacheMargin.OffsetRect(0, nSubLineOffset * nLineHeight );
    		rcCacheLine.OffsetRect(0, nSubLineOffset * nLineHeight );
    	}
    	   	
    	int nMaxLineChars = GetScreenChars();
    	
    	int nCurrentLine = m_nTopLine;
    	while (rcLine.top < rcClient.bottom)
        {
    		rcLine.bottom = rcLine.top + (m_nBreaks.getIntValue() + 1) * nLineHeight;
    		rcCacheLine.bottom = rcCacheLine.top + rcLine.Height();
    		rcCacheMargin.bottom = rcCacheMargin.top + rcLine.Height();
    		
    		if( rcCacheLine.top < 0 )
    		{
    			rcLine.bottom += rcCacheLine.top;
    		}
    		if (nCurrentLine < nLineCount)
            {
    			//DrawMargin(g, rcCacheMargin, nCurrentLine);
    			DrawSingleLine(g, rcLine, nCurrentLine);
            }
    		else
            {
    			//DrawMargin(g, rcCacheMargin, -1);
    			DrawSingleLine(g, rcLine, -1);
            }
     		
    		nCurrentLine++;
    		rcLine.top = rcLine.bottom;
    		rcCacheLine.top = 0;
    		rcCacheMargin.top = 0;
       }
    }
    
    public void UpdateView(CTextView pSource, CUpdateContext pContext,
			   long dwFlags, int nLineIndex)
    {
        repaint();
    }
    
    public void ResetView()
    {
        m_dwFlags = 0;
    	m_nTopLine = 0;
    	m_nTopSubLine = 0;
    	m_nOffsetChar = 0;
    	m_nLineHeight = -1;
    	m_nCharWidth = -1;
    	m_nMaxLineLength = -1;
    	m_nScreenLines = -1;
    	m_nScreenChars = -1;
    	m_nIdealCharPos = -1;
    	m_parySubLines.clear();
    	m_nLastFindWhatLen = 0;
    }
    
    public void DetachFromBuffer()
    {
      if (m_pTextBuffer != null)
        {
          m_pTextBuffer.RemoveView(this);
          m_pTextBuffer = null;
          ResetView();
        }
    }

    //  Attach buffer (maybe for the first time)
    //  initialize the view and initialize both scrollbars
    public void AttachToBuffer(CTextBuffer pBuf)
    {
        if (m_pTextBuffer != null)
        {
            m_pTextBuffer.RemoveView(this);
        }
    	if (pBuf == null)
        {
    		pBuf = LocateTextBuffer();
        }
        m_pTextBuffer = pBuf;
        if (m_pTextBuffer != null)
        {
            m_pTextBuffer.AddView(this);
        }

        ResetView();
    }
    
    public CTextBuffer LocateTextBuffer()
    {
    	return null;
    }
    
    public long GetLineFlags(int index)
    {
    	if (m_pTextBuffer == null || !m_pTextBuffer.m_bInit)
    	{
    		return 0;
    	}
    	
    	return m_pTextBuffer.GetLineFlags(index);
    }
    
    private Color getLinebkgColor(int index)
    {
    	long dwLineFlags = GetLineFlags(index);
    	
    	m_crBkgnd = m_colorsSetting.m_clrTrival;
    	
    	// Line inside diff
    	if (dwLineFlags != 0)
    	{
    		Color crText = m_colorsSetting.m_clrText;
 
    		if ((dwLineFlags & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_INSERTED) == CDynamicTextBuffer.MERGE_LINEFLAGS.LF_INSERTED)
    		{
    			if ((dwLineFlags & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_ADDED) == CDynamicTextBuffer.MERGE_LINEFLAGS.LF_ADDED && !m_bMergeView)
    			{
    				m_crBkgnd = m_colorsSetting.m_clrAdded;
    			}
    			else
    			{
    				m_crBkgnd = m_colorsSetting.m_clrInserted;
    			}
    		    return m_crBkgnd;
    		}
    		if ((dwLineFlags & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_GHOST) == CDynamicTextBuffer.MERGE_LINEFLAGS.LF_GHOST)
    		{
    		    m_crBkgnd = m_colorsSetting.m_clrGhost;
    		    return m_crBkgnd;
    		}
    		if ((dwLineFlags & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED) == CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED ||
    		        ((dwLineFlags & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN) == CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN) &&
	                ((dwLineFlags & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_CONFLICT) != CDynamicTextBuffer.MERGE_LINEFLAGS.LF_CONFLICT))
    		{
    			if ((dwLineFlags & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_ADDED) == CDynamicTextBuffer.MERGE_LINEFLAGS.LF_ADDED)
    			{
    				m_crBkgnd = m_colorsSetting.m_clrAdded;
    			}
    			else
    			{
   				    m_crBkgnd = m_colorsSetting.m_clrDeleted;
    			}
    		    return m_crBkgnd;
    		}
    		if ((dwLineFlags & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_CONFLICT) == CDynamicTextBuffer.MERGE_LINEFLAGS.LF_CONFLICT)
    		{
    		    m_crBkgnd = m_colorsSetting.m_clrConflict;
    		    return m_crBkgnd;
    		}
    		if ((dwLineFlags & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_TRIVIAL) == CDynamicTextBuffer.MERGE_LINEFLAGS.LF_TRIVIAL)
    		{
    		    m_crBkgnd = m_colorsSetting.m_clrTrival;
    		    return m_crBkgnd;
    		}
    	}  
 
    	return m_crBkgnd;
    }
    
    public void setFonts(Graphics g)
    {
        if (f != null)
        {
            return;
        }
        
        f = new Font("SansSerif", Font.PLAIN, 14);
        fm = g.getFontMetrics(f);  
        rcNewCaret.setRect(LEFT_BEGIN, 0, LEFT_BEGIN, GetLineHeight());
        rcOldCaret.copy(rcNewCaret);
    }
    
    // Line/character dimensions
    private void CalcLineCharDim()
    {
        if (fm != null)
        {
            m_nLineHeight = fm.getHeight();
        }
    }
    
	public void paintComponent(Graphics g)
	{
	    super.paintComponent(g);
	    g.setColor(Color.white);
	    g.fillRect(0, 0, this.getSize().width, this.getSize().height);
	    if (m_pTextBuffer == null || !m_pTextBuffer.m_bInit) 
	    {
			return;
		}	    
	    g.translate(-dx, -dy);
	    showText(g);
	    drawCaret(g);
	    drawSelection(g);
	}
	
	void PrepareSelBounds()
	{
		if (m_ptSelStart.y < m_ptSelEnd.y ||
	        (m_ptSelStart.y == m_ptSelEnd.y && m_ptSelStart.x < m_ptSelEnd.x))
	    {
			m_ptDrawSelStart.copy(m_ptSelStart);
			m_ptDrawSelEnd.copy(m_ptSelEnd);
	    }
		else
	    {
			m_ptDrawSelStart.copy(m_ptSelEnd);
			m_ptDrawSelEnd.copy(m_ptSelStart);
	    }
	}
	
	int GetLineCount()
	{
		if (m_pTextBuffer == null || !m_pTextBuffer.m_bInit)
		{
			return 1;  //  Single empty line
		}
		
		int nLineCount = m_pTextBuffer.GetLineCount();
		return nLineCount;
	}
	
	int GetLineHeight()
	{
		if (m_nLineHeight == -1)
		{
			CalcLineCharDim();
		}
		return m_nLineHeight;
	}
	
	int GetLineMaxWidth()
	{
		if (m_pTextBuffer == null || !m_pTextBuffer.m_bInit)
		{
			return 1;  //  Single empty line
		}
		
		return (m_pTextBuffer.GetMaxString() * GetCharWidth());
	}
	
	int GetMarginWidth()
	{
		return m_bSelMargin ? 20 : 1 * LEFT_BEGIN;
	}
	
	void WrapLineCached(int nLineIndex, int nMaxLineWidth, BaseDataObject anBreaks, BaseDataObject nBreaks )
	{
		// if word wrap is not active, there is not any break in the line
		if (!m_bWordWrap)
		{
			nBreaks.setIntValue(0);
			return;
		}
	}
	
	int GetSubLineIndex(int nLineIndex)
	{
		// if we do not wrap words, subline index of this line is equal to its index
		if (!m_bWordWrap)
		{
			return nLineIndex;
		}
		
		// calculate subline index of the line
		int nSubLineCount = 0;
		int nLineCount = GetLineCount();
		
		if (nLineIndex >= nLineCount)
		{
			nLineIndex = nLineCount - 1;
		}
		
		for (int i = 0; i < nLineIndex; i++)
		{
			nSubLineCount+= GetSubLines(i);
		}
		
		return nSubLineCount;
	}
	
	int GetScreenChars()
	{
		if (m_nScreenChars == -1)
	    {
			m_nScreenChars = (rcClient.Width() - GetMarginWidth())/GetCharWidth();
	    }
		return m_nScreenChars;
	}
	
	void DrawMargin(Graphics g, CRect rect, int nLineIndex)
	{
		if (!m_bSelMargin)
	    {
		    fillRect(g, GetColor(EnumDrawColor.COLORINDEX_BKGND), rect);
			return;
	    }
		
		fillRect(g, GetColor(EnumDrawColor.COLORINDEX_SELMARGIN), rect);		
	}
	
	void fillRect(Graphics g, Color color, CRect rect)
	{
	    Color old = g.getColor();
	    g.setColor(color);
	    g.fillRect(rect.left, rect.top, rect.Width(), rect.Height());
	    g.setColor(old);
	}
	
	Color GetColor(int nColorIndex)
	{
		switch (nColorIndex)
	    {
	    case EnumDrawColor.COLORINDEX_WHITESPACE:
	    case EnumDrawColor.COLORINDEX_BKGND:
			return Color.white;
	    case EnumDrawColor.COLORINDEX_NORMALTEXT:
			return Color.black;
	    case EnumDrawColor.COLORINDEX_SELMARGIN:
			return new Color(192, 192, 192);
	    case EnumDrawColor.COLORINDEX_SELBKGND:
			return new Color(0, 0, 0);
	    case EnumDrawColor.COLORINDEX_SELTEXT:
			return new Color(255, 255, 255);
	    }
		// return RGB(255, 0, 0);
		return new Color(128, 0, 0);
	}
	
	int GetCharWidth()
	{
	    if (fm != null)
	    {
	        return fm.stringWidth("1");
	    }
	    return 6;
	}
	
	int GetLineLength(int nLineIndex)
	{
		if (m_pTextBuffer == null || !m_pTextBuffer.m_bInit)
		{
			return 0;
		}
		return m_pTextBuffer.GetLineLength (nLineIndex);
	}
	
	char[] GetLineChars(int nLineIndex)
	{
		if (m_pTextBuffer == null || !m_pTextBuffer.m_bInit)
		{
			return null;
		}
		return m_pTextBuffer.GetLineChars(nLineIndex);
	}
	
	void GetLineColors(int nLineIndex, Color crBkgnd, Color crText,
            BaseDataObject bDrawWhitespace)
    {
        long dwLineFlags = GetLineFlags(nLineIndex);
        bDrawWhitespace.setBooleanValue(true);
        crBkgnd = Color.white;
        crText = Color.black;
        if ((dwLineFlags & EnumLineFlags.LF_EXECUTION) != 0)
        {
            crBkgnd = new Color(0, 128, 0);
            return;
        }
        crBkgnd = CLR_NONE;
        crText = CLR_NONE;
        bDrawWhitespace.setBooleanValue(false);
    }
	
	Color GetLineColors(int nLineIndex)
	{
	    return getLinebkgColor(nLineIndex);
	}
	
	int GetFullLineLength(int nLineIndex)
	{
		if (m_pTextBuffer == null || !m_pTextBuffer.m_bInit)
		{
			return 0;
		}
		return m_pTextBuffer.GetFullLineLength(nLineIndex);
	}
	
	void DrawSingleLine(Graphics g, CRect rc, int nLineIndex)
	{
		int nCharWidth = GetCharWidth();
		
		if (nLineIndex == -1)
	    {
			// Draw line beyond the text
		    fillRect(g, GetColor(EnumDrawColor.COLORINDEX_WHITESPACE), rc);
			return;
	    }
		
		//  Acquire the background color for the current line
		boolean bDeleted = false;
		long dwLineFlags = GetLineFlags(nLineIndex);
		// Draw deleted line
		if ((dwLineFlags & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED) == CDynamicTextBuffer.MERGE_LINEFLAGS.LF_DELETED ||
		        (((dwLineFlags & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN) == CDynamicTextBuffer.MERGE_LINEFLAGS.LF_HIDEN) &&
		                ((dwLineFlags & CDynamicTextBuffer.MERGE_LINEFLAGS.LF_CONFLICT) != CDynamicTextBuffer.MERGE_LINEFLAGS.LF_CONFLICT)))
		{
			bDeleted = true;
		}

		//  Draw the line text
		fillRect(g, getLinebkgColor(nLineIndex), rc);
		g.setColor(Color.black);
		
		String str = m_pTextBuffer.GetLineText(nLineIndex);
		g.drawString(str, rc.left + 2* LEFT_BEGIN, rc.top + fm.getAscent());
        if (bDeleted)
        {
             g.setColor(Color.red);
             g.drawLine(2 * LEFT_BEGIN, rc.top + fm.getAscent()/2, rc.right + 2 * LEFT_BEGIN, rc.top + fm.getAscent()/2);
        }  
	}
	
	void drawCaret(Graphics g)
	{
	    //g.setColor(Color.white);
	    //g.drawLine(rcOldCaret.left, rcOldCaret.top, rcOldCaret.left, rcOldCaret.bottom);
	    //repaint(rcOldCaret.left, rcOldCaret.top, rcOldCaret.Width(), rcOldCaret.Height());
	    g.setColor(Color.black);
	    g.drawLine(rcNewCaret.left, rcNewCaret.top, rcNewCaret.left, rcNewCaret.bottom);
	    //System.out.println("Draw caret." + rcNewCaret.left + rcNewCaret.top + rcNewCaret.left + rcNewCaret.bottom);
	    //repaint(rcNewCaret.left, rcNewCaret.top, rcNewCaret.Width(), rcNewCaret.Height());
	}
	
	void setCursor(CPoint point, boolean bShift, boolean bControl)
	{
		if (point.x < GetMarginWidth())
	    {
			AdjustTextPoint(point);
			if (bControl)
	        {
				SelectAll();
	        }
			else
	        {
				m_ptCursorPos.copy(ClientToText(point));
				// Find char pos that is the beginning of the subline clicked on
				CharPosToPoint(m_ptCursorPos.y, m_ptCursorPos.x, pos);
				m_ptCursorPos.x = SubLineHomeToCharPos(m_ptCursorPos.y, pos.y);
				
				if (!bShift)
				{
					m_ptAnchor.copy(m_ptCursorPos);
				}
				
				CharPosToPoint(m_ptAnchor.y, m_ptAnchor.x, pos);
				ptStart.y = m_ptAnchor.y;
				if(GetSubLineIndex(ptStart.y) + pos.y == GetSubLineCount() - 1)
				{
					// select to end of subline
					ptStart.x = SubLineEndToCharPos(ptStart.y, pos.y);
					ptStart.y = 0;
				}
				else
				{
					GetLineBySubLine(GetSubLineIndex(ptStart.y) + pos.y + 1, nLine, nSubLine);
					ptStart.y = nLine.getIntValue();
					ptStart.x = SubLineHomeToCharPos(nLine.getIntValue(), nSubLine.getIntValue());
				}

				ptEnd.copy(m_ptCursorPos);
				
				m_ptCursorPos.copy(ptEnd);
				UpdateCaret();
				EnsureVisible(m_ptCursorPos);
				SetSelection(ptStart, ptEnd);
				
				m_bWordSelection = false;
				m_bLineSelection = true;
	        }
	    }
		else
	    {
			CPoint ptText = ClientToText(point);
			PrepareSelBounds();
			// Support For Disabling Drag and Drop...
			if ((IsInsideSelBlock(ptText)) &&    // If Inside Selection Area
	            (!m_bDisableDragAndDrop))    // And D&D Not Disabled
				
	        {
				//m_bPreparingToDrag = TRUE; // Marked by yns
	        }
			else
	        {
				AdjustTextPoint(point);
				m_ptCursorPos.copy(ClientToText(point));
				if (!bShift)
				{
					m_ptAnchor.copy(m_ptCursorPos);
				}
				
				if (bControl)
	            {
					if (m_ptCursorPos.y < m_ptAnchor.y ||
	                    m_ptCursorPos.y == m_ptAnchor.y && m_ptCursorPos.x < m_ptAnchor.x)
	                {
						ptStart.copy(WordToLeft(m_ptCursorPos));
						ptEnd.copy(WordToRight(m_ptAnchor));
	                }
					else
	                {
						ptStart.copy(WordToLeft(m_ptAnchor));
						ptEnd.copy(WordToRight(m_ptCursorPos));
	                }
	            }
				else
	            {
					ptStart.copy(m_ptAnchor);
					ptEnd.copy(m_ptCursorPos);
	            }
				
				m_ptCursorPos.copy(ptEnd);
				UpdateCaret();
				EnsureVisible(m_ptCursorPos);
				SetSelection(ptStart, ptEnd);
				
				m_bWordSelection = bControl;
				m_bLineSelection = false;
	        }
	    }
		
		// we must set the ideal character position here!
		m_nIdealCharPos = CalculateActualOffset(m_ptCursorPos.y, m_ptCursorPos.x);
	    m_nLineIndex = m_ptCursorPos.y;	    
	}
	
	public void mouseClicked(MouseEvent evt)
	{	    
	    this.requestFocus();
	    setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
	}
	
	public void mousePressed(MouseEvent evt)
	{
	    FileDiffViewPanel.pActiveView = this;
	    this.requestFocus();
	    pointCursor.x = evt.getX() + dx;
	    pointCursor.y = evt.getY() + dy;
		boolean bShift = evt.isShiftDown();
		boolean bControl = evt.isControlDown();
						
		setCursor(pointCursor, bShift, bControl);
	}
	
	public void mouseReleased(MouseEvent evt)
	{	        
	}
	
	public void mouseEntered(MouseEvent evt)
	{
	    this.requestFocus();	    
	}
	
	public void mouseExited(MouseEvent evt)
	{	    
	}
	
	public void mouseMoved(MouseEvent evt)
	{
	    this.requestFocus();
	    setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
	}
	
	public void mouseDragged(MouseEvent evt)
	{
        pointCursor.x = evt.getX() + dx;
        pointCursor.y = evt.getY() + dy;
        
        boolean bReverse = false;

        if (pointCursor.y > this.m_ptCursorPos.y)
        {
            bReverse = true;
        }
        else
        {
            if (pointCursor.x > m_ptCursorPos.x)
            {
                bReverse = true;
            }
            else
            {
                bReverse = false;
            }
        }
        
        if (bReverse)
        {
            this.SetSelection(m_ptCursorPos, ClientToText(pointCursor));
        }
        else
        {
            this.SetSelection(ClientToText(pointCursor), m_ptCursorPos);
        }
        
        UpdateCaret();
	}
	
	public void keyTyped(KeyEvent e)
	{
	    this.requestFocus();
	}
	
	public void keyPressed(KeyEvent e)
	{
	    this.requestFocus();
        int increment = 0;
        int direction = 0;
        boolean bHoriz = false;
        JScrollBar horiz = scrollFrame.getHoriz();
        JScrollBar vert = scrollFrame.getVert();
		boolean bShift = e.isShiftDown();
		boolean bControl = e.isControlDown();        
        
        switch(e.getKeyCode())
	    {
	    case KeyEvent.VK_LEFT:
	        increment = this.GetCharWidth();
	    	direction = -1;
	    	bHoriz = true;
	    	this.MoveLeft(bShift);
	    	break;
	    case KeyEvent.VK_RIGHT:
	        increment = this.GetCharWidth();
	    	bHoriz = true;
    		direction = 1;
    		this.MoveRight(bShift);
    		break;
	    case KeyEvent.VK_UP:
	        increment = vert.getUnitIncrement();
	    	direction = -1;
	    	this.MoveUp(bShift);
	    	break;
	    case KeyEvent.VK_DOWN:
	        increment = vert.getUnitIncrement();
    		direction = 1;
    		this.MoveDown(bShift);
    		break;    		
	    case KeyEvent.VK_PAGE_UP:
	        increment = vert.getBlockIncrement();
	    	direction = -1;
	    	this.MovePgUp((bShift & bControl));
	    	break;
	    case KeyEvent.VK_PAGE_DOWN:
	        increment = vert.getBlockIncrement();
    		direction = 1;
    		this.MovePgDn((bShift & bControl));
    		break;
	    case KeyEvent.VK_HOME:
	        if (bControl)
	        {
	            increment = vert.getValue();
	            direction = -1;
	        }
	        this.MoveCtrlHome(bShift);
	    	break;
	    case KeyEvent.VK_END:
	        if (bControl)
	        {
	            increment = vert.getMaximum() - vert.getValue();
	            direction = 1;
	        }
	        this.MoveCtrlEnd(bShift);
    		break; 
    	}
        
        scrollFrame.keyMoved(bHoriz, increment, direction);
	}
	
	public void keyReleased(KeyEvent e)
	{
	    this.requestFocus();
	}
	
	public int GetScreenLines()
	{
		if (m_nScreenLines == -1)
	    {
			m_nScreenLines = this.getSize().height/GetLineHeight();
	    }
		return m_nScreenLines;
	}
	
	boolean IsReadOnly()
	{
	    if (m_pTextBuffer == null)
	    {
	        return true;
	    }
	    
	    if (m_pTextBuffer.m_bInit)
	    {
	        return m_pTextBuffer.GetReadOnly();
	    }
	    
	    return true;
	}
	
	void AdjustTextPoint(CPoint point)
	{
		point.x += GetCharWidth () / 2;   //todo
	}
	
	void ScrollToLine(int nNewTopLine, boolean bNoSmoothScroll, boolean bTrackScrollBar)
	{
		if(m_nTopLine != nNewTopLine)
		{
			ScrollToSubLine(GetSubLineIndex(nNewTopLine), bNoSmoothScroll, bTrackScrollBar);
		}
	}

	void ScrollToSubLine(int nNewTopSubLine, boolean bNoSmoothScroll, boolean bTrackScrollBar)
	{
		if (m_nTopSubLine != nNewTopSubLine)
	    {/*
			if (bNoSmoothScroll || ! m_bSmoothScroll)
	        {
				int nScrollLines = m_nTopSubLine - nNewTopSubLine;
				m_nTopSubLine = nNewTopSubLine;
				// OnDraw() uses m_nTopLine to determine topline
				m_nTopLine = m_nTopSubLine;
				ScrollWindow(0, nScrollLines * GetLineHeight());
				UpdateWindow();
				if (bTrackScrollBar)
				{
					RecalcVertScrollBar(TRUE);
				}
	        }
			else
	        {
				// Do smooth scrolling
				int nLineHeight = GetLineHeight();
				if (m_nTopSubLine > nNewTopSubLine)
	            {
					int nIncrement = (m_nTopSubLine - nNewTopSubLine)/SMOOTH_SCROLL_FACTOR + 1;
					while (m_nTopSubLine != nNewTopSubLine)
	                {
						int nTopSubLine = m_nTopSubLine - nIncrement;
						if (nTopSubLine < nNewTopSubLine)
						{
							nTopSubLine = nNewTopSubLine;
						}
						int nScrollLines = nTopSubLine - m_nTopSubLine;
						m_nTopSubLine = nTopSubLine;
						ScrollWindow(0, - nLineHeight * nScrollLines);
						UpdateWindow();
						if (bTrackScrollBar)
						{
							RecalcVertScrollBar(TRUE);
						}
	                }
	            }
				else
	            {
					int nIncrement = (nNewTopSubLine - m_nTopSubLine)/SMOOTH_SCROLL_FACTOR + 1;
					while (m_nTopSubLine != nNewTopSubLine)
	                {
						int nTopSubLine = m_nTopSubLine + nIncrement;
						if (nTopSubLine > nNewTopSubLine)
						{
							nTopSubLine = nNewTopSubLine;
						}
						int nScrollLines = nTopSubLine - m_nTopSubLine;
						m_nTopSubLine = nTopSubLine;
						ScrollWindow(0, - nLineHeight * nScrollLines);
						UpdateWindow();
						if (bTrackScrollBar)
						{
							RecalcVertScrollBar(TRUE);
						}
	                }
	            }
	        }
			int nDummy;
			GetLineBySubLine(m_nTopSubLine, m_nTopLine, nDummy);
			InvalidateRect(NULL);	// repaint whole window*/
	    }
	}
	
	void drawSelection(Graphics g)
	{
	    if (!m_ptDrawSelStart.equals(m_ptDrawSelEnd))
	    {
	        g.setColor(Color.black);
            g.setXORMode(Color.white);
            CPoint ptDrawSelStart = TextToClient(m_ptDrawSelStart);
            CPoint ptDrawSelEnd = TextToClient(m_ptDrawSelEnd);
            int iHeight = m_ptDrawSelEnd.y - m_ptDrawSelStart.y;
            if (iHeight == 0)
            {
                g.fillRect(ptDrawSelStart.x, ptDrawSelStart.y,
                            ptDrawSelEnd.x - ptDrawSelStart.x,
                            m_nLineHeight);
            }
            else
            {
                g.fillRect(ptDrawSelStart.x, ptDrawSelStart.y,
                        getSize().width - ptDrawSelStart.x, m_nLineHeight);
                
                for (int i = m_ptDrawSelStart.y + 1; i < (m_ptDrawSelStart.y + iHeight); i++)
                {
                    g.fillRect(0, i * m_nLineHeight, getSize().width,  m_nLineHeight);     
                }
                
                g.fillRect(0, ptDrawSelEnd.y, ptDrawSelEnd.x, m_nLineHeight);                
            }
        }
	}
	
	void SetSelection(CPoint ptStart, CPoint ptEnd)
	{
		// for select the whole line
		CPoint ptSelStart = ptStart;
		CPoint ptSelEnd = ptEnd;

		if (m_ptSelStart.equals(ptSelStart))
	    {
			if (!m_ptSelEnd.equals(ptSelEnd))
			{
			    Graphics g = this.getGraphics();
			    g.setColor(Color.blue);
			    g.setXORMode(Color.black);
			    g.fillRect(ptSelEnd.x, ptSelEnd.y, m_ptSelEnd.x - ptSelEnd.x, m_ptSelEnd.y - ptSelEnd.y);
				InvalidateLines(ptSelEnd.y, m_ptSelEnd.y, false);
			}
	    }
		else
	    {	
		    Graphics g = this.getGraphics();
		    g.setColor(Color.blue);
	    	g.setXORMode(Color.black);
			InvalidateLines(ptSelStart.y, ptSelEnd.y, false);
			InvalidateLines(m_ptSelStart.y, m_ptSelEnd.y, false);
	    }
		m_ptSelStart.copy(ptSelStart);
		m_ptSelEnd.copy(ptSelEnd);
	}
	
	void SetCursorPos(CPoint ptCursorPos)
	{
		m_ptCursorPos.copy(ptCursorPos);
		m_nIdealCharPos = CalculateActualOffset(m_ptCursorPos.y, m_ptCursorPos.x);
		UpdateCaret();
	}
	
	void GetSelection(CPoint ptStart, CPoint ptEnd)
	{
		PrepareSelBounds();
		ptStart.copy(m_ptDrawSelStart);
		ptEnd.copy(m_ptDrawSelEnd);
	}
	
	void EnsureVisible(CPoint pt)
	{
	    
	}
	
	void ScrollToVisible(CPoint pt)
	{
	    boolean bHoriz = false;
	    CPoint ptTemp = this.TextToClient(pt);
	    int direction = -1;
	    int increment = 0;
	    JScrollBar horiz = scrollFrame.getHoriz();
        JScrollBar vert = scrollFrame.getVert();
        int iVert = vert.getValue();
        int iHoriz = horiz.getValue();
		
        // Scroll vertically
        if (ptTemp.y > iVert)
        {
            direction = 1;
            increment = ptTemp.y - iVert;
            scrollFrame.keyMoved(bHoriz, increment, direction);
        }
        else
        {
            direction = -1;
            increment = iVert - ptTemp.y;
            scrollFrame.keyMoved(bHoriz, increment, direction);
        }
		
		// Scroll horizontally
		// we do not need horizontally scrolling, if we wrap the words
        if (ptTemp.x > (iHoriz + getSize().width))
        {
            bHoriz = true;
            direction = 1;
            increment = ptTemp.x - iHoriz - getSize().width;
            scrollFrame.keyMoved(bHoriz, increment, direction);
        }
        else if (ptTemp.x < getSize().width && iHoriz > 0)
        {
            bHoriz = true;
            direction = -1;
            increment = -iHoriz;
            scrollFrame.keyMoved(bHoriz, increment, direction);
        }      
	}
	
//	 This function assumes selection is in one line
	void EnsureVisible(CPoint ptStart, CPoint ptEnd)
	{/*
		// Scroll vertically
		int nSubLineCount = GetSubLineCount();
		int nNewTopSubLine = m_nTopSubLine;
		
		CharPosToPoint(ptStart.y, ptStart.x, subLinePos);
		subLinePos.y += GetSubLineIndex(ptStart.y);
		CharPosToPoint(ptEnd.y, ptEnd.x, subLinePosEnd);
		subLinePosEnd.y += GetSubLineIndex(ptEnd.y);
		
		if (subLinePos.y >= nNewTopSubLine + GetScreenLines())
		{
			nNewTopSubLine = subLinePos.y - GetScreenLines() + 1;
		}
		if (subLinePos.y < nNewTopSubLine)
		{
			nNewTopSubLine = subLinePos.y;
		}
		
		if (nNewTopSubLine < 0)
		{
			nNewTopSubLine = 0;
		}
		if (nNewTopSubLine >= nSubLineCount)
		{
			nNewTopSubLine = nSubLineCount - 1;
		}
		
		// This line fixes (cursor) slowdown after merges!
		// I don't know exactly why, but propably we are setting
		// m_nTopLine to zero in ResetView() and are not setting to
		// valid value again.  Maybe this is a good place to set it?
		m_nTopLine = nNewTopSubLine;
		
		if (nNewTopSubLine != m_nTopSubLine)
	    {
			ScrollToSubLine(nNewTopSubLine, false, true);
			UpdateCaret();
			//UpdateSiblingScrollPos(false);
	    }
		
		// Scroll horizontally
		// we do not need horizontally scrolling, if we wrap the words
		if (m_bWordWrap)
		{
			return;
		}

		int nActualPos = CalculateActualOffset(ptStart.y, ptStart.x);
		int nActualEndPos = CalculateActualOffset(ptEnd.y, ptEnd.x);
		int nNewOffset = m_nOffsetChar;
		int nScreenChars = GetScreenChars();
		int nBeginOffset = nActualPos - m_nOffsetChar;
		int nEndOffset = nActualEndPos - m_nOffsetChar;
		int nSelLen = nActualEndPos - nActualPos;
		
		// Selection fits to screen, scroll whole selection visible
		if (nSelLen < nScreenChars)
	    {
			// Begin of selection not visible 
			if (nBeginOffset > nScreenChars)
	        {
				// Scroll so that there is max 5 chars margin at end
				if (nScreenChars - nSelLen > 5)
				{
					nNewOffset = nActualPos + 5 - nScreenChars + nSelLen;
				}
				else
				{
					nNewOffset = nActualPos - 5;
				}
	        }
			else if (nBeginOffset < 0)
	        {
				// Scroll so that there is max 5 chars margin at begin
				if (nScreenChars - nSelLen >= 5)
				{
					nNewOffset = nActualPos - 5;
				}
				else
				{
					nNewOffset = nActualPos - 5 - nScreenChars + nSelLen;
				}
	        }
			// End of selection not visible
			else if (nEndOffset > nScreenChars ||
				nEndOffset < 0)
	        {
				nNewOffset = nActualPos - 5;
	        }
		}
		else // Selection does not fit screen so scroll to begin of selection
	    {
			nNewOffset = nActualPos - 5;
	    }
		
		// Horiz scroll limit to longest line + one screenwidth
		int nMaxLineLen = GetMaxLineLength();
		if (nNewOffset >= nMaxLineLen + nScreenChars)
		{
			nNewOffset = nMaxLineLen + nScreenChars - 1;
		}
		if (nNewOffset < 0)
		{
			nNewOffset = 0;
		}
		
		if (m_nOffsetChar != nNewOffset)
	    {
			//ScrollToChar(nNewOffset);
			UpdateCaret();
			//UpdateSiblingScrollPos(true);
	    }*/
	}
	
	boolean IsTextBufferInitialized()
	{
		return (m_pTextBuffer != null && m_pTextBuffer.m_bInit); 
	}
	
	CPoint GetCursorPos()
	{
		return m_ptCursorPos;
	}
	
	void InvalidateLines(int nLine1, int nLine2, boolean bInvalidateMargin)
	{
		bInvalidateMargin = true;
		int nLineHeight = GetLineHeight();
		CRect rcInvalid = rcClient;
		if (nLine2 == -1)
	    {
			if (!bInvalidateMargin)
			{
				rcInvalid.left += GetMarginWidth();
			}
			rcInvalid.top = (GetSubLineIndex( nLine1 ) - m_nTopSubLine) * nLineHeight;
			repaint(rcInvalid.left, rcInvalid.top, rcInvalid.Width(), rcInvalid.Height());
	    }
		else
	    {
			if (nLine2 < nLine1)
	        {
				int nTemp = nLine1;
				nLine1 = nLine2;
				nLine2 = nTemp;
	        }
			if (!bInvalidateMargin)
			{
				rcInvalid.left += GetMarginWidth();
			}
			rcInvalid.top = (GetSubLineIndex(nLine1) - m_nTopSubLine) * nLineHeight;
			rcInvalid.bottom = (GetSubLineIndex(nLine2) - m_nTopSubLine + GetSubLines(nLine2)) * nLineHeight;
			repaint(rcInvalid.left, rcInvalid.top, rcInvalid.Width(), rcInvalid.Height());
	    }
	}
	
	int CalculateActualOffset(int nLineIndex, int nCharIndex)
	{
		int nLength = GetLineLength(nLineIndex);
		char[] pszChars = GetLineChars(nLineIndex);
		int nTabSize = GetTabSize();
		int nBreaks = 0;
		int nOffset = 0;
		
		m_nBreaks.setIntValue(0);
		WrapLineCached(nLineIndex, GetScreenChars(), null, m_nBreaks);
		nBreaks = m_nBreaks.getIntValue();
		
		int nPreOffset = 0;
		int nPreBreak = 0;
		
		if(nBreaks > 0)
		{
		    int j = 0;
			for( j = nBreaks - 1; j >= 0 && nCharIndex < anBreaks[j]; j-- );
			nPreBreak = anBreaks[j];
		}

		int i = 0;
		for (i = 0; i < nCharIndex; i++)
	    {
			if(nPreBreak == i && nBreaks > 0)
			{
				nPreOffset = nOffset;
			}
			if (pszChars[i] == '\t')
			{
				nOffset += (nTabSize - nOffset % nTabSize);
			}
			else
			{

	        nOffset++;
			}
	    }
		if( nPreBreak == i && nBreaks > 0)
		{
			return 0;
		}
		else
		{
			return (nOffset - nPreOffset);
		}
	}
	
	int CharPosToPoint(int nLineIndex, int nCharPos, CPoint charPoint)
	{
		// if we do not wrap lines, y is allways 0 and x is equl to nCharPos
		if (!m_bWordWrap)
	    {
			charPoint.x = nCharPos;
			charPoint.y = 0;
	    }
		
		// line is wrapped
		int	nBreaks = 0;
		
		m_nBreaks.setIntValue(0);
		WrapLineCached(nLineIndex, GetScreenChars(), null, m_nBreaks);
		nBreaks = m_nBreaks.getIntValue();
		
		int i = 0;
		for (i = nBreaks - 1; i >= 0 && nCharPos < anBreaks[i]; i--);
		
		charPoint.x = (i >= 0) ? nCharPos - anBreaks[i] : nCharPos;
		charPoint.y = i + 1;
		
		int nReturnVal = (i >= 0) ? anBreaks[i] : 0;
		
		return nReturnVal;
	}

	CPoint ClientToText(final CPoint point)
	{
	    CPoint pt = new CPoint(0, 0);
		int nSubLineCount = GetSubLineCount();
		int nLineCount = GetLineCount();
		
		pt.zero();
		pt.y = m_nTopSubLine + point.y/GetLineHeight();
		if (pt.y >= nSubLineCount)
			pt.y = nSubLineCount - 1;
		if (pt.y < 0)
			pt.y = 0;
		
		int nOffsetChar = m_nOffsetChar;
		
		GetLineBySubLine(pt.y, nLine, nSubLineOffset);
		pt.y = nLine.getIntValue();
		
		char[] pszLine = null;
		int	nLength = 0;
		int	nBreaks = 0;
		
		if (pt.y >= 0 && pt.y < nLineCount)
	    {
			nLength = GetLineLength(pt.y);
			pszLine = GetLineChars(pt.y);
			
			m_nBreaks.setIntValue(0);
			WrapLineCached(pt.y, GetScreenChars(), null, m_nBreaks);
			nBreaks = m_nBreaks.getIntValue();
			
			if (nSubLineOffset.getIntValue() > 0)
			{
				nOffsetChar = anBreaks[nSubLineOffset.getIntValue() - 1];
			}
			if (nBreaks > nSubLineOffset.getIntValue())
			{
				nLength = anBreaks[nSubLineOffset.getIntValue()] - 1;
			}
	    }
		
		int nPos = nOffsetChar + (point.x - GetMarginWidth())/GetCharWidth();
		if (nPos < 0)
		{
			nPos = 0;
		}
		
		int nIndex = 0, nCurPos = 0, n = 0, i = 0;
		int nTabSize = GetTabSize();
		
		boolean bDBCSLeadPrev = false;  //DBCS support
		while (nIndex < nLength)
	    {
			if(nBreaks > 0 && nIndex == anBreaks[i])
	        {
				n = nIndex;
				i++;
	        }
			
			if (pszLine[nIndex] == '\t')
	        {
				int nOffset = nTabSize - nCurPos % nTabSize;
				n += nOffset;
				nCurPos += nOffset;
	        }
			else
	        {
				n++;
				nCurPos ++;
	        }
			
			if (n > nPos && i == nSubLineOffset.getIntValue())
	        {
				if(bDBCSLeadPrev)
				{
					nIndex--;
				}
				break;
	        }
			
			if(bDBCSLeadPrev)
			{
				bDBCSLeadPrev=false;
			}
			else
			{
				//bDBCSLeadPrev = IsDBCSLeadByte(pszLine[nIndex]);
			}
			
			nIndex ++;
	    }
		
		pt.x = nIndex;
		return pt;
	}
	
	public void Copy()
	{
		if (m_ptSelStart.equals(m_ptSelEnd))
		{
			return;
		}
		
		PrepareSelBounds();
		String strText = "";
		m_strText.setStringValue("");
		GetText(m_ptDrawSelStart, m_ptDrawSelEnd, m_strText);
		strText = m_strText.getStringValue();
		StringSelection selection = new StringSelection(strText);
		textClipboard.setContents(selection, null);
	}
	
	void GetText(CPoint ptStart, CPoint ptEnd, BaseDataObject strText)
	{
		if (m_pTextBuffer != null)
		{
			m_pTextBuffer.GetText(ptStart.y, ptStart.x, ptEnd.y, ptEnd.x, strText, null);
		}
		else
		{
			strText.setStringValue("");
		}
	}

	int CursorPointToCharPos(int nLineIndex, CPoint curPoint)
	{
		// calculate char pos out of point
		int nLength = GetLineLength(nLineIndex);
		int nScreenChars = GetScreenChars();
		char[]	szLine = GetLineChars(nLineIndex);
		
		// wrap line
		int	nBreaks = 0;
		
		m_nBreaks.setIntValue(0);
		WrapLineCached(nLineIndex, nScreenChars, null, m_nBreaks);
		m_nBreaks.getIntValue();
		
		// find char pos that matches cursor position
		int nXPos = 0;
		int nYPos = 0;
		int	nCurPos = 0;
		int nTabSize = GetTabSize();
		
		boolean bDBCSLeadPrev = false;  //DBCS support
		int nIndex = 0;
		for(nIndex = 0; nIndex < nLength; nIndex++)
	    {
			if(nBreaks > 0 && nIndex == anBreaks[nYPos])
	        {
				nXPos = 0;
				nYPos++;
	        }
			
			if (szLine[nIndex] == '\t')
	        {
				int nOffset = nTabSize - nCurPos % nTabSize;
				nXPos += nOffset;
				nCurPos += nOffset;
	        }
			else
	        {
				nXPos++;
				nCurPos++;
	        }
			
			if(nXPos > curPoint.x && nYPos == curPoint.y)
	        {
				if(bDBCSLeadPrev)
				{
					nIndex--;
				}
				break;
	        }
			else if(nYPos > curPoint.y)
	        {
				nIndex--;
				break;
	        }
			
			if(bDBCSLeadPrev)
			{
				bDBCSLeadPrev=false;
			}
			else
			{
				//bDBCSLeadPrev = IsDBCSLeadByte(szLine[nIndex]);
			}
	    }
		
		return nIndex;	
	}
	
	int GetTabSize()
	{
		if (m_pTextBuffer == null || !m_pTextBuffer.m_bInit)
		{
			return 4;	
		}
		
		return m_pTextBuffer.GetTabSize();
	}
	
	int HowManyStr(String s, char c)
	{
		int n = 0;
		for (int i = 0; i < s.length(); i ++)
	    {
		    if (s.charAt(i) == c)
		    {
		        n++;
		    }
	    }
		return n;
	}
	
	int GetCRLFMode()
	{
		if (m_pTextBuffer != null)
	    {
			return m_pTextBuffer.GetCRLFMode ();
	    }
		return -1;
	}

	long GetFlags()
	{
		return m_dwFlags;
	}
	
	void GetFullySelectedLines(BaseDataObject nFirstLine, BaseDataObject nLastLine)
	{
		GetSelection(ptStart, ptEnd);
		
		if (ptStart.x == 0)
		{
			nFirstLine.setIntValue(ptStart.y);
		}
		else
		{
			nFirstLine.setIntValue(ptStart.y + 1);
		}
		if (ptEnd.x == GetLineLength(ptEnd.y))
		{
			nLastLine.setIntValue(ptEnd.y);
		}
		else
		{
			nLastLine.setIntValue(ptEnd.y-1);
		}
	}
	
//	 Get the line length, for cursor movement 
//	 there are at least 4 line lengths :
//	 - number of characters (memory, no EOL)
//	 - number of characters (memory, with EOL)
//	 - number of characters for cursor position (tabs are expanded, no EOL)
//	 - number of displayed characters (tabs are expanded, with EOL)
//	 Corresponding functions :
//	 - GetLineLength
//	 - GetFullLineLength
//	 - GetLineActualLength
//	 - ExpandChars (returns the line to be displayed as a CString)
	int GetLineActualLength(int nLineIndex)
	{
		int nLineCount = GetLineCount ();

		//  Actual line length is not determined yet, let's calculate a little
		int nActualLength = 0;
		int nLength = GetLineLength (nLineIndex);
		if (nLength > 0)
	    {
			char[] pszLine = GetLineChars(nLineIndex);
			char[] pszChars = new char[nLength + 1];
			if (pszChars == null)
			{
				return 0;		// TODO: what to do if alloc fails...???
			}
			CMemoryOperator.CopyMemory(pszChars, pszLine, nLength);
			pszChars[nLength] = 0;
			char[] pszCurrent = pszChars;
			
			int nTabSize = GetTabSize ();
			int ind = 0;
			
			while (ind < nLength)
	        {
				if (pszCurrent[ind] == '\t')
				{
					nActualLength += (nTabSize - nActualLength % nTabSize);
				}
				else
				{
					nActualLength++;
				}
				
				ind++;
	        }
	    }
		
		return nActualLength;
	}
	
	void GetLineBySubLine(int nSubLineIndex, BaseDataObject nLine, BaseDataObject nSubLine)
	{
		// if we do not wrap words, nLine is equal to nSubLineIndex and nSubLine is allways 0
		if(!m_bWordWrap)
	    {
			nLine.setIntValue(nSubLineIndex);
			nSubLine.setIntValue(0);
	    }
	}
	
	int GetMaxLineLength()
	{
		if (m_nMaxLineLength == -1)
	    {
			m_nMaxLineLength = 0;
			int nLineCount = GetLineCount();
			for (int I = 0; I < nLineCount; I++)
	        {
				int nActualLength = GetLineActualLength (I);
				if (m_nMaxLineLength < nActualLength)
				{
					m_nMaxLineLength = nActualLength;
				}
	        }
	    }
		return m_nMaxLineLength;
	}
	
	boolean GetSelectionMargin()
	{
		return m_bSelMargin;
	}
	
	int GetSubLineCount()
	{
		int nLineCount = GetLineCount();
		
		// if we do not wrap words, number of sub lines is
		// equal to number of lines
		if (!m_bWordWrap)
		{
			return nLineCount;
		}
		
		return 0;
	}
	
	int GetSubLines(int nLineIndex)
	{
	   // get number of wrapped lines, this line contains of
	  int nBreaks = 0;
	  m_nBreaks.setIntValue(0);
	  WrapLineCached(nLineIndex, GetScreenChars(), null, m_nBreaks);

	  return (m_nBreaks.getIntValue() + 1);
	}	
	
	String GetTextBufferEol(int nLine)
	{
		return m_pTextBuffer.GetLineEol(nLine); 
	}

	boolean GetWordWrapping()
	{
		return m_bWordWrap;
	}
	
	void GoToLine(int nLine, boolean bRelative)
	{
		int nLines = m_pTextBuffer.GetLineCount() - 1;
		CPoint ptCursorPos = GetCursorPos();
		if (bRelative)
	    {
			nLine += ptCursorPos.y;
	    }
		if (nLine > 0)
	    {
			nLine--;
	    }
		if (nLine > nLines)
	    {
			nLine = nLines;
	    }
		if (nLine >= 0)
	    {
			int nChars = m_pTextBuffer.GetLineLength(nLine);
			if (nChars > 0)
	        {
				nChars--;
	        }
			if (ptCursorPos.x > nChars)
	        {
				ptCursorPos.x = nChars;
	        }
			if (ptCursorPos.x >= 0)
	        {
				ptCursorPos.y = nLine;
				//SetAnchor(ptCursorPos);
				SetSelection(ptCursorPos, ptCursorPos);
				SetCursorPos(ptCursorPos);
				EnsureVisible(ptCursorPos);
	        }
	    }
	}

	void HideCursor()
	{
		m_bCursorHidden = true;
		UpdateCaret();
	}
	

	void ShowCursor()
	{
		m_bCursorHidden = false;
		UpdateCaret();
	}
	
	void UpdateCaret()
	{
		if (isFocusOwner() && !m_bCursorHidden &&
	        CalculateActualOffset(m_ptCursorPos.y, m_ptCursorPos.x) >= m_nOffsetChar)
	    {
			CPoint temp = TextToClient(m_ptCursorPos);
			rcOldCaret.copy(rcNewCaret);
			rcNewCaret.setRect(temp.x, temp.y, temp.x, temp.y + GetLineHeight());
			//drawCaret();
			repaint();
			//ShowCaret();
	    }
		else
	    {
			//HideCaret();
	    }
		
		OnUpdateCaret();
	}
	
	void OnUpdateCaret()
	{
	}
	
	CPoint TextToClient(final CPoint point)
	{
	   if (m_pTextBuffer == null || !m_pTextBuffer.m_bInit)
	   {
	       return new CPoint(0, 0);
	   }
	       
	    CPoint pt = new CPoint(0, 0);
	    pt.zero();
	    m_charPoint.zero();
		char[] pszLine = GetLineChars(point.y);
		
		int	nSubLineStart = CharPosToPoint(point.y, point.x, m_charPoint);
		m_charPoint.y += GetSubLineIndex(point.y);
		
		// compute y-position
		pt.y = (m_charPoint.y - m_nTopSubLine) * GetLineHeight();
		
		// if pt.x is null, we know the result
		if (m_charPoint.x == 0)
	    {
			pt.x = GetMarginWidth();
			return pt;
	    }
		
		// we have to calculate x-position
		int	nPreOffset = 0;
		pt.x = 0;
		
		int nTabSize = GetTabSize();
		for (int nIndex = 0; nIndex < point.x; nIndex++)
	    {
			if (nIndex == nSubLineStart)
			{
				nPreOffset = pt.x;
			}
			
			if (pszLine[nIndex] == '\t')
			{
				pt.x += (nTabSize - pt.x % nTabSize);
			}
			else
			{
				pt.x++;
			}
	    }
		
		pt.x -= nPreOffset;
		
		pt.x = (pt.x - m_nOffsetChar) * GetCharWidth() + GetMarginWidth();

		return pt;
	}
	
	boolean HighlightText(CPoint ptStartPos, int nLength, boolean bReverse)
	{
		CPoint ptEndPos = new CPoint(0, 0);
		ptEndPos.copy(ptStartPos);
		int nCount = GetLineLength(ptEndPos.y) - ptEndPos.x;
		if (nLength <= nCount)
	    {
			ptEndPos.x += nLength;
	    }
		else
	    {
			while (nLength > nCount)
	        {
				nLength -= nCount + 1;
				nCount = GetLineLength (++ptEndPos.y);
	        }
			ptEndPos.x = nLength;
	    }
		
		m_ptCursorPos.copy(bReverse ? ptStartPos : ptEndPos);
		m_ptAnchor.copy(m_ptCursorPos);
		SetSelection(ptStartPos, ptEndPos);
		UpdateCaret();
		ScrollToVisible(m_ptCursorPos);

		return true;
	}
	
	boolean IsInsideSelBlock(CPoint ptTextPos)
	{
		PrepareSelBounds();
		if (ptTextPos.y < m_ptDrawSelStart.y)
		{
			return false;
		}
		if (ptTextPos.y > m_ptDrawSelEnd.y)
		{
			return false;
		}
		if (ptTextPos.y < m_ptDrawSelEnd.y && ptTextPos.y > m_ptDrawSelStart.y)
		{
			return true;
		}
		if (m_ptDrawSelStart.y < m_ptDrawSelEnd.y)
	    {
			if (ptTextPos.y == m_ptDrawSelEnd.y)
			{
				return ptTextPos.x < m_ptDrawSelEnd.x;
			}
			return ptTextPos.x >= m_ptDrawSelStart.x;
	    }
		return (ptTextPos.x >= m_ptDrawSelStart.x && ptTextPos.x < m_ptDrawSelEnd.x);
	}

	boolean IsInsideSelection(CPoint ptTextPos)
	{
		PrepareSelBounds();
		return IsInsideSelBlock(ptTextPos);
	}

	boolean IsSelection()
	{
		return !m_ptSelStart.equals(m_ptSelEnd);
	}
	
	boolean IsValidTextPos(CPoint point)
	{
		return (GetLineCount() > 0 && m_nTopLine >= 0 && m_nOffsetChar >= 0 &&
				point.y >= 0 && point.y < GetLineCount() && point.x >= 0 && 
				point.x <= GetLineLength(point.y));
	}

	boolean IsValidTextPosX(CPoint point)
	{
		return (GetLineCount() > 0 && m_nTopLine >= 0 && m_nOffsetChar >= 0 &&
				point.y >= 0 && point.y < GetLineCount() && point.x >= 0 && 
				point.x <= GetLineLength(point.y));
	}

	boolean IsValidTextPosY(CPoint point)
	{
		return (GetLineCount() > 0 && m_nTopLine >= 0 && m_nOffsetChar >= 0 &&
				point.y >= 0 && point.y < GetLineCount());
	}
	
	void MoveCtrlEnd(boolean bSelect)
	{
		m_ptCursorPos.y = GetLineCount() - 1;
		m_ptCursorPos.x = GetLineLength(m_ptCursorPos.y);
		m_nIdealCharPos = CalculateActualOffset(m_ptCursorPos.y, m_ptCursorPos.x);
		EnsureVisible(m_ptCursorPos);
		UpdateCaret();

		if (!bSelect)
		{
			m_ptAnchor.copy(m_ptCursorPos);
		}
		SetSelection(m_ptAnchor, m_ptCursorPos);
	}

	void MoveCtrlHome(boolean bSelect)
	{
		m_ptCursorPos.x = 0;
		m_ptCursorPos.y = 0;
		m_nIdealCharPos = CalculateActualOffset(m_ptCursorPos.y, m_ptCursorPos.x);
		EnsureVisible(m_ptCursorPos);
		UpdateCaret();

		if (!bSelect)
		{
			m_ptAnchor.copy(m_ptCursorPos);
		}
		SetSelection(m_ptAnchor, m_ptCursorPos);
	}

	void MoveDown(boolean bSelect)
	{
		PrepareSelBounds();
		if (!m_ptDrawSelStart.equals(m_ptDrawSelEnd) && !bSelect)
		{
			m_ptCursorPos.copy(m_ptDrawSelEnd);
		}
		
		CPoint subLinePos = new CPoint(0, 0);
		CharPosToPoint(m_ptCursorPos.y, m_ptCursorPos.x, subLinePos);
		
		int	nSubLine = GetSubLineIndex(m_ptCursorPos.y) + subLinePos.y;
		
		if (nSubLine < GetSubLineCount() - 1)
		{
			if (m_nIdealCharPos == -1)
			{
				m_nIdealCharPos = CalculateActualOffset(m_ptCursorPos.y, m_ptCursorPos.x);
			}

			SubLineCursorPosToTextPos(new CPoint( m_nIdealCharPos, nSubLine + 1 ), m_ptCursorPos);

			if (m_ptCursorPos.x > GetLineLength (m_ptCursorPos.y))
			{
				m_ptCursorPos.x = GetLineLength (m_ptCursorPos.y);
			}
	    }

		EnsureVisible(m_ptCursorPos);
		UpdateCaret();
		if (!bSelect)
		{
			m_ptAnchor.copy(m_ptCursorPos);
		}
		SetSelection (m_ptAnchor, m_ptCursorPos);
	}

	void MoveEnd(boolean bSelect)
	{
		CPoint	pos = new CPoint(0, 0);
		CharPosToPoint(m_ptCursorPos.y, m_ptCursorPos.x, pos);
		m_ptCursorPos.x = SubLineEndToCharPos(m_ptCursorPos.y, pos.y);
		m_nIdealCharPos = CalculateActualOffset(m_ptCursorPos.y, m_ptCursorPos.x);
		EnsureVisible(m_ptCursorPos);
		UpdateCaret();

		if (!bSelect)
		{
			m_ptAnchor.copy(m_ptCursorPos);
		}

		SetSelection(m_ptAnchor, m_ptCursorPos);
	}

	void MoveHome(boolean bSelect)
	{
		int nLength = GetLineLength(m_ptCursorPos.y);
		char[] pszChars = GetLineChars(m_ptCursorPos.y);

		CPoint pos = new CPoint(0, 0);
		CharPosToPoint(m_ptCursorPos.y, m_ptCursorPos.x, pos);

		int nHomePos = SubLineHomeToCharPos(m_ptCursorPos.y, pos.y);
		int nOriginalHomePos = nHomePos;
		while (nHomePos < nLength && pszChars[nHomePos] == ' ')
		{
			nHomePos++;
		}
		if (nHomePos == nLength || m_ptCursorPos.x == nHomePos)
		{
			m_ptCursorPos.x = nOriginalHomePos;
		}
		else
		{
			m_ptCursorPos.x = nHomePos;
		}

		m_nIdealCharPos = CalculateActualOffset(m_ptCursorPos.y, m_ptCursorPos.x);
		EnsureVisible(m_ptCursorPos);
		UpdateCaret();

		if (!bSelect)
		{
			m_ptAnchor.copy(m_ptCursorPos);
		}
		SetSelection(m_ptAnchor, m_ptCursorPos);
	}

	void MoveLeft(boolean bSelect)
	{
		PrepareSelBounds();
		if (!m_ptDrawSelStart.equals(m_ptDrawSelEnd) && !bSelect)
	    {
			m_ptCursorPos.copy(m_ptDrawSelStart);
	    }
		else
	    {
			if (m_ptCursorPos.x == 0)
	        {
				if (m_ptCursorPos.y > 0)
	            {
					m_ptCursorPos.y--;
					m_ptCursorPos.x = GetLineLength(m_ptCursorPos.y);
	            }
	        }
			else
	        {
				m_ptCursorPos.x--;
				/*if (m_pTextBuffer.IsMBSTrail(m_ptCursorPos.y, m_ptCursorPos.x) &&
	                // here... if its a MBSTrail, then should move one character more....
	                m_ptCursorPos.x > 0)
				{
					m_ptCursorPos.x--;
				}*/
	        }
	    }
		m_nIdealCharPos = CalculateActualOffset(m_ptCursorPos.y, m_ptCursorPos.x);
		EnsureVisible(m_ptCursorPos);
		UpdateCaret();
		if (!bSelect)
		{
			m_ptAnchor.copy(m_ptCursorPos);
		}

		SetSelection(m_ptAnchor, m_ptCursorPos);
	}

	void MovePgDn(boolean bSelect)
	{
		// scrolling windows
		int nNewTopSubLine = m_nTopSubLine + GetScreenLines() - 1;
		int nSubLineCount = GetSubLineCount();
		
		if (nNewTopSubLine > nSubLineCount)
		{
			nNewTopSubLine = nSubLineCount - 1;
		}
		if (m_nTopSubLine != nNewTopSubLine)
		{
			//ScrollToSubLine(nNewTopSubLine);
			//UpdateSiblingScrollPos(false);
		}
		
		// setting cursor
		CPoint subLinePos = new CPoint(0, 0);
		CharPosToPoint(m_ptCursorPos.y, m_ptCursorPos.x, subLinePos);
		
		int	nSubLine = GetSubLineIndex(m_ptCursorPos.y) + subLinePos.y + GetScreenLines() - 1;
		
		if (nSubLine > nSubLineCount - 1)
		{
			nSubLine = nSubLineCount - 1;
		}
		
		SubLineCursorPosToTextPos(new CPoint(m_nIdealCharPos, nSubLine), m_ptCursorPos);

		m_nIdealCharPos = CalculateActualOffset(m_ptCursorPos.y, m_ptCursorPos.x);
		EnsureVisible(m_ptCursorPos);    //todo: no vertical scroll
		
		UpdateCaret();
		if (!bSelect)
		{
			m_ptAnchor.copy(m_ptCursorPos);
		}

		SetSelection(m_ptAnchor, m_ptCursorPos);
	}

	void MovePgUp(boolean bSelect)
	{
		// scrolling windows
		int nNewTopSubLine = m_nTopSubLine - GetScreenLines() + 1;
		if (nNewTopSubLine < 0)
		{
			nNewTopSubLine = 0;
		}
		if (m_nTopSubLine != nNewTopSubLine)
		{
			//ScrollToSubLine(nNewTopSubLine);
			//UpdateSiblingScrollPos(false);
		}
		
		// setting cursor
		CPoint subLinePos = new CPoint(0, 0);
		CharPosToPoint(m_ptCursorPos.y, m_ptCursorPos.x, subLinePos);
		
		int	nSubLine = GetSubLineIndex(m_ptCursorPos.y) + subLinePos.y - GetScreenLines() + 1;
		
		if (nSubLine < 0)
		{
			nSubLine = 0;
		}
		
		SubLineCursorPosToTextPos(new CPoint(m_nIdealCharPos, nSubLine), m_ptCursorPos);
		
		m_nIdealCharPos = CalculateActualOffset(m_ptCursorPos.y, m_ptCursorPos.x);
		EnsureVisible(m_ptCursorPos);    //todo: no vertical scroll
		
		UpdateCaret();
		if (!bSelect)
		{
			m_ptAnchor.copy(m_ptCursorPos);
		}

		SetSelection(m_ptAnchor, m_ptCursorPos);
	}

	void MoveRight(boolean bSelect)
	{
		PrepareSelBounds();
		if (!m_ptDrawSelStart.equals(m_ptDrawSelEnd) && !bSelect)
	    {
			m_ptCursorPos.copy(m_ptDrawSelEnd);
	    }
		else
	    {
			if (m_ptCursorPos.x == GetLineLength(m_ptCursorPos.y))
	        {
				if (m_ptCursorPos.y < GetLineCount() - 1)
	            {
					m_ptCursorPos.y++;
					m_ptCursorPos.x = 0;
	            }
	        }
			else
	        {
				m_ptCursorPos.x++;
				/*if (m_pTextBuffer.IsMBSTrail(m_ptCursorPos.y, m_ptCursorPos.x) &&
	                // here... if its a MBSTrail, then should move one character more....
	                m_ptCursorPos.x < GetLineLength(m_ptCursorPos.y))
				{
					m_ptCursorPos.x++;
				}*/
	        }
	    }
		m_nIdealCharPos = CalculateActualOffset(m_ptCursorPos.y, m_ptCursorPos.x);
		EnsureVisible(m_ptCursorPos);
		UpdateCaret();

		if (!bSelect)
		{
			m_ptAnchor.copy(m_ptCursorPos);
		}

		SetSelection(m_ptAnchor, m_ptCursorPos);
	}

	void MoveUp(boolean bSelect)
	{
		PrepareSelBounds();
		if (!m_ptDrawSelStart.equals(m_ptDrawSelEnd) && !bSelect)
		{
			m_ptCursorPos.copy(m_ptDrawSelStart);
		}
		
		CPoint subLinePos = new CPoint(0, 0);
		CharPosToPoint(m_ptCursorPos.y, m_ptCursorPos.x, subLinePos);
		
		int	nSubLine = GetSubLineIndex(m_ptCursorPos.y) + subLinePos.y;
		
		if (nSubLine > 0)
	   {
			if (m_nIdealCharPos == -1)
			{
				m_nIdealCharPos = CalculateActualOffset (m_ptCursorPos.y, m_ptCursorPos.x);
			}
			SubLineCursorPosToTextPos(new CPoint(m_nIdealCharPos, nSubLine - 1), m_ptCursorPos );

			if (m_ptCursorPos.x > GetLineLength (m_ptCursorPos.y))
			{
				m_ptCursorPos.x = GetLineLength (m_ptCursorPos.y);
			}
	    }
		EnsureVisible(m_ptCursorPos);
		UpdateCaret();

		if (!bSelect)
		{
			m_ptAnchor.copy(m_ptCursorPos);
		}
		SetSelection(m_ptAnchor, m_ptCursorPos);
	}
	
	void SubLineCursorPosToTextPos(CPoint subLineCurPos, CPoint textPos)
	{
		// Get line breaks
		BaseDataObject nSubLineOffset = new BaseDataObject(), nLine = new  BaseDataObject();
		
		GetLineBySubLine(subLineCurPos.y, nLine, nSubLineOffset);
		
		// compute cursor-position
		textPos.x = CursorPointToCharPos(nLine.getIntValue(), new CPoint(subLineCurPos.x, nSubLineOffset.getIntValue()));
		textPos.y = nLine.getIntValue();
	}
	
	public void SelectAll()
	{
		int nLineCount = GetLineCount();
		m_ptCursorPos.x = GetLineLength(nLineCount - 1);
		m_ptCursorPos.y = nLineCount - 1;
		SetSelection(new CPoint(0, 0), m_ptCursorPos);
		UpdateCaret();
	}
	
	int SubLineEndToCharPos(int nLineIndex, int nSubLineOffset)
	{
		int nLength = GetLineLength(nLineIndex);
		
		// if word wrapping is disabled, the end is equal to the length of the line -1
		if (!m_bWordWrap)
		{
			return nLength;
		}
		
		return 0;
	}
	
	int SubLineHomeToCharPos(int nLineIndex, int nSubLineOffset)
	{
		int	nLength = GetLineLength(nLineIndex);
		
		// if word wrapping is disabled, the start is 0
		if(!m_bWordWrap || nSubLineOffset == 0)
		{
			return 0;
		}
		
		return 0;
	}
	
//	 Convert any negative inputs to negative char equivalents
//	 This is aimed at correcting any chars mistakenly 
//	 sign-extended to negative ints.
//	 This is ok for the UNICODE build because UCS-2LE code bytes
//	 do not extend as high as 2Gig (actually even full Unicode
//	 codepoints don't extend that high).
	int normch(int c)
	{
		return (char)c;
	}

//	 Returns nonzero if input is outside ASCII or is underline
	boolean xisspecial(int c)
	{
	   int t = 0x7f;
	  return (normch(c) > t || c == '_');
	}

//	 Returns non-zero if input is alphanumeric or "special" (see xisspecial)
//	 Also converts any negative inputs to negative char equivalents (see normch)
	boolean xisalnum(int c)
	{
		return (istalnum(normch(c)) || xisspecial(normch(c)));
	}

	boolean istalnum(int c)
	{
	    if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
	            (c >= '0' && c <= '9'))
	    {
	        return true;
	    }
	    
	    return false;
	}
	
	CPoint WordToRight(CPoint pt)
	{
		int nLength = GetLineLength(pt.y);
		char[] pszChars = GetLineChars(pt.y);
		while (pt.x < nLength)
	    {
			if (!xisalnum(pszChars[pt.x]))
			{
				break;
			}
			pt.x++;
	    }
		return pt;
	}

	CPoint WordToLeft(CPoint pt)
	{
		char[] pszChars = GetLineChars(pt.y);
		while (pt.x > 0)
	    {
			if (!xisalnum (pszChars[pt.x - 1]))
			{
				break;
			}
			pt.x--;
	    }
		return pt;
	}
	
	public boolean FindText(String pszText, CPoint ptStartPos, long dwFlags,
			 boolean bWrapSearch, CPoint pptFoundPos)
	{
	    pattern = Pattern.compile(pszText);
	    if (pattern == null)
	    {
	        return false;
	    }
	    
	    int nLineCount = GetLineCount();

	    boolean bRet = FindTextInBlock(pszText, m_ptCursorPos, new CPoint(0, 0),
	            			new CPoint(GetLineLength(nLineCount - 1), nLineCount - 1),
	            				dwFlags, bWrapSearch, pptFoundPos);
	    
	    if (bRet)
	    {
	        this.HighlightText(pptFoundPos, pszText.length(), FindJDialog.bFindUp);
	    }
	    
	    return bRet;
	}

    public boolean FindTextInBlock(String pszText, CPoint ptStartPosition,
            CPoint ptBlockBegin, CPoint ptBlockEnd, long dwFlags,
            boolean bWrapSearch, CPoint pptFoundPos)
    {
        ptCurrentPos.copy(ptStartPosition);

        if (ptBlockBegin.equals(ptBlockEnd))
        {
            return false;
        }

        if (ptCurrentPos.y < ptBlockBegin.y || ptCurrentPos.y == ptBlockBegin.y
                && ptCurrentPos.x < ptBlockBegin.x)
        {
            ptCurrentPos.copy(ptBlockBegin);
        }

        String what = pszText;
        int nEolns;
        if ((dwFlags & EnumFindText.FIND_REGEXP) == EnumFindText.FIND_REGEXP)
        {
            nEolns = HowManyStr(what, '\n');
        }
        else
        {
            nEolns = 0;
            if ((dwFlags & EnumFindText.FIND_MATCH_CASE) == 0)
            {
                what = what.toUpperCase();
                pattern = Pattern.compile(what);
            }
        }
        if ((dwFlags & EnumFindText.FIND_DIRECTION_UP) == EnumFindText.FIND_DIRECTION_UP)
        {
            //  Let's check if we deal with whole text.
            //  At this point, we cannot search *up* in selection

            //  Proceed as if we have whole text search.
            for (;;)
            {
                while (ptCurrentPos.y >= 0)
                {
                    int nLineLength;
                    String line = "";

                    nLineLength = GetLineLength(ptCurrentPos.y);
                    if (ptCurrentPos.x == -1)
                    {
                        ptCurrentPos.x = nLineLength;
                    }
                    else
                    {
                        if (ptCurrentPos.x >= nLineLength)
                        {
                            ptCurrentPos.x = nLineLength - 1;
                        }
                    }

                    char[] pszChars = GetLineChars(ptCurrentPos.y);
                    CMemoryOperator.CopyMemory(line.toCharArray(), pszChars,
                            ptCurrentPos.x + 1);
                    if ((dwFlags & EnumFindText.FIND_MATCH_CASE) == 0)
                    {
                        line = line.toUpperCase();
                    }

                    int nFoundPos = -1;
                    int nMatchLen = what.length();
                    int nLineLen = line.length();
                    int nPos = 0;
                    matcher = pattern.matcher(line);
                    while(matcher.find())
                    {
                        nPos = matcher.start();
                        if (nPos >= 0)
                        {
                            nFoundPos = (nFoundPos == -1) ? nPos : nFoundPos
                                    + nPos;
                            nFoundPos += nMatchLen;
                            break;
                        }
                    }

                    if (nFoundPos >= 0) // Found text!
                    {
                        ptCurrentPos.x = nFoundPos - nMatchLen;
                        pptFoundPos.copy(ptCurrentPos);
                        //this.TextToClient()
                        return true;
                    }

                    ptCurrentPos.y--;
                    if (ptCurrentPos.y >= 0)
                    {
                        ptCurrentPos.x = GetLineLength(ptCurrentPos.y);
                    }
                }

                // Beginning of text reached
                if (!bWrapSearch)
                {
                    return false;
                }

                // Start again from the end of text
                bWrapSearch = false;
                ptCurrentPos.x = 0;
                ptCurrentPos.y = GetLineCount() - 1;
            }
        }
        else
        {
            for (;;)
            {
                while (ptCurrentPos.y <= ptBlockEnd.y)
                {
                    int nLineLength, nLines;
                    String line = "";

                     nLineLength = GetLineLength(ptCurrentPos.y)
                            - ptCurrentPos.x;
                    if (nLineLength <= 0)
                    {
                        ptCurrentPos.x = 0;
                        ptCurrentPos.y++;
                        continue;
                    }

                    char[] pszChars = GetLineChars(ptCurrentPos.y);
                    line = String.valueOf(pszChars);
                    line = line.substring(ptCurrentPos.x);

                    //  Prepare necessary part of line
                    if ((dwFlags & EnumFindText.FIND_MATCH_CASE) == 0)
                    {
                        line = line.toUpperCase();
                    }

                    // Perform search in the line
                    matcher = pattern.matcher(line);
                    while(matcher.find())
                    {
                        ptCurrentPos.x += matcher.start();
                        // Check of the text found is outside the block.
                        if (ptCurrentPos.y == ptBlockEnd.y
                                && ptCurrentPos.x >= ptBlockEnd.x)
                        {
                            break;
                        }

                        pptFoundPos.copy(ptCurrentPos);
                        return true;
                    }

                    // Go further, text was not found
                    ptCurrentPos.x = 0;
                    ptCurrentPos.y++;
                }

                // End of text reached
                if (!bWrapSearch)
                {
                    return false;
                }

                // Start from the beginning
                bWrapSearch = false;
                ptCurrentPos = ptBlockBegin;
            }
        }
    }
    
    public void onEditFind()
    {
        FindJDialog.createAndShowGUI(mainFrame, this);
    }
    
    public void onEditFindNext()
    {
        FindText(FindJDialog.strFindWhat, this.m_ptCursorPos, FindJDialog.dwSearchFlags, true, new CPoint(0, 0));
    }

/*	public void lostOwnership(Clipboard arg0, Transferable arg1) 
	{
		
	}*/
    
}

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
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.WindowConstants;
import javax.swing.JFrame;

import MergeHeroLib.CPoint;

import com.dynamsoft.sourceanywhere.BaseDataObject;

/**
 * @author Thomas Wong
 *
 */
public class LocationViewJPanel extends javax.swing.JPanel implements MouseListener 
{

	/**
	* Auto-generated main method to display this 
	* JPanel inside a new JFrame.
	*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new LocationViewJPanel());
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.show();
	}
	
	private CDiffTextView m_pLeftView = null;
	private CDiffTextView m_pRightView = null;
	private double m_dbPixInLines = 0; // How many pixels is one line in bars
	private CPoint m_ptClick = new CPoint(0, 0);
	private boolean m_bFirstLineDiff = false; // if the first is diff, then not draw black line
//	 Size of empty frame above and below bars (in pixels)
	private final int Y_OFFSET = 5;
	CRect rc = new CRect();
	CRect r0 = new CRect();
	CRect r1 = new CRect();
	CRect drawrc = new CRect();
	CRect rcStatus = new CRect();
	Color CLR_NONE = null;
	Color cr0 = null; // Left side color
	Color cr1 = null; // Right side color
	Color crt = Color.black; // Text color
	BaseDataObject bwh = new BaseDataObject();
	Color statusCr = new Color(0, 128, 255);
	
	public LocationViewJPanel()
	{
		super();
		initGUI();	
		addMouseListener(this);
	}
	
	private void initGUI() {
		try {
			this.setPreferredSize(new java.awt.Dimension(64, 155));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void paintComponent(Graphics g)
	{
	    Dimension d = getSize();
	    rc.setRect(0, 0, d.width, d.height);
	    super.paintComponent(g);
	    g.setColor(Color.white);
		g.fillRect(rc.left, rc.top, rc.Width(), rc.Height());

		DrawView(g);
	}
	
	public void ResetView()
	{
		m_pLeftView = null;
		m_pRightView = null;
		m_dbPixInLines = 0.0; // How many pixels is one line in bars
		m_ptClick.x = 0;
		m_ptClick.y = 0;
		m_bFirstLineDiff = false; // if the first is diff, then not draw black line   
	}
	
	public void Update(int nPos)
	{
		if (m_pLeftView == null || m_pRightView == null ||
			m_pLeftView.m_pDiffBuffer == null ||
			m_pRightView.m_pDiffBuffer == null ||
			!m_pLeftView.m_pDiffBuffer.m_bInit || 
			!m_pRightView.m_pDiffBuffer.m_bInit)
		{
			return;
		}

		/*int nScreenLines = m_pLeftView.GetScreenLines();
		if ((nPos + nScreenLines) >= m_pLeftView.GetLineCount()) 
		{
			nPos = m_pLeftView.GetLineCount();
		}*/
		m_ptClick.y = GetYPosFromLine(nPos);

		repaint();
	}
	
	public void mouseClicked(MouseEvent evt)
	{
	    FileDiffViewPanel.pActiveView = null;
	    this.requestFocus();
		m_ptClick.x = evt.getX();
		m_ptClick.y = evt.getY();
		if (!GotoLocation(m_ptClick))
		{
			return;
		}

		repaint();
 	}
	
	public void mousePressed(MouseEvent evt)
	{
	    
	}
	
	public void mouseReleased(MouseEvent evt)
	{
	    
	}
	
	public void mouseEntered(MouseEvent evt)
	{
	    
	}
	
	public void mouseExited(MouseEvent evt)
	{
	    
	}
	
	int GetYPosFromLine(int nLine)
	{
		int nYPos = -1;	

		nYPos = (int)((double)nLine/m_dbPixInLines) + Y_OFFSET;

		return nYPos;
	}
	
//	 Calculates real line in file from given YCoord in bar.
//	 nYcoord [in] ycoord in pane
//	 rc [in] size of locationpane
//	 return 0-based index of real line in file [0...lines-1]
	public int GetLineFromYPos(int nYCoord, CRect rc)
	{
		int nLines = Math.min(m_pLeftView.GetLineCount(), m_pRightView.GetLineCount());
		if (nYCoord < Y_OFFSET)
		{
			nYCoord = Y_OFFSET;
		}

		if (nYCoord > (rc.Height() - Y_OFFSET))
		{
			nYCoord = rc.Height() - Y_OFFSET;
		}

		int nLine = (int)(m_dbPixInLines * (nYCoord - Y_OFFSET));
		int nRealLine = -1;	

		nLine--; // Convert linenumber to lineindex
		if (nLine > nLines - 1) // Just to be sure
		{
			nLine = nLines - 1;
		}

		if (nLine < 0)
		{
			nLine = 0;
		}
		return nLine;
	}

//	/ Move both views to point given (if in one of the file columns, else return false)
	boolean GotoLocation(CPoint point)
	{
		if (m_pLeftView == null || m_pRightView == null ||
				m_pLeftView.m_pDiffBuffer == null ||
				m_pRightView.m_pDiffBuffer == null ||
				!m_pLeftView.m_pDiffBuffer.m_bInit || 
				!m_pRightView.m_pDiffBuffer.m_bInit)
			{
				return false;
			}

		int line = GetLineFromYPos(point.y, rc);
		
		m_pLeftView.mainFrame.fileFrame.getDiffPane().setLeftViewVertScrollPos(line * m_pLeftView.m_nLineHeight);
		
		return true;
	}
	
//	 Draw one block of map.
	void DrawRect(Graphics g, CRect r, Color cr, boolean border)
	{
		if (cr == null)
		{
			assert(false);
			return;
		}
		if (cr.equals(Color.white) || cr.equals(CLR_NONE))
	 	{
		    g.setColor(Color.black);
			if (r.top == Y_OFFSET && !m_bFirstLineDiff)
			{
			    g.drawLine(r.left, r.top, r.right - 1, r.top);
			}

			g.drawLine(r.left, r.top, r.left, r.bottom);
			g.drawLine(r.right - 1, r.top, r.right - 1, r.bottom);
			g.drawLine(r.left, rc.bottom - Y_OFFSET, r.right, rc.bottom - Y_OFFSET);
	 	}
	 	// colored rectangle
	 	else
	 	{
	 	   g.setColor(cr);
			if ((r.bottom - r.top) <= 1)
			{		    
				for (int i = 0; i <= (r.bottom - r.top); i++)
				{
				    g.drawLine(r.left, r.top + i, r.right, r.top + i);
				}

				if (r.bottom == Y_OFFSET)
				{
					m_bFirstLineDiff = true;
				}
			}
			else
			{
				g.fillRect(r.left, r.top, r.Width(), r.Height());
			}
		}
	}
	
	void DrawView(Graphics g)
	{
		if (m_pLeftView == null || m_pRightView == null)
		{
			return;
		}

		int nStatusWidth = rc.Width()/16;
		int nStatusHeight = rc.Height()/25;
		int nDiffWidth = 3 * rc.Width()/16;
		int nDisLeftRight = rc.Width()/8;

		int nLeftStatus = rc.Width()/8;
		int nRightStatus = 13 * rc.Width()/16;
		int nLeft = rc.Width()/4;
		int nRight = 9 * rc.Width()/16;

		// Draw status bar
		if (m_ptClick.y < Y_OFFSET)
		{
			m_ptClick.y = Y_OFFSET;
		}
		if ((m_ptClick.y + nStatusHeight) > rc.Height() - Y_OFFSET)
		{
			m_ptClick.y = rc.Height() - Y_OFFSET - nStatusHeight;
		}
		rcStatus.setRect(nLeftStatus, m_ptClick.y, nLeftStatus + nStatusWidth, m_ptClick.y + nStatusHeight);
		DrawRect(g, rcStatus, statusCr, false);
		rcStatus.setRect(nRightStatus, m_ptClick.y, nRightStatus + nStatusWidth, m_ptClick.y + nStatusHeight);
		DrawRect(g, rcStatus, statusCr, false);

		// Draw diff pane
		rcStatus.setRect(nLeft, Y_OFFSET, nLeft + nDiffWidth, rc.Height() - Y_OFFSET);
		DrawRect(g, rcStatus, Color.white, false);
		rcStatus.setRect(nRight, Y_OFFSET, nRight + nDiffWidth, rc.Height() - Y_OFFSET);
		DrawRect(g, rcStatus, Color.white, false);
		double hTotal = rc.Height() - (2 * Y_OFFSET); // Height of draw area
		int nLines = Math.min(m_pLeftView.GetLineCount(), m_pRightView.GetLineCount());
		double nLineInPix = hTotal/nLines;
		bwh.setBooleanValue(false);
		int nStart = 0;
		int nEnd = 0;
		int nDeleteGrowLength = 0;
		int nInsertGrowLength = 0;

		m_dbPixInLines = nLines/hTotal;

		int nLeftTotalLines = m_pLeftView.GetLineCount();
		int nRightTotalLines = m_pRightView.GetLineCount();

		assert (nLeftTotalLines == nRightTotalLines);

		// First draw left view
		long dwFlags = 0;
		int iBlockHeight = 0;
		int iHideCount = 0;
		int i = 0;

		for (i = 0; i < nLeftTotalLines; i++)
		{
			dwFlags = m_pLeftView.GetLineFlags(i);
				
			nStart = i;
			do
			{
				dwFlags = m_pLeftView.GetLineFlags(i);
				i ++;
			}
			while (i < nLeftTotalLines && m_pLeftView.GetLineFlags(i) == dwFlags);

			nEnd = -- i;
			iBlockHeight = nEnd - nStart + 1;
			
			// here nStart = first line of block
			final double dbBeginY = (nStart - iHideCount) * nLineInPix + Y_OFFSET;
			final double dbEndY = (iBlockHeight + nStart - iHideCount) * nLineInPix + Y_OFFSET;
				
			// Draw left side block
			cr0 = m_pLeftView.GetLineColors(nStart);
			r0.setRect(nLeft, (int)dbBeginY, (nLeft + nDiffWidth), (int)dbEndY);
			DrawRect(g, r0, cr0, false);
		}

		iHideCount = 0;

		// Second draw right view
		for (i = 0; i < nRightTotalLines; i++)
		{
			dwFlags = m_pLeftView.GetLineFlags(i);
				
			nStart = i;
			do
			{
				dwFlags = m_pRightView.GetLineFlags(i);
				i ++;
			}
			while (i < nLeftTotalLines && m_pRightView.GetLineFlags(i) == dwFlags);

			nEnd = -- i;
			iBlockHeight = nEnd - nStart + 1;
			
			// here nStart = first line of block
			final double dbBeginY = (nStart - iHideCount) * nLineInPix + Y_OFFSET;
			final double dbEndY = (iBlockHeight + nStart - iHideCount) * nLineInPix + Y_OFFSET;
				
			// Draw right side block
			cr1 = m_pRightView.GetLineColors(nStart);
			r1.setRect(nRight, (int)dbBeginY, (nRight + nDiffWidth), (int)dbEndY);
			DrawRect(g, r1, cr1, false);
		}	
	}
	
	public void attachView(CDiffTextView pLeftView, CDiffTextView pRightView)
	{
		m_pLeftView = pLeftView;
		m_pRightView = pRightView; 
	}
}

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
import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;
import javax.swing.JScrollBar;

/*
 * Created on 2005-4-30
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
public class ScrollFrame extends JPanel implements AdjustmentListener, MouseWheelListener
{

    public static void main(String[] args)
    {
    }
    
    public JScrollBar horiz = null;
    public JScrollBar vert = null;
    private CTextView textView = null;
    int m_iViewType = -1; 
   
    public ScrollFrame(CTextView textView, int iViewType)
    {
        this.setLayout(new BorderLayout());
        
        horiz = new JScrollBar(Adjustable.HORIZONTAL);
        vert = new JScrollBar(Adjustable.VERTICAL);
        
        m_iViewType = iViewType;
        
        if (m_iViewType == Common.LEFT_VIEW)
        {
            vert.setVisible(false);
        }

        this.textView = textView;      
        // put the status pane and the split pane in a pane.
        add(textView, BorderLayout.CENTER);
        add(horiz, BorderLayout.SOUTH);
        add(vert, BorderLayout.EAST);
       
	    vert.addAdjustmentListener(this);
        horiz.addAdjustmentListener(this);
        horiz.setValues(horiz.getValue(), 0, 0, 100);
        vert.setValues(vert.getValue(), 0, 0, 100);
	            
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent evt)
            {
                setVisibleAmounts();
            }

            public void componentResized(ComponentEvent evt)
            {
                setVisibleAmounts();
            }
        });  
        
        addMouseWheelListener(this);
     }
    
    public void reset()
    {
        horiz.setValues(0, 0, 0, 10);
        vert.setValues(0, 0, 0, 10);   
        horiz.setVisibleAmount(0);
		vert.setVisibleAmount(0);
    }
    
	protected void add(Component c, GridBagConstraints gbc, int x, int y, int w, int h)
	{
	    gbc.gridx = x;
	    gbc.gridy = y;
	    gbc.gridwidth = w;
	    gbc.gridheight = h;
	    add(c, gbc);
	}
    
    public void setVisibleAmounts()
    {
        if (textView != null)
        {
            horiz.setValues(horiz.getValue(), 0, 0, textView.iRealX);
            vert.setValues(vert.getValue(), 0, 0, textView.iRealY);
            horiz.setVisibleAmount(textView.getSize().width);
  		    vert.setVisibleAmount(textView.getSize().height);
        }
    }
    
    public void adjustmentValueChanged(AdjustmentEvent evt)
    {
        if (textView.getSize().height == 0 || textView.getSize().width == 0)
        {
            return;
        }
        
        vert.setUnitIncrement(textView.m_nLineHeight);
        vert.setBlockIncrement(textView.getSize().height);
        
        int iHoriz = horiz.getValue();
        int iVert = vert.getValue();
        
        textView.translate(iHoriz, iVert);
        switch(m_iViewType)
		{
        case Common.LEFT_VIEW:
        	textView.mainFrame.fileFrame.getDiffPane().setRightViewScrollPos(iHoriz, iVert);
        	textView.mainFrame.fileFrame.getDiffPane().setMergeViewScrollPos(iHoriz, iVert);
        	break;
        case Common.RIGHT_VIEW:
           	textView.mainFrame.fileFrame.getDiffPane().setLeftViewScrollPos(iHoriz, iVert);
            textView.mainFrame.fileFrame.getDiffPane().setMergeViewScrollPos(iHoriz, iVert);
    		break;
    	case Common.MERGE_VIEW:
    		textView.mainFrame.fileFrame.getDiffPane().setLeftViewScrollPos(iHoriz, iVert);
    	    textView.mainFrame.fileFrame.getDiffPane().setRightViewScrollPos(iHoriz, iVert);
			break;     		
        }
         
        textView.mainFrame.fileFrame.getDiffPane().updateLocationView(iVert/textView.m_nLineHeight);
    }
    
    public JScrollBar getHoriz()
    {
        return horiz;
    }
    
    public JScrollBar getVert()
    {
        return vert;
    }
    
	public void keyMoved(boolean bHoriz, int increment, int direction)
	{
	    if (increment == 0)
	    {
	        return;
	    }
	    if (bHoriz)
	    {
	        horiz.setValue(horiz.getValue() + increment * direction);
	    }
	    else
	    {
	        vert.setValue(vert.getValue() + increment * direction);
	    }
	}
	
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        int increment = 0;
        int direction = e.getWheelRotation();
        
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
        {
            increment = vert.getUnitIncrement();
        }
        else if (e.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL)
        {
            increment = vert.getBlockIncrement();
        }
        
        vert.setValue(vert.getValue() + direction * increment);
    }
}

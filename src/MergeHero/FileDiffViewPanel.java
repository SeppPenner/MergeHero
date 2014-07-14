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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;
/*
 * Created on 2005-4-25
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
public class FileDiffViewPanel extends JPanel
{
	private StatusJPanel statusJPanelTop = null;
	private StatusJPanel statusJPanelBottom = null;
	private LocationViewJPanel leftPane = null;
	private CDiffTextView leftView = null;
	private CDiffTextView rightView = null;
	private CMergeTextView mergeView = null;
	private ScrollFrame leftScrollPane = null;
	private ScrollFrame rightScrollPane = null;
	private ScrollFrame mergeScrollPane = null;
	private JSplitPane splitDiffPane = null;
	private JSplitPane splitTopPane = null;
	private JSplitPane splitAllPane = null;
	private JPanel rightPane = null;
	private boolean m_bOnlyDiff = true;
	public static CTextView pActiveView = null;
	
    public static void main(String[] args)
    {
		JFrame frame = new JFrame();
		FileDiffViewPanel diffPane = new FileDiffViewPanel(false);
		frame.getContentPane().add(diffPane);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.show();
    }
    
	public FileDiffViewPanel(boolean bOnlyDiff)
	{
		super();
		m_bOnlyDiff = bOnlyDiff;
		initGUI();
	}
	
	private void initGUI() {
		try {
			setPreferredSize(new Dimension(800, 600));
			setBounds(new Rectangle(0, 0, 800, 600));
			setLayout(new BorderLayout());
			
			// Create the diff view pane
			rightPane = new JPanel(new GridBagLayout());
			
            statusJPanelTop = new StatusJPanel(2, 82);
            statusJPanelBottom = new StatusJPanel(2, 82);
            leftView = new CDiffTextView();
            rightView = new CDiffTextView();
            leftScrollPane = new ScrollFrame(leftView, Common.LEFT_VIEW);
            rightScrollPane = new ScrollFrame(rightView, Common.RIGHT_VIEW);
            leftView.setScrollFrame(leftScrollPane);
            rightView.setScrollFrame(rightScrollPane);
              
            splitDiffPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
                    leftScrollPane, rightScrollPane);
            splitDiffPane.setDividerSize(5);
             
            // put the status pane and the split pane in a pane.
			GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTH;           
            gbc.weightx = 100;
            gbc.weighty = 0;
            add(statusJPanelTop, gbc, 0, 0, 1, 1);
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 0;
            gbc.weighty = 100;
            add(splitDiffPane, gbc, 0, 1, 1, 1);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.SOUTH;           
            gbc.weightx = 100;
            gbc.weighty = 0;
            add(statusJPanelBottom, gbc, 0, 2, 1, 1);
            
			// Create the location view pane in left
			leftPane = new LocationViewJPanel();
            
            // put the left pane and the right pane in a split pane
            splitTopPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
                    leftPane, rightPane);
            splitTopPane.setOneTouchExpandable(true);
            splitTopPane.setResizeWeight(0);
  
            if (m_bOnlyDiff)
            {
                add(splitTopPane);
            }
            else
            {
                // Create the merge view
                mergeView = new CMergeTextView();
                mergeScrollPane = new ScrollFrame(mergeView, Common.MERGE_VIEW);
                mergeView.setScrollFrame(mergeScrollPane);
 
                // put the merge view and the top split pane in a split pane
                splitAllPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                        splitTopPane, mergeScrollPane);
                splitAllPane.setOneTouchExpandable(true);
                splitAllPane.setResizeWeight(0);
                
                add(splitAllPane);
            }
            			
			setVisible(true);
			
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	protected void add(Component c, GridBagConstraints gbc, int x, int y, int w, int h)
	{
	    gbc.gridx = x;
	    gbc.gridy = y;
	    gbc.gridwidth = w;
	    gbc.gridheight = h;
	    rightPane.add(c, gbc);
	}
	
	public void reset()
	{
	    if (leftScrollPane != null)
	    {
	        leftScrollPane.reset();
	    }
	    if (rightScrollPane != null)
	    {
	        rightScrollPane.reset();
	    }
	    if (mergeScrollPane != null)
	    {
	        mergeScrollPane.reset();
	    }
	    pActiveView = null;
	}
	
	public StatusJPanel getTopStatusbar()
	{
	    return statusJPanelTop;
	}
	
	public StatusJPanel getBottomStatusbar()
	{
	    return statusJPanelBottom;
	}
	
	public LocationViewJPanel getLocationView()
	{
	    return leftPane;
	}
	
	public CDiffTextView getLeftView()
	{
	    return leftView;
	}
	
	public CDiffTextView getRightView()
	{
	    return rightView;
	}
	
	public CMergeTextView getMergeView()
	{
	    return mergeView;
	}
	
	public ScrollFrame getLeftScrollPane()
	{
	    return leftScrollPane;
	}
	
	public ScrollFrame getRightScrollPane()
	{
	    return rightScrollPane;
	}
	
	public ScrollFrame getMergeScrollPane()
	{
	    return mergeScrollPane;
	}
	
	public JSplitPane getDiffSplitPane()
	{
	    return splitDiffPane;
	}
	
	public JSplitPane getTopSplitPane()
	{
	    return splitTopPane;
	}
	
	public JSplitPane getAllSplitPane()
	{
	    return splitAllPane;
	}
	
	public boolean isSelection()
	{
	    if (pActiveView == null)
	    {
	        return false;
	    }
	    return pActiveView.IsSelection();
	}
	
	public void copy()
	{
	    if (pActiveView != null)
	    {
	        pActiveView.Copy();
	    }
	}
	
	public void selectAll()
	{
	    if (pActiveView != null)
	    {
	        pActiveView.SelectAll();
	    }	    
	}
	
	public void find()
	{
	    if (pActiveView != null)
	    {
	        pActiveView.onEditFind();
	    }
	}
	
	public void findNext()
	{
	    if (pActiveView != null)
	    {
	        pActiveView.onEditFindNext();
	    }
	}
	
	public void setLeftViewScrollPos(int iHoriz, int iVert)
	{
		if (leftScrollPane != null)
		{
			if (leftScrollPane.horiz != null && leftScrollPane.vert != null)
			{
				leftScrollPane.horiz.setValue(iHoriz);
				leftScrollPane.vert.setValue(iVert);
			}
			else
			{
				assert(false);
			}
		}
		else
		{
			assert(false);
		}
	}
	
	public void setLeftViewVertScrollPos(int iVert)
	{
		if (leftScrollPane != null)
		{
			if (leftScrollPane.vert != null)
			{
				leftScrollPane.vert.setValue(iVert);
			}
			else
			{
				assert(false);
			}
		}
		else
		{
			assert(false);
		}
	}	
	
	public void setRightViewScrollPos(int iHoriz, int iVert)
	{
		if (rightScrollPane != null)
		{
			if (rightScrollPane.horiz != null && rightScrollPane.vert != null)
			{
				rightScrollPane.horiz.setValue(iHoriz);
				rightScrollPane.vert.setValue(iVert);
			}
			else
			{
				assert(false);
			}
		}
		else
		{
			assert(false);
		}	
	}
	
	public void setMergeViewScrollPos(int iHoriz, int iVert)
	{
		if (mergeScrollPane != null && !m_bOnlyDiff)
		{
			if (mergeScrollPane.horiz != null && mergeScrollPane.vert != null)
			{
				mergeScrollPane.horiz.setValue(iHoriz);
				mergeScrollPane.vert.setValue(iVert);
			}
			else
			{
				assert(false);
			}
		}
	}
	
	public void updateLocationView(int iPos)
	{
		if (leftPane != null)
		{
			leftPane.Update(iPos);
		}
		else
		{
			assert(false);
		}
	}
}

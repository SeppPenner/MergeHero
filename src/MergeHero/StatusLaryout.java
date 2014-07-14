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
 * Created on 2005-4-18
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package MergeHero;
import java.awt.*;
/**
 * @author Falcon Young
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StatusLaryout implements LayoutManager
{
    private int minWidth = 0;
    private int minHeight = 0;
    private int preferredWidth = 0, preferredHeight = 0;
    private boolean sizesSet = false;
    private int maxComponentWidth = 0;
    private int maxComponentHeight = 0;
    private int sepDis = 0;
    private int totalWidth = 0;
    
    public static void main(String[] args)
    {
    }
    
    public void setStatusLayout(int sepDis, int totalWidth)
    {
        this.sepDis = sepDis;
        this.totalWidth = totalWidth;
     }
      
    public void addLayoutComponent(String name, Component comp)
    {
    }
    
    public void removeLayoutComponent(Component comp)
    {
    }
    
    public void setSizes(Container parent)
    {
        if (sizesSet) return;
        int n = parent.getComponentCount();
        
        preferredWidth = 0;
        preferredHeight = 0;
        minWidth = 0;
        minHeight = 0;
        maxComponentWidth = 0;
        maxComponentHeight = 0;
        
        for (int i = 0; i < n; i++)
        {
            Component c = parent.getComponent(i);
            
            if (c.isVisible())
            {
                Dimension d = c.getPreferredSize();
                maxComponentWidth = Math.max(maxComponentWidth, d.width);
                maxComponentHeight = Math.max(maxComponentHeight, d.height);
            }
        }
        preferredHeight = maxComponentHeight;
        preferredWidth += 2 * maxComponentWidth;
        minHeight = preferredHeight;
        minWidth = preferredWidth;
        sizesSet = true;
    }
    
    public Dimension preferredLayoutSize(Container parent)
    {
        Dimension dim = new Dimension(0,0);
        setSizes(parent);
        Insets insets = parent.getInsets();
        dim.width = preferredWidth + insets.left + insets.right;
        dim.height = preferredHeight + insets.top + insets.bottom;
        
        return dim;
    }
    
    public Dimension minimumLayoutSize(Container parent)
    {
        Dimension dim = new Dimension(0,0);
        setSizes(parent);
        Insets insets = parent.getInsets();
        dim.width = preferredWidth + insets.left + insets.right;
        dim.height = preferredHeight + insets.top + insets.bottom;
        
        return dim;
    } 
    
    public void layoutContainer(Container parent)
    {
        Insets insets = parent.getInsets();
        
        int curContainerWidth = parent.getSize().width;
        
        setSizes(parent);
         
        int n = parent.getComponentCount();
        int widthDelta = 0;
         
        widthDelta = (curContainerWidth - totalWidth) / n;
        widthDelta +=  -1;
        
        for (int i = 0; i < n; i++)
        {
            Component c = parent.getComponent(i);
            
            if (c.isVisible())
            {
                Dimension d = c.getPreferredSize();
                c.setBounds(insets.left + i * (sepDis + widthDelta + d.width), insets.top, d.width + widthDelta, d.height);
            }            
        }
    }
}

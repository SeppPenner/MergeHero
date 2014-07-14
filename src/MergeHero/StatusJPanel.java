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
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.JFrame;

/**
 * @author Thomas Wong
 *
 */
public class StatusJPanel extends javax.swing.JPanel 
{
	private JTextField[] status = null;
	private static int totalWidth = 82;
	private final int sepDis = 2;
	private final int heigth = 15;
	private int DEF_COUNT = 2;
	private int count = DEF_COUNT;

	/**
	* Auto-generated main method to display this 
	* JPanel inside a new JFrame.
	*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new StatusJPanel(2, 205));
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.show();
	}
	
	public StatusJPanel(int nCount, int nTotalWidth)
	{
		super();
	    count = nCount;
	    totalWidth = nTotalWidth;	    
		initGUI();
	}
	
	public void setText(int nIndex, String strText)
	{
	    if (nIndex >= 0 && nIndex < count)
	    {
	        status[nIndex].setText(strText);
	        status[nIndex].setToolTipText(strText);
	    }
	}
	
	private void initGUI() {
		try {
		    StatusLaryout thisLayout = new StatusLaryout();
            this.setLayout(thisLayout);
            thisLayout.setStatusLayout(sepDis, totalWidth);
			this.setPreferredSize(new java.awt.Dimension(totalWidth, heigth));
			
			status = new JTextField[count];
			for (int i = 0; i < count; i++)
            {
			    status[i] = new JTextField();
                this.add(status[i]);
                //status[i].setText(String.valueOf(i));
                status[i].setPreferredSize(new java.awt.Dimension(
                        (totalWidth - sepDis) / count, heigth));
                status[i].setEditable(false);
                //status[i].setToolTipText("pane");
                status[i].setVerifyInputWhenFocusTarget(false);
            }
   		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

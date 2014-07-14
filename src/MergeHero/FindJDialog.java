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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.BorderFactory;
import javax.swing.JFrame;

import MergeHeroLib.CPoint;

/**
 * @author Ellie Smith
 *
 */
public class FindJDialog extends javax.swing.JDialog implements ActionListener, KeyListener
{
	private JLabel jLabelFindWhat;
	private JComboBox jComboBoxFindText;
	private JCheckBox jCheckMatchCase;
	private JRadioButton jRadioButtonDown;
	private JRadioButton jRadioButtonUp;
	private JPanel jPanelGroup;
	private JCheckBox jCheckBoxMatch;
	private JButton jButtonCancel;
	private JButton jButtonFindNext;
	CPoint ptCursor = new CPoint(0, 0), ptFind = new CPoint(0, 0);
	public static String strFindWhat = "";
	public static boolean bFindUp = false;
	boolean bMatchCase = false;
	boolean bMatchWholeWord = false;
	CTextView textView = null;
	static int MAX_FIND = 20;
	static String[] strFinded = new String[MAX_FIND];
	static int iFindedIndex = 0;
	public static long dwSearchFlags = 0;

	/**
	* Auto-generated main method to display this JDialog
	*/
	public static void main(String[] args) {
		//JFrame frame = new JFrame();
		//createAndShowGUI(findDlg, frame, null);
	}
	
	public FindJDialog(JFrame frame, CTextView textView) {
		super(frame);
		this.textView = textView;
		initGUI();
	}
	
	private void initGUI() {
		try {
			{
				this.getContentPane().setLayout(null);
				this.setTitle("Find");
                {
                    jLabelFindWhat = new JLabel();
                    this.getContentPane().add(jLabelFindWhat);
                    jLabelFindWhat.setText("FindWhat:");
                    jLabelFindWhat.setBounds(3, 5, 63, 22);
                }
                {
                    jComboBoxFindText = new JComboBox(strFinded);
                    this.getContentPane().add(jComboBoxFindText);
                    jComboBoxFindText.setEditable(true);
                    jComboBoxFindText.setBounds(75, 2, 266, 30);
                    jComboBoxFindText.setActionCommand("FindWhat");
                    jComboBoxFindText.requestFocus();
                    jComboBoxFindText.addKeyListener(this);
                    jComboBoxFindText.addActionListener(this);
                }
                {
                    jButtonFindNext = new JButton();
                    this.getContentPane().add(jButtonFindNext);
                    jButtonFindNext.setText("Find Next");
                    jButtonFindNext.setMnemonic(KeyEvent.VK_N);
                    jButtonFindNext.setActionCommand("Find Next");
                    jButtonFindNext.addActionListener(this);
                    jButtonFindNext.setBounds(351, 2, 94, 30);
                }
                {
                    jButtonCancel = new JButton();
                    this.getContentPane().add(jButtonCancel);
                    jButtonCancel.setText("Cancel");
                    jButtonCancel.setActionCommand("Cancel");
                    jButtonCancel.addActionListener(this);
                    jButtonCancel.setBounds(352, 41, 92, 30);
                }
                {
                    jCheckBoxMatch = new JCheckBox();
                    this.getContentPane().add(jCheckBoxMatch);
                    jCheckBoxMatch.setText("Match whole word only");
                    jCheckBoxMatch.setMnemonic(KeyEvent.VK_W);
                    jCheckBoxMatch.setActionCommand("Match whole word only");
                    jCheckBoxMatch.addActionListener(this);
                    jCheckBoxMatch.setBounds(2, 48, 195, 30);
                }
                {
                    jCheckMatchCase = new JCheckBox();
                    this.getContentPane().add(jCheckMatchCase);
                    jCheckMatchCase.setText("Match case");
                    jCheckMatchCase.setMnemonic(KeyEvent.VK_C);
                    jCheckMatchCase.setActionCommand("Match case");
                    jCheckMatchCase.addActionListener(this);
                    jCheckMatchCase.setBounds(2, 77, 135, 30);
                }
                {
                    jPanelGroup = new JPanel();
                    this.getContentPane().add(jPanelGroup);
                    jPanelGroup.setLayout(null);
                    jPanelGroup.setBounds(209, 50, 130, 68);
                    jPanelGroup.setBorder(BorderFactory.createTitledBorder("Direction"));
                    jPanelGroup.setOpaque(false);
                    {
                        jRadioButtonDown = new JRadioButton();
                        jPanelGroup.add(jRadioButtonDown);
                        jRadioButtonDown.setText("Down");
                        jRadioButtonDown.setMnemonic(KeyEvent.VK_D);
                        jRadioButtonDown.setActionCommand("Down");
                        jRadioButtonDown.addActionListener(this);
                        jRadioButtonDown.setSelected(!bFindUp);
                        jRadioButtonDown.setBounds(5, 40, 102, 22);
                    }
                    {
                        jRadioButtonUp = new JRadioButton();
                        jPanelGroup.add(jRadioButtonUp);
                        jRadioButtonUp.setText("Up");
                        jRadioButtonUp.setMnemonic(KeyEvent.VK_U);
                        jRadioButtonUp.setActionCommand("Up");
                        jRadioButtonUp.addActionListener(this);
                        //jRadioButtonDown.setSelected(bFindUp);
                        jRadioButtonUp.setBounds(5, 18, 102, 22);
                    }
                }
			}
			this.setSize(459, 171);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Create the GUI and show it. For thread safety,
	// this method should be invoked from the event-dispatching thread.
	public static void createAndShowGUI(JFrame frame, CTextView textView)
	{
	    // Make sure we have nice window decorations.
	    //JDialog.setDefaultLookAndFeelDecorated(true);
	    
	    // Create and set up the window.
	    FindJDialog findJDlg = new FindJDialog(frame, textView);
	    //findJDlg.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
	    
	    // Display the window.
	    findJDlg.setResizable(false);
	    findJDlg.setVisible(true);
	}

	public void keyTyped(KeyEvent e)
	{
	    enableFind();
	}
	
	public void keyPressed(KeyEvent e)
	{
	    enableFind();
	}
	
	public void keyReleased(KeyEvent e)
	{
	    enableFind();
	}
	
	public void actionPerformed(ActionEvent e)
	{
	    if ("Find Next".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
			if (bMatchCase)
			{
				dwSearchFlags |= EnumFindText.FIND_MATCH_CASE;
			}
			if (bMatchWholeWord)
			{
				dwSearchFlags |= EnumFindText.FIND_WHOLE_WORD;
			}
			if (bFindUp)
			{
				dwSearchFlags |= EnumFindText.FIND_DIRECTION_UP;
			}
			
	        if (textView != null)
	        {
	            if (textView.FindText(strFindWhat, ptCursor, dwSearchFlags, true, ptFind))
	            {
	                int i = 0;
	                for (i = 0; i < MAX_FIND; i++)
	                {
	                    if (strFinded[i] == null)
	                    {
	                        strFinded[i] = new String(strFindWhat);
	                        jComboBoxFindText.insertItemAt(strFinded[i], i);
	                        break;
	                    }
	                    else if (strFindWhat.compareTo(strFinded[i]) == 0)
	                    {
	                        break;
	                    }
	                    else
	                    {
	                        continue;
	                    }	                        
	                }
	                if (i == MAX_FIND)
	                {
	                    strFinded[i - 1] = new String(strFindWhat); 
	                    jComboBoxFindText.insertItemAt(strFinded[i - 1], i - 1);
	                }
	            }
	        }
	    }	
	    else if ("FindWhat".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        strFindWhat = (String)jComboBoxFindText.getSelectedItem();
	    }
	    else if ("Match whole word only".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        bMatchWholeWord =  jCheckBoxMatch.isSelected();
	    }	
	    else if ("Match case".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        bMatchCase =  jCheckMatchCase.isSelected();
	    }	
	    else if ("Down".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        bFindUp = !jRadioButtonDown.isSelected();
	        jRadioButtonUp.setSelected(bFindUp);
	    }	
	    else if ("Up".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        bFindUp = jRadioButtonUp.isSelected();
	        jRadioButtonDown.setSelected(!bFindUp);
	    }	
	    else if ("Cancel".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        dispose();
	    }	
	}
	
	void enableFind()
	{
	    strFindWhat = (String)jComboBoxFindText.getSelectedItem();
	    strFindWhat.trim();
	    if (strFindWhat.length() == 0)
	    {
	        jButtonFindNext.setEnabled(false);
	    }
	    else
	    {
	        jButtonFindNext.setEnabled(true);
	    }
	}
}

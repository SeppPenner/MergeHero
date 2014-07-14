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
import javax.swing.*;

import java.awt.event.*;
import java.io.IOException;
import java.net.*;
import javax.imageio.ImageIO;



/**
 * @author Ellie Smith
 *
 */

public class AboutJDialog extends JDialog implements ActionListener
{
	private JLabel jLabel;
	private JButton jButtonOk;
	private JLabel jLabelURL;
	private JLabel jLabelVersion;
	private String IMAGE_URL = "/MergeHero/IMAGE/dynamsoft_logo_black.png";
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); //this is the user's screen size
	private ImageIcon image; 
	/**
	* Auto-generated main method to display this JDialog
	*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		AboutJDialog.createAndShowGUI(frame);
	}
	
	public AboutJDialog(JFrame frame) {
		super(frame, true);
		initGUI();
	}
	
	// Create the GUI and show it. For thread safety,
	// this method should be invoked from the event-dispatching thread.
	public static void createAndShowGUI(JFrame frame)
	{
	    // Make sure we have nice window decorations.
	    JDialog.setDefaultLookAndFeelDecorated(true);
	    
	    // Create and set up the window.
	    AboutJDialog aboutDlg = new AboutJDialog(frame);
	    //aboutDlg.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
	    
	    // Display the window.
	    aboutDlg.setResizable(false);
	    
	    aboutDlg.setVisible(true);
	}
	
	private void initGUI() 
	{
		try {
			{
				this.setName("AboutDialog");
				this.setTitle("About MergeHero");
				this.getContentPane().setLayout(null);
			}
            {
                jLabel = new JLabel();
                this.getContentPane().add(jLabel);
                jLabel.setText("MergeHero Version 1.0.1");
                jLabel.setBounds(50, 19, 200, 17);
                
            }
            {
                jLabelVersion = new JLabel();
                this.getContentPane().add(jLabelVersion);
                jLabelVersion.setText("Copyright (C) 2006 Dynamsoft Corporation ");
                jLabelVersion.setBounds(50, 41, 300, 21);
            }
            {
            	
             	JLabel jLabelURL = new JLabel();
            	jLabelURL.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            	jLabelURL.addMouseListener(new MouseAdapter() {
	           	   public void mouseClicked(MouseEvent e) {
	           		   if (e.getClickCount() > 0) {
	           	          if (Desktop.isDesktopSupported()) {
	           	                Desktop desktop = Desktop.getDesktop();
	           	                try {
	           	                    URI uri = new URI("http://www.dynamsoft.com/");
	           	                    desktop.browse(uri);
	           	                } catch (IOException ex) {
	           	                    ex.printStackTrace();
	           	                } catch (URISyntaxException ex) {
	           	                    ex.printStackTrace();
	           	                }
	           	          }else {
	           	        	JOptionPane.showMessageDialog(null, "Error opening browser");
	                      
	           	          }
	           	               	   
	           	      }//end if(e.getClickCount..)
	           	   }//end mouseClicked constructor
            	});//end addMouseListener
           	        	  
                this.getContentPane().add(jLabelURL);
                jLabelURL.setText("<HTML><U>www.dynamsoft.com<HTML><U>");
                jLabelURL.setForeground(Color.blue);
                jLabelURL.setBounds(50, 64, 200, 18);
            }
            {
                jButtonOk = new JButton();
                this.getContentPane().add(jButtonOk, BorderLayout.WEST);
                jButtonOk.setText("Ok");
                jButtonOk.setActionCommand("Ok");
                jButtonOk.addActionListener(this);
                jButtonOk.setBounds(325, 30, 60, 30);
                            
            }
            {
            	/*Set Dynamsoft icon*/
            	image = new ImageIcon((ImageIO.read(getClass().getResource(IMAGE_URL)))); //imports the image
            	
            	JLabel lbl = new JLabel(image); //puts the image into a jlabel
            	lbl.setBounds(-14, 92, 200, 18);
            	this.getContentPane().add(lbl);

            }
			this.setSize(400, 150);
	

			int x = (screenSize.width - this.getSize().width)/2; //These two lines are the dimensions
			int y = (screenSize.height - this.getSize().height)/2;//of the center of the screen
			
			this.setLocation(x, y); //sets the location of the jframe
		
         
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void actionPerformed(ActionEvent e)
	{
	    if ("Ok".compareToIgnoreCase(e.getActionCommand()) == 0)
	    {
	        dispose();
	    }
	}

}


/*
 *  02/02/2002 - 20:54:54
 *
 *  JCalendarDemo.java - Demonstration of JCalendar Java Bean
 *  Copyright (C) 2002 Kai Toedter
 *  kai@toedter.com
 *  www.toedter.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.toedter.calendar;

import java.beans.*;
import java.util.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.applet.*;
import javax.swing.*;
import javax.swing.border.*;
import com.toedter.components.*;
import com.incors.plaf.kunststoff.*;

/**
 *  A demonstration Applet for the JCalendar bean. The demo can also be started
 *  as Java application.
 *
 *@author     Kai Toedter
 *@version    1.1.4 07/16/02
 */
public class JCalendarDemo extends JApplet implements PropertyChangeListener {
	/**
	 *  Initializes the applet.
	 */
	public void init() {
		// Set the Kunststoff Look And Feel:
		initializeLookAndFeel();

		getContentPane().setLayout(new BorderLayout());
		calendarPanel = new JPanel();
		calendarPanel.setLayout(new BorderLayout());

		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout());
		JLocaleChooser localeChooser = new JLocaleChooser();
		localeChooser.addPropertyChangeListener(this);
		controlPanel.add(new JLabel(" Locale:   "), BorderLayout.WEST);
		controlPanel.add(localeChooser, BorderLayout.CENTER);

		dateField = new JTextField();
		dateField.setEditable(false);
		controlPanel.add(dateField, BorderLayout.SOUTH);

		JTabbedPane tabbedPane = new JTabbedPane();

		jcalendar1 = new JCalendar();
		jcalendar1.setBorder(new EmptyBorder(10, 10, 10, 10));
		jcalendar1.addPropertyChangeListener(this);

		jcalendar2 = new JCalendar(JMonthChooser.LEFT_SPINNER);
		jcalendar2.setBorder(new EmptyBorder(10, 10, 10, 10));
		jcalendar2.addPropertyChangeListener(this);

		jcalendar3 = new JCalendar(JMonthChooser.NO_SPINNER);
		jcalendar3.setBorder(new EmptyBorder(10, 10, 10, 10));
		jcalendar3.addPropertyChangeListener(this);

		tabbedPane.addTab("Demo 1", null, jcalendar1, "Default JCalendar");
		tabbedPane.addTab("Demo 2", null, jcalendar2, "JCalendar with left month spinner");
		tabbedPane.addTab("Demo 3", null, jcalendar3, "JCalendar with no month spinner");
		tabbedPane.setSelectedIndex(0);

		// Thanks to Paul Galbraith for adding enable/disable capabilities
		Box disablePanel = new Box(BoxLayout.X_AXIS);
		disablePanel.add(Box.createHorizontalStrut(4));
		JLabel disableLabel = new JLabel("Disable Calendars");
		disablePanel.add(disableLabel);
		disablePanel.add(Box.createHorizontalStrut(7));
		final JCheckBox disableCheckBox = new JCheckBox();
		disableCheckBox.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					if (disableCheckBox.isSelected()) {
						jcalendar1.setEnabled(false);
						jcalendar2.setEnabled(false);
						jcalendar3.setEnabled(false);
					} else {
						jcalendar1.setEnabled(true);
						jcalendar2.setEnabled(true);
						jcalendar3.setEnabled(true);
					}
				}
			});
		disablePanel.add(disableCheckBox);

		calendarPanel.add(controlPanel, BorderLayout.NORTH);
		calendarPanel.add(tabbedPane, BorderLayout.CENTER);
		calendarPanel.add(disablePanel, BorderLayout.SOUTH);

		getContentPane().add(createMenuBar(), BorderLayout.NORTH);
		getContentPane().add(calendarPanel, BorderLayout.CENTER);

		calendar = Calendar.getInstance();
		jcalendar1.setCalendar(calendar);
	}


	/**
	 *  Installs the Kunststoff Look And Feel.
	 */
	public final void initializeLookAndFeel() {
		UIManager.put("ClassLoader", UIManager.class.getClassLoader());
		try {
			kunststoffLnF = new KunststoffLookAndFeel();
			kunststoffLnF.setCurrentTheme(new KunststoffTheme());
			UIManager.setLookAndFeel(kunststoffLnF);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			// handle exception or not, whatever you prefer
		}

		UIManager.getLookAndFeelDefaults().put("ClassLoader", getClass().getClassLoader());
	}


	/**
	 *  Creates the menu bar
	 *
	 *@return    Description of the Return Value
	 */
	public JMenuBar createMenuBar() {
		// Create the menu bar
		JMenuBar menuBar = new JMenuBar();

		// Menu for the look and feels (lnfs).
		UIManager.LookAndFeelInfo[] lnfs =
				UIManager.getInstalledLookAndFeels();

		ButtonGroup lnfGroup = new ButtonGroup();
		JMenu lnfMenu = new JMenu("Look&Feel");
		lnfMenu.setMnemonic('L');

		menuBar.add(lnfMenu);

		for (int i = 0; i < lnfs.length; i++) {
			if (!lnfs[i].getName().equals("CDE/Motif")) {
				JRadioButtonMenuItem rbmi =
						new JRadioButtonMenuItem(lnfs[i].getName());
				lnfMenu.add(rbmi);

				// preselect the current Look & feel
				rbmi.setSelected(
						UIManager.getLookAndFeel().getName().equals(
						lnfs[i].getName()));

				// store lool & feel info as client property
				rbmi.putClientProperty("lnf name", lnfs[i]);

				// create and add the item listener
				rbmi.addItemListener(
					// inlining
					new ItemListener() {
						public void itemStateChanged(ItemEvent ie) {
							JRadioButtonMenuItem rbmi2 =
									(JRadioButtonMenuItem) ie.getSource();
							if (rbmi2.isSelected()) {
								// get the stored look & feel info
								UIManager.LookAndFeelInfo info =
										(UIManager.LookAndFeelInfo)
										rbmi2.getClientProperty("lnf name");
								try {
									// setting the Kunststoff L&F with info.getClassName()
									// does not work with some Java Plugins
									if (info.getClassName().equals("com.incors.plaf.kunststoff.KunststoffLookAndFeel")) {
										UIManager.setLookAndFeel(kunststoffLnF);
									} else {
										UIManager.setLookAndFeel(info.getClassName());
									}
									// update the complete application's look & feel
									SwingUtilities.updateComponentTreeUI(
											JCalendarDemo.this);
								} catch (Exception e) {
									System.err.println("Unable to set UI " +
											e.getMessage());
								}
							}
						}
					}
						);
				lnfGroup.add(rbmi);
			}
		}

		// the help menu
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');

		JMenuItem aboutItem = helpMenu.add(new AboutAction(this));
		aboutItem.setMnemonic('A');
		aboutItem.setAccelerator(
				KeyStroke.getKeyStroke('A', java.awt.Event.CTRL_MASK));

		menuBar.add(helpMenu);

		return menuBar;
	}


	/**
	 *  The applet is a PropertyChangeListener for "locale" and "calendar".
	 *
	 *@param  evt  Description of the Parameter
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (calendarPanel != null) {
			if (evt.getPropertyName().equals("locale")) {
				jcalendar1.setLocale((Locale) evt.getNewValue());
				jcalendar2.setLocale((Locale) evt.getNewValue());
				jcalendar3.setLocale((Locale) evt.getNewValue());
				DateFormat df = DateFormat.getDateInstance(DateFormat.LONG,
						jcalendar1.getLocale());
				dateField.setText(df.format(calendar.getTime()));
			} else if (evt.getPropertyName().equals("calendar")) {
				calendar = (Calendar) evt.getNewValue();
				DateFormat df = DateFormat.getDateInstance(DateFormat.LONG,
						jcalendar1.getLocale());
				dateField.setText(df.format(calendar.getTime()));
				jcalendar1.setCalendar(calendar);
				jcalendar2.setCalendar(calendar);
				jcalendar3.setCalendar(calendar);
			}
		}
	}


	/**
	 *  Action to show the About dialog
	 *
	 *@author    toedter_k
	 */
	class AboutAction extends AbstractAction {
		/**
		 *  Constructor for the AboutAction object
		 *
		 *@param  demo  Description of the Parameter
		 */
		AboutAction(JCalendarDemo demo) {
			super("About...");
			this.demo = demo;
		}


		/**
		 *  Description of the Method
		 *
		 *@param  event  Description of the Parameter
		 */
		public void actionPerformed(ActionEvent event) {
			JOptionPane.showMessageDialog(demo, "JCalendar Demo\nVersion 1.1\n\nKai Toedter\nkai@toedter.com\nwww.toedter.com",
					"About...", JOptionPane.INFORMATION_MESSAGE);
		}


		private JCalendarDemo demo;
	}


	/**
	 *  Creates a JFrame with a JCalendarDemo inside and can be used for testing.
	 *
	 *@param  s  The command line arguments
	 */
	public static void main(String[] s) {
		WindowListener l =
			new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			};

		JFrame frame = new JFrame("JCalendar Demo");
		frame.addWindowListener(l);
		JCalendarDemo demo = new JCalendarDemo();
		demo.init();
		frame.getContentPane().add(demo);
		frame.pack();
		frame.setVisible(true);
	}


	private JPanel calendarPanel;
	private JPanel demoPanel;
	private JCalendar jcalendar1;
	private JCalendar jcalendar2;
	private JCalendar jcalendar3;
	private JTextField dateField;
	private JLocaleChooser localeChooser;
	private Calendar calendar;
	private KunststoffLookAndFeel kunststoffLnF;
}









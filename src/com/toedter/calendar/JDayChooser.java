/*
 *  JDayChooser.java  - A bean for choosing a day
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

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 *  JCalendar is a bean for choosing a day.
 *
 *@author     Kai Toedter
 *@version    1.1.3 07/16/02
 */
public class JDayChooser extends JPanel implements ActionListener, KeyListener, FocusListener {
	/**
	 *  Default JDayChooser constructor.
	 */
	public JDayChooser() {
		locale = Locale.getDefault();
		days = new JButton[49];
		selectedDay = null;
		Calendar calendar = Calendar.getInstance(locale);
		today = (Calendar) calendar.clone();

		setLayout(new GridLayout(7, 7));

		for (int y = 0; y < 7; y++) {
			for (int x = 0; x < 7; x++) {
				int index = x + 7 * y;
				if (y == 0) {
					// Create a button that doesn't react on clicks or focus changes
					// Thanks to Thomas Schaefer for the focus hint :)
					days[index] =
						new JButton() {
							public void addMouseListener(
									MouseListener l) { }

							// This method has been deprecated by 1.4
							// and will be replaced by isFocusable in future versions
							public boolean isFocusTraversable() {
								return false;
							}
						};
					days[index].setBackground(new Color(180, 180, 200));
				} else {
					days[index] = new JButton("x");
					days[index].addActionListener(this);
					days[index].addKeyListener(this);
					days[index].addFocusListener(this);
				}

				days[index].setMargin(new Insets(0, 0, 0, 0));
				days[index].setFocusPainted(false);
				add(days[index]);
			}
		}
		init();
		setDay(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
		initialized = true;
	}


	/**
	 *  Initilizes the locale specific names for the days of the week.
	 */
	protected void init() {
		colorRed = new Color(164, 0, 0);
		colorBlue = new Color(0, 0, 164);
		JButton testButton = new JButton();
		oldDayBackgroundColor = testButton.getBackground();
		selectedColor = new Color(160, 160, 160);

		calendar = Calendar.getInstance(locale);
		int firstDayOfWeek = calendar.getFirstDayOfWeek();
		DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(locale);
		dayNames = dateFormatSymbols.getShortWeekdays();
		int day = firstDayOfWeek;
		for (int i = 0; i < 7; i++) {
			days[i].setText(dayNames[day]);
			if (day == 1) {
				days[i].setForeground(colorRed);
			} else {
				days[i].setForeground(colorBlue);
			}

			if (day < 7) {
				day++;
			} else {
				day -= 6;
			}

		}
		drawDays();
	}


	/**
	 *  Hides and shows the day buttons.
	 */
	protected void drawDays() {
		Calendar tmpCalendar = (Calendar) calendar.clone();
		int firstDayOfWeek = tmpCalendar.getFirstDayOfWeek();
		tmpCalendar.set(Calendar.DAY_OF_MONTH, 1);

		int firstDay =
				tmpCalendar.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek;
		if (firstDay < 0) {
			firstDay += 7;
		}

		int i;

		for (i = 0; i < firstDay; i++) {
			days[i + 7].setVisible(false);
			days[i + 7].setText("");
		}

		tmpCalendar.add(Calendar.MONTH, 1);
		Date firstDayInNextMonth = tmpCalendar.getTime();
		tmpCalendar.add(Calendar.MONTH, -1);

		Date day = tmpCalendar.getTime();
		int n = 0;
		Color foregroundColor = getForeground();
		while (day.before(firstDayInNextMonth)) {
			days[i + n + 7].setText(Integer.toString(n + 1));
			days[i + n + 7].setVisible(true);
			if (tmpCalendar.get(Calendar.DAY_OF_YEAR) ==
					today.get(Calendar.DAY_OF_YEAR) &&
					tmpCalendar.get(Calendar.YEAR) ==
					today.get(Calendar.YEAR)) {
				days[i + n + 7].setForeground(colorRed);
			} else {
				days[i + n + 7].setForeground(foregroundColor);
			}

			if (n + 1 == this.day) {
				days[i + n + 7].setBackground(selectedColor);
				selectedDay = days[i + n + 7];
			} else {
				days[i + n + 7].setBackground(oldDayBackgroundColor);
			}

			n++;
			tmpCalendar.add(Calendar.DATE, 1);
			day = tmpCalendar.getTime();
		}

		for (int k = n + i + 7; k < 49; k++) {
			days[k].setVisible(false);
			days[k].setText("");
		}
	}


	/**
	 *  Returns the locale.
	 *
	 *@return    The locale value
	 *@see       #setLocale
	 */
	public Locale getLocale() {
		return locale;
	}


	/**
	 *  Sets the locale.
	 *
	 *@param  l  The new locale value
	 *@see       #getLocale
	 */
	public void setLocale(Locale l) {
		if (!initialized) {
			super.setLocale(l);
		} else {
			locale = l;
			init();
		}
	}


	/**
	 *  Sets the day. This is a bound property.
	 *
	 *@param  d  the day
	 *@see       #getDay
	 */
	public void setDay(int d) {
		if (d < 1) {
			d = 1;
		}

		Calendar tmpCalendar = (Calendar) calendar.clone();
		tmpCalendar.set(Calendar.DAY_OF_MONTH, 1);
		tmpCalendar.add(Calendar.MONTH, 1);
		tmpCalendar.add(Calendar.DATE, -1);
		int maxDaysInMonth = tmpCalendar.get(Calendar.DATE);

		if (d > maxDaysInMonth) {
			d = maxDaysInMonth;
		}

		int oldDay = day;
		day = d;

		if (selectedDay != null) {
			selectedDay.setBackground(oldDayBackgroundColor);
			selectedDay.repaint();
			// Bug: needed for Swing 1.0.3
		}

		for (int i = 7; i < 49; i++) {
			if (days[i].getText().equals(Integer.toString(day))) {
				selectedDay = days[i];
				selectedDay.setBackground(selectedColor);
				break;
			}
		}
		firePropertyChange("day", oldDay, day);
	}


	/**
	 *  Returns the selected day.
	 *
	 *@return    The day value
	 *@see       #setDay
	 */
	public int getDay() {
		return day;
	}


	/**
	 *  Sets a specific month. This is needed for correct graphical representation
	 *  of the days.
	 *
	 *@param  month  the new month
	 */
	public void setMonth(int month) {
		calendar.set(Calendar.MONTH, month);
		setDay(day);
		drawDays();
	}


	/**
	 *  Sets a specific year. This is needed for correct graphical representation
	 *  of the days.
	 *
	 *@param  year  the new year
	 */
	public void setYear(int year) {
		calendar.set(Calendar.YEAR, year);
		drawDays();
	}


	/**
	 *  Sets a specific calendar. This is needed for correct graphical
	 *  representation of the days.
	 *
	 *@param  c  the new calendar
	 */
	public void setCalendar(Calendar c) {
		calendar = c;
		drawDays();
	}


	/**
	 *  Sets the font property.
	 *
	 *@param  font  the new font
	 */
	public void setFont(Font font) {
		if (days != null) {
			for (int i = 0; i < 49; i++) {
				days[i].setFont(font);
			}
		}
	}


	/**
	 *  Sets the foregroundColor color.
	 *
	 *@param  fg  the new foregroundColor
	 */
	public void setForeground(Color fg) {
		super.setForeground(fg);
		if (days != null) {
			for (int i = 7; i < 49; i++) {
				days[i].setForeground(fg);
			}
			drawDays();
		}
	}


	/**
	 *  Returns "JDayChooser".
	 *
	 *@return    The name value
	 */
	public String getName() {
		return "JDayChooser";
	}


	/**
	 *  JDayChooser is the ActionListener for all day buttons.
	 *
	 *@param  e  Description of the Parameter
	 */
	public void actionPerformed(ActionEvent e) {
		JButton button = (JButton) e.getSource();
		String buttonText = button.getText();
		int day = new Integer(buttonText).intValue();
		setDay(day);
	}


	/**
	 *  JDayChooser is the FocusListener for all day buttons. (Added by Thomas
	 *  Schaefer)
	 *
	 *@param  e  Description of the Parameter
	 */
	public void focusGained(FocusEvent e) {
		JButton button = (JButton) e.getSource();
		String buttonText = button.getText();
		if (buttonText != null && !buttonText.equals("")) {
			actionPerformed(new ActionEvent(e.getSource(), 0, null));
		}
	}


	/**
	 *  Does nothing.
	 *
	 *@param  e  Description of the Parameter
	 */
	public void focusLost(FocusEvent e) {
	}


	/**
	 *  JDayChooser is the KeyListener for all day buttons. (Added by Thomas
	 *  Schaefer)
	 *
	 *@param  e  Description of the Parameter
	 */
	public void keyPressed(KeyEvent e) {
		int offset = e.getKeyCode() == KeyEvent.VK_UP ? -7 :
				e.getKeyCode() == KeyEvent.VK_DOWN ? +7 :
				e.getKeyCode() == KeyEvent.VK_LEFT ? -1 :
				e.getKeyCode() == KeyEvent.VK_RIGHT ? +1 : 0;

		if (offset != 0) {
			for (int i = getComponentCount() - 1; i >= 0; --i) {
				if (getComponent(i) == selectedDay) {
					i += offset;
					if (i > 7 && i < days.length && days[i].isVisible()) {
						days[i].requestFocus();
						//int day = new Integer(days[i].getText()).intValue();
						//setDay( day );
					}
					break;
				}
			}
		}
	}


	/**
	 *  Does nothing.
	 *
	 *@param  e  Description of the Parameter
	 */
	public void keyTyped(KeyEvent e) {
	}


	/**
	 *  Does nothing.
	 *
	 *@param  e  Description of the Parameter
	 */
	public void keyReleased(KeyEvent e) {
	}


	/**
	 *  Enable or disable the JDayChooser.
	 *
	 *@param  enabled  The new enabled value
	 */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		for (short i = 0; i < days.length; i++) {
			if (days[i] != null) {
				days[i].setEnabled(enabled);
			}
		}
	}


	/**
	 *  Creates a JFrame with a JDayChooser inside and can be used for testing.
	 *
	 *@param  s  The command line arguments
	 */
	public static void main(String[] s) {
		JFrame frame = new JFrame("JDayChooser");
		frame.getContentPane().add(new JDayChooser());
		frame.pack();
		frame.setVisible(true);
	}


	private JButton days[];
	private JButton selectedDay;
	private int day;
	private Color oldDayBackgroundColor;
	private Color selectedColor;
	private Color colorRed;
	private Color colorBlue;
	private String dayNames[];
	private Calendar calendar;
	private Calendar today;
	private Locale locale;
	private boolean initialized = false;
}



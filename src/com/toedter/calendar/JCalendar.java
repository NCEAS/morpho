/*
 *  02/02/2002 - 20:54:54
 *
 *  JCalendar.java - JCalendar Java Bean
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
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/**
 *  JCalendar is a bean for entering a date by choosing the year, month and day.
 *
 *@author     Kai Toedter
 *@version    1.1.4 07/16/02
 */
public class JCalendar extends JPanel implements PropertyChangeListener {
	/**
	 *  Default JCalendar constructor.
	 */
	public JCalendar() {
		this(JMonthChooser.RIGHT_SPINNER);
	}


	/**
	 *  JCalendar constructor with month spinner parameter.
	 *
	 *@param  monthSpinner  Possible values are JMonthChooser.RIGHT_SPINNER,
	 *      JMonthChooser.LEFT_SPINNER, JMonthChooser.NO_SPINNER
	 */
	public JCalendar(int monthSpinner) {
		// needed for setFont() etc.
		dayChooser = null;
		monthChooser = null;
		yearChooser = null;

		locale = Locale.getDefault();
		calendar = Calendar.getInstance();

		setLayout(new BorderLayout());
		JPanel myPanel = new JPanel();
		myPanel.setLayout(new GridLayout(1, 3));
		monthChooser = new JMonthChooser(monthSpinner);
		yearChooser = new JYearChooser();
		monthChooser.setYearChooser(yearChooser);
		myPanel.add(monthChooser);
		myPanel.add(yearChooser);
		dayChooser = new JDayChooser();
		dayChooser.addPropertyChangeListener(this);
		monthChooser.setDayChooser(dayChooser);
		monthChooser.addPropertyChangeListener(this);
		yearChooser.setDayChooser(dayChooser);
		yearChooser.addPropertyChangeListener(this);
		add(myPanel, BorderLayout.NORTH);
		add(dayChooser, BorderLayout.CENTER);
		initialized = true;
	}


	/**
	 *  Sets the calendar attribute of the JCalendar object
	 *
	 *@param  c       The new calendar value
	 *@param  update  The new calendar value
	 */
	private void setCalendar(Calendar c, boolean update) {
		Calendar oldCalendar = calendar;
		calendar = c;
		if (update) {
			// Thanks to Jeff Ulmer for correcting a bug in the sequence :)
			yearChooser.setYear(c.get(Calendar.YEAR));
			monthChooser.setMonth(c.get(Calendar.MONTH));
			dayChooser.setDay(c.get(Calendar.DATE));
		}
		firePropertyChange("calendar", oldCalendar, calendar);
	}


	/**
	 *  Sets the calendar property. This is a bound property.
	 *
	 *@param  c  the new calendar
	 *@see       #getCalendar
	 */
	public void setCalendar(Calendar c) {
		setCalendar(c, true);
	}


	/**
	 *  Returns the calendar property.
	 *
	 *@return    the value of the calendar property.
	 *@see       #setCalendar
	 */
	public Calendar getCalendar() {
		return calendar;
	}


	/**
	 *  Sets the locale property. This is a bound property.
	 *
	 *@param  l  The new locale value
	 *@see       #getLocale
	 */
	public void setLocale(Locale l) {
		if (!initialized) {
			super.setLocale(l);
		} else {
			Locale oldLocale = locale;
			locale = l;
			dayChooser.setLocale(locale);
			monthChooser.setLocale(locale);
			firePropertyChange("locale", oldLocale, locale);
		}
	}


	/**
	 *  Returns the locale.
	 *
	 *@return    the value of the locale property.
	 *@see       #setLocale
	 */
	public Locale getLocale() {
		return locale;
	}


	/**
	 *  Sets the font property.
	 *
	 *@param  font  the new font
	 */
	public void setFont(Font font) {
		super.setFont(font);
		if (dayChooser != null) {
			dayChooser.setFont(font);
			monthChooser.setFont(font);
			yearChooser.setFont(font);
		}
	}


	/**
	 *  Sets the foreground color.
	 *
	 *@param  fg  the new foreground
	 */
	public void setForeground(Color fg) {
		super.setForeground(fg);
		if (dayChooser != null) {
			dayChooser.setForeground(fg);
			monthChooser.setForeground(fg);
			yearChooser.setForeground(fg);
		}
	}


	/**
	 *  Sets the background color.
	 *
	 *@param  bg  the new background
	 */
	public void setBackground(Color bg) {
		super.setBackground(bg);
		if (dayChooser != null) {
			dayChooser.setBackground(bg);
		}
	}


	/**
	 *  JCalendar is a PropertyChangeListener, for its day, month and year chooser.
	 *
	 *@param  evt  Description of the Parameter
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (calendar != null) {
			Calendar c = (Calendar) calendar.clone();
			if (evt.getPropertyName().equals("day")) {
				c.set(Calendar.DAY_OF_MONTH,
						((Integer) evt.getNewValue()).intValue());
				setCalendar(c, false);
			} else if (evt.getPropertyName().equals("month")) {
				c.set(Calendar.MONTH,
						((Integer) evt.getNewValue()).intValue());
				setCalendar(c, false);
			} else if (evt.getPropertyName().equals("year")) {
				c.set(Calendar.YEAR,
						((Integer) evt.getNewValue()).intValue());
				setCalendar(c, false);
			}
		}
	}


	/**
	 *  Returns "JCalendar".
	 *
	 *@return    The name value
	 */
	public String getName() {
		return "JCalendar";
	}


	/**
	 *  Enable or disable the JCalendar.
	 *
	 *@param  enabled  The new enabled value
	 */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (dayChooser != null) {
			dayChooser.setEnabled(enabled);
			monthChooser.setEnabled(enabled);
			yearChooser.setEnabled(enabled);
		}
	}


	/**
	 *  Gets the dayChooser attribute of the JCalendar object
	 *
	 *@return    The dayChooser value
	 */
	public JDayChooser getDayChooser() {
		return dayChooser;
	}


	/**
	 *  Gets the monthChooser attribute of the JCalendar object
	 *
	 *@return    The monthChooser value
	 */
	public JMonthChooser getMonthChooser() {
		return monthChooser;
	}


	/**
	 *  Gets the yearChooser attribute of the JCalendar object
	 *
	 *@return    The yearChooser value
	 */
	public JYearChooser getYearChooser() {
		return yearChooser;
	}


	/**
	 *  Creates a JFrame with a JCalendar inside and can be used for testing.
	 *
	 *@param  s  The command line arguments
	 */
	public static void main(String[] s) {
		JFrame frame = new JFrame("JCalendar");
		frame.getContentPane().add(new JCalendar());
		frame.pack();
		frame.setVisible(true);
	}


	/**
	 *  the year chhoser
	 */
	protected JYearChooser yearChooser;

	/**
	 *  the month chooser
	 */
	protected JMonthChooser monthChooser;

	/**
	 *  the day chooser
	 */
	protected JDayChooser dayChooser;

	private Calendar calendar;
	private Locale locale;
	private boolean initialized = false;
}


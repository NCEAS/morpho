/*
 *  06/27/2002 - 20:54:54
 *
 *  JMonthChooser.java  - A bean for choosing a month
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
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.text.*;
import javax.swing.*;

/**
 *  JMonthChooser is a bean for choosing a month.
 *
 *@author     Kai Toedter
 *@version    1.1.3 07/16/02
 */
public class JMonthChooser extends JPanel implements ItemListener,
		AdjustmentListener {

	/**
	 *  Displays a JSpinField on the right
	 */
	public final static int RIGHT_SPINNER = 0;
	/**
	 *  Displays a JSpinField on the left
	 */
	public final static int LEFT_SPINNER = 1;
	/**
	 *  Displays no JSpinField
	 */
	public final static int NO_SPINNER = 2;


	/**
	 *  Default JMonthChooser constructor.
	 */
	public JMonthChooser() {
		this(RIGHT_SPINNER);
	}


	/**
	 *  JMonthChooser constructor with month spinner parameter.
	 *
	 *@param  spinner  Possible values are RIGHT_SPINNER, LEFT_SPINNER, NO_SPINNER
	 */
	public JMonthChooser(int spinner) {
		super();

		setLayout(new BorderLayout());

		comboBox = new JComboBox();
		comboBox.addItemListener(this);
		dayChooser = null;
		locale = Locale.getDefault();
		initNames();
		add(comboBox, BorderLayout.CENTER);

		if (spinner != NO_SPINNER) {
			// 10000 possible clicks in both directions should be enough :)
			scrollBar =
					new JScrollBar(Adjustable.VERTICAL, 0, 0, -10000,
					10000);
			scrollBar.setPreferredSize(
					new Dimension(scrollBar.getPreferredSize().width,
					this.getPreferredSize().height));
			scrollBar.setVisibleAmount(0);
			scrollBar.addAdjustmentListener(this);

			if (spinner == RIGHT_SPINNER) {
				add(scrollBar, BorderLayout.EAST);
			} else {
				add(scrollBar, BorderLayout.WEST);
			}
		}

		initialized = true;
		setMonth(Calendar.getInstance().get(Calendar.MONTH));
	}


	/**
	 *  Initializes the locale specific month names.
	 */
	public void initNames() {
		localInitialize = true;

		DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(locale);
		String[] monthNames = dateFormatSymbols.getMonths();

		if (comboBox.getItemCount() == 12) {
			comboBox.removeAllItems();
		}
		for (int i = 0; i < 12; i++) {
			comboBox.addItem(monthNames[i]);
		}

		localInitialize = false;
		comboBox.setSelectedIndex(month);
	}


	/**
	 *  The ItemListener for the months.
	 *
	 *@param  iEvt  Description of the Parameter
	 */
	public void itemStateChanged(ItemEvent iEvt) {
		int index = comboBox.getSelectedIndex();
		if (index >= 0) {
			setMonth(index, false);
		}
	}


	/**
	 *  The 2 buttons are implemented with a JScrollBar.
	 *
	 *@param  e  Description of the Parameter
	 */
	public void adjustmentValueChanged(AdjustmentEvent e) {
		boolean increase = true;
		int newScrollBarValue = e.getValue();
		if (newScrollBarValue > oldScrollBarValue) {
			increase = false;
		}
		oldScrollBarValue = newScrollBarValue;
		int month = getMonth();
		if (increase) {
			month += 1;
			if (month == 12) {
				month = 0;
				if (yearChooser != null) {
					int year = yearChooser.getYear();
					year += 1;
					yearChooser.setYear(year);
				}
			}
		} else {
			month -= 1;
			if (month == -1) {
				month = 11;
				if (yearChooser != null) {
					int year = yearChooser.getYear();
					year -= 1;
					yearChooser.setYear(year);
				}
			}
		}
		setMonth(month);
	}


	/**
	 *  Sets the month attribute of the JMonthChooser object
	 *
	 *@param  newMonth  The new month value
	 *@param  select    The new month value
	 */
	private void setMonth(int newMonth, boolean select) {
		if (!initialized || localInitialize) {
			return;
		}

		int oldMonth = month;
		month = newMonth;
		if (select) {
			comboBox.setSelectedIndex(month);
		}
		if (dayChooser != null) {
			dayChooser.setMonth(month);
		}
		firePropertyChange("month", oldMonth, month);
	}


	/**
	 *  Sets the month. This is a bound property.
	 *
	 *@param  newMonth  The new month value
	 *@see              #getMonth
	 */
	public void setMonth(int newMonth) {
		setMonth(newMonth, true);
	}


	/**
	 *  Returns the month.
	 *
	 *@return    The month value
	 *@see       #setMonth
	 */
	public int getMonth() {
		return month;
	}


	/**
	 *  Convenience method set a day chooser.
	 *
	 *@param  dayChooser  the day chooser
	 */
	public void setDayChooser(JDayChooser dayChooser) {
		this.dayChooser = dayChooser;
	}


	/**
	 *  Convenience method set a year chooser. If set, the spin buttons will spin
	 *  the year as well
	 *
	 *@param  yearChooser  The new yearChooser value
	 */
	public void setYearChooser(JYearChooser yearChooser) {
		this.yearChooser = yearChooser;
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
	 *  Set the locale and initializes the new month names.
	 *
	 *@param  l  The new locale value
	 *@see       #getLocale
	 */
	public void setLocale(Locale l) {
		if (!initialized) {
			super.setLocale(l);
		} else {
			locale = l;
			initNames();
		}
	}


	/**
	 *  Enable or disable the JMonthChooser.
	 *
	 *@param  enabled  The new enabled value
	 */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		comboBox.setEnabled(enabled);
		if (scrollBar != null) {
			scrollBar.setEnabled(enabled);
		}
	}


	/**
	 *  Creates a JFrame with a JMonthChooser inside and can be used for testing.
	 *
	 *@param  s  The command line arguments
	 */
	public static void main(String[] s) {
		JFrame frame = new JFrame("MonthChooser");
		frame.getContentPane().add(new JMonthChooser());
		frame.pack();
		frame.setVisible(true);
	}


	private Locale locale;
	private int month;
	private int oldScrollBarValue = 0;
	// needed for comparison
	private JDayChooser dayChooser = null;
	private JYearChooser yearChooser = null;
	private JComboBox comboBox;
	private JScrollBar scrollBar;
	private boolean initialized = false;
	private boolean localInitialize = false;
}



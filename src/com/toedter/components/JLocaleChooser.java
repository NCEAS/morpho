/*
* 02/02/2002 - 20:54:54
*
* JLocaleChooser.java  - A bean for choosing locales
* Copyright (C) 2002 Kai Toedter
* kai@toedter.com
* www.toedter.com
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package com.toedter.components;

import java.beans.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;

/**
 * JLocaleChooser is a bean for choosing locales.
 *
 * @version  1.1 02/04/02
 * @author  Kai Toedter
 */
public class JLocaleChooser extends JComboBox implements ItemListener {
    /**
     * Default JLocaleChooser constructor.
     */
    public JLocaleChooser() {
        super();
        addItemListener(this);
        locales = Calendar.getAvailableLocales();
        localeCount = locales.length;

        for (int i = 0; i < localeCount; i++) {
            if (locales[i].getCountry().length() > 0) {
                addItem(locales[i].getDisplayName());
            }
        }

        setLocale(Locale.getDefault());
    }

    /**
     * The ItemListener for the locales.
     */
    public void itemStateChanged(ItemEvent iEvt) {
        String item = (String) iEvt.getItem();
        int i;

        for (i = 0; i < localeCount; i++) {
            if (locales[i].getDisplayName().equals(item))
                break;
        }
        setLocale(locales[i], false);
    }

    /**
     * Sets the locale.
     *
     * @see #getLocale
     */
    private void setLocale(Locale l, boolean select) {
        Locale oldLocale = locale;
        locale = l;
        int n = 0;

        if (select) {
            for (int i = 0; i < localeCount; i++) {
                if (locales[i].getCountry().length() > 0) {
                    if (locales[i].equals(locale))
                        setSelectedIndex(n);
                    n += 1;
                }
            }
        }

        firePropertyChange("locale", oldLocale, locale);
    }

    /**
     * Sets the locale.
     * This is a bound property.
     *
     * @see #getLocale
     */
    public void setLocale(Locale l) {
        setLocale(l, true);
    }

    /**
     * Returns the locale.
     *
     * @see #setLocale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Creates a JFrame with a JLocaleChooser inside and can be used for testing.
     */
    static public void main(String[] s) {
        JFrame frame = new JFrame("LocaleChooser");
        frame.getContentPane().add(new JLocaleChooser());
        frame.pack();
        frame.setVisible(true);
    }

    private Locale[] locales;
    private Locale locale;
    private int localeCount;
}


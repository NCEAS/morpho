/*
* 02/02/2002 - 20:54:54
*
* JSpinFieldBeanInfo.java  - Bean Info for JSpinField
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

import java.util.Locale;
import java.awt.Image;
import java.beans.*;
import javax.swing.*;


/**
 * A BeanInfo class for the JSpinField bean.
 *
 * @version 1.1 02/04/02
 * @author  Kai Toedter
 */
public class JSpinFieldBeanInfo extends SimpleBeanInfo
{

   /** 16x16 color icon. */
   Image icon;
   /** 32x32 color icon. */
   Image icon32;
   /** 16x16 mono icon. */
   Image iconM;
   /** 32x32 mono icon. */
   Image icon32M;

   /**
    * Constructs a new BeanInfo class for the JSpinField bean.
    */
   public JSpinFieldBeanInfo()
   {
      icon    = loadImage ("images/JSpinFieldColor16.gif");
      icon32  = loadImage ("images/JSpinFieldColor32.gif");
      iconM   = loadImage ("images/JSpinFieldMono16.gif");
      icon32M = loadImage ("images/JSpinFieldMono32.gif");
   }

   /**
    * This method returns an image object that can be used 
    * to represent the bean in toolboxes, toolbars, etc.
    */
   public Image getIcon( int iconKind )
   {
      switch( iconKind )
      {
      case ICON_COLOR_16x16: return icon;
      case ICON_COLOR_32x32: return icon32;
      case ICON_MONO_16x16: return iconM;
      case ICON_MONO_32x32: return icon32M;
      }
      return null;
   }
}


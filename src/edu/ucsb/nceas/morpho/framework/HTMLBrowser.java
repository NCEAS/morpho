/**
 *        Name: HTMLBrowser.java
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @higgins@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-04-14 20:59:18 $'
 * '$Revision: 1.3 $'
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.ucsb.nceas.morpho.framework;

import java.net.URL;
import java.util.Stack;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.JFrame;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.morpho.util.Log;


/**
 * This class is a 'quick and dirty' HTML browser written in
 * Java. It is designed as a simple means of displaying HTML
 * help files within a Java application (Thus avoiding the
 * problem of launching a local browser in a platform independent
 * manner.)
 */
public class HTMLBrowser {

  private static final Cursor waitCursor =
      Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);

  private static final Cursor defaultCursor =
      Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

  private static final Cursor handCursor =
      Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

  private boolean loadingPage = false;

  private Stack pageList;


  // Used by addNotify
  private boolean frameSizeAdjusted = false;

  private MorphoFrame frame;

  private JEditorPane HTMLPane = new JEditorPane();

  private JButton backButton = new JButton();

  private JTextField URLTextField = new JTextField();

  private JButton loadButton = new JButton();


  /**
   * Creates a new instance of JFrame1 with the given title.
   * @param sTitle the title for the new frame.
   * @see #JFrame1()
   */
  public HTMLBrowser() {
    this(UIController.getInstance().addWindow("Browser"));
  }


  /**
   * Creates a new instance of JFrame1 with the given title.
   * @param sTitle the title for the new frame.
   * @see #JFrame1()
   */
  public HTMLBrowser(String sTitle) {
    this(UIController.getInstance().addWindow(sTitle));
    frame.setTitle(sTitle);
  }


  public HTMLBrowser(MorphoFrame frame) {

    this.frame = frame;
    frame.setSize((int)UISettings.DEFAULT_WINDOW_WIDTH,
                  (int)UISettings.DEFAULT_WINDOW_HEIGHT);
    JPanel mainPanel = new JPanel();
    JScrollPane JScrollPane1 = new JScrollPane();
    JPanel controlsPanel = new JPanel();

    frame.setTitle("");

    mainPanel.setLayout(new BorderLayout(0, 0));
    frame.setMainContentPane(mainPanel);

    JScrollPane1.setOpaque(true);
    JScrollPane1.getViewport().setBackground(Color.white);
    JScrollPane1.getViewport().add(HTMLPane);
    mainPanel.add(BorderLayout.CENTER, JScrollPane1);

    HTMLPane.setEditable(false);
    controlsPanel.setLayout(new BorderLayout(5, 5));
    mainPanel.add(BorderLayout.NORTH, controlsPanel);

    backButton.setText("< Back");
    controlsPanel.add(BorderLayout.WEST, backButton);
    URLTextField.setText("");
    URLTextField.setColumns(40);
    controlsPanel.add(BorderLayout.CENTER, URLTextField);
    loadButton.setText("Go");
    controlsPanel.add(BorderLayout.EAST, loadButton);

    //REGISTER_LISTENERS
    ButtonAction lButtonAction = new ButtonAction();
    loadButton.addActionListener(lButtonAction);
    backButton.addActionListener(lButtonAction);

    pageList = new Stack();

    // Listener for hypertext events
    HTMLPane.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent evt) {
        // Ignore hyperlink events if the frame is busy
        if (loadingPage == true) {
          return;
        }
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          JEditorPane sp = (JEditorPane)evt.getSource();
          if (evt instanceof HTMLFrameHyperlinkEvent) {
            HTMLDocument doc = (HTMLDocument)sp.getDocument();
            doc.processHTMLFrameHyperlinkEvent((HTMLFrameHyperlinkEvent)evt);
          } else {
            loadNewPage(evt.getURL());
          }
        } else if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) {
          HTMLPane.setCursor(handCursor);
        } else if (evt.getEventType() == HyperlinkEvent.EventType.EXITED) {
          HTMLPane.setCursor(defaultCursor);
        }
      }
    });
  }

  /**
   *
   * @param page Object can be a URL object or a String url
   */
  public void loadNewPage(Object page) {

    HTMLPane.setCursor(waitCursor);
    URL url = null;
    try {

      if (page instanceof URL) {
        url = (URL)page;
      } else {
        url = new URL((String)page);
      }
      if (url==null) throw new java.lang.IllegalArgumentException("URL IS NULL!");
      pageList.push(url);
      HTMLPane.setPage(url);
      URLTextField.setText(url.toString());

    } catch (Exception e) {
      e.printStackTrace();
      Log.debug(1, "Cannot load page: "+url);
    } finally {
      HTMLPane.setCursor(defaultCursor);
    }
  }




  public void setVisible(boolean visible) {

    frame.setVisible(visible);
  }


//  /**
//   * Notifies this component that it has been added to a container
//   * This method should be called by <code>Container.add</code>, and
//   * not by user code directly.
//   * Overridden here to adjust the size of the frame if needed.
//   * @see java.awt.Container#removeNotify
//   */
//  public void addNotify() {
//    // Record the size of the window prior to calling parents addNotify.
//    Dimension size = frame.getSize();
//
//    frame.addNotify();
//
//    if (frameSizeAdjusted)
//      return;
//    frameSizeAdjusted = true;
//
//    // Adjust size of frame according to the insets and menu bar
//    JMenuBar menuBar = frame.getRootPane().getJMenuBar();
//    int menuBarHeight = 0;
//    if (menuBar != null)
//      menuBarHeight = menuBar.getPreferredSize().height;
//    Insets insets = frame.getInsets();
//    frame.setSize(insets.left + insets.right + size.width,
//                  insets.top + insets.bottom + size.height + menuBarHeight);
//  }



  class ButtonAction implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      Object object = event.getSource();
      if (object == loadButton)
        loadButton_actionPerformed(event);
      else if (object == backButton)
        backButton_actionPerformed(event);

    }
  }

  void loadButton_actionPerformed(ActionEvent event) {
    String url = URLTextField.getText();
    pageList.push(url);
    try {
      HTMLPane.setPage(url);
    } catch (Exception e) {
      System.out.println("Problem loading URL");
    }
  }


  void backButton_actionPerformed(ActionEvent event) {
    pageList.pop();
    Object url = pageList.pop();
    loadNewPage(url);
  }




  /**
   * The entry point for this application. Sets the Look and Feel to the System
   * Look and Feel. Creates a new JFrame1 and makes it visible.
   *
   * @param args String[]
   */
  static public void main(String args[]) {
    try {
      // Add the following code if you want the Look and Feel
      // to be set to the Look and Feel of the native system.
      /*
               try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
               }
               catch (Exception e) {
               }
       */

      //Create a new instance of our application's frame, and make it visible.
      (new HTMLBrowser()).setVisible(true);
    } catch (Throwable t) {
      t.printStackTrace();
      //Ensure the application exits with an error condition.
      System.exit(1);
    }
  }

}

/**
 *        Name: HTMLBrowser.java
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @higgins@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-04-15 17:07:55 $'
 * '$Revision: 1.5 $'
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

import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;

import java.net.URL;
import java.util.Stack;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import java.awt.event.ActionListener;


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
  private JToolBar toolBar;

  private JEditorPane HTMLPane = new JEditorPane();

  private Action backAction;

  private JTextField urlTextField = new JTextField();

  private Action loadAction;

  /**
   * Creates a new instance of JFrame1 with the given title.
   *
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

    JPanel mainPanel = new JPanel();
    JScrollPane JScrollPane1 = new JScrollPane();

    frame.setTitle("");

    mainPanel.setLayout(new BorderLayout(0, 0));
    frame.setMainContentPane(mainPanel);

    JScrollPane1.setOpaque(true);
    JScrollPane1.getViewport().setBackground(Color.white);
    JScrollPane1.getViewport().add(HTMLPane);
    mainPanel.add(BorderLayout.CENTER, JScrollPane1);

    HTMLPane.setEditable(false);

    toolBar = frame.getJToolBar();
    toolBar.removeAll();

    backAction = new GUIAction("< Back", null, new Command() {

      public void execute(ActionEvent e) {

        //use < 2, not < 1 ot empty, since current
        // page is put on stack before displaying
        if (pageList.size() < 2)return;
        //remove current page..
        pageList.pop();
        //...and get preious page
        Object url = pageList.pop();
        loadNewPage(url);
      }
    });

    loadAction = new GUIAction("Go", null, new Command() {

       public void execute(ActionEvent e) {

         String url = urlTextField.getText();
         pageList.push(url);
         try {
           loadNewPage(url);
         } catch (Exception ex) {
           ex.printStackTrace();
           Log.debug(1, "Cannot open page: "+url);
         }
       }
     });

    backAction.setEnabled(false);

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    //IMPORTANT NOTE: if you change the order in which the following components
    //are added, or if you add new components, you must also edit the code in
    //setTextFieldDims() that gets the component sizes
    ////////////////////////////////////////////////////////////////////////////

    toolBar.add(backAction);

    toolBar.addSeparator();

    toolBar.add(urlTextField);

    toolBar.addSeparator();

    toolBar.add(loadAction);

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    urlTextField.setText("");

    setTextFieldDims();

    urlTextField.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        loadAction.actionPerformed(e);
      }
    });
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
    enableActions();
    frame.setSize((int)UISettings.DEFAULT_WINDOW_WIDTH,
                  (int)UISettings.DEFAULT_WINDOW_HEIGHT);

    frame.addComponentListener(
        new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        setTextFieldDims();
      }
    });
  }


  private void enableActions() {

    //use > 1, not > 0, since first page is put on stack before displaying
    backAction.setEnabled(pageList.size() > 1);
  }


  private void setTextFieldDims() {

    Dimension toolBarDims = frame.getJToolBarDims();
    int toolBarWidth = toolBarDims.width;
    int toolBarHeight = toolBarDims.height;
    int backButtonWidth = toolBar.getComponent(0).getWidth();
    int dividerWidth = toolBar.getComponent(1).getWidth();
    int goButtonWidth = toolBar.getComponent(4).getWidth();

    int textFieldWidth = toolBarWidth - backButtonWidth
                         - 2*dividerWidth - goButtonWidth - 10;
    Dimension textFieldDims = new Dimension(textFieldWidth, toolBarHeight);
    urlTextField.setPreferredSize(textFieldDims);
    urlTextField.setMaximumSize(textFieldDims);

    urlTextField.invalidate();
    frame.validate();
    frame.repaint();
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
      urlTextField.setText(url.toString());

    } catch (Exception e) {
      e.printStackTrace();
      Log.debug(1, "Cannot open page: "+url);
    } finally {
      HTMLPane.setCursor(defaultCursor);
      enableActions();
    }
  }




  public void setVisible(boolean visible) {

    frame.setVisible(visible);
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

/**
 *  '$RCSfile: HelpMetadataIntroCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-04-14 05:25:04 $'
 * '$Revision: 1.1 $'
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

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.IOUtil;
import edu.ucsb.nceas.morpho.util.Log;

import java.io.IOException;
import java.io.Reader;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;


public class HelpMetadataIntroCommand implements Command, HyperlinkListener {


  public HelpMetadataIntroCommand(Morpho morpho) {}

  public void execute(ActionEvent ae) {

    AbstractDataPackage adp = UIController.getInstance().
                              getCurrentAbstractDataPackage();
    if (adp == null) {
      Log.debug(16, " Abstract Data Package is null in the Print Plugin");
      return;
    }
    String htmlDoc = null;
    Reader resultReader = null;
    StringBuffer sb = null;
    try {
      sb = IOUtil.getAsStringBuffer(resultReader, true);
    } catch (IOException ex) {
      Log.debug(5, "\n**ERROR: can't open Metadata Guide!");
      return;
    }
    htmlDoc = sb.toString();

    display(htmlDoc);
  }


  public void display(String displayString) {

    if (displayString == null || displayString.trim().equals("")) {
      return;
    }
    displayString = processHTMLString(displayString);


    initDisplayFrame(displayString);
  }


  private void initDisplayFrame(String displayString) {

    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    JEditorPane viewer = new JEditorPane();
    JScrollPane scrollPane = new JScrollPane(viewer,
                                             JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                             JScrollPane.
                                             HORIZONTAL_SCROLLBAR_AS_NEEDED);
    viewer.setContentType("text/html");
    viewer.setText(displayString);
    viewer.setEditable(false);
    viewer.addHyperlinkListener(this);
    viewer.setCaretPosition(0);

    panel.add(scrollPane, BorderLayout.CENTER);
    MorphoFrame frame = MorphoFrame.getInstance();
    frame.getContentPane().add(panel);
    frame.setVisible(true);
  }


  private String processHTMLString(String displayString) {
    displayString = stripHTMLMetaTags(displayString);
    displayString = stripComments(displayString);
    displayString = appendTrailingSpace(displayString);
    return displayString;
  }


  private static String stripHTMLMetaTags(String html) {

    int META_END = 0;
    final int META_START = html.indexOf("<META");

    if (META_START >= 0) {
      final char[] htmlChars = html.toCharArray();
      int charIndex = META_START;
      char nextChar = ' ';

      do {
        nextChar = htmlChars[charIndex];
        htmlChars[charIndex] = ' ';
        charIndex++;
      } while ((nextChar != '>') && (charIndex < htmlChars.length));

      html = String.valueOf(htmlChars);
      return stripHTMLMetaTags(html);

    }
    return html;

  }


  private static String stripComments(String html) {

    int prev = 0;
    String res = "";
    int pos = html.indexOf("<!--");
    if (pos < 0)return html;

    while (pos >= 0) {

      res += html.substring(prev, pos);
      html = html.substring(pos);
      int next = html.indexOf("-->");
      prev = 0;
      html = html.substring(next + 3);
      pos = html.indexOf("<!--");

    }
    res += html;

    return res;
  }


  private String appendTrailingSpace(String html) {
    int pos;
    pos = html.indexOf("<tail>");
    if (pos != -1)
      return html;
    pos = html.indexOf("</html>");
    if (pos == -1)
      return html;
    String init = html.substring(0, pos);
    init += "\n<tail>\n</tail>\n </html>\n";
    return init;

  }


  /**
   * hyperlinkUpdate
   *
   * @param e HyperlinkEvent
   */
  public void hyperlinkUpdate(HyperlinkEvent e) {
    Log.debug(5," hyperlinkUpdate called; \n eventType=" + e.getEventType()
              + "\n NEED TO IMPLEMENT!!");
  }

}

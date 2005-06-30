/**
*  '$RCSfile: ViewDocumentationCommand.java,v $'
*  Copyright: 2000 Regents of the University of California and the
*              National Center for Ecological Analysis and Synthesis
*    Authors: @authors@
*    Release: @release@
*
*   '$Author: sgarg $'
*     '$Date: 2005-06-30 16:16:55 $'
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

package edu.ucsb.nceas.morpho.datapackage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.JEditorPane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import java.awt.event.ActionEvent;

import java.io.Reader;
import java.io.File;
import java.io.IOException;
import org.w3c.dom.Document;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.util.DocumentNotFoundException;
import edu.ucsb.nceas.morpho.util.XMLTransformer;
import edu.ucsb.nceas.morpho.util.IOUtil;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;

/**
 * Class to handle view documentation command
 */
public class ViewDocumentationCommand implements Command
{
	private ConfigXML config;
        private final String CONFIG_KEY_CSS_LOCATION = "emlCSSLocation";
        private final String CONFIG_KEY_MCONFJAR_LOC   = "morphoConfigJarLocation";

	public ViewDocumentationCommand() {

		config = Morpho.getConfiguration();
	}


	public void execute(ActionEvent ae) {

		AbstractDataPackage adp = UIController.getInstance().getCurrentAbstractDataPackage();
		if(adp == null) {
			Log.debug(16, " Abstract Data Package is null in View Documentation");
			return;
		}

		XMLTransformer transformer = XMLTransformer.getInstance();
		transformer.addTransformerProperty(XMLTransformer.SELECTED_DISPLAY_XSLPROP,
		XMLTransformer.XSLVALU_DISPLAY_PRNT);
		transformer.addTransformerProperty( XMLTransformer.CSS_PATH_XSLPROP,
		getFullStylePath());
		Reader xmlReader = null, resultReader = null;
		String htmlDoc = "<html><head><h2>Error displaying the requested Document</h2></head></html>";
		String ID = "";
		try{
			ID = adp.getPackageId();
			if ((ID==null)||(ID.equals(""))) ID = "tempid";
			resultReader = null;
			Document doc = adp.openAsDom(ID);
			if (doc == null) {
				xmlReader = adp.openAsReader(ID);
				resultReader = transformer.transform(xmlReader);
			} else {
				resultReader = transformer.transform(doc);
			}
			StringBuffer sb = IOUtil.getAsStringBuffer(resultReader, true);
			htmlDoc = sb.toString();
		} catch (DocumentNotFoundException dnfe) {
			Log.debug(12, "DocumentNotFoundException getting Reader for ID: "
			+ID+"; "+dnfe.getMessage());
		} catch (IOException io) {
			Log.debug(12, "IOException while getting the string for ID:" + ID + "; "+ io);
		}	catch (Exception e) {
			Log.debug(12, "Exception during Transformation in ViewDocumentationCommand - " + e);
		}
		htmlDoc = processHTMLString(htmlDoc);
		JFrame parent = (JFrame)UIController.getInstance().getCurrentActiveWindow();
		JFrame frame = new DisplayFrame(htmlDoc , "text/html", new Dimension(UISettings.POPUPDIALOG_WIDTH, UISettings.POPUPDIALOG_HEIGHT));
		resetBounds(frame, parent);

	}

	private static String stripHTMLMetaTags(String html)
	{
		int META_END = 0;
		final int META_START = html.indexOf("<META");
		if (META_START>=0)  {
			final char[] htmlChars = html.toCharArray();
			int charIndex = META_START;
			char nextChar = ' ';
			do {
				nextChar = htmlChars[charIndex];
				htmlChars[charIndex] = ' ';
				charIndex++;
			} while ((nextChar!='>') && (charIndex < htmlChars.length));
			html = String.valueOf(htmlChars);
			return stripHTMLMetaTags(html);
		}
		return html;
	}

	private static String stripComments(String html)
	{
		int prev = 0;
		String res = "";
		int pos = html.indexOf("<!--");
		if(pos < 0) return html;

		while(pos>=0) {

			res += html.substring(prev, pos);
			html = html.substring(pos);
			int next = html.indexOf("-->");
			prev = 0;
			html = html.substring(next+3);
			pos = html.indexOf("<!--");

		}
		res += html;
		return res;
	}

	private String addTitleTag(String html)
	{
		int pos;
		pos = html.indexOf("<title>");
		if(pos != -1) return html;
		pos = html.indexOf("</head>");
		String init = "";
		if(pos != -1) {
			init = html.substring(0, pos);
			init += "<title></title>";
			init += html.substring(pos);
		} else {
			pos = html.indexOf("<body>");
			init = html.substring(0, pos);
			init += "<head><title></title></head>";
			init += html.substring(pos);
		}
		return init;
	}


	private String processHTMLString(String displayString) {
		displayString = stripHTMLMetaTags(displayString);
		displayString = stripComments(displayString);
		displayString = addTitleTag(displayString);
		return displayString;
	}

	private void resetBounds(JFrame frame, JFrame parent) {

		int xcoord, ycoord;
		if(parent == null) {
			xcoord = ycoord = 50;
		} else {
			xcoord = ( parent.getX() + parent.getWidth()/2 ) - frame.getWidth()/2;
			ycoord = ( parent.getY() + parent.getHeight()/2 ) - frame.getHeight()/2;
		}

		frame.setBounds(xcoord, ycoord,  frame.getWidth(), frame.getHeight());

	}

	private String getFullStylePath()
  {
			StringBuffer pathBuff = new StringBuffer();
      pathBuff.append("jar:file:");
      pathBuff.append(new File("").getAbsolutePath());
      pathBuff.append("/");
      pathBuff.append(config.get(CONFIG_KEY_MCONFJAR_LOC, 0));
      pathBuff.append("!/");
      pathBuff.append(config.get(CONFIG_KEY_CSS_LOCATION, 0));
      Log.debug(50,"ViewDocumentationCommand.getFullStylePath() returning: "
                                                              +pathBuff.toString());
      return pathBuff.toString();
  }

}


class DisplayFrame extends JFrame implements HyperlinkListener
{
	JEditorPane editor;
	JScrollPane scrollPane;

	DisplayFrame(String text, String contentType) {

		this(text, contentType, new Dimension(800, 600));
	}

	DisplayFrame(String text, String contentType, Dimension dim) {

		super();
		System.out.println("Type = " +contentType);
		System.out.println("Text = " + text);
		editor = new JEditorPane();
		editor.setContentType(contentType);
		editor.setText(text);
		editor.setEditable(false);
		editor.addHyperlinkListener(this);
		editor.setCaretPosition(0);
		scrollPane = new JScrollPane(editor, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(new JPanel(), BorderLayout.NORTH);
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		setSize((int)dim.getWidth(), (int)dim.getHeight());
		setVisible(true);
	}

	public void hyperlinkUpdate(HyperlinkEvent e)
	{
		Log.debug(50,"hyperlinkUpdate called in ViewDocumentation; eventType=" + e.getEventType());
		Log.debug(50,"hyperlinks not supported in ViewDocumentation Window");

	}

} // end of DisplayFrame class

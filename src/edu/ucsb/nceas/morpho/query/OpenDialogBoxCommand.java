/**
 *  '$RCSfile: OpenDialogBoxCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: leinfelder $'
 *     '$Date: 2008-11-27 00:47:17 $'
 * '$Revision: 1.19 $'
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
package edu.ucsb.nceas.morpho.query;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;

import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import java.awt.event.ActionEvent;

import org.dataone.client.auth.CertificateManager;
import org.dataone.service.types.v1.Subject;
import org.dataone.service.types.v1.SubjectInfo;
import org.dataone.service.types.v1.util.AuthUtils;


/**
 * Class to handle Open a dialog box command
 */
public class OpenDialogBoxCommand implements Command
{

  /** A reference to Morpho application */
  private Morpho morpho = null;

  /** A reference to the owner query*/
  private Query ownerQuery = null;

  /**
   * Constructor of SearchCommand
   *
   * @param morpho the Morpho app to which the cancel command will apply
   */
  public OpenDialogBoxCommand(Morpho morpho)
  {
    this.morpho = morpho;

  }//OpenDialogBoxCommand


  /**
   * execute cancel command
   *
   * @param event ActionEvent
   */
  public void execute(ActionEvent event)
  {
    // create ownerQuery depend on the suiation when it executed
    ownerQuery = new Query(getOwnerQuery(), morpho);
    ownerQuery.setSearchLocal(true);
    if (Morpho.thisStaticInstance.getDataONEDataStoreService().isQueryEngineSupported()) {
    	ownerQuery.setSearchNetwork(true);
    }
    // Get the current morphoFrame. Maybe change get open dialog parent
    MorphoFrame frame =
                    UIController.getInstance().getCurrentActiveWindow();

    // Open a open dialog
    if ( frame != null)
    {
      OpenDialogBox open = new OpenDialogBox(frame, morpho, ownerQuery);
      //doOpenDialog(frame);
    }


  }//execute


  /**
   * Using SwingWorket class to open open dialog
   *
   * @param morphoFrame MorphoFrame
   */
  private void doOpenDialog(final MorphoFrame morphoFrame)
  {

    final SwingWorker worker = new SwingWorker()
    {
        OpenDialogBox open = null;
        public Object construct()
        {
          // set frame butterfly flapping
          morphoFrame.setBusy(true);
          morphoFrame.setEnabled(false);
          open = new OpenDialogBox(morphoFrame, morpho, ownerQuery);
          return null;
        }

        //Runs on the event-dispatching thread.
        public void finished()
        {
          morphoFrame.setEnabled(true);
          morphoFrame.setBusy(false);
          // Set the open dialog box modal true
          if (open!=null) {
              open.setModal(true);
              open.setVisible(true);
          }
        }
    };
    worker.start();  //required for SwingWorker 3
  }//doOpenDialog


  /**
   * Construct a query suitable for getting the owner documents
   *
   * @return String
   */
  public String getOwnerQuery()
  {
  	ConfigXML config = Morpho.getConfiguration();
  	ConfigXML profile = morpho.getProfile();
    StringBuffer searchtext = new StringBuffer();
    searchtext.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    searchtext.append("<pathquery version=\"1.0\">\n");
    String lastname = profile.get("lastname", 0);
    String firstname = profile.get("firstname", 0);
    searchtext.append("<querytitle>My Data (" + firstname + " " + lastname);
    searchtext.append(")</querytitle>\n");
    Vector returnDoctypeList = config.get("returndoc");
    for (int i=0; i < returnDoctypeList.size(); i++) {
      searchtext.append("<returndoctype>");
      searchtext.append((String)returnDoctypeList.elementAt(i));
      searchtext.append("</returndoctype>\n");
    }
    Vector returnFieldList = config.get("returnfield");
    for (int i=0; i < returnFieldList.size(); i++) {
      searchtext.append("<returnfield>");
      searchtext.append((String)returnFieldList.elementAt(i));
      searchtext.append("</returnfield>\n");
    }
    
	// use all user identities    
    searchtext.append(getOwnerElement());
    
    searchtext.append("<querygroup operator=\"UNION\">\n");
    searchtext.append("<queryterm casesensitive=\"true\" ");
    searchtext.append("searchmode=\"contains\">\n");
    searchtext.append("<value>%</value>\n");
    searchtext.append("</queryterm></querygroup></pathquery>");
    return searchtext.toString();
  }
  
  	/**
  	 * Construct the <owner> element[s] so that we search for owner[s]
  	 * that are listed as equivalent identities as well
  	 * as the primary certificate subject.
  	 * @see http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5864
  	 * @return a string containing one or more <owner> elements
  	 */
	private String getOwnerElement() {
		StringBuffer ownerElement = new StringBuffer();
		
		// always include the main owner
		Set<Subject> subjects = new TreeSet<Subject>();
		String subjectDN = morpho.getUserName();
		Subject subject = new Subject();
		subject.setValue(subjectDN);
		subjects.add(subject);

		// add in the alt identities
		try {
			X509Certificate certificate = CertificateManager.getInstance().loadCertificate();
			SubjectInfo subjectInfo = CertificateManager.getInstance().getSubjectInfo(certificate);
			if (subjectInfo != null) {
				AuthUtils.findPersonsSubjects(subjects, subjectInfo, subject);
			}
		} catch (Exception e) {
			Log.debug(20, "Error calculating owner subject list: " + e.getMessage());
			e.printStackTrace();
		}
		
		// add all the alt subjects
		for (Subject s: subjects) {
			ownerElement.append("<owner>");
			ownerElement.append(s.getValue());
			ownerElement.append("</owner>\n");
		}
		
		return ownerElement.toString();
	}

  /**
   * could also have undo functionality; disabled for now
   */
  // public void undo();

}//class OpenDialogBoxCommand

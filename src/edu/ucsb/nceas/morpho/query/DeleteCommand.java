/**
 *  '$RCSfile: DeleteCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2004-03-26 21:49:18 $'
 * '$Revision: 1.16 $'
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

import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.SortableJTable;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;
import java.awt.event.ActionEvent;
import java.util.Vector;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * Class to handle delete local copy command
 */
public class DeleteCommand implements Command
{

  /** A reference to the delete dialog */
   private JDialog deleteDialog = null;

  /** A reference to the open dialog */
   private OpenDialogBox openDialog = null;

  /** A reference to the MorphoFrame */
   private MorphoFrame morphoFrame = null;

  /** A String indicating the morpho frame' type*/
   String morphoFrameType = null;

  /** A refernce to the ResultPanel */
   private ResultPanel resultPane = null;

  /** State of the delete */
  private String state = null;

  /** Warning message */
  private String message = null;

  /** Constant String Warning message for warning message */
  private static final String LOCALWARNING =
                          "Are you sure you want to delete \nthe package from "
                            + "your local file system?";
  private static final String NETWORKWARNING =
                        "Are you sure you want to delete \nthe package from " +
                        "the Network? You \nwill not be able to upload \nit " +
                        "again with the same identifier.";
  private static final String BOTHWARNING =
                        "Are you sure you want to delete \nthe package from " +
                        "the Network and your \nlocal file system? " +
                        "Deleting a package\n cannot be undone!";
  /** selected docid to delete */
  String selectDocId = null;

  /** flag to indicate a deletion can be execute, it depends package location */
  private boolean execute = false;

  /** flag to indicate selected data package has local copy */
  private boolean inLocal = false;

  /** flag to indicate selected data package has local copy */
  private boolean inNetwork = false;

  /** flag for if the delete command come from a open dialog */
  private boolean comeFromOpenDialog = false;

  DataPackageInterface dataPackage = null;

  /** Title string for blank window*/
  private String BLANK = "Blank";
  /** index for blank window */
  private static int index = 1;
  /**
   * Constructor of DeleteCommand
   * @param myOpenDialog the open dialog which will be applied delete action
   * @param myDeleteDialog a delete dialog need to be destroied
   * @param myFrame the parent frame of delete dialog or parent of open dialog
   * @param frameType the parent frame's type, search result or datapackage
   * @param selectId the id of data package need to be deleted
   * @param myState which deletion will happend, local, network or both
   * @param myInLocal if the datapackage has a local copy
   * @param myInNetwork if the datapackage has a network copy
   */
  public DeleteCommand(OpenDialogBox myOpenDialog, JDialog myDeleteDialog,
                      MorphoFrame myFrame, String frameType,  String myState,
                      String selectId, boolean myInLocal, boolean myInNetwork)
  {
    if ( myOpenDialog != null)
    {
      openDialog = myOpenDialog;
      comeFromOpenDialog = true;
      // this come from a open dialog, so we can selt morphoFrameType = null
      morphoFrameType = null;
    }
    else
    {
      // this come from a morpho frame, we need to know it's type
      morphoFrameType = frameType;
    }

    deleteDialog = myDeleteDialog;
    morphoFrame = myFrame;
    selectDocId = selectId;
    state = myState;
    inLocal = myInLocal;
    inNetwork = myInNetwork;

  }//LocalToNetworkCommand


  /**
   * execute delete local package command
   */
  public void execute(ActionEvent event)
  {

      if (state.equals(DataPackageInterface.LOCAL))
      {
        // Delete local copy
        message = LOCALWARNING;
        // If has local copy, can execute delete local
        execute = inLocal;
      }
      else if (state.equals(DataPackageInterface.METACAT))
      {
        // Delete network copy
        message = NETWORKWARNING;
        // If has network copy can delete network copy
        execute = inNetwork;
      }
      else if (state.equals(DataPackageInterface.BOTH))
      {
       // Delete both
       message = BOTHWARNING;
       // if has both copy can delete both.
       execute = inLocal && inNetwork;
      }
      else
      {
        Log.debug(20, "Unkown deletion command!");
      }

      // Make sure selected a id, and there is local pacakge
      if ( selectDocId != null && !selectDocId.equals("") && execute)
      {
        // Destroy the delete dialog
        if (deleteDialog != null)
        {
          deleteDialog.setVisible(false);
          deleteDialog.dispose();
          deleteDialog = null;
        }
        doDelete(selectDocId, openDialog);
      }

  }//execute

  /**
   * Using SwingWorket class to delete a local package
   *
   */
 private void doDelete(final String docid, final OpenDialogBox open)
 {
  final SwingWorker worker = new SwingWorker()
  {
        // A variable to indicate it reach refresh command or not
        // This is for butterfly flapping, if reach refresh, butterfly will
        // stop flapping by refresh
        boolean refreshFlag = false;
        public Object construct()
        {
          if (morphoFrame!=null)
          {
            morphoFrame.setBusy(true);
          }

          try
          {
            ServiceController services = ServiceController.getInstance();
            ServiceProvider provider =
                     services.getServiceProvider(DataPackageInterface.class);
            dataPackage = (DataPackageInterface)provider;
          }
          catch (ServiceNotHandledException snhe)
          {
            Log.debug(6, "Error in delete");
            return null;
          }

          // find the location of delete

                      //delete the local package
          Log.debug(20, "Deleteing the package.");
          int choice = JOptionPane.YES_OPTION;
          // come from open dialog
          if (comeFromOpenDialog)
          {
             choice = JOptionPane.showConfirmDialog(open, message,
                               "Morpho",
                               JOptionPane.YES_NO_CANCEL_OPTION,
                               JOptionPane.WARNING_MESSAGE);
          }
          else
          {
            // For morpho frame
            choice = JOptionPane.showConfirmDialog(morphoFrame, message,
                               "Morpho",
                               JOptionPane.YES_NO_CANCEL_OPTION,
                               JOptionPane.WARNING_MESSAGE);
          }

          if(choice == JOptionPane.YES_OPTION)
          {

            if (comeFromOpenDialog)
            {
              // this is for open dialg box
              try
              {
                dataPackage.delete(docid, state);
              }
              catch (Exception e)
              {
                 JOptionPane.showMessageDialog(open, e.getMessage());
                 return null;
              }
              refreshFlag = true;
              ResultPanel result = open.getResultPanel();
              refreshSearchResultPanel(result,open, comeFromOpenDialog);
              refreshFlag=false;
            }
            else if (morphoFrameType != null &&
                     morphoFrameType.equals(morphoFrame.SEARCHRESULTFRAME))
            {
              //for search result frame
              try
              {
                dataPackage.delete(docid, state);
              }
              catch (Exception e)
              {
                JOptionPane.showMessageDialog(morphoFrame, e.getMessage());
                return null;
              }
              refreshFlag = true;
              ResultPanel result =(ResultPanel)
                                     morphoFrame.getContentComponent();
              refreshSearchResultPanel(result, null, comeFromOpenDialog);
              refreshFlag=false;
            }
            else if (morphoFrameType != null &&
                     morphoFrameType.equals(morphoFrame.DATAPACKAGEFRAME))
            {
              //Fore data package frame
              //morphoFrame.setBusy(true);
              try
              {
                dataPackage.delete(docid, state);
              }
              catch (Exception e)
              {
                JOptionPane.showMessageDialog(morphoFrame, e.getMessage());
                 return null;
              }
              refreshFlag = true;
              refreshDataPackageFrame();
              refreshFlag=false;

            }
          }

           return null;

        }

        /*
         * Method to refresh a search result panel for opendialog or search
         * result frame
         */
        private void refreshSearchResultPanel(ResultPanel resultPane,
                                    OpenDialogBox open, boolean fromDialog)
        {
           ResultPanel newResult = null;
           if ( resultPane == null)
           {
             return;
           }
           boolean sorted = false;
           int index = -1;
           String order = null;
           SortableJTable table = resultPane.getJTable();
           if (table != null)
           {
                sorted = table.getSorted();
                index = table.getIndexOfSortedColumn();
                order = table.getOrderOfSortedColumn();
            }
            HeadResultSet resultSet = (HeadResultSet)resultPane.getResultSet();
            //get morpho from resultSet
            Morpho morpho = resultSet.getMorpho();
            //get Query
            Query query = resultSet.getQuery();
            //get original result vector
            Vector resultVector = resultSet.getResultsVector();
            //update the vector after deleting ( we don't research again).
            resultVector = updateResultVector(resultVector);
            //create a new HeadResultSet base on the modified resultSet vector
            // null is source(local or metacat)
            //String source ="both";
            HeadResultSet newResultSet =
                         new HeadResultSet(query, resultVector,morpho);
            // The size of resultpanel for morpho frame
             if (!fromDialog)
             {
            	boolean listCrashedDoc = false;
                newResult = new ResultPanel(null, newResultSet, 12, null,
                                       morphoFrame.getDefaultContentAreaSize(), listCrashedDoc);
                //if the table alread sort the new resul panel should be
                //sorted too
                if (sorted)
                {
                   newResult.sortTable(index, order);
                }
                newResult.setVisible(true);
                morphoFrame.setMainContentPane(newResult);
                morphoFrame.setMessage(newResultSet.getRowCount() +
                                       " data sets found");
                // Generate a non select event for resulpane
                StateChangeMonitor monitor = StateChangeMonitor.getInstance();
                monitor.notifyStateChange(
                                     new StateChangeEvent(
                                     newResult,
                                     StateChangeEvent.SEARCH_RESULT_NONSELECTED));
              }//if
              else
              {
                  // size for open dialog
                  newResult = new ResultPanel(open, newResultSet, null);
                  //if the table alread sort the new resul panel should
                  //be sorted too
                  if (sorted)
                  {
                    newResult.sortTable(index, order);
                  }
                  newResult.setVisible(true);
                  open.setResultPanel(newResult);
              }//esle

        }

        /*
         * Method to update a resultset vector
         */
        private Vector updateResultVector(Vector resultVector)
        {
          Vector newVector = null;
          // make sure parameter is not null
          if (resultVector == null)
          {
            return newVector;
          }
          // go through the vector to remove the deleted one
          for (int i=0; i<resultVector.size(); i++)
          {
           // vector for whole row
           Vector row = (Vector)resultVector.elementAt(i);
           // the DOCIDINDEX index element has the docid
           String docid = (String)row.elementAt(ResultSet.DOCIDINDEX);
           Log.debug(30, "the package id is: "+docid
                          + " and row number is " + i);
           if (selectDocId.equals(docid))
           {
             // find the deleted package and get rid of from resultset vector
             Log.debug(30, "finding the delete packagid: "+docid);
             if (state.equals(DataPackageInterface.LOCAL) && inLocal
                 && inNetwork)
             {
                Log.debug(30, "delete local copy from local copy "
                          +"and networkcopy - delete the local icon");
                // set index= ISLOCALINDEX value flase
                row.set(ResultSet.ISLOCALINDEX, new Boolean(false));

             }
             else if (state.equals(DataPackageInterface.LOCAL) && inLocal
                      && !inNetwork)
             {
                Log.debug(30,"delete local copy from local copy - get rid "+
                          "this row");
                resultVector.remove(i);
             }
             else if (state.equals(DataPackageInterface.METACAT) && inLocal
                       && inNetwork)
             {
                Log.debug(30, "delete network copy from local copy and "+
                          "network copy - delete network icon");
                // set index=ISMETACATINDEX value false
                row.set(ResultSet.ISMETACATINDEX, new Boolean(false));
             }
             else if (state.equals(DataPackageInterface.METACAT) && !inLocal
                     && inNetwork)
             {
                Log.debug(30, "delete network copy from "+
                         "network copy - delete the row");
                resultVector.remove(i);
             }
             else if (state.equals(DataPackageInterface.BOTH) && inLocal
                      && inNetwork)
             {
                Log.debug(30,"delete both copy, so remove this row");
                resultVector.remove(i);
             }
             else
             {
                Log.debug(20, "Unkown deletion command!");
              }
            }//if find the pakcage id needed be deleted
          }//for
          newVector = resultVector;
          return newVector;
        }

        /*
         * Method to refresh a open datackage
         */
        private void refreshDataPackageFrame()
        {


          // if pakcage have local and network copy and not delete both
          // reopen the package
          if ( inLocal && inNetwork && !state.equals(DataPackageInterface.BOTH))
          {

            // the location of data package after deleting
            String location = null;
            if (state.equals(DataPackageInterface.LOCAL))
            {
              location = DataPackageInterface.METACAT;
            }
            else
            {
              location = DataPackageInterface.LOCAL;
            }
            dataPackage.openDataPackage(location, selectDocId, null, null, null);

          }
          else
          {
          // new action - don't create a blank window DFH March 2004
          }

          // Distroy old frame
          UIController.getInstance().removeWindow(morphoFrame);
          morphoFrame.dispose();
          morphoFrame = null;
          //
        }

        //Runs on the event-dispatching thread.
        public void finished()
        {
          if (morphoFrame!=null && !refreshFlag)
          {
            // Refresh will stop butterfly flapping. So here we don't need
            morphoFrame.setBusy(false);
          }
        }
    };//final
    worker.start();  //required for SwingWorker 3

  }

   /**
    * could also have undo functionality; disabled for now
   */
  // public void undo();

}//class CancelCommand

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
import edu.ucsb.nceas.morpho.framework.QueryRefreshInterface;
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
  private String chosenDeletingStatus = null;

  /** Warning message */
  private String message = null;
  /** Constant String Warning message for warning message */
  private static final String LOCALINCOMPLETEWARNING =
                          "Are you sure you want to delete \nthe incomplete package from "
                            + "your local file system?";

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

  /** flag to indicate selected data package has local copy */
  private String packageLocalStatus = null;

  /** flag to indicate selected data package has local copy */
  private String packageNetworkStatus = null;

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
   * @param localStatus the local status of the data package
   * @param myInNetwork if the datapackage has a network copy
   */
  public DeleteCommand(OpenDialogBox myOpenDialog, JDialog myDeleteDialog,
                      MorphoFrame myFrame, String frameType,  String myState,
                      String selectId, String localStatus, String networkStatus)
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
    chosenDeletingStatus = myState;
    packageLocalStatus = localStatus;
    packageNetworkStatus = networkStatus;

  }//LocalToNetworkCommand


  /**
   * execute delete local package command
   */
  public void execute(ActionEvent event)
  {
      //Log.debug(5, "chosenDeltingStatus is "+chosenDeletingStatus+".\n local status is "+packageLocalStatus+".\n network status is "+packageNetworkStatus);

      if (chosenDeletingStatus != null && chosenDeletingStatus.equals(DataPackageInterface.LOCAL))
      {
        // Delete local copy
        message = LOCALWARNING;
        // If has local copy, can execute delete local
        if(packageLocalStatus == null || !packageLocalStatus.equals(DataPackageInterface.LOCAL))
        {
          showWarningPanel("Morpho couldn't delete the local copy of the data package since it doesn't exist");
          return;
        }
      }
      else if (chosenDeletingStatus != null && chosenDeletingStatus.equals(DataPackageInterface.METACAT))
      {
        // Delete network copy
        message = NETWORKWARNING;
        // If has network copy can delete network copy
        if(packageNetworkStatus == null  || !packageNetworkStatus.equals(DataPackageInterface.METACAT))
        {
          showWarningPanel("Morpho couldn't delete the network copy of the data package since it doesn't exist");
          return;
        }
      }
      else if (chosenDeletingStatus != null && chosenDeletingStatus.equals(DataPackageInterface.BOTH))
      {
       // Delete both
       message = BOTHWARNING;
       if(packageLocalStatus == null  || packageNetworkStatus == null || !packageLocalStatus.equals(DataPackageInterface.LOCAL) ||
           !packageNetworkStatus.equals(DataPackageInterface.METACAT))
       {
         showWarningPanel("Morpho couldn't delete the both local and network copy of the data package since they don't exist");
         return;
       }
      }
      else if(chosenDeletingStatus != null && chosenDeletingStatus.equals(QueryRefreshInterface.LOCALINCOMPLETEPACKAGE))
      {
        message =  LOCALINCOMPLETEWARNING;
        if(packageLocalStatus == null ||!(packageLocalStatus.equals(QueryRefreshInterface.LOCALAUTOSAVEDINCOMPLETE) || packageLocalStatus.equals(QueryRefreshInterface.LOCALUSERSAVEDINCOMPLETE)))
        {
          showWarningPanel("Morpho couldn't delete the incomplete copy of the data package since it doesn't exist");
          return;
        }
      }
      else
      {
        Log.debug(20, "Unkown deletion command!");
        return;
      }

      // Make sure selected a id, and there is local pacakge
      if ( selectDocId != null && !selectDocId.equals(""))
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
  
  private void showWarningPanel(String message)
  {
    JOptionPane.showMessageDialog(deleteDialog, message, "Warning!",
        JOptionPane.WARNING_MESSAGE);
  }

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
                dataPackage.delete(docid, chosenDeletingStatus);
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
                dataPackage.delete(docid, chosenDeletingStatus);
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
                dataPackage.delete(docid, chosenDeletingStatus);
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
            	  //boolean listCrashedDoc = false;
                newResult = new ResultPanel(null, newResultSet, 12, null,
                                       morphoFrame.getDefaultContentAreaSize());
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
             if (chosenDeletingStatus.equals(DataPackageInterface.LOCAL) && 
                      packageLocalStatus.equals(DataPackageInterface.LOCAL) &&
                 packageNetworkStatus.equals(DataPackageInterface.METACAT))
             {
                Log.debug(30, "delete local copy from local copy "
                          +"and networkcopy - delete the local icon");
                // set index= ISLOCALINDEX value flase
                row.set(ResultSet.ISLOCALINDEX, QueryRefreshInterface.NONEXIST);

             }
             else if (chosenDeletingStatus.equals(DataPackageInterface.LOCAL) && packageLocalStatus.equals(DataPackageInterface.LOCAL)
                 && !packageNetworkStatus.equals(DataPackageInterface.METACAT))
             {
                Log.debug(30,"delete local copy from local copy - get rid "+
                          "this row");
                resultVector.remove(i);
             }
             else if (chosenDeletingStatus.equals(DataPackageInterface.METACAT) && packageLocalStatus.equals(DataPackageInterface.LOCAL)
                 && packageNetworkStatus.equals(DataPackageInterface.METACAT))
             {
                Log.debug(30, "delete network copy from local copy and "+
                          "network copy - delete network icon");
                // set index=ISMETACATINDEX value false
                row.set(ResultSet.ISMETACATINDEX, QueryRefreshInterface.NONEXIST);
             }
             else if (chosenDeletingStatus.equals(DataPackageInterface.METACAT) && !packageLocalStatus.equals(DataPackageInterface.LOCAL)
                     && packageNetworkStatus.equals(DataPackageInterface.METACAT))
             {
                Log.debug(30, "delete network copy from "+
                         "network copy - delete the row");
                resultVector.remove(i);
             }
             else if (chosenDeletingStatus.equals(DataPackageInterface.BOTH) && packageLocalStatus.equals(DataPackageInterface.LOCAL)
                 && packageNetworkStatus.equals(DataPackageInterface.METACAT))
             {
                Log.debug(30,"delete both copy, so remove this row");
                resultVector.remove(i);
             }
             else if(chosenDeletingStatus.equals(QueryRefreshInterface.LOCALINCOMPLETEPACKAGE) && (packageLocalStatus.equals(QueryRefreshInterface.LOCALAUTOSAVEDINCOMPLETE) || packageLocalStatus.equals(QueryRefreshInterface.LOCALUSERSAVEDINCOMPLETE)))
             {
               Log.debug(30,"delete incomplete copy, so remove this row");
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
          // the location of data package after deleting
          String location = null;
          if ( packageLocalStatus.equals(DataPackageInterface.LOCAL) && 
               packageNetworkStatus.equals(DataPackageInterface.METACAT) && 
               chosenDeletingStatus.equals(DataPackageInterface.LOCAL))
          {
            location = DataPackageInterface.METACAT;
            dataPackage.openDataPackage(location, selectDocId, null, null, null);
          }
          else if(packageLocalStatus.equals(DataPackageInterface.LOCAL) && 
                    packageNetworkStatus.equals(DataPackageInterface.METACAT) && 
                    chosenDeletingStatus.equals(DataPackageInterface.METACAT))
          {
            location = DataPackageInterface.LOCAL;
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

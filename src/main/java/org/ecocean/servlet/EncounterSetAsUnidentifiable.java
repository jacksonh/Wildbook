/*
 * The Shepherd Project - A Mark-Recapture Framework
 * Copyright (C) 2011 Jason Holmberg
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.ecocean.servlet;

import org.ecocean.CommonConfiguration;
import org.ecocean.Encounter;
import org.ecocean.MailThreadExecutorService;
import org.ecocean.NotificationMailer;
import org.ecocean.Shepherd;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ThreadPoolExecutor;

//Set alternateID for this encounter/sighting
public class EncounterSetAsUnidentifiable extends HttpServlet {

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }


  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }


  private void setDateLastModified(Encounter enc) {
    String strOutputDateTime = ServletUtilities.getDate();
    enc.setDWCDateLastModified(strOutputDateTime);
  }


  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    Shepherd myShepherd = new Shepherd();
    //set up for response
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    boolean locked = false;
    boolean isOwner = true;


    if (request.getParameter("number") != null) {
      myShepherd.beginDBTransaction();
      Encounter enc2reject = myShepherd.getEncounter(request.getParameter("number"));
      setDateLastModified(enc2reject);
      boolean isOK = enc2reject.isAssignedToMarkedIndividual().equals("Unassigned");
      myShepherd.rollbackDBTransaction();
      if (isOK) {

        myShepherd.beginDBTransaction();
        try {

          //enc2reject.reject();
          enc2reject.setState("unidentifiable");
          enc2reject.addComments("<p><em>" + request.getRemoteUser() + " on " + (new java.util.Date()).toString() + "</em><br>Set this encounter as unidentifiable in the database.</p>");
          //enc2reject.approved = false;
          enc2reject.setState("unidentifiable");
        } catch (Exception le) {
          locked = true;
          le.printStackTrace();
          myShepherd.rollbackDBTransaction();
        }


        if (!locked) {
          String submitterEmail = enc2reject.getSubmitterEmail();
          myShepherd.commitDBTransaction();
          out.println(ServletUtilities.getHeader(request));
          out.println("<strong>Success:</strong> I have set encounter " + request.getParameter("number") + " as unidentifiable in the database.");
          out.println("<p><a href=\"http://" + CommonConfiguration.getURLLocation(request) + "/encounters/encounter.jsp?number=" + request.getParameter("number") + "\">View unidentifiable encounter #" + request.getParameter("number") + "</a></p>\n");
          ArrayList<String> allStates=CommonConfiguration.getSequentialPropertyValues("encounterState");
          int allStatesSize=allStates.size();
          if(allStatesSize>0){
            for(int i=0;i<allStatesSize;i++){
              String stateName=allStates.get(i);
              out.println("<p><a href=\"encounters/searchResults.jsp?state="+stateName+"\">View all "+stateName+" encounters</a></font></p>");   
            }
          }
          out.println(ServletUtilities.getFooter());
          String message = "Encounter " + request.getParameter("number") + " was set as unidentifiable in the database.";
          ServletUtilities.informInterestedParties(request, request.getParameter("number"),
            message);

          String emailUpdate = ServletUtilities.getText("dataOnlyUpdate.txt") + "\nEncounter: " + request.getParameter("number") + "\nhttp://" + CommonConfiguration.getURLLocation(request) + "/encounters/encounter.jsp?number=" + request.getParameter("number") + "\n";

          Vector e_images = new Vector();

          emailUpdate = CommonConfiguration.appendEmailRemoveHashString(request, emailUpdate,
            submitterEmail);

          
        //let's get ready for emailing
        ThreadPoolExecutor es = MailThreadExecutorService.getExecutorService();
        
        es.execute(new NotificationMailer(CommonConfiguration.getMailHost(), CommonConfiguration.getAutoEmailAddress(), submitterEmail, ("Encounter update: " + request.getParameter("number")), emailUpdate, e_images));
        es.shutdown();
          
          

        } else {
          out.println(ServletUtilities.getHeader(request));
          out.println("<strong>Failure:</strong> I have NOT modified encounter " + request.getParameter("number") + " in the database because another user is currently modifying its entry. Please try this operation again in a few seconds.");
          out.println("<p><a href=\"http://" + CommonConfiguration.getURLLocation(request) + "/encounters/encounter.jsp?number=" + request.getParameter("number") + "\">View unidentifiable encounter #" + request.getParameter("number") + "</a></p>\n");
          ArrayList<String> allStates=CommonConfiguration.getSequentialPropertyValues("encounterState");
          int allStatesSize=allStates.size();
          if(allStatesSize>0){
            for(int i=0;i<allStatesSize;i++){
              String stateName=allStates.get(i);
              out.println("<p><a href=\"encounters/searchResults.jsp?state="+stateName+"\">View all "+stateName+" encounters</a></font></p>");   
            }
          }
          out.println(ServletUtilities.getFooter());

        }

      } else {
        out.println(ServletUtilities.getHeader(request));
        out.println("Encounter# " + request.getParameter("number") + " is assigned to an individual and cannot be set as unidentifiable until it has been removed from that individual.");
        out.println(ServletUtilities.getFooter());
      }
    } else {
      out.println(ServletUtilities.getHeader(request));
      out.println("<strong>Error:</strong> I do not know which encounter you are trying to remove.");
      out.println(ServletUtilities.getFooter());

    }

    out.close();
    myShepherd.closeDBTransaction();
  }
}
	
	

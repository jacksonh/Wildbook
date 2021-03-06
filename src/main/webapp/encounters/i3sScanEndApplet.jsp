<%--
  ~ The Shepherd Project - A Mark-Recapture Framework
  ~ Copyright (C) 2011 Jason Holmberg
  ~
  ~ This program is free software; you can redistribute it and/or
  ~ modify it under the terms of the GNU General Public License
  ~ as published by the Free Software Foundation; either version 2
  ~ of the License, or (at your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  --%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=iso-8859-1" language="java"
         import="org.dom4j.Document, org.dom4j.Element, org.dom4j.io.SAXReader, org.ecocean.CommonConfiguration, org.ecocean.Shepherd, org.ecocean.grid.I3SMatchComparator, org.ecocean.grid.I3SMatchObject, java.io.File, java.util.Arrays, java.util.Iterator, java.util.List, java.util.Vector" %>
<html>
<%
  session.setMaxInactiveInterval(6000);
  String num = request.getParameter("number");
  Shepherd myShepherd = new Shepherd();
  if (request.getParameter("writeThis") == null) {
    myShepherd = (Shepherd) session.getAttribute(request.getParameter("number"));
  }
  Shepherd altShepherd = new Shepherd();
  String sessionId = session.getId();
  boolean xmlOK = false;
  SAXReader xmlReader = new SAXReader();
  File file = new File("foo");
  String scanDate = "";
  String side2 = "";
  
  //setup data dir
  String rootWebappPath = getServletContext().getRealPath("/");
  File webappsDir = new File(rootWebappPath).getParentFile();
  File shepherdDataDir = new File(webappsDir, CommonConfiguration.getDataDirectoryName());
  //if(!shepherdDataDir.exists()){shepherdDataDir.mkdir();}
  File encountersDir=new File(shepherdDataDir.getAbsolutePath()+"/encounters");
  //if(!encountersDir.exists()){encountersDir.mkdir();}
  File thisEncounterDir = new File(encountersDir, num);
 
%>

<head>
  <title>Best matches for #<%=num%>
  </title>
  <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
  <meta http-equiv="expires" content="0">
  <link
    href="http://<%=CommonConfiguration.getURLLocation(request)%>/css/ecocean.css"
    rel="stylesheet" type="text/css"/>
</head>

<style type="text/css">
  #tabmenu {
    color: #000;
    border-bottom: 2px solid black;
    margin: 12px 0px 0px 0px;
    padding: 0px;
    z-index: 1;
    padding-left: 10px
  }

  #tabmenu li {
    display: inline;
    overflow: hidden;
    list-style-type: none;
  }

  #tabmenu a, a.active {
    color: #DEDECF;
    background: #000;
    font: bold 1em "Trebuchet MS", Arial, sans-serif;
    border: 2px solid black;
    padding: 2px 5px 0px 5px;
    margin: 0;
    text-decoration: none;
    border-bottom: 0px solid #FFFFFF;
  }

  #tabmenu a.active {
    background: #FFFFFF;
    color: #000000;
    border-bottom: 2px solid #FFFFFF;
  }

  #tabmenu a:hover {
    color: #ffffff;
    background: #7484ad;
  }

  #tabmenu a:visited {
    color: #E8E9BE;
  }

  #tabmenu a.active:hover {
    background: #7484ad;
    color: #DEDECF;
    border-bottom: 2px solid #000000;
  }
</style>

<body>
<div id="wrapper">
<div id="page">
<jsp:include page="../header.jsp" flush="true">
  <jsp:param name="isAdmin" value="<%=request.isUserInRole(\"admin\")%>" />
</jsp:include>
<div id="main">

<ul id="tabmenu">
  <li><a
    href="encounter.jsp?number=<%=request.getParameter("number")%>">Encounter
    <%=request.getParameter("number")%>
  </a></li>
  <%
    String fileSider = "";
    File finalXMLFile;
    if ((request.getParameter("rightSide") != null) && (request.getParameter("rightSide").equals("true"))) {
      finalXMLFile = new File(encountersDir.getAbsolutePath()+"/" + num + "/lastFullRightScan.xml");

      side2 = "right";
      fileSider = "&rightSide=true";
    } else {
      finalXMLFile = new File(encountersDir.getAbsolutePath()+"/" + num + "/lastFullScan.xml");

    }
    if (finalXMLFile.exists()) {
  %>
  <li><a
    href="scanEndApplet.jsp?writeThis=true&number=<%=request.getParameter("number")%><%=fileSider%>">Modified
    Groth</a></li>

  <%
    }
  %>
  <li><a class="active">I3S</a></li>


</ul>

<%
  Vector initresults = new Vector();
  Document doc;
  Element root;
  String side = "left";

  if (request.getParameter("writeThis") == null) {
    //initresults=myShepherd.matches;
    if ((request.getParameter("rightSide") != null) && (request.getParameter("rightSide").equals("true"))) {
      side = "right";
    }
  } else {

//read from the written XML here if flagged
    try {
      if ((request.getParameter("rightSide") != null) && (request.getParameter("rightSide").equals("true"))) {
        //file=new File((new File(".")).getCanonicalPath()+File.separator+"webapps"+File.separator+"ROOT"+File.separator+"encounters"+File.separator+num+File.separator+"lastFullRightI3SScan.xml");
        file = new File(encountersDir.getAbsolutePath()+"/" + num + "/lastFullRightI3SScan.xml");

        side = "right";
      } else {
        //file=new File((new File(".")).getCanonicalPath()+File.separator+"webapps"+File.separator+"ROOT"+File.separator+"encounters"+File.separator+num+File.separator+"lastFullI3SScan.xml");
        file = new File(encountersDir.getAbsolutePath()+"/" + num + "/lastFullI3SScan.xml");
      }
      doc = xmlReader.read(file);
      root = doc.getRootElement();
      scanDate = root.attributeValue("scanDate");
      xmlOK = true;
    } catch (Exception ioe) {
      System.out.println("Error accessing the stored scan XML data for encounter: " + num);
      ioe.printStackTrace();
      //initresults=myShepherd.matches;
      xmlOK = false;
    }

  }
  I3SMatchObject[] matches = new I3SMatchObject[0];
  if (!xmlOK) {
    int resultsSize = initresults.size();
    System.out.println(resultsSize);
    matches = new I3SMatchObject[resultsSize];
    for (int a = 0; a < resultsSize; a++) {
      matches[a] = (I3SMatchObject) initresults.get(a);
    }

  }
%>

<p>

<h2>I3S Scan Results <a
  href="<%=CommonConfiguration.getWikiLocation()%>scan_results"
  target="_blank"><img src="../images/information_icon_svg.gif"
                       alt="Help" border="0" align="absmiddle"></a></h2>
</p>
<p><strong>The following encounter(s) received the best
  match values using the I3S algorithm against a <%=side%>-side scan of
  encounter# <a href="encounter.jsp?number=<%=num%>"><%=num%>
  </a>.</strong></p>


<%
  if (xmlOK) {%>
<p><img src="../images/Crystal_Clear_action_flag.png" width="28px" height="28px" hspace="2" vspace="2" align="absmiddle">&nbsp;<strong>Saved
  scan data may be old and invalid. Check the date below and run a fresh
  scan for the latest results.</strong></p>

<p><em>Date of scan: <%=scanDate%>
</em></p>
<%}%>
<table width="524" border="1" cellspacing="0" cellpadding="5">
  <tr>

    <td width="355" align="left" valign="top">
      <table width="100%" border="1" align="left" cellpadding="3">
        <tr align="left" valign="top">
          <td><strong>Shark</strong></td>
          <td><strong> Encounter</strong></td>
          <td><strong>Match Score </strong></td>


        </tr>
        <%
          if (!xmlOK) {

            I3SMatchObject[] results = new I3SMatchObject[1];
            results = matches;
            Arrays.sort(results, new I3SMatchComparator());
            for (int p = 0; p < results.length; p++) {
              if ((results[p].matchValue != 0) || (request.getAttribute("singleComparison") != null)) {%>
        <tr align="left" valign="top">
          <td>
            <table width="62">

              <tr>
                <td width="60" align="left"><a
                  href="http://<%=CommonConfiguration.getURLLocation(request)%>/individuals.jsp?number=<%=results[p].getIndividualName()%>"><%=results[p].getIndividualName()%>
                </a></td>
              </tr>
            </table>
          </td>
          <%if (results[p].encounterNumber.equals("N/A")) {%>
          <td>N/A</td>
          <%} else {%>
          <td><a
            href="http://<%=CommonConfiguration.getURLLocation(request)%>/encounters/encounter.jsp?number=<%=results[p].encounterNumber%>"><%=results[p].encounterNumber%>
          </a></td>
          <%
            }
            String finalscore2 = (new Double(results[p].matchValue)).toString();

            //trim the length of finalscore
            if (finalscore2.length() > 7) {
              finalscore2 = finalscore2.substring(0, 6);
            }
          %>
          <td><%=finalscore2%>
          </td>


        </tr>

        <%
              //end if matchValue!=0 loop
            }
            //end for loop
          }

//or use XML output here	
        } else {
          doc = xmlReader.read(file);
          root = doc.getRootElement();

          Iterator matchsets = root.elementIterator("match");
          while (matchsets.hasNext()) {
            Element match = (Element) matchsets.next();
            List encounters = match.elements("encounter");
            Element enc1 = (Element) encounters.get(0);
            Element enc2 = (Element) encounters.get(1);
        %>
        <tr align="left" valign="top">
          <td>
            <table width="62">

              <tr>
                <td width="60" align="left"><a
                  href="http://<%=CommonConfiguration.getURLLocation(request)%>/individuals.jsp?number=<%=enc1.attributeValue("assignedToShark")%>"><%=enc1.attributeValue("assignedToShark")%>
                </a></td>
              </tr>
            </table>
          </td>
          <%if (enc1.attributeValue("number").equals("N/A")) {%>
          <td>N/A</td>
          <%} else {%>
          <td><a
            href="http://<%=CommonConfiguration.getURLLocation(request)%>/encounters/encounter.jsp?number=<%=enc1.attributeValue("number")%>"><%=enc1.attributeValue("number")%>
          </a></td>
          <%
            }

            String finalscore = "&nbsp;";
            try {
              if (match.attributeValue("finalscore") != null) {
                finalscore = match.attributeValue("finalscore");
              }
            } catch (NullPointerException npe) {
            }

            //trim the length of finalscore
            if (finalscore.length() > 7) {
              finalscore = finalscore.substring(0, 6);
            }

          %>
          <td><%=finalscore%>
          </td>


          <%
            String evaluation = "No Adj.";
            evaluation = match.attributeValue("evaluation");
            if (evaluation == null) {
              evaluation = "&nbsp;";
            }

          %>

        </tr>

        <%


            }


          }

        %>

      </table>
    </td>
  </tr>
</table>
</tr>
</table>

<p><font size="+1">Visualizations for Potential Matches (as
  scored above)</font></p>

<p>

<p>
  <%
    String feedURL = "http://" + CommonConfiguration.getURLLocation(request) + "/TrackerFeed?number=" + num;
    String baseURL = "/"+CommonConfiguration.getDataDirectoryName()+"/encounters/";


//myShepherd.rollbackDBTransaction();
    myShepherd = null;
    doc = null;
    root = null;
    initresults = null;
    file = null;
    xmlReader = null;

    System.out.println("Base URL is: " + baseURL);
    if (xmlOK) {
      if ((request.getParameter("rightSide") != null) && (request.getParameter("rightSide").equals("true"))) {
        feedURL = baseURL + num + "/lastFullRightI3SScan.xml?";
      } else {
        feedURL = baseURL + num + "/lastFullI3SScan.xml?";
      }
    }
    String rightSA = "";
    if ((request.getParameter("rightSide") != null) && (request.getParameter("rightSide").equals("true"))) {
      rightSA = "&filePrefix=extractRight";
    }
    System.out.println("I made it to the Flash without exception.");
  %>
  <OBJECT id="sharkflash"
          codeBase=http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0
          height=450 width=800 classid=clsid:D27CDB6E-AE6D-11cf-96B8-444553540000>
    <PARAM NAME="movie"
           VALUE="tracker.swf?sessionId=<%=sessionId%>&rootURL=<%=CommonConfiguration.getURLLocation(request)%>&baseURL=<%=baseURL%>&feedurl=<%=feedURL%><%=rightSA%>">
    <PARAM NAME="quality" VALUE="high">
    <PARAM NAME="scale" VALUE="exactfit">
    <PARAM NAME="bgcolor" VALUE="#ddddff">
    <EMBED
      src="tracker.swf?sessionId=<%=sessionId%>&rootURL=<%=CommonConfiguration.getURLLocation(request)%>&baseURL=<%=baseURL%>&feedurl=<%=feedURL%>&time=<%=System.currentTimeMillis()%><%=rightSA%>"
      quality=high scale=exactfit bgcolor=#ddddff swLiveConnect=TRUE
      WIDTH="800" HEIGHT="450" NAME="sharkflash" ALIGN=""
      TYPE="application/x-shockwave-flash"
      PLUGINSPAGE="http://www.macromedia.com/go/getflashplayer"></EMBED>
  </OBJECT>
</p>
<jsp:include page="../footer.jsp" flush="true"/>
</div>
</div>
<!-- end page --></div>
<!--end wrapper -->
</body>
</html>

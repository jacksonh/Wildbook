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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java"
         import="org.ecocean.CommonConfiguration, org.ecocean.Encounter, org.ecocean.Shepherd, java.awt.*, java.io.File" %>
<%@ taglib uri="http://www.sunwesttek.com/di" prefix="di" %>

<%
  String number = request.getParameter("number").trim();
  int imageNum = 1;
  try {
    imageNum = (new Integer(request.getParameter("imageNum"))).intValue();
  } catch (Exception cce) {
  }
  
  //setup data dir
  String rootWebappPath = getServletContext().getRealPath("/");
  File webappsDir = new File(rootWebappPath).getParentFile();
  File shepherdDataDir = new File(webappsDir, CommonConfiguration.getDataDirectoryName());
  File encountersDir=new File(shepherdDataDir.getAbsolutePath()+"/encounters");
  File thisEncounterDir = new File(encountersDir, number);


%>

<html>
<head>
  <title><%=CommonConfiguration.getHTMLTitle() %>
  </title>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
  <meta name="Description"
        content="<%=CommonConfiguration.getHTMLDescription() %>"/>
  <meta name="Keywords"
        content="<%=CommonConfiguration.getHTMLKeywords() %>"/>
  <meta name="Author" content="<%=CommonConfiguration.getHTMLAuthor() %>"/>
  <link href="<%=CommonConfiguration.getCSSURLLocation(request) %>"
        rel="stylesheet" type="text/css"/>
  <link rel="shortcut icon"
        href="<%=CommonConfiguration.getHTMLShortcutIcon() %>"/>

</head>

<body>
<div id="wrapper">
  <div id="page">
    <jsp:include page="header.jsp" flush="true">
      
      <jsp:param name="isAdmin" value="<%=request.isUserInRole(\"admin\")%>" />
    </jsp:include>
    <div id="main">

      <div id="maincol-wide">

        <div id="maintext">
          <%
          Shepherd myShepherd = new Shepherd();
          try {
            String addText = "";
            if (request.getParameter("imageName") != null) {
              addText = request.getParameter("imageName");
              addText = encountersDir.getAbsolutePath()+"/" + request.getParameter("number") + "/" + addText;

            } 
            else {
              
              myShepherd.beginDBTransaction();
              Encounter enc = myShepherd.getEncounter(number);
              addText = (String) enc.getAdditionalImageNames().get((imageNum - 1));
              if (myShepherd.isAcceptableVideoFile(addText)) {
                addText = getServletContext().getRealPath("/")+"/images/video_thumb.jpg";
              } else {
                addText = encountersDir.getAbsolutePath()+"/"+ request.getParameter("number") + "/" + addText;
              }
              myShepherd.rollbackDBTransaction();
              myShepherd.closeDBTransaction();
            }

            int intWidth = 100;
            int intHeight = 75;
            int thumbnailHeight = 75;
            int thumbnailWidth = 100;


            File file2process = new File(addText);

            


              //ImageInfo iInfo=new ImageInfo();
              if ((file2process.exists()) && (file2process.length() > 0)) {
                //iInfo.setInput(new FileInputStream(file2process));
                String height = "";
                String width = "";


                Dimension imageDimensions = org.apache.sanselan.Sanselan.getImageSize(file2process);

                //height+=iInfo.getHeight();
                //width+=iInfo.getWidth();

                width = Double.toString(imageDimensions.getWidth());
                height = Double.toString(imageDimensions.getHeight());

                intHeight = ((new Double(height)).intValue());
                intWidth = ((new Double(width)).intValue());

                if (intWidth > thumbnailWidth) {
                  double scalingFactor = intWidth / thumbnailWidth;
                  intWidth = (int) (intWidth / scalingFactor);
                  intHeight = (int) (intHeight / scalingFactor);
                  if (intHeight < thumbnailHeight) {
                    thumbnailHeight = intHeight;
                  }
                } else {
                  thumbnailWidth = intWidth;
                  thumbnailHeight = intHeight;
                }


              }
            


            String thumbLocation = "file-"+encountersDir.getAbsolutePath()+"/" + number + "/thumb.jpg";

            //generate the thumbnail image
          %>
          <di:img width="<%=thumbnailWidth %>" height="<%=thumbnailHeight %>" border="0"
                  fillPaint="#ffffff" output="<%=thumbLocation%>" expAfter="0" threading="limited"
                  align="left" valign="left">
            <di:image width="<%=Integer.toString(intWidth) %>"
                      height="<%=Integer.toString(intHeight) %>" srcurl="<%=addText%>"/>
          </di:img>

          <h1 class="intro">Success</h1>

          <p>I have successfully reset the thumbnail image for encounter number <strong><%=number%></strong>.</p>

          <p><a
            href="http://<%=CommonConfiguration.getURLLocation(request)%>/encounters/encounter.jsp?number=<%=number%>">View encounter <%=number%>.</a></p>


        </div>
        <%
        } catch (Exception e) {
              //e.printStackTrace();
          %>

          <p>Hit an error trying to generate the thumbnail. Either the specified encounter or image does not exist.</p>
	</div>
	
          <%
            }
          myShepherd.rollbackDBTransaction();
                  myShepherd.closeDBTransaction();
        		  myShepherd=null;
          %>
        <!-- end maintext --></div>
      <!-- end maincol -->
      <jsp:include page="footer.jsp" flush="true"/>
    </div>
    <!-- end page --></div>
  <!--end wrapper -->
</body>
</html>
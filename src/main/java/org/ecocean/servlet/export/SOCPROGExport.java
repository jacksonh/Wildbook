package org.ecocean.servlet.export;
import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;

import org.ecocean.*;
import org.ecocean.servlet.ServletUtilities;
import org.springframework.mock.web.MockHttpServletRequest;

import jxl.write.*;
import jxl.Workbook;


//adds spots to a new encounter
public class SOCPROGExport extends HttpServlet{
  
  private static final int BYTES_DOWNLOAD = 1024;

  
  public void init(ServletConfig config) throws ServletException {
      super.init(config);
    }

  
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {
      doPost(request, response);
  }
    


  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
    
    //set the response
    
    
    Shepherd myShepherd = new Shepherd();
    

 
    
    //set up the files
    String filename = "SOCPROGExport_" + request.getRemoteUser() + ".xls";
    
    //setup data dir
    String rootWebappPath = getServletContext().getRealPath("/");
    File webappsDir = new File(rootWebappPath).getParentFile();
    File shepherdDataDir = new File(webappsDir, CommonConfiguration.getDataDirectoryName());
    File encountersDir=new File(shepherdDataDir.getAbsolutePath()+"/encounters");
    File excelFile = new File(encountersDir.getAbsolutePath()+"/"+ filename);

    int numPopulations=2;

    myShepherd.beginDBTransaction();
    
    
    try {
      
    //set up the vector for matching encounters
      Vector query1Individuals = new Vector();
      Vector query2Individuals = new Vector();

      //kick off the transaction
      myShepherd.beginDBTransaction();

      //start the query and get the results
      String order = "";
      //HttpServletRequest request1=(MockHttpServletRequest)request.getSession().getAttribute("locationSearch1");
    
      if(request!=null){
    
        //MarkedIndividualQueryResult queryResult1 = IndividualQueryProcessor.processQuery(myShepherd, request1, order);
        //System.out.println(((MockHttpServletRequest)session.getAttribute("locationSearch1")).getQueryString());
        //query1Individuals = queryResult1.getResult();
        //int numSearch1Individuals = query1Individuals.size();
        
        MarkedIndividualQueryResult queryResult2 = IndividualQueryProcessor.processQuery(myShepherd, request, order);
        query2Individuals = queryResult2.getResult();
        int numSearch2Individuals = query2Individuals.size();
      
      //set up the output stream
      FileOutputStream fos = new FileOutputStream(excelFile);
      OutputStreamWriter outp = new OutputStreamWriter(fos);
      
      try{

       //business logic start here
        
        //load the optional locales
        Properties props = new Properties();
        try {
          props.load(getClass().getResourceAsStream("/bundles/locales.properties"));
        } catch (Exception e) {
          System.out.println("     Could not load locales.properties in class GenalexExportCodominantMSDataBySize.");
          e.printStackTrace();
        }
        
      //let's set up some cell formats
        WritableCellFormat floatFormat = new WritableCellFormat(NumberFormats.FLOAT);
        WritableCellFormat integerFormat = new WritableCellFormat(NumberFormats.INTEGER);

      //let's write out headers for the OBIS export file
        WritableWorkbook workbookOBIS = Workbook.createWorkbook(excelFile);
        WritableSheet sheet = workbookOBIS.createSheet("Shepherd Project SOCPROG Data Export", 0);
        WritableSheet sheet2 = workbookOBIS.createSheet("Additional data", 1);


        
        Label indieLabel = new Label(0, 0, "Date");
        sheet.addCell(indieLabel);
        
        
        
        Label popLabel = new Label(1, 0, "Lat");
        sheet.addCell(popLabel);

        Label popLabel2 = new Label(2, 0, "Long");
        sheet.addCell(popLabel2);
        
        Label popLabel3 = new Label(3, 0, "ElevationOrDepth");
        sheet.addCell(popLabel3);
        
        Label popLabel3a = new Label(4, 0, "LocationID");
        sheet.addCell(popLabel3a);

        Label popLabel4 = new Label(5, 0, "ID");
        sheet.addCell(popLabel4);
        
        
        //sheet 2 entries
        Label popLabel4a = new Label(0, 0, "ID");
        sheet2.addCell(popLabel4a);
        
        Label popLabel7 = new Label(1, 0, "GroupID");
        sheet2.addCell(popLabel7);
        
        Label popLabel5 = new Label(2, 0, "Sex");
        sheet2.addCell(popLabel5);
        
        Label popLabel6 = new Label(3, 0, "Behavior");
        sheet2.addCell(popLabel6);
        
        Label popLabel8 = new Label(4, 0, "Haplotype");
        sheet2.addCell(popLabel8);
        
        
        
        
        //later, we might ant to add columns for Lat and Long
       
         int count = 0;

           
            Vector iterateMe=query2Individuals;
            
            
            for(int k=0;k<iterateMe.size();k++){
              
              MarkedIndividual indy=(MarkedIndividual)iterateMe.get(k);
              //System.out.println("          Individual: "+indy.getIndividualID());
              Vector encs=indy.getEncounters();
              int numEncs=encs.size();
              for(int j=0;j<numEncs;j++){
                  Encounter enc=(Encounter)encs.get(j);
                  if((enc.getLocationID()!=null)||((enc.getLongitudeAsDouble()!=null)&&(enc.getLatitudeAsDouble()!=null))){
                    
                    if((enc.getYear()>0)&&(enc.getMonth()>0)&&(enc.getDay()>0)){
                      
                    count++;
                    
                    
                    Label encLabel = new Label(0, count, enc.getDate().replaceAll("-", "/"));
                    sheet.addCell(encLabel);
                    
                    
                    if((enc.getLongitudeAsDouble()!=null)&&(enc.getLatitudeAsDouble()!=null)){
                      Label popLabel1a = new Label(1, count, enc.getLatitudeAsDouble().toString());
                      sheet.addCell(popLabel1a);

                    
                      Label popLabel2a = new Label(2, count, enc.getLongitudeAsDouble().toString());
                      sheet.addCell(popLabel2a);
                    }
                    
                    if((enc.getMaximumDepthInMeters()!=null)||(enc.getMaximumElevationInMeters()!=null)){
                      if(enc.getMaximumDepthInMeters()!=null){
                        Label popLabel3c = new Label(3, count, enc.getMaximumDepthInMeters().toString());
                        sheet.addCell(popLabel3c);
                      }
                      else{
                        Label popLabel3c = new Label(3, count, enc.getMaximumElevationInMeters().toString());
                        sheet.addCell(popLabel3c);
                      }
                    }
                    
                    
                    if(enc.getLocationID()!=null){
                      Label popLabel3d = new Label(4, count, enc.getLocationID());
                      sheet.addCell(popLabel3d);
                    }
                    
                    
                    //
                    if((enc.getIndividualID()!=null)&&(!enc.getIndividualID().equals("Unassigned"))){
                      Label popLabel4a1 = new Label(5, count, enc.getIndividualID().replaceAll("[^a-zA-Z0-9]", ""));
                      sheet.addCell(popLabel4a1);
                      
                      Label popLabel4a2 = new Label(0, count, enc.getIndividualID());
                      sheet2.addCell(popLabel4a2);
                      
                    }
                    
                    
                    if(myShepherd.getOccurrenceForEncounter(enc.getCatalogNumber())!=null){
                      Occurrence oc=myShepherd.getOccurrenceForEncounter(enc.getCatalogNumber());
                      Label popLabel7a = new Label(1, count, oc.getOccurrenceID());
                      sheet2.addCell(popLabel7a);
                    }
                    
                    if(enc.getSex()!=null){
                      Label popLabel5a = new Label(2, count, enc.getSex());
                      sheet2.addCell(popLabel5a);
                    }
                    
                    
                    if(enc.getBehavior()!=null){
                      Label popLabel6a = new Label(3, count, enc.getBehavior());
                      sheet2.addCell(popLabel6a);
                    }
                    
                    
                    if(enc.getHaplotype()!=null){
                      Label popLabel8a = new Label(4, count, enc.getHaplotype());
                      sheet2.addCell(popLabel8a);
                    }
                    
                  }
                    
                  }

              }
                         
             
            }
            
            
           
         workbookOBIS.write();
         workbookOBIS.close();
         
            

      // end Excel export =========================================================

        
        outp.close();
        outp=null;
        
      }
      catch(Exception ioe){
        ioe.printStackTrace();
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println(ServletUtilities.getHeader(request));
        out.println("<html><body><p><strong>Error encountered</strong> with file writing. Check the relevant log.</p>");
        out.println("<p>Please let the webmaster know you encountered an error at: SOCPROGExport servlet</p></body></html>");
        out.println(ServletUtilities.getFooter());
        out.close();
        outp.close();
        outp=null;
      }
      
    }
    }
    catch(Exception e) {
      e.printStackTrace();
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      out.println(ServletUtilities.getHeader(request));  
      out.println("<html><body><p><strong>Error encountered</strong></p>");
        out.println("<p>Please let the webmaster know you encountered an error at: SOCPROGExport servlet</p></body></html>");
        out.println(ServletUtilities.getFooter());
        out.close();
    }

    myShepherd.rollbackDBTransaction();
    myShepherd.closeDBTransaction();

      //now write out the file
      response.setContentType("application/msexcel");
      response.setHeader("Content-Disposition","attachment;filename="+filename);
      ServletContext ctx = getServletContext();
      //InputStream is = ctx.getResourceAsStream("/encounters/"+filename);
     InputStream is=new FileInputStream(excelFile);
      
      int read=0;
      byte[] bytes = new byte[BYTES_DOWNLOAD];
      OutputStream os = response.getOutputStream();
     
      while((read = is.read(bytes))!= -1){
        os.write(bytes, 0, read);
      }
      os.flush();
      os.close(); 
      
      
      
    }

  }
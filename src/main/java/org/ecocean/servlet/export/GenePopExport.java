package org.ecocean.servlet.export;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import org.ecocean.*;
import java.lang.StringBuffer;
import javax.jdo.Query;
import org.springframework.mock.web.MockHttpServletRequest;
import org.ecocean.genetics.*;
import java.net.URI;




//adds spots to a new encounter
public class GenePopExport extends HttpServlet{
  


  
  public void init(ServletConfig config) throws ServletException {
      super.init(config);
    }

  
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {
      doPost(request, response);
  }
    


  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
    
    //set the response
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    
    //get our Shepherd
    Shepherd myShepherd = new Shepherd();



    try{


      int numResults = 0;

      //set up the vector for matching encounters
      Vector query1Individuals = new Vector();
      Vector query2Individuals = new Vector();

      //kick off the transaction
      myShepherd.beginDBTransaction();

      //start the query and get the results
      String order = "";
      //EncounterQueryResult queryResult1 = EncounterQueryProcessor.processQuery(myShepherd, request, order);
      HttpServletRequest request1=(MockHttpServletRequest)request.getSession().getAttribute("locationSearch1");
    
      if((request!=null)&&(request1!=null)){
    
        MarkedIndividualQueryResult queryResult1 = IndividualQueryProcessor.processQuery(myShepherd, request1, order);
        //System.out.println(((MockHttpServletRequest)session.getAttribute("locationSearch1")).getQueryString());
        query1Individuals = queryResult1.getResult();
        int numSearch1Individuals = query1Individuals.size();
        
        MarkedIndividualQueryResult queryResult2 = IndividualQueryProcessor.processQuery(myShepherd, request, order);
        query2Individuals = queryResult2.getResult();
        int numSearch2Individuals = query2Individuals.size();
        
        //now let's start writing output
        
        //Line 1: write the title
        String additionalSearchString="";
        if((request.getParameter("searchNameField")!=null)&&(request1.getParameter("searchNameField")!=null)){
          additionalSearchString=": "+request1.getParameter("searchNameField")+" vs. "+request.getParameter("searchNameField");
          
        }
        out.println("Search Comparison GenePop Export"+additionalSearchString+"<br />");
        
        //Lines 2+: write the loci
        //let's calculate Fst for each of the loci
        //iterate through the loci
        ArrayList<String> loci=myShepherd.getAllLoci();
        int numLoci=loci.size();
        for(int r=0;r<numLoci;r++){
          String locus=loci.get(r);
          out.println(locus+"<br />");
        }

        
        //now write out POP1 for search1
        out.println("POP"+"<br />");
        for(int i=0;i<numSearch1Individuals;i++){
          MarkedIndividual indie=(MarkedIndividual)query1Individuals.get(i);
          
          String lociString="";
          for(int r=0;r<numLoci;r++){
            String locus=loci.get(r);
            ArrayList<Integer> values=indie.getAlleleValuesForLocus(locus);
            if(indie.getAlleleValuesForLocus(locus).size()==2){
              lociString+=values.get(0).toString();
              lociString+=values.get(1).toString()+" ";
            }
            else if(indie.getAlleleValuesForLocus(locus).size()==1){
              lociString+=values.get(0).toString();
              lociString+=values.get(0).toString()+" ";
            }
            else{lociString+="000000 ";}
            
          }
          
          out.println(indie.getIndividualID()+","+" "+lociString+"<br />");
          
        }
        
        
        //now write out POP2 for search2
        out.println("POP"+"<br />");
        for(int i=0;i<numSearch2Individuals;i++){
          MarkedIndividual indie=(MarkedIndividual)query2Individuals.get(i);
          
          String lociString="";
          for(int r=0;r<numLoci;r++){
            String locus=loci.get(r);
            ArrayList<Integer> values=indie.getAlleleValuesForLocus(locus);
            if(indie.getAlleleValuesForLocus(locus).size()==2){
              lociString+=values.get(0).toString();
              lociString+=values.get(1).toString()+" ";
            }
            else if(indie.getAlleleValuesForLocus(locus).size()==1){
              lociString+=values.get(0).toString();
              lociString+=values.get(0).toString()+" ";
            }
            else{lociString+="000000 ";}
            
          }
          
          out.println(indie.getIndividualID()+","+" "+lociString+"<br />");
          
        }

        
      }
      myShepherd.rollbackDBTransaction();
      myShepherd.closeDBTransaction();

    }
    catch(Exception e) {
      out.println("<p><strong>Error encountered</strong></p>");
      out.println("<p>Please let the webmaster know you encountered an error at: GenePopExport servlet.</p>");
      e.printStackTrace();
      myShepherd.rollbackDBTransaction();
      myShepherd.closeDBTransaction();
    }
    myShepherd=null;
    out.close();
    out=null;
  }

  
  }
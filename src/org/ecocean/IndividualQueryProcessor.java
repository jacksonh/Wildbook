package org.ecocean;

import java.util.Vector;
import java.lang.StringBuffer;
import javax.servlet.http.HttpServletRequest;
import javax.jdo.Extent;
import javax.jdo.Query;
import java.util.Iterator;
//import java.util.StringTokenizer;
//import java.util.Collections;


public class IndividualQueryProcessor {
  
  public static MarkedIndividualQueryResult processQuery(Shepherd myShepherd, HttpServletRequest request, String order){
    
      Vector<MarkedIndividual> rIndividuals=new Vector<MarkedIndividual>();  
      StringBuffer prettyPrint=new StringBuffer();
      String filter="";
      Iterator allSharks;
      
      int day1=1, day2=31, month1=1, month2=12, year1=0, year2=3000;
      try{month1=(new Integer(request.getParameter("month1"))).intValue();} catch(NumberFormatException nfe) {}
      try{month2=(new Integer(request.getParameter("month2"))).intValue();} catch(NumberFormatException nfe) {}
      try{year1=(new Integer(request.getParameter("year1"))).intValue();} catch(NumberFormatException nfe) {}
      try{year2=(new Integer(request.getParameter("year2"))).intValue();} catch(NumberFormatException nfe) {}
      try{day1=(new Integer(request.getParameter("day1"))).intValue();} catch(NumberFormatException nfe) {}
      try{day2=(new Integer(request.getParameter("day2"))).intValue();} catch(NumberFormatException nfe) {}
      
      
      /*
       * START SECTION NEEDING CHANGE
       * 
       * Must be much faster
       * Start with an encounter query spitting out a list of the matching individual IDs
       * then iterate where absolutely necessary.
       * 
       */
      
      Extent indieClass=myShepherd.getPM().getExtent(MarkedIndividual.class, true);
      Query query=myShepherd.getPM().newQuery(indieClass);
      
      /*
      if((request.getParameter("noQuery")!=null)&&(request.getParameter("startNum")!=null)&&(request.getParameter("endNum")!=null)){
        int startNum=Integer.parseInt(request.getParameter("startNum"));
        int endNum=Integer.parseInt(request.getParameter("endNum"));
        query.setRange(startNum, endNum);
      }
      */
      
      
        if(request.getParameter("sort")!=null) {
          if(request.getParameter("sort").equals("sex")){allSharks=myShepherd.getAllMarkedIndividuals(query, "sex ascending");}
          else if(request.getParameter("sort").equals("name")) {allSharks=myShepherd.getAllMarkedIndividuals(query, "name ascending");}
          else if(request.getParameter("sort").equals("numberEncounters")) {allSharks=myShepherd.getAllMarkedIndividuals(query, "numberEncounters descending");}
          else{
            allSharks=myShepherd.getAllMarkedIndividuals(query, "colorCode ascending, name ascending");
          }
        }
        else{
          allSharks=myShepherd.getAllMarkedIndividuals(query, "colorCode ascending, name ascending");
          //keyword and then name ascending 
        }
      
        //process over to Vector
        while (allSharks.hasNext()) {
          MarkedIndividual temp_shark=(MarkedIndividual)allSharks.next();
          rIndividuals.add(temp_shark);
        }
      

     /*
      * END SECTION NEEDING CHANGE
      * 
      */
      
      
      if(request.getParameter("noQuery")==null){
      
      //------------------------------------------------------------------
      //GPS filters-------------------------------------------------
      if((request.getParameter("ne_lat")!=null)&&(!request.getParameter("ne_lat").equals(""))) {
        if((request.getParameter("ne_long")!=null)&&(!request.getParameter("ne_long").equals(""))) {
          if((request.getParameter("sw_lat")!=null)&&(!request.getParameter("sw_lat").equals(""))) {
            if((request.getParameter("sw_long")!=null)&&(!request.getParameter("sw_long").equals(""))) {
              
              for(int q=0;q<rIndividuals.size();q++) {
                MarkedIndividual tShark=(MarkedIndividual)rIndividuals.get(q);
                boolean wasSightedInThisLocation = false;
                Vector rEncounters=tShark.getEncounters();
                int numEncs=rEncounters.size();
                
                for(int y=0;y<numEncs;y++) {
                  Encounter rEnc=(Encounter)rEncounters.get(y);
                  if(!((rEnc.getDecimalLatitude()==null)||(rEnc.getDecimalLongitude()==null))){
                  
                    try{
                      
                      double encLat=(new Double(rEnc.getDecimalLatitude())).doubleValue();
                      double encLong=(new Double(rEnc.getDecimalLongitude())).doubleValue();
                      
                      double ne_lat=(new Double(request.getParameter("ne_lat"))).doubleValue();
                      double ne_long = (new Double(request.getParameter("ne_long"))).doubleValue();
                      double sw_lat = (new Double(request.getParameter("sw_lat"))).doubleValue();
                      double sw_long=(new Double(request.getParameter("sw_long"))).doubleValue();
                      if((sw_long>0)&&(ne_long<0)){
                        if(((encLat<=ne_lat)&&(encLat>=sw_lat)&&((encLong<=ne_long)||(encLong>=sw_long)))){
                          wasSightedInThisLocation = true;
                        }
                      }
                      else if(((encLat<=ne_lat)&&(encLat>=sw_lat)&&(encLong<=ne_long)&&(encLong>=sw_long))){
                        wasSightedInThisLocation = true;
                       }

                      
                    }
                    catch(NumberFormatException nfe){
                      nfe.printStackTrace();
                    }
                    catch(Exception ee){
                      ee.printStackTrace();
                    }
                    
                  }
                }
                if(!wasSightedInThisLocation) {
                  rIndividuals.remove(q);
                  q--;
                }
                
              }
              
              prettyPrint.append("GPS Boundary NE: \""+request.getParameter("ne_lat")+", "+request.getParameter("ne_long")+"\".<br />");
              prettyPrint.append("GPS Boundary SW: \""+request.getParameter("sw_lat")+", "+request.getParameter("sw_long")+"\".<br />");
              
        
            }
          }
        }
      }
      //end GPS filters----------------------------------------------- 
      
      
      
      
      
      //individuals with a particular location
      if((request.getParameter("locationField")!=null)&&(!request.getParameter("locationField").equals(""))) {
        prettyPrint.append("locationField is: "+request.getParameter("locationField")+"<br />");      
        String loc=request.getParameter("locationField").toLowerCase().trim();
        for(int q=0;q<rIndividuals.size();q++) {
                MarkedIndividual tShark=(MarkedIndividual)rIndividuals.get(q);
                boolean wasSightedInThisLocation = false;
                Vector encounters = tShark.getEncounters();
                int numEncs=encounters.size();
                for(int f=0;f<numEncs;f++) {
                  Encounter enc=(Encounter)encounters.get(f);
                  if(enc.getVerbatimLocality().toLowerCase().indexOf(loc)!=-1){wasSightedInThisLocation = true;}
                }
                if(!wasSightedInThisLocation) {
                  rIndividuals.remove(q);
                  q--;
                }
                
              }     //end for
      }//end if with location

      //locationID filter-------------------------------------------------
      String[] locCodes=request.getParameterValues("locationCodeField");
      if((locCodes!=null)&&(!locCodes[0].equals("None"))){
        prettyPrint.append("locationCodeField is one of the following: ");
            int kwLength=locCodes.length;
            
            for(int kwIter=0;kwIter<kwLength;kwIter++) {
              String kwParam=locCodes[kwIter].replaceAll("%20", " ").trim();
              prettyPrint.append(kwParam+" ");
            }
            
            for(int q=0;q<rIndividuals.size();q++) {
              MarkedIndividual tShark=(MarkedIndividual)rIndividuals.get(q);
              boolean wasSightedInOneOfThese=false;
            for(int kwIter=0;kwIter<kwLength;kwIter++) {
                
                String kwParam=locCodes[kwIter].replaceAll("%20", " ").trim();
                if(!kwParam.equals("")){
                  if(tShark.wasSightedInLocationCode(kwParam)) {
                    wasSightedInOneOfThese=true;
                  }
                  
                }
                
              }
              if(!wasSightedInOneOfThese) {
                 rIndividuals.remove(q);
                 q--;
              }
              
            }     //end for  
            

              prettyPrint.append("<br />");
      }
      //end locationID filter-----------------------------------------------  
      
      //verbatimEventDateField filter-------------------------------------------------
      String[] verbatimEventDates=request.getParameterValues("verbatimEventDateField");
      if((request.getParameterValues("verbatimEventDateField")!=null)&&(!verbatimEventDates[0].equals("None"))){
            prettyPrint.append("verbatimEventDateField is one of the following: ");
            int kwLength=verbatimEventDates.length;
            
            for(int kwIter=0;kwIter<kwLength;kwIter++) {
              String kwParam=verbatimEventDates[kwIter].replaceAll("%20", " ").trim();
              prettyPrint.append(kwParam+" ");
            }
            
            
            for(int q=0;q<rIndividuals.size();q++) {
              MarkedIndividual tShark=(MarkedIndividual)rIndividuals.get(q);
              boolean wasSightedInOneOfThese=false;
              
              
              
              for(int kwIter=0;kwIter<kwLength;kwIter++) {
                String kwParam=verbatimEventDates[kwIter].replaceAll("%20", " ").trim();
                if(!kwParam.equals("")){
                  if(tShark.wasSightedInVerbatimEventDate(kwParam)) {
                    wasSightedInOneOfThese=true;
                 }
                 
                }
              } //end for
              if(!wasSightedInOneOfThese) {
                 rIndividuals.remove(q);
                 q--;
              }
              
            }     //end for  
            prettyPrint.append("<br />");
      }
      //end verbatimEventDateField filter-----------------------------------------------   
      
      
      //individuals with a particular alternateID
      if((request.getParameter("alternateIDField")!=null)&&(!request.getParameter("alternateIDField").equals(""))) {
        prettyPrint.append("alternateIDField: "+request.getParameter("alternateIDField")+"<br />");      
        for(int q=0;q<rIndividuals.size();q++) {
                MarkedIndividual tShark=(MarkedIndividual)rIndividuals.get(q);
                if(tShark.getAllAlternateIDs().toLowerCase().indexOf(request.getParameter("alternateIDField").toLowerCase().trim())!=-1) {
                  rIndividuals.remove(q);
                  q--;
                }
                
              }     //end for
      }//end if with alternateID
      
      


      //individuals with a photo keyword assigned to one of their encounters
      String[] keywords=request.getParameterValues("keyword");
      if((request.getParameterValues("keyword")!=null)&&(!keywords[0].equals("None"))){
        
        prettyPrint.append("Keywords: ");
        int kwLength=keywords.length;
        
        for(int kwIter=0;kwIter<kwLength;kwIter++) {
          String kwParam=keywords[kwIter].replaceAll("%20", " ").trim();
          prettyPrint.append(kwParam+" ");
        }
        prettyPrint.append("<br />");
      for(int kwIter=0;kwIter<kwLength;kwIter++) {
          String kwParam=keywords[kwIter];
          if(myShepherd.isKeyword(kwParam)) {
            Keyword word=myShepherd.getKeyword(kwParam);
            for(int q=0;q<rIndividuals.size();q++) {
              MarkedIndividual tShark=(MarkedIndividual)rIndividuals.get(q);
              if(!tShark.isDescribedByPhotoKeyword(word)) {
                rIndividuals.remove(q);
                q--;
              }
            } //end for
          } //end if isKeyword
      }
      }



      //individuals of a particular sex
      if((request.getParameter("sex")!=null)&&(!request.getParameter("sex").equals("all"))) {
        prettyPrint.append("Sex is: "+request.getParameter("sex").replaceAll("mf", "male or female")+"<br />");      
        
              for(int q=0;q<rIndividuals.size();q++) {
                MarkedIndividual tShark=(MarkedIndividual)rIndividuals.get(q);
                if((request.getParameter("sex").equals("male"))&&(!tShark.getSex().equals("male"))) {
                  rIndividuals.remove(q);
                  q--;
                }
                else if((request.getParameter("sex").equals("female"))&&(!tShark.getSex().equals("female"))) {
                  rIndividuals.remove(q);
                  q--;
                }
                else if((request.getParameter("sex").equals("unknown"))&&(!tShark.getSex().equals("unknown"))) {
                  rIndividuals.remove(q);
                  q--;
                }
                else if((request.getParameter("sex").equals("mf"))&&(tShark.getSex().equals("unknown"))) {
                  rIndividuals.remove(q);
                  q--;
                }
              } //end for
      }//end if of sex

            
      //min number of resights      
      if((request.getParameter("numResights")!=null)&&(!request.getParameter("numResights").equals(""))&&(request.getParameter("numResightsOperator")!=null)) {
        prettyPrint.append("Number of resights is "+request.getParameter("numResightsOperator")+" than "+request.getParameter("numResights")+"<br />");      
              
        int numResights=1;
              String operator = "greater";
              try{
                numResights=(new Integer(request.getParameter("numResights"))).intValue();
                operator = request.getParameter("numResightsOperator");
              }
              catch(NumberFormatException nfe) {}
              for(int q=0;q<rIndividuals.size();q++) {
                MarkedIndividual tShark=(MarkedIndividual)rIndividuals.get(q);
                
                
                if(operator.equals("greater")){
                  if(tShark.getMaxNumYearsBetweenSightings()<numResights) {
                    rIndividuals.remove(q);
                    q--;
                  }
                }
                else if(operator.equals("less")){
                  if(tShark.getMaxNumYearsBetweenSightings()>numResights) {
                    rIndividuals.remove(q);
                    q--;
                  }
                }
                else if(operator.equals("equals")){
                  if(tShark.getMaxNumYearsBetweenSightings() != numResights) {
                    rIndividuals.remove(q);
                    q--;
                  }
                }
                
                
              } //end for
      }//end if resightOnly


      //now filter for date-----------------------------
      
       prettyPrint.append("Dates between: "+year1+"-"+month1+"-"+day1+" and "+year2+"-"+month2+"-"+day2+"<br />");
       
       
       if((year1==myShepherd.getEarliestSightingYear())&&(year2==myShepherd.getLastSightingYear())&&(month1==1)&&(month2==12)&&(day1==1)&&(day2==31)){}
       else{
        for(int q=0;q<rIndividuals.size();q++) {
                MarkedIndividual tShark=(MarkedIndividual)rIndividuals.get(q);
                if(!tShark.wasSightedInPeriod(year1, month1, day1, year2, month2, day2)) {
                  rIndividuals.remove(q);
                  q--;
                }
        } 
      }
      //end for
      //--------------------------------------------------
      
    }
      
      return (new MarkedIndividualQueryResult(rIndividuals,filter,prettyPrint.toString()));
    
  }

}
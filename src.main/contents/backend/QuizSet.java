package contents.backend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import utils.Parsor;


public class QuizSet 
{
	final private static Logger log = Logger.getLogger(QuizSet.class);
	
	public Integer quizsetId = 0;
	public String title = "";
	public Long createdDateTime = 0L;
	
	public final static String Field_QUIZSET_ID = "SCRIPT_ID";
	public final static String Field_QUIZSET_TITLE = "SCRIPT_TITLE";
	
	public final static Integer TEACHER_MADE_SCRIPT_TYPE = 0;
	public final static Integer USER_MADE_QUIZSET_TYPE = 1;
	public final static Integer USER_MADE_QUIZSET_ID_MIN = 10000;
	
	public QuizSet(){}
	public QuizSet( Integer index, String filename )
	{	
    	this.title = filename;
    	this.quizsetId = index;
    	//this.sentences = sentences;
	}
	
	public static boolean isNull( QuizSet script ){
		if( script == null )
			return true;
		
		if( script.quizsetId == 0 
				&& script.title == "")
			return true;
		
		return false;
	}
	
   public static boolean checkQuizSetID( Integer id ) {
       if( id == null || id <= 0){
          return false;
       }
       return true;
   }
   
   public static boolean checkQuizSetIDs( List<Integer> ids ) {
	   if( ids == null || ids.isEmpty()){
		   log.error("scriptIds is null");
		   return false;
	   }
	   
	   for(Integer id: ids){
		   if( id == null || id <= 0){
			   log.error("invalid scriptId ("+id+")");
		        return false;
		    }
	   }
       return true;
   }
   
   // 우저가 만든  문장들이 들어있는, 스크립트인가?
   public static boolean isMadeByUser(Integer id){
	   return id >= USER_MADE_QUIZSET_ID_MIN;   
   }
   
   public Map<String, Object> serialize(){
		Map<String, Object> map = new HashMap<>();
		map.put(Field_QUIZSET_ID, this.quizsetId);
		map.put(Field_QUIZSET_TITLE, this.title);
		return map;
	}
	
	public String toString()  
	{
		StringBuffer text = new StringBuffer();
		
		text.append(quizsetId + Parsor.QuizUnitSeperator);
		text.append(title + Parsor.QuizUnitSeperator);
		return text.toString();
	}
}

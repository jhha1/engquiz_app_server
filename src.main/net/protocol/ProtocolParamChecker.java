package net.protocol;

import java.util.List;
import java.util.Map;

import org.bouncycastle.util.encoders.Base64;

import contents.backend.QuizSet;
import contents.backend.Sentence;
import contents.backend.User;
import exception.system.MyIllegalArgumentException;
import exception.system.ProtocolParameterException;
import utils.StringHelper;

public class ProtocolParamChecker 
{
	private ProtocolParamChecker(){}
	
	public static Integer userID_Length_MIN = 4;
	public static Integer userID_Length_MAX = 50;
	public static Integer nickname_Length_MIN = 4;
	public static Integer nickname_Length_MAX = 8;
	public static Integer pw_LenMIN = 3;
	public static Integer pw_LenMAX = 25;
	
	public static Integer pidMIN = 1000;
	public static Integer pidMAX = 9999;
	

	public static void checkPID( Object pid ) throws MyIllegalArgumentException
	{
		if( null == pid )
			throw new ProtocolParameterException(EResultCode.INVALID_PID, "NullPID");
		
		// type
		Integer iPID = 0;
		if( pid instanceof String ) 
		{ 
			try {
				iPID = Integer.valueOf( (String) pid );
			} catch(Exception e) {
				throw new ProtocolParameterException(EResultCode.INVALID_PID, "invalidPID:"+pid);
			}
		} 
		else if ( pid instanceof Integer ) 
			iPID = (Integer) pid;
		else 
			throw new ProtocolParameterException(EResultCode.INVALID_PID, "InvalidPIDType: " + pid.getClass().getTypeName());
		
		// range
		if( iPID < pidMIN || pidMAX < iPID ) 
			throw new ProtocolParameterException(EResultCode.INVALID_PID, 
					"OutOfPIDRange(min:" + pidMIN 
					+",max:" + pidMAX
					+",cur:" + iPID);
	}
	
	public static Integer checkUserID( Object userId ) {
   	 if( userId == null || !(userId instanceof Integer) ){
   		 String reason = "Invalid UserID. userId type is not Integer. userId:"+userId;
   		 throw new ProtocolParameterException(EResultCode.INVALID_DATATYPE, "CheckFailed. " + reason);
        }

   	 	return checkUserID( (Integer)userId );
   }
	
	public static Integer checkUserID( Integer userId ) {
		if( ! User.checkUserID(userId) ){
			String reason = "Invalid userId. userId("+userId+")";
			throw new ProtocolParameterException("CheckFailed. " + reason);
		}
		return userId;
    }
	
	public static boolean checkBooleanDefault(Object obj) {
		if( obj == null || !(obj instanceof Boolean) ){
			 String reason = "Invalid param. type is not Boolean. obj:"+obj;
   		 throw new ProtocolParameterException(EResultCode.INVALID_DATATYPE, "CheckFailed. " + reason);
        }
		 return (boolean)obj;
	}
	
	public static Integer checkIntDefault(Object obj) {
		if( obj == null || !(obj instanceof Integer) ){
			 String reason = "Invalid param. type is not Integer. obj:"+obj;
   		 throw new ProtocolParameterException(EResultCode.INVALID_DATATYPE, "CheckFailed. " + reason);
        }
		 return (Integer)obj;
	}
	
	public static String checkUsername( Object username ){
		 if( username == null || !(username instanceof String) ){
			 String reason = "Invalid Username. username type is not string. username:"+username;
    		 throw new ProtocolParameterException(EResultCode.INVALID_DATATYPE, "CheckFailed. " + reason);
         }
		 return checkUsername((String)username);
	}
	
	public static String checkUsername( String username ){
		if( ! User.checkUserName(username) ){
			String reason = "Invalid Username. username("+username+")";
			throw new ProtocolParameterException("CheckFailed. " + reason);
		}
		return username;
	}
	
	
	public static Integer checkScriptId( Object scriptId ){
		 if( scriptId == null || !(scriptId instanceof Integer) ){
			 String reason = "Invalid scriptId. scriptId type is not Integer. scriptId:"+scriptId;
  		 throw new ProtocolParameterException(EResultCode.INVALID_DATATYPE, "CheckFailed. " + reason);
       }
		 return checkScriptId((Integer)scriptId);
	}
	
	public static Integer checkScriptId( Integer scriptId ){
		if( ! QuizSet.checkQuizSetID(scriptId) ){
			String reason = "Invalid scriptId. scriptId("+scriptId+")";
			throw new ProtocolParameterException("CheckFailed. " + reason);
		}
		return scriptId;
	}
	
	public static Integer checkSentenceId( Object sentenceId ){
		 if( sentenceId == null || !(sentenceId instanceof Integer) ){
			 String reason = "Invalid sentenceId. sentenceId type is not Integer. sentenceId:"+sentenceId;
 		 throw new ProtocolParameterException(EResultCode.INVALID_DATATYPE, "CheckFailed. " + reason);
      }
		 return checkSentenceId((Integer)sentenceId);
	}
	
	public static Integer checkSentenceId( Integer sentenceId ){
		if( ! Sentence.checkSentenceId(sentenceId) ){
			String reason = "Invalid sentenceId. sentenceId("+sentenceId+")";
			throw new ProtocolParameterException("CheckFailed. " + reason);
		}
		return sentenceId;
	}
	
	public static String checkSentenceText( Object sentenceText ){
		 if( sentenceText == null || !(sentenceText instanceof String) ){
			 String reason = "Invalid sentenceText. sentenceText type is not String. sentenceText:"+sentenceText;
		 throw new ProtocolParameterException(EResultCode.INVALID_DATATYPE, "CheckFailed. " + reason);
     }
		 return checkSentenceText((String)sentenceText);
	}
	
	public static String checkSentenceText( String sentenceText ){
		if( ! Sentence.checkText(sentenceText) ){
			String reason = "Invalid sentenceText. sentenceText("+sentenceText+")";
			throw new ProtocolParameterException("CheckFailed. " + reason);
		}
		return sentenceText;
	}
	
	public static String checkTitle( Object title ){
		 if( title == null || !(title instanceof String) ){
			 String reason = "Invalid title. title type is not String. title:"+title;
		 throw new ProtocolParameterException(EResultCode.INVALID_DATATYPE, "CheckFailed. " + reason);
    }
		 return checkTitle((String)title);
	}
	
	public static String checkTitle( String title ){
		if(StringHelper.isNull(title)) {
			String reason = "Invalid title. title("+title+")";
			throw new ProtocolParameterException("CheckFailed. " + reason);
		}
		return title;
	}
	
	@SuppressWarnings("rawtypes")
	public static List checkList( Object list ){
		if( list == null || !(list instanceof List) ){
			 String reason = "Invalid list. list type is not List.";
			 throw new ProtocolParameterException(EResultCode.INVALID_DATATYPE, "CheckFailed. " + reason);
        }
		return checkList((List)list);
	}
	
	@SuppressWarnings("rawtypes")
	public static List checkList( List list ){
		if( list == null ){
			String reason = "Null list Parameter.";
			throw new ProtocolParameterException("CheckFailed. " + reason);
		}
		
		return list;
	}
	
	public static byte[] checkPDF( Object pdf ){
		 if( pdf == null || !(pdf instanceof String) ){
			 String reason = "Invalid pdf. pdf type is not String. pdf:"+pdf;
		 throw new ProtocolParameterException(EResultCode.INVALID_DATATYPE, "CheckFailed. " + reason);
   }
		 return checkPDF((String)pdf);
	}
	
	public static byte[] checkPDF( String pdfString ){
		if(StringHelper.isNull(pdfString)) {
			String reason = "Null pdfString. pdfString("+pdfString+")";
			throw new ProtocolParameterException("CheckFailed. " + reason);
		}
		try {
			return (byte[]) Base64.decode(pdfString);
		} catch (Exception e){
			throw new ProtocolParameterException("CheckFailed. " + e.getMessage());
		}
	}
	
	
	public static void 
		checkProtocolRequiredParams( Map<EProtocol, Object> protocolData )  
			throws ProtocolParameterException
	{
		if( protocolData == null || protocolData.isEmpty() )
			throw new ProtocolParameterException("protocolMapNull:"+protocolData);
		
		// PID
		if( false == protocolData.containsKey( EProtocol.PID ) )
			throw new ProtocolParameterException(EResultCode.INVALID_PID, "NoExistPID"); 
		
		checkPID( protocolData.get( EProtocol.PID ) );
		
		// USER ID
		Object pid = protocolData.get( EProtocol.PID );
		Integer iPID = ( pid instanceof String )? Integer.valueOf((String)pid) : (Integer) pid;
		boolean bCheckIgnoreProtocol = ( iPID == 1001 || iPID == 1002 );  
		if( false == bCheckIgnoreProtocol ) {  // userID기 인들어가는 프로토콜 제외. 
			// check USERID
			if( false == protocolData.containsKey( EProtocol.UserID )) 
				throw new ProtocolParameterException(EResultCode.INVALID_USERID, "NoExistUserID"); 
			
			checkUserID( protocolData.get( EProtocol.UserID ) );
		}
	}
	

	
}

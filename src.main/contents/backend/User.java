package contents.backend;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import net.protocol.EResultCode;
import utils.StringHelper;

public class User {
	final private static Logger log = Logger.getLogger( User.class );
	
	public Integer userID = 0;
	public String userName = null;
	public Long signin_dt = 0L;
	private boolean bAdmin = false;
	

	private static final int USERNAME_MAX_LEN = 20;
	public static int USERTYPE_NORMAL = 0; 
	public static int USERTYPE_ADMIN = 1;
	
	public static boolean isNull( User user ){
		if( user == null )
			return true;
		
		if( user.userID == 0 
				&& user.userName == null )
			return true;
		
		return false;
	}
	
	
	public String toString() {
		return "id("+userID+"), name("+userName+"), signin_dt("+signin_dt+")";
	}
	
	public Integer getUserID() {
        return this.userID;
    }
    public String getUserName() {
        return this.userName;
    }
   
    public boolean isAdmin() {
		return bAdmin;
	}

    public void setAdmin(Integer isAdmin) {
    	if( isAdmin == USERTYPE_ADMIN )
    		this.bAdmin = true;
    	else
    		this.bAdmin = false;
	}
    
    public static boolean checkUserID( Integer userId ) {
        if( userId == null || userId < 1 ) {
            return false;
        } 
        return true;
    }

    
    public static boolean checkUserName( String username ) {
    	if( StringHelper.isNull(username)){
             return false;
        }
    	return true;
   }
    
    /*
     *  user name
     *  영문, 숫자 가능.  
     *  20자 이내
     */
    private static final Pattern patternAlphaNumeric = Pattern.compile("^[a-zA-Z0-9]*$");
    public static EResultCode checkUserNameDetail( String username ) {
        if( StringHelper.isNull(username)){
            log.error("username is null");
        	return EResultCode.INVALID_USERNAME;
        }
        
        if( username.length() > USERNAME_MAX_LEN ){
        	log.error("Invalid username length. len("+ username.length() +"), name("+ username +")");
        	return EResultCode.INVALID_USERNAME_LENGTH;
        }
        
        Matcher m = patternAlphaNumeric.matcher(username);
        if(false == m.matches()) {
        	log.error("Invalid username. Only alphanumeric allowed. name("+ username +")");
        	return EResultCode.INVALID_USERNAME_NONE_ALPHANUMERIC;
        }
       
        return EResultCode.SUCCESS;
    }

}

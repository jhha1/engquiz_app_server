package exception;

import exception.contents.ContentsRoleException;
import exception.system.DBException;
import exception.system.MyIllegalArgumentException;
import exception.system.MyIllegalStateException;
import exception.system.ProtocolDataHandlingException;
import exception.system.ProtocolParameterException;
import exception.system.SystemException;
import net.protocol.EResultCode;

public class ExceptionHandler {

	public static boolean isCustomException( Exception e ) {
		if( e instanceof SystemException 
				|| e instanceof ContentsRoleException )
			return true;
		else 
			return false;
	}
	
	public static EResultCode getCodeForClient( Exception e ) 
	{
		if( e == null ) 
			return EResultCode.UNKNOUN_ERR;
		
		if( e instanceof ErrorCode ) 
		{
			EResultCode code = ((ErrorCode)e).getErrorCode();
			if( code == null )
				return EResultCode.UNKNOUN_ERR;
			return code;
		}
		
		if( e instanceof MyIllegalStateException ) return EResultCode.SYSTEM_ERR;
		else if( e instanceof ProtocolParameterException ) return EResultCode.INVALID_ARGUMENT;
		else return EResultCode.UNKNOUN_ERR;
	}
	
	public static String getMsgForClient( Exception e ) 
	{
		String none = "NONE";
		if( e instanceof DBException ) return none;
		else if( e instanceof exception.system.MyIllegalStateException ) return none;
		else if( e instanceof exception.system.ProtocolParameterException ) return e.getMessage();
		else if( e instanceof ContentsRoleException ) return e.getMessage();
		else if( e instanceof ProtocolDataHandlingException ) return e.getMessage();
		else return none;
	}
}

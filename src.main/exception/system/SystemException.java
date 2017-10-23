package exception.system;

import exception.ErrorCode;
import net.protocol.EResultCode;

@SuppressWarnings("serial")
public abstract class SystemException extends RuntimeException implements ErrorCode
{
	public SystemException () 
	{}
	
	public SystemException (String msg) {
		super(msg);
	}
	
	public SystemException (Throwable cause) {
		super(cause);
	}

	public SystemException (String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public abstract EResultCode getErrorCode();

}

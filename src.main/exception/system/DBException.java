package exception.system;

import net.protocol.EResultCode;

@SuppressWarnings("serial")
public class DBException extends SystemException 
{
	EResultCode code = EResultCode.DB_ERR;

	public DBException () 
	{}
	
	public DBException (String msg) {
		super(msg);
	}
	
	public DBException (Throwable cause) {
		super(cause);
	}
	
	public DBException (EResultCode code) {
		this.code = code;
	}
	
	public DBException (EResultCode code, String msg) {
		super(msg);
		this.code = code;
	}
	
	public DBException (EResultCode code, Throwable cause) {
		super(cause);
		this.code = code;
	}
	
	public DBException (String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public DBException (EResultCode code, String msg, Throwable cause) {
		super(msg, cause);
		this.code = code;
	}

	@Override
	public EResultCode getErrorCode() {
		return this.code;
	}

}

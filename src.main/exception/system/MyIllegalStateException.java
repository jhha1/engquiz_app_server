package exception.system;

import net.protocol.EResultCode;

@SuppressWarnings("serial")
public class MyIllegalStateException extends SystemException
{
	EResultCode code = EResultCode.SYSTEM_ERR;
	
	public MyIllegalStateException () 
	{}
	
	public MyIllegalStateException (String msg) {
		super(msg);
	}
	
	public MyIllegalStateException (Throwable cause) {
		super(cause);
	}
	
	public MyIllegalStateException (EResultCode code) {
		this.code = code;
	}
	
	public MyIllegalStateException (EResultCode code, String msg) {
		super(msg);
		this.code = code;
	}
	
	public MyIllegalStateException (EResultCode code, Throwable cause) {
		super(cause);
		this.code = code;
	}
	
	public MyIllegalStateException (String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public MyIllegalStateException (EResultCode code, String msg, Throwable cause) {
		super(msg, cause);
		this.code = code;
	}

	@Override
	public EResultCode getErrorCode() {
		return this.code;
	}
}

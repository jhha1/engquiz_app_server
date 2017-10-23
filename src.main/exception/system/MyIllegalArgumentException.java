package exception.system;

import net.protocol.EResultCode;

@SuppressWarnings("serial")
public class MyIllegalArgumentException extends SystemException
{
	EResultCode code = EResultCode.INVALID_ARGUMENT;
	
	public MyIllegalArgumentException () 
	{}
	
	public MyIllegalArgumentException (String log) {
		super(log);
	}
	
	public MyIllegalArgumentException (Throwable cause) {
		super(cause);
	}
	
	public MyIllegalArgumentException (EResultCode code) {
		this.code = code;
	}
	
	public MyIllegalArgumentException (EResultCode code, String log) {
		super(log);
		this.code = code;
	}
	
	public MyIllegalArgumentException (EResultCode code, Throwable cause) {
		super(cause);
		this.code = code;
	}
	
	public MyIllegalArgumentException (String log, Throwable cause) {
		super(log, cause);
	}
	
	public MyIllegalArgumentException (EResultCode code, String log, Throwable cause) {
		super(log, cause);
		this.code = code;
	}
	

	@Override
	public EResultCode getErrorCode() {
		return this.code;
	}
}

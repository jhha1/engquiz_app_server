package exception.system;

import net.protocol.EResultCode;

@SuppressWarnings("serial")
public class ProtocolParameterException extends SystemException
{
	EResultCode code = EResultCode.INVALID_ARGUMENT;
	
	public ProtocolParameterException () 
	{}
	
	public ProtocolParameterException (String log) {
		super(log);
	}
	
	public ProtocolParameterException (Throwable cause) {
		super(cause);
	}
	
	public ProtocolParameterException (EResultCode code) {
		this.code = code;
	}
	
	public ProtocolParameterException (EResultCode code, String log) {
		super(log);
		this.code = code;
	}
	
	public ProtocolParameterException (EResultCode code, Throwable cause) {
		super(cause);
		this.code = code;
	}
	
	public ProtocolParameterException (String log, Throwable cause) {
		super(log, cause);
	}
	
	public ProtocolParameterException (EResultCode code, String log, Throwable cause) {
		super(log, cause);
		this.code = code;
	}
	

	@Override
	public EResultCode getErrorCode() {
		return this.code;
	}
}

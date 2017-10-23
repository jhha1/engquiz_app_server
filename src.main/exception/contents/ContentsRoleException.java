package exception.contents;

import exception.ErrorCode;
import net.protocol.EResultCode;

@SuppressWarnings("serial")
public class ContentsRoleException extends Exception implements ErrorCode
{
	EResultCode code = EResultCode.UNKNOUN_ERR;
	
	public ContentsRoleException () 
	{}
	
	public ContentsRoleException (String msg) {
		super(msg);
	}
	
	public ContentsRoleException (Throwable cause) {
		super(cause);
	}
	
	public ContentsRoleException (EResultCode code) {
		this.code = code;
	}
	
	public ContentsRoleException (EResultCode code, String msg) {
		super(msg);
		this.code = code;
	}
	
	public ContentsRoleException (EResultCode code, Throwable cause) {
		super(cause);
		this.code = code;
	}
	
	public ContentsRoleException (String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public ContentsRoleException (EResultCode code, String msg, Throwable cause) {
		super(msg, cause);
		this.code = code;
	}

	@Override
	public EResultCode getErrorCode() {
		return this.code;
	}
}

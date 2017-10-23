package exception.system;

import exception.ErrorCode;
import net.ClientInfo;
import net.protocol.EResultCode;

@SuppressWarnings("serial")
public class ProtocolDataHandlingException extends SystemException
{
	EResultCode code = EResultCode.UNKNOUN_ERR;
	StringBuffer msgTrace = new StringBuffer();
	
	// RequestException만 갖는 특수 변수.
	// 요청데이터 parsing이 완료 안된 상태에서   예외가 발생한경우, client 구분 및 protocol 정보를 알기위해.
	ClientInfo clientInfo = new ClientInfo();
	// 요청데이터 parsing이 완료 안된 상태에서 예외발생한 경우, client가 보낸 원본 데이터 알기위해
	String requestedJson = new String();
	
	public ProtocolDataHandlingException () 
	{}
	
	public ProtocolDataHandlingException (String msg) {
		super(msg);
	}
	
	public ProtocolDataHandlingException (Throwable cause) {
		super(cause);
		setCode( cause );
		setLogmsgTrace( cause );
	} 
	
	public ProtocolDataHandlingException (EResultCode code) {
		this.code = code;
	}
	
	public ProtocolDataHandlingException (EResultCode code, String msg) {
		super(msg);
		this.code = code;
	}
	
	public ProtocolDataHandlingException (EResultCode code, Throwable cause) {
		super(cause);
		this.code = code;
		setLogmsgTrace( cause );
	}
	
	public ProtocolDataHandlingException (String msg, Throwable cause) {
		super(msg, cause);
		setCode( cause );
		setLogmsgTrace( cause );
	}
	
	public ProtocolDataHandlingException (EResultCode code, String msg, Throwable cause) {
		super(msg, cause);
		this.code = code;
		setLogmsgTrace( cause );
	}
	
	// 특수 케이스: see. clientInfo 변수 선언 라인.
	public ProtocolDataHandlingException (Throwable cause, ClientInfo clientInfo, String requestedJson ) {
		super(cause);
		setCode( cause );
		setLogmsgTrace( cause );	
	
		this.clientInfo = clientInfo;
		this.requestedJson = requestedJson;
	}
	
	// exception call stack에서, 하위 예외객체에 저장한 resultCode를 알기위해. 
	//  -> 하위 함수에서 발생한 예외가 상위 함수로 throw도는 과정에서, 예외가 다른예외로 감싸진다. 		
	//     감싼 상위 예외에서는 ResultCode를 알 수 없으므로, resultCode보존을 위해 이렇게 함.
	//  -> 상위 예외에서는, 이 ResultCode를 그대로 클라에게 넘겨주거나
	//     적절한 예외코드로 변경하여 클라에게 넘겨줄수있다
	private void setCode( Throwable cause ) 
	{
		if( cause instanceof ErrorCode )
			this.code = ((ErrorCode) cause).getErrorCode();
	}
	
	private void setLogmsgTrace( Throwable cause )
	{
		
		if( cause!=null 
				&& cause.getMessage() != null
				&& !cause.getMessage().isEmpty() )
			this.msgTrace.append( cause.getMessage() );
	}

	@Override
	public EResultCode getErrorCode() {
		return this.code;
	}
	
	public String getMsgTrace() {
		return this.msgTrace.toString();
	}
	
	public ClientInfo getClientInfo() 
	{
		if( this.clientInfo == null )
			return new ClientInfo();
		
		return this.clientInfo;
	}
	
	public String getRequestedJson() {
		return this.requestedJson;
	}
}

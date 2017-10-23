package net.protocol;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import exception.ExceptionHandler;
import exception.system.ProtocolDataHandlingException;
import net.ClientInfo;


public class Protocol 
{
	final static Logger log = Logger.getLogger(Protocol.class);
	
	public Request request = null;
	public Response response = null;
	
	public Integer pid = 0;
	public Integer userId = 0;
	
	public Protocol( HttpServletRequest httpRequest, 
					HttpServletResponse httpResponse ) 
					throws ProtocolDataHandlingException
	{	
		try 
		{
			this.request = new Request( httpRequest );
			this.response = new Response( httpResponse, request );	
	
			this.pid = (Integer) this.request.get(EProtocol.iPID);
			this.userId = (Integer) this.request.get(EProtocol.UserID);
		}
		catch( ProtocolDataHandlingException e ) 
		{
			throw e;
		}
		catch( Exception e )
		{
			// client 정보를 로깅하고, 
			// error response protocol에   필수 값(pid, userId)을 넣기 위해.
			ClientInfo clientInfo = (this.request != null)? this.request.getClientInfo() : new ClientInfo();
			String requestedJson = (this.request != null)? this.request.getRequestJson(): new String();
			throw new ProtocolDataHandlingException(e, clientInfo, requestedJson);
		}
	}
	
	public void setSuccess() {
		this.response.setSuccess();
	}
	
	public void setFail( Exception e ) {
		EResultCode code = ExceptionHandler.getCodeForClient(e);
		String msg = ExceptionHandler.getMsgForClient(e); 
		setFail( code, msg );
	}
	
	public void setFail( EResultCode code, String msg ) {
		this.response.setFail(code, msg);
	}
	
	public void setFail( EResultCode code ) {
		this.response.setFail(code);
	}
	
	public ClientInfo getClientInfo() 
	{
		return request.getClientInfo();
	}
	
	public String toString()
	{
		return request.toString() + ", "
				+ response.toString();
	}
}

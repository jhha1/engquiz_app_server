package net.protocol;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import utils.JsonHelper;
import utils.LogFormater;
import exception.ExceptionHandler;
import exception.system.MyIllegalArgumentException;
import exception.system.ProtocolDataHandlingException;
import net.ClientInfo;

public class Response 
{
	final static Logger log = Logger.getLogger(Response.class);
	
	private HttpServletResponse httpResponse = null;
	private Map<EProtocol, Object> map = new HashMap<EProtocol, Object>();
	private String responseJson = null;
	
	final private static String ContentType = "text/html; charset=utf-8";
	final private static String DefaultFailResponseString = "JSON={\"CODE\":\"9999\", \"MSG\":\"UnknownERR\"}";
	
	private ClientInfo clientInfo = null;
	
	public Response( HttpServletResponse httpResponse, Request request ) 
					throws ProtocolDataHandlingException 
	{
		try 
		{
			initHttpResponse( httpResponse );
			initResponseMap( request );
		} 
		catch (Exception e)
		{
			ClientInfo clientInfo = (request != null)? request.getClientInfo(): new ClientInfo();
			String requestedJson = (request != null)? request.getRequestJson(): new String();
			throw new ProtocolDataHandlingException(e, clientInfo, requestedJson);
		}
	}
	
	private Response( HttpServletResponse httpResponse )
	{
		initHttpResponse( httpResponse );
	}
	
	public Object get( EProtocol key ) 
	{
		if( null == key ) {
			log.error("invalid parameter: map key is null");
			return null;
		}
		
		if( false == this.map.containsKey(key) ) {
			log.error("no existed map key [" +key+ "]");
			return null;
		}
		
		return this.map.get(key);
	}
	
	public Boolean set( EProtocol key, Object value ) 
	{
		if( null == key || null == value ) {
			log.error("invalid parameter. key["+key+"], value["+value+"]");
			return false;
		}
		
		this.map.put(key, value);
		
		return true;
	}
	
	private void initHttpResponse( HttpServletResponse httpResponse )
	{
		if( httpResponse == null ) {
			throw new MyIllegalArgumentException(EResultCode.SYSTEM_ERR, 
												"httpServletResponse is null");
		}
		this.httpResponse = httpResponse;
		setResponseConfig();
	}
	
	private void initResponseMap( Request request )
	{
		if( request == null ) {
			throw new MyIllegalArgumentException(EResultCode.SYSTEM_ERR,
												"Cannot Init ResponseMap. Request is null!");
		}
		this.map.put( EProtocol.CODE, EResultCode.UNKNOUN_ERR );
		this.map.put( EProtocol.MSG, "NONE" );
		this.map.put( EProtocol.PID, request.get(EProtocol.PID) );
		this.map.put( EProtocol.UserID, request.get(EProtocol.UserID) );
		
		this.clientInfo = request.getClientInfo();
	}

	public void setSuccess()
	{
		this.map.put(EProtocol.CODE, EResultCode.SUCCESS);
		this.map.put(EProtocol.MSG, "SUCCESS");
	}
	
	public void setFail( EResultCode code )
	{
		setFail( code, "NONE" );
	}
	
	public void setFail( EResultCode code, String msg )
	{	
		this.map.put(EProtocol.CODE, code);
		this.map.put(EProtocol.MSG, msg);
	}
	
	public void respond() 
	{ 
		try 
		{
			check(); 
			this.responseJson = makeResponseData();
		} 
		catch( Exception e )
		{
			this.responseJson = makeResponseDataError( e );
			
			log.error("Failed Response handling. "
					+ "will send: " + this.responseJson
					+ clientInfo.toString()
					+ LogFormater.exceptionLog(e));
			e.printStackTrace();
		}
		
		respondImpl( httpResponse, responseJson );
	}
	
	public void respondError( Exception e )
	{
		this.responseJson = makeResponseDataError( e );
		
		respondImpl( httpResponse, responseJson );
	}
	
	public static void respondError( HttpServletResponse httpResponse, Exception e )
	{
		try 
		{
			Response response = new Response(httpResponse);
			response.map.put( EProtocol.CODE, EResultCode.UNKNOUN_ERR );
			response.map.put( EProtocol.MSG, "NONE" );
				
			if( e instanceof ProtocolDataHandlingException ) {
				response.clientInfo = ((ProtocolDataHandlingException) e).getClientInfo();
			} else {
				response.clientInfo = new ClientInfo();
			}
			response.map.put( EProtocol.PID, response.clientInfo.getPID() );
			response.map.put( EProtocol.UserID, response.clientInfo.getUserID() );
			response.respondError(e);
		} 
		catch (Exception errorResponseException) 
		{
			log.error("Failed Error Response." 
					+ "msg:"+ errorResponseException.getMessage()
					+ ", cause:" + errorResponseException.getCause());
		}
	}
	
	private String makeResponseData() 
	{
		try 
		{
			return makeResponseDataImpl( this.map );
		}  
		catch (MyIllegalArgumentException e) 
		{
			// response map은 서버에서 만드는 것이므로, map의 이상은 system err로 내보냄.
			throw new exception.system.MyIllegalStateException("makeResponseDataError. ", e);
		}
	}
	
	private String makeResponseDataError( Exception e ) 
	{
		try 
		{
			this.map.put(EProtocol.CODE, ExceptionHandler.getCodeForClient(e));
			this.map.put(EProtocol.MSG, "NONE");
			
			return makeResponseDataImpl( this.map );
		}
		catch( Exception makingErrorDataException )
		{
			log.error("Failed Making Error Response Data. "
						+ "will send: " + DefaultFailResponseString  
						+ clientInfo.toString()
						+ LogFormater.exceptionLog(makingErrorDataException));
			e.printStackTrace();
			return DefaultFailResponseString;
		}
	}
	
	private String makeResponseDataImpl( Map<EProtocol, Object> resmap ) 
	{
		EResultCode code = (EResultCode) resmap.get(EProtocol.CODE);
		if( code != null )
			resmap.replace(EProtocol.CODE, code.stringCode());
		
		Map<String, Object> stringKeyResponseMap = toString_responseMapKeys(resmap);
		return EProtocol.JSON.string() + "=" + JsonHelper.map2json( stringKeyResponseMap );
	}
	
	private void check()
	{
		if( httpResponse == null ) {
			throw new IllegalStateException("httpServletResponse is Null.");
		}
		
		if( this.map == null || this.map.isEmpty() )
			throw new IllegalStateException("protocolMapNull:"+this.map);
		
		try 
		{
			ProtocolParamChecker.checkProtocolRequiredParams( this.map );
		}
		catch( MyIllegalArgumentException e )
		{
			// response map은 서버에서 만드는 것이므로, map의 이상은 system err로 내보냄.
			throw new IllegalStateException(e);
		}
	}

	private void setResponseConfig() 
	{
		this.httpResponse.setContentType( ContentType );
	}
	
	private void respondImpl( HttpServletResponse httpResponse, 
							String responseData ) 
	{
		try 
		{
			if( httpResponse == null )
				throw new MyIllegalArgumentException("httpResponse is null");
			if( httpResponse.getWriter() == null )
				throw new IllegalStateException("httpResponse Writer is null");
			
			// send characters. 
			// if want to send byte, use PrintStream. 
			httpResponse.getWriter().println( responseData );
		} 
		catch (Exception e) 
		{
			log.error( makeRespondErrorMsg(e) );
			e.printStackTrace();
		}
	}
	
	private String makeRespondErrorMsg( Exception e ) 
	{
		StringBuffer errmsg = new StringBuffer();
		if( e instanceof IOException ) {
			errmsg.append("Disconnected Client");
		} 
		else {
			errmsg.append(e.getMessage());
		}
		errmsg.append(" {");
		if( httpResponse!=null ) {
			errmsg.append("httpStatus:"+ httpResponse.getStatus() +", ");
		}
		if( clientInfo != null ) {
			errmsg.append(clientInfo.toStringContentsHeaderInfo() +", ");
			errmsg.append(clientInfo.toStringNetworkInfo());
		}
		errmsg.append("}");
		return errmsg.toString();
	}
	
	private Map<String, Object> toString_responseMapKeys( Map<EProtocol, Object> resmap ) 
	{
		if( resmap == null || resmap.isEmpty() ) 
			throw new MyIllegalArgumentException(EResultCode.INVALID_ARGUMENT, 
												"ResponseMap is null or empty (map:"+ resmap +")");
		
		Map<String, Object> stringKeyResponseMap = new HashMap<String, Object>();
		for( Entry<EProtocol, Object> e : resmap.entrySet() ) 
		{
			if( e.getKey() == null ) 
				throw new MyIllegalArgumentException(EResultCode.INVALID_ARGUMENT, 
													"Invalid Protocol Field:" + e.getKey());
			
			String stringKey = ((EProtocol) e.getKey()).string();
			if( stringKey == null ) 
				throw new MyIllegalArgumentException(EResultCode.INVALID_ARGUMENT, 
													"Invalid Protocol Field:" + e.getKey() + "," + stringKey);
			
			String upperStringKey = stringKey.trim().toUpperCase();
			stringKeyResponseMap.put( upperStringKey, e.getValue() );
		}
		return stringKeyResponseMap;
	}
	
	
	public String getRespondedJson()
	{
		return this.responseJson;
	}
	
	public String toString()
	{
		return "resmap:"+ this.map;
	}
}



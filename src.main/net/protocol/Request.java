package net.protocol;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import exception.system.MyIllegalArgumentException;
import exception.system.MyIllegalStateException;
import exception.system.ProtocolDataHandlingException;
import utils.JsonHelper;
import net.ClientInfo;

public class Request 
{
	final static Logger log = Logger.getLogger(Request.class);
	
	private Map<EProtocol, Object> map = null;
	private String requestedJson = new String(); // 필요없으면 지운다
	
	private ClientInfo clientInfo = new ClientInfo();

	public Request( HttpServletRequest httpRequest ) throws ProtocolDataHandlingException
	{
		try 
		{
			if( httpRequest == null ) {
				throw new MyIllegalArgumentException(EResultCode.SYSTEM_ERR, "httpRequest is Null.");
			}
			
			// extract client information. (for logging)
			this.clientInfo.initNetworkInfo(httpRequest);

			this.map = parse( httpRequest );
			
			// extract user default information. (for logging)
			this.clientInfo.initProtocolInfo( this.map );
						
			checkParameters( this.map );
		}
		catch( Exception e )
		{
			throw new ProtocolDataHandlingException(e, this.clientInfo, this.requestedJson);
		}
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
	
	private Map<EProtocol, Object> parse( HttpServletRequest httpRequest )  
	{
		this.requestedJson = extractContentsBody( httpRequest );
		// TODO object 값들을 모두 string으로??
		Map<String, Object> reqmap = JsonHelper.json2map( this.requestedJson );
		return toEnum_reqeustMapKeys(reqmap);
	}
	
	private String extractContentsBody( HttpServletRequest httpRequest ) 
	{
		if( httpRequest == null ) {
			throw new MyIllegalStateException("httpServletRequest is Null.");
		}
		
		if( httpRequest.getParameterMap() == null ) {
			throw new MyIllegalArgumentException(
						EResultCode.INVALID_ARGUMENT, 
						"httpRequestBodyMap is null");
		}
		
		Map<String, String[]> httpRequestBodyMap = httpRequest.getParameterMap();
		Map<String, String[]> upperKeyHttpRequestBodyMap = toUpper_httpBodyParameterkeys(httpRequestBodyMap);
		String[] jsonString = upperKeyHttpRequestBodyMap.get( EProtocol.JSON.string() );
		if( jsonString == null || jsonString[0] == null || jsonString[0].isEmpty() ) {
			throw new MyIllegalArgumentException(EResultCode.INVALID_ARGUMENT, 
												"Null Or Empty 'json' param:"+jsonString);
		}	
		
		return jsonString[0];
	}
	
	private Map<String, String[]> toUpper_httpBodyParameterkeys( Map<String, String[]> httpRequestBodyMap )
	{
		Map<String, String[]> upperKeyMap = new HashMap<String, String[]>();		
		httpRequestBodyMap.forEach((k,v) -> upperKeyMap.put(k.toUpperCase(), v));
		return upperKeyMap; 
	}
	
	private Map<EProtocol, Object> toEnum_reqeustMapKeys( Map<String, Object> reqmap ) 
	{
		if( reqmap == null || reqmap.isEmpty() ) 
			throw new MyIllegalArgumentException(EResultCode.INVALID_ARGUMENT, 
												"RequestMap is null or empty (map:"+reqmap+")");
		
		Map<EProtocol, Object> dst = new HashMap<EProtocol, Object>();
		for( Entry<String, Object> e : reqmap.entrySet() )
		{
			if( e.getKey() == null ) 
				throw new MyIllegalArgumentException(EResultCode.INVALID_ARGUMENT, 
													"Invalid Protocol Field:" + e.getKey());
			EProtocol enumKey = EProtocol.toEnum( e.getKey() );
			if( EProtocol.NULL == enumKey )
				throw new MyIllegalArgumentException(EResultCode.INVALID_ARGUMENT, 
													"Invalid Protocol Field:" + e.getKey());
			
			dst.put( enumKey, e.getValue() );
		}
		return dst;
	}
	
	private void checkParameters( Map<EProtocol, Object> reqmap ) 
	{
		ProtocolParamChecker.checkProtocolRequiredParams( reqmap );
		changePID2String_and_addIntegerPID();
	}
	
	private void changePID2String_and_addIntegerPID()
	{	
		if( this.map == null )
			return;
		
		Object pid = this.map.get( EProtocol.PID );
		// test 결과, long, biginteger, short 모두 integer로 인식됨..
		if( pid instanceof Integer )
		{
			this.map.put( EProtocol.iPID, pid );
			this.map.put( EProtocol.PID, String.valueOf(pid) );
		}
		else if( pid instanceof String )
		{
			Integer iPID = Integer.valueOf( (String)pid );
			this.map.put( EProtocol.iPID, iPID );
		}
		else
		{
			throw new MyIllegalStateException("Illegal PID Type:"+pid);
		}
	}
	
	public String getRequestJson()
	{
		return this.requestedJson;
	}
	
	public ClientInfo getClientInfo()
	{
		return this.clientInfo;
	}
	
	public String toString()
	{
		return "reqmap"+ this.map; 
	}
}

package net;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.protocol.EProtocol;

/*
 *  Request 에서 client의 요청 데이터를 파싱하는 도중에 생성되며, Response에서 pointer를 가지고 있다.
 *  
 *  사용처   : logging
 *  client를 구분할수있는 값들이 들어있으므로, 모든 곳에서 로깅에 사용.
 */
public class ClientInfo 
{
	
	private Object pid = null;  
	private Object userID = null;
	private Object userName = null;
	
	private String ip = null;
	private Integer port = 0;
	private String hostName = null;
	
	public ClientInfo(){}
	
	public void initNetworkInfo( HttpServletRequest httpRequest )
	{
		if( httpRequest == null )
		{
			// TODO using logger
			System.out.println("httpServletRequest is null");
			return;
		}
		this.ip = httpRequest.getRemoteAddr();
		this.port = httpRequest.getRemotePort();
		this.hostName = httpRequest.getRemoteHost();
	}
	
	// 유저구분 logging용으로만 쓰이므로, 값의 이상으로 로직을 중지시키지 않는다.
	// TODO method naming,  pid enum화....    
	public void initProtocolInfo( Map<EProtocol, Object> reqmap )
	{
		if( reqmap == null || reqmap.isEmpty() )
		{
			System.out.println("requestMap is null");
			return;
		}
		
		this.pid = reqmap.getOrDefault(EProtocol.PID, "");
		this.userID = (Integer) reqmap.getOrDefault(EProtocol.UserID, -1);
		this.userName = (String) reqmap.getOrDefault(EProtocol.UserName, "");
	}
	
	public Object getPID() 
	{
		return this.pid;
	}
	
	public Object getUserID()
	{
		return this.userID;
	}
	
	public String toStringNetworkInfo()
	{
		return "ip:"+ this.ip + 
				",port:"+ this.port +
				",hostName:"+ this.hostName;
	}
	
	public String toStringContentsHeaderInfo()
	{
		return "pid:"+ this.pid 
				+ ",user(ID:"+ this.userID
				+ ",Name:" + this.userName +")";
	}
	
	public String toStringAllInfo() 
	{
		return toStringNetworkInfo()
				+", "+ toStringContentsHeaderInfo();
	}
	
	public String toString() 
	{
		return toStringAllInfo();
	}
}


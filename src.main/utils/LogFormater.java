package utils;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;

import net.ClientInfo;
import net.protocol.EProtocol;
import net.protocol.Protocol;
import net.protocol.Request;
import net.protocol.Response;
import exception.ErrorCode;
import exception.system.ProtocolDataHandlingException;
import exception.system.SystemException;

public class LogFormater 
{	
	final static int CurruntCallStackDepth = 2;
	
	private LogFormater(){}
	
	// 몇번째 스택 trace에서 발생한 에러의
	// 발생위치를 가져옴. 
	// .. 별로 필요 없어서 버릴까 고민중
	//
	// method + line
	// example: 
	// 1. log.info( MsgFormat.ML() + "some msg..");
	// 	    -> [MyMethod:32] some msg.. 
	// 2. return MsgFormat.ML() + "some reason..";
	//     -> Logic Error! [MyMethod:32] some reason...
	public static String ML() { 
		return ML(CurruntCallStackDepth);
	}
	
	//
	// class + method + line
	// example: 
	// log.info( MsgFormat.CML() + "some msg..");
	// 	-> [MyClass.MyMethod:32] some msg.. 
	//
	public static String CML() { 
		return CML(CurruntCallStackDepth);
	}
	
	private static String CML(int callStackDepth) {
		return "["+className(callStackDepth)+"."+methodName(callStackDepth) + ":" + lineNumber(callStackDepth) + "] ";
	}
	
	private static String ML(int callStackDepth) {
		return "["+methodName(callStackDepth) + ":" + lineNumber(callStackDepth) + "] ";
	}
	
	private static String className( int callStackDepth ) {
		
		return Thread.currentThread().getStackTrace()[ callStackDepth ].getClassName();
	}
	
	private static String methodName( int callStackDepth ) {
		return Thread.currentThread().getStackTrace()[ callStackDepth ].getMethodName();
	}
	
	private static int lineNumber( int callStackDepth ) {
		return Thread.currentThread().getStackTrace()[ callStackDepth ].getLineNumber();
	}
	
	public static String START_PROTOCOL( HttpServletRequest httpRequest, int uniqueKey )
	{
		StringBuffer sb = new StringBuffer("START_PROTOCOL ");
		String contentType = (httpRequest.getHeader("Content-Type")==null)?"null":httpRequest.getHeader("Content-Type");
		sb.append("content-type" + contentType);
		sb.append("hash:"+ uniqueKey);
		sb.append(", body:{");
		if( httpRequest != null && httpRequest.getParameterMap() != null ) 
		{
			httpRequest.getParameterMap().forEach((k,v) 
							-> sb.append(k + "=" + 
								((v != null)? Arrays.asList(v).toString():null)));
		}
		sb.append("}");
		return sb.toString();
	}
	
	public static String END_PROTOCOL( Protocol p, int uniqueKeyForLog )
	{
		if( p == null ) {
			return "END_PROTOCOL {"
					+ "hash:"+uniqueKeyForLog
					+ ",ERROR!!! Protocol is null!!!";
		}
 		
		String resultCode = null;
		if( p.response != null )
			resultCode = (String) p.response.get(EProtocol.CODE);
		
		String protocolInfo = null;
		if( p.getClientInfo() != null ) {
			protocolInfo = p.getClientInfo().toStringContentsHeaderInfo();
		}
		
		String requestFullData = null;
		if( p.request != null ) {
			requestFullData = p.request.toString()
								+", reqjson:"
								+ p.request.getRequestJson();
		}
		String responseFullData = null;
		if( p.response != null ) {
			responseFullData = p.response.toString()
								+", resjson:"
								+ p.response.getRespondedJson();
		}
		
		return "END_PROTOCOL {"
				+ "hash:"+uniqueKeyForLog +", "
				+ "result{"+ resultCode + "}, "
				+ protocolInfo + ", "
				+ "\n"+ responseFullData
				+ "}";
	}
	
	public static String END_PROTOCOL( Protocol p, Exception e, int uniqueKeyForLog )
	{
		try {
			String resultCode = null;
			Request req = (p != null)? p.request: null;
			Response res = (p != null)? p.response: null;
			
			// ResultCode
			// log 우선순위 1) responseMap에 저장된 code
			if( res != null ) {
				resultCode = (String) res.get(EProtocol.CODE);
			}
			
			// log 우선순위 2) exception에 저장된 code
			if( resultCode == null ) {
				if( e instanceof ErrorCode ) 
					resultCode = ((ErrorCode)e).getErrorCode().stringCode();
			}
			
			// Pid, userId
			// log 우선순위 1) 파라메터의 info
			String contentsHeaderInfo = null;
			if( p != null && null != p.getClientInfo() ) {
				contentsHeaderInfo = p.getClientInfo().toStringContentsHeaderInfo();
			}
			// log 우선순위 2) exception에 저장된 값
			if( contentsHeaderInfo == null ) {
				if( e instanceof ProtocolDataHandlingException ) {
					ClientInfo cliInfo = ((ProtocolDataHandlingException)e).getClientInfo();
					contentsHeaderInfo = (cliInfo != null)? cliInfo.toStringContentsHeaderInfo(): "unknown PID";
				}
			}
			
			String reqmap = (req != null)? req.toString(): "reqmap{}";
			String resmap = (res != null)? res.toString(): "resmap{}";
			String requestedJson = (req != null )? req.getRequestJson(): "";
			String responseJson = (res != null )? res.getRespondedJson(): "";
			return "END_PROTOCOL {"
					+ "hash:"+ uniqueKeyForLog +", "
					+ "result{"+ resultCode +"}, "
					+ contentsHeaderInfo
					+ exceptionLog(e) + ", "
					+ "\n resjson:"+ responseJson
					+ "}";

		} catch ( Exception e1 ) {
			System.out.println("Failed Logging ErrorResponse:" + e.getMessage()+","+e.getCause());
		}
		return "Failed Logging ErrorResponse";
	}
	
	public static String exceptionLog( Exception e )
	{
		if( e == null ) 
			return new String();

		StringBuffer sb = new StringBuffer();
		if( e instanceof ErrorCode )
			sb.append("code:"+ ((ErrorCode)e).getErrorCode());
		sb.append(", msg: "+e.getMessage());
		sb.append(", cause: "+e.getCause());
		return sb.toString();
	}

	public static String logicErrorLog( Exception e, ClientInfo info ) 
	{
		if( e == null ) 
			return new String();
		
		StringBuffer sb = new StringBuffer();
		
		if( info != null ) {
			sb.append( info.toStringContentsHeaderInfo() + ". ");
		}
		sb.append( exceptionLog(e) );
		
		// 미 예상 에러는 스택트레이스
		if( false == e instanceof ErrorCode ) {
			sb.append(", stacktrace: ");
			for(int i = 0; i < e.getStackTrace().length; ++i){
				sb.append("["+i+"] " + e.getStackTrace()[i] + "\n");
			}
		}
		
		return sb.toString();
	}
}

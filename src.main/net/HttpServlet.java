package net;

import org.apache.log4j.Logger;

import app.Initailizer;
import contents.front.FrontLogic;
import contents.front.LogicFactory;
import utils.LogFormater;

import java.util.LinkedHashMap;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.protocol.Protocol;
import net.protocol.Response;

@SuppressWarnings("serial")
@WebServlet("/servlet")
public class HttpServlet extends javax.servlet.http.HttpServlet 
{
	final static Logger log = Logger.getLogger(HttpServlet.class);
	int uniqueKeyForLog;
	
    public HttpServlet() 
    {
        super();
     // TODO 임시적. requestServletObject hashcode 쫑 여부 test안됬음  . 중복됨!!!!!!!
        uniqueKeyForLog = 0;
    }
    
    // 첫번째 http request 시에 호출됨
    public void init() 
    {
    	Initailizer.getInstance().init();
    }
    
    public void destroy() 
    {
    	Initailizer.getInstance().destroy();
    }

	protected void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) 
	{
		String resBody = "<html><head></head><body>GET Response OK</body></html>";
		try {
			httpResponse.setContentType("text/html; charset=utf-8");
			httpResponse.getWriter().println( resBody );
		} catch (Exception e) {
			log.error("HTTP GET Fail." + e.getMessage());
			e.printStackTrace();
		}
	}

	protected void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse) 
	{
		System.out.println("----------------------------------------------------------------------");
		log.info( LogFormater.START_PROTOCOL(httpRequest, uniqueKeyForLog) );
		
		Protocol protocol = null;
		try
		{
			protocol = new Protocol( httpRequest, httpResponse );
		}
		catch( Exception e )
		{
			Response.respondError(httpResponse, e); 
			
			log.info( LogFormater.END_PROTOCOL(protocol, e, uniqueKeyForLog) );
			e.printStackTrace();
			return;
		}
		
		try
		{
			FrontLogic logic = LogicFactory.createFrontLogic( protocol.pid );
			//FrontLogic logic = LogicFactory.createFrontLogic( 1000 );

			
			log.info("START LOGIC");
			logic.action( protocol );
			log.info("END LOGIC");
			
			protocol.response.respond();	
			
			log.info( LogFormater.END_PROTOCOL(protocol, uniqueKeyForLog) ); 
		}
		catch( Exception e ) 
		{
			// exception cases:
			// logic 상의 fail이 아닌, System fail 일 결우 발생.
			protocol.response.respondError(e);
			
			log.info( LogFormater.END_PROTOCOL(protocol, e, uniqueKeyForLog) );
			e.printStackTrace();
		} 
		
		System.out.println("----------------------------------------------------------------------");
	}
}

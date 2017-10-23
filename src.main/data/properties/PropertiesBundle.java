package data.properties;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import app.Initailizer;
import utils.FileHelper;

public class PropertiesBundle {
	
	private static final PropertiesBundle instance = new PropertiesBundle();
	private Map<String, String> propertiesMap = new HashMap<>();
	
	private PropertiesBundle()
	{
		final FileHelper fileHelper = FileHelper.getInstance();
		final String propertyPath = getPropertyPath();
		java.util.Properties properties = fileHelper.readPropertiesXML( propertyPath );

		Enumeration<Object> enuKeys = properties.keys();
		while (enuKeys.hasMoreElements()) {
			String key = (String) enuKeys.nextElement();
			String value = properties.getProperty(key);
			
			propertiesMap.put(key, value);
		}
	}
	
	public static PropertiesBundle getInstance() {
		return instance;
	}
	
	private static String getPropertyPath(){
		if( Initailizer.isDubugMode() )
			return "F:\\Users\\jhha\\Projects\\Engquiz\\engquiz_server\\properties.xml";
		else 
			return "/home/tomcat/properties/properties.xml";
	}
	
	private String getProperty( String key ) {
		if( propertiesMap.containsKey(key) )
			return propertiesMap.get(key);
		else
			return new String();
	}
	
	/*
	 * # connectTimeout=30000&socketTimeout=20000
	 * 
	 * http://d2.naver.com/helloworld/1321
	 * 
	 * JDBC 드라이버의 SocketTimeout 값은 DBMS가 비정상으로 종료되었거나 네트워크 장애(기기 장애 등)가 발생했을 때 필요한 값이다. 
	 * TCP/IP의 구조상 소켓에는 네트워크의 장애를 감지할 수 있는 방법이 없다. 그렇기 때문에 애플리케이션은 DBMS와의 연결 끊김을 알 수 없다. 
	 * 이럴 때 SocketTimeout이 설정되어 있지 않다면 애플리케이션은 DBMS로부터의 결과를 무한정 기다릴 수도 있다(이러한 Connection을 Dead Connection이라고 부르기도 한다).
	 * SocketTimeout을 설정하면 네트워크 장애 발생 시 무한 대기 상황을 방지하여 장애 시간을 단축할 수 있다.
	 * 
	 * SocketTimeout에는 아래 두 가지 옵션이 있고, 드라이버별로 설정 방법이 다르다.
		Socket Connect 시 타임아웃(connectTimeout): Socket.connect(SocketAddress endpoint, int timeout) 메서드를 위한 제한 시간
		Socket Read/Write의 타임아웃(socketTimeout): Socket.setSoTimeout(int timeout) 메서드를 위한 제한 시간
	 */
	public String dbUrl() {
		return getProperty( "db_url" ) + "?connectTimeout=30000&socketTimeout=20000&useUnicode=true&characterEncoding=UTF-8";
	}
	
	public String dbId() {
		return getProperty( "db_id" );
	}
	
	public String dbPassword() {
		return getProperty( "db_pwd" );
	}
}

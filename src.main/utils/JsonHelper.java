package utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import exception.system.MyIllegalArgumentException;
import exception.system.MyIllegalStateException;

public class JsonHelper {

	private static ObjectMapper jsonMapper = new ObjectMapper();
	
	public static Map<String, Object> json2map( String jsonString ) 
	{
		if( jsonString == null || jsonString.isEmpty() || jsonString.equals("{}") )
			throw new MyIllegalArgumentException("invalid Json("+ jsonString + ")");
		
		try 
		{
			return jsonMapper.readValue(jsonString, new TypeReference<HashMap<String, Object>>() {});
		} 
		catch ( Exception e ) 
		{
			throw new MyIllegalStateException("JsonAPI ERR", e);
		}
	}
	
	public static String map2json( Map map ) 
	{
		if( map == null || map.isEmpty() )
			throw new MyIllegalArgumentException("map is null or empty:("+ map +")");
		
		try 
		{ 
			return jsonMapper.writeValueAsString(map);
		}
		catch ( Exception e ) 
		{
			throw new MyIllegalStateException("JsonAPI ERR", e);
		}
	}
	
	public static Object json2list( String jsonString ) 
	{
		if( jsonString == null || jsonString.isEmpty() || jsonString.equals("{}") )
			throw new MyIllegalArgumentException("invalid Json("+ jsonString + ")");
		
		try 
		{
			return jsonMapper.readValue(jsonString, new TypeReference<List<Integer>>() {});
		} 
		catch ( Exception e ) 
		{
			throw new MyIllegalStateException("JsonAPI ERR", e);
		}
	}
	
	public static String toJson( Object object ) 
	{
		if( object == null )
			throw new MyIllegalArgumentException("object is null("+ object +")");
		
		try 
		{ 
			return jsonMapper.writeValueAsString(object);
		}
		catch ( Exception e ) 
		{
			throw new MyIllegalStateException("JsonAPI ERR", e);
		}
	}
}

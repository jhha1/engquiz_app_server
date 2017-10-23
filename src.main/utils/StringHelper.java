package utils;

public class StringHelper {
	
	public static final String EMPTY_STRING = new String();
	
	public static boolean isNull( String str ) {
		return (str == null) || str.isEmpty();
	}
}

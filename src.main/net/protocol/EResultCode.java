package net.protocol;

public enum EResultCode {
	
	SUCCESS ( 0000 ),
	
	// common
	INVALID_ARGUMENT ( 1001 ),
	NOEXSIT (1002),
	NULL_VALUE(1003),
	INVALID_DATATYPE(1004),
	NOT_AUTHORITY(1005),
	
	// pid
	INVALID_PID ( 1100 ),

	// user 
	INVALID_USERID ( 1100 ),
	INVALID_USER_ID_RANGE ( 1101 ),
	INVALID_USERNAME( 1102 ),
	INVALID_USERNAME_NONE_ALPHANUMERIC( 1103 ),
	INVALID_USERNAME_LENGTH( 1104 ),
	USERNAME_DUPLICATED( 1105 ),
	USER_CREATED_BUT_FAILED_GET_USERINFO(1106),
	NONEXIST_USER( 1107 ),
	
	
	// script
	SCRIPT__NO_HAS_KR_OR_EN (1320),
	
	// add script into quiz folder
	INVALID_SCRIPT_ID(1301),
	SCRIPT_DUPLICATED(1302),
	
	
	// sentence
	INVALID_SENTENCE ( 1401 ),
	INVALID_SENTENCE_ID ( 1402 ),
	INVALID_SENTENCE_TEXT( 1403 ),
	USER_SENTENCE_REVISION_IS_OLD_THEN_SYSTEMS(1404),
	
	// report
	REPORT_DUPLICATED(1801),
	REPORT_MEMORY_UPDATE_FAIL(1802),
	
	
	// system
	MAINTAIN_SERVER ( 9993 ),
	ENCODING_ERR(9994),
	SYSTEM_ERR ( 9995 ),
	NETWORK_ERR ( 9996 ),
	DB_ERR ( 9997 ),
	UNKNOUN_ERR ( 9998 ),
	MAX( 9999 );
	
	
	private Integer code;
	
	private EResultCode( int value ) 
	{
		this.code = value;
	}
	
	public Integer intCode() 
	{
		return code;
	}
	
	public String stringCode()
	{
		return code.toString();
	}
	
	public boolean isSuccess(){
		return code == SUCCESS.intCode();
	}
	
	public boolean isFail(){
		return !isSuccess();
	}
}

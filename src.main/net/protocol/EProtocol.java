package net.protocol;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum EProtocol {
	
	// Required 
	JSON ("JSON"),
	PID ("pid"),	// string
	iPID ("iPID"),  // integer 
	sPID ("sPID"),  // pid name
	
	// Response Only 
	CODE("code"),
	MSG("msg"),
	
	
	// user
	UserID ("UserID"),
	UserName("UserName"),
	IsExistedUser("IsExistedUser"),
	IsAdmin ("IsAdmin"),
	
	// Script
	ScriptId("ScriptId"),
	ScriptIds("ScriptIds"),
	ScriptTitle("title"),
	ScriptSentences("sentences"),
	ParsedSciprt("parsedScript"),
	SciprtPDF("scriptPDF"),
	SciprtDOCX("scriptDocx"),
	
	// Sentence
	SentenceId("SentenceId"),
	Revision("Revision"),
	SentenceKo("SenteceKo"),
	SentenceEn("SenteceEn"),
	
	// Sync
	SyncResult("SyncResult"),
	SYNC__NEED_SENTENCE_ADD("SYNC__NEED_SENTENCE_ADD"),
	SYNC__NEED_SENTENCE_DEL("SYNC__NEED_SENTENCE_DEL"),
	SYNC__NEED_SENTENCE_UPDATE("SYNC__NEED_SENTENCE_UPDATE"),
	
	
	// Report
	ReportCountAll("ReportCountAll"),
	ReportList("ReportList"),
	ReportModifyType("ReportModifyType"),
	
	// for test
	TEST("test"),
	TEST_TIME("time"),
	TEST_FLOAT("-float"),
	TEST_INTMAX("intmax"),
	TEST_LIST("list"),
	TEST_COMPLEXMAP("complexMap"),
	
	NULL("null");
	
	private static Map<String, EProtocol> lookup = null; 
	static 
	{
		lookup = new HashMap<String, EProtocol>();
		for( EProtocol e : EnumSet.allOf(EProtocol.class) ) 
		{
			String upperKey = e.string().trim().toUpperCase();
		    lookup.put( upperKey, e );
		}
	}
	
	private String value;
	
	private EProtocol( String value ) 
	{
		this.value = value;
	}
	
	public String string() 
	{
		return value;
	}
	
	public static EProtocol toEnum( String key )
	{
		if( key == null || key.isEmpty() )
			return EProtocol.NULL;
		
		String upperKey = key.trim().toUpperCase();
		return lookup.getOrDefault(upperKey, NULL);
	}
}

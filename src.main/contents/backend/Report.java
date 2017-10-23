package contents.backend;


import java.util.HashMap;
import java.util.Map;

/*
 * create table report (
   	sentenceId int unsigned not null default 0,
   	scriptId int unsigned default 0,
    userId int unsigned default 0,
   	state tinyint not null default 0,
    text_ko text not null default "",
    text_en text not null default "",
    created_dt datetime,
   	primary key(sentenceId)
    );
 */

public class Report {
	
	private Integer sentenceId = 0;
	private Integer scriptId = 0; 
	private Integer userId = 0;
	private Integer state = 0;
	private String textKo = null;
	private String textEn = null;
	private Long createdTime_UnixTimestamp = 0L;
	
	public static final int STATE_REPORTED = 0;
	public static final int STATE_MODIFILED = 1;
	
	// field for serialize/unserialize
	public static final String Field_SENTENCE_ID = "SENTENCE_ID";
	public static final String Field_SCRIPT_ID = "SCRIPT_ID";
	public static final String Field_USER_ID = "USER_ID";
	public static final String Field_STATE = "STATE";
	public static final String Field_TEXT_KO = "TEXT_KO";
	public static final String Field_TEXT_EN = "TEXT_EN";
	
	public static boolean isNull( Report report ){
		if( report == null )
			return true;
		
		if( report.sentenceId == 0 
				&& report.scriptId == 0
				&& report.state == 0
				&& report.userId == 0
				&& report.textKo == null
				&& report.textEn == null
				&& report.createdTime_UnixTimestamp == 0L)
			return true;
		
		return false;
	}
	
	public Integer getSentenceId() {
		return sentenceId;
	}

	public void setSentenceId(Integer sentenceId) {
		this.sentenceId = sentenceId;
	}

	public Integer getScriptId() {
		return scriptId;
	}

	public void setScriptId(Integer scriptId) {
		this.scriptId = scriptId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public String getTextKo() {
		return textKo;
	}

	public void setTextKo(String textKo) {
		this.textKo = textKo;
	}

	public String getTextEn() {
		return textEn;
	}

	public void setTextEn(String textEn) {
		this.textEn = textEn;
	}

	public static boolean checkState( Integer state ) {
        if( state == null )
            return false;
         
        if( state < STATE_REPORTED
        		|| state > STATE_MODIFILED)
        	return false;
        
        return true;
    }
	
	public Map<String, Object> serialize(){
		Map<String, Object> map = new HashMap<>();
		map.put(Field_SENTENCE_ID, this.sentenceId);
		map.put(Field_SCRIPT_ID, this.scriptId);
		map.put(Field_USER_ID, this.userId);
		map.put(Field_STATE, this.state);
		map.put(Field_TEXT_KO, this.textKo);
		map.put(Field_TEXT_EN, this.textEn);
		return map;
	}
	
	public String toString() {
		return "sentenceId("+sentenceId+"), "
				+ "scriptId("+scriptId+"), "
				+ "state("+state+"), "
				+ "userId("+userId+"), "
				+ "textKo("+textKo+"), "
				+ "textEn("+textEn+"), "
				+ "createdTime("+createdTime_UnixTimestamp+")";
	}
}

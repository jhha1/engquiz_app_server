package contents.backend;

import java.util.HashMap;
import java.util.Map;

import utils.StringHelper;

/*
 * create table script_sentence (
	id int unsigned not null auto_increment,
	scriptId int unsigned not null default 0,
	revision smallint not null default 0,
	text_ko varchar(256) default "",
	text_en varchar(256) default "",
	updated_dt datetime,
	primary key(id),
	FOREIGN KEY (`scriptId`) REFERENCES `script` (`id`)
	) default charset=utf8;
 */
public class Sentence {
	
	public Integer id;
	public Integer quizsetId;
	public Integer revision;
	public String textQuestion;
	public String textAnswer;
	public Long updatedTimeStamp;
	
	public static enum TYPE {NONE, KOREAN, ENLGISH};
	
	public final static String FIELD_SENTENCE_ID = "SENTENCE_ID";
	public final static String FIELD_QUIZSET_ID = "QUIZSET_ID";
	public final static String FIELD_SENTENCE_QUESTION = "SENTENCE_QUESTION";
	public final static String FIELD_SENTENCE_ANSWER = "SENTENCE_ANSWER";
	public final static String FIELD_REVISION = "REVISION";
	
	
	public final static Integer NULL_REVISION = 0;
	public final static Integer NULL_SENTENCEID = 0;
	public final static Long NULL_UPDATED_TIME = 0L;
	
	public final static Integer DELETED_SENTENCE_REVIISON = -999; // 삭제된 문장은 revision == -999 
	

	public Sentence(){
		id = NULL_SENTENCEID;
		quizsetId = 0;
		revision = NULL_REVISION;
		textQuestion = StringHelper.EMPTY_STRING;
		textAnswer = StringHelper.EMPTY_STRING;
		updatedTimeStamp = NULL_UPDATED_TIME;
	}
	public Sentence(Integer sentenceId, Integer scriptId, 
			Integer revision, String textQ, String textA,
			Long updatedTimeStamp){
		this.id = sentenceId;
		this.quizsetId = scriptId;
		this.revision = revision;
		this.textQuestion = textQ;
		this.textAnswer = textA;
		this.updatedTimeStamp = updatedTimeStamp;
	}
	
	public static boolean isNull( Sentence sentence ){
		if( sentence == null )
			return true;
		
		if( sentence.id == 0 
				&& sentence.quizsetId == 0
				&& sentence.revision == 0
				&& sentence.textQuestion == null
				&& sentence.textAnswer == null)
			return true;
		
		return false;
	}
	
	public Integer getSentenceId() {
		return id;
	}
	public void setSentenceId(Integer id) {
		this.id = id;
	}
	public Integer getQuizsetId() {
		return quizsetId;
	}
	public void setQuizsetId(Integer id) {
		this.quizsetId = id;
	}
	public Integer getRevision() {
		return revision;
	}
	public void setRevision(Integer revision) {
		this.revision = revision;
	}
	public String getTextQuestion() {
		return textQuestion;
	}
	public void setTextQuestion(String textQ) {
		this.textQuestion = textQ;
	}
	public String getTextAnswer() {
		return textAnswer;
	}
	public void setTextEn(String textA) {
		this.textAnswer = textA;
	}

	
    public Boolean hasKorean() {
    	return (textQuestion.length() > 0)? true: false; 
    }
    
    public Boolean hasEnglish() {
    	return (textAnswer.length() > 0)? true: false; 
    }
    
    public static boolean checkSentenceId( Integer sentenceId ){
    	if(sentenceId == null || sentenceId <= 0){
    		return false;
    	}
    	return true;
    }
    
    public static boolean checkText( String text ){
    	if(StringHelper.isNull(text)){
    		return false;
    	}
    	return true;
    }
  

    public Map<String, Object> serialize(){
		Map<String, Object> map = new HashMap<>();
		map.put(FIELD_SENTENCE_ID, this.id);
		map.put(FIELD_QUIZSET_ID, this.quizsetId);
		map.put(FIELD_SENTENCE_QUESTION, this.textQuestion);
		map.put(FIELD_SENTENCE_ANSWER, this.textAnswer);
		return map;
	}
	
	public String toString() {
		return "id("+id+"), "
				+ "quizsetId("+quizsetId+"), "
				+ "revision("+revision+"), "
				+ "textQuestion("+textQuestion+"), "
				+ "textAnswer("+textAnswer+")";
	}
}

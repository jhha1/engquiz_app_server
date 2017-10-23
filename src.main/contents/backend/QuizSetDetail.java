package contents.backend;


import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class QuizSetDetail extends QuizSet
{
	public List<Sentence> sentences = new LinkedList<Sentence>();
	
	public final static String Field_SENTENCES = "SENTENCES";
	
	public QuizSetDetail(){}
	public QuizSetDetail( Integer quizsetID, String quizsetTitle, List<Sentence> sentences ){	
		super(quizsetID, quizsetTitle);
    	this.sentences = sentences;
	}
	
	public static boolean isNull( QuizSetDetail quizset ){
		if( quizset == null )
			return true;
		
		QuizSet parent = new QuizSet(quizset.quizsetId, quizset.title);
		if(QuizSet.isNull(parent)){
			return true;
		}
		
		if(quizset.sentences.isEmpty())
			return true;
		
		return false;
	}
	
	
	public Map<String, Object> serialize(){
		Map<String, Object> map = super.serialize();

		List<Map<String, Object>> serializedSentences = new LinkedList<>();
		for(Sentence s : sentences){
			serializedSentences.add(s.serialize());
		}
		map.put(Field_SENTENCES, serializedSentences);
		return map;
	}
	
	public String toString()  
	{
		StringBuffer text = new StringBuffer("sentences:");
		for(contents.backend.Sentence unit : sentences) {
			text.append("{ko:"+unit.textQuestion+ ", en:" + unit.textAnswer+"}");
		}
		return super.toString() + ", " + text.toString();
	}
}

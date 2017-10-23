package contents.backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import data.db.DBQueries;
import data.db.DBQueries.DBResult;
import exception.contents.ContentsRoleException;
import net.protocol.EResultCode;
import utils.Parsor;
import utils.StringHelper;

public class QuizSetManager {
	final private Logger log = Logger.getLogger(QuizSetManager.class);

	private static QuizSetManager instance = new QuizSetManager();
	private Map<Integer, QuizSetDetail> mQuizsets = new HashMap<Integer, QuizSetDetail>();
	private Map<String, Integer> quizsetNameMap = new HashMap<String, Integer>();

	private QuizSetManager() {
	}

	public static QuizSetManager getInstance() {
		return instance;
	}

	public void initailize() {
		initQuizsetMap();
		printQuizsetMap();
	}

	@SuppressWarnings("unchecked")
	private void initQuizsetMap() {
		final DBQueries db = DBQueries.getInstance();
		DBResult dbResult = db.selectQuizsetSummaryAll();
		if (dbResult == null || dbResult.noSelected()) {
			log.error("Failed initScriptMap! DBERR or noSelected");
			return;
		}
		List<QuizSet> quizsets = (List<QuizSet>) dbResult.getResultObject();

		for (QuizSet quizset : quizsets) {
			if (QuizSet.isNull(quizset)) {
				log.error("ScriptObject is null. but Ignore and Init other scripts");
				continue;
			}

			dbResult = db.selectSentencesByQuizsetId(quizset.quizsetId);
			if (dbResult == null) {
				log.error("DBResult is null. but Ignore and Init other scripts");
				continue;
			}
			if (dbResult.noSelected()) {
				log.error("NoSelected. selectSentencesByScriptId. " + "but Ignore and Init other scripts");
				continue;
			}

			QuizSetDetail scriptDetail = new QuizSetDetail();
			scriptDetail.quizsetId = quizset.quizsetId;
			scriptDetail.title = quizset.title;
			scriptDetail.createdDateTime = quizset.createdDateTime;
			scriptDetail.sentences = (List<Sentence>) dbResult.getResultObject();

			if (false == mQuizsets.containsKey(quizset.title)) {
				mQuizsets.put(quizset.quizsetId, scriptDetail);
				quizsetNameMap.put(quizset.title, quizset.quizsetId);
			}
		}
	}

	public QuizSetDetail getParsedScript(String scriptTitle) {
		int scriptIndex = getScriptIdByTitle(scriptTitle);
		if (scriptIndex < 0)
			return null;
		return getParsedScript(scriptIndex);
	}

	public QuizSetDetail getParsedScript(Integer scriptIndex) {
		return mQuizsets.get(scriptIndex);
	}

	public Boolean hasParsedScript(String scriptTitle) {

		int scriptIndex = getScriptIdByTitle(scriptTitle);
		if (scriptIndex < 0)
			return false;
		return hasParsedScript(scriptIndex);
	}

	public Boolean hasParsedScript(Integer scriptIndex) {
		return mQuizsets.containsKey(scriptIndex);
	}

	public Integer getScriptIdByTitle(String scriptTitle) {
		if (false == quizsetNameMap.containsKey(scriptTitle))
			return -1;
		return quizsetNameMap.get(scriptTitle);
	}

	public String getTitleByScriptId(Integer scriptIndex) {
		if (false == mQuizsets.containsKey(scriptIndex))
			return "";
		QuizSet s = mQuizsets.get(scriptIndex);
		return s.title;
	}

	public QuizSetDetail parsePDFScript(String scriptTitle, byte[] pdf) throws ContentsRoleException {
		scriptTitle = Parsor.parseTitle(scriptTitle);

		if (hasParsedScript(scriptTitle)) {
			return getParsedScript(scriptTitle);
		}

		log.debug("New Script Detacted.. Parsing Process Start... title[" +scriptTitle+ "]");
		
		List<Sentence> parsedSentences = new Parsor().parsePDF(pdf);
		if (parsedSentences == null || parsedSentences.isEmpty()) {
			log.error("parsedSentences is null. scriptTitle:" + scriptTitle);
			throw new ContentsRoleException(EResultCode.NULL_VALUE);
		}

		boolean bNoHas = checkSentences_no_has_korean_or_enlgish(parsedSentences);
		if (bNoHas) {
			log.error("checkSentences_no_has_korean_or_enlgish. scriptTitle:" + scriptTitle);
			throw new ContentsRoleException(EResultCode.SCRIPT__NO_HAS_KR_OR_EN);
		}

		QuizSetDetail quizset = createAndAddScript(scriptTitle, parsedSentences);
		if (quizset == null) {
			log.error("failed save to DB and create quizset. Title:" + scriptTitle);
			throw new ContentsRoleException(EResultCode.NULL_VALUE);
		}
		return quizset;
	}

	public QuizSet parseDocxScript(String scriptTitle, byte[] docx) {
		if (hasParsedScript(scriptTitle)) {
			return getParsedScript(scriptTitle);
		}

		File file = new File(
				"F:\\Users\\jhha\\Projects\\git\\engquiz\\engquiz_server\\resource\\2017 Basic Grammar Drill (no.115 ~ 122).docx");
		FileInputStream fis;
		try {
			fis = new FileInputStream(file.getAbsolutePath());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		List<Sentence> parsedSentences = new Parsor().parseDocx(fis);
		QuizSet newScript = createAndAddScript(scriptTitle, parsedSentences);
		return newScript;
	}

	// 영어문장만 있거나, 한글문장만 있는 스크립트인지 걸러내기.
	public boolean checkSentences_no_has_korean_or_enlgish(List<Sentence> parsedSentences) {
		int enCount = 0;
		int koCount = 0;
		for (Sentence sentence : parsedSentences) {
			if (!sentence.textAnswer.isEmpty())
				++enCount;
			if (!sentence.textQuestion.isEmpty())
				++koCount;
		}

		int minCount = 5;
		if (enCount < minCount) {
			return true;
		}
		if (koCount < minCount) {
			return true;
		}
		return false;
	}

	private QuizSetDetail createAndAddScript(String scriptTitle, List<Sentence> sentences) 
	{
		// add Script to DB
		final DBQueries db = DBQueries.getInstance();
		DBResult dbResult = db.insertQuizSetAndSentencesToSystem(scriptTitle, sentences);
		if (dbResult == null) {
			log.error("New Parsed Script - Failed add script into DB. " + "DBError. " + "scriptTitle:" + scriptTitle + ",scriptTitle:"
					+ scriptTitle);
			return null;
		}
		Integer scriptId = (Integer) dbResult.getResultObject();

		log.debug("New Parsed Script - DB Inserted. QuizsetID["+scriptId+"], title[" +scriptTitle+ "]");
		
		// add Script to Memory
		QuizSetDetail newScript = makeNewScript(scriptId, scriptTitle);
		if (QuizSet.isNull(newScript)) {
			log.error("Failed add script into map. " + "newScript is null. " + "id:" + scriptId + ",scriptTitle:"
					+ scriptTitle);
			return null; 
		}
		if (newScript.sentences == null || newScript.sentences.isEmpty()) {
			log.error("Failed add script into map. " + "sentences are null or empty. "
					+ "No allowed it when 'Add Script'. " + "id:" + scriptId + ",scriptTitle:" + scriptTitle);
			return null;
		}

		if (false == mQuizsets.containsKey(scriptId)) {
			mQuizsets.put(scriptId, newScript);
			quizsetNameMap.put(newScript.title, scriptId);
			
			log.debug("New Parsed Script - M/M Inserted. QuizsetID["+scriptId+"], QuizsetObjectID["+newScript+"], title[" +scriptTitle+ "]");
			
		} else {
			log.error("Failed add script into map. id:" + scriptId + ",scriptTitle:" + scriptTitle);
			return null;
		}

		// writeParsedScriptToFile( scriptTitle, newScript.toString() );
		alarmNewScriptAdded(scriptTitle, newScript);

		return newScript;
	}

	@SuppressWarnings("unchecked")
	private QuizSetDetail makeNewScript(Integer scriptId, String scriptTitle) {
		QuizSetDetail newScript = new QuizSetDetail();
		newScript.quizsetId = scriptId;
		newScript.title = scriptTitle;

		// load sentences
		final DBQueries db = DBQueries.getInstance();
		DBResult dbResult = db.selectSentencesByQuizsetId(scriptId);
		if (dbResult == null) {
			log.error("Failed makeNewScript(). DB Error");
			return null;
		}
		if (dbResult.noSelected()) {
			// 유저가 퀴즈폴더내의 스크립트를 모두 삭제했을 경우, no selected 가능.
			// 'New..'버튼은 client에서 만들므로.
			newScript.sentences = new LinkedList<Sentence>();
		} else {
			newScript.sentences = (List<Sentence>) dbResult.getResultObject();
		}

		return newScript;
	}

	private Boolean alarmNewScriptAdded(String scriptTitle, QuizSet script) {
		// TODO 결과 알람
		return true;
	}
	
	public Sentence updateSentence(Integer scriptId, Integer sentenceId, String ko, String en){
		if( QuizSet.checkQuizSetID(scriptId) == false ){
			log.error("Failed updateSentence(). invalid scriptId:"+scriptId);
			return null;
		}
		if( Sentence.checkSentenceId(sentenceId) == false ){
			log.error("Failed updateSentence(). invalid sentenceId:"+sentenceId);
			return null;
		}
		if( StringHelper.isNull(ko) 
				||  StringHelper.isNull(en)){
			log.error("Failed updateSentence(). invalid sentence ko:"+ko +",en:"+en);
			return null;
		}
		
		boolean bOK = updateSentenceDB(sentenceId, ko, en);
		if( ! bOK ){
			return null;
		}
		
		return updateSentenceMemory(scriptId, sentenceId, ko, en);
	}
	
	private boolean updateSentenceDB(Integer sentenceId, String textKo, String textEn) 
	{
		final DBQueries db = DBQueries.getInstance();
		boolean bOK = db.updateSentence(sentenceId, textKo, textEn);
		if( !bOK ){
			log.error("Failed updateSentenceDB. DBError. "
					+ "sentenceId("+sentenceId+"), textKo("+textKo+"), textEn("+textEn+")");
			return false;
		}
		return true;
	}
	
	private Sentence updateSentenceMemory(Integer scriptId, Integer sentenceId, String textKo, String textEn) 
	{
		QuizSetDetail script = getParsedScript(scriptId);
		if( QuizSetDetail.isNull(script)){
			log.error("Failed updateSentenceMemory(). script is nuill. scriptId:"+scriptId);
			return null;
		}
		
		List<Sentence> sentences = script.sentences;
		if( sentences == null || sentences.isEmpty() ){
			log.error("Failed updateSentenceMemory(). sentence list is null. scriptId:"+scriptId);
			return null;
		}
		
		for( Sentence sentence: sentences){
			if( Sentence.isNull(sentence) ){
				log.error("sentence is null. but ignore it and continue");
				continue;
			}
			
			if( sentenceId.equals(sentence.id) ){ // (sentence.id == sentenceId  로 하면 일치 안함.
				sentence.textQuestion = textKo;
				sentence.textAnswer = textEn;
				return sentence;
			}
		}
		
		return null;
	}
	
	public Sentence addSentence( Integer scriptId, String textKo, String textEn ) {
		if( QuizSet.checkQuizSetID(scriptId) == false ){
			log.error("Failed deleteSentence(). invalid scriptId:"+scriptId);
			return null;
		}
		if( StringHelper.isNull(textKo) 
				||  StringHelper.isNull(textEn)){
			log.error("Failed updateSentence(). invalid sentence ko:"+textKo +",en:"+textEn);
			return null;
		}
		
		// DB
		final DBQueries db = DBQueries.getInstance();
		int lastInsertId = db.insertSentenceToSystem(scriptId, textKo, textEn);
		if (lastInsertId < 0) {
			log.error("Failed insertSentence to sentenceTable(). DB Error");
			return null;
		}
		
		// Memory
		List<Sentence> sentences = getSentencesInScriptMap(scriptId);
		if( sentences == null ){
			log.error("failed add sentence. sentencelist is null. scriptId:"+scriptId);
			return null;
		}
		// sentences가 객체 포인터라서 mScriptMap에 적용됨
		Sentence sentence = new Sentence();
		sentence.id = lastInsertId;
		sentence.revision = 0;
		sentence.quizsetId = scriptId;
		sentence.textAnswer = textEn;
		sentence.textQuestion = textKo;
		sentence.updatedTimeStamp = System.currentTimeMillis();
		sentences.add(sentence);
		
		return sentence;
	}
	
	public boolean deleteSentence(Integer scriptId, Integer sentenceId){
		if( QuizSet.checkQuizSetID(scriptId) == false ){
			log.error("Failed deleteSentence(). invalid scriptId:"+scriptId);
			return false;
		}
		if( Sentence.checkSentenceId(sentenceId) == false ){
			log.error("Failed deleteSentence(). invalid sentenceId:"+sentenceId);
			return false;
		}
		
		// update DB pretend deleted 
		final DBQueries db = DBQueries.getInstance();
		boolean bOk = db.deleteSentence(sentenceId);
		if ( ! bOk ) {
			log.error("Failed deleteSentence From sentenceTable(). DB Error");
			return false;
		}
		
		// delete from Memory
		List<Sentence> sentences = getSentencesInScriptMap(scriptId);
		if( sentences == null ){
			// 애초에 delete가 목적이니, map에 없어도 상관없음
			log.warn("script.sentences is null. but ignore it. scriptId:"+scriptId);
			return true;
		}
		
		Sentence targetSentenceObj = null;
		for(Sentence sentence : sentences){
			boolean bSameSentence =  sentence.getSentenceId().equals(sentenceId);
			if(bSameSentence){
				targetSentenceObj = sentence;
				break;
			}
		}
		
		if( targetSentenceObj == null ){
			// 애초에 delete가 목적이니, map에 없어도 상관없음
			log.warn("script.sentences is null. but ignore it. scriptId:"+scriptId);
			return true;	
		}
		
		// delete. sentences가 객체 포인터라서 mScriptMap에 적용됨
		sentences.remove(targetSentenceObj); 
		return true;
		
	}
	
	public List<Sentence> getSentencesInScriptMap(Integer scriptId){
		QuizSetDetail script = getParsedScript(scriptId);
		if( QuizSetDetail.isNull(script) ){
			log.error("script is null. scriptId:"+scriptId);
			return null;
		}
		
		return script.sentences;
	}
	
	
	public void printQuizsetMap() {
		System.out.println("---------- Print QuizMap Start -----------");
		for (Entry<Integer, QuizSetDetail> entry : mQuizsets.entrySet()) {
			Integer index = entry.getKey();
			QuizSetDetail script = entry.getValue();
			System.out.println("[[key  ]] " + index);
			System.out.println("[[value]] " + script.toString());
		}
		System.out.println("---------- Print QuizMap Finish -----------");
	}

	public void printScriptMapSummay() {
		System.out.println("---------- Print QuizMap Summary Start -----------");
		for (Entry<Integer, QuizSetDetail> entry : mQuizsets.entrySet()) {
			Integer index = entry.getKey();
			QuizSetDetail script = entry.getValue();
			StringBuilder builder = new StringBuilder();
			builder.append("[[key  ]] " + index);
			builder.append(", [[value]]");
			if (script != null) {
				builder.append(" scriptid:" + script.quizsetId);
				builder.append(", sentenceCount:" + ((script.sentences != null) ? script.sentences.size() : null));
			}
			System.out.println(builder);
		}
		System.out.println("---------- Print QuizMap Summary Finish -----------");
	}
}

/*
 * A[] quiz;
 * 
 * for(i = j; i <= k; ++i){ Array.concat(quiz, generateQuiz(n[i].filename)); }
 * 
 * }
 */
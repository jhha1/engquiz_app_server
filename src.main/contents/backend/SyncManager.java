package contents.backend;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import data.db.DBQueries;
import data.db.DBQueries.DBResult;
import net.protocol.EResultCode;
import net.protocol.ObjectBundle;

public class SyncManager {
	final private Logger log = Logger.getLogger( SyncManager.class );
	
	private static SyncManager instance = new SyncManager();
	private SyncManager() {}
	
	public static SyncManager getInstance() {
		return instance;
	}
	
	public final static String FIELD_SYNC__ACTION = "SYNC__ACTION";
	public static enum SYNC_ACTION {NONE, ADD, DEL, UPDATE};
	
	public List checkSyncNeeded( Integer userId ) 
	{
		if(!User.checkUserID(userId)){
			log.error("invalid userId. userId(" + userId +")");
			return null;
		}
		List<ObjectBundle> syncNeedSentences = getSentencesForSync( userId );
		if( syncNeedSentences == null ) {
			return Collections.EMPTY_LIST;
		}
		
		List< Map<String,Integer> > sentenceList = new LinkedList<>();
		for( ObjectBundle sentence : syncNeedSentences ){
			Map<String,Integer> oldRevisionSentence = new HashMap<>();
	    	oldRevisionSentence.put("scriptId", sentence.getInt(Sentence.FIELD_QUIZSET_ID));
	    	oldRevisionSentence.put("sentenceId", sentence.getInt(Sentence.FIELD_SENTENCE_ID));
	    	
			sentenceList.add(oldRevisionSentence);
		}
		
		return sentenceList;
	}
	
	public List<ObjectBundle> getSentencesForSync( Integer userId ) 
	{
		if(!User.checkUserID(userId)){
			log.error("invalid userId. userId(" + userId +")");
			return null;
		}
		
		final DBQueries db = DBQueries.getInstance();
		DBResult dbResult = db.selectSentencesForSync( userId );
		if( dbResult == null ){
			return null;
		}
		@SuppressWarnings("unchecked")
		List<ObjectBundle> syncNeedsentences = (List<ObjectBundle>)dbResult.getResultObject(); 
		return syncNeedsentences;
	}
	
	public EResultCode sync( Integer userId, List<ObjectBundle> sentencesForSync ) {
		final DBQueries db = DBQueries.getInstance();
		
		for( ObjectBundle sentence : sentencesForSync ){
			
			int syncAction = sentence.getInt(SyncManager.FIELD_SYNC__ACTION);
			int sentenceID = sentence.getInt(Sentence.FIELD_SENTENCE_ID);
		
			// del sentence
			if( syncAction == SYNC_ACTION.DEL.ordinal() ) {
				db.deleteSentenceFromAllUser( sentenceID );
				continue;
			}
			
			// add sentence
			if( syncAction == SYNC_ACTION.ADD.ordinal() ) {
				int quizsetId = sentence.getInt(Sentence.FIELD_QUIZSET_ID);
				int revision =  sentence.getInt(Sentence.FIELD_REVISION);
				db.insertSentenceToUser( userId, quizsetId, sentenceID, revision );
				continue;
			}
			
			// update sentence
			if( syncAction == SYNC_ACTION.UPDATE.ordinal() ) {
				boolean bOK = db.updataUserSentencesRevision( userId, sentencesForSync );
				if( !bOK ){
					log.error("Failed Sync. DB Error. userId(" + userId +")");
					return EResultCode.DB_ERR;
				}
				continue;
			}
		}
		
		return EResultCode.SUCCESS;
	}
	
	/* Sync 실패한 것만 revision을 한단계 낮춘다.
	 	so, 다음에 다시 sync 받을 수 있다.
		클라에서 넘어오는 결과를 믿지 않는 구조로 이렇게 만듬.
	*/
	public EResultCode updateSyncResult( Integer userId, List<Integer> syncFailedResults ){
		
		if( !User.checkUserID(userId)){
			log.error("invalid userId. userId(" + userId +")");
			return EResultCode.INVALID_USERID;
		}
		if( syncFailedResults == null || syncFailedResults.isEmpty() ){
			log.error("syncFailedResults is null. userId:" + userId);
			return EResultCode.INVALID_ARGUMENT;
		}
		
		List<ObjectBundle> failedSentences = new LinkedList<>();
		for(Integer sentenceId : syncFailedResults)
		{
			ObjectBundle bundle = new ObjectBundle();
			// db update용 data 만들기 
			// 아래 두개 값만 사용해서, 두개만 셋팅.
			bundle.setInt(Sentence.FIELD_SENTENCE_ID, sentenceId);
			// 다음에 sync 다시 할수 있도록만 하면 되므로, 가장 낮은 값으로 셋팅.
			bundle.setInt(Sentence.FIELD_REVISION, 0);
			failedSentences.add(bundle);
		}
		
		final DBQueries db = DBQueries.getInstance();
		boolean bOK = db.updataUserSentencesRevision( userId, failedSentences ); 
		if( !bOK ){
			log.error("DB Error. userId(" + userId +")");
			return EResultCode.DB_ERR;
		}
		return EResultCode.SUCCESS;
	}
}

package data.db;


import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mysql.jdbc.Statement;


import contents.backend.Report;
import contents.backend.QuizSet;
import contents.backend.QuizSetDetail;
import contents.backend.Sentence;
import contents.backend.SyncManager;
import contents.backend.User;
import exception.system.DBException;
import net.protocol.EProtocol;
import net.protocol.ObjectBundle;
import utils.StringHelper;

public class DBQueries {
	
	final private Logger log = Logger.getLogger( DBQueries.class );
	private static DBQueries instance = new DBQueries();
	private DBQueries(){}
	
	public static DBQueries getInstance() {
		return instance;
	}

	
	public class DBResult {
		boolean noSelected = false;
		Object resultObject = null;
		
		public boolean noSelected(){
			return noSelected;
		}
		public Object getResultObject(){
			return resultObject;
		}
		
	}
	
	/*
	 *  User
	 */
	public DBResult selectUserById( Integer userId ) 
	{	
		if( ! User.checkUserID(userId)) {
			log.error("invalid argment. userId("+userId+")");
			return null;
		}
		
		String query = "SELECT id, name, "
							+ "unix_timestamp(signin_dt) as signin_long, "
							+ "is_admin"
						+ " FROM user "
		 				+ " WHERE id = ?";
 
		DBConnectionHelper db = null;
		try {
			db = new DBConnectionHelper( query );
		    db.setInt(1, userId);

		    ResultSet rs = db.executeQuery();

		    User user = new User();
		    while(rs.next()){     
		    	user.userID = rs.getInt("id");
		    	user.userName = rs.getString("name");
                user.signin_dt = rs.getLong("signin_long");
                user.setAdmin(rs.getInt("is_admin"));
                System.out.println( user.toString() + "\t");
		    }
		    DBResult dbResult = new DBResult();
		    dbResult.noSelected = User.isNull(user);
		    dbResult.resultObject = user;
		    return dbResult;
		    
		} catch (Exception e) {
			log.error("query["+query+"], args[userId:"+userId+"]");
			e.printStackTrace();
		} finally {
			db.close();
		} 
		return null; 
	}
	
	public DBResult selectUserByName( String userName ) 
	{
		if( ! User.checkUserName(userName)) {
			log.error("invalid argment. userName("+userName+")");
			return null;
		}
		
		String query = "SELECT id, name, "
				+ "unix_timestamp(signin_dt) as signin_long, "
				+ "is_admin"
				+ " FROM user"
 				+ " WHERE name = ?";

		DBConnectionHelper db = null;
		try {
			db = new DBConnectionHelper( query );
		    db.setString(1, userName);
		    ResultSet rs = db.executeQuery(); 
		    
		    User user = new User();
		    while(rs.next()){     
		    	user.userID = rs.getInt("id");
		    	user.userName = rs.getString("name");
		        user.signin_dt = rs.getLong("signin_long");
		        user.setAdmin(rs.getInt("is_admin"));
		        System.out.println( user.toString() + "\t");
		    }
		    DBResult dbResult = new DBResult();
		    dbResult.noSelected = User.isNull(user);
		    dbResult.resultObject = user;
		    return dbResult;
		    
		} catch (Exception e) {
			log.error("query["+query+"], args[userName:"+userName+"]");
			e.printStackTrace();
		} finally {
			db.close();
		} 
		return null; 
	}
	
	/*
	 * 
	 CREATE TABLE user ( 
		  id int unsigned NOT NULL AUTO_INCREMENT,
		  name varchar(255) NOT NULL,
		  signin_dt datetime,
		  login_dt datetime,
		  PRIMARY KEY (id)
		);
	 */
	public boolean insertUserAccount( String userName )
	{
		if( StringHelper.isNull(userName) ) {
			log.error("Invalid Agument. userName:"+userName);
			return false;
		}
		
		String query = "insert into user (name, signin_dt, is_admin) "
		 				+ "values(?, now(), ?)";
		 
		DBConnectionHelper db = null;
		try {
			db = new DBConnectionHelper( query );
			db.setString(1, userName);
			db.setInt(2, User.USERTYPE_NORMAL);
			db.executeUpdate();
			return true;
			
        } catch (Exception e) {
        	log.error("querys["+query+"], "
        			+ "userName["+userName+"]");
        	e.printStackTrace();
        } finally {
        	db.close();
        } 
		return false;  
	}

	
	/*
	 *  QuizSet
	 */
	public DBResult selectQuizsetIDByTitle( String scriptTitle ) 
	{
		if( StringHelper.isNull(scriptTitle)){
			log.error("Invalid scriptTitle("+scriptTitle+")");
			return null;
		}
		
		String query = "SELECT id FROM all_quizset WHERE title = ?";
		
		Integer scriptId = 0;
		DBConnectionHelper db = null;
		try {
			db = new DBConnectionHelper( query );
			db.setString(1, scriptTitle);
		    ResultSet rs = db.executeQuery(); 
		    
		    while(rs.next()) {    
		    	scriptId = rs.getInt("id");
		    }
		    
		    DBResult dbResult = new DBResult();
		    dbResult.noSelected = (scriptId == 0);
		    dbResult.resultObject = scriptId;
		    return dbResult;
		    
		} catch (Exception e) {
			log.error("query["+query+"], args[scriptTitle:"+scriptTitle+"]");
			e.printStackTrace();
		} finally {
			db.close();
		} 
		return null;
	}

	public DBResult selectQuizSetsOrderByTitleAsc( List<Integer> scriptIds )
	{
		if( scriptIds == null || scriptIds.isEmpty() ) {
			log.error("Null Agument - scriptIds.");
			return null;
		}
		
		StringBuilder query = new StringBuilder("SELECT id, title "
												+ " FROM all_quizset"
												+ " WHERE ");
		for(int i=0; i<scriptIds.size()-1; ++i){
			query.append("id = ? OR ");
		}
		query.append("id = ?");
		query.append("	ORDER BY title ASC");
		
		List<QuizSet> scripts = new ArrayList<>();
		DBConnectionHelper db = null;
		try {
			db = new DBConnectionHelper( query.toString() );
			for(int i=0; i<scriptIds.size(); ++i){
				 db.setInt(i+1, scriptIds.get(i));
			}
		    ResultSet rs = db.executeQuery(); 
		    
		    while(rs.next()){   
		    	QuizSet script = new QuizSet();
		    	script.quizsetId = rs.getInt("id");
		    	script.title = rs.getString("title");
		    	scripts.add(script);
		    }
		    DBResult dbResult = new DBResult();
		    dbResult.noSelected = scripts.isEmpty();
		    dbResult.resultObject = scripts;
		    return dbResult;
		    
		} catch (Exception e) {
			log.error("query["+query+"], args[scriptIds:"+scriptIds.toString()+"]");
			e.printStackTrace();
		} finally {
			db.close();
		} 
		return null; 
	}
	
	public DBResult selectUserQuizset( Integer userId, Integer scriptId ) {
		if( ! User.checkUserID(userId)) {
			log.error("invalid argment. userId("+userId+")");
			return null;
		}
		if( ! QuizSet.checkQuizSetID(scriptId)) {
			log.error("invalid argment. scriptId("+scriptId+")");
			return null;
		}
		
		String query = "SELECT sentenceId, revision"
					+ " FROM user_quizset"
	 				+ " WHERE userid = ? and scriptId = ?";

		DBConnectionHelper db = null;
		try {
			db = new DBConnectionHelper( query );
		    db.setInt(1, userId);
			db.setInt(2, scriptId);
		    ResultSet rs = db.executeQuery(); 
		    
		    List<Sentence> userScript = new LinkedList<>();
		    while(rs.next()){     
		    	Sentence userSentence = new Sentence();
		    	userSentence.quizsetId = scriptId;
		    	userSentence.id = rs.getInt("sentenceId");
		    	userSentence.revision = rs.getInt("revision");
		    	userScript.add(userSentence);
		    }
		    
		    log.debug("DB userScript. "
		    		+ "SeneteceCount["+ userScript.size() +"], "
		    		+ "query["+query+"], "
		    		+ "args[userId:"+userId+", scriptId:"+scriptId+"]");
		    
		    DBResult dbResult = new DBResult();
		    dbResult.noSelected = userScript.isEmpty();
		    dbResult.resultObject = userScript;
		    return dbResult;
		} catch (Exception e) {
			log.error("query["+query+"], args[userId:"+userId+", scriptId:"+scriptId+"]");
			e.printStackTrace();
		} finally {
			db.close();
		} 
		return null;
	}
	
	public boolean insertSentencesToUser( Integer userId, QuizSetDetail script ){
		if( ! User.checkUserID(userId)) {
			log.error("invalid argment. userId("+userId+")");
			return false;
		}
		if( QuizSet.isNull(script) ) {
			log.error("null argment - script. userId("+userId+")");
			return false;
		}
		
		String query = "insert into user_quizset (userId, scriptId, sentenceId, revision) "
					+ "values(?, ?, ?, ?)";
		
		StringBuffer logString = new StringBuffer(",userId:"+userId+", sentenceId,revision/");
		
		DBConnectionHelper db = null;
		try{
			db = new DBConnectionHelper( query );
			for(Sentence s : script.sentences){
				if( Sentence.isNull(s) ){
					log.error("Sentence is null. "
							+ "but Ignore it and Continue Inserting a next sentence into DB.."
							+ " scriptId:"+script.quizsetId+", userId:"+userId);
					continue;
				}
				
				db.setInt(1, userId);
				db.setInt(2, script.quizsetId);
				db.setInt(3, s.id);
				db.setInt(4, s.revision);
				db.executeUpdate();
				
				logString.append(s.id+","+s.revision+"/");
			}
			log.debug("user_quizset Inserted.. " +logString);
			
			return true;
			
        } catch (Exception e) {
        	log.error("querys["+query+"], "
        			+ "args["+logString+"]");
        	e.printStackTrace();
        } finally {
        	db.close();
        } 
		return false;
	}
	
	public boolean insertSentenceToUser( Integer userId, Integer quizsetId, Integer sentenceId, Integer revision ){
		if( ! User.checkUserID(userId)) {
			log.error("invalid argment. userId("+userId+")");
			return false;
		}
		
		String query = "insert into user_quizset (userId, scriptId, sentenceId, revision) "
					+ "values(?, ?, ?, ?)";
		
		StringBuffer logString = new StringBuffer(",userId:"+userId+", sentenceId:"+sentenceId+",revision:"+revision);
		
		DBConnectionHelper db = null;
		try{
			db = new DBConnectionHelper( query );
			
			db.setInt(1, userId);
			db.setInt(2, quizsetId);
			db.setInt(3, sentenceId);
			db.setInt(4, revision);
			db.executeUpdate();
			
			logString.append(sentenceId+","+revision+"/");
		
			log.debug("user_quizset Inserted.. " +logString);
			
			return true;
			
        } catch (Exception e) {
        	log.error("querys["+query+"], "
        			+ "args["+logString+"]");
        	e.printStackTrace();
        } finally {
        	db.close();
        } 
		return false;
	}
	
	public boolean deleteUserQuizset( Integer userId, Integer scriptId ){
		if( ! User.checkUserID(userId)) {
			log.error("invalid argment. userId("+userId+")");
			return false;
		}
		if( false == QuizSet.checkQuizSetID(scriptId)) {
			log.error("invalid argment - scriptId("+userId+")");
			return false;
		}
		
		String query = "delete from user_quizset "
						+ "WHERE userId = ? and scriptId = ?";
		
		StringBuffer logString = new StringBuffer(",userId:"+userId+", scriptId:"+scriptId);
		
		DBConnectionHelper db = null;
		try{
			db = new DBConnectionHelper( query );
			db.setInt(1, userId);
			db.setInt(2, scriptId);
			db.executeUpdate();
	
			log.debug("user_quizset Deleted.. " +logString);
			
			return true;
			
        } catch (Exception e) {
        	log.error("querys["+query+"], "
        			+ "args["+logString+"]");
        	e.printStackTrace();
        } finally {
        	db.close();
        } 
		return false;
	}
	
	public DBResult selectQuizsetSummaryAll()
	 {
		 String query = "SELECT id, title, "
					+ "unix_timestamp(created_dt) as created_long"
					+ " FROM all_quizset";

		DBConnectionHelper db = null;
		try {
			db = new DBConnectionHelper( query );
		    ResultSet rs = db.executeQuery(); 
		    
		    List<QuizSet> scripts = new LinkedList<>();
		    while(rs.next()) {    
		    	QuizSet script = new QuizSet();
		    	
		    	script.quizsetId = rs.getInt("id");
		    	script.title = rs.getString("title");
		    	script.createdDateTime = rs.getLong("created_long");
		    	scripts.add(script);
		    }
		    
		    DBResult dbResult = new DBResult();
		    dbResult.noSelected = scripts.isEmpty();
		    dbResult.resultObject = scripts;
		    return dbResult;
		    
		} catch (Exception e) {
       	log.error("querys["+query+"] ");
       	e.printStackTrace();
       } finally {
       	db.close();
       } 
		return null;
	 }

	/*
	 *  Senetce
	 */
	
	public DBResult selectSentenceBySentenceId( Integer sentenceId ) 
	{
		if( ! Sentence.checkSentenceId(sentenceId)){
			log.error("Invalid sentenceId("+sentenceId+")");
			return null;
		}
	
		String query = "SELECT id, scriptId, revision, "
				+ "text_ko, text_en, "
				+ "unix_timestamp(updated_dt) as updated_long"
				+ " FROM all_sentence"
 				+ " WHERE id = ?";

		DBConnectionHelper db = null;
		try {
			db = new DBConnectionHelper( query );
		    db.setInt(1, sentenceId);
		    ResultSet rs = db.executeQuery(); 
		    
		    Sentence sentence = new Sentence();
		    while(rs.next()) {    	
		    	sentence.quizsetId = rs.getInt("scriptId");
		    	sentence.id = rs.getInt("id");
		    	sentence.revision = rs.getInt("revision");
		    	sentence.textQuestion = rs.getString("text_ko");
		    	sentence.textAnswer = rs.getString("text_en");
		    	sentence.updatedTimeStamp = rs.getLong("updated_long");
		    }
		    
		    DBResult dbResult = new DBResult();
		    dbResult.noSelected = Sentence.isNull(sentence);
		    dbResult.resultObject = sentence;
		    return dbResult;
		    
		} catch (Exception e) {
			log.error("query["+query+"], args[sentenceId:"+sentenceId+"]");
			e.printStackTrace();
		} finally {
			db.close();
		} 
		return null;
	}
	
	public DBResult selectSentencesByQuizsetId( Integer scriptId ) 
	{
		if( ! QuizSet.checkQuizSetID(scriptId)){
			log.error("Invalid scriptId("+scriptId+")");
			return null;
		}
		
		String query = "SELECT id, revision, "
				+ "text_ko, text_en, "
				+ "unix_timestamp(updated_dt) as updated_long"
				+ " FROM all_sentence"
 				+ " WHERE scriptId = ? AND revision != ?";

		DBConnectionHelper db = null;
		try {
			db = new DBConnectionHelper( query );
		    db.setInt(1, scriptId);
		    db.setInt(2, Sentence.DELETED_SENTENCE_REVIISON);
		    
		    ResultSet rs = db.executeQuery();  
		    List<Sentence> sentences = new LinkedList<>();
		    while(rs.next()) {    
		    	Sentence sentence = new Sentence();
		    	
		    	sentence.quizsetId = scriptId;
		    	sentence.id = rs.getInt("id");
		    	sentence.revision = rs.getInt("revision");
		    	sentence.textQuestion = rs.getString("text_ko");
		    	sentence.textAnswer = rs.getString("text_en");
		    	sentence.updatedTimeStamp = rs.getLong("updated_long");
		    	sentences.add(sentence);
		    }
		    
		    DBResult dbResult = new DBResult();
		    dbResult.noSelected = sentences.isEmpty();
		    dbResult.resultObject = sentences;
		    return dbResult;
		    
		} catch (Exception e) {
			log.error("query["+query+"], args[scriptId:"+scriptId+"]");
			e.printStackTrace();
		} finally {
			db.close();
		} 
		return null;
	}
	
	
	private List<ObjectBundle> selectSentencesForSync_Update( Integer userID )
	{
		List<ObjectBundle> syncedList = new LinkedList<>();
		
		String queryUpdate = "SELECT A.sentenceId, B.scriptId, B.revision, B.text_ko, B.text_en "
				+ " FROM user_quizset as A "
				+ 	" INNER JOIN all_sentence as B "
 				+ " ON A.userId = ? "
 				+ 	" AND A.scriptId = B.scriptId "
 				+ 	" AND A.sentenceId = B.id "
 				+ 	" AND A.revision < B.revision";
		
		DBConnectionHelper db = null;
		try {
			
			db = new DBConnectionHelper( queryUpdate );
		    db.setInt(1, userID);
		    ResultSet rs = db.executeQuery(); 
		    while(rs.next()){   
		    	ObjectBundle bundle = new ObjectBundle();
		    	bundle.setInt(SyncManager.FIELD_SYNC__ACTION, SyncManager.SYNC_ACTION.UPDATE.ordinal());
				bundle.setInt(Sentence.FIELD_SENTENCE_ID, rs.getInt("sentenceId"));
				bundle.setInt(Sentence.FIELD_QUIZSET_ID, rs.getInt("scriptId"));
				bundle.setInt(Sentence.FIELD_REVISION, rs.getInt("revision"));
				bundle.setString(Sentence.FIELD_SENTENCE_QUESTION, rs.getString("text_ko"));
				bundle.setString(Sentence.FIELD_SENTENCE_ANSWER, rs.getString("text_en"));
		    	syncedList.add(bundle);
		    }
		    return syncedList;
	    	
	    } catch (Exception e) {
			log.error("query["+queryUpdate+"]");
			e.printStackTrace();
		} finally {
			db.close();
		} 
		return Collections.emptyList();
	}
	
	private List<ObjectBundle> selectSentencesForSync_Del( Integer userID )
	{
		List<ObjectBundle> syncedList = new LinkedList<>();
		
		String queryDel = "SELECT A.sentenceId, B.scriptId, B.revision, B.text_ko, B.text_en "
				+ " FROM user_quizset as A "
				+ 	" INNER JOIN all_sentence as B "
 				+ " ON A.userId = ? "
 				+ 	" AND A.scriptId = B.scriptId "
 				+ 	" AND A.sentenceId = B.id "
 				+ 	" AND B.revision = ?";
		
		DBConnectionHelper db = null;
		try {
			
			db = new DBConnectionHelper( queryDel );
		    db.setInt(1, userID);
		    db.setInt(2, Sentence.DELETED_SENTENCE_REVIISON);
		    ResultSet rs = db.executeQuery(); 
		    while(rs.next()){   
		    	ObjectBundle bundle = new ObjectBundle();
		    	bundle.setInt(SyncManager.FIELD_SYNC__ACTION, SyncManager.SYNC_ACTION.DEL.ordinal());
				bundle.setInt(Sentence.FIELD_SENTENCE_ID, rs.getInt("sentenceId"));
				bundle.setInt(Sentence.FIELD_QUIZSET_ID, rs.getInt("scriptId"));
				bundle.setInt(Sentence.FIELD_REVISION, rs.getInt("revision"));
				bundle.setString(Sentence.FIELD_SENTENCE_QUESTION, rs.getString("text_ko"));
				bundle.setString(Sentence.FIELD_SENTENCE_ANSWER, rs.getString("text_en"));
		    	syncedList.add(bundle);
		    }
		    return syncedList;
		    
	    } catch (Exception e) {
			log.error("query["+queryDel+"]");
			e.printStackTrace();
		} finally {
			db.close();
		} 
		return Collections.emptyList();
	}
	
	
	private List<ObjectBundle> selectSentencesForSync_Add( Integer userID )
	{
		List<ObjectBundle> syncedList = new LinkedList<>();
		
		 /* 
		    * find sentences to Need 'ADD' 
		    * 
		    * System's sentence 중에 , 나중에 추가된 sentence(report 수정으로 인해)가 있다면,
		    * user도 추가 받아야 한다. 
		    * 
		    * all_sentence 
		   
		   */ 
		String queryAdd = "SELECT distinct D.id, D.scriptId, D.revision, D.text_ko, D.text_en "
	   			+ "FROM user_quizset as C "
	   			+ "INNER JOIN ("
	   			+ 				"SELECT A.id, A.scriptId, A.revision, A.text_ko, A.text_en  "
	   			+ 				"FROM all_sentence as A " 
				+ 				"LEFT JOIN user_quizset as B "
				+ 				"ON A.id = B.sentenceId "
				+ 				"WHERE B.sentenceId is null"
				+ ") as D " 
				+ "ON C.userId = ? AND C.scriptId = D.scriptId AND D.revision != ?";
		
		
		DBConnectionHelper db = null;
		try {
			
			db = new DBConnectionHelper( queryAdd );
		    db.setInt(1, userID);
		    db.setInt(2, Sentence.DELETED_SENTENCE_REVIISON);
		    ResultSet rs = db.executeQuery(); 
		    while(rs.next()){   
		    	ObjectBundle bundle = new ObjectBundle();
		    	bundle.setInt(SyncManager.FIELD_SYNC__ACTION, SyncManager.SYNC_ACTION.ADD.ordinal());
				bundle.setInt(Sentence.FIELD_SENTENCE_ID, rs.getInt("id"));
				bundle.setInt(Sentence.FIELD_QUIZSET_ID, rs.getInt("scriptId"));
				bundle.setInt(Sentence.FIELD_REVISION, rs.getInt("revision"));
				bundle.setString(Sentence.FIELD_SENTENCE_QUESTION, rs.getString("text_ko"));
				bundle.setString(Sentence.FIELD_SENTENCE_ANSWER, rs.getString("text_en"));
		    	syncedList.add(bundle);
		    }
		    return syncedList;
		    
	    } catch (Exception e) {
			log.error("query["+queryAdd+"]");
			e.printStackTrace();
		} finally {
			db.close();
		} 
		return Collections.emptyList();
	}
	
	/*
	 *  유저 문장 싵크.
	 *  
	 *  업테이드, 삭제 , 추가
	 */
	public DBResult selectSentencesForSync( Integer userID ) 
	{
		if( ! User.checkUserID(userID)) {
			log.error("invalid argment. userId("+userID+")");
			return null;
		}
		
		List<ObjectBundle> syncedList = new LinkedList<>();
		
		syncedList.addAll( selectSentencesForSync_Update(userID) );
		syncedList.addAll( selectSentencesForSync_Del(userID) );
		syncedList.addAll( selectSentencesForSync_Add(userID) );
		
		DBResult dbResult = new DBResult();
	    dbResult.noSelected = syncedList.isEmpty();
	    dbResult.resultObject = syncedList;
	    return dbResult;
	}
	
	public DBResult selectUserSentence( Integer userId, Integer sentenceId ) {
		if( ! User.checkUserID(userId)) {
			log.error("invalid argment. userId("+userId+")");
			return null;
		}
		if( ! Sentence.checkSentenceId(sentenceId)) {
			log.error("invalid argment. sentenceId("+sentenceId+")");
			return null;
		}
		
		String query = "SELECT scriptId, sentenceId, revision"
					+ " FROM user_quizset"
	 				+ " WHERE userid = ? and sentenceId = ?";

		DBConnectionHelper db = null;
		try {
			db = new DBConnectionHelper( query );
		    db.setInt(1, userId);
			db.setInt(2, sentenceId);
		    ResultSet rs = db.executeQuery(); 

		    Sentence userSentence = new Sentence();
		    while(rs.next()){     
		    	userSentence.quizsetId = rs.getInt("scriptId");
		    	userSentence.id = rs.getInt("sentenceId");
		    	userSentence.revision = rs.getInt("revision");
		    }
		    
		    log.debug("DB userScript. "
		    		+ "query["+query+"], "
		    		+ "args[userId:"+userId+", sentenceId:"+sentenceId+"]");
		    
		    
		    DBResult dbResult = new DBResult();
		    dbResult.noSelected = Sentence.isNull(userSentence);
		    dbResult.resultObject = userSentence;
		    return dbResult;
		    
		} catch (Exception e) {
			log.error("query["+query+"], args[userId:"+userId+", sentenceId:"+sentenceId+"]");
			e.printStackTrace();
		} finally {
			db.close();
		} 
		return null;
	}
	

	public int insertSentenceToSystem( Integer scriptId, String textKo, String textEn )
	{
		String queryInsert = "insert into all_sentence (scriptId, revision, text_ko, text_en, updated_dt) "
						+ "values(?, ?, ?, ?, now())";
		String queryGetLastInsertID = "select id from all_sentence order by id desc limit 1";
		
		int lastInsertID = -1;

		StringBuffer logString = new StringBuffer("scriptId:"+scriptId +",textKo:"+textKo+",textEn:"+textEn);
		
		DBConnectionHelper db = null;
		boolean bSucc = false;
		
		try{
			// INSERT
			db = new DBConnectionHelper( queryInsert );
			db.setInt(1, scriptId);
			db.setInt(2, 0); // revision
			db.setString(3, textKo);
			db.setString(4, textEn);
			db.executeUpdate();
			bSucc = true;
			
		} catch (Exception e) {
			log.error("querys["+queryInsert+"] "
					+ "args["+logString+"]");
			e.printStackTrace();
		} finally {
			db.close();
		}
		
		if( bSucc )
		{
			try{
				// SELECT LAST ID
				db = new DBConnectionHelper( queryGetLastInsertID );
				ResultSet rs = db.executeQuery(); 
				
			    while(rs.next()){     
			    	lastInsertID = rs.getInt("id");
			    }
				    
			    log.debug("Last insert ID:" + lastInsertID);
		
			} catch (Exception e) {
				log.error("querys["+queryInsert+"], [" +queryGetLastInsertID +"], "
						+ "args["+logString+"]");
				e.printStackTrace();
			} finally {
				db.close();
			} 
		}
		
		return lastInsertID;
	}

	
	public boolean updataUserSentencesRevision( Integer userId, List<ObjectBundle> sentences ) 
	{
		if( ! User.checkUserID(userId)) {
			log.error("invalid argment. userId("+userId+")");
			return false;
		}
		
		if( sentences == null || sentences.isEmpty() ){
			log.error("Failed updataUserSentencesRevision. "
					+ "sentences is null");
			return false;
		}
		StringBuilder forlog = new StringBuilder();
		forlog.append("userId:"+userId);
		forlog.append(",totalSentencesCount:"+sentences.size());
		forlog.append(", (updatedCount:revision,id): ");
			
		String query = "UPDATE user_quizset "
						+ "SET revision = ? "
						+ "WHERE userId = ? AND sentenceId = ? ";
		 
		DBConnectionHelper db = null;
		try {
			
			db = new DBConnectionHelper( query );
			int updatedCount = 0;
			for(ObjectBundle sentence : sentences)
			{
				int revision = sentence.getInt(Sentence.FIELD_REVISION);
				int sentenceId = sentence.getInt(Sentence.FIELD_SENTENCE_ID);
				
				forlog.append("("+updatedCount++
								+":"+revision
								+","+sentenceId+"),");
				
				db.setInt(1, revision);
				db.setInt(2, userId);
				db.setInt(3, sentenceId);
				db.executeUpdate();
			}
			return true;
			
        } catch (Exception e) {
        	log.error("querys["+query+"], "
        			+ "args["+forlog+"]");
        	e.printStackTrace();
        } finally {
        	db.close();
        } 
		return false;     
	}
	

	 /*
	  *  Update a weird Sentence to Correctly.
	  *  Revision updated here !!
	  */
	 public boolean updateSentence(Integer sentenceId, String textKo, String textEn)
	 {
		 if( ! Sentence.checkSentenceId(sentenceId)){
			 log.error("Invalid sentenceId("+sentenceId+")");
			 return false;
		 }
		 if( ! Sentence.checkText(textKo)
				 || ! Sentence.checkText(textEn)){
			 log.error("Invalid textKo("+textKo+"),textEn("+textEn+")");
			 return false;
		 }
			
		String query = "UPDATE all_sentence "
						+ " SET revision = revision + 1"
						+ " , text_ko = ? "
						+ " , text_en =? "
						+ " , updated_dt = now()"
						+ " WHERE id = ? ";
		
		String logString = "sentenceId:"+sentenceId
							+", textKo:"+textKo
							+", textEn:"+textEn;
		 
		DBConnectionHelper db = null;
		try {
			db = new DBConnectionHelper( query );
			db.setString(1, textKo);
			db.setString(2, textEn);
			db.setInt(3, sentenceId);
			db.executeUpdate();
			
			log.debug("updateSentence() changed sentence.."+logString);
			
			return true;
			
      } catch (Exception e) {
      	log.error("querys["+query+"], "
      			+ "args["+logString+"]");
      	e.printStackTrace();
      } finally {
      	db.close();
      } 
		return false;
	 }
	
	 
	 
	public boolean deleteSentenceFromAllUser( Integer sentenceId )
	{
		String query = "delete from user_quizset "
						+ "WHERE sentenceId = ?";
		
		StringBuffer logString = new StringBuffer("sentenceId:"+sentenceId);
		
		DBConnectionHelper db = null;
		try{
			db = new DBConnectionHelper( query );
			db.setInt(1, sentenceId);
			db.executeUpdate();
	
			log.debug("Sentence Deleted From user_quizset Table.. " +logString);
			
			return true;
			
        } catch (Exception e) {
        	log.error("querys["+query+"], "
        			+ "args["+logString+"]");
        	e.printStackTrace();
        } finally {
        	db.close();
        } 
		return false;
	}
	
	public boolean deleteSentence( Integer sentenceId )
	{
		String query = "UPDATE all_sentence "
						+ "SET revision = ? " 
						+ "WHERE id = ?";
		
		StringBuffer logString = new StringBuffer(query + ", ar1:"+Sentence.DELETED_SENTENCE_REVIISON+", arg2:"+ sentenceId);
		
		DBConnectionHelper db = null;
		try{
			db = new DBConnectionHelper( query );
			db.setInt(1, Sentence.DELETED_SENTENCE_REVIISON);
			db.setInt(2, sentenceId);
			db.executeUpdate();
	
			log.debug("Query[ " + logString +" ]");
			
			return true;
			
        } catch (Exception e) {
        	log.error("querys["+query+"], "
        			+ "args["+logString+"]");
        	e.printStackTrace();
        } finally {
        	db.close();
        } 
		return false;
	}
	
	
	public DBResult insertQuizSetAndSentencesToSystem( String scriptTitle, List<Sentence> sentences )
	{
		//log.debug("insertScriptAndSentences() called.. scriptTitle:"+scriptTitle);
		
		if( StringHelper.isNull(scriptTitle) ){
			log.error("insertScriptAndSentences() scriptTitle is null");
			return null;
		}
		if( sentences == null || sentences.isEmpty() ){
			log.error("insertScriptAndSentences() entences is null");
			return null;
		}
			
		String queryInsertScript = "insert into all_quizset (title, created_dt) "
									+ "values(?, now())";
		String queryInsertSentences = "insert into all_sentence (scriptId, revision, text_ko, text_en, updated_dt) "
									+ "values(?, ?, ?, ?, now())";
		
		Integer scriptId = -1;
		StringBuffer logString = new StringBuffer();
		
		DBConnectionHelper db = null;
		
		// 1. insert script 
		// a scriptID is auto increment in DB.
		try {
			logString.append("scriptTitle:"+scriptTitle);
			db = new DBConnectionHelper( queryInsertScript );
			db.setString(1, scriptTitle);
			db.executeUpdate();
			
			// 1-2. check Insert Well. 
			// If Not? RollBack !!!
			DBResult dbResult = selectQuizsetIDByTitle(scriptTitle);
			if( dbResult == null || dbResult.noSelected() ){
				throw new DBException("Finish Script Insert. "
						+ "but can't Select ScriptId and can't Insert Sentences. "
						+ "so, Starting Rollback. scriptTitle"+scriptTitle);
			}
			scriptId = (Integer)dbResult.getResultObject();
		    logString.append(", scriptId:"+scriptId);
		    log.info("1. Script Inserted.. " +logString);
		} catch (Exception e) {
        	String msg = "querys["+queryInsertScript+"], args["+logString+"]";
        	log.error("DB insert failed. "+ msg);
        	
        	rollbackInsertQuizsetAndSentences(scriptId, scriptTitle);
        	
        } finally {
        	db.close();
        }  
		    
		
		// 2. insert sentences
		// a sentenceID is auto increment in DB.
		try {	
			logString.append(",sentences(len:"+sentences.size()+") - ");
			db = new DBConnectionHelper( queryInsertSentences );
			for(Sentence s : sentences)
			{
				if( s == null ){
					log.error("sentence is null. "
							+ "but Ignore it and Continue inserting Next Sentence into DB.."
							+ "scriptId: "+scriptId);
					continue;
				}
				
				db.setInt(1, scriptId);
				db.setInt(2, s.revision);
				String ko = s.textQuestion.toString();
				String en = s.textAnswer.toString();
				db.setString(3, ko);
				db.setString(4, en);
				
				logString.append("{id:"+s.id+", revi:"+s.revision
											+", ko:"+ko+", en:"+en+"}/");
				
				db.executeUpdate();
			}
			
			
			// 2-2. check Insert Well Sentences. 
			// If Not? RollBack!!
			DBResult dbResult = selectSentencesByQuizsetId(scriptId);
			if( dbResult == null ){
				log.error("Failed insertScriptAndSentences(). DB Error");
				return null;
			}
			if( dbResult.noSelected ){
				throw new DBException("Finish Sentences Insert. "
						+ "but can't Select Sentences. "
						+ "so, Starting Rollback. scriptTitle"+scriptTitle);
			}
			log.info("2. Sentences Inserted.. "+logString);
			
			dbResult.resultObject = scriptId;
			return dbResult;
			
		} catch (Exception e) {
        	String msg = "querys["+queryInsertSentences+"], args["+logString+"]";
        	log.error("DB insert failed. "+ msg);
        	
        	rollbackInsertQuizsetAndSentences(scriptId, scriptTitle);
        	
        } finally {
        	db.close();
        }  
		
		return null;
	}
	
	
	public void rollbackInsertQuizsetAndSentences( Integer scriptId, String scriptTitle )
	{
		log.info("rollbackInsertQuizsetAndSentences() called.. scriptTitle:"+scriptTitle );
		
		if( ! QuizSet.checkQuizSetID(scriptId)){
			log.error("Invalid scriptId("+scriptId+")");
			return;
		}
		if( ! StringHelper.isNull(scriptTitle)){
			log.error("Invalid scriptTitle("+scriptTitle+")");
			return;
		}
		
		
		String queryDeleteSentences = "DELETE from all_sentence "
									+ "WHERE scriptId = ?";
		String queryDeleteScript = "DELETE from all_quizset "
									+ "WHERE id = ?";
	 
		StringBuffer logString = new StringBuffer("scriptId:"+scriptId);
		
		DBConnectionHelper db = null;
		try {
			// 1. delete sentences
			// success insert all_quizset. 
			// but, fail insert all_sectence. 
			// 해당 scriptId로 저장된 sentences를 삭제.
			db = new DBConnectionHelper( queryDeleteSentences );
			db.setInt(1, scriptId);
			db.executeUpdate();
			log.info("1. Senetences Deleted.. " + logString);
			
		} catch (Exception e) {
			log.error("query["+queryDeleteSentences+"], args["+logString+"]");
			e.printStackTrace();
		} finally {
			db.close();
		} 
		
		
		try {
			//2. delete script
			db = new DBConnectionHelper( queryDeleteScript );
			db.setInt(1, scriptId);
			db.executeUpdate();
			log.info("2. Script Deleted.." + logString);
			
		} catch (Exception e) {
			log.error("query["+queryDeleteScript+"], args["+logString+"]");
			e.printStackTrace();
		} finally {
			db.close();
		} 
	}
	
	 
	 
	
	/*
	 *  Report
	 */
	 public DBResult selectReportBySentenceId(Integer sentenceId)
	 {
		 if( ! Sentence.checkSentenceId(sentenceId)){
			log.error("Invalid sentenceId("+sentenceId+")");
			return null;
		}
		 
		 String query = "SELECT sentenceId, scriptId, "
		 						+ "userId, state, "
		 						+ "text_ko, text_en, "
		 						+ "unix_timestamp(created_dt) as created_long"
					+ " FROM report "
					+ " WHERE sentenceId = ?";

		DBConnectionHelper db = null;
		try {
			db = new DBConnectionHelper( query );
			db.setInt(1, sentenceId);
		    ResultSet rs = db.executeQuery(); 
		    
		    Report report = new Report();
		    while(rs.next()) {    
		    	report.setSentenceId(rs.getInt("sentenceId"));
				report.setScriptId(rs.getInt("scriptId")); 
				report.setUserId(rs.getInt("userId"));
				report.setState(rs.getInt("state"));
				report.setTextKo(rs.getString("text_ko"));
				report.setTextEn(rs.getString("text_en"));
		    }

		    DBResult dbResult = new DBResult();
		    dbResult.noSelected = Report.isNull(report);
		    dbResult.resultObject = report;
		    return dbResult;
		    
		 } catch (Exception e) {
        	log.error("querys["+query+"] ");
        	e.printStackTrace();
        } finally {
        	db.close();
        } 
		return null;
	 }
	 
	 public DBResult selectReportsByCreatedTimeDesc(Integer state, Integer selectCount)
	 {
		 if( ! Report.checkState(state) ){
			log.error("Invalid state("+state+")");
			return null;
		 }
		 if( selectCount == null || selectCount < 0 ){
			log.error("Invalid selectCount("+selectCount+")");
			return null;
		 }
			
		 String query = "SELECT sentenceId, scriptId, "
								+ "userId, state, "
								+ "text_ko, text_en, "
								+ "unix_timestamp(created_dt) as created_long"
					+ " FROM report "
					+ " WHERE state = ?"
					+ " ORDER BY created_dt DESC"
					+ " LIMIT ?";
			
			DBConnectionHelper db = null;
			try {
			db = new DBConnectionHelper( query );
			db.setInt(1, state);
			db.setInt(2, selectCount);
			ResultSet rs = db.executeQuery(); 
			
			List<Report> reports = new LinkedList<>();
			while(rs.next()) {    
				Report report = new Report();
				report.setSentenceId(rs.getInt("sentenceId"));
				report.setScriptId(rs.getInt("scriptId")); 
				report.setUserId(rs.getInt("userId"));
				report.setState(rs.getInt("state"));
				report.setTextKo(rs.getString("text_ko"));
				report.setTextEn(rs.getString("text_en"));
				
				reports.add(report);
			}
			
			DBResult dbResult = new DBResult();
		    dbResult.noSelected = reports.isEmpty();
		    dbResult.resultObject = reports;
		    return dbResult;
			
        } catch (Exception e) {
        	log.error("querys["+query+"] ");
        	e.printStackTrace();
        } finally {
        	db.close();
        } 
		return null;
	 }
	 
	 public DBResult selectReportCount(Integer state)
	 {
		 if( ! Report.checkState(state) ){
			log.error("Invalid state("+state+")");
			return null;
		 }
			
		 String query = "SELECT count(*) as cnt"
					+ " FROM report "
					+ " WHERE state = ?";
			
			DBConnectionHelper db = null;
			try {
			db = new DBConnectionHelper( query );
			db.setInt(1, state);
			ResultSet rs = db.executeQuery(); 
			
			int reportCount = -1;
			while(rs.next()) {    
				reportCount = rs.getInt("cnt");
			}
			
			DBResult dbResult = new DBResult();
		    dbResult.noSelected = (reportCount == -1);
		    dbResult.resultObject = reportCount;
		    return dbResult;
			
        } catch (Exception e) {
        	log.error("querys["+query+"] ");
        	e.printStackTrace();
        } finally {
        	db.close();
        } 
		return null;
	 }
	 
	 public boolean insertReport(Integer userId, Sentence sentence)
	 {		
		if( ! User.checkUserID(userId) ){
			log.error("Invalid UserID("+userId+")");
			return false;
		}
		if( Sentence.isNull(sentence)){
			log.error("Null sentence("+sentence+")");
			return false;
		}
			
		String query = "insert into report (sentenceId, scriptId, userId, state, text_ko, text_en, created_dt) "
									+ "values(?,?,?,?,?, ?, now())";
		
		String logString = "userId:"+userId+",sentence:"+sentence.toString();
		 
		DBConnectionHelper db = null;
		try {
			db = new DBConnectionHelper( query );
			db.setInt(1, sentence.id);
			db.setInt(2, sentence.quizsetId);
			db.setInt(3, userId);
			db.setInt(4, Report.STATE_REPORTED);
			db.setString(5, sentence.textQuestion);
			db.setString(6, sentence.textAnswer);
			db.executeUpdate();
			return true;
			
        } catch (Exception e) {
        	log.error("querys["+query+"], "
        			+ "args["+logString+"]");
        	e.printStackTrace();
        } finally {
        	db.close();
        } 
		return false;
	 }
	 
	 public boolean updateReport(Integer sentenceId, Integer state)
	 {
		if( ! Report.checkState(state) ){
			log.error("Invalid state("+state+")");
			return false;
		}
		if( ! Sentence.checkSentenceId(sentenceId)){
			log.error("Invalid sentenceId("+sentenceId+")");
			return false;
		}
		
		String query = "UPDATE report "
						+ "SET state = ? "
						+ "WHERE sentenceId = ? ";
		
		String logString = "sentenceId:"+sentenceId
							+", state:"+state;
		 
		DBConnectionHelper db = null;
		try {
			db = new DBConnectionHelper( query );
			db.setInt(1, state);
			db.setInt(2, sentenceId);
			db.executeUpdate();
			return true;
			
        } catch (Exception e) {
        	log.error("querys["+query+"], "
        			+ "args["+logString+"]");
        	e.printStackTrace();
        } finally {
        	db.close();
        } 
		return false;
	 }
	 
	 public boolean updateReport(Integer userId, Integer sentenceId, Integer state, String textKo, String textEn)
	 {
			
		 if( ! User.checkUserID(userId) ){
			log.error("Invalid UserID("+userId+")");
			return false;
		 }
		 if( ! Report.checkState(state) ){
			 log.error("Invalid state("+state+")");
			 return false;
		 }
		 if( ! Sentence.checkSentenceId(sentenceId)){
			 log.error("Invalid sentenceId("+sentenceId+")");
			 return false;
		 }
		 if( ! Sentence.checkText(textKo)
				 || ! Sentence.checkText(textEn)){
			 log.error("Invalid textKo("+textKo+"),textEn("+textEn+")");
			 return false;
		 }
		
		String query = "UPDATE report "
						+ "SET state = ? "
						+ ", text_ko = ? "
						+ ", text_en = ? "
						+ ", userId = ? "
						+ "WHERE sentenceId = ? ";
		
		String logString = "sentenceId:"+sentenceId
							+", state:"+state;
		 
		DBConnectionHelper db = null;
		try {
			db = new DBConnectionHelper( query );
			db.setInt(1, state);
			db.setString(2, textKo);
			db.setString(3, textEn);
			db.setInt(4, userId);
			db.setInt(5, sentenceId);
			db.executeUpdate();
			
			return true;
			
        } catch (Exception e) {
        	log.error("querys["+query+"], "
        			+ "args["+logString+"]");
        	e.printStackTrace();
        } finally {
        	db.close();
        } 
		return false;
	 }
	 
	
}

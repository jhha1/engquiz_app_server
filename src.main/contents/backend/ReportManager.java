package contents.backend;

import java.util.List;

import org.apache.log4j.Logger;

import data.db.DBQueries;
import data.db.DBQueries.DBResult;
import exception.contents.ContentsRoleException;
import net.protocol.EResultCode;

public class ReportManager {
	final private Logger log = Logger.getLogger( ReportManager.class );
	
	private static ReportManager instance = new ReportManager();
	private ReportManager() {}
	
	public static ReportManager getInstance() {
		return instance;
	}

	public static final int MODIFY_UPDATE = 1;
	public static final int MODIFY_DEL = 2; 
	public static final int MODIFY_ADD = 3; 
	
	public boolean checkAdmin(Integer userId){
		return UserManager.getInstance().isAdminUser(userId);
	}
	
	public int getReportsCount(){
		final DBQueries db = DBQueries.getInstance();
		DBResult dbResult = db.selectReportCount(Report.STATE_REPORTED);
		if( dbResult == null ){
			log.error("Failed getReportsCount(). DBError");
			return -1;
		}
		return (int) dbResult.getResultObject();
	}
	
	@SuppressWarnings("unchecked")
	public List<Report> getReportsByReportTimeDesc(int needReportCount){
		final DBQueries db = DBQueries.getInstance();
		DBResult dbResult = db.selectReportsByCreatedTimeDesc(Report.STATE_REPORTED, needReportCount);
		if( dbResult == null ){
			log.error("Failed getReportsByReportTimeDesc(). DBError");
			return null;
		}
		return (List<Report>) dbResult.getResultObject();
	}
	
	public EResultCode addReportedSentence(Integer userId, Sentence sentence)
	{
		
		EResultCode resultCode = checkReportedSentence(userId, sentence);
		if( resultCode.isFail() ){
			switch(resultCode){
				case INVALID_USERID:
				case INVALID_SENTENCE:
				case INVALID_SENTENCE_ID:
					log.error("invalid argment("+resultCode.toString()+")");
					break;
				case REPORT_DUPLICATED:
					// nothing.
					// 이미 reporting 된 거니까,  
					// user에게는 report 성공이라고 보낸다
					return EResultCode.SUCCESS;
				case USER_SENTENCE_REVISION_IS_OLD_THEN_SYSTEMS:
					// nothing.
					// DB에 최신 버전이 있어서, 구지 옛버전 sentence를 수정할 필요 없다.
					// user는 sync로 최신버전을 업데이트 받으면 된다.
					log.info("Ignore this request. No need to save a Reported Sentence."
							+ "because DB already has updated sentence. "
							+ "User can update it by 'Sync'. ");
					break;
				default:
					log.error("other error code("+resultCode.toString()+")");
					break;
			}
			return resultCode;
		}
			
			
		final DBQueries db = DBQueries.getInstance();
		DBResult dbResult = db.selectReportBySentenceId(sentence.id); 
		if(dbResult == null || dbResult.noSelected())
		{
			boolean bOK = db.insertReport(userId, sentence);
			if( !bOK ){
				log.error("Failed addReportedSentence. InsertDBError. "
						+ "userId("+userId+"), sentence("+sentence+")");
				return EResultCode.DB_ERR;
			}
		} 
		else 
		{
			boolean bOK = db.updateReport(userId, sentence.id, 
										Report.STATE_REPORTED,
										sentence.textQuestion, sentence.textAnswer);
			if( !bOK ){
				log.error("Failed addReportedSentence. UpdateDBError. "
						+ "userId("+userId+"), sentence("+sentence+")");
				return EResultCode.DB_ERR;
			}
		}
		return EResultCode.SUCCESS;
	}

	private EResultCode checkReportedSentence(Integer userId, Sentence reportedSentence)
	{
		if( !User.checkUserID(userId)) {
			log.error("Invalid userId("+userId+")");
			return EResultCode.INVALID_USERID;
		}
		
		if(Sentence.isNull(reportedSentence)){
			log.error("Failed seveReportedSentence. "
							+ "reportedSentence argumet is null ");
			return EResultCode.INVALID_SENTENCE;
		}
		
		Integer sentenceId = reportedSentence.id;
		final DBQueries db = DBQueries.getInstance();
		DBResult dbResult = db.selectReportBySentenceId(sentenceId);
		if(dbResult ==null){
			log.error("Failed seveReportedSentence. DBError");
			return EResultCode.DB_ERR;
		}
		if(false == dbResult.noSelected()){
			log.error("Failed seveReportedSentence. "
					+ "Duplicated Reported. "
					+ "sentenceId:"+sentenceId);
			return EResultCode.REPORT_DUPLICATED;
		}
		
		dbResult = db.selectSentenceBySentenceId(sentenceId);
		if(dbResult ==null){
			log.error("Failed seveReportedSentence. DBError");
			return EResultCode.DB_ERR;
		}
		if(dbResult.noSelected()){
			log.error("Failed seveReportedSentence. "
					+ "No Existed Sentnece. "
					+ "sentenceId:"+sentenceId);
			return EResultCode.INVALID_SENTENCE_ID;
		}
		Sentence systemSentence = (Sentence)dbResult.getResultObject();
		
		dbResult = db.selectUserSentence(userId, sentenceId);
		if(dbResult.noSelected()){
			log.error("Failed seveReportedSentence. "
					+ "User No has this Sentnece. "
					+ "sentenceId:"+sentenceId);
			return EResultCode.INVALID_SENTENCE_ID;
		}
		Sentence userSentence = (Sentence)dbResult.getResultObject();
		
		// check revision
		Integer userRevision = userSentence.revision;
		Integer sysRevision = systemSentence.revision;
		if( sysRevision == Sentence.DELETED_SENTENCE_REVIISON ){
			log.error("cannot accept 수정요청. because this is deleted sentence. "
							+ " sentenceId("+sentenceId+"), "
							+ "user CAN resolve it by 'Sync'");
			return EResultCode.USER_SENTENCE_REVISION_IS_OLD_THEN_SYSTEMS;
		} else if( userRevision < sysRevision ){
			log.error("cannot accept 수정요청. because user's revision older then system's. "
							+ "{userRevision("+userRevision+"), "
							+ "systemRevision("+sysRevision+")} "
							+ "user can resolve it by 'Sync'");
			return EResultCode.USER_SENTENCE_REVISION_IS_OLD_THEN_SYSTEMS;
		} 
		return EResultCode.SUCCESS;
	}
	
	public void delSentence( Integer scriptId, Integer sentenceId ) throws ContentsRoleException {
		if( ! QuizSet.checkQuizSetID(scriptId)) {
			log.error("Invalid scriptId("+scriptId+")");
			throw new ContentsRoleException(EResultCode.INVALID_SCRIPT_ID);
		}
		if( ! Sentence.checkSentenceId(sentenceId)) {
			log.error("Invalid sentenceId("+sentenceId+")");
			throw new ContentsRoleException(EResultCode.INVALID_SENTENCE_ID);
		}
		
		final QuizSetManager scriptMgr = QuizSetManager.getInstance();
		if( false == scriptMgr.deleteSentence(scriptId, sentenceId) ){
			log.error("Failed delete sentence from ScriptManager. ("+sentenceId+")");
			throw new ContentsRoleException(EResultCode.SYSTEM_ERR);
		}
		
		// update report 
		final DBQueries db = DBQueries.getInstance();
		boolean bOK = db.updateReport(sentenceId, Report.STATE_MODIFILED);
		if( !bOK ){
			log.error("Failed delReportedSentence. updateReport-DBError. "
					+ "sentenceId("+sentenceId+"), sentenceId("+sentenceId+")");
			throw new ContentsRoleException(EResultCode.DB_ERR);
		}
	}
	
	public Sentence addSentence( Integer scriptId, String textKo, String textEn ) throws ContentsRoleException {
		if( ! QuizSet.checkQuizSetID(scriptId)) {
			log.error("Invalid scriptId("+scriptId+")");
			throw new ContentsRoleException(EResultCode.INVALID_SCRIPT_ID);
		}
		if( ! Sentence.checkText(textKo) || ! Sentence.checkText(textEn) ){
			log.error("Invalid Text. ko("+textKo+"), en("+textEn+")");
			throw new ContentsRoleException(EResultCode.INVALID_SENTENCE_TEXT);
		}
		
		final QuizSetManager scriptMgr = QuizSetManager.getInstance();
		Sentence addedSentenceObject = scriptMgr.addSentence(scriptId, textKo, textEn);
		if( Sentence.isNull(addedSentenceObject) ){
			throw new ContentsRoleException(EResultCode.SYSTEM_ERR);
		}

				
		return addedSentenceObject;
	}
	
	public Sentence modifyReportedSentence(Integer scriptId, Integer sentenceId, String textKo, String textEn) throws ContentsRoleException
	{
		if( ! Sentence.checkSentenceId(sentenceId)) {
			log.error("Invalid sentenceId("+sentenceId+")");
			throw new ContentsRoleException(EResultCode.INVALID_SENTENCE_ID);
		}
		
		if( ! Sentence.checkText(textKo) || ! Sentence.checkText(textEn) ){
			log.error("Invalid Text. ko("+textKo+"), en("+textEn+")");
			throw new ContentsRoleException(EResultCode.INVALID_SENTENCE_TEXT);
		}
		
		// check - reporting 된 문장인지
		final DBQueries db = DBQueries.getInstance();
		DBResult dbResult  = db.selectReportBySentenceId(sentenceId);
		if(dbResult == null || dbResult.noSelected()){
			log.error("Failed modifyReportedSentence. "
					+ "Invalid sentenceId. "
					+ "sentenceId:"+sentenceId);
			throw new ContentsRoleException(EResultCode.INVALID_SENTENCE_ID);
		} 
		
		// update sentence
		QuizSetManager scriptMgr = QuizSetManager.getInstance();
		Sentence updatedSentence = scriptMgr.updateSentence(scriptId, sentenceId, textKo, textEn);
		if( Sentence.isNull(updatedSentence) ){
			log.error("Failed UpdateSentence. "
					+ "sentenceId("+sentenceId+"), textKo("+textKo+"), textEn("+textEn+")");
			throw new ContentsRoleException(EResultCode.REPORT_MEMORY_UPDATE_FAIL);
		}
		
		// update report 
		boolean bOK = db.updateReport(sentenceId, Report.STATE_MODIFILED);
		if( !bOK ){
			log.error("Failed delReportedSentence. updateReport-DBError. "
					+ "sentenceId("+sentenceId+"), textKo("+textKo+"), textEn("+textEn+")");
			throw new ContentsRoleException(EResultCode.DB_ERR);
		}
		
		return updatedSentence;
	}

}

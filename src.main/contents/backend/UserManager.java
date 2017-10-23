package contents.backend;

import org.apache.log4j.Logger;
import data.db.DBQueries;
import data.db.DBQueries.DBResult;
import net.protocol.EResultCode;
import utils.StringHelper;

public class UserManager 
{
	final private Logger log = Logger.getLogger( UserManager.class );
	
	private static UserManager instance = new UserManager();
	private UserManager() {}
	
	public static UserManager getInstance() {
		return instance;
	}
	
	public boolean isExistUser( String userName ) 
	{
		if( StringHelper.isNull(userName) ) {
			log.error("Null or Empty UserName(" + userName +")");
			return false;
		}
		
		User user = getUserByName( userName );
		boolean bNoSelected = User.isNull(user);
		if( bNoSelected ){
			log.error("bNoSelected in DB. username(" + userName +")");
			return false;
		}

		return true;
	}
	
	public User getUserByName( String userName ) 
	{
		if( StringHelper.isNull(userName) ) {
			log.error("Null or Empty UserName(" + userName +")");
			return null;
		}

		final DBQueries db = DBQueries.getInstance(); 
		DBResult dbResult = db.selectUserByName( userName ); 
		if( dbResult == null || dbResult.noSelected() ){
			return null;
		}
		return (User)dbResult.getResultObject(); 
	}
	

	public EResultCode signIn( String userName )
	{
		EResultCode resultCode = User.checkUserNameDetail(userName);
		if( resultCode.isFail() ){
			log.error("Invalid UserName. "
					+ "reason("+resultCode.toString()+"), "
					+ "name(" + userName +")");
			return EResultCode.INVALID_USERNAME;
		}

		boolean bDuplicated = isExistUser( userName );
		if( bDuplicated ) {
			log.error("USERNAME_DUPLICATED(" + userName +")");
			return EResultCode.USERNAME_DUPLICATED;
		}
		
		return createUser( userName );
	}
	
	private EResultCode createUser( String userName ) {
		EResultCode resultCode = User.checkUserNameDetail(userName);
		if( resultCode.isFail() ){
			log.error("Invalid UserName. "
					+ "reason("+resultCode.toString()+"), "
					+ "name(" + userName +")");
			return EResultCode.INVALID_USERNAME;
		}
		
		final DBQueries db = DBQueries.getInstance(); 
		
		boolean bOK = db.insertUserAccount( userName );
		if( !bOK ){
			log.error("DB Error. (" + userName +")");
			return EResultCode.DB_ERR;
		}
		return EResultCode.SUCCESS;
	}
	

	/*
	 * contents rule 적인 실패를 return값으로 대체 할 수 있으므로, 
	 * ContentsRuleException 사용안함.  
	 */
	public Integer logIn( Integer userId ) 
	{
		log.debug( "2-1. Login Called. ");
		if( userId == null ) {
			log.error("Null userId(" + userId +")");
			return 0;
		}
	
		final DBQueries db = DBQueries.getInstance(); 
		log.debug( "2-2. start selectUserById DB . ");
		DBResult dbResult = db.selectUserById( userId ); 
		if( dbResult == null ){
			log.error("DB Error. userId(" + userId +")");
			return -1;
		}

		if( dbResult.noSelected() ){
			log.error("bNoSelected in DB. userId(" + userId +")");
			return 0;
		}
		// login success.
		return 1;
	}
	
	public boolean isAdminUser( Integer userId ){
		if( userId == null ) {
			log.error("Null userId(" + userId +")");
			return false;
		}
		
		final DBQueries db = DBQueries.getInstance();
		DBResult dbResult = db.selectUserById( userId ); 
		if( dbResult == null || dbResult.noSelected() ){
			log.error("bNoSelected in DB. userId(" + userId +")");
			return false;
		}
		User user = (User)dbResult.getResultObject();
		return user.isAdmin();
	}
	
	
	/*
	 * user_quizset db에  script 추가
	 */
	public boolean addUserHasScriptList( Integer userId, QuizSetDetail parsedScript ){

		if(! User.checkUserID(userId)){
			log.error("invalid userId. userId(" + userId +")");
			return false;
		}
		if( QuizSetDetail.isNull(parsedScript) ){
			log.error("parsedScript is null. userId:" + userId);
			return false;
		}
			
		final DBQueries db = DBQueries.getInstance(); 
		DBResult dbResult = db.selectUserQuizset(userId, parsedScript.quizsetId);
		if( dbResult == null ){
			log.error("Failed DB. userId(" + userId +"), scriptTitle("+ parsedScript.title +")");
			return false;
		}
		boolean bUserNoHas = dbResult.noSelected();
		if( bUserNoHas ){
			return db.insertSentencesToUser( userId, parsedScript );
		}
		return true;
	}
	
	/*
	 * user_quizset db에  script 제거
	 */
	public boolean delUserScript( Integer userId, Integer scriptId ){

		if( false == User.checkUserID(userId)){
			log.error("invalid userId. userId(" + userId +")");
			return false;
		}
		if( false == QuizSet.checkQuizSetID(scriptId) ){
			log.error("invalid scriptId. userId:" + userId+ ", scriptId:"+scriptId);
			return false;
		}
			
		final DBQueries db = DBQueries.getInstance(); 
		boolean dbResult = db.deleteUserQuizset(userId, scriptId);
		if( dbResult == false ){
			log.error("Failed DB. userId(" + userId +"), scriptId("+ scriptId +")");
			return false;
		}
		
		return true;
	}
	
}

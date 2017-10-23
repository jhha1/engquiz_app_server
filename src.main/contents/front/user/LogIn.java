package contents.front.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import contents.backend.SyncManager;
import contents.backend.UserManager;
import contents.front.FrontLogic;
import net.protocol.EProtocol;
import net.protocol.EResultCode;
import net.protocol.Protocol;
import net.protocol.ProtocolParamChecker;


public class LogIn implements FrontLogic {

	final private Logger log = Logger.getLogger( LogIn.class );
	
	@Override
	public void action(Protocol p)
	{
		log.debug( this.getClass().getName() + " FrontLogic Called. "
					+ p.toString());

		Integer userId = ProtocolParamChecker.checkUserID(p.request.get(EProtocol.UserID));
		
		log.debug( "1. Id Checked. ");
		
		/*
		 * login
		 */
		log.debug( "2. Login process start. ");
		final UserManager user = UserManager.getInstance();
		Integer loginResult = user.logIn( userId );
		if( loginResult == 0 ) {
			p.setFail(EResultCode.INVALID_USERID, "userID:"+userId);
			return;
		} else if ( loginResult == -1 ) {
			p.setFail(EResultCode.SYSTEM_ERR);
			return;
		}
		
		log.debug( "3. isAdminUser Start ");
		boolean bAdminUser = user.isAdminUser(userId);
		
		
		log.debug( "4. sync Start ");
		/*
		 * get sync needed list
		 */
		// login시점에 sync가 필요한 sentence 정보를 가져와 sync 알람하고,  이 후 앱 실행중에 updated sentence는  sync alram을 위해 계속 check하지 않는다 (성능을 우선)
		// logic시에 얻은 정보를 들고 있다가, sync 버튼 누르면, 해당 list를 서버로 보내, list에 해당하는 sentence만 sync.
		final SyncManager sync = SyncManager.getInstance();
		List syncNeededSentenceIds = sync.checkSyncNeeded(userId);
		if( syncNeededSentenceIds == null ){
			syncNeededSentenceIds = new ArrayList<>();
		}
				
		
		p.response.set(EProtocol.UserID, userId);
		p.response.set(EProtocol.IsAdmin, bAdminUser);
		p.response.set(EProtocol.ScriptSentences, syncNeededSentenceIds);
		p.setSuccess();
	}
}

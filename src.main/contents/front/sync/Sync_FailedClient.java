package contents.front.sync;

import java.util.List;
import org.apache.log4j.Logger;

import contents.backend.SyncManager;
import contents.backend.UserManager;
import contents.front.FrontLogic;
import net.protocol.EProtocol;
import net.protocol.EResultCode;
import net.protocol.Protocol;
import net.protocol.ProtocolParamChecker;


public class Sync_FailedClient implements FrontLogic {

	final private Logger log = Logger.getLogger( Sync_FailedClient.class );
	
	@SuppressWarnings("unchecked")
	@Override
	public void action(Protocol p)
	{
		log.debug( this.getClass().getName() 
					+ " FrontLogic Called. "
					+ p.toString());
		
		Integer userId = ProtocolParamChecker.checkUserID(p.request.get(EProtocol.UserID));
		List<Integer> syncFailedResults = ProtocolParamChecker.checkList(p.request.get(EProtocol.SyncResult));  
		
		// Sync 실패한 것만 revision을 한단계 낮춘다.
		// so, 다음에 다시 sync 받을 수 있다.
		// 클라에서 넘어오는 결과를 믿지 않는 구조로 이렇게 만듬.
		final SyncManager sync = SyncManager.getInstance();
		EResultCode resultCode = sync.updateSyncResult(userId, syncFailedResults);
		if( resultCode.isFail()){
			p.setFail(EResultCode.SYSTEM_ERR);
			return;
		}
		
		p.setSuccess();
	}
}

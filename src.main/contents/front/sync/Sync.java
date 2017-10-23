package contents.front.sync;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import contents.backend.Sentence;
import contents.backend.SyncManager;
import contents.front.FrontLogic;
import net.protocol.EProtocol;
import net.protocol.EResultCode;
import net.protocol.ObjectBundle;
import net.protocol.Protocol;
import net.protocol.ProtocolParamChecker;


public class Sync implements FrontLogic {

	final private Logger log = Logger.getLogger( Sync.class );
	
	@Override
	public void action(Protocol p)
	{
		log.debug( this.getClass().getName() + " FrontLogic Called. "
					+ p.toString());
		
		Integer userId = ProtocolParamChecker.checkUserID(p.request.get(EProtocol.UserID));
		
		final SyncManager syncManager = SyncManager.getInstance();
		List<ObjectBundle> syncNeedSentences = syncManager.getSentencesForSync(userId);
		if( !(syncNeedSentences == null || syncNeedSentences.isEmpty()) ){
			// 서버에서 미리 Sync 처리
			EResultCode syncResult = syncManager.sync(userId, syncNeedSentences);
			if( syncResult.isFail() ){
				p.setFail(syncResult);
				return;
			}
		}
		
		
		List<String> serializedSentencesForSync = new LinkedList<>();
		for(ObjectBundle bundle : syncNeedSentences){
			serializedSentencesForSync.add(bundle.serialize());
		}
		
		p.response.set(EProtocol.UserID, userId);
		p.response.set(EProtocol.ScriptSentences, serializedSentencesForSync);
		p.setSuccess();
	}
}

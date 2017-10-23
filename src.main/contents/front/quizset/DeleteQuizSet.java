package contents.front.quizset;


import java.util.List;


import org.apache.log4j.Logger;

import contents.backend.UserManager;
import contents.front.FrontLogic;
import exception.contents.ContentsRoleException;
import net.protocol.EProtocol;
import net.protocol.EResultCode;
import net.protocol.Protocol;
import net.protocol.ProtocolParamChecker;

// 원본삭제는 아니고,
// 유저가 가진 스크립트 리스트에서 제거
public class DeleteQuizSet implements FrontLogic {

	final private Logger log = Logger.getLogger( DeleteQuizSet.class );
	
	@Override
	public void action(Protocol p) throws ContentsRoleException
	{
		log.debug( this.getClass().getName() + " FrontLogic Called. "
					+ p.toString());
		
		Integer userId = ProtocolParamChecker.checkUserID(p.request.get(EProtocol.UserID));
		Integer scriptId = ProtocolParamChecker.checkScriptId(p.request.get(EProtocol.ScriptId)); 
		
		// 1. user db에서 스크립트 제거
		final UserManager user = UserManager.getInstance();
		boolean bOK = user.delUserScript(userId, scriptId);
		if( ! bOK ){
			p.setFail(EResultCode.SYSTEM_ERR);
			return;
		}
				

		p.setSuccess();
	}
}

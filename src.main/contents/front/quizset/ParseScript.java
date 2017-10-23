package contents.front.quizset;

import org.apache.log4j.Logger;
import contents.backend.QuizSetDetail;
import contents.backend.QuizSetManager;
import contents.backend.UserManager;
import contents.front.FrontLogic;
import exception.contents.ContentsRoleException;
import net.protocol.EProtocol;
import net.protocol.EResultCode;
import net.protocol.Protocol;
import net.protocol.ProtocolParamChecker;
import net.protocol.Request;


public class ParseScript implements FrontLogic {

	final private Logger log = Logger.getLogger( ParseScript.class );
	
	@Override
	public void action(Protocol protocol) 
	{
		log.debug(this.getClass().getName() + " FrontLogic Called. "); // PDF binary size 때문에 protocol 인자 프린트 안함
		
		final Request req = protocol.request;
		Integer userId = ProtocolParamChecker.checkUserID(req.get(EProtocol.UserID));
		String scriptTitle = ProtocolParamChecker.checkTitle(req.get(EProtocol.ScriptTitle));
		byte[] pdf = ProtocolParamChecker.checkPDF(req.get(EProtocol.SciprtPDF));
		
		final QuizSetManager sm = QuizSetManager.getInstance();
		QuizSetDetail parsedScript;
		try {
			parsedScript = sm.parsePDFScript(scriptTitle, pdf);
		} catch (ContentsRoleException e) {
			if(e.getErrorCode() == EResultCode.SCRIPT__NO_HAS_KR_OR_EN){
				protocol.setFail( EResultCode.SCRIPT__NO_HAS_KR_OR_EN );
			} else if(e.getErrorCode() == EResultCode.NULL_VALUE){
				protocol.setFail( EResultCode.SYSTEM_ERR );
			}
			return;
		}
	
		// 유저의 스크립트 목록에 추가
		final UserManager user = UserManager.getInstance();
		user.addUserHasScriptList( userId, parsedScript );
		
		protocol.response.set(EProtocol.ParsedSciprt, parsedScript.serialize());
		protocol.setSuccess();
	}
}

package contents.front.report;

import org.apache.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;

import contents.backend.ReportManager;
import contents.backend.QuizSet;
import contents.backend.QuizSetDetail;
import contents.backend.QuizSetManager;
import contents.backend.Sentence;
import contents.backend.UserManager;
import contents.front.FrontLogic;
import exception.contents.ContentsRoleException;
import exception.system.MyIllegalStateException;
import net.protocol.EProtocol;
import net.protocol.EResultCode;
import net.protocol.Protocol;
import net.protocol.ProtocolParamChecker;


public class Report_Send implements FrontLogic {

	final private Logger log = Logger.getLogger( Report_Send.class );
	
	@Override
	public void action(Protocol p) throws ContentsRoleException 
	{
		log.debug("SendReport FrontLogic Called. "); 

		Integer userId = ProtocolParamChecker.checkUserID(p.request.get(EProtocol.UserID));
		Integer scriptId = ProtocolParamChecker.checkScriptId(p.request.get(EProtocol.ScriptId)); 
		Integer sentenceId = ProtocolParamChecker.checkSentenceId(p.request.get(EProtocol.SentenceId));
		String sentenceKo = ProtocolParamChecker.checkSentenceText(p.request.get(EProtocol.SentenceKo));
		String sentenceEn = ProtocolParamChecker.checkSentenceText(p.request.get(EProtocol.SentenceEn));
		

		Sentence sentence = new Sentence(sentenceId, scriptId,
										Sentence.NULL_REVISION, sentenceKo, 
										sentenceEn, Sentence.NULL_UPDATED_TIME);
		
		log.debug("Report_Send() sentenceKo:"+sentenceKo +", textKo:"+sentence.textQuestion);
		
		final ReportManager report = ReportManager.getInstance();
		EResultCode resultCode = report.addReportedSentence(userId, sentence);
		if( resultCode.isFail() ){
			switch(resultCode){
				case REPORT_DUPLICATED:
				case USER_SENTENCE_REVISION_IS_OLD_THEN_SYSTEMS:
					// nothing.
					break;
				default:
					p.setFail(EResultCode.SYSTEM_ERR);
					break;
			}
			return;
		}

		p.setSuccess();
	}
}

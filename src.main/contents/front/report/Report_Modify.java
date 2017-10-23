package contents.front.report;

import org.apache.log4j.Logger;

import contents.backend.ReportManager;
import contents.backend.QuizSetManager;
import contents.backend.Sentence;
import contents.front.FrontLogic;
import data.db.DBQueries;
import data.db.DBQueries.DBResult;
import exception.contents.ContentsRoleException;
import net.protocol.EProtocol;
import net.protocol.EResultCode;
import net.protocol.Protocol;
import net.protocol.ProtocolParamChecker;


public class Report_Modify implements FrontLogic {

	final private Logger log = Logger.getLogger( Report_Modify.class );
	
	@Override
	public void action(Protocol p) throws ContentsRoleException 
	{
		log.debug( this.getClass().getName() 
				+ " FrontLogic Called. "
				+ p.toString());

		Integer userId = ProtocolParamChecker.checkUserID(p.request.get(EProtocol.UserID));
		Integer modifyType = ProtocolParamChecker.checkIntDefault(p.request.get(EProtocol.ReportModifyType));
		Integer scriptId = ProtocolParamChecker.checkScriptId(p.request.get(EProtocol.ScriptId));
		Integer sentenceId = null;
		String sentenceKo = null;
		String sentenceEn = null;
		
		if( modifyType == ReportManager.MODIFY_DEL ) 
		{
			sentenceId = ProtocolParamChecker.checkSentenceId(p.request.get(EProtocol.SentenceId));
		}
		else if( modifyType == ReportManager.MODIFY_ADD )
		{
			sentenceKo = ProtocolParamChecker.checkSentenceText(p.request.get(EProtocol.SentenceKo));
			sentenceEn = ProtocolParamChecker.checkSentenceText(p.request.get(EProtocol.SentenceEn));
		}
		else if( modifyType == ReportManager.MODIFY_UPDATE )
		{
			sentenceId = ProtocolParamChecker.checkSentenceId(p.request.get(EProtocol.SentenceId));
			sentenceKo = ProtocolParamChecker.checkSentenceText(p.request.get(EProtocol.SentenceKo));
			sentenceEn = ProtocolParamChecker.checkSentenceText(p.request.get(EProtocol.SentenceEn));
		}
		
		// Only Admin Can Modify. 
		final ReportManager report = ReportManager.getInstance();
		if( false == report.checkAdmin(userId) ){
			p.setFail(EResultCode.NOT_AUTHORITY);
			return;
		}
		
		Sentence modifyResult = null;
		try 
		{	
			if( modifyType == ReportManager.MODIFY_DEL ) 
			{
				report.delSentence( scriptId, sentenceId );
			} 
			else if( modifyType == ReportManager.MODIFY_ADD ) 
			{
				modifyResult = report.addSentence(scriptId, sentenceKo, sentenceEn);
			} 
			else if( modifyType == ReportManager.MODIFY_UPDATE )
			{
				modifyResult = report.modifyReportedSentence( scriptId, sentenceId, sentenceKo, sentenceEn);
			} 
			else 
			{
				p.setFail(EResultCode.UNKNOUN_ERR);
				return;
			}
		} 
		catch ( ContentsRoleException e )
		{
			switch(e.getErrorCode()){
				case INVALID_SENTENCE_ID:
				case INVALID_SENTENCE_TEXT:
				case REPORT_MEMORY_UPDATE_FAIL:
					p.setFail(e.getErrorCode());
					break;
				default:
					p.setFail(EResultCode.SYSTEM_ERR);
					break;
			}
			return;
		}
		
	
		// set result.
		if( modifyType == ReportManager.MODIFY_DEL ) 
		{
			// nothing
		}
		else if( modifyType == ReportManager.MODIFY_ADD )
		{
			p.response.set(EProtocol.SentenceId, modifyResult.id);
			p.response.set(EProtocol.SentenceKo, modifyResult.textQuestion);
			p.response.set(EProtocol.SentenceEn, modifyResult.textAnswer);
		}
		else if( modifyType == ReportManager.MODIFY_UPDATE )
		{
			p.response.set(EProtocol.SentenceKo, modifyResult.textQuestion);
			p.response.set(EProtocol.SentenceEn, modifyResult.textAnswer);
		}
		
		p.setSuccess();
	}
}

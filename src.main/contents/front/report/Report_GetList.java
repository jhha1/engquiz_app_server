package contents.front.report;

import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

import contents.backend.Report;
import contents.backend.ReportManager;
import contents.backend.QuizSetManager;
import contents.front.FrontLogic;
import exception.contents.ContentsRoleException;
import net.protocol.EProtocol;
import net.protocol.ObjectBundle;
import net.protocol.Protocol;


public class Report_GetList implements FrontLogic {

	final private Logger log = Logger.getLogger( Report_GetList.class );
	
	@Override
	public void action(Protocol protocol) throws ContentsRoleException 
	{
		log.debug( this.getClass().getName() 
				+ " FrontLogic Called. "
				+ protocol.toString()); 

		int reportCount = 10;
		final ReportManager report = ReportManager.getInstance();
		int reportCounts = report.getReportsCount();
		List<Report> reports = report.getReportsByReportTimeDesc(reportCount);
		if( reports == null || reports.isEmpty() ){
			log.info("No Reports.");
			protocol.setSuccess();
			return;
		} 
		
		List<String> serializedReports = new LinkedList<>();
		for(Report r : reports){
			ObjectBundle serializedReport = new ObjectBundle();
			serializedReport.setInt(Report.Field_SCRIPT_ID, r.getScriptId());
			serializedReport.setInt(Report.Field_SENTENCE_ID, r.getSentenceId());
			serializedReport.setString(Report.Field_TEXT_KO, r.getTextKo());
			serializedReport.setString(Report.Field_TEXT_EN, r.getTextEn());
			serializedReports.add(serializedReport.serialize());
		}
		protocol.response.set(EProtocol.ReportCountAll, reportCounts);
		protocol.response.set(EProtocol.ReportList, serializedReports);
		protocol.setSuccess();
	}
}

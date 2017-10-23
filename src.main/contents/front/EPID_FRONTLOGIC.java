package contents.front;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum EPID_FRONTLOGIC 
{	
	Dummy ( new Pair(1000, contents.front.Dummy.class) ),
	User_CheckExist ( new Pair(1001, contents.front.user.CheckExistUser.class) ),
	User_SignIn ( new Pair(1002, contents.front.user.SignIn.class) ),
	User_LogIn ( new Pair(1003, contents.front.user.LogIn.class) ),
	
	Sciprt_ParsePDF ( new Pair(1100, contents.front.quizset.ParseScript.class) ),
	
	DeleteScript( new Pair(1201, contents.front.quizset.DeleteQuizSet.class) ),
	
	
	
	
	
	
	
	Report_GetList( new Pair(5001, contents.front.report.Report_GetList.class) ),
	Report_Send( new Pair(5002, contents.front.report.Report_Send.class) ),
	Report_Modify( new Pair(5003, contents.front.report.Report_Modify.class) ),
	
	Sync ( new Pair(3001, contents.front.sync.Sync.class) ),
	Sync_SendResult ( new Pair(3002, contents.front.sync.Sync_FailedClient.class) ),
	
	
	NULL( new Pair(-1, Class.class) );

	
	
	private static Map<Integer, EPID_FRONTLOGIC> lookup = null; 
	static 
	{
		lookup = new HashMap<Integer, EPID_FRONTLOGIC>();
		for( EPID_FRONTLOGIC e : EnumSet.allOf(EPID_FRONTLOGIC.class) )
		    lookup.put( e.getPID(), e );
	}
	
	private Pair value;
	
	private EPID_FRONTLOGIC( Pair value ) 
	{
		this.value = value;
	}
	
	public Integer getPID() 
	{
		return value.getPID();
	}
	
	public String getFrontLogicName() 
	{
		return value.getLogicName();
	}
	
	public static EPID_FRONTLOGIC toEnum( Integer key )
	{
		return lookup.getOrDefault(key, NULL);
	}
}


class Pair 
{
	private Integer pid;
	private String logicname;
	
	public Pair( Integer pid, Class<?> logic )
	{
		this.pid = pid;
		this.logicname = logic.getName();
	}
	
	public Integer getPID() { return pid; }
	public String getLogicName() { return logicname; }
};

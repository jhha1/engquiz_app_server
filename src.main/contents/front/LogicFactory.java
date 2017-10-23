package contents.front;

import org.apache.log4j.Logger;

import exception.system.MyIllegalArgumentException;
import exception.system.MyIllegalStateException;
import net.protocol.EResultCode;

public class LogicFactory 
{
	@SuppressWarnings("unused")
	final private static Logger log = Logger.getLogger( LogicFactory.class );
	
	private LogicFactory(){}
	
	public static FrontLogic createFrontLogic( Integer pid ) throws MyIllegalArgumentException, 
																	MyIllegalStateException
	{
		String logicName = findLogicnameByPID( pid );
		FrontLogic logic = createLogicClass( logicName );
			
		return logic;
	}
	
	private static String findLogicnameByPID( Integer pid ) 
	{
		EPID_FRONTLOGIC eLogic = EPID_FRONTLOGIC.toEnum( pid );
		if( eLogic == EPID_FRONTLOGIC.NULL ) {
			throw new MyIllegalArgumentException(EResultCode.INVALID_PID, "Invalid PID:" + pid);
		}
		
		return eLogic.getFrontLogicName();
	}
	
	private static FrontLogic createLogicClass( String logicClassName ) 
	{
		try 
		{
			Class<?> cls = Class.forName( logicClassName );
			return (FrontLogic) cls.newInstance(); 
		} 
		catch ( ClassNotFoundException |
				InstantiationException | 
				IllegalAccessException e) 
		{
			throw new MyIllegalStateException( EResultCode.SYSTEM_ERR, 
											"Failed Create Logic: " + logicClassName, e );
		}
	}
}
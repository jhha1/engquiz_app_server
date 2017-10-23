package contents.front;

import net.protocol.Protocol;

import org.apache.log4j.Logger;

import contents.backend.User;
import contents.backend.UserManager;
import exception.contents.ContentsRoleException;


public class Dummy implements FrontLogic {

	final private Logger log = Logger.getLogger( Dummy.class );
	
	@Override
	public void action(Protocol protocol) throws ContentsRoleException 
	{
		log.debug("Dummy FrontLogic Called. "
				+ protocol.toString());

		protocol.setSuccess();
	}
}

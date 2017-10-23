package contents.front.user;

import org.apache.log4j.Logger;

import contents.backend.User;
import contents.backend.UserManager;
import contents.front.FrontLogic;
import net.protocol.EProtocol;
import net.protocol.EResultCode;
import net.protocol.Protocol;
import net.protocol.ProtocolParamChecker;


public class CheckExistUser implements FrontLogic {

	final private Logger log = Logger.getLogger( CheckExistUser.class );
	
	/* (non-Javadoc)
	 * @see contents.front.FrontLogic#action(net.protocol.Protocol)
	 */
	@Override
	public void action(Protocol p) throws Exception 
	{
		log.debug( this.getClass().getName() + " FrontLogic Called. "
				+ p.toString());
		
		String username = ProtocolParamChecker.checkUsername(p.request.get(EProtocol.UserName));
		Integer userID = -1;
		username = username.toLowerCase();

		final UserManager userManager = UserManager.getInstance();
		Boolean bExistedUser = userManager.isExistUser( username );
		if( ! bExistedUser ) {
			p.response.set(EProtocol.IsExistedUser, bExistedUser);
			p.setSuccess();
			return;
		}
		
		User user = userManager.getUserByName(username);
		if(User.isNull(user)){
			p.setFail(EResultCode.SYSTEM_ERR);
			return;
		}
		userID = user.userID;		// NullPointerException이 날 경우, catch에서 받아, 상위 class에서 logging and respond client 'SystemError'
		p.response.set(EProtocol.IsExistedUser, bExistedUser);
		p.response.set(EProtocol.UserName, username);
		p.response.set(EProtocol.UserID, userID);
		p.setSuccess();
	}
}

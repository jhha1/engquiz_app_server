package contents.front.user;

import org.apache.log4j.Logger;

import contents.backend.User;
import contents.backend.UserManager;
import contents.front.FrontLogic;
import exception.contents.ContentsRoleException;
import exception.system.MyIllegalStateException;
import net.protocol.EProtocol;
import net.protocol.EResultCode;
import net.protocol.Protocol;
import net.protocol.ProtocolParamChecker;


public class SignIn implements FrontLogic {

	final private Logger log = Logger.getLogger( SignIn.class );
	
	@Override
	public void action(Protocol p)
	{
		log.debug( this.getClass().getName() + " FrontLogic Called. "
				+ p.toString());

		// sign in은 username을 protocolchecker에게 안맡기고 별도로 체크.
		Object username = p.request.get(EProtocol.UserName);
		String usernameString = (username==null) ? new String() : (String)username;
		//usernameString = usernameString.toLowerCase();
		
		final UserManager userManager = UserManager.getInstance();
		EResultCode resultCode = userManager.signIn( usernameString );
		if( resultCode.isFail() ){
			switch(resultCode){
				case INVALID_USERNAME:		// role에 맞는 이름을 다시 입력해야함
				case USERNAME_DUPLICATED:
					p.setFail(resultCode);
					break;
				default:
					p.setFail(EResultCode.SYSTEM_ERR);
					break;
			}
			return;
		}

		User newUser = userManager.getUserByName(usernameString);
		boolean bNoSelected = User.isNull(newUser);
		if( bNoSelected ){
			log.error("Created User Well. But NoSelected in DB. userName(" + usernameString +")");
			p.setFail(EResultCode.USER_CREATED_BUT_FAILED_GET_USERINFO);
			return;
		}
		
		
		p.response.set(EProtocol.UserID, newUser.userID);
		p.response.set(EProtocol.UserName, newUser.userName);
		p.setSuccess();
	}
}

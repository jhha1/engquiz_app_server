package contents.front;

import exception.contents.ContentsRoleException;
import net.protocol.Protocol;

public interface FrontLogic {
	
	public void action( Protocol protocol ) throws Exception;
	
}

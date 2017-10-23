package app;

import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import contents.backend.QuizSetManager;
import utils.FileHelper;

public class Initailizer {
	final static Logger log = Logger.getLogger(Initailizer.class);
	
	private static Initailizer instance = new Initailizer();
	private static Boolean bInitalized = false;
	
	static boolean DEBUG_MODE = false; 
	
	private Initailizer() {}
	
	public static Initailizer getInstance() {
		return instance;
	}
	
	public static boolean isDubugMode(){
		return DEBUG_MODE;
	}
	
	public void init()
	{	
		if(bInitalized) {
			System.out.println("[Warn] already initalized.");
			return;
		}
		
		System.out.println("////////////////////////////////////////////////////////////////////////");
		System.out.println("////////////////////////////////////////////////////////////////////////");
		System.out.println("//////////////// START - Server (" + 
		(isDubugMode()? " DEBUG MODE " : " RELEASE MODE " )+ ") ///////////////////////");
		System.out.println("////////////////////////////////////////////////////////////////////////");
		System.out.println("////////////////////////////////////////////////////////////////////////");
		//test.TestMain.parseAllPDF_Then_overwriteToTextFiles();
		
		//Map<String, String> loadedFiles = loadQuizTextFiles();
		QuizSetManager.getInstance().initailize();
		
		System.out.println("//////////////// Server Init Finished ////////////////////");
		
		bInitalized = true;
	}
	
	// TODO 
	// 리눅스 상에서 -> 톰캣 destory시에  new 된 memory 삭제 되는지 확인 : 톰캣 종료 안될수 있다.
	// 애플리케이션이 사용하는 memory가 톰캣위에 할당되는데, 톰캣 내릴때 애플리케이션 memory 해제가 안되있으면,, 톰캣이 종료되지 않음.
	public void destroy() 
	{
		System.out.println("/////////////////////////////////////////////////////");
		System.out.println("//////////////// DESTORY Server ////////////////////");
		System.out.println("/////////////////////////////////////////////////////");
		
		instance = null;
	}
	
	private Map<String,String> loadQuizTextFiles()
	{
		final FileHelper fm = FileHelper.getInstance();
		List<String> fileNames = fm.readFileListInDirectory( FileHelper.FilePathText );
		return fm.readTextFiles( fileNames );
	}

}

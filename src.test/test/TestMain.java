package test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import app.Initailizer;
import contents.backend.QuizSet;
import contents.backend.QuizSetDetail;
import contents.backend.QuizSetManager;
import contents.backend.Sentence;
import contents.front.user.LogIn;
import exception.contents.ContentsRoleException;
import net.protocol.EResultCode;
import utils.FileHelper;
import utils.Parsor;


public class TestMain {
	
	final private static Logger log = Logger.getLogger( TestMain.class );
	
	public static void main(String[] args) 
    {      
		
		String scriptTitle = "132) UNIT 69-11 a cake, some cake, some cakes (countable, uncountable 2) (with answers)-111.pdf";
		
		String REGEX_DOUBLE_DOWNLOAD_NUMBER = "-([0-9]{1,3}).pdf";
		
		scriptTitle = replaceLast(scriptTitle, REGEX_DOUBLE_DOWNLOAD_NUMBER, "");
		scriptTitle = replaceLast(scriptTitle, ".pdf", "");
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@ "+scriptTitle);
		
		try {
			batch_pdf2DB();
		} catch (Exception e){
			e.printStackTrace();
		}
    }

	public static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }
	
	// PDF 전체를 quiz format의  text 파일로 변환. 
		public static void batch_pdf2DB() 
		{
			/*
			final FileHelper fm = FileHelper.getInstance();
			final ScriptManager sm = ScriptManager.getInstance();
			List<String> fileNames = fm.readFileListInDirectory( FileHelper.FilePathPDF );
			for(String filename : fileNames)  
			{
				String fullFilePath = FileHelper.FilePathPDF + filename;
		    	PDDocument pdf = fm.readPDF(fullFilePath);
		    	if(pdf == null) {
		    		System.out.println("[ERROR] pdfDocument is null. filename["+ fullFilePath +"]");
		    		continue;
		    	}
		    	
		    	String title = filename;
				
		    	List<Sentence> sentences = new Parsor().parsePDF(pdf);
		    	if (sentences == null || sentences.isEmpty()) {
		    		System.out.println("[ERROR] parsedSentences is null. scriptTitle:" + title);
					continue;
				}
				
				boolean bNoHas = sm.checkSentences_no_has_korean_or_enlgish(sentences);
				if( bNoHas ){
					System.out.println("[ERROR] checkSentences_no_has_korean_or_enlgish. scriptTitle:" + title);
					continue;
				}
				
				ScriptDetail newScript = sm.createAndAddScript(title, sentences);
				if( newScript != null)
					System.out.println("SUCCESS script added: "+ newScript.title);
				else
					System.out.println("FAIL script added: "+ title);
			}
			*/
	
		}

	// PDF 전체를 quiz format의  text 파일로 변환. 
	public static void batcg_pdf2file() 
	{
		final FileHelper fm = FileHelper.getInstance();
		List<String> fileNames = fm.readFileListInDirectory( FileHelper.FilePathPDF );
		Map<String, PDDocument> readFiles = fm.readPDFFiles(fileNames);
		Map<String, QuizSet> scriptMap = new HashMap<String, QuizSet>();
		
		int scriptIndex = 0;
		int scriptRevision = 0;
		for(Entry<String, PDDocument> e: readFiles.entrySet()) 
		{
			String title = e.getKey();
			PDDocument pdf = e.getValue();
			
	    	List<Sentence> sentences = new Parsor().parsePDF(pdf);
	    	/*
	    	Script script = new Script( title, scriptIndex++, 
	    								sentences );
	    	
	    	if(false == scriptMap.containsKey(title))
	    		scriptMap.put(title, script);
	    	
	    	String filePath = FileManager.FilePathText + title + ".txt";
	    	fm.overwrite(filePath, script.toString());*/
		}
	}
}

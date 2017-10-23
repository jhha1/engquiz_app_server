package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.pdfbox.pdmodel.PDDocument;

import contents.backend.QuizSet;


public class FileHelper {
	
	private static FileHelper instance = new FileHelper();
	
	public static final String FILE_ABSOULUTE_PATH = "F:\\Users\\jhha\\Projects\\English\\sda_grammer\\";
	//public static final String FILE_ABSOULUTE_PATH = "D:\\Users\\jhha\\Projects\\git\\engquiz_server\\resource\\";

	public static final String FilePathPDF = FileHelper.FILE_ABSOULUTE_PATH + "pdf\\";
	//public static final String FilePathText = FileManager.FILE_ABSOULUTE_PATH + "parsedScripts\\";
	//public static final String FilePathPDF = FILE_ABSOULUTE_PATH + "pdftest\\";
	public static final String FilePathText = FILE_ABSOULUTE_PATH + "texttest\\";
	public static final String PathMacID = FILE_ABSOULUTE_PATH + "macIds.txt";
	
	private FileHelper() {}

	public static FileHelper getInstance() {
		return instance;
	}
    
    public void saveQuizFiles( Map<String, QuizSet> files ) 
    {
    	for(Entry<String, QuizSet> file: files.entrySet()) 
		{
    		QuizSet quiz = file.getValue();
    		String filePath = FileHelper.FilePathText + quiz.title + ".txt";
    		String fileData = quiz.toString();
	    	overwrite( filePath, fileData );
		}
    	
    	List<String> filelist = readFileListInDirectory(FileHelper.FilePathText);
    	fileLog("text saved", filelist, files);
    }
    
    public String readText(String textFileName)
	{
		StringBuilder contentReceiver = new StringBuilder();
		try {
	        BufferedReader bufferedTextFileReader = new BufferedReader(new FileReader(textFileName));
	        char[] buf = new char[2048];  
	        
	        while (bufferedTextFileReader.read(buf) > 0) {
	            contentReceiver.append(buf);
	        } 
		} catch (IOException e) {
			e.printStackTrace();
		}
		return contentReceiver.toString();
    }
	
	public Map<String,String> readTextFiles( List<String> fileNames )
	{
		Map<String, String> readFiles = new HashMap<String, String>();
	
		for(String filename : fileNames)  
		{
			String fullFilePath = FileHelper.FilePathText + filename;
			String text = this.readText(fullFilePath);
			readFiles.put(filename, text);
		}
		fileLog("text read",fileNames, readFiles);
		
		return readFiles;
	}
    
	public PDDocument readPDF(String textFileName) 
	{
        PDDocument pdDocument = null;
        try {
        		System.out.println("[DEBUG] Stating ReadPDF["+ textFileName +"]");
               File targetFile = new File(textFileName);
               pdDocument = PDDocument.load(targetFile);
               
        } catch (IOException e) {
                e.printStackTrace();
        }
        return pdDocument;
	}
    
    public Map<String, PDDocument> readPDFFiles( List<String> fileNames ) 
    {
    	Map<String, PDDocument> readFiles = new HashMap<String, PDDocument>();
   
		for(String filename : fileNames)  
		{
			String fullFilePath = FileHelper.FilePathPDF + filename;
	    	PDDocument pdf = readPDF(fullFilePath);
	    	if(pdf == null) {
	    		System.out.println("[ERROR] pdfDocument is null. filename["+ fullFilePath +"]");
	    		continue;
	    	}
	    
	    	readFiles.put(filename, pdf);
	    	
	    	try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		fileLog("pdf read", fileNames, readFiles);
		
		return readFiles;
    }
    
	
	
	public int getFileCountInDirectory(String directoryPath) 
	{
		File directory = new File(directoryPath);
		if(directory.exists() && null != directory.listFiles())
			return directory.listFiles().length;
		else 
			return 0;
	}
	
	public List<String> readFileListInDirectory(String directoryPath) 
	{
		File directory = new File(directoryPath);
		if(false == directory.exists())
			return Collections.EMPTY_LIST;
		
		List<String> filelist = new LinkedList<String>();
		for(File file : directory.listFiles())
		{
			filelist.add(file.getName());
		}
	    return filelist;
	}
	
	// 파일이 존재하면 덮어씀
	public void overwrite(String filePath, String data)
	{
		System.out.println("[DEBUG] OverWRITE TextFile. path["+filePath+"] .");
		
		File file = new File(filePath);
		if(file.exists()) {
			Boolean bDeleted = file.delete();	
			if(false == bDeleted) {
				System.out.println("["+filePath+"] " + "delete failed.");
				return;
			}
		}
		
		write(filePath, data);
	}
	
	private Boolean delete(String filename)
	{
		Boolean bDeleted = false;
		File file = new File(filename);
		if(file.exists()) {
			bDeleted = file.delete();	
		}
		return bDeleted;
	}
	
	private void write(String filepath, String text)
	{
        try{
                         
            // BufferedWriter 와 FileWriter를 조합하여 사용 (속도 향상)
            BufferedWriter fw = new BufferedWriter(new FileWriter(filepath, true));
             
            //System.out.println("wirte["+fileName+"] " + text);
            // 파일안에 문자열 쓰기
            fw.write(text);
            fw.flush();
 
            // 객체 닫기
            fw.close(); 
        
        } catch(Exception e){
            e.printStackTrace();
        }
	}
    /*
    private void addQuizsetIfNoExsit()
    {
    	List<String> pdfFileNames = FileManager.instance.readFileListInDirectory(Const.FilePathPDF);
    	List<String> parsedFileNames = FileManager.instance.readFileListInDirectory(Const.FilePathText);
    	for(String pdfFileName : pdfFileNames) 
		{	
    		if(parsedFileNames.contains(pdfFileName + ".txt"))
    			continue;
    		
    		uploadPDF(Const.FilePathPDF, pdfFileName);
    		
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
    }*/
    
	public Properties readPropertiesXML( String propertiesFilePath ) 
	{
		try {
			File file = new File( propertiesFilePath );
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.loadFromXML(fileInput);
			fileInput.close();
			
			return properties;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
   
    private void fileLog(String msg, List filelist, Map files) 
	{
		System.out.println("[INFO] FileLog: " + msg
				+ "(FileCntInDir:"+filelist.size()
				+ ",FileCntInMemory:"+files.size() +")"
				);
	}


}

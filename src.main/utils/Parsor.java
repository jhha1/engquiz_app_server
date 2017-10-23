package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import contents.backend.QuizSet;
import contents.backend.QuizSetDetail;
import contents.backend.Sentence;


public class Parsor {

	final private Logger log = Logger.getLogger( Parsor.class );
	
	//public static final String QuizUnitSeperator = "@@@";
	public static final String QuizUnitSeperator = "\n--\n";
	
	public Parsor(){}
	
	public static String parseTitle( String title ){
		return TitleParsor.parse( title );
	}
	
	public QuizSet parseText( String fileName, String textdump ){
		return new TextParsor().parse( fileName, textdump );
	}
	
	public List<Sentence> parsePDF( byte[] pdfBinary ){
		return new PDFParsor().parse( pdfBinary );
	}
	
	public List<Sentence> parsePDF( PDDocument pdDocument ){
		return new PDFParsor().parse( pdDocument );
	}
	
	public List<Sentence> parseDocx( FileInputStream fis ){
		return new DocxParser().parse( fis );
	}
	
}

class TitleParsor
{
	
	private static final String REGEX_DOUBLE_DOWNLOAD_NUMBER = "-([0-9]{1,2}).pdf";  // -(1 ~ 99) 까지  식제
	private static final String REGEX_EXTENSION_PDF = ".pdf";
	
	public static String parse(String scriptTitle){
		if( StringHelper.isNull(scriptTitle) )
			return scriptTitle;
		
		scriptTitle = removeTagOfDoubleDownload(scriptTitle);
		return removeExtansion(scriptTitle, REGEX_EXTENSION_PDF);
	}
	
	public static String removeExtansion(String scriptTitle, String extension){
		if( StringHelper.isNull(scriptTitle) )
			return scriptTitle;

		// .pdf 삭제
		return replaceLast(scriptTitle, extension, "");
	}
	
	/*
	 * kakao talk 에서 script double 시에  붙는 tag 삭제
	 * -1 ~ -99 까지  식제
	 * ex) 
	 * 	UNIT 58-1 scriptTitle.pdf  <- normal
	 * 	UNIT 58-1 scriptTitle-1.pdf  <- double download tagging. 
	 * 
	 * script title 다듬는 이유
	 * parse script에서는 제목으로 스크립트를 구분하기 때문.
	 * 		-> 동일 스크립트가 제목이 달라셔 다른것으로 인지, 다른 scriptId를 가짐.
	 * 		-> 하여, sentence 수정시 해당 scriptID에 해당하는 것만 수정됨  <- 가장 큰 이유!!!
	 */
	public static String removeTagOfDoubleDownload(String scriptTitle){
		if( StringHelper.isNull(scriptTitle) )
			return scriptTitle;

		// .pdf 까지 삭제됨
		return replaceLast(scriptTitle, REGEX_DOUBLE_DOWNLOAD_NUMBER, "");
	}
	
	/*
	 * replace the last String.. matched pattern  
	 * 기능이 업어서 인터냇애서 긁어옴
	 */
	public static String replaceLast(String text, String regex, String replacement) {
		if( StringHelper.isNull(text) )
			return text;
		
        return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }
}

class TextParsor
{
	final private Logger log = Logger.getLogger( TextParsor.class );
	public TextParsor() {}
	
	public QuizSetDetail parse( String fileName, String textdump ) 
	{
    	if(StringHelper.isNull(textdump)) { 
    		log.error("Invalid file[" + fileName +"]. file is NULL or empty");
    		return null;
    	}
    
    	try {
			String rows[] = textdump.split(Parsor.QuizUnitSeperator);
			if(rows.length <= 4) {
				log.error("Invalid file[" + fileName +"]. "
						+ "file rows is too short["+rows.length+"]" );
				return null;
			}
			
			log.debug("FileName[" + fileName +"], total splited len["+rows.length+"]"
					+ "index["+rows[0]+"] len["+rows[0].length()+"]"
					+ "/ revision["+rows[1]+"] len["+rows[1].length()+"]"
					+ "/ title["+rows[2]+"],len["+rows[2].length()+"]");
		
			Integer iScriptIndex = Integer.parseInt( rows[0].trim() );			
			int iScriptRevision = Integer.parseInt( rows[1] );
			String scriptTitle = rows[2];
			LinkedList<Sentence> parsedSentences = parseSentences( rows );
			if( parsedSentences == null ) {
				return null;
			}
			
	    	return new QuizSetDetail(iScriptIndex, scriptTitle, parsedSentences);
	    	
    	} catch (Exception e) {
    		log.error("Parse ERROR. fileName["+fileName+"]" );
    		e.printStackTrace();
    		return null;
    	}	
	}
	
	private LinkedList<Sentence> parseSentences( String rows[] ) 
	{
		if( rows == null ) return null;
		if( rows.length <= 0 ) return null;
		
		LinkedList<Sentence> parsedSentences = new LinkedList<Sentence>();
    	for(int i=3; i<rows.length; ++i)
    	{
    		String row = rows[i];
    		if(row.isEmpty())
    			continue;
    		
    		String[] dividedRow = row.split("\t");
    		if(dividedRow.length != 2)
    			continue;
    		
    		Sentence unit = new Sentence();
    		unit.textQuestion = dividedRow[0].trim();
    		unit.textAnswer = dividedRow[1].trim();
    		parsedSentences.add(unit);
    	}
    	
    	return parsedSentences;
	}
}



class DocxParser
{
	final private Logger log = Logger.getLogger( DocxParser.class );
	
	private static final String REGEX_NUMBER = "^([0-9]{1,3})\\.";
	private static final String REGEX_KO_SENTENCE = "^.*[가-힣].+$";
	private static final String REGEX_EN_SENTENCE = ".*[^가-힣].*[a-zA-Z].*"; 
	private static final String REGEX_REGACY1 = ".*(Symptoms).*";
	private static final String REGEX_REGACY2 = ".*(Make your own sentences using the expressions above.).*";
	
	private static final Pattern pattern_number = Pattern.compile(REGEX_NUMBER);
	private static final Pattern pattern_ko = Pattern.compile(REGEX_KO_SENTENCE);
	private static final Pattern pattern_en = Pattern.compile(REGEX_EN_SENTENCE);
	private static final Pattern pattern_regacy1 = Pattern.compile(REGEX_REGACY1);
	private static final Pattern pattern_regacy2 = Pattern.compile(REGEX_REGACY2);
	
	public DocxParser(){}
	
	public List<Sentence> parse( FileInputStream fis ) 
	{
		List<Sentence> extractedSentences = null;
        try
        {
            XWPFDocument docx = new XWPFDocument( fis );
            XWPFWordExtractor we = new XWPFWordExtractor(docx);
            log.debug("DOCX textdump : " + we.getText());
                  
            extractedSentences = parseTable( docx.getTables() );
            
            // logging
            log.debug("DOCX Parsed. list len[" + extractedSentences.size() +"]" );
            for( Sentence s : extractedSentences){
            	log.debug("Parsed DOCX [" +s.textQuestion+"]["+s.textAnswer+"]");
            }
            
            return extractedSentences;
        }
        catch (Exception exep)
        {
            exep.printStackTrace();
        }
        return null;
	}
	
	private List<Sentence> parseTable( List<XWPFTable> table ) 
	{
		List<Sentence> extractedSentences = new LinkedList<Sentence>();
		
		for (XWPFTable xwpfTable : table) 
		{ 
            List<XWPFTableRow> row = xwpfTable.getRows(); 
            for (XWPFTableRow xwpfTableRow : row) 
            { 
            	Map<String, String> sentencePair = parseRow( xwpfTableRow );
            	if( sentencePair == null ) {
            		System.out.println("!!!!!!! extractedSentencePair is null"); 
                	continue;
            	}
            	
            	// 정상. 스크립트의 빈 줄이나, 작문란 등이 여기에 해당 
            	if( sentencePair.isEmpty() )
            		continue;
            	
            	String koSentence = sentencePair.get("ko");
    			String enSentence = sentencePair.get("en");
    			Sentence sentence = new Sentence();
    			sentence.textQuestion = koSentence;
    			sentence.textAnswer = enSentence;
    			extractedSentences.add(sentence);
            } 
        }
		return extractedSentences;
	}
	
	// (row 구성:     12. 한글문장  영어문장  )
	private Map<String, String> parseRow( XWPFTableRow xwpfTableRow ) 
	{
		
		
		String what = "";
		String lastTextForlog = ""; 
		Map<String, String> extractedSentencs = new HashMap<String, String>();
		List<XWPFTableCell> cells = xwpfTableRow.getTableCells(); 
        for (XWPFTableCell xwpfTableCell : cells) { 
            if( xwpfTableCell == null ) { 
            	System.out.println("!!!!!!! Cell is null"); 
            	continue;
            }
            
            System.out.println("!! "+ what + " !! " + lastTextForlog); 
            
            String text = xwpfTableCell.getText();
            lastTextForlog = text;
            
            if( StringHelper.isNull(text) ) {
            	what = "null";
            	continue;
            }
            // regacy 필터링이 ko,en 보다 먼저 와야함
            if( pattern_regacy1.matcher(text).find() ) {
            	what = "regacy1";
            	continue;
            }	
            if( pattern_regacy2.matcher(text).find() ) {
            	what = "regacy2";
            	continue;
            }	
            if( pattern_number.matcher(text).find() ) {
            	what = "number";
            	continue;
            }
            if( pattern_ko.matcher(text).find() ) {
            	extractedSentencs.put("ko", text);
            	what = "ko";
            	continue;
            }
            if( pattern_en.matcher(text).find() ) {
            	what = "en";
            	extractedSentencs.put("en", text);
            	continue;
            }	
                  
        } 
         
        return extractedSentencs;
	}
}

// TODO 한글만 있는 스크립트 예외처리. 클라에서 메세지 처리 가능하게.
class PDFParsor
{ 
	class SentenceBuffer{
		private static final int BUF_CAPATITY = 100;
		StringBuffer ko = new StringBuffer(BUF_CAPATITY);
		StringBuffer en = new StringBuffer(BUF_CAPATITY);
	}
	
	private LinkedList<SentenceBuffer> parsedSentences = new LinkedList<SentenceBuffer>();
	
	private Sentence.TYPE LastParsingFlag = Sentence.TYPE.NONE;
	
	private static final String REGEX_KO_SENTENCE1 = "^([0-9]{1,3})\\.\\s.*[가-힣a-zA-Z].*"; // "�ִ� 3�ڸ�����,".",�����̽��� ������� �� �־����.�� �ڷ� �ѱۿ��������Ե� �����̿�����. 
	private static final String REGEX_KO_SENTENCE2 = "^.*[가-힣].+$";  						// �ѱ��� ���� �Ǿ� �� (���� ������� �ɸ����������� �߰��� ���� ���� ���ο� �ִ� ���)
	private static final String REGEX_EN_SENTENCE = ".*[^가-힣].*[a-zA-Z].*";  				// �ѱ����Ծȵǰ� �����ڴ����ԵǾ�� ��.
	private static final String REGEX_MAINTITLE = ".*(Inter\\. UNIT ).*";  
	private static final String REGEX_SUBTITLE = ".*[-|+].*";
	private static final String REGEX_REGACY1 = ".*(Symptoms).*";
	private static final String REGEX_REGACY2 = ".*(idioms).*";
	private static final String REGEX_REGACY3 = ".*(Make your own sentences using the expressions above.).*";
	
	private static final Pattern pattern_ko1 = Pattern.compile(REGEX_KO_SENTENCE1);
	private static final Pattern pattern_ko2 = Pattern.compile(REGEX_KO_SENTENCE2);
	private static final Pattern pattern_en = Pattern.compile(REGEX_EN_SENTENCE);
	private static final Pattern pattern_mainTitle = Pattern.compile(REGEX_MAINTITLE);
	private static final Pattern pattern_subTitle = Pattern.compile(REGEX_SUBTITLE);  
	private static final Pattern pattern_regacy1 = Pattern.compile(REGEX_REGACY1);
	private static final Pattern pattern_regacy2 = Pattern.compile(REGEX_REGACY2); 
	private static final Pattern pattern_regacy3 = Pattern.compile(REGEX_REGACY3); 
	
	public PDFParsor(){}
	
	public List<Sentence> parse( byte[] pdfBinary ) 
	{
		String textdump = extractingText( pdfBinary );
		extractingSentences( textdump );  
		
		return convertSentenceObjects( this.parsedSentences );
	}
	
	public List<Sentence> parse( PDDocument pdDocument ) 
	{
		String textdump = extractingText( pdDocument );
		extractingSentences( textdump );  
		
		return convertSentenceObjects( this.parsedSentences );
	}

	private String extractingText( byte[] pdfBinary )
	{
		PDDocument pdDocument = null;
		try {
			pdDocument = PDDocument.load(pdfBinary);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}	
		
		return extractingText( pdDocument );
	}
	
	private String extractingText( PDDocument pdDocument )
	{
		if(pdDocument == null) {
    		System.out.println("[ERROR] pdDocument is null.");
    		return new String();
    	}
		
		String extractedText = new String();   
		PDFTextStripper stripper = null;
		try {        
               stripper = new PDFTextStripper();
              
		} catch (IOException e) {
               System.out.println("TextExtraction-extractingText ERROR: PDFTextStripper ��ü���� ����");
		}
      
		stripper.setStartPage(1);
		try {
    	   extractedText = stripper.getText(pdDocument);
              
		} catch (IOException e) {
              
               System.out.println("TextExtraction-extractingText ERROR: text ���� ����");
		}
      
		try {
               pdDocument.close();
              
		} catch (IOException e) {
               System.out.println("TextExtraction-extractingText ERROR: PDDocument close ����");
		}
      
		return extractedText;
	}
	
	// textdump의 문장깨짐이 심해서 
	// (한 문장이 여러 row에 걸쳐 있거나, 한 row에 여러 문장이 있는 등)
	// 각 row에서 뽑아낸 글자들을 class 변수에 누적해 한 문장을 복원한다. 
	private void extractingSentences( String textdump ) 
	{
		String rows[] = textdump.split("\n");
    	for(int i=0; i<rows.length; ++i)
    	{
    		String row = trim(rows[i]);
    		parseRow(row);
    	}
	}
	
	private List<Sentence> convertSentenceObjects( List<SentenceBuffer> src )
	{
		List<Sentence> dest = new LinkedList<Sentence>();
		for(SentenceBuffer buffer : src){
			Sentence sentence = new Sentence();
			sentence.textAnswer = buffer.en.toString();
			sentence.textQuestion = buffer.ko.toString();
			dest.add(sentence);
		}
		
		return dest;
	}
	
	private String trim(String row) 
	{
		if(StringHelper.isNull(row)) return row;
		
		row = row.substring(0, row.length()-1); // \n ����
		return row.trim();
	}
	
    private void parseRow(String row) 
    {
    	if(StringHelper.isNull(row)) return;
    	
    	row = deleteRegacyRow(row);
    	
    	Boolean bKoreanRow = pattern_ko1.matcher(row).find()
    								|| pattern_ko2.matcher(row).find();
    	if(bKoreanRow){ 
    		parseKoreanRow(row);
    		return;
    	}
    	
    	Boolean bEnglishRow = pattern_en.matcher(row).find();
    	if(bEnglishRow){
    		parseEnglishRow(row);
    		return;
    	}
    	
    	//System.out.println("[Regacy] " + row);
    }
    
    private String deleteRegacyRow(String row)
	{
		if(pattern_subTitle.matcher(row).find()) {
			//System.out.println("[SubTitle] "+ row);
			row = ""; 		// subtitle�� ����.
			return row;
		}
		
		row = pattern_regacy1.matcher(row).replaceFirst("");
		row = pattern_regacy2.matcher(row).replaceFirst("");
		row = pattern_regacy3.matcher(row).replaceFirst("");
		return row;
	}
    
    private void parseKoreanRow(String row) 
    {
    	String korean = row;
		String english = new String(); // 한 row안에 영어문장이 포함된 경우, 영어문장 떼어내어 담기위해
		
    	// 한 row안에 영어문장이 포함된 경우, 영어문장이 시작되는 인덱스.
		int firstIdxOfEnglishSentence = isAttechedEnglishAfterKorean_InThisRow(row);  
		Boolean bAttechedEnglishAfterKorean = (firstIdxOfEnglishSentence > 0)? true : false;
		if( bAttechedEnglishAfterKorean ) 
		{									 
			korean = row.substring(0, firstIdxOfEnglishSentence);
			english = row.substring(firstIdxOfEnglishSentence+1, row.length()-1);
			//System.out.println("korean+English[" + korean + "]["+ english +"]");
		}
		//System.out.println("[Q] "+ row);
		
		
		// 한국문장에서, 한 문장이 여러줄에 나눠 저장된 경우, 각 라인에서 해당 문장이 어떤 위치에 소속되는지 알기위해.
		//// 문장의 시작점이다.
		Boolean bNewKorean = (this.LastParsingFlag == Sentence.TYPE.ENLGISH 
									|| this.LastParsingFlag == Sentence.TYPE.NONE);
		//// 문장의 중간부분이다.
		Boolean bConcatKorean = (this.LastParsingFlag == Sentence.TYPE.KOREAN);
		
		// 문장 객체를 새로 생성.
		if(bNewKorean)
			generateSentence( korean, english );
		else if(bConcatKorean) 
			appendRow( " " + korean, english );  // 기존 객체에 문장을 붙임.
		
		//플래그 셋팅. 마지막 파싱한 문장이 어떤 건지알기위해.ㅏㅣ  한 문장이 여러 줄에 나눠 저장된경우
		if(bNewKorean || bConcatKorean) {
			this.LastParsingFlag = Sentence.TYPE.KOREAN; 
		}
		if(bAttechedEnglishAfterKorean) {
			this.LastParsingFlag = Sentence.TYPE.ENLGISH; // 한문장 내에 한국문장뒤에 영어문장이 붙은경우에는, 마지막 파싱한 문장이 영어라고 플래그셋팅.
		}
    }
    
    
    private void parseEnglishRow(String row)
    {
    	String korean = new String();
		String english = row;
		
		// ���帮��Ʈ�� ����. 
		// PDF�Ľ� ��, �� ������ ��� �ɰ�����, �迭 �� �ٸ� ������ ���������� ����Ǿ� �ִ� ��찡 ����, 
		// flag�� ����� ������ �ϳ��� �̾�ٿ��� ���� ���� �����Ѵ�.
    	if(this.LastParsingFlag == Sentence.TYPE.ENLGISH) {
			appendRow( korean, " " + english );
    	}
		else if(this.LastParsingFlag == Sentence.TYPE.KOREAN) {
			appendRow( korean, english );
			this.LastParsingFlag = Sentence.TYPE.ENLGISH;
		}
    }
    
    private void generateSentence(String korean, String english) {
    	SentenceBuffer unit = new SentenceBuffer();
    	unit.ko = new StringBuffer(korean);
    	unit.en = new StringBuffer(english);
		this.parsedSentences.add(unit);
    }
    
    private void appendRow(String korean, String english) 
    {
    	SentenceBuffer unit = this.parsedSentences.getLast();
		unit.ko.append(korean);	
		unit.en.append(english);
    }
    
	 // �� �ٿ� �ѱ۹���� ������� ���� �ִ� ������� Ȯ���ϰ�, ���� �ִ� ����̸� �и�.
    private int isAttechedEnglishAfterKorean_InThisRow(String row) 
    {	
		int partitionIdx = -1;					// �� ������ �� row�� �ִ°��, �� ���� ���� Index (= �ι�° ������ �����ϴ� Index) 
    	int continuousEnglishCharacterCnt = 0;  // ����ڷ� �ǽɵǴ� ���ڵ��� ���������� �� �� �����ϴ°�
		int ENGLISH_SENTENCE = 10; 				// ����ڵ��� ���������� 10���� ������, ���� �������� ����.
		int englishCharacterStartIdx = 0; 		// ����ڷ� �ǽɵǴ� ������ ��ġ
		
		for(int j=0; j<row.length(); ++j) 
		{
			char ch = row.charAt(j);
			
			// �ѱ��� �ƴ� ��� ����(Ư����������)�� ����� �ǽ��ϵ��� ����. 
			// Ư�����ڴ� ��������� ���������� ��� �������� �ʴ´ٰ� ����.
			Boolean isEnglishCharacter = (false == (ch >= '가' && ch <= '힣')); 
			if(isEnglishCharacter) 
			{    
				// ���� ���ڰ� ������ ��� :
				// ��������� ������ Ȯ���Ͽ� ���ٿ� �ѱ�/���� ������ ���� �ִ��� Ȯ��.
				if(0 == continuousEnglishCharacterCnt) {
					englishCharacterStartIdx = j;
				}
				++continuousEnglishCharacterCnt; 
	
				// �� �ٿ� �ѱ۹����� �������� ���� �ִ� ���̽�.
				if(continuousEnglishCharacterCnt > ENGLISH_SENTENCE) {
					partitionIdx = englishCharacterStartIdx;
					return partitionIdx;
				}
			} 
			else 
			{
				// ���� ���ڰ� �ѱ��� ��� :
				// �ѱ۹��� ���ο� �����(����̸�)�� �ִ� ��찡 �����Ƿ�, 
				// ����ڴ��������� �����ϰ� ���� ���ڷ� �Ѿ��.
				continuousEnglishCharacterCnt = 0;
			}
		}
		return partitionIdx;
    }
    
}
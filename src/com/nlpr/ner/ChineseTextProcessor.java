package com.nlpr.ner;

import gnu.trove.TIntArrayList;
import gnu.trove.TObjectIntHashMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import com.nlpr.ChineseNLPTools;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;


public class ChineseTextProcessor {

	private int LONGEST_SEARCH_IN_DICT = 10;
	private ChineseNLPTools m_nlptool;
	private String m_sStopFile = "";
	private String m_sDomainFile = "";
	//private StopWordsFilter m_stopfilter = null;
	public  HashMap<String, String> m_entity2Label = new HashMap<String, String>();
	public ChineseTextProcessor( ChineseNLPTools nlptool, String sStopFile, String sDomainFile ){
		this.m_nlptool = nlptool;
		this.m_sStopFile = sStopFile;
		this.m_sDomainFile = sDomainFile;
		Initialize();
	}
	
	public ChineseTextProcessor( ChineseNLPTools nlptool, String sStopFile ){
        this.m_nlptool = nlptool;
        this.m_sStopFile = sStopFile;
        Initialize();
    }
	
	public Boolean Initialize(){
		// Read Stop file
		//m_stopfilter = new StopWordsFilter( m_sStopFile );
		ReadDomainList();
		// Read Domain File
		return true;
	}
	
	private Boolean ReadDomainList(){
		try {
			BufferedReader br = new BufferedReader(new FileReader(this.m_sDomainFile));
			String line;
			while((line = br.readLine()) != null)
			{
				int index = line.indexOf("||");
				//DomainSpecificNamedEntity.add(line.substring(0, index));
				m_entity2Label.put(line.substring(0, index), line.substring(index+2));
			}
		}
		catch(Exception e)
		{

		}
		return true;
	}
	
	public Vector<Token> Processing( String sline, Vector<Token> nerResult){
	    //分词过程
		Vector<Token> results = new Vector<Token>();
		String sResult = m_nlptool.SEGPOS(sline);
		String[] words = ChineseNLPTools.resultWordParser(sResult);
		String[] POSs = ChineseNLPTools.resultLabelParser(sResult);
		if(words.length != POSs.length){
			System.out.println("SEG processing failed");
			return null;
		}
		int offset = 0;
		for( int i=0; i< words.length; i++ ){
			Token re = new Token(words[i], offset, offset + words[i].length(), POSs[i]);
			offset += words[i].length();
			results.add(re);
		}
		//不要去除停用词
		//results = m_stopfilter.filtertoken(results);
		
		//记录下分词过程中不重复的词
		Hashtable<String, Integer> allWords = new Hashtable<String, Integer>();
        for (int i = 0; i < words.length; i++) {
            allWords.put(words[i], 1);
        }
		
		//NER过程
        sResult = m_nlptool.NERPOS(sResult);
        words = ChineseNLPTools.resultWordParser(sResult);
        POSs = ChineseNLPTools.resultLabelParser(sResult);
        if (words.length != POSs.length) {
            System.out.println("NER processing failed");
            return null;
        }

        offset = 0;
        for (int i = 0; i < words.length; i++) {
            if (!allWords.containsKey(words[i])) {
                allWords.put(words[i], 1);
                Token re = new Token(words[i], offset, offset
                        + words[i].length(), POSs[i]);
                nerResult.add(re);
            }
            offset += words[i].length();
        }
		
		return results;
	}
	
	public Vector<Token> OnlySeg( String sline){
	    //分词过程
		Vector<Token> results = new Vector<Token>();
		String sResult = m_nlptool.SEGPOS(sline);
		String[] words = ChineseNLPTools.resultWordParser(sResult);
		String[] POSs = ChineseNLPTools.resultLabelParser(sResult);
		if(words.length != POSs.length){
			System.out.println("SEG processing failed");
			return null;
		}
		int offset = 0;
		for( int i=0; i< words.length; i++ ){
			Token re = new Token(words[i], offset, offset + words[i].length(), POSs[i]);
			offset += words[i].length();
			results.add(re);
		}
		//不要去除停用词
		//results = m_stopfilter.filtertoken(results);
		
		return results;
	}
	
	public boolean isNameEntity(String spos)
	{
		if(spos.equals("APER") || spos.equals("ALOC") || spos.equals("AORG") || spos.equals("PER") ||
				spos.equals("LOC") || spos.equals("ORG")	|| spos.equals("TIM" )|| spos.equals("NUM"))
				return true;
			else return false;
	}
	
	public boolean isNPhase(String label)
	{
		if(label.startsWith("n") || label.startsWith("N"))
			return true;
		else
			return false;
	}
	
	public int overlap(Token t1, Token t2)
	{
		int s1 = t1.startOffset(), e1 = t1.endOffset(), s2 = t2.startOffset(), e2 = t2.endOffset();
		if(e1 <= s2 || e2 <= s1)
			return 0;//互相不包含
		else if(s1 == s2 && e1 == e2)
			return 4;//完全重叠
		else if(s1 <= s2 && e1 >= e2)
			return 2;//t1包含t2
		else if(s2 <= s1 && e2 >= e1)
			return 3;//t2包含t1
		else
			return 1;//部分重叠
		
	}
	
	protected void addDomainNamedEntityToken(Vector<Token> tokens, String sb, String[] words, int[] offsets)
	{
		int nowWord = 0;
		
		Vector<Token> toks = new Vector<Token>();
		for(int i = 0; i < sb.length(); i++)
		{
			// 找实体词中的最长匹配
			if(i >= offsets[nowWord + 1])
				nowWord = nowWord + 1;
			
			int max = LONGEST_SEARCH_IN_DICT > sb.length() - i ? sb.length()  - i : LONGEST_SEARCH_IN_DICT;
			String maxMatch = null;
			for(int j = max; j > 0; j--)
			{
				String tk = sb.substring(i, i+ j);
				if(m_entity2Label.containsKey(tk))
				{
					maxMatch = tk;
					break;
				}
			}
			
			if(maxMatch != null && (offsets[nowWord+1] - offsets[nowWord] <= maxMatch.length()))
			{
				Token t = new Token(maxMatch, i, i + maxMatch.length(), m_entity2Label.get(maxMatch));
				
				toks.add(t);
			}
		}
		
		// refine overlap
		for(int i=toks.size() -1; i >= 0; i--)
		{
			Token t = toks.get(i);
			
			if(t.endOffset() - t.startOffset() >= 3)
			{
				continue;
			}
			for(int j = i-1 ; j >= 0; j--)
			{
				Token t2 = toks.get(j);
				if(t.endOffset() < t2.endOffset())
				{
					toks.remove(t);
					break;
				}
			}
		}
		
		tokens.addAll(toks);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

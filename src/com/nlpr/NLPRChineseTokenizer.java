package com.nlpr;


import java.io.*;
import java.util.*;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;

import com.nlpr.ChineseNLPTools;

/**
 *
 * @author han
 *
 */
public class NLPRChineseTokenizer extends Tokenizer{
	public ChineseNLPTools nlptools;
	private int nowIndex = 0;
	private Vector<Token> tokens = new Vector<Token>();
	//public static Pattern bookName = Pattern.compile("《(.*?)》");
	//public static Pattern LinkName = Pattern.compile("〖HTK〗(.*?)〖HT〗");
	//public static Vector<String> DomainSpecificNamedEntity;
	public static Set<String> stopWords = null;
	
	//public static String DomainSpecificNamedEntityListFile = "data/DomainSpecificNamedEntityList.txt";
	//public static  boolean isfilterStopWord = true;// 默认滤出停用词
	
	public NLPRChineseTokenizer(Reader input, ChineseNLPTools nlptools)
	{
		super(input);
		this.nlptools = nlptools;
		StringBuffer sb = new StringBuffer();
		int t;
		try {
			while((t = input.read()) != -1)
			{
				sb.append((char)t);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		String result =nlptools.NERPOS(nlptools.SEGPOS(sb.toString()));
		String[] words = ChineseNLPTools.resultWordParser(result);
		String[] labels = ChineseNLPTools.resultLabelParser(result);
		int offset = 0;
		int now = 0;
		while (now < words.length)
		{
			Token re = new Token(words[now], offset, offset + words[now].length(), labels[now]);
			offset += words[now].length();
			tokens.add(re);
			now++;
		}
	}
	

	public Vector<Token> tokens()
	{
		return tokens;
	}

	/** Returns the next token in the stream, or null at EOS. */
	public final Token next() throws IOException {
		if(nowIndex < tokens.size())
		{
			nowIndex++;
			return tokens.get(nowIndex - 1);
		}
		else
			return null;
	}
	
	/**
	 * 保留分词和ner的结果的基础之上增加领域的结果
	 * @param t
	 * @return
	 */
	public static int getTokenTypeValue(Token t)
	{
		if(com.nlpr.ner.PublicFunction.isNAMEDENTITY(t.type()))
				return 2;
		else if("BOOKNAME".equals(t.type()))
				return 1;
		else if("LINKNAME".equals(t.type()))
				return 3;
		else if("DOMAINENTITY".equals(t.type()))
			return 4;
		else
			return 0;
	}
	
	public static void main(String[] args) throws Exception
	{
		ChineseNLPTools nlptools = new ChineseNLPTools();
		NLPRChineseTokenizer tz = new NLPRChineseTokenizer(new StringReader("<xml>hafkjf</xml>我是韩先培【HLK】"),nlptools);
		Token t;
		while((t = tz.next()) != null)
		{
			System.out.println(t.toString());
		}
	}

}
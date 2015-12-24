package com.nlpr;

import java.io.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import com.nlpr.ChineseNLPTools;

public class ChineseAnalyzer extends Analyzer{
	public ChineseNLPTools nlptools;

	public ChineseAnalyzer()
	{
		nlptools = new ChineseNLPTools();
	}

	public ChineseAnalyzer(ChineseNLPTools nlptools)
	{
		this.nlptools = nlptools;
	}

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		TokenStream result = new NLPRChineseTokenizer(reader, nlptools);
	    //result = new StopTokenFilter(result);
	    //result = new MarkFilter(result);
	    return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		ChineseAnalyzer analyser = new ChineseAnalyzer();
		String test = "《春秋左氏传》、《春秋内传》。传为春秋末鲁人左丘明作，实际成书时间当在战国中期。";

		/*
		BufferedReader br = new BufferedReader(new FileReader("d:/中国历史text.txt"));
		BufferedWriter bw = new BufferedWriter(new FileWriter("d:/test.txt"));
		String line = null;
		while((line = br.readLine()) != null)
		{
			System.out.println(line);
			bw.write(line+"\n");
			TokenStream ss = analyser.tokenStream("content", new StringReader(line));
			Token t;

			while((t = ss.next()) != null)
			{
				bw.write(t.toString() +"\n");
				System.out.println(t);
			}
		}
		bw.flush();
		bw.close();
	*/
		TokenStream ss = analyser.tokenStream("content", new StringReader(test));
		Token t;
		while((t = ((NLPRChineseTokenizer) ss).next()) != null)
			System.out.println(t);
	}
}

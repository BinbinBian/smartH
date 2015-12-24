package com.nlpr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

import com.nlpr.ner.CTrigram;
import com.nlpr.ner.NERMachine;
import com.nlpr.segment.StateMachine;

public class ChineseNLPTools {
	private StateMachine sm;
	private NERMachine nm;
	public static Pattern find = Pattern.compile(".*?/.*?  ");
	private String m_sWorkDir = "";

	public ChineseNLPTools()
	{
		System.out.println("Now Loading model file...");
		//sm = new StateMachine(); // 80 m
		StateMachine.LoadModel(m_sWorkDir);
		//nm = new NERMachine();  // 50 m
		CTrigram.LoadTagGramModel(StateMachine.src);
		NERMachine.ReadJapSurName("data/JapSurName.txt");
		NERMachine.ReadFPNC("data/FPNC.txt");
		CTrigram.LoadNerGramModel(); // 300 m
		System.out.println("load model file completed");
	}
	
	public ChineseNLPTools( String sWorkDir )
	{
		if((sWorkDir.lastIndexOf("\\")!=sWorkDir.length()-1) && (sWorkDir.lastIndexOf("/")!=sWorkDir.length()-1)){
			sWorkDir+="\\";
		}
		m_sWorkDir = sWorkDir;
		System.out.println("Now Loading model file...");
		StateMachine.LoadModel(m_sWorkDir); // 80 m
		//nm = new NERMachine();  // 50 m
		CTrigram.LoadTagGramModel(StateMachine.src);
		NERMachine.ReadJapSurName("data/JapSurName.txt");
		NERMachine.ReadFPNC("data/FPNC.txt");
		CTrigram.LoadNerGramModel(); // 300 m
		System.out.println("load model file completed");
	}
	
	public String segmentAndPOS(String in)
	{
		return sm.Segmentation_POS_str(in);
	}

	/**
	 * 将结果字符串的分词结果parser出来
	 * @param result
	 * @return
	 */
	public static String[] resultWordParser(String result)
	{
		Matcher mt = find.matcher(result);
		Vector<String> re = new Vector<String>(10);
		int s = 0;
		String temp;
		int in = 0;
		while(mt.find(s))
		{
			s = mt.end();
			temp = result.substring(mt.start(), mt.end());
			in = temp.lastIndexOf('/');
			re.add(temp.substring(0, in));
		}
		String[] ss = new String[re.size()];
		re.toArray(ss);
		return ss;
	}


	/**
	 * 将结果字符串的标注结果parser出来
	 * @param result
	 * @return
	 */
	public static String[] resultLabelParser(String result)
	{
		Matcher mt = find.matcher(result);
		Vector<String> re = new Vector<String>(10);
		int s = 0;
		String temp;
		int in = 0;
		while(mt.find(s))
		{
			s = mt.end();
			temp = result.substring(mt.start(), mt.end());
			in = temp.lastIndexOf('/');
			re.add(temp.substring(in+1).trim());
		}
		String[] ss = new String[re.size()];
		re.toArray(ss);
		return ss;
	}

    public String SEGPOS(String in) {
        if (in.trim().length() == 0)
            return "";

        StateMachine sm = new StateMachine(); // 分词
        String sSegStr = sm.Segmentation_POS_str(in);

        return sSegStr;
    }

    public String NERPOS(String sSegStr) {
        if (sSegStr.trim().length() == 0)
            return "";

        NERMachine nm = new NERMachine(); // NER
        String sTagStr = nm.ParagraphProcessing(sSegStr, 0);
        String sNerStr = nm.ParagraphProcessing(sTagStr, 1);

        return sNerStr;
    }

	public static void main(String[] args) throws Exception
	{
		System.out.println("Usage: infile outfile");
		ChineseNLPTools cnlp = new ChineseNLPTools();
		
		//System.out.println(cnlp.SEGPOSNER("刘康是中科院自动化所的助理研究员"));
		
		System.out.println("Process completed");
	}
}

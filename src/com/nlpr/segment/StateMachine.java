package com.nlpr.segment;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.*;

/* This is the State Machine Class for Segmentation & POS
 *    -- Main Class of Segmentation & POS
 *    -- Author: Jin Qianli
 *    -- Contect: aupbli@yahoo.com.cn
 *    -- Last Modify: 2004-03-18
 *    @ported by xphan
 *    @modifed by kliu
 */
public class StateMachine implements Serializable {


	////////////////////////////////////////////////////////////////
	// 以下为static 成员变量
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public static final double ver = 1.10;

	//状态机状态
	public static final int State_words = 0;  //汉字序列
	public static final int State_symbols = 1; //中文和英文标点
	public static final int State_number_1 = 2;//中文全角数字
	public static final int State_number_2 = 3;//chinese special number
	public static final int State_number_3 = 4;//英文数字
	public static final int State_English_Word = 5;//英文字序列

	public static final String[] quanjiaoshuzi= new String[]//a2b1-a2fc, chinese whole number
	 {
	"ⅰ","ⅱ","ⅲ","ⅳ","ⅴ","ⅵ","ⅶ","ⅷ","ⅸ","ⅹ",//a2a1-a2aa
	"⒈","⒉","⒊","⒋","⒌","⒍","⒎","⒏","⒐","⒑",//a2b1-a2ba
	"⒒","⒓","⒔","⒕","⒖","⒗","⒘","⒙","⒚","⒛",//a2bb-a2c4
	"⑴","⑵","⑶","⑷","⑸","⑹","⑺","⑻","⑼","⑽",//a2c5-a2ce
	"⑾","⑿","⒀","⒁","⒂","⒃","⒄","⒅","⒆","⒇", //a2cf-a2d8
	"①","②","③","④","⑤","⑥","⑦","⑧","⑨","⑩",//a2d9-a2e2
	"㈠","㈡","㈢","㈣","㈤","㈥","㈦","㈧","㈨","㈩",//a2e5-a2ee
	"Ⅰ","Ⅱ","Ⅲ","Ⅳ","Ⅴ","Ⅵ","Ⅶ","Ⅷ","Ⅸ","Ⅹ","Ⅺ","Ⅻ",//a2f1-a2fc
	"０","１","２","３","４","５","６","７","８","９"//a3b0-a3b9
	 };

	public static final String[] banjiaoyingwen=  new String[]  //english symbols
	{
		"~","`","!","@","#","$","%","^","&","*","(",")",
			"_","-","+","=","{","[","}","]","|","\\",":",";",
		"\"","\'","<",",",">",".","?","/"," ","\t","\r","\n"
	};

	public static final String[] banjiaozhongwen=  new String[]//chinese symbols
	{
		"，","．","。","～","！","◎","＃","￥","％","…","※","×","（",
		"）","—","－","＋","＝","『","【","』","】","§","÷",
		"：","；","“","‘","《","》","？","、","”","’","＿","…","？","　"
	};

	public static final String[] num_china=  new String[]// chinese specail number
	{"·","零","一","二","三","四","五","六","七","八","九"};

	public static final String[] num_english=  new String[] // english number
	{"0","1","2","3","4","5","6","7","8","9"};

	public static final String[] symbol_stop=  new String[] // stop symbols as the sentence delimitations
	{ ".","!","?","\r","\n","。","！","？"};

	/////////////////////////////////////////////////////////////////////////////////////////////////////////

	// 当前状态和下一个状态
	private int State;
	//private int subState;

	// 词典及概率信息
	static public CSource src;

	//current String
	private StringBuffer currentS;
	public int sPos;

	//current sequence
	private StringBuffer currentQ;
	int qPos;
	private int begin, end;

	// output string
	private StringBuffer output;
	private int oPos;

	// storing word - temp
	StringBuffer word,word2;

	// 一个中文词的最大长度,按照一个中文一个长度来算，跟c++版不一样之处，需要除于2
	private int maxlength;

	// current position of 'currentQ'
	int length;

	// best sequence for 2-level state machine
	private int[] nbest;

	// probabilistics of sequence
	double[] prob;

	private int in_len = 100000;

	static private HashSet<String> quanjiaoshuziSet = new HashSet<String>(200);
	static private HashSet<String> banjiaoyingwenSet = new HashSet<String>(70);
	static private HashSet<String> banjiaozhongwenSet = new HashSet<String>(70);
	static private HashSet<String> num_chinaSet = new HashSet<String>(20);
	static private HashSet<String> num_englishSet = new HashSet<String>(20);
	static private HashSet<String> symbol_stopSet  = new HashSet<String>(20);

	//static private String m_sWorkDir = "";

	public StateMachine()
	{
		//this.LoadModel();
		this.init_StateMachine();
	}
	
//	public StateMachine(String sWorkDir )
//	{
//		m_sWorkDir = sWorkDir;
//		//this.LoadModel();
//		this.init_StateMachine();
//	}

	private int init_StateMachine()
	{
		//assert(s != null);
		//this.src = s;
		this.maxlength = src.WORD_MAXLENGTH;
		// TODO 除于2
		this.maxlength /= 2;
		this.word = new StringBuffer(maxlength + 3);
		this.word2 = new StringBuffer(maxlength + 3);

		this.currentS = new StringBuffer(in_len);
		this.currentQ = new StringBuffer(in_len);

		this.output = new StringBuffer(in_len);

		return 1;
	}

	/**
	 * @param s
	 * @return 4 英文数字 3 中文半角数字 2 全角数字 0 非数字
	 */
	public int is_number(String s)
	{
		if(num_englishSet.contains(s))
			return 4;
		else if(num_chinaSet.contains(s))
			return 3;
		else if(quanjiaoshuziSet.contains(s))
			return 2;
		else
			return 0;
	}

	/**
	 * 是否是英文?
	 * 1 是 0 否
	 */
	int is_English_word(String s)
	{
		if(s.matches("[a-zA-Z]"))
			return 1;
		else
			return 0;
	}

	/**
	 *
	 * @param s
	 * @return 1 半角英文 2 半角中文 0 false
	 */
	int is_Symbols(String s)
	{
		if(banjiaoyingwenSet.contains(s))
			return 1;
		else if(banjiaozhongwenSet.contains(s))
			return 2;
		else
			return 0;
	}

	/** Is the sentence delimitation?
	 * Input is 'CurrentS'
	 * return 1 if TRUE, otherwise return 0
	 */
	int is_Delimitation(String s)
	{
		if(symbol_stopSet.contains(s))
			return 1;
		else
			return 0;
	}

	/* Output the result
	 * Input is 'output' & 'out_FILE*'
	 * return 1 if success, otherwise return 0
	 */
	int output_Result()
	{
		if(currentS.length() == 0)
			return 0;
		if(State == State_words)
		{
			output.append(currentS);
			oPos += currentS.length();
		}
		else if(State == State_symbols)
		{
			for(int i = 0; i < currentS.length(); i++)
			{
				// '/' -> 1 , for the use of combine_rule(), change back in combine_rule()
				if(currentS.charAt(i) == '/')
					currentS.setCharAt(i, '1');
			}
			if(currentS.charAt(0) == ' ' || currentS.charAt(0) == '\t'
				|| currentS.charAt(0) == '　')
			{

			}
			else if(currentS.charAt(0) == '\r' || currentS.charAt(0) == '\n')
			{
				output.append(currentS);
				oPos += currentS.length();
				oPos = 0; 
			}
			else {
				output.append(currentS);
				output.append("/w  ");
				oPos += currentS.length() + 4;
			}
		}
		else if(State == State_number_1)
		{
			output.append(currentS);
			output.append("/m  ");
			oPos += currentS.length() + 4;
		}
		else if(State == State_number_2)
		{
			output.append(currentS);
			output.append("/m  ");
			oPos += currentS.length() + 4;
		}
		else if(State == State_number_3)
		{
			output.append(currentS);
			output.append("/m  ");
			oPos += currentS.length() + 4;
		}
		else if(State == State_English_Word)
		{
			// '/' -> 1 , for the use of combine_rule(), change back in combine_rule()
			for(int i = 0; i < currentS.length(); i++)
			{
				if(currentS.charAt(i) == '/')
					currentS.setCharAt(i, '1');
			}
			output.append(currentS);
			output.append("/nx  ");
			oPos += currentS.length() + 5;
		}

		if(oPos >= 2 * in_len)
		{
			oPos = 0; 
		}
		begin = end;
		return 1;
	}

	/**
	 * state change
	 * @param src source state
	 * @param dest destination state
	 */
	public void state_Change(int src, int dest)
	{
		// 从 State_Words出发
		if(src == State_words && dest == State_words)
		{
			;
		}
		else if(src == State_words && dest == State_symbols)
		{
			this.state_word_process();
			State = State_symbols;
		}
		else if(src == State_words && dest == State_number_1)
		{
			this.state_word_process();
			State = State_number_1;
		}
		else if(src == State_words && dest == State_number_2)
		{
			int i = end - 1;
			while(i >= begin)
			{
				if(end + 1 - i > maxlength) 
				{
					i = begin -1;
					break;
				}
				word.delete(0, word.length());
				word.append(currentQ.subSequence(i, end + 1));
				if(this.src.isPrex(word.toString()) > 0)
					break;
				i--;
			}

			if(i >= begin) //continue State_words
			{}
			else
			{
				this.state_word_process();
				State = State_number_2;
			}
		}
		else if(src == State_words && dest == State_number_3)
		{
			this.state_word_process();
			State = State_number_3;
		}
		else if(src == State_words && dest == State_English_Word)
		{
			this.state_word_process();
			State = State_English_Word;
		}

		//-------------State_symbols ==>---------------------
		else if(src == State_symbols)
		{
			this.state_symbols_Process(dest);
		}

		// ------------- State_number_1 ==> -------------------------------

		else if(src == State_number_1)
		{
			if(dest == State_words)
			{
				this.copyCurrentSfromCurrentP();
				output_Result();
				State = State_words;
			}
			else if(dest == State_symbols)
			{
				if(end >= currentQ.length())
				{
					this.copyCurrentSfromCurrentP();
					output_Result();
					State = State_symbols;
				}
				else if(currentQ.charAt(end) == '％')
				{
					end += 1;
					this.copyCurrentSfromCurrentP();
					output_Result();
					State = State_symbols;
				}
				else if(currentQ.charAt(end) == '－')
				{

				}
				else if(end+1 < currentQ.length() &&
					(currentQ.charAt(end) == '，'	|| currentQ.charAt(end) == '．')
					&& this.is_number(currentQ.substring(end+1, end+2)) == State_number_1
				)
				{

				}
				else{
					this.copyCurrentSfromCurrentP();
					output_Result();
					State = State_symbols;
				}
			}
			else if(dest == State_number_1)
			{

			}
			else if(dest == State_number_2)
			{
				this.copyCurrentSfromCurrentP();
				output_Result();
				State = State_number_2;
			}
			else if(dest == State_number_3)
			{
				this.copyCurrentSfromCurrentP();
				output_Result();
				State = State_number_3;
			}
			else if(dest == State_English_Word)
			{
				this.copyCurrentSfromCurrentP();
				output_Result();
				State = State_English_Word;
			}
		}

		//---------------------State_number_2 ==>----------------
		else if(src == State_number_2 && dest == State_words)
		{
			int i = end - 1;
			while(i >= begin)
			{
				if(end + 1 - i > maxlength)
				{
					i = begin -1;
					break;
				}
				word.delete(0, word.length());
				word.append(currentQ.subSequence(i, end + 1));
				if(this.src.isPrex(word.toString()) > 0)
					break;
				i--;
			}

			if(i >= begin) //change to State_words
			{
				State = State_words;
			}
			else
			{
				this.copyCurrentSfromCurrentP();
				output_Result();
				State = State_words;
			}
		}
		else if(src == State_number_2 && dest == State_symbols)
		{
			this.copyCurrentSfromCurrentP();
			output_Result();
			State = State_symbols;
		}
		else if(src == State_number_2 && dest == State_number_1)
		{
			this.copyCurrentSfromCurrentP();
			output_Result();
		    State = State_number_1;
		}
		else if(src == State_number_2 && dest == State_number_2)
		{

		}
		else if(src == State_number_2 && dest == State_number_3)
		{
			this.copyCurrentSfromCurrentP();
			output_Result();
		    State = State_number_3;
		}
		else if(src == State_number_2 && dest == State_English_Word)
		{
			this.copyCurrentSfromCurrentP();
			output_Result();
		   State = State_English_Word;
		}

		// ------------- State_number_3 ==> -------------------------------
		else if(src == State_number_3 && dest == State_words)
		{
			this.copyCurrentSfromCurrentP();
			output_Result();
			State = State_words;
		}
		else if(src == State_number_3 && dest == State_symbols)
		{
			if(end < currentQ.length() && currentQ.charAt(end) == '%')
			{
				end += 1;
				this.copyCurrentSfromCurrentP();
				output_Result();
				State = State_symbols;
			}
			else if(end < currentQ.length() && currentQ.charAt(end) == '-')
			{

			}
			else if(end < currentQ.length() && end + 1 < currentQ.length() && (currentQ.charAt(end) == ',' ||
					currentQ.charAt(end) == '.') && this.is_number(currentQ.substring(end + 1, end + 2)) == State_number_3)
			{
			}
			else{
				this.copyCurrentSfromCurrentP();
				output_Result();
				State = State_symbols;
			}
		}
		else if(src == State_number_3 && dest == State_number_1)
		{
			this.copyCurrentSfromCurrentP();
			output_Result();
			State = State_number_1;
		}
		else if(src == State_number_3 && dest == State_number_2)
		{
			this.copyCurrentSfromCurrentP();
			output_Result();
			State = State_number_2;
		}
		else if(src == State_number_3 && dest == State_number_3)
		{

		}
		else if(src == State_number_3 && dest == State_English_Word)
		{
			this.copyCurrentSfromCurrentP();
			output_Result();
			State = State_English_Word;
		}

		// -------State_English_Word ==> -----------------------------
		else if(src == State_English_Word && dest == State_words)
		{
			this.copyCurrentSfromCurrentP();
			output_Result();
			State = State_words;
		}
		else if(src == State_English_Word && dest == State_symbols)
		{
			if(end < currentQ.length() && (currentQ.charAt(end) == ':' || currentQ.charAt(end) == '/'
				|| currentQ.charAt(end) == '.' || currentQ.charAt(end) == '@'))
			{

			}
			else{
				this.copyCurrentSfromCurrentP();
				output_Result();
				State = State_symbols;
			}
		}
		   else if(src == State_English_Word && dest == State_number_1)
		   {
			   this.copyCurrentSfromCurrentP();
			   output_Result();
		       State = State_number_1;
		   }
		   else if(src == State_English_Word && dest == State_number_2)
		   {
			   this.copyCurrentSfromCurrentP();
			   output_Result();
		       State = State_number_2;
		   }
		   else if(src == State_English_Word && dest == State_number_3)
		   {
			   ;
		   }
		   else if(src == State_English_Word && dest == State_English_Word)
		   { ; }
	}

	private void state_symbols_Process(int dest_States)
	{
		this.copyCurrentSfromCurrentP();
		output_Result();
		State = dest_States;
	}

	private void state_word_process()
	{
		this.copyCurrentSfromCurrentP();
		find_best();
		output_Result();
	}
	private void copyCurrentSfromCurrentP()
	{
		currentS.delete(0, currentS.length());
		currentS.append(currentQ, begin, end);
	}

	/**
	 * find the best roads for POS
	 * @input currentS
	 * @output nbest
	 * @return the best sequence
	 */
	int find_best()
	{
		// prepare memory
		int i, j, k;
		int notes = currentS.length() + 1;
		double[][] road = new double[notes][notes];
		double[] D = new double[notes];
		int[] flag = new int[notes];

		this.nbest = new int[notes];

		// find best load
		// Dijkstra Algorithm (Greedy)
		//---------------------------------

		// initial flag set and load weights

		double min = -10e300;
		flag[0] = 1;
		for(i = 1; i < notes; i++)
			flag[i] = 0;
		for(i = 0; i < notes; i++)
			for(j = 0; j < notes; j++)
				road[i][j] = min;

		// compute the weight of each road
		int wPos;
		int wordID;
		sPos = 0;
		for(i = 0; i < notes - 1; i++)
		{
			word.delete(0, word.length());
			word.append(currentS.charAt(i));
			wPos = 1;
			road[i][i+1] = Math.log(src.smoothData(0)); // single chinese character, intital

			while(src.isPrex(word.toString()) > 0)
			{
				if((wordID = src.getWordID(word.toString())) >= 0 )
				{
					road[i][i+wPos] = Math.log(src.getUnigramofWord(wordID));
				}
				if(i + wPos >= currentS.length())
					break;
				word.append(currentS.charAt(i + wPos));
				wPos += 1;
				if(wPos > maxlength)
					break;
			}
		}

		// find the best road
		double max_value;
		int pos;

		for(i = 1; i < notes; i++)
		{
			D[i] = road[0][i];
			nbest[i] = 0;
		}
		for(i = 0; i < notes -1; i++)
		{
			max_value = min;
			pos = 0;
			for(j = 0; j < notes; j++)
			{
				if(flag[j] == 0 && D[j] >= max_value)
				{
					pos = j;
					max_value = D[j];
				}
			}
			flag[pos] = 1;
			for(j = 0; j < notes; j++)
			{
				if(flag[j] == 0)
				{
					if(D[j] < D[pos] + road[pos][j])
					{
						D[j] = D[pos] + road[pos][j];
						nbest[j] = pos;
					}
				}
			}
		}

		//--------------------------------------

		// find out POS
		j = notes - 1;
		i = 0;
		while(nbest[j] != 0)
		{
			word.delete(0, word.length());
			word.append(currentS.subSequence(nbest[j], j));
			D[i] = (double)src.getWordID(word.toString());
			flag[i] = j; // segment information
			i++;
			j = nbest[j];
		}
		word.delete(0, word.length());
		word.append(currentS.subSequence(0, j));
		D[i] = (double)src.getWordID(word.toString());
		flag[i] = j; // segment information
		i++;

		double swapd;
		int swapl;
		for(j = 0; j < i/2; j++)
		{
			swapd = D[j]; D[j] = D[i-1-j]; D[i-1-j] = swapd;
			swapl = flag[j]; flag[j] = flag[i-1-j]; flag[i-1-j] = swapl;
		}
		find_POS(D, i); // return value: nbest[]

		StringBuffer ss = new StringBuffer();
		ss.append(currentS);
		sPos = k = 0;
		this.currentS.delete(0, currentS.length());
		for(j = 0; j < i ; j++)
		{
			word.delete(0, word.length());
			word.append(ss.subSequence(k, flag[j]));
			k = flag[j];

			this.currentS.append(word + "/" + src.getPOSString(nbest[j]) +"  ");
			//sPos += word.length() + src.getPOSString(nbest[j]).length() + 3;
		}
		return 1;
	}

	/**
	 * find out POS based on Word ID in 'D[]'
	 * no the number of words
	 * return value nbest[]
	 */
	public void find_POS(double[] D, int no)
	{
		int[] NumOfPos = new int[no];
		int i, j, k, p=0, q = 0, max = 1;
		for(i = 0; i < no; i++)
		{
			if(D[i] < 0) // unknown single chinese character
			{
				NumOfPos[i] = 1;
			}
			else if((NumOfPos[i] = src.getNumofWordPOS((int)D[i])) >= max)
				max = NumOfPos[i];
		}

		double[][][] value = new double[no][max][max];
		int[][][] path = new int[no][max][max];
		int[][] POS = new int[no][max];

		// Dynamic Program
		double temp, maxd;
		for(i = 0; i < no; i++)
		{
			if(D[i] < 0)// unknown single chinese character
				POS[i][0] = src.getPOSID("nx");
			else
			{
				for(j = 0; j < NumOfPos[i]; j++)
					POS[i][j] = src.getIDofWordPOS((int)D[i], j);
			}
		}
		
		double adjust = 1.0; // adjust weights between P(w|t) and p(t|t)
		if(no > 0)
		{
			for(i = 0; i < NumOfPos[0];i++)
			{
					if(D[0] < 0)// unknown single chinese character
						value[0][i][0] = Math.log(src.getUnigramofPOS(POS[0][i]));
					else
						value[0][i][0] = Math.log(src.getPOSWordProb((int)D[0], POS[0][i])) * adjust
						+ Math.log(src.getUnigramofPOS(POS[0][i]));
			}
		}
		if(no > 1)
		{
			for(i = 0; i < NumOfPos[1]; i++)
				for(j = 0; j < NumOfPos[0]; j++)
				{
					if(D[1]< 0)
						value[1][i][j] = value[0][j][0]+
							Math.log(src.getBigramofPOS(POS[1][i], POS[0][j]));
					else
							value[1][i][j] = value[0][j][0]+
							Math.log(src.getBigramofPOS(POS[1][i], POS[0][j]))
							+Math.log(src.getPOSWordProb((int)D[1], POS[1][i]))*adjust;

				}
		}
		if(no == 1)
		{
			maxd=-10e300; j=0;
			for(i=0;i<NumOfPos[0];i++)
			{ if(value[0][i][0]>maxd)
		      {
				maxd=value[0][i][0];
				j=i;
		      }
			}
			nbest[0] = POS[0][j];
		}
		else if(no==2)
		{
				maxd=-10e300; k=0; p=0;
				for(i=0;i<NumOfPos[1];i++)
					for(j=0;j<NumOfPos[0];j++)
					{
						if(value[1][i][j]>maxd)
						{
							maxd=value[1][i][j];
							k=i;
							p=j;
						}
					}
					nbest[0] = POS[0][p]; nbest[1] = POS[1][k];
		}
		else{
			k = 2;
			while(k < no)
			{
				for(i = 0; i < NumOfPos[k];i++)
					for(j = 0; j < NumOfPos[k-1]; j++)
					{
						maxd = -10e300;
						for(p = 0; p < NumOfPos[k-2]; p++)
						{
							if(D[k] < 0)
							{	temp = value[k-1][j][p]
									        + Math.log(src.getTrigramofPOS(POS[k][i], POS[k-1][j], POS[k-2][p]));
							}
							else
							{
								temp = value[k-1][j][p]+
									Math.log(src.getTrigramofPOS(POS[k][i], POS[k-1][j], POS[k-2][p]))+
									 Math.log(src.getPOSWordProb((int)D[k], POS[k][i]))*adjust;


							 }
							if( temp > maxd)
							{
								 maxd = temp; q = p;
							}
						}
						value[k][i][j] = maxd;
						path[k][i][j] = q;
					}
					k++;
				 } // for 'while'

				 maxd = -10e300;
				 for(i = 0; i < NumOfPos[no -1]; i++)
					 for(j = 0; j < NumOfPos[no -2]; j++)
					 {
						 if(value[no-1][i][j] > maxd)
						 {
							 maxd = value[no - 1][i][j];
							 p = i;
							 q = j;
						 }
					 }
				 nbest[no - 1] = POS[no - 1][p];
				 nbest[no -2] = POS[no - 2][q];
				 k = no - 3;
				 while (k >= 0)
				 {
					 i = path[k+2][p][q];
					 nbest[k] = POS[k][i];
					 p = q;
					 q = i;
					 k--;
				 }
			 }// for 'else'
		}


	/**
	 * 将CSource中的数据load进来
	 */
	static public void LoadModel(String m_sWorkDir)
	{
		src = new CSource(new File(m_sWorkDir+ "data/segmentData.txt"), 1);

        for(int i = 0; i < quanjiaoshuzi.length; i++)
        {
            quanjiaoshuziSet.add(quanjiaoshuzi[i]);
        }
        for(int i = 0; i < banjiaoyingwen.length; i++)
        {
            banjiaoyingwenSet.add(banjiaoyingwen[i]);
        }
        for(int i = 0; i < banjiaozhongwen.length; i++)
        {
            banjiaozhongwenSet.add(banjiaozhongwen[i]);
        }
        for(int i = 0; i < num_china.length; i++)
        {
            num_chinaSet.add(num_china[i]);
        }
        for(int i = 0; i < num_english.length; i++)
        {
            num_englishSet.add(num_english[i]);
        }
        for(int i = 0; i < symbol_stop.length; i++)
        {
            symbol_stopSet.add(symbol_stop[i]);
        }
	}

	/**
	 * 切分in
	 * @param in 输入字符串
	 * @return
	 */
	public String Segmentation_POS_str(String in)
	{
		output.delete(0, output.length());
		return this.str_Run(in);
	}

	public String str_Run(String t)
	{
		qPos = 0;
		sPos = 0;
		oPos = 0;

		// 将字符串t拷入currentQ中
		currentQ.delete(0, currentQ.length());
		currentQ.append(t);

		str_Run_line();
		String result = str_combine_rule();
		return result;
	}

	/**
	 * 判断一个字符是否是中文字符
	 * @param ss
	 * @return
	 */
	public static boolean isChinese(String ss)
	{
	    /*
	    byte[] sss = ss.getBytes();
	    if(sss.length == 1)
	        return false;
	    else
	        return true;
	    */
	    
		byte[] sss = ss.getBytes(Charset.forName("gbk"));
		if(sss.length <= 0 || sss.length % 2 != 0)
			return false;
		for(int i = 0; i < sss.length / 2; i++)
		{
			int value = sss[2*i] >= 0 ? sss[2*i] : 256 + sss[2*i];
			if(value < 176)
				return false;
		}
		return true;
		
	}

	public void str_Run_line()
	{
		// intial state and sub state
		int nState;
		this.State = this.currentState(0);
		
		
		// main loop
		this.begin = 0;
		nState = State_symbols;
		for(int j = 1; j < currentQ.length(); j++)
		{
			nState = this.currentState(j);
			
			this.end = j;
			state_Change(State, nState);
		}
		end = currentQ.length();
		this.state_Change(State, State_symbols);
		this.oPos = 0; // output
	}

	public int currentState(int index)
	{
		int temp;
		if(this.is_English_word(currentQ.substring(index, index + 1)) == 1)
			return State_English_Word;
		else if((temp = this.is_number(currentQ.substring(index, index + 1))) > 0)
			return temp;
		else if(this.is_Symbols(currentQ.substring(index, index + 1)) > 0)
            return State_symbols;
		else if(StateMachine.isChinese(currentQ.substring(index, index + 1)))
			return  State_words;
		else
			return State_symbols;
	}

	/**
	 * combine some tags like: /m 数字 /t 时间  /w 标点
	 * @return
	 */
	public String str_combine_rule()
	{
		int  flag, old_flag;
		String ss;
		char rule_state, now_state;
		String str, temp  = null;
		char[] time = new char[]{
				'年','月','日','点','分','秒','时','天'
		};
		ss = output.toString();
		StringBuffer result = new StringBuffer();
		StringTokenizer st = new StringTokenizer(ss, "/ \r\n");

		rule_state = now_state = '0';
		flag = old_flag = 0;
		try {
			str= st.nextToken();
		}catch (Exception e)
		{
			str = null;
		}
		while(str != null)
		{
			if(flag == 0)
			{
				result.append(str);
			}
			else
				temp = str;

			try {
				str= st.nextToken();
			}catch (Exception e)
			{
				str = null;
			}
			if(str.equals("w"))
				now_state = 'w';
			else if(str.equals("m"))
				now_state = 'm';
			else if(str.equals("t"))
			{
				now_state = 'm';
				flag = 2;
			}
			else
				now_state = '0';

			// little state machine
			if(rule_state == '0' && now_state == 'm')
			{
				if(flag == 2)
				{
					result.append("/"+str +"  ");
					try {
						str= st.nextToken();
					}catch (Exception e)
					{
						str = null;
					}
					flag = 0;
					now_state = '0';
				}
				else
				{
					try {
						str= st.nextToken();
					}catch (Exception e)
					{
						str = null;
					}
					flag = 1;
				}
				old_flag = flag;
			}
			else if(rule_state == '0' && now_state == 'w')
			{
				try {
					str= st.nextToken();
				}catch (Exception e)
				{
					str = null;
				}
				flag = -1;
			}
			else if(rule_state == '0' && now_state == '0')
			{
				result.append("/" + str + "  ");
				try {
					str= st.nextToken();
				}catch (Exception e)
				{
					str = null;
				}
				flag = 0;
			}
			else if(rule_state == 'w' && now_state == 'w')
			{
				result.append(temp);
				try {
					str= st.nextToken();
				}catch (Exception e)
				{
					str = null;
				}
				flag = -1;
			}
			else if(rule_state == 'w' && now_state == '0')
			{
				result.append("/w  " + temp +"/" + str + "  ");
				try {
					str= st.nextToken();
				}catch (Exception e)
				{
					str = null;
				}
				flag = 0;
			}
			else if(rule_state == 'w' && now_state == 'm')
			{
				if(flag == 2)
				{
					result.append("/w  " + temp +"/" + str+"  ");
					try {
						str= st.nextToken();
					}catch (Exception e)
					{
						str = null;
					}
					flag = 0;
					now_state = '0';
				}
				else
				{
					result.append("/w  " + temp);
					flag = 1;
					try {
						str= st.nextToken();
					}catch (Exception e)
					{
						str = null;
					}
				}
				old_flag = flag;
			}
			else if(rule_state == 'm' && now_state == 'm')
			{
				if((flag ==1 || flag == 2) && old_flag == 1) // not 'time'
				{
					result.append("/m  " + temp);
				}
				else if((flag == 1 || flag == 2) && old_flag >= 2) // is single 'time'
				{
					result.append("/t  " + temp);
				}
				else // is combined 'time'
				{
					result.append(temp);
				}
				old_flag = flag;
				try {
					str= st.nextToken();
				}catch (Exception e)
				{
					str = null;
				}
			}
			else if(rule_state == 'm' && now_state == 'w')
			{
				if(flag == 1)
				{
					result.append("/m  " + temp);
				}
				else{
					result.append("/t  " + temp);
				}
				flag = -1;
				try {
					str= st.nextToken();
				}catch (Exception e)
				{
					str = null;
				}
			}
			else if(rule_state == 'm' && now_state == '0')
			{
				int i = 0;
				for( i = 0; i < time.length; i++)
				{
					if(temp.charAt(0) == time[i] && temp.getBytes(Charset.forName("gbk")).length <= 3)
						break;
				}
				if(i < time.length)
				{
					result.append(temp);
					flag = 3;
					try {
						str= st.nextToken();
					}catch (Exception e)
					{
						str = null;
					}
					now_state = 'm';
					old_flag = flag;
				}
				else {
					if(flag == 1)
					{
						result.append("/m  " + temp +"/" + str + "  ");
					}
					else
					{
						result.append("/t  " + temp + "/" + str + "  ");
					}
					flag = 0;
					try {
						str= st.nextToken();
					}catch (Exception e)
					{
						str = null;
					}
				}
			}
			rule_state = now_state;
		}
		if(rule_state == 'w')
			result.append("/w  ");
		else if(rule_state == 'm' && flag == 1)
		{
			result.append("/m  ");
		}
		else if(rule_state == 'm' && flag == 2)
		{
			result.append("/t  ");
		}
		else if(rule_state == 'm' && flag == 3)
		{
			result.append("/t  ");
		}

		result.append("\n");

		return result.toString();
	}

	public static void main(String[] args)
	{
		//System.out.println(StateMachine.isChinese("a"));
		StateMachine sm = new StateMachine();
		String results = sm.Segmentation_POS_str("夏代经济以农业为主，使用石蚌骨木工具，偶见铜器。统治者治水以保障农业生产，制历法以定农时。渔猎还有重要经济地位。阶级、国家的出现，王位世袭的确定，刑法和贡赋制的建立,都引起守旧势力的反抗,但新制度毕竟取得胜利。夏末国王履癸（桀）昏暴，人民反对，被征服部落叛乱，夏朝被商朝取代。"
				+"　　<h4>商</h4>　　商朝是活动于黄河中下游的商部落建立的。始祖契，相传曾随夏禹治水。");
		System.out.println(results);
		//byte[] ss = "工作工龄".getBytes();
		//for(int i = 0; i < ss.length; i++)
			//System.out.print(ss[i] + " ");
	}
}

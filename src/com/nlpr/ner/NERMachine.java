package com.nlpr.ner;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

/**
 * 用来对应C++中的CNamEnRecog类
 * @author han
 *
 */
public class NERMachine {


	public final static double PERWORD_POS = 0.5; //由人名词汇实体模型概率换算词性实体模型概率

	public final static int MAX_SEN_LEN = 1000; //能够处理整个句子的长度
	public final static int MAX_CHARACTER= 1000 / 2;//每次实体的最大字符数
	public final static int iStep_MAX_NODE_NUM= 1000 ;//第i步最多的可能产生的状态数目
	public final static int TO_iStep_MAX_NODE_NUM= 1000 ;//指向iStep的最大节点数

	public final static int FN_NAME_LEN =13; //外国人名最多包含10个字
	public final static int JN_NAME_LEN= 4;//日本人名最多包含2个字（不包括姓氏）
	public final static int MAX_LOC_LEN_DOUBLEKEYWORD =12;//地名最多包含12个字
	public final static int MAX_LOC_LEN_SINGLEKEYWORD =5 ;//地名最多包含5个字
	public final static int MAX_ORG_LEN =16 ;//机构名最多包含16个字

	public final static int MAX_CANDI_LOC= 500 ;//最多能够产生的候选地名数目
	public final static int MAX_CANDI_ORG= 500 ;//最多能够产生的候选机构名数目

	public final static int MAX_CHNAME_TEXT= 1000; //一篇文章中最多包含的中国人名
	public final static int MAX_LOCNAME_TEXT =1000; //一篇文章中最多包含的地名
	public final static int MAX_ORGNAME_TEXT =1000 ;//一篇文章中最多包含的机构名

	public final static int LEXICON_SIZE =1000 ;//定义词典的大小

	/******************public function*************/
	public SegTag[] segtag;//分词标注结果
	public int segtagword_num;//分词标注结果长度

	// 第i步包含的节点
	public NODE_ATTRIBUTE[][] iStepNode;
	public STEP_ATTRIBUTE[] iStep;
	//步数
	public int iStepNum;

	//一篇文章中识别出的中国人名，地名，机构名
	public CACHEENTITY[] ORGNAME_TEXT;
	public int[] ORGNAME_TEXT_NUM;

	// 外国人名用字集合
	static public HashSet<String> TRANS_ENGLISH_CHARACTER_SET = new HashSet<String>();
	public String[] TRANS_ENGLISH_CHARACTER;
	public int TRANS_ENGLISH_CHARACTER_NUM;

	public CTrigram m_Trigram;

	// 日本人名姓氏
	static public HashSet<String> JapSurName_set = new HashSet<String>();
	public String[] JapSurName;
	public int JapSurNameLen;
	/******************private variaty****************/

	private String sNowWord, sHisWord1, sHisWord2;

	/*******************public function**************/

	public NERMachine()
	{
		this.segtag = new SegTag[MAX_SEN_LEN];
		for(int i = 0; i < MAX_SEN_LEN; i++)
			this.segtag[i] = new SegTag();
		this.iStep = new STEP_ATTRIBUTE[MAX_SEN_LEN];
		this.m_Trigram = new CTrigram();
		// 初始化第i步的属性值
		for(int i = 0; i < MAX_SEN_LEN; i++)
		{
			this.iStep[i] = new STEP_ATTRIBUTE();
			this.iStep[i].NodeToiStep = new NODE_COORDINATE[TO_iStep_MAX_NODE_NUM];
			for(int j = 0; j < TO_iStep_MAX_NODE_NUM; j++)
				this.iStep[i].NodeToiStep[j] = new NODE_COORDINATE();
			this.iStep[i].NodeToiStepNum = 0;
		}
		// 初始化第i个节点的属性值
		this.iStepNode = new NODE_ATTRIBUTE[MAX_SEN_LEN][iStep_MAX_NODE_NUM];
		for(int i = 0; i < MAX_SEN_LEN; i++)
			for(int j = 0; j < iStep_MAX_NODE_NUM; j++)
				this.iStepNode[i][j] = new NODE_ATTRIBUTE();
		// 给日本人名姓氏分配空间
		this.JapSurName = new String[10000];
		this.JapSurNameLen = 0;

		// 给cache机构名分配空间
		ORGNAME_TEXT_NUM = new int[1];
		ORGNAME_TEXT = new CACHEENTITY[20000];
		ORGNAME_TEXT_NUM[0] = 0;

		// 给挖过人名用字分配空间
		this.TRANS_ENGLISH_CHARACTER = new String[1000];
		this.TRANS_ENGLISH_CHARACTER_NUM = 0;

	}

	/**
	 * 以段落为单位进行处理，将段落按照分隔符或\n分开成一句句处理
	 * @stage 目前的阶段
	 */
	public String ParagraphProcessing(String sParagraph, int stage)
	{
		String sSentence;
		StringBuffer sResultSentence = new StringBuffer();

		int lastI = 0, i = 0;
		for(i = 0; i < sParagraph.length(); )
		{
			//String temp = sParagraph.substring(i, i + 1);
			//遇到分隔符"。！？：；…，"等时开始实体识别
			if(PublicFunction.ISSEPERATOR(sParagraph.substring(i, i + 1)))
			{
				// Get the Sentence
				while(i < sParagraph.length() && sParagraph.charAt(i) != ' ' && sParagraph.charAt(i) != '\n')
					i++;

				sSentence = PublicFunction.TRIMLEFT(sParagraph.substring(lastI, i) + "  ");
				lastI = i;
				// 考虑空格
				if(lastI + 2 <= sParagraph.length())
				{
					lastI += 2;
					i += 2;
				}
				if(stage == 0)
				{
					//System.out.println("sSentence11: " + sSentence);
					sResultSentence.append(this.SentenceProcessingTagging(sSentence));
				}
				else if(stage == 1)
				{
					//System.out.println("sSentence12: " + sSentence);
					sResultSentence.append(this.SentenceProcessing(sSentence));
				}
				else if(stage == 2)
				{
					//System.out.println("sSentence13: " + sSentence);
					sResultSentence.append(this.SentenceProcessing2(sSentence));
				}
				continue;
			}
			//一行没有分隔符也没有超过最大长度
			else if(sParagraph.charAt(i) == '\n')
			{
				sSentence = sParagraph.substring(lastI, i) + "  ";
				lastI = i + 1;
				if(stage == 0)
				{
					//System.out.println("sSentence21: " + sSentence);
					sResultSentence.append(this.SentenceProcessingTagging(sSentence) );
					sResultSentence.append("\n");
				}
				else if(stage == 1)
				{
					//System.out.println("sSentence22: " + sSentence);
					sResultSentence.append(this.SentenceProcessing(sSentence));
					sResultSentence.append("\n");
				}
				else if(stage == 2)
				{
					//System.out.println("sSentence23: " + sSentence);
					sResultSentence.append(this.SentenceProcessing2(sSentence));
					sResultSentence.append("\n");
				}
				i++;
				continue;
			}
			else
				i++;
		}
		//处理余留情况
		if(lastI < sParagraph.length() && lastI  < i)
		{
			sSentence = sParagraph.substring(lastI) + "  ";
			if(stage == 0)
			{
				//System.out.println("sSentence31: " + sSentence);
				sResultSentence.append(this.SentenceProcessingTagging(sSentence));
			}
			else if(stage == 1)
			{
				//System.out.println("sSentence32: " + sSentence);
				sResultSentence.append(this.SentenceProcessing(sSentence));
			}
			else if(stage == 2)
			{
				//System.out.println("sSentence33: " + sSentence);
				sResultSentence.append(this.SentenceProcessing2(sSentence));
			}
		}
		return sResultSentence.toString();
	}

	/**
	 * 返回TriGram词性标注的结果,重新对第一步分词结果进行POS标注
	 * @param sSentence
	 * @return
	 */
	public String SentenceProcessingTagging(String sSentence)
	{
		InitNerNet(sSentence);
		TrigramTagging();
		m_Trigram.Create(iStepNode, iStep, iStepNum);
		m_Trigram.PosTriGramViterbi();
		return (m_Trigram.BestTagRoute());
	}

	/**
	 * 返回第一阶段命名实体识别的识别结果
	 * @param sSentence
	 * @return
	 */
	public String SentenceProcessing(String sSentence)
	{
		InitNerNet(sSentence);//named entity recognition base on the sentence.

		//--------atom segmentation--------//
		AtomSegment ();
		//this.print();

		//--------generation the chinese person name--------//
		GeneCHName ();
		//this.print();

		//--------Generation the japanese person name--------//
		GeneJNName ();
		//this.print();

		//--------generation the foreign person name--------//
		GeneFNName ();
		//this.print();

		//--------generation the location name--------//
		GeneLOCName();
		//this.print();

		//--------generation the organization name--------//
		GeneORGName();
		//this.print();

		//--------generation the abbrievation entity--------//
		GeneAbbrLocOrg();
		//this.print();

		m_Trigram.Create(iStepNode, iStep, iStepNum);

		m_Trigram.WordPosTrigramViterbi();

		//不需要记录识别的机构名
		//return(m_Trigram.BestRoute(ORGNAME_TEXT, ORGNAME_TEXT_NUM));
		return(m_Trigram.BestRoute());
	}

	public void print()
	{
		for(int i  = 0; i < iStepNum; i++)
		{
			System.out.println();
			System.out.println();
			System.out.println("第" + i + "步: "+iStep[i].iStepNodeNum);
			for(int j = 0; j < iStep[i].iStepNodeNum; j++)
				System.out.println("sWord: "+iStepNode[i][j].sWord +" sPos: "+ iStepNode[i][j].sPos +" fPosProb: "+  iStepNode[i][j].fPosProb
						+ " fProb: "+ iStepNode[i][j].fProb +" nNext_word: "+  iStepNode[i][j].nNext_word);
		}
	}

	/**
	 * 返回第二阶段命名实体失败的识别结果
	 * @param sSentence
	 * @return
	 */
	public String SentenceProcessing2(String sSentence)
	{
		InitNerNet(sSentence);//named entity recognition base on the sentence.

		AtomSegment2();//atom segmentation

		GeneORGName2();

		m_Trigram.Create(iStepNode, iStep, iStepNum);

		m_Trigram.WordPosTrigramViterbi();
		m_Trigram.WordPosTrigramViterbi2();

		return(m_Trigram.BestNerRoute());
	}

	/**
	 * 将第切分与初步POS标注的结果填充到segtag中，并且将iStep中的每个节点的节点数目置0
	 * @param sSentence
	 * @return
	 */
	public boolean InitNerNet(String sSentence)
	{
		this.segtagword_num = 0;
		Pattern find = Pattern.compile(".*?/.*?  ");
		Matcher mt = find.matcher(sSentence);
		int s = 0;
		String temp;
		int in = 0;
		while(mt.find(s))
		{
			s = mt.end();
			temp = sSentence.substring(mt.start(), mt.end());
			in = temp.lastIndexOf('/');
			this.segtag[segtagword_num].sWord = temp.substring(0, in);
			this.segtag[segtagword_num].sPos = temp.substring(in+1).trim();
			segtagword_num++;
		}

		// 网络开始建立之前，指向每个节点的节点数目为0
		for(int i = 0; i < MAX_SEN_LEN; i++)
		{
			iStep[i].NodeToiStepNum = 0;
		}
		return true;
	}

	/**
	 * 使用Trigram进行词性标记，在分词阶段的标注注重的速度，性能较差
	 * @return
	 */
	public boolean TrigramTagging()
	{
		this.iStepNum = 0;
		int i = 0, j = 0;
		String ch;
		int tposnum = 0, twordid, tposid;
		String tword, tpos;
		double wordpos = 0;

		for(i = 0; i < this.segtagword_num; i++)
		{
			iStep[iStepNum].iStepNodeNum = 0;
			ch = segtag[i].sWord.substring(0, 1);
			if(PublicFunction.ISCHINESEWORD(ch))//中文汉字
			{
				tword = segtag[i].sWord;
				twordid = CTrigram.source_WORDPOS.getWordID(tword);
				if(twordid < 0)
				{
					this.SetNodeValue(iStepNum, iStep[iStepNum].iStepNodeNum, segtag[i].sWord, segtag[i].sPos, 0, 0, iStepNum + 1);
					iStep[iStepNum].iStepNodeNum++;
					iStepNum++;
					continue;
				}
				tposnum = CTrigram.source_WORDPOS.getNumofWordPOS(twordid);
				for(j = 0; j < tposnum; j++)
				{
					tposid = CTrigram.source_WORDPOS.getIDofWordPOS(twordid, j);
					tpos = CTrigram.source_WORDPOS.getPOSString(tposid);
					wordpos = CTrigram.source_WORDPOS.getPOSWordProb(twordid, tposid);
					if(wordpos == -1)
						wordpos = CTrigram.source_WORDPOS.smoothData(6);
					this.SetNodeValue(iStepNum, iStep[iStepNum].iStepNodeNum, segtag[i].sWord, tpos, 0, Math.log10(wordpos), iStepNum+1);
					iStep[iStepNum].iStepNodeNum++;
				}
				iStepNum++;
			}
			else
			{
				this.SetNodeValue(iStepNum, iStep[iStepNum].iStepNodeNum, segtag[i].sWord,segtag[i].sPos, 0, 0, iStepNum + 1);
				iStep[iStepNum].iStepNodeNum++;
				iStepNum++;
			}
		}
		return true;
	}

	/**
	 * 按照汉字分
	 * @return
	 */
	public boolean AtomSegment()
	{
		iStepNum = 0;
		int i = 0, j = 0, len = 0;
		for(i = 0; i < segtagword_num; i++)
		{
			String temp = segtag[i].sWord;
			len = temp.length();
			if(PublicFunction.ISCHINESEWORD(temp))//中文汉字
			{
				for(j = 0; j < len; j++)
				{
					iStep[iStepNum].iStepNodeNum = 0;
					SetNodeValue(iStepNum, iStep[iStepNum].iStepNodeNum, segtag[i].sWord.substring(j, j+1), segtag[i].sPos, 0, 0, iStepNum + 1);
					iStep[iStepNum].iStepNodeNum++;
					iStepNum++;
				}

				//词性标记统一
				if(segtag[i].sPos.equals("t")) segtag[i].sPos = "TIM";
				else if(segtag[i].sPos.equals("m")) segtag[i].sPos = "NUM";
				else if(segtag[i].sPos.equals("ns")) segtag[i].sPos = "LOC";
				else if(segtag[i].sPos.equals("nt")) segtag[i].sPos = "ORG";

				SetNodeValue(iStepNum - len, iStep[iStepNum - len].iStepNodeNum, segtag[i].sWord, segtag[i].sPos, 0, 0, iStepNum);
				iStep[iStepNum - len].iStepNodeNum++;
			}
			else
			{
				for(iStep[iStepNum].iStepNodeNum = 0; iStep[iStepNum].iStepNodeNum < 2; iStep[iStepNum].iStepNodeNum++)
				{
				  //词性标记统一
				  if(segtag[i].sPos.equals("t")) segtag[i].sPos = "TIM";
				  else if(segtag[i].sPos.equals("m")) segtag[i].sPos = "NUM";
				  else if(segtag[i].sPos.equals("ns")) segtag[i].sPos = "LOC";
				  else if(segtag[i].sPos.equals("nt")) segtag[i].sPos = "ORG";

				  SetNodeValue(iStepNum, iStep[iStepNum].iStepNodeNum, segtag[i].sWord, segtag[i].sPos, 0, 0, iStepNum + 1);
				}
				iStepNum++;
			 }
		}
		return true;
	}

	/**
	 * 第二阶段按照汉字分
	 * @return
	 */
	public boolean AtomSegment2()
	{
		iStepNum = 0; int i = 0;
		for(i = 0 ; i < segtagword_num; i++)
		{
		  iStep[i].iStepNodeNum = 0;
		  SetNodeValue(i, iStep[i].iStepNodeNum, segtag[i].sWord, segtag[i].sPos, 0, 0, iStepNum + 1);
		  iStep[i].iStepNodeNum++;
		  iStepNum++;
		}
		return true;
	}

	/**
	 * 设置某一个节点的各个属性
	 * @param istep_value
	 * @param jstate_value
	 * @param word_value
	 * @param pos_value
	 * @param prob_value
	 * @param prob_value_pos
	 * @param next_word_value
	 * @return
	 */
	public boolean SetNodeValue(
			int istep_value,
			int jstate_value,
			String word_value,
			String pos_value,
			double prob_value,
			double prob_value_pos,
			int next_word_value
			)
	{
		//如果句子太长,系统会出错
		  if(istep_value >= MAX_SEN_LEN || next_word_value >= MAX_SEN_LEN) {
			  System.out.println("Sentence is too long!\n");
			  return false;
		  }
		  iStepNode[istep_value][jstate_value].sWord = word_value;
		  iStepNode[istep_value][jstate_value].sPos = pos_value;
		  iStepNode[istep_value][jstate_value].fProb = prob_value;
		  iStepNode[istep_value][jstate_value].fPosProb = prob_value_pos;
		  iStepNode[istep_value][jstate_value].nNext_word = next_word_value;

		  if(next_word_value > -1){
			  	//iStep[next_word_value].NodeToiStep[iStep[next_word_value].NodeToiStepNum] = new NODE_COORDINATE();
			    iStep[next_word_value].NodeToiStep[iStep[next_word_value].NodeToiStepNum].nCol = istep_value;
			    iStep[next_word_value].NodeToiStep[iStep[next_word_value].NodeToiStepNum].nRow = jstate_value;
				if(iStep[next_word_value].NodeToiStepNum >= TO_iStep_MAX_NODE_NUM)
					iStep[next_word_value].NodeToiStepNum = iStep[next_word_value].NodeToiStepNum;
			    iStep[next_word_value].NodeToiStepNum++; //指向iStep = next_word_value的节点数增加1
		}
		return true;
	}

	/**
	 * 设置某一个候选实体各个属性值
	 * @param candi_entity
	 * @param entity_id
	 * @param entity
	 * @return
	 */
	public boolean SetEntityValue(
			CANDI_ENTITY[] candi_entity,
			int entity_id,
			CANDI_ENTITY entity)
	{
		candi_entity[entity_id] = entity;
		return true;
	}

	/**
	 *产生候选中国人名
	 */
	public void GeneCHName()
	{
		int i = 0, j =0;
		String chname;
		double dPerProb = 0;
		int first, second, mid = CTrigram.source.getWordID("APER");
		for(i = 0; i < iStepNum; i++)
		{
			//--------复姓中国人名候选，例如：欧阳修--------//
			if(iStepNode[i][1].sWord.length() == 2 && PublicFunction.ISCHSURNAME(iStepNode[i][1].sWord))
			{
				chname = iStepNode[i][1].sWord;

				dPerProb = this.CacuCHNameProbFromWord(chname);
				SetNodeValue(i, iStep[i].iStepNodeNum, chname, "APER", dPerProb, 0, i+2);

				iStep[i].iStepNodeNum++;

				for(j = 2; i+j < iStepNum && j < 4; j++)
				{
					if(PublicFunction.ISCHINESEWORD(iStepNode[i+j][0].sWord))
					{
						chname = chname +"  " + iStepNode[i+j][0].sWord;
					}
					else
						break;//如果退出循环,说明其中有非中文字符
					dPerProb = this.CacuCHNameProbFromWord(chname);
					SetNodeValue(i, iStep[i].iStepNodeNum, chname, "PER", dPerProb, dPerProb * PERWORD_POS, i+j+1);

					iStep[i].iStepNodeNum++;
				}
				continue;
			}

			//--------单姓中国人名候选，例如：吴友政--------//
			if(iStepNode[i][0].sWord == iStepNode[i][1].sWord && PublicFunction.ISCHSURNAME(iStepNode[i][0].sWord))
			{
				chname = iStepNode[i][0].sWord;

				 //--------单字人名的产生有一定的限制--------//
				if(i + 1 < iStepNum && (second = CTrigram.source.getWordID(iStepNode[i+1][1].sWord)) >= 0
						&& CTrigram.source.getBigramID(second, mid) >= 0)
				{
					dPerProb = this.CacuCHNameProbFromWord(chname);
					SetNodeValue(i, iStep[i].iStepNodeNum, chname, "APER", dPerProb, 0, i+ 1);
					iStep[i].iStepNodeNum++;
				}
				//--------靠前文的约束产生单字人名--------//
				else
				  {
					  j = 1;
					  while(i - j >=0)
					  {
						  if(iStep[i - j].iStepNodeNum <= 1) { j++; continue; }
						  else if ((first = CTrigram.source.getWordID(iStepNode[i - j][1].sWord)) >= 0
							&& CTrigram.source.getBigramID(mid,first) >= 0)
						  {
							dPerProb = CacuCHNameProbFromWord(chname);
							SetNodeValue(i, iStep[i].iStepNodeNum, chname, "APER", dPerProb, 0, i + 1);
							iStep[i].iStepNodeNum++;
						  }
						  break;
					  }
				  }

					  /*
					  //--------没有任何限制地产生单字人名--------//
					  dPerProb = CacuCHNameProbFromWord(chname);
					  SetNodeValue(i, iStep[i].iStepNodeNum, chname, "APER", dPerProb, 0, i + 1);
					  iStep[i].iStepNodeNum++;
					  */

					  for(j = 1; i + j < iStepNum && j < 3; j++)
					  {
						if(PublicFunction.ISCHINESEWORD(iStepNode[i + j][0].sWord)){
						  chname = chname + "  " + iStepNode[i+j][0].sWord ;
						}
						else break;//如果退出循环,说明其中有非中文字符

						dPerProb = CacuCHNameProbFromWord(chname);
						SetNodeValue(i, iStep[i].iStepNodeNum, chname, "PER", dPerProb, dPerProb * PERWORD_POS, i + j + 1);

					    iStep[i].iStepNodeNum++;
					  }
					}
				//--------港人婚后姓名,例如：陈方安生--------//
				if(i + 1 < iStepNum && iStepNode[i][1].sWord.length() == 2 && iStepNode[i + 1][1].sWord.length() == 2
					&& PublicFunction.ISCHSURNAME(iStepNode[i][0].sWord) && PublicFunction.ISCHSURNAME(iStepNode[i + 1][0].sWord) )
				{
				  chname = iStepNode[i][0].sWord + "  " + iStepNode[i + 1][0].sWord;

				  for(j = 2; i + j < iStepNum && j < 4; j++)
				  {
					if(PublicFunction.ISCHINESEWORD(iStepNode[i + j][0].sWord)){
					  chname = chname + "  " + iStepNode[i+j][0].sWord ;
					}
					else break;//如果退出循环,说明其中有非中文字符
				  }
				  if(j == 4){
				    dPerProb = CacuHKNameProbFromWord(chname);
				    SetNodeValue(i, iStep[i].iStepNodeNum, chname, "PER", dPerProb, dPerProb * PERWORD_POS, i + j);
				    iStep[i].iStepNodeNum++;
				  }
				}
			}
	}

	/**
	 *产生候选外国人名
	 */
	public void GeneFNName()
	{
		int i = 0, j = 0; String fnname; double dPerProb = 0;
		for(i = 0 ; i < iStepNum; i++)
		{
			if(IsFPNC(iStepNode[i][0].sWord))
			{
				if(iStepNode[i][0].sPos == "w") continue;
				fnname = iStepNode[i][0].sWord;

				//--------产生候选的外国人名--------//
				for(j = 1; j < FN_NAME_LEN; j++)
				{
					if(i + j >= iStepNum) break;
					if(!IsFPNC(iStepNode[i + j][0].sWord)) break;

					fnname = fnname + "  " + iStepNode[i + j][0].sWord;

					if(iStepNode[i + j][0].sPos == "w") continue;

					dPerProb = CacuFNNameProbFromWord(fnname);
					SetNodeValue(i, iStep[i].iStepNodeNum, fnname, "PER", dPerProb, dPerProb * PERWORD_POS, i + j + 1);

					iStep[i].iStepNodeNum++;
				}
				//--------产生结束--------//
			}
		}
	}

	/**
	 *产生候选日本人名
	 */
	public void GeneJNName()
	{
		int i = 0, j = 0; String jnname; double dPerProb = 0;
		for(i = 0 ; i < iStepNum; i++)
		{
			if(i + 1 < iStepNum)
			{
				jnname = iStepNode[i][0].sWord + iStepNode[i + 1][0].sWord;
				if(IsJapSurName(jnname))
				{
					//--------仅仅是姓氏的日本人名，例如：西野/JPER  先生--------//
					dPerProb = CacuJNNameProbFromWord(jnname);
					SetNodeValue(i, iStep[i].iStepNodeNum, jnname, "PER", dPerProb, dPerProb * PERWORD_POS, i + 2);
					iStep[i].iStepNodeNum++;

					//--------包括名字的本人名候选产生，例如：西野/JPER  文人--------//
					for(j = 2; j <= JN_NAME_LEN;j++)
					{
						if(i + j >= iStepNum) break;
						jnname = jnname + "  " + iStepNode[i + j][0].sWord;
						if(iStepNode[i + j][0].sPos == "w") continue;

						dPerProb = CacuJNNameProbFromWord(jnname);
						SetNodeValue(i, iStep[i].iStepNodeNum, jnname, "PER", dPerProb, dPerProb * PERWORD_POS, i + j + 1); //* 0.5

						iStep[i].iStepNodeNum++;
					}
				}
			}
		}
	}

	/**
	 *产生候选地名
	 */
	public void GeneLOCName()
	{
		int i = 0, j = 0, k = 0, col = 1, row = 0, MAX_LOC_LEN;
		CANDI_ENTITY sLocname = new CANDI_ENTITY();
		//int candi_loc_len = 0;
		String sProb = null;
		//--------由当前触发条件产生的候选实体-地名--------//
		CANDI_ENTITY[] m_CandiLoc = new CANDI_ENTITY[MAX_CANDI_LOC];
		int all_candi_loc_num = 0, current_candi_loc_num = 0, previous_candi_loc_num = 0;
		boolean EndLoop = false;

		for(i = 0; i < iStepNum; i++)
		{
			//--------由关键词触发地名的识别--------//
			if(iStep[i].iStepNodeNum > 1 && !iStepNode[i][1].sWord.equals("") && PublicFunction.ISLOCKEYWORD(iStepNode[i][1].sWord) &&
					!iStepNode[i][1].sPos.equals("PER") && !iStepNode[i][1].sPos.equals("APER"))
			{
				if(iStepNode[i][1].sWord.length() == 1)
					MAX_LOC_LEN = MAX_LOC_LEN_SINGLEKEYWORD;
				else
					MAX_LOC_LEN = MAX_LOC_LEN_DOUBLEKEYWORD;
				EndLoop = false;
				sLocname.sEntity = iStepNode[i][1].sWord;
				sLocname.sWordPattern = iStepNode[i][1].sWord;
				sLocname.sPosPattern = iStepNode[i][1].sPos;
				sLocname.nEntity_length = iStepNode[i][1].sWord.length();
				sLocname.nCurrent_word = i;
				sLocname.bComplete = false;
				sLocname.nNext_word = iStepNode[i][1].nNext_word;
				SetEntityValue(m_CandiLoc, 0, sLocname);
				all_candi_loc_num = 1;
				previous_candi_loc_num = 0;

				while(!EndLoop)
				{
					for(j = previous_candi_loc_num; j < all_candi_loc_num; j++)
					{
						if(m_CandiLoc[j].bComplete == true) {
							EndLoop = true;
							continue;
						}
						else
						{
							//--------如果当前候选已经到达了输入串的开始，此后选的产生结束--------//
							if(m_CandiLoc[j].nCurrent_word == 0)
								m_CandiLoc[j].bComplete = true;
							for(k = 0; k < iStep[m_CandiLoc[j].nCurrent_word].NodeToiStepNum;k++)
							{
								if((row = iStep[m_CandiLoc[j].nCurrent_word].NodeToiStep[k].nRow) == 0)
									continue;
								col = iStep[m_CandiLoc[j].nCurrent_word].NodeToiStep[k].nCol;
								if(iStepNode[col][1].sPos == "w")
								{
									m_CandiLoc[j].bComplete = true;
									break;
								}
								//--------候选实体的词形--------//
								sLocname.sEntity = iStepNode[col][row].sWord + "  " + m_CandiLoc[j].sEntity;
								//--------候选实体的模板，用于计算候选实体的概率--------//
								if(iStepNode[col][row].sPos == "LOC" || iStepNode[col][row].sPos == "PER"){
									  sProb = new Double(iStepNode[col][row].fProb).toString();
									  sLocname.sWordPattern = iStepNode[col][row].sPos + "/" + sProb + "  " + m_CandiLoc[j].sWordPattern;
									  sLocname.sPosPattern = iStepNode[col][row].sPos + "/" + sProb + "  " + m_CandiLoc[j].sPosPattern;
							    }
							    else {
									  sLocname.sWordPattern = iStepNode[col][row].sWord + "  " + m_CandiLoc[j].sWordPattern;
									  sLocname.sPosPattern = iStepNode[col][row].sPos + "  " + m_CandiLoc[j].sPosPattern;
							    }
								 //--------候选地名的长度,当前所在的步，以及下面的步和是否结束标志--------//
								 sLocname.nEntity_length = i - col + iStepNode[i][1].sWord.length();
								 sLocname.nCurrent_word = col; sLocname.nNext_word = iStepNode[i][1].nNext_word;
								 sLocname.bComplete = false;
								//--------如果候选地名的长度满足条件，则当前候选有效--------//
								if(sLocname.nEntity_length > MAX_LOC_LEN) m_CandiLoc[j].bComplete = true;
								else {
									//--------万一出现候选实体的个数大于预先设置的最大候选体数，程序直接跳出产生候选实体的过程--------//
									if(all_candi_loc_num + current_candi_loc_num >= MAX_CANDI_LOC)
									{ EndLoop = true; break;}
								    SetEntityValue(m_CandiLoc, all_candi_loc_num + current_candi_loc_num, sLocname);
								    //--------将产生的候选加入网络--------//
								    col = m_CandiLoc[all_candi_loc_num + current_candi_loc_num].nCurrent_word;
								    //--------指向iStepNode[i][1].nNext_word步的节点数超过预设值，退出候选的产生--------//
									if(iStep[iStepNode[i][1].nNext_word].NodeToiStepNum >= TO_iStep_MAX_NODE_NUM)
									{EndLoop = true; break; }
									SetNodeValue(col, iStep[col].iStepNodeNum, sLocname.sEntity, "LOC",
										CacuLOCNameProbFromWord(sLocname.sWordPattern), CacuLOCNameProbFromPos(sLocname.sPosPattern), iStepNode[i][1].nNext_word);

									iStep[col].iStepNodeNum++;

								    current_candi_loc_num++;
								    EndLoop = false;
								  }
							}
						}
						 //--------万一出现候选实体的个数大于预先设置的最大候选体数，程序直接跳出产生候选实体的过程--------//
						  if(EndLoop == true) break;
					}
					previous_candi_loc_num = all_candi_loc_num;
					all_candi_loc_num = previous_candi_loc_num + current_candi_loc_num;
					current_candi_loc_num = 0;
					if(previous_candi_loc_num == all_candi_loc_num) EndLoop = true;
				}

			}
		}
	}

	/**
	 *第一阶段产生候选机构名
	 */
	public void GeneORGName()
	{
		int i = 0, j = 0, k = 0, col = 1, row = 0;
		CANDI_ENTITY sOrgname = new CANDI_ENTITY();
		//int candi_org_len = 0;
		String sProb;
		//--------由当前触发条件产生的候选实体-机构名--------//
		CANDI_ENTITY[] m_CandiOrg = new CANDI_ENTITY [MAX_CANDI_LOC];
		int all_candi_org_num = 0, current_candi_org_num = 0, previous_candi_org_num = 0;
		boolean EndLoop = false;
		for(i = 0 ; i < iStepNum; i++)
		{
			//--------由关键词触发机构名的识别--------//
			if(iStep[i].iStepNodeNum > 1 &&
				iStepNode[i][1].sWord != "" &&
				PublicFunction.ISORGKEYWORD(iStepNode[i][1].sWord)	&&
				iStepNode[i][1].sPos != "PER"
				&& iStepNode[i][1].sPos != "APER")
			{
			  EndLoop = false;

			  sOrgname.sEntity = iStepNode[i][1].sWord;
			  sOrgname.sWordPattern = iStepNode[i][1].sWord;
			  sOrgname.sPosPattern = iStepNode[i][1].sPos;
			  sOrgname.nEntity_length = iStepNode[i][1].sWord.length();
			  sOrgname.nCurrent_word = i;
			  sOrgname.nNext_word = iStepNode[i][1].nNext_word;
			  sOrgname.bComplete = false;

			  SetEntityValue(m_CandiOrg, 0, sOrgname);
			  all_candi_org_num = 1; previous_candi_org_num = 0;

			  while(!EndLoop)
			  {
				for(j = previous_candi_org_num; j < all_candi_org_num; j++)
				{
				  if(m_CandiOrg[j].bComplete == true)
				  {
					  EndLoop = true;
					  continue;
				  }
				  else
				  {
					//如果当前候选已经到达了输入串的开始，此后选的产生结束
					if(m_CandiOrg[j].nCurrent_word == 0)
						m_CandiOrg[j].bComplete = true;

					for(k = 0; k < iStep[m_CandiOrg[j].nCurrent_word].NodeToiStepNum; k++)
					{
					  if( (row = iStep[m_CandiOrg[j].nCurrent_word].NodeToiStep[k].nRow) == 0)
						  continue;

					  col = iStep[m_CandiOrg[j].nCurrent_word].NodeToiStep[k].nCol;

					  //允许机构名中存在（）对
					  if(iStepNode[col][1].sPos == "w" &&
						  iStepNode[col][1].sWord != "（" &&
						  iStepNode[col][1].sWord != "）"
						  && iStepNode[col][1].sWord != "(" &&
						  iStepNode[col][1].sWord != ")")
					  {
						  m_CandiOrg[j].bComplete = true;
						  break;
					  }

					  //机构名中的（）必须成对出现
					  if((iStepNode[col][1].sWord == "(" || iStepNode[col][1].sWord == "（") &&
						  (sOrgname.sEntity.indexOf(")") == -1 && sOrgname.sEntity.indexOf("）") == -1))
					  {
						  m_CandiOrg[j].bComplete = true;
						  break;
					  }

					  //候选实体的词形
					  sOrgname.sEntity = iStepNode[col][row].sWord + "  " + m_CandiOrg[j].sEntity;

					  if(iStepNode[col][row].sPos == "LOC" ||
						  iStepNode[col][row].sPos == "PER" ||
						  iStepNode[col][row].sPos == "ORG")
					  {
						  sProb = new Double(iStepNode[col][row].fProb).toString();
						  sOrgname.sWordPattern = iStepNode[col][row].sPos + "/" + sProb + "  " + m_CandiOrg[j].sWordPattern;
						  sOrgname.sPosPattern = iStepNode[col][row].sPos + "/" + sProb + "  " + m_CandiOrg[j].sPosPattern;
					  }
					  else
					  {
						  sOrgname.sWordPattern = iStepNode[col][row].sWord + "  " + m_CandiOrg[j].sWordPattern;
						  sOrgname.sPosPattern = iStepNode[col][row].sPos + "  " + m_CandiOrg[j].sPosPattern;
					  }
					  //候选地名的长度,当前所在的步，以及下面的步和是否结束标志
					  sOrgname.nEntity_length = i - col + iStepNode[i][1].sWord.length()/2;
					  sOrgname.nCurrent_word = col;

					  sOrgname.nNext_word = iStepNode[i][1].nNext_word;
					  sOrgname.bComplete = false;

					  //如果候选地名的长度满足条件，则当前候选有效
					  if(sOrgname.nEntity_length > MAX_ORG_LEN)
						  m_CandiOrg[j].bComplete = true;
					  else
					  {
						//万一出现候选实体的个数大于预先设置的最大候选体数，程序直接跳出产生候选实体的过程
						if(all_candi_org_num + current_candi_org_num >= MAX_CANDI_ORG)
						{
							EndLoop = true;
							break;
						}
						else if(iStepNode[col][row].sWord == "VS") //VS通常不能出现在机构名中
						{
							EndLoop = true;
							break;
						}

					    SetEntityValue(m_CandiOrg, all_candi_org_num + current_candi_org_num, sOrgname);

						//将产生的候选加入网络
						if((sOrgname.sEntity.indexOf("(") == -1  && sOrgname.sEntity.indexOf(")") != -1) ||
							(sOrgname.sEntity.indexOf("（") == -1  && sOrgname.sEntity.indexOf("）") != -1))
						{
							;
						}
						else if(iStepNode[col][row].sPos != "w")
						{
							col = m_CandiOrg[all_candi_org_num + current_candi_org_num].nCurrent_word;
							//指向iStepNode[i][1].nNext_word步的节点数超过预设值，退出候选的产生
							if(iStep[iStepNode[i][1].nNext_word].NodeToiStepNum >= TO_iStep_MAX_NODE_NUM)
							{
								EndLoop = true;
								break;
							}

							SetNodeValue(col, iStep[col].iStepNodeNum, m_CandiOrg[all_candi_org_num + current_candi_org_num].sEntity,
								"ORG", CacuORGNameProbFromWord(sOrgname.sWordPattern), CacuORGNameProbFromPos(sOrgname.sPosPattern),iStepNode[i][1].nNext_word);

							iStep[col].iStepNodeNum++;
						}
					    current_candi_org_num++;
					    EndLoop = false;
					  }
					}
				  }
				  //万一出现候选实体的个数大于预先设置的最大候选体数，程序直接跳出产生候选实体的过程
				  if(EndLoop == true) break;
				}
				previous_candi_org_num = all_candi_org_num;
				all_candi_org_num = previous_candi_org_num + current_candi_org_num;
				current_candi_org_num = 0;
				if(previous_candi_org_num == all_candi_org_num) EndLoop = true;
			  }
			}//end if(ISORGKEYWORD(iStepState[i][1].word))
		}
		//Debug_Candi_Loc(m_CandiOrg, all_candi_org_num, "Test\\CANDI_ORG.txt");
	}

	/**
	 *第二阶段产生候选机构名
	 */
	public void GeneORGName2()
	{
		int i = 0;

		for(i = 0 ; i < iStepNum; i++)
		{
			if(iStepNode[i][0].sPos.equals("w")) continue;
			else if(i + 2 < iStepNum && !iStepNode[i + 1][0].sPos.equals("w") &&
				!iStepNode[i + 2][0].sPos.equals("w"))
				IsCacheORGName(i, i + 2);
			if(i + 1 < iStepNum &&
				!iStepNode[i + 1][0].sPos.equals("w"))
				IsCacheORGName(i, i + 1);
		}
	}

	/********产生简称候选实体，包括地名简称(如: 中/jns 日/jns关系)
	和机构名简称(如：上证所/jnt)********/

	/**
	 * 在命名实体的第一阶段进行简称的识别
	 */
	public void GeneAbbrLocOrg()
	{
		int i = 0, j = 0; double dPerProb = 0;
		int first, secondloc = CTrigram.source_ABBR.getWordID("ALOC"), secondorg = CTrigram.source_ABBR.getWordID("AORG");
		int firstsource, midsource = CTrigram.source.getWordID("ALOC"), secondsource, cur;
		//String tabbrloc; int first1 = 0, first2 = 0;

		for(i = 0 ; i < iStepNum; i++)
		{
			if(iStep[i].iStepNodeNum <= 1 || iStepNode[i][1].sPos == "ALOC") continue;
			first = CTrigram.source_ABBR.getWordID(iStepNode[i][1].sWord);

			//--------简称地名--------//

			//--------当前字 词必须属于地名简称用字, 即变量first和secondloc必须大于等于0, 且变量cur也必须大于等于0--------//
			if(first >= 0 && ( cur = CTrigram.source_ABBR.getBigramID(first,secondloc)) >= 0)
			{
				//--------获取当前词的前一个词在词典中的ID--------//
				j = 1; firstsource = -1;
				while(i - j >= 0)
				{
					if(iStep[i - j].iStepNodeNum <= 1 || iStepNode[i - j][1].nNext_word != i)
					{ j += 1; continue; }
					else if(iStepNode[i - j][1].nNext_word == i)
					{
						if(iStepNode[i - j][iStep[i - j].iStepNodeNum - 1].sPos == "ALOC")
						firstsource = CTrigram.source.getWordID("ALOC");
						else if(PublicFunction.isNAMEDENTITY(iStepNode[i - j][1].sPos))
						firstsource = CTrigram.source.getWordID(iStepNode[i - j][1].sPos);
						else firstsource = CTrigram.source.getWordID(iStepNode[i - j][1].sWord);
						break;
					}
				}

				//-----------靠简称的前文决定是否产生候选-----------//
				if(firstsource >= 0 && CTrigram.source.getBigramID(midsource, firstsource) >= 0)
				{
					dPerProb = CTrigram.source_ABBR.getBigramofWord(iStepNode[i][1].sWord, "ALOC");
					SetNodeValue(i, iStep[i].iStepNodeNum, iStepNode[i][1].sWord,
							"ALOC", Math.log10(dPerProb), 0, i + iStepNode[i][1].sWord.length());
					iStep[i].iStepNodeNum++;
				}
				//---------靠简称的后文决定是否产生候选--------------//
				else
				{
					j = iStepNode[i][1].sWord.length(); cur = -1;
					//--------当前简称的后文要么也属于地名简称,要么和当前简称在训练语料中有共现--------//
					while(i + j < iStepNum)
					{
						//--------判断当前简称的后文是否也是简称--------//
						first = CTrigram.source_ABBR.getWordID(iStepNode[i + j][1].sWord);

						//--------判断当前简称和后文是否有共现--------//
						if(PublicFunction.isNAMEDENTITY(iStepNode[i + j][1].sPos))
						secondsource = CTrigram.source.getWordID(iStepNode[i + j][1].sPos);
						else secondsource = CTrigram.source.getWordID(iStepNode[i + j][1].sWord);

						if(secondsource >= 0 && (cur = CTrigram.source.getBigramID(secondsource, midsource)) >= 0)
						{
							dPerProb = CTrigram.source_ABBR.getBigramofWord(iStepNode[i][1].sWord, "ALOC");
							SetNodeValue(i, iStep[i].iStepNodeNum, iStepNode[i][1].sWord,
								"ALOC", Math.log10(dPerProb), 0, i + iStepNode[i][1].sWord.length());
							iStep[i].iStepNodeNum++;
							break;
						}
						else if(first < 0 && cur < 0) break;
						else j += iStepNode[i + j][1].sWord.length();
					}
				}

				/*
				//--------对地名简称的产生没有上下文限制--------//
				dPerProb = m_Trigram.source_ABBR.GetBigramofWord((char *)iStepNode[i][1].sWord.data(), "ALOC");
				SetNodeValue(i, iStep[i].iStepNodeNum, iStepNode[i][1].sWord,
							"ALOC", log10(dPerProb), 0, i + iStepNode[i][1].sWord.length() / 2);
				iStep[i].iStepNodeNum++;
				*/
			}

			//--------简称机构名--------//
			if(first >= 0 && CTrigram.source_ABBR.getBigramID(first,secondorg) >= 0)
			{
				dPerProb = CTrigram.source_ABBR.getBigramofWord(iStepNode[i][1].sWord, "AORG");
				SetNodeValue(i, iStep[i].iStepNodeNum, iStepNode[i][1].sWord,
							"AORG", Math.log10(dPerProb), 0, i + iStepNode[i][1].sWord.length());
				iStep[i].iStepNodeNum++;
			}
		}
	}

	/**
	 * 判断当前词是否是cache中机构名的组成部分
	 * @param start
	 * @param len
	 * @return
	 */
	public String IsCacheORGName(int start, int len)
	{
		int i, j, situation = 0;
		String wcache, pcache, abbrorg;
		double wabbrprob, pabbrprob, tabbrprob;

		{
			for(j = 0; j < ORGNAME_TEXT_NUM[0]; j++)
			{
				pcache = iStepNode[start][0].sPos;
				wcache = iStepNode[start][0].sWord;

				// 当前词不再cache机构名中
				if((situation = ORGNAME_TEXT[j].SegCacheEntity.indexOf(wcache)) != -1
				|| (pcache.equals("PER") && (situation = ORGNAME_TEXT[j].NonSegCacheEntity.indexOf(wcache)) != -1))
					;
				else
					continue;

				// 是cache中的机构名中的尾词EOO
				if(situation + wcache.length() == ORGNAME_TEXT[j].NonSegCacheEntity.length())
				{
					continue;
				}

				// 是cache中的机构名中的首词-BOO
				else if(situation == 0) {
					if(pcache.equals("PER") || pcache.equals("ORG") || pcache.equals("LOC"))
						wabbrprob = CacuEntityBigram("POS", pcache, "BOO");
					else
						wabbrprob = CacuEntityBigram("WORD", wcache, "BOO");
					pabbrprob = CacuEntityBigram("POS", pcache, "BOO");
				}
				// 是cache中的机构名中的中间词 MOO
				else
				{
					if(pcache.equals("PER") || pcache.equals("ORG") || pcache.equals("LOC"))
						wabbrprob = CacuEntityBigram("POS", pcache, "MOO");
					else
						wabbrprob = CacuEntityBigram("WORD", wcache, "MOO");
					pabbrprob = CacuEntityBigram("POS", pcache, "MOO");
				}
				abbrorg = wcache;
				i = start + 1;
				while(i <= len)
				{
					wcache = iStepNode[i][0].sWord;
					pcache = iStepNode[i][0].sPos;
					if((situation = ORGNAME_TEXT[j].SegCacheEntity.indexOf(wcache)) == -1)
					{
						//部分候选不能加入网络节点，因为其根本不可能是一个简称的机构名
						if(i == start + 1 && ((iStepNode[start][0].sWord.length() <= 1 &&
								PublicFunction.ISCHINESEWORD(iStepNode[start][0].sWord) ||
								iStepNode[start][0].sPos.equals("LOC"))))
							break;
						else if(PublicFunction.ISORGKEYWORD(iStepNode[start][0].sWord))
							break;
						//将产生的候选节点加入网络
						SetNodeValue(start, iStep[start].iStepNodeNum, abbrorg,
							"ORG", wabbrprob, pabbrprob, i);
						iStep[start].iStepNodeNum++;

						break;
					}
					// EOO
					if(situation + wcache.length() == ORGNAME_TEXT[j].SegCacheEntity.length())	{

						if(pcache.equals("PER") || pcache.equals("ORG") || pcache.equals("LOC"))
							tabbrprob = CacuEntityBigram("POS", pcache, "EOO");
						else tabbrprob = CacuEntityBigram("WORD", wcache, "EOO");
						wabbrprob = wabbrprob + tabbrprob;

						tabbrprob = CacuEntityBigram("POS", pcache, "EOO");
						pabbrprob = pabbrprob + tabbrprob;

						abbrorg = abbrorg + "  " + iStepNode[i][0].sWord;

						//将产生的候选节点加入网络
						SetNodeValue(start, iStep[start].iStepNodeNum, abbrorg, "ORG", wabbrprob, pabbrprob, i + 1);
						iStep[start].iStepNodeNum++;
						break;
					}
					//BOO
					else if(situation == 0) {
						//部分候选不能加入网络节点，因为其根本不可能是一个简称的机构名
						if(i == start + 1 && ((iStepNode[start][0].sWord.length() <= 2 &&
							PublicFunction.ISCHINESEWORD(iStepNode[start][0].sWord)) || iStepNode[start][0].sPos.equals("LOC")))
							break;
						else if(PublicFunction.ISORGKEYWORD(iStepNode[start][0].sWord))
							break;

						//将产生的候选节点加入网络
						SetNodeValue(start, iStep[start].iStepNodeNum, abbrorg,
							"ORG", wabbrprob, pabbrprob, i);
						iStep[start].iStepNodeNum++;
						break;
					}
					//MOO
					else {
						abbrorg = abbrorg + "  " + iStepNode[i][0].sWord;

						if(pcache.equals("PER") || pcache.equals("ORG") || pcache.equals("LOC"))
							tabbrprob = CacuEntityBigram("POS", pcache, "MOO");
						else tabbrprob = CacuEntityBigram("WORD", wcache, "MOO");
						wabbrprob = wabbrprob + tabbrprob;

						tabbrprob = CacuEntityBigram("POS", pcache, "MOO");
						pabbrprob = pabbrprob + tabbrprob;

						i++;
					}
				}//while
				//将产生的候选节点加入网络
				if(i > len){
					SetNodeValue(start, iStep[start].iStepNodeNum, abbrorg, "ORG", wabbrprob, pabbrprob, i);
					iStep[start].iStepNodeNum++;
				}
			}//j
		}//tstart
		return "-1"; //不属于cache中的机构名中的词
	}

	/**
	 * 计算简称机构名的概率
	 * @param model
	 * @param current
	 * @param past
	 * @return
	 */
	public double CacuEntityBigram(String model, String future, String past)
	{
		double wabbrprob = 0;

		sNowWord = future;
		sHisWord1 = past;

		if(model == "WORD")
		{
			wabbrprob = CTrigram.source_ORG.getBigramofWord(sNowWord, sHisWord1);
			if(wabbrprob == -1 ) wabbrprob = 0.00002;

			//if(wabbrprob == -1 ) wabbrprob = m_Trigram.source_ORG.SmoothData(6);
		}
		else if(model == "POS")
		{
			wabbrprob = CTrigram.source_ORGPOS.getBigramofWord(sNowWord, sHisWord1);
			if(wabbrprob == -1 ) wabbrprob = 0.00002;

			//if(wabbrprob == -1 ) wabbrprob = m_Trigram.source_ORGPOS.SmoothData(6);
		}

		return Math.log10(wabbrprob);
	}

	/**
	 * 日本人名姓氏
	 * @param jnamefile
	 * @return
	 */
	static public boolean ReadJapSurName(String jnamefile)
	{
		//System.out.println("读取日本人名姓氏文件");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(jnamefile), "gbk"));//new BufferedReader(new FileReader(jnamefile));
			String line;
			while((line = br.readLine()) != null)
			{
				JapSurName_set.add(line);
			}
			br.close();
		}
		catch(Exception e)
		{
			System.out.println("读取日本人名姓氏文件出错");
			return false;
		}
		return true;
	}

	/**
	 * 是否是日本人名姓氏
	 * @param jname
	 * @return
	 */
	public boolean IsJapSurName(String jname)
	{
		return JapSurName_set.contains(jname);
	}

	/*******外国人名用字*****/
	static public boolean ReadFPNC(String fpnfile)
	{
		//System.out.println("读取外国人名用字文件");
		try {
			BufferedReader br =  new BufferedReader(new InputStreamReader(new FileInputStream(fpnfile), "gbk"));//new BufferedReader(new FileReader(fpnfile));
			String line;
			while((line = br.readLine()) != null)
			{
				TRANS_ENGLISH_CHARACTER_SET.add(line);
			}
			br.close();
		}
		catch(Exception e)
		{
			System.out.println("读取外国人名用字文件出错");
			return false;
		}
		return true;
	}
	public boolean IsFPNC(String pfn)
	{
		return TRANS_ENGLISH_CHARACTER_SET.contains(pfn);
	}

	/***计算各个实体的产生概率,即实体的词类和词性模型***/
	public double CacuCHNameProbFromWord(String cname)	{
		int id = 0, prt = 0;
		double fProb = 0, tfProb = 0, tSProb = 0;
		sNowWord = "";
		sHisWord1 = "";
		sHisWord2 = "";

		//“朱镕基”的“镕”不再ＧＢ２３１２编码中，所以无法识别
		if(cname.equals("朱  镕  基"))
			return 1.0;

		while( (prt = cname.indexOf("  ")) != -1)
		{
			id++;
			sHisWord2 = sNowWord;
			sNowWord = cname.substring(0, prt);
			cname = cname.substring(prt+2);

			if(id == 1)
			{
				if(sNowWord.length() == 2)
					sHisWord1 = "PSUR";
				else
					sHisWord1 = "SUR";
				fProb = Math.log10(CTrigram.source_PER.getBigramofWord(sNowWord, sHisWord1));
			}
			else
			{
				sHisWord1 = "DGB";
				tSProb = 0.95648;
				tfProb = CTrigram.source_PER.getTrigramofWord(sNowWord, sHisWord1, sHisWord2);
				if(tfProb == -1)
					tfProb = Math.pow(CTrigram.source_PER.smoothData(6), 2);
				fProb = fProb + Math.log10(tSProb * tfProb);
			}
		}

		if(id == 0)
		{
			 sHisWord1 = "SUR";
			  sNowWord = cname;

			  fProb = Math.log10(CTrigram.source_PER.getBigramofWord(sNowWord, sHisWord1));
		}
		else
		{
		  sHisWord2 = sNowWord;
		  sHisWord1 = "DGE";
		  sNowWord = cname;

		  if(id == 2) tSProb = 1;
		  else if(id == 1) tSProb = 0.0435; //67643/1554450
		  tfProb = CTrigram.source_PER.getTrigramofWord(sNowWord, sHisWord1, sHisWord2);
		  if(tfProb == -1)  tfProb = Math.pow(CTrigram.source_PER.smoothData(6), 2);
		  fProb = fProb + Math.log10(tSProb * tfProb);//
		}
		return fProb;
	}
	public double CacuHKNameProbFromWord(String hkname)	{
		int id = 0, prt = 0; double fProb = 0, tfProb = 0;

		sNowWord = "";
		sHisWord1 = "";
		sHisWord2 = "";

		while( (prt = hkname.indexOf("  ")) != -1)
		{
		  id++;

		  sHisWord2 = sNowWord;
		  sNowWord = hkname.substring(0, prt);
		  hkname = hkname.substring(prt + 2);

		  if(id == 1)
		  {
			  sHisWord1 = "SUR";
			  fProb = Math.log10(CTrigram.source_PER.getBigramofWord(sNowWord, sHisWord1));
		  }
		  else if(id == 2)
		  {
			  sHisWord1 = "SUR";
			  fProb = Math.log10(CTrigram.source_PER.getTrigramofWord(sNowWord, sHisWord1, sHisWord2));
			  fProb = fProb + tfProb;
		  }
		  else
		  {
			  sHisWord1 = "DGB";

			  tfProb = CTrigram.source_PER.getTrigramofWord(sNowWord, sHisWord1, sHisWord2);
			  if(tfProb == -1)  tfProb  = Math.pow(CTrigram.source_PER.smoothData(6), 2);
			  fProb = fProb + Math.log10(tfProb);
		  }
		}

		sHisWord2 = sNowWord;
		sHisWord1 = "DGE";
		sNowWord = hkname;

		tfProb = CTrigram.source_PER.getTrigramofWord(sNowWord, sHisWord1, sHisWord2);

		if(tfProb == -1)  tfProb = Math.pow(CTrigram.source_PER.smoothData(6), 2);
		fProb = fProb + Math.log10(tfProb);

		return fProb + Math.log10(2.3e-5);
	}
	public double CacuJNNameProbFromWord(String jnname)	{
		int id = 0, prt = 0; double fProb = 0, tfProb = 0;

		sNowWord = "";
		sHisWord1 = "";
		sHisWord2 = "";

		while( (prt = jnname.indexOf("  ")) != -1)
		{
		  id++;

		  sHisWord2 = sNowWord;
		  sNowWord = jnname.substring(0, prt);

		  jnname = jnname.substring(prt + 2);
		  if(id == 1)
		  {
			  sHisWord1 = "BJN";
			  if( (tfProb = CTrigram.source_JPN.getBigramofWord(sNowWord, sHisWord1)) == -1)
			    tfProb = CTrigram.source_JPN.smoothData(6);
			  fProb = Math.log10(tfProb);
		  }
		  else
		  {
			sHisWord1 = "MJN";

			if( (tfProb = CTrigram.source_JPN.getTrigramofWord(sNowWord, sHisWord1, sHisWord2)) == -1)
				tfProb = CTrigram.source_JPN.smoothData(6);
			fProb = fProb + Math.log10(tfProb);//
		  }
		}

		sHisWord2 = sNowWord; sHisWord1 = "EJN"; sNowWord = jnname;

		if(id == 0 && (tfProb = CTrigram.source_JPN.getBigramofWord(sNowWord, "BJN")) == -1)
			tfProb = CTrigram.source_JPN.smoothData(6);
		else if(id != 0 && (tfProb = CTrigram.source_JPN.getTrigramofWord(sNowWord,sHisWord1, sHisWord2)) == -1)
		   tfProb = CTrigram.source_JPN.smoothData(6);

		fProb = fProb + Math.log10(tfProb);//

		return fProb;
	}
	public double CacuFNNameProbFromWord(String fname)	{
		int id = 0, prt = 0;
		double fProb = 0, tfProb = 0, tSProb = 0;

		sNowWord = "";
		sHisWord1 = "";
		sHisWord2 = "";

		while( (prt = fname.indexOf("  ")) != -1)
		{
		  id++;

		  sHisWord2 =  sNowWord;
		  sNowWord = fname.substring(0, prt);

		  if(sNowWord.equals("-"))  sNowWord =  "·";

		  fname = fname.substring(prt + 2);

		  if(id == 1)
		  {
			  sHisWord1 = "BFN";

			  if( (tfProb = CTrigram.source_FPN.getBigramofWord(sNowWord, sHisWord1)) == -1)
			    tfProb = CTrigram.source_FPN.smoothData(6);

			  fProb = Math.log10(tfProb);
		  }
		  else
		  {
			sHisWord1 = "MFN";

			if(id == 2) tSProb = 0.95669;//13344/13948
			else if(id == 3) tSProb = 0.7768;//10366/13344
			else tSProb = 0.3764;//6257/16623

			if( (tfProb = CTrigram.source_FPN.getTrigramofWord(sNowWord, sHisWord1, sHisWord2)) == -1)
				tfProb = CTrigram.source_FPN.smoothData(6);
			fProb = fProb + Math.log10(tSProb * tfProb);
		  }
		}

		sHisWord2 = sNowWord;
		sHisWord1 = "EFN";
		sNowWord =fname;

		if(id == 1) tSProb = 0.0433;//604/13978
		else if(id == 2)
			tSProb = 0.2231; //2978/13344
		else tSProb = 0.7; //10366/16623

		if( (tfProb = CTrigram.source_FPN.getTrigramofWord(sNowWord, sHisWord1, sHisWord2)) == -1)
		   tfProb = CTrigram.source_FPN.smoothData(6);
		fProb = fProb + Math.log10(tSProb * tfProb);//

		return fProb;
	}
	public double CacuLOCNameProbFromWord(String slocname)	{
		int prt = 0, id = 0;
		double fObserve = 0, fProb = 0, tfProb = 0;

		sNowWord = "";
		sHisWord1 = "";
		sHisWord2 = "";

		while( (prt = slocname.indexOf("  ")) != -1)
		{
		   id++; fObserve =0;

		   sHisWord2 = sNowWord;
		   sNowWord = slocname.substring(0, prt);
		   slocname = slocname.substring(prt + 2);

		   if( ( prt = sNowWord.indexOf("/")) != -1)
		   {
			 fObserve = Double.parseDouble(sNowWord.substring(prt + 1));
			 sNowWord = sNowWord.substring(0, prt);
		   }
		   if(id == 1)
		   {
			   if( (tfProb = CTrigram.source_LOC.getBigramofWord(sNowWord, "BOL")) == -1)
				   tfProb = CTrigram.source_LOC.smoothData(6);
			   if(fObserve != 0)  fProb = Math.log10(tfProb) + fObserve;
			   else fProb = Math.log10(tfProb);
		   }
		   else
		   {
			   if( (tfProb = CTrigram.source_LOC.getTrigramofWord(sNowWord, "MOL", sHisWord2)) == -1)
				   tfProb = CTrigram.source_LOC.smoothData(6);
			   if(fObserve != 0) fProb = fProb + Math.log10(tfProb) + fObserve;
			   else fProb = fProb + Math.log10(tfProb);
		   }
		}

		sHisWord2 = sNowWord;
		sNowWord = slocname;

		if( (tfProb = CTrigram.source_LOC.getTrigramofWord(sNowWord, "EOL", sHisWord2)) == -1)
			tfProb = CTrigram.source_LOC.smoothData(6);
		fProb = fProb + Math.log10(tfProb);

		return fProb;
	}
	public double CacuLOCNameProbFromPos(String slocname)	{
		int prt = 0, id = 0;
		double fObserve = 0, fProb = 0, tfProb = 0;

		sNowWord = "";
		sHisWord1 = "";
		sHisWord2 = "";

		while( (prt = slocname.indexOf("  ")) != -1)
		{
		   id++;
		   fObserve =0;

		   sHisWord2 = sNowWord;
		   sNowWord = slocname.substring(0, prt);
		   slocname = slocname.substring(prt + 2);
		   if( ( prt = sNowWord.indexOf("/")) != -1){
			 fObserve = Double.parseDouble(sNowWord.substring(prt + 1));
			 sNowWord = sNowWord.substring(0, prt);
		   }
		   if(id == 1)
		   {
			   if( (tfProb = CTrigram.source_LOCPOS.getBigramofWord(sNowWord, "BOL")) == -1)
				   tfProb = CTrigram.source_LOCPOS.smoothData(6);
			   if(fObserve != 0)  fProb = Math.log10(tfProb) + fObserve;
			   else fProb = Math.log10(tfProb);
		   }
		   else
		   {
			   if( (tfProb = CTrigram.source_LOCPOS.getTrigramofWord(sNowWord, "MOL", sHisWord2)) == -1)
				   tfProb = CTrigram.source_LOCPOS.smoothData(6);
			   if(fObserve != 0) fProb = fProb + Math.log10(tfProb) + fObserve;
			   else fProb = fProb + Math.log10(tfProb);
		   }
		}
		sHisWord2 = sNowWord;
		sNowWord = slocname;

		if( (tfProb = CTrigram.source_LOCPOS.getTrigramofWord(sNowWord, "EOL", sHisWord2)) == -1)
			tfProb = CTrigram.source_LOCPOS.smoothData(6);
		fProb = fProb + Math.log10(tfProb);

		return fProb;
	}
	public double CacuORGNameProbFromWord(String sorgname)	{
		int prt = 0, id = 0;
		double fObserve = 0, fProb = 0, tfProb = 0;

		sNowWord = "";
		sHisWord1 = "";
		sHisWord2 = "";

		while( (prt = sorgname.indexOf("  ")) != -1)
		{
		   id++; fObserve = 0;

		   sHisWord2 = sNowWord;
		   sNowWord = sorgname.substring(0, prt);
		   sorgname = sorgname.substring(prt + 2);

		   if( ( prt = sNowWord.indexOf("/")) != -1)
		   {
			 fObserve = Double.parseDouble(sNowWord.substring(prt + 1));
			 sNowWord = sNowWord.substring(0, prt);
		   }
		   if(id == 1)
		   {
			   if( (tfProb = CTrigram.source_ORG.getBigramofWord(sNowWord, "BOO")) == -1)
				   tfProb = CTrigram.source_ORG.smoothData(6);
			   //当ORG, LOC, PER作为机构名的首字时，给与适当的加权
			   if(PublicFunction.isNAMEDENTITY(sNowWord)) tfProb = tfProb * 100;
			   if(fObserve != 0)  fProb = Math.log10(tfProb) + fObserve;
			   else fProb = Math.log10(tfProb);
		   }
		   else
		   {
			   if( (tfProb = CTrigram.source_ORG.getTrigramofWord(sNowWord, "MOO", sHisWord2)) == -1)
				   tfProb = CTrigram.source_ORG.smoothData(6);
			   if(fObserve != 0) fProb = fProb + Math.log10(tfProb) + fObserve;
			   else fProb = fProb + Math.log10(tfProb);
		   }
		}
		//sHisWord1 = "EOO";
		sHisWord2 = sNowWord;
		sNowWord = sorgname;

		if( (tfProb = CTrigram.source_ORG.getTrigramofWord(sNowWord, "EOO", sHisWord2)) == -1)
			tfProb = CTrigram.source_ORG.smoothData(6);
		fProb = fProb + Math.log10(tfProb);

		return fProb;
	}
	public double CacuORGNameProbFromPos(String sorgname)	{
		int prt = 0, id = 0;
		double fObserve = 0, fProb = 0, tfProb = 0;

		sNowWord = "";
		sHisWord1 = "";
		sHisWord2 = "";

		while( (prt = sorgname.indexOf("  ")) != -1)
		{
		   id++; fObserve = 0;

		   sHisWord2 = sNowWord;
		   sNowWord = sorgname.substring(0, prt);
		   sorgname = sorgname.substring(prt + 2);

		   if( ( prt = sNowWord.indexOf("/")) != -1)
		   {
			 fObserve = Double.parseDouble(sNowWord.substring(prt + 1));
			 sNowWord = sNowWord.substring(0, prt);
		   }
		   if(id == 1)
		   {
			   if( (tfProb = CTrigram.source_ORGPOS.getBigramofWord(sNowWord, "BOO")) == -1)
				   tfProb = CTrigram.source_ORGPOS.smoothData(6);
			   if(fObserve != 0)  fProb = Math.log10(tfProb) + fObserve;
			   else fProb = Math.log10(tfProb);
		   }
		   else
		   {
			   if( (tfProb = CTrigram.source_ORGPOS.getTrigramofWord(sNowWord, "MOO", sHisWord2)) == -1)
				   tfProb = CTrigram.source_ORGPOS.smoothData(6);
			   if(fObserve != 0) fProb = fProb + Math.log10(tfProb) + fObserve;
			   else fProb = fProb + Math.log10(tfProb);
		   }
		}

		sHisWord2 = sNowWord;
		sNowWord = sorgname;

		if( (tfProb = CTrigram.source_ORGPOS.getTrigramofWord(sNowWord, "EOO", sHisWord2)) == -1)
			tfProb = CTrigram.source_ORGPOS.smoothData(6);
		fProb = fProb + Math.log10(tfProb);

		return fProb;
	}

	/**
	 * 输出Cache中的机构名实体
	 */
	public void TrimCacheOrg()
	{
		// TODO
	}




	public static void main(String[] args)
	{

	}
}

package com.nlpr.ner;

import com.nlpr.segment.*;
import java.io.*;

/**
 * 求取最佳路径的类
 * @author han
 *
 */
public class CTrigram {

	public static double Lamda = 2.8;

	//////////////////////////////////////////////
	public int HMM_stepnum;
	public NODE_ATTRIBUTE[][] HMM_segtagnernet;
	public STEP_ATTRIBUTE[] HMM_istep;
	public NODE_COORDINATE[] piBestRoute;
	public int BestRouteStep;

	static public CSource source_WORDPOS;
	static public CSource source, source_POS;
	static public CSource source_FPN, source_PER, source_JPN;
	static public CSource source_LOC, source_LOCPOS;
	static public CSource source_ORG, source_ORGPOS;
	static public CSource source_ABBR;

	public static final double SmallThresh = -100000000;

	///////////////////////////////////////////////
	private String History, Present, Future;
	private String PHistory, PPresent, PFuture;


	///////////////////////////////////////////////
	/**
	 *
	 * @return
	 */
	static public boolean LoadNerGramModel(){
		//clock_t start, finish;	int	m_nTime;
		//start = clock();
		//System.out.println("加载命名实体识别模型......");

		/********加载没有简称识别的词性语言模型********/
		//source_POS.LoadDataforLine("data\\POS\\01-06PosGram\\data.dat", "data\\POS\\01-06PosGram\\Parm.txt");
		//source_POS.LoadDataforLine("data\\POS\\02-06PosGram\\data.dat", "data\\POS\\02-06PosGram\\Parm.txt");
		/********加载没有简称识别的词类语言模型********/
		//source.LoadDataforLine("data\\01-06Gram\\gramdata.dat", "data\\01-06Gram\\POSandParm.txt");
		//source.LoadDataforLine("data\\02-06Gram\\gramdata.dat", "data\\02-06Gram\\POSandParm.txt");

		/********加载有简称识别的词性语言模型********/
		//source_POS.LoadDataforLine("data\\PosGram\\data.dat", "data\\PosGram\\Parm.txt");
		//System.out.println("加载有简称识别的词性语言模型......");
		source_POS = new CSource(new File("data/PosGram.txt"));

		/********加载有简称识别的词类语言模型********/
		//source.LoadDataforLine("data\\WordGram\\data.dat", "data\\WordGram\\Parm.txt");
		//System.out.println("加载有简称识别的词类语言模型......");
		source = new CSource(new File("data/WordGram.txt"));

		/********加载外国人名的实体模型********/
		//source_FPN.LoadDataforLine("data\\AFN\\data.dat", "data\\AFN\\Parm.txt");
		//System.out.println("加载外国人名的实体模型......");
		source_FPN = new CSource(new File("data/AFN.txt"));

		/********加载地名的词性实体模型和词类实体模型********/
		//source_LOC.LoadDataforLine("data\\LOC\\data.dat", "data\\LOC\\Parm.txt");
		//source_LOCPOS.LoadDataforLine("data\\LOCPOS\\data.dat","data\\LOCPOS\\Parm.txt");
		//System.out.println("加载地名的词性实体模型和词类实体模型......");
		source_LOC = new CSource(new File("data/LOC.txt"));
		source_LOCPOS = new CSource(new File("data/LOCPOS.txt"));

		/********加载机构名的词性实体模型和词类实体模型********/
		//source_ORG.LoadDataforLine("data\\ORG\\data.dat","data\\ORG\\Parm.txt");
		//source_ORGPOS.LoadDataforLine("data\\ORGPOS\\data.dat","data\\ORGPOS\\Parm.txt");
		//System.out.println("加载机构名的词性实体模型和词类实体模型......");
		source_ORG = new CSource(new File("data/ORG.txt"));
		source_ORGPOS = new CSource(new File("data/ORGPOS.txt"));

		/********加载中国人名实体模型********/
		//source_PER.LoadDataforLine("data\\PER\\data.dat", "data\\PER\\Parm.txt");
		//System.out.println("加载中国人名实体模型......");
		source_PER = new CSource(new File("data/PER.txt"));

		/********加载日本人名的实体模型********/
		//source_JPN.LoadDataforLine("data\\JPN\\data.dat", "data\\JPN\\Parm.txt");
		//System.out.println("加载日本人名的实体模型......");
		source_JPN = new CSource(new File("data/JPN.txt"));

		/********加载简称的实体模型********/
		//source_ABBR.LoadDataforLine("data\\ABBR\\data.dat", "data\\ABBR\\Parm.txt");
		//System.out.println("加载简称的实体模型......");
		source_ABBR = new CSource(new File("data/ABBR.txt"));

		/********计算数据加载的耗时********/
		//finish = clock();
		//m_nTime = 1000 * (finish - start)/CLOCKS_PER_SEC;
		//printf("The Loading Spends %dms\n\n",m_nTime/1000);

		return true;
	}

	static public boolean LoadNerGramModel( String sWorkDir ){
		//clock_t start, finish;	int	m_nTime;
		//start = clock();
		//System.out.println("加载命名实体识别模型......");

		/********加载没有简称识别的词性语言模型********/
		//source_POS.LoadDataforLine("data\\POS\\01-06PosGram\\data.dat", "data\\POS\\01-06PosGram\\Parm.txt");
		//source_POS.LoadDataforLine("data\\POS\\02-06PosGram\\data.dat", "data\\POS\\02-06PosGram\\Parm.txt");
		/********加载没有简称识别的词类语言模型********/
		//source.LoadDataforLine("data\\01-06Gram\\gramdata.dat", "data\\01-06Gram\\POSandParm.txt");
		//source.LoadDataforLine("data\\02-06Gram\\gramdata.dat", "data\\02-06Gram\\POSandParm.txt");

		/********加载有简称识别的词性语言模型********/
		//source_POS.LoadDataforLine("data\\PosGram\\data.dat", "data\\PosGram\\Parm.txt");
		//System.out.println("加载有简称识别的词性语言模型......");
		source_POS = new CSource(new File(sWorkDir+"data/PosGram.txt"));

		/********加载有简称识别的词类语言模型********/
		//source.LoadDataforLine("data\\WordGram\\data.dat", "data\\WordGram\\Parm.txt");
		//System.out.println("加载有简称识别的词类语言模型......");
		source = new CSource(new File(sWorkDir+"data/WordGram.txt"));

		/********加载外国人名的实体模型********/
		//source_FPN.LoadDataforLine("data\\AFN\\data.dat", "data\\AFN\\Parm.txt");
		//System.out.println("加载外国人名的实体模型......");
		source_FPN = new CSource(new File(sWorkDir+"data/AFN.txt"));

		/********加载地名的词性实体模型和词类实体模型********/
		//source_LOC.LoadDataforLine("data\\LOC\\data.dat", "data\\LOC\\Parm.txt");
		//source_LOCPOS.LoadDataforLine("data\\LOCPOS\\data.dat","data\\LOCPOS\\Parm.txt");
		//System.out.println("加载地名的词性实体模型和词类实体模型......");
		source_LOC = new CSource(new File(sWorkDir+"data/LOC.txt"));
		source_LOCPOS = new CSource(new File(sWorkDir+"data/LOCPOS.txt"));

		/********加载机构名的词性实体模型和词类实体模型********/
		//source_ORG.LoadDataforLine("data\\ORG\\data.dat","data\\ORG\\Parm.txt");
		//source_ORGPOS.LoadDataforLine("data\\ORGPOS\\data.dat","data\\ORGPOS\\Parm.txt");
		//System.out.println("加载机构名的词性实体模型和词类实体模型......");
		source_ORG = new CSource(new File(sWorkDir+"data/ORG.txt"));
		source_ORGPOS = new CSource(new File(sWorkDir+"data/ORGPOS.txt"));

		/********加载中国人名实体模型********/
		//source_PER.LoadDataforLine("data\\PER\\data.dat", "data\\PER\\Parm.txt");
		//System.out.println("加载中国人名实体模型......");
		source_PER = new CSource(new File(sWorkDir+"data/PER.txt"));

		/********加载日本人名的实体模型********/
		//source_JPN.LoadDataforLine("data\\JPN\\data.dat", "data\\JPN\\Parm.txt");
		//System.out.println("加载日本人名的实体模型......");
		source_JPN = new CSource(new File(sWorkDir+"data/JPN.txt"));

		/********加载简称的实体模型********/
		//source_ABBR.LoadDataforLine("data\\ABBR\\data.dat", "data\\ABBR\\Parm.txt");
		//System.out.println("加载简称的实体模型......");
		source_ABBR = new CSource(new File(sWorkDir+"data/ABBR.txt"));

		/********计算数据加载的耗时********/
		//finish = clock();
		//m_nTime = 1000 * (finish - start)/CLOCKS_PER_SEC;
		//printf("The Loading Spends %dms\n\n",m_nTime/1000);

		return true;
	}
	
	static public boolean LoadTagGramModel(CSource model){
		//System.out.println("加载词性标注模型......");

		/********加载词性标注的语言模型********/
		source_WORDPOS = model;
		return true;
	}

	static public boolean LoadTagGramModel(){
		System.out.println("加载词性标注模型......");

		/********加载词性标注的语言模型********/
		source_WORDPOS = new CSource(new File("data/segmentData.txt"), 1);
		return true;
	}


	public void Create(NODE_ATTRIBUTE[][] segtagnernet, STEP_ATTRIBUTE[] istep, int stepnum)
	{
		this.HMM_segtagnernet = segtagnernet;
		this.HMM_stepnum = stepnum;
		this.HMM_istep = istep;

		piBestRoute = new NODE_COORDINATE[HMM_stepnum];
		for(int i = 0; i < HMM_stepnum; i++)
			piBestRoute[i] = new NODE_COORDINATE();
	}

	/**
	 * 基于词类的Trigram模型进行Viterbi寻优
	 */
	public void WordTriGramViterbi(){
	}

	/**
	 * 基于词类的Bigram模型进行Viterbi进行寻优
	 */
	public void WordBiGramViterbi(){
		// TODO
	}

	/**
	 * 基于词性的Trigram模型进行Viterbi寻优
	 */
	public void PosTriGramViterbi(){
		// 如果当前只有一步，最优路径很容易确定
		if(HMM_stepnum == 0)
		{
			BestRouteStep = 0;
			return;
		}
		else if(HMM_stepnum == 1)
		{
			piBestRoute[0].nCol = 0;
			piBestRoute[0].nRow = HMM_istep[0].iStepNodeNum - 1;
			BestRouteStep = 1;
			return;
		}

		int i = 0, j = 0, k = 0, m = 0;
		int xpos = 0, ypos = 0;

		//-------------动态规划过程中的最少的路径数目--------//
		int ALLPATHNUM = 0;
		for(i = 1; i < HMM_stepnum; i++)
		{
			ALLPATHNUM = ALLPATHNUM + HMM_istep[i].iStepNodeNum * HMM_istep[i].NodeToiStepNum;
		}
		int[] SUMPATHNUM = new int[HMM_stepnum];

		// 在时间的计算中，步是从0到T-1
		double[] pftViterbji = new double[ALLPATHNUM];
		int[] piRoute = new int[ALLPATHNUM];
		double[] pftViterbji_multi_ajik = new double[NERMachine.TO_iStep_MAX_NODE_NUM];
		double ftMaxViterbji_multi_ajik = 0;
		int iMaxIndex = 0, iStep = 0, history = 0;
		double aji0=0, bk0 = 0, aji = 0, ajik = 0, bk = 0;

		// k,j,i这三个表示过去现在和未来
		for(iStep = 1; iStep < HMM_stepnum; iStep++)
		{
			//确定在前面的iStep - 1步中共产生了多少条路经
			if(iStep == 1)
			{
				SUMPATHNUM[iStep] = 0;
				SUMPATHNUM[iStep - 1] = 0;
			}
			else
			{
				SUMPATHNUM[iStep -1] = HMM_istep[iStep -1].NodeToiStepNum * HMM_istep[iStep -1].iStepNodeNum + SUMPATHNUM[iStep - 2];
			}

			for(i = 0; i < HMM_istep[iStep].iStepNodeNum; i++)//第iStep步的节点数，未来
			{
				Future = HMM_segtagnernet[iStep][i].sPos;
				for(j = 0; j < HMM_istep[iStep].NodeToiStepNum; j++)//指向第iStep步第i个节点的节点数
				{
					k = HMM_istep[iStep].NodeToiStep[j].nCol; //哪些步有节点指向第i步第j个节点
					Present = HMM_segtagnernet[k][HMM_istep[iStep].NodeToiStep[j].nRow].sPos;
					// Trigram
					if(HMM_istep[k].NodeToiStepNum != 0)
					{
						for(m = 0; m < HMM_istep[k].NodeToiStepNum; m++) //m 过去
						{
							ypos = HMM_istep[k].NodeToiStep[m].nCol;
							xpos = HMM_istep[k].NodeToiStep[m].nRow;
							History = HMM_segtagnernet[ypos][xpos].sPos;
							history = SUMPATHNUM[k-1] + m + HMM_istep[iStep].NodeToiStep[j].nRow * HMM_istep[k].NodeToiStepNum;
							// ajik是状态转移概率
							ajik = source_WORDPOS.getTrigramofPOS(source_WORDPOS.getPOSID(Future), source_WORDPOS.getPOSID(Present), source_WORDPOS.getPOSID(History));
							// 当没有出现aji转移概率是，赋予一个最小值
							if(ajik == -1)
								ajik = source_WORDPOS.smoothData(6);
							pftViterbji_multi_ajik[m] = pftViterbji[history] + Math.log10(ajik);
						}
						 //找出istep-2步的哪个节点(m)转移到第istep-1步节点(j), 第istep步节点(i)的概率最大
						ftMaxViterbji_multi_ajik = CTrigram.SmallThresh;
						iMaxIndex = -1;
						for(m = 0; m < HMM_istep[k].NodeToiStepNum; m++)
						{
							if(pftViterbji_multi_ajik[m] > ftMaxViterbji_multi_ajik)
							{
								ftMaxViterbji_multi_ajik = pftViterbji_multi_ajik[m];
								iMaxIndex = m;
							}
						}
						assert(iMaxIndex != -1);
						//bk是转台观察概率
						bk = HMM_segtagnernet[iStep][i].fPosProb;
						pftViterbji[SUMPATHNUM[iStep - 1] + j + HMM_istep[iStep].NodeToiStepNum*i] = ftMaxViterbji_multi_ajik + bk;
						if(iMaxIndex == -1)
						{
							System.out.println();
						}
						piRoute[SUMPATHNUM[iStep -1] + j + HMM_istep[iStep].NodeToiStepNum * i] = iMaxIndex;
					}
					//Bigram
					else {
						//aji从词j转移到词i的二元转移概率//bk是状态观察概率
						  //aji0初始转移概率，bk0初始观察概率
						aji0 = 1;
						aji0 = source_WORDPOS.getUnigramofPOS(source_WORDPOS.getPOSID(Present));
						if(aji0 == -1)
							aji0 = source_WORDPOS.smoothData(6);
						bk0 = HMM_segtagnernet[k][HMM_istep[iStep].NodeToiStep[j].nRow].fPosProb;

						aji = source_WORDPOS.getBigramofPOS(source_WORDPOS.getPOSID(Future), source_WORDPOS.getPOSID(Present));
						if(aji == -1) aji = source_WORDPOS.smoothData(6);

						bk = HMM_segtagnernet[iStep][i].fPosProb;
						pftViterbji[SUMPATHNUM[iStep -1] + j + HMM_istep[iStep].NodeToiStepNum * i]
						            = Math.log10(aji0) + bk0 + Math.log10(aji) + bk;
						piRoute[SUMPATHNUM[iStep-1] + j + HMM_istep[iStep].NodeToiStepNum * i] = j;
					}
				}
			}
		}
		//---Viterbi寻优结束
		iStep = HMM_stepnum;
		double ftMaxViterb = CTrigram.SmallThresh;
		int iMaxi = -1, iMaxj = -1, iMaxk = -1;
		for(i = 0; i < HMM_istep[iStep].NodeToiStepNum;i++)
		{
			ypos = HMM_istep[iStep].NodeToiStep[i].nCol;
			xpos = HMM_istep[iStep].NodeToiStep[i].nRow;
			if(ypos == 0) {
				if(HMM_segtagnernet[ypos][xpos].fProb > ftMaxViterb)
				{
					piBestRoute[0].nCol = ypos;
					piBestRoute[0].nRow = xpos;
					BestRouteStep = 1;
					return;
				}
			}
			for(k = 0; k < HMM_istep[ypos].NodeToiStepNum;k++)
			{
				if(pftViterbji[SUMPATHNUM[ypos -1] + k + HMM_istep[ypos].NodeToiStepNum* xpos] > ftMaxViterb)
				{
					ftMaxViterb = pftViterbji[SUMPATHNUM[ypos - 1] + k + HMM_istep[ypos].NodeToiStepNum * xpos];
					iMaxi = xpos;
					iMaxj = ypos;
					iMaxk = k;
				}
			}
		}
		assert(iMaxi != -1);
		assert(iMaxj != -1);
		assert(iMaxk != -1);
		//路径回溯
		BestRouteStep = 0;
		piBestRoute[BestRouteStep].nCol = iMaxj;
		piBestRoute[BestRouteStep].nRow = iMaxi;
		BestRouteStep++;
		piBestRoute[BestRouteStep].nCol = HMM_istep[piBestRoute[BestRouteStep - 1].nCol].NodeToiStep[iMaxk].nCol;
		piBestRoute[BestRouteStep].nRow = HMM_istep[piBestRoute[BestRouteStep - 1].nCol].NodeToiStep[iMaxk].nRow;
		iMaxIndex = iMaxk;
		//从最后一个Viterbi变量直接推断出最后两步的最优路径
		int col_k = 0, col_j = 0;
		for(BestRouteStep = 2; BestRouteStep < HMM_stepnum; BestRouteStep++){
			   col_k = piBestRoute[BestRouteStep - 2].nCol;
			   if ( (col_j = piBestRoute[BestRouteStep - 1].nCol) == 0) break;
			   iMaxIndex = piRoute[SUMPATHNUM[col_k - 1] + piBestRoute[BestRouteStep-2].nRow*HMM_istep[col_k].NodeToiStepNum + iMaxIndex];
			   piBestRoute[BestRouteStep].nCol = HMM_istep[col_j].NodeToiStep[iMaxIndex].nCol;
			   piBestRoute[BestRouteStep].nRow = HMM_istep[col_j].NodeToiStep[iMaxIndex].nRow;
			   if(piBestRoute[BestRouteStep].nCol == 0) {BestRouteStep++; break;}
		}

		return;

	}

	/**
	 * 基于词类和词性的Trigram模型进行Viterbi寻优
	 */
	public void WordPosTrigramViterbi(){
		//如果当前只有一步，最有路径很容易确定
		if(HMM_stepnum == 0) {BestRouteStep = 0; return ;}
		if(HMM_stepnum == 1)
		{
		  piBestRoute[0].nCol = 0;	piBestRoute[0].nRow = 1;	BestRouteStep = 1;
		  return ;
		}

		int i = 0, j = 0, k = 0, m = 0;
		int xpos = 0, ypos = 0;
		//动态规划过程中的最少的路径数目
		//int ALLPATHNUM = MAX_SEN_LEN * iStep_MAX_NODE_NUM * TO_iStep_MAX_NODE_NUM;
		int ALLPATHNUM = 0;
		for(i = 1; i < HMM_stepnum; i++){
		  ALLPATHNUM = ALLPATHNUM + HMM_istep[i].iStepNodeNum * HMM_istep[i].NodeToiStepNum;
		  //if(HMM_istep[i].NodeToiStepNum >j) j = HMM_istep[i].NodeToiStepNum;
		}
		int[] SUMPATHNUM = new int[HMM_stepnum];

		//在实际的运算中，步是从0到T-1
		double[] pftViterbji=new double[ALLPATHNUM];
		int[] piRoute=new int[ALLPATHNUM];
		double[] pftViterbji_multi_ajik = new double[NERMachine.TO_iStep_MAX_NODE_NUM];//TO_iStep_MAX_NODE_NUM,指向iStep的最大节点数
		double ftMaxViterbji_multi_ajik = 0;
		int iMaxIndex = 0, iStep = 0, history = 0;
		double aji0 = 0,
			bk0 = 0, pbk0 = 0,
			aji = 0, paji = 0,
			ajik = 0, pajik = 0,
			bk = 0, pbk = 0;
		//char *History = new char [1024], *Present = new char [1024], *Future = new char [1024];
		//char *PHistory = new char [1024], *PPresent = new char [1024], *PFuture = new char [1024];

		//k, j, i这三个表示过去，现在和未来
		for(iStep = 1; iStep < HMM_stepnum; iStep++){
			//确定在前面的iStep - 1步中共产生了多少条路经
			if(iStep == 1)
			{
				SUMPATHNUM[iStep] = 0; SUMPATHNUM[iStep - 1] = 0;
			}
			else
			{
				SUMPATHNUM[iStep - 1] = HMM_istep[iStep - 1].NodeToiStepNum * HMM_istep[iStep - 1].iStepNodeNum + SUMPATHNUM[iStep - 2];
			}

			for(i = 0; i < HMM_istep[iStep].iStepNodeNum; i++)//第iStep步的节点数，未来
			{
				if(i > 0 && PublicFunction.isNAMEDENTITY(HMM_segtagnernet[iStep][i].sPos))
					Future=HMM_segtagnernet[iStep][i].sPos;
				else
					Future= HMM_segtagnernet[iStep][i].sWord;

				PFuture=HMM_segtagnernet[iStep][i].sPos;
				for(j = 0; j < HMM_istep[iStep].NodeToiStepNum; j++)//指向第iStep步第i个节点的节点数
				{
					k = HMM_istep[iStep].NodeToiStep[j].nCol;//哪些步有节点指向第i步第j个节点

					if(HMM_istep[iStep].NodeToiStep[j].nRow > 0
					&& PublicFunction.isNAMEDENTITY(HMM_segtagnernet[k][HMM_istep[iStep].NodeToiStep[j].nRow].sPos)
					   )
					{
						Present=HMM_segtagnernet[k][HMM_istep[iStep].NodeToiStep[j].nRow].sPos;
					}
					else
						Present=HMM_segtagnernet[k][HMM_istep[iStep].NodeToiStep[j].nRow].sWord;

					PPresent=HMM_segtagnernet[k][HMM_istep[iStep].NodeToiStep[j].nRow].sPos;

					//Trigram
					if(HMM_istep[k].NodeToiStepNum != 0)
					{
					 for(m = 0; m < HMM_istep[k].NodeToiStepNum; m++)//m过去
					 {
						 ypos = HMM_istep[k].NodeToiStep[m].nCol;
						 xpos = HMM_istep[k].NodeToiStep[m].nRow;

						 if(xpos > 0 && PublicFunction.isNAMEDENTITY(HMM_segtagnernet[ypos][xpos].sPos))
							 History=HMM_segtagnernet[ypos][xpos].sPos ;
						 else
							 History= HMM_segtagnernet[ypos][xpos].sWord ;

						 PHistory=HMM_segtagnernet[ypos][xpos].sPos;

						 history = SUMPATHNUM[k - 1] + m + HMM_istep[iStep].NodeToiStep[j].nRow * HMM_istep[k].NodeToiStepNum;

						 //ajik是状态转移概率
						 ajik = source.getTrigramofWord(Future, Present, History);
						 pajik = source_POS.getTrigramofWord(PFuture, PPresent, PHistory);
						 //当没有出现aji转移概率时，赋予一个最小值
						 if(ajik == -1) ajik = source.smoothData(6);
						 if(pajik == -1) pajik = source_POS.smoothData(6);
						 //bk是状态观察概率
						 bk = HMM_segtagnernet[iStep][i].fProb;
						 pbk = HMM_segtagnernet[iStep][i].fPosProb;
						 pftViterbji_multi_ajik[m] = pftViterbji[history] +
						  							Math.log10(ajik) + bk + Lamda * (Math.log10(pajik) + pbk);
					 }

					 //找出istep-2步的哪个节点(m)转移到第istep-1步节点(j), 第istep步节点(i)的概率最大
					 ftMaxViterbji_multi_ajik = CTrigram.SmallThresh;
					 iMaxIndex = -1;
					 for(m = 0; m < HMM_istep[k].NodeToiStepNum; m++){
					  if(pftViterbji_multi_ajik[m] > ftMaxViterbji_multi_ajik){
						ftMaxViterbji_multi_ajik = pftViterbji_multi_ajik[m];
						iMaxIndex = m;
					  }
					 }
					 assert(iMaxIndex != -1);
					 pftViterbji[SUMPATHNUM[iStep - 1] + j + HMM_istep[iStep].NodeToiStepNum * i] = ftMaxViterbji_multi_ajik;
					 piRoute[SUMPATHNUM[iStep - 1] + j + HMM_istep[iStep].NodeToiStepNum * i] = iMaxIndex;
					}
					//Bigram
					else{
					  //aji从词j转移到词i的二元转移概率//bk是状态观察概率
					  //aji0初始转移概率，bk0初始观察概率
					  aji0 = 1;
					  bk0 = HMM_segtagnernet[k][HMM_istep[iStep].NodeToiStep[j].nRow].fProb;
					  pbk0 = HMM_segtagnernet[k][HMM_istep[iStep].NodeToiStep[j].nRow].fPosProb;
					  aji = source.getBigramofWord(Future, Present);
					  paji = source_POS.getBigramofWord(PFuture, PPresent);

					  bk = HMM_segtagnernet[iStep][i].fProb ;
					  pbk = HMM_segtagnernet[iStep][i].fPosProb ;
					  //当没有出现aji转移概率时，赋予一个最小值
					  if(aji == -1) aji = source.smoothData(6);
					  if(paji == -1) paji = source_POS.smoothData(6);

					  pftViterbji[SUMPATHNUM[iStep - 1] + j + HMM_istep[iStep].NodeToiStepNum * i]
						= (Math.log10(aji0) + bk0 + Math.log10(aji) + bk) + Lamda * (Math.log10(aji0) + pbk0 + Math.log10(paji) + pbk);
					  piRoute[SUMPATHNUM[iStep - 1] + j + HMM_istep[iStep].NodeToiStepNum * i] = j;
					}
				}
			}
		}//终结*/

		iStep = HMM_stepnum;
		double ftMaxViterb = CTrigram.SmallThresh;
		int iMaxi = -1, iMaxj = -1, iMaxk = -1;
		for(i = 0; i < HMM_istep[iStep].NodeToiStepNum; i++){
			ypos = HMM_istep[iStep].NodeToiStep[i].nCol;
			xpos = HMM_istep[iStep].NodeToiStep[i].nRow;
			if(ypos == 0) {
			  if(HMM_segtagnernet[ypos][xpos].fProb > ftMaxViterb) {
				piBestRoute[0].nCol = ypos;
				piBestRoute[0].nRow = xpos;
				BestRouteStep = 1;
				return;
			  }
			}
			for(k = 0; k < HMM_istep[ypos].NodeToiStepNum; k++){
				if(pftViterbji[SUMPATHNUM[ypos - 1] + k + HMM_istep[ypos].NodeToiStepNum * xpos] > ftMaxViterb)
				{
					ftMaxViterb = pftViterbji[SUMPATHNUM[ypos - 1] + k + HMM_istep[ypos].NodeToiStepNum * xpos];
					iMaxi = xpos;
					iMaxj = ypos;
					iMaxk = k;
				}
			}
		}
		assert(iMaxi != -1);
		assert(iMaxj != -1);
		assert(iMaxk != -1);

		//路径回溯
		BestRouteStep = 0;
		piBestRoute[BestRouteStep].nCol = iMaxj;
		piBestRoute[BestRouteStep].nRow = iMaxi;
		BestRouteStep++;
		piBestRoute[BestRouteStep].nCol = HMM_istep[piBestRoute[BestRouteStep - 1].nCol].NodeToiStep[iMaxk].nCol;
		piBestRoute[BestRouteStep].nRow = HMM_istep[piBestRoute[BestRouteStep - 1].nCol].NodeToiStep[iMaxk].nRow;
		iMaxIndex = iMaxk;
		//从最后一个Viterbi变量直接推断出最后两步的最优路径
		int col_k = 0, col_j = 0;
		for(BestRouteStep = 2; BestRouteStep < HMM_stepnum; BestRouteStep++){
		   col_k = piBestRoute[BestRouteStep - 2].nCol;
		   col_j = piBestRoute[BestRouteStep - 1].nCol;
		   if (col_j == 0)
			   break;
		   iMaxIndex = piRoute[SUMPATHNUM[col_k - 1] + piBestRoute[BestRouteStep-2].nRow * HMM_istep[col_k].NodeToiStepNum + iMaxIndex];
		   piBestRoute[BestRouteStep].nCol = HMM_istep[col_j].NodeToiStep[iMaxIndex].nCol;
		   piBestRoute[BestRouteStep].nRow = HMM_istep[col_j].NodeToiStep[iMaxIndex].nRow;
		   if(piBestRoute[BestRouteStep].nCol == 0)
		   {
			   BestRouteStep++;
			   break;
		   }
		}

		return;
	}

	/**
	 * 第二阶段的基于词类和词性的Trigram模型进行Viterbi寻优
	 * 主要是当stepnum = 1时的处理不同
	 */
	public void WordPosTrigramViterbi2(){
		//如果当前只有一步，最有路径很容易确定
		if(HMM_stepnum == 0) {BestRouteStep = 0; return ;}
		if(HMM_stepnum == 1)
		{
		  piBestRoute[0].nCol = 0;	piBestRoute[0].nRow = 0;	BestRouteStep = 1;
		  return ;
		}
	}

	public String BestTagRoute()
	{
		int prt = 0, iStep = 0, row = 0, col = 0;
		SegTag[] sResult = new SegTag[HMM_stepnum];
		for(int i = 0; i < HMM_stepnum; i++)
			sResult[i] = new SegTag();
		int sResultNum = 0;

		for(iStep = BestRouteStep -1; iStep >=0; iStep--)
		{
			row = piBestRoute[iStep].nRow;
			col = piBestRoute[iStep].nCol;
			sResult[sResultNum].sWord = HMM_segtagnernet[col][row].sWord;
			sResult[sResultNum].sPos = HMM_segtagnernet[col][row].sPos;
			sResultNum++;
		}
		StringBuffer sOut = new StringBuffer();
		for(int i = 0; i < sResultNum; i++)
		{
			if(PublicFunction.isNAMEDENTITY(sResult[i].sPos))
			{
				while((prt = sResult[i].sWord.indexOf("  ")) != -1)
				{
					sResult[i].sWord = sResult[i].sWord.substring(0, prt) + sResult[i].sWord.substring(prt + 2);
				}
				sOut.append(sResult[i].sWord);
				sOut.append("/");
				sOut.append(sResult[i].sPos);
				sOut.append("  ");
			}
			else
				sOut.append(sResult[i].sWord +"/" + sResult[i].sPos + "  ");
		}
		for(int i = 0; i < piBestRoute.length; i++)
		{
			piBestRoute[i] = new NODE_COORDINATE();
		}
		String ss = sOut.toString();
		return ss;
	}

	public String BestNerRoute(){
	    int prt = 0, iStep = 0, row = 0, col = 0;
		SegTag[] sResult = new SegTag [HMM_stepnum]; int sResultNum = 0;

		for(iStep = BestRouteStep - 1; iStep >= 0; iStep--)
		{
		  row = piBestRoute[iStep].nRow; col = piBestRoute[iStep].nCol;

			if(HMM_segtagnernet[col][row].sPos == "nr" && HMM_segtagnernet[col][row].sWord.length() > 2)
			{
				sResult[sResultNum].sWord = HMM_segtagnernet[col][row].sWord;
				sResult[sResultNum].sPos = "PER";
				sResultNum++;
			}
			else
			{
				sResult[sResultNum].sWord  = HMM_segtagnernet[col][row].sWord ;
				sResult[sResultNum].sPos = HMM_segtagnernet[col][row].sPos;
				sResultNum++;
			}
		}

		String sOut = "";
		for(int  i = 0; i < sResultNum; i++){
			if(PublicFunction.isNAMEDENTITY(sResult[i].sPos))
			{
				while( ( prt = sResult[i].sWord.indexOf("  ")) != -1)
				{
					sResult[i].sWord = sResult[i].sWord.substring(0, prt) + sResult[i].sWord.substring(prt + 2);
				}
				sOut = sOut + sResult[i].sWord + "/" + sResult[i].sPos + "  ";
			}
			else sOut = sOut + sResult[i].sWord + "/" + sResult[i].sPos + "  ";
		}
		return sOut;
	}

	/**
	 * 存储Viterbi识别出的实体名
	 * @param lattice
	 * @param latticeid
	 * @return
	 */
	public String BestRoute(CACHEENTITY[] lattice, int[] latticeid)
	{
		// TODO
		return null;
	}

	public String BestRoute()
	{
		int prt = 0, iStep = 0, row = 0, col = 0;
		SegTag[] sResult = new SegTag [HMM_stepnum]; int sResultNum = 0;
		for(int i = 0; i < HMM_stepnum; i++)
			sResult[i] = new SegTag();

		for(iStep = BestRouteStep - 1; iStep >= 0; iStep--)
		{
		  row = piBestRoute[iStep].nRow;
		  col = piBestRoute[iStep].nCol;

			if(HMM_segtagnernet[col][row].sPos.equals("nr") && HMM_segtagnernet[col][row].sWord.length() > 1)
			{
				sResult[sResultNum].sWord = HMM_segtagnernet[col][row].sWord;
				sResult[sResultNum].sPos = "PER";
				sResultNum++;
			}
			else
			{
				sResult[sResultNum].sWord  = HMM_segtagnernet[col][row].sWord ;
				sResult[sResultNum].sPos = HMM_segtagnernet[col][row].sPos;
				sResultNum++;
			}
			//--------合并如" 李  ××", "  李  ××  "的分词结果--------//
			if(sResultNum >=2 && (sResult[sResultNum -1].sWord.equals("××") || sResult[sResultNum -1].sWord.equals("×")) && (sResult[sResultNum - 2].sPos.equals("APER")))
			{
				sResult[sResultNum - 2].sWord = sResult[sResultNum - 2].sWord + sResult[sResultNum - 1].sWord;
				sResult[sResultNum - 2].sPos = "PER";
				sResultNum--;
			}
		}

		String sOut = "";
		for(int  i = 0; i < sResultNum; i++){
			if(PublicFunction.isNAMEDENTITY(sResult[i].sPos))
			{
				while( ( prt = sResult[i].sWord.indexOf("  ")) != -1)
				{
					String ss1 = sResult[i].sWord.substring(0, prt);
					String ss2 = sResult[i].sWord.substring(prt + 2);
					sResult[i].sWord =  ss1+ss2 ;
				}
				//--------简称如京津、港澳等拆开--------//
				if(sResult[i].sWord.length() == 2 && PublicFunction.SELF_CONTRADICTION_ABBR_LOC.contains(sResult[i].sWord))
				sOut = sOut + sResult[i].sWord.substring(0, 1) + "/ALOC  " + sResult[i].sWord.substring(1, 2) + "/ALOC  ";
				else sOut = sOut + sResult[i].sWord + "/" + sResult[i].sPos + "  ";

				/********保存此阶段识别出的结构名,方便下一阶段机构名简称的识别******/
				/********此处取出Cache中的重复串，应该使用BinSearch提高速度********/
			}
			//简称(没有标记为ALOC的简称)拆开
			else if(sResult[i].sWord.length() == 2 && sResult[i].sPos.equals("j") && PublicFunction.SELF_CONTRADICTION_ABBR_LOC.contains(sResult[i].sWord))
			sOut = sOut + sResult[i].sWord.substring(0, 1) + "/ALOC  " + sResult[i].sWord.substring(1, 2) + "/ALOC  ";
			else sOut = sOut + sResult[i].sWord + "/" + sResult[i].sPos + "  ";
		}
		return sOut;
	}
}

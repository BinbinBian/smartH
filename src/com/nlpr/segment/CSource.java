package com.nlpr.segment;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Vector;
import gnu.trove.TObjectIntHashMap;

/**
 * 用于存储word,pos及其相关概率信息的类
 * 
 * @author han
 */
public class CSource implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -4059456213223366719L;

	public int max_POS;
	public int max_Word;
	public int WORD_MAXLENGTH;

	private TObjectIntHashMap<String> POS2ID = new TObjectIntHashMap<String>();
	private TObjectIntHashMap<String> Word2ID = new TObjectIntHashMap<String>();

	private WordItem[] wordlist;
	private String[] POS;

	// POS probabilistics
	private double[][][] TrigramofPOS;
	private double[][] BigramofPOS;
	private double[] UnigramofPOS;

	private int total_POS;
	private int total_Word;

	// 中文字的数量
	private int cc_NUM;

	/**
	 * 一个以中文字索引的结构
	 */
	// c++版本里使用汉字编码来做index，这里使用hash表，需要一个转换
	private WordIndexTable[] m_IndexTable;
	public static TObjectIntHashMap<String> char2Index = null;

	/**
	 * NER 时候的CSource
	 * 
	 * @param file
	 */
	public CSource(File file) {
		try {
		    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "gbk"));
            String line;
			int state = 0;
			int mIn = 0;
			int lineindex = 0;
			while ((line = br.readLine()) != null) {
				lineindex++;
				if (line.startsWith("max_POS")) {
					this.max_POS = Integer.parseInt(line.substring(line
							.indexOf(":") + 1));
					// this.POS = new String[this.max_POS];
					// this.TrigramofPOS = new
					// double[this.max_POS][this.max_POS][this.max_POS];
					// this.BigramofPOS = new
					// double[this.max_POS][this.max_POS];
					// this.UnigramofPOS = new double[this.max_POS];
					continue;
				} else if (line.startsWith("cc_NUM")) {
					this.cc_NUM = Integer.parseInt(line.substring(
							line.indexOf(" ")).trim());
					this.m_IndexTable = new WordIndexTable[this.cc_NUM];
				} else if (line.startsWith("max_Word")) {
					this.max_Word = Integer.parseInt(line.substring(line
							.indexOf(":") + 1));
					this.wordlist = new WordItem[this.max_Word];
					this.Word2ID = new TObjectIntHashMap<String>(
							this.max_Word * 2);
					continue;
				} else if (line.startsWith("word_maxlength")) {
					this.WORD_MAXLENGTH = Integer.parseInt(line.substring(line
							.indexOf(":") + 1));
					continue;
				} else if (line.startsWith("wordItem list")) {
					state = 1;
					continue;
				} else if (line.startsWith("total_POS")) {
					this.total_POS = Integer.parseInt(line.substring(line
							.indexOf(":") + 1));
					continue;
				} else if (line.startsWith("total_word")) {
					this.total_Word = Integer.parseInt(line.substring(line
							.indexOf(":") + 1));
					continue;
				} else if (line.startsWith("Chinese character")) {
					state = 6;
					if (char2Index == null) {
						char2Index = new TObjectIntHashMap<String>();
						BufferedReader cbr = new BufferedReader(new InputStreamReader(new FileInputStream("data/chineseCharacter.txt"), "gbk"));
						String cbrline = null;
						int cindex = 0;
						while ((cbrline = cbr.readLine()) != null) {
							char2Index.put(cbrline, cindex);
							cindex++;
						}
					}
				} else if (line.startsWith("m_IndexTable")) {
					// this.m_IndexTable = new WordIndexTable[chIn];
					state = 7;
				}
				// wordItem list

				else if (state == 1) {
					WordItem wi = new WordItem();
					int index1 = line.indexOf('\t');
					int index2 = line.indexOf('\t', index1 + 1);
					wi.nID = Integer.parseInt(line.substring(0, index1));
					wi.sWord = line.substring(index1 + 1, index2);
					index1 = index2;
					index2 = line.indexOf('\t', index1 + 1);
					wi.nWordLen = Integer.parseInt(line.substring(index1 + 1,
							index2));
					index1 = index2;
					index2 = line.indexOf('\t', index1 + 1);
					wi.nFrequency = Integer.parseInt(line.substring(index1 + 1,
							index2));
					index1 = index2;
					index2 = line.indexOf('\t', index1 + 1);
					wi.nBiCount = Integer.parseInt(line.substring(index1 + 1,
							index2));
					index1 = index2;
					wi.nTriCount = Integer.parseInt(line.substring(index2 + 1)
							.trim());
					wi.bigram = new WordBigram[wi.nBiCount];
					wi.trigram = new WordTrigram[wi.nTriCount];
					line = br.readLine();
					int ind1 = -2;
					int ind = line.indexOf("||");
					int wbin = 0;
					while (ind != -1) {
						String s1 = line.substring(ind1 + 2, ind).trim();
						WordBigram wb = new WordBigram();
						int in1 = s1.indexOf("\t");
						wb.nBigram = Integer.parseInt(s1.substring(0, in1));
						wb.nWID1 = Integer.parseInt(s1.substring(in1 + 1)
								.trim());
						wi.bigram[wbin] = wb;
						ind1 = ind;
						ind = line.indexOf("||", ind + 2);
						wbin++;
					}
					line = br.readLine();
					ind1 = -2;
					ind = line.indexOf("||");
					wbin = 0;
					while (ind != -1) {
						String s1 = line.substring(ind1 + 2, ind).trim();
						WordTrigram wt = new WordTrigram();
						int in1 = s1.indexOf("\t");
						int in2 = s1.indexOf("\t", in1 + 1);
						wt.nTrigram = Integer.parseInt(s1.substring(0, in1));
						wt.nWID1 = Integer.parseInt(s1.substring(in1 + 1, in2));
						wt.nWID2 = Integer.parseInt(s1.substring(in2 + 1)
								.trim());
						wi.trigram[wbin] = wt;
						ind1 = ind;
						ind = line.indexOf("||", ind + 2);
						wbin++;
					}
					this.wordlist[wi.nID] = wi;
					this.Word2ID.put(wi.sWord, wi.nID);
				}

				else if (state == 6) // chinese character
				{
					continue;
					// System.out.println("lineNumber: " + lineindex);
					// this.char2Index.put(line.substring(0,1), chIn);
					// chIn ++;
				} else if (state == 7) {
					WordIndexTable wit = new WordIndexTable();
					wit.nCount = Integer.parseInt(line.substring(0, line
							.indexOf('\t')));
					wit.offset = Integer.parseInt(line.substring(line
							.indexOf('\t') + 1));
					this.m_IndexTable[mIn] = wit;
					mIn++;
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param file
	 * @param filetype
	 *            1 为分词用的CSource
	 */
	public CSource(File file, int filetype) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "gbk"));
			String line;
			int state = 0;
			int posInde = 0;
			int tIn = 0;
			int bIn = 0;
			int uIn = 0;
			int mIn = 0;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("max_POS")) {
					this.max_POS = Integer.parseInt(line.substring(line
							.indexOf(":") + 1));
					this.POS = new String[this.max_POS];
					this.TrigramofPOS = new double[this.max_POS][this.max_POS][this.max_POS];
					this.BigramofPOS = new double[this.max_POS][this.max_POS];
					this.UnigramofPOS = new double[this.max_POS];
					continue;
				} else if (line.startsWith("cc_NUM")) {
					this.cc_NUM = Integer.parseInt(line.substring(
							line.indexOf(" ")).trim());
					this.m_IndexTable = new WordIndexTable[this.cc_NUM];
				} else if (line.startsWith("max_Word")) {
					this.max_Word = Integer.parseInt(line.substring(line
							.indexOf(":") + 1));
					this.wordlist = new WordItem[this.max_Word];
					this.Word2ID = new TObjectIntHashMap<String>(
							this.max_Word * 2);
					continue;
				} else if (line.startsWith("word_maxlength")) {
					this.WORD_MAXLENGTH = Integer.parseInt(line.substring(line
							.indexOf(":") + 1));
					continue;
				} else if (line.startsWith("wordItem list")) {
					state = 1;
					continue;
				} else if (line.startsWith("POS string list")) {
					state = 2;
					continue;
				} else if (line.startsWith("TrigramofPOS")) {
					state = 3;
					continue;
				} else if (line.startsWith("BigramofPOS")) {
					state = 4;
					continue;
				} else if (line.startsWith("UnigramofPOS")) {
					state = 5;
					continue;
				} else if (line.startsWith("total_POS")) {
					this.total_POS = Integer.parseInt(line.substring(line
							.indexOf(":") + 1));
					continue;
				} else if (line.startsWith("total_Word")) {
					this.total_Word = Integer.parseInt(line.substring(line
							.indexOf(":") + 1));
					continue;
				} else if (line.startsWith("Chinese character")) {
					state = 6;
					if (char2Index == null) {
						char2Index = new TObjectIntHashMap<String>();
						BufferedReader cbr = new BufferedReader(new InputStreamReader(new FileInputStream("data/chineseCharacter.txt"), "gbk"));
                        
						String cbrline = null;
						int cindex = 0;
						while ((cbrline = cbr.readLine()) != null) {
							char2Index.put(cbrline, cindex);
							cindex++;
						}
					}
				} else if (line.startsWith("m_IndexTable")) {
					// this.m_IndexTable = new WordIndexTable[chIn];
					state = 7;
				}
				// wordItem list

				else if (state == 1) {
					WordItem wi = new WordItem();
					int index1 = line.indexOf('\t');
					int index2 = line.indexOf('\t', index1 + 1);
					wi.nID = Integer.parseInt(line.substring(0, index1));
					wi.sWord = line.substring(index1 + 1, index2);
					index1 = index2;
					index2 = line.indexOf('\t', index1 + 1);
					wi.nWordLen = Integer.parseInt(line.substring(index1 + 1,
							index2));
					wi.nFrequency = Integer.parseInt(line.substring(index2 + 1)
							.trim());
					wi.pPOS = new Vector<POSITEM>(1);
					line = br.readLine();
					int ind1 = -2;
					int ind = line.indexOf("||");
					while (ind != -1) {
						String s1 = line.substring(ind1 + 2, ind).trim();
						POSITEM pi = new POSITEM();
						int in1 = s1.indexOf("\t");
						pi.nID = Integer.parseInt(s1.substring(0, in1));
						int in2 = s1.indexOf("\t", in1 + 1);
						pi.POS = s1.substring(in1 + 1, in2);
						in1 = in2;
						in2 = s1.indexOf("\t", in1 + 1);
						pi.nLength = Integer.parseInt(s1
								.substring(in1 + 1, in2));
						pi.frequency = Integer.parseInt(s1.substring(in2 + 1)
								.trim());
						wi.pPOS.add(pi);
						ind1 = ind;
						ind = line.indexOf("||", ind1 + 2);
					}
					this.wordlist[wi.nID] = wi;
					Word2ID.put(wi.sWord, wi.nID);
				}

				else if (state == 2) // POS string list
				{
					this.POS[posInde] = line;
					this.POS2ID.put(line, posInde);
					posInde++;
				} else if (state == 3) // trigram of POS;
				{
					int k = tIn % this.max_POS;
					int i = tIn / (this.max_POS * this.max_POS );
					int j = (tIn - i * this.max_POS * this.max_POS)
							/ this.max_POS ;
					this.TrigramofPOS[i][j][k] = Double.parseDouble(line);
					tIn++;
				} else if (state == 4) // bigram of POS;
				{
					int i = bIn / this.max_POS ;
					int j = bIn % this.max_POS;
					this.BigramofPOS[i][j] = Double.parseDouble(line);
					bIn++;
				} else if (state == 5) // unigram of POS;
				{
					this.UnigramofPOS[uIn] = Double.parseDouble(line);
					uIn++;
				} else if (state == 6) // chinese character
				{
					// this.char2Index.put(line.substring(0,1), chIn);
					// chIn ++;
				} else if (state == 7) {
					WordIndexTable wit = new WordIndexTable();
					wit.nCount = Integer.parseInt(line.substring(0, line
							.indexOf('\t')));
					wit.offset = Integer.parseInt(line.substring(line
							.indexOf('\t') + 1));
					this.m_IndexTable[mIn] = wit;
					mIn++;
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public double getBigramofWord(String sWord_first, String sWord_second) {
		int first, second;
		first = getWordID(sWord_first);
		second = getWordID(sWord_second);
		int cur;
		if (first >= 0 && first < max_Word && second >= 0 && second < max_Word) {
			if ((cur = getBigramID(first, second)) >= 0) {
				return (
						(((double) this.wordlist[first].bigram[cur].nBigram)
						/ ((double) (this.wordlist[second].nFrequency + 1)))
						+ ((double) this.wordlist[first].nFrequency)
						/ ((double)(this.wordlist[second].nFrequency + 1) * (double) (this.total_Word + 1)) 
						+ 1.0 / ((double) (this.wordlist[second].nFrequency + 1)
						* (double) (this.total_Word + 1) * (double) (this.max_Word + 1)));
			} else {
				return (0.0
						+ (double) (this.wordlist[first].nFrequency)
						/ (double) ((this.wordlist[second].nFrequency + 1) * (double) (this.total_Word + 1)) + 1.0 / ((double) (this.wordlist[second].nFrequency + 1)
						* (double) (this.total_Word + 1) * (double) (this.max_Word + 1)));
			}
		} else
			return -1.0;
	}

	public double getTrigramofWord(String sWord_first, String sWord_second,
			String sWord_third) {
		int first, second, third;
		first = this.getWordID(sWord_first);
		second = this.getWordID(sWord_second);
		third = this.getWordID(sWord_third);
		int cur;
		double trigram;
		double bigram1;
		double bigram2;
		if (first >= 0 && first < max_Word && second >= 0 && second < max_Word
				&& third >= 0 && third < max_Word) {
			if ((cur = this.getTrigramID(first, second, third)) >= 0) {
				trigram = this.wordlist[first].trigram[cur].nTrigram;
			} else
				trigram = 0;
			if ((cur = this.getBigramID(first, second)) >= 0) {
				bigram1 = this.wordlist[first].bigram[cur].nBigram;
			} else
				bigram1 = 0;
			if ((cur = this.getBigramID(second, third)) >= 0) {
				bigram2 = this.wordlist[second].bigram[cur].nBigram;
			} else
				bigram2 = 0;

			return (trigram
					/ (bigram2 + 2)
					+ bigram1
					/ ((bigram2 + 2) * (this.wordlist[second].nFrequency + 2))
					+ this.wordlist[first].nFrequency
					/ ((bigram2 + 2) * (this.wordlist[second].nFrequency + 2) * (double) (this.total_Word + 2)) + 1.0 / ((bigram2 + 2)
					* (this.wordlist[second].nFrequency + 2)
					* (double) (this.total_Word + 2) * (double) (this.max_Word + 2)));
		} else
			return -1.0;
	}

	/**
	 * the smoothing minimum data for unoccuring word or POS
	 * 
	 * @param nFlagofMethod
	 *            is flag of variable smoothing method nFlagofMethod =0 for
	 *            unigram of word nFlagofMethod =1 for unigram of POS
	 *            nFlagofMethod =2 for bigram of POS nFlagofMethod =3 for
	 *            Trigram of POS nFlagofMehtod =4 for probability of word and
	 *            Part of Speech cooccurence p(w|t) nFlagofMehtod =5 for
	 *            probability of word and Part of Speech cooccurence p(t|w)
	 */
	public double smoothData(int nFlagofMethod) {
		if (nFlagofMethod == 0) {
			return 1.0 / ((double) (max_Word + 1) * max_Word * (this.total_Word + 1.0));
		} else if (nFlagofMethod == 1)
			return 1.0 / ((double) (this.total_POS + 1.0)
					* (double) (this.max_POS + 1.0) * (double) (this.max_POS + 1.0));
		else if (nFlagofMethod == 2)
			return 1.0 / ((double) (this.total_POS / this.max_POS + 1.0)
					* (double) (this.total_POS + 1)
					* (double) (this.max_POS + 1) * (double) (this.max_POS + 1));
		else if (nFlagofMethod == 3)
			return 1.0 / ((double) (this.total_POS / this.max_POS + 1.0)
					* (double) (this.total_POS / (this.max_POS * this.max_POS) + 1.0)
					* (double) (this.total_POS + 1)
					* (double) (this.max_POS + 1) * (double) (this.max_POS + 1));
		else if (nFlagofMethod == 4)
			return 1.0 / ((double) (this.total_POS + 1.0)
					* (double) (this.max_Word + 1)
					* (double) (this.total_Word + 1) * (double) (this.max_Word + 1));
		else if (nFlagofMethod == 5)
			return 1.0 / ((double) (this.total_Word + 1.0)
					* (double) (this.max_POS + 1)
					* (double) (this.total_POS + 1) * (double) (this.max_POS + 1));
		else if (nFlagofMethod == 6)
			return 1.0 / (double) (this.total_Word + 1.0);
		else if (nFlagofMethod == 7)
			return 1.0 / ((double) (max_Word + 1) * (double) (max_Word) * (double) (this.total_Word + 1.0));
		else
			return -1.0;
	}

	// flag of which gram data is validated
	public int m_nFlagofDataValidated;

	/**
	 * get ID of the word
	 * 
	 * @param word
	 * @return
	 */
	public int getWordID(String word) {
		if (Word2ID.containsKey(word))
			return this.Word2ID.get(word);
		else
			return -1;
	}

	/**
	 * return: 1, 是纯前缀 2， 是纯词 3， 既是词也是前缀 -1，以上皆非
	 */
	public int isPrex(String word) {
		int offset = char2Index.get(word.substring(0, 1));
		if (offset < 0 || offset >= this.cc_NUM) {
			return -1;
		}
		int flag = 0;
		int low = 0;
		int high = this.m_IndexTable[offset].nCount - 1;
		int mid;
		
		while (low <= high) {
			mid = (low + high) / 2;
			String sWord = this.wordlist[m_IndexTable[offset].offset + mid].sWord;
			int tmp = this.strncmp(word, sWord, word.getBytes(Charset.forName("gbk")).length);
			if (sWord.startsWith(word)) {
				if (word.length() < sWord.length())
					return 1;
				else if (sWord.equals(word)) {
					if (mid >= this.m_IndexTable[offset].nCount - 1)
						return 2;
					else {
						if (this.wordlist[(m_IndexTable[offset].offset) + mid
								+ 1].sWord.startsWith(word))
							return 3;
						else
							return 2;
					}
				} else
					return -1;
			} else if (tmp < 0)
				high = mid - 1;
			else
				low = mid + 1;
		} 
		if (flag == 0)
			return -1;
		else
			return 1;
	}

	public int strncmp(String word1, String word2) {
		byte[] b1 = word1.getBytes(Charset.forName("gbk"));
		byte[] b2 = word2.getBytes(Charset.forName("gbk"));
		int len = b1.length > b2.length ? b2.length : b1.length;
		for (int i = 0; i < len; i++) {
			if (b1[i] == b2[i])
				continue;
			else if (b1[i] > b2[i])
				return 1;
			else
				return -1;
		}
		return b1.length > b2.length ? -1 : 1;
	}
	
	private int strncmp(String word1, String word2, int comparelen) {
		byte[] b1 = word1.getBytes(Charset.forName("gbk"));
		byte[] b2 = word2.getBytes(Charset.forName("gbk"));
		int len = b1.length > b2.length ? b2.length : b1.length;
		if(comparelen <= len)
		{
			for (int i = 0; i < comparelen; i++) {
				if (b1[i] == b2[i])
					continue;
				else if (b1[i] > b2[i])
					return 1;
				else
					return -1;
			}
			return 0;
		}
		else {
		
		for (int i = 0; i < len; i++) {
			if (b1[i] == b2[i])
				continue;
			else if (b1[i] > b2[i])
				return 1;
			else
				return -1;
		}
			if(b1.length == b2.length)
				return 0;
			else
				return b1.length > b2.length ? 1 : -1;
		}
		
	}
	
	/**
	 * get word of the word ID
	 */
	public String getWord(int wordID) {
		return this.wordlist[wordID].sWord;
	}

	public int getPOSID(String POS) {
		return this.POS2ID.get(POS);
	}

	/**
	 * get the nCur'th ID of the word wordID
	 * 
	 * @param wordID
	 * @param nCur
	 * @return
	 */
	public int getIDofWordPOS(int wordID, int nCur) {
		if (wordID >= 0 && wordID < this.wordlist.length) {
			if (nCur >= 0 && nCur < this.wordlist[wordID].pPOS.size())
				return this.wordlist[wordID].pPOS.get(nCur).nID;
			else
				return -1;
		} else
			return -1;
	}

	/**
	 * get Number of POS of the word nID
	 * 
	 * @param nID
	 * @return
	 */
	public int getNumofWordPOS(int nID) {
		if (nID > 0 && nID < this.wordlist.length)
			return this.wordlist[nID].pPOS.size();
		else
			return -1;
	}

	public String getPOSString(int posID) {
		if (posID < this.POS.length)
			return this.POS[posID];
		else
			return null;
	}

	/**
	 * get trigram probability of Part of Speech
	 * 
	 * @param firstID
	 * @param secondID
	 * @param thirdID
	 * @return -1.0 如果没有找到
	 */
	public double getTrigramofPOS(int firstPOSID, int secondPOSID,
			int thirdPOSID) {
		if (firstPOSID >= 0 && firstPOSID <= this.max_POS && secondPOSID >= 0
				&& secondPOSID <= this.max_POS && thirdPOSID >= 0
				&& thirdPOSID <= this.max_POS) {
			return this.TrigramofPOS[firstPOSID][secondPOSID][thirdPOSID]
					/ (this.BigramofPOS[secondPOSID][thirdPOSID] + 1)
					+ this.BigramofPOS[firstPOSID][secondPOSID]
					/ ((this.BigramofPOS[secondPOSID][thirdPOSID] + 1)
							* (UnigramofPOS[secondPOSID] + 1) * (this.total_POS + 1))
					+ 1.0
					/ ((BigramofPOS[secondPOSID][thirdPOSID] + 1)
							* (UnigramofPOS[secondPOSID] + 1)
							* (this.total_POS + 1) * (this.max_POS + 1));
		} else
			return -1.0;
	}

	/**
	 * get Bigram probability of Part of Speech
	 * 
	 * @param firstPOSID
	 * @param secondPOSID
	 * @return
	 */
	public double getBigramofPOS(int firstPOSID, int secondPOSID) {
		if (firstPOSID >= 0 && firstPOSID < this.max_POS && secondPOSID >= 0
				&& secondPOSID < this.max_Word) {
			return (this.BigramofPOS[firstPOSID][secondPOSID]
					/ (this.UnigramofPOS[secondPOSID] + 1)
					+ this.UnigramofPOS[firstPOSID]
					/ ((this.UnigramofPOS[secondPOSID] + 1) * (this.total_POS + 1)) + 1.0 / ((this.UnigramofPOS[secondPOSID] + 1)
					* (this.total_POS + 1) * (this.max_POS + 1)));
		} else
			return -1.0;
	}

	/**
	 * get unigram probability of Part of Speech
	 * 
	 * @param firstPOSID
	 * @return
	 */
	public double getUnigramofPOS(int firstPOSID) {
		if (firstPOSID >= 0 && firstPOSID < max_POS) {
			return (this.UnigramofPOS[firstPOSID] / (this.total_POS + 1.0) + 1.0 / ((this.total_POS + 1.0) * (this.max_POS + 1.0)));
		} else
			return -1.0;
	}

	public double getUnigramofWord(String word) {
		int firstWordID = this.getWordID(word);
		if (firstWordID >= 0 && firstWordID < max_Word)
			return ( ((double)this.wordlist[firstWordID].nFrequency)
					/ ((double)(this.total_Word + 1)) + 1.0 / ((double)(this.max_Word + 1) * (double)(this.total_Word + 1.0)));
		else
			return -1.0;
	}

	public double getUnigramofWord(int firstWordID) {
		if (firstWordID >= 0 && firstWordID < max_Word)
			return (((double) this.wordlist[firstWordID].nFrequency)
					/ (double) (this.total_Word + 1) + 1.0 / ((double) (this.max_Word + 1) * (this.total_Word + 1.0)));
		else
			return -1.0;
	}

	/**
	 * get probability of word and Part of Speech cooccurence p(w|t)
	 * 
	 * @param wordID
	 * @param POSID
	 * @return
	 */
	public double getPOSWordProb(int wordID, int POSID) {
		if (wordID >= 0 && wordID < this.max_Word && POSID >= 0
				&& POSID < this.max_POS) {
			for (int i = 0; i < this.wordlist[wordID].pPOS.size(); i++) {
				if (this.wordlist[wordID].pPOS.get(i).nID == POSID) {
					return ((((double) (this.wordlist[wordID].pPOS.get(i).frequency)) / ((double) (this.UnigramofPOS[POSID]) + 1)) 
							+ 1.0 / ((double)(this.UnigramofPOS[POSID] + 1) * 
							(double)(this.max_Word/ this.max_POS + 1)));
				}
			}
			return -1.0;
		} else
			return -1.0;
	}

	/**
	 * get probability of word and Part of Speech cooccurence p(t|w)
	 * 
	 * @param wordID
	 * @param POSID
	 * @return
	 */
	public double getWordPOSProb(int wordID, int POSID) {
		if (wordID >= 0 && wordID < this.max_Word && POSID >= 0
				&& POSID < this.max_POS) {
			WordItem wi = this.wordlist[wordID];
			for (int i = 0; i < wi.pPOS.size(); i++) {
				if (wi.pPOS.get(i).nID == POSID)
					return (((double) (wi.pPOS.get(i).frequency) / (wi.nFrequency + 1)) + 1.0 / ((wi.nFrequency + 1) * (wi.pPOS
							.size())));
			}
			return -1.0;
		} else
			return -1.0;
	}

	public int getBigramID(int ID_Word, int secID_Word) {
		int low, mid, high;
		// long i;
		double tmp;
		low = 0;
		high = this.wordlist[ID_Word].nBiCount - 1;
		while (low <= high) {
			mid = (low + high) / 2;
			if ((tmp = (this.wordlist[ID_Word].bigram[mid].nWID1 - secID_Word)) == 0)
				return mid;
			else if (tmp > 0)
				high = mid - 1;
			else
				low = mid + 1;
		}
		return (-1) * (low + 1);
	}

	public int getTrigramID(int ID_Word, int secID_Word, int thID_Word) {
		int low, mid, high;
		// long i;
		double tmp;
		low = 0;
		high = this.wordlist[ID_Word].nTriCount - 1;
		while (low <= high) {
			mid = (low + high) / 2;
			if ((tmp = (this.wordlist[ID_Word].trigram[mid].nWID1
					* (double) (max_Word)
					+ this.wordlist[ID_Word].trigram[mid].nWID2
					- (double) (secID_Word) * (double) (max_Word) - (double) (thID_Word))) == 0)
				return mid;
			else if (tmp > 0)
				high = mid - 1;
			else
				low = mid + 1;
		}
		return (-1) * (low + 1);
	}

	public static void main(String[] args) {
		CSource cs = new CSource(new File("D:/test.txt"), 1);
		cs.getPOSID("w");
	}
}

class POSITEM {
	public int nLength; // the length of POS char
	public int nID; // the ID of POS
	public int frequency; // the count of POS appearing in this word
	public String POS; // the POS;
}

/* data structure of word item */
class WordItem {
	public int nWordLen;
	// the length of the word char
	public int nID;
	// the ID of the word
	public int nFrequency;
	// The count of appearing
	public String sWord;
	// The word

	// The number of POS cooccuring with the word
	Vector<POSITEM> pPOS;
	// point to these POS

	// new add
	int nBiCount;
	WordBigram[] bigram;
	int nTriCount;
	WordTrigram[] trigram;
};

class WordBigram {
	public int nBigram;
	public int nWID1;
};

class WordTrigram {
	public int nTrigram;
	public int nWID1;
	public int nWID2;
};

/* data structure of dictionary */
class WordIndexTable {
	public int nCount;
	// The number of word beginning with the Chinese Char
	public int offset;
	// point to these word
};
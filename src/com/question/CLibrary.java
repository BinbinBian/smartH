package com.question;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * Created by shawn on 15-12-24.
 */
public interface CLibrary extends Library{

    public int NLPIR_Init(String sDataPath, int encoding, String sLicenceCode);

    public String NLPIR_ParagraphProcess(String sSrc, int bPOSTagged);

    public String NLPIR_GetKeyWords(String sLine, int nMaxKeyLimit, boolean bWeightOut);

    public String NLPIR_GetFileKeyWords(String sLine, int nMaxKeyLimit, boolean bWeightOut);

    public int NLPIR_AddUserWord(String sWord);

    public int NLPIR_DelUsrWord(String sWord);

    public int NLPIR_ImportUserDict(String sFilename);

    public String NLPIR_GetLastErrorMsg();

    public void NLPIR_Exit();
}

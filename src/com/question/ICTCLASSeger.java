package com.question;

import com.sun.jna.Library;
import com.sun.jna.Native;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by Shawn Guo on 2015/12/15.
 */
public class ICTCLASSeger {
    // 定义接口CLibrary，继承自com.sun.jna.Library

    public ICTCLASSeger(String fileName) {
        long startTime = System.currentTimeMillis();
        System.out.println("Initializing ICTCLASSeger");
        initLib();
        try {
            importUserDict(System.getProperty("user.dir") + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Successfully initialized ICTCLASSeger! " + (endTime - startTime));
    }

    public ICTCLASSeger() {
        System.out.println("Initializing ICTCLASSeger");
        initLib();
        System.out.println("Successfully initialized ICTCLASSeger!");
    }

    public interface CLibrary extends Library {
        // 定义并初始化接口的静态变量
        CLibrary Instance = (CLibrary) Native.loadLibrary(
                System.getProperty("user.dir") + "\\NLPIR", CLibrary.class);

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

    public  void initLib() {
        int charset_type = 1;
        String argu = System.getProperty("user.dir");
        int init_flag = CLibrary.Instance.NLPIR_Init(argu, charset_type, "0");

        if (init_flag == 0) {
            System.err.println(argu);
            System.err.println("ICTCLAS initLib: 初始化失败！");
            System.exit(1);
        }
    }

    public  void importUserDict(String dictName) throws IOException, ClassNotFoundException {
        File inputFile = new File(dictName);
        FileInputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;

        String lineString = null;

        try {
            inputStream = new FileInputStream(inputFile);
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            while ( (lineString = bufferedReader.readLine()) != null) {
                CLibrary.Instance.NLPIR_AddUserWord(lineString);
            }
        }  catch (IOException e) {
            System.out.println("ICTCLAS importUserDict: 读取文件失败！");
            System.exit(1);
        } finally {
            try {
                bufferedReader.close();
                inputStreamReader.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String tokenizeAndTag(String inputStr) throws Exception {
        String outputString = CLibrary.Instance.NLPIR_ParagraphProcess(inputStr, 3);
        return outputString;
    }

}

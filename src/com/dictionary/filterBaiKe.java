package com.dictionary;

import com.question.ICTCLASSeger;

import java.io.*;

/**
 * Created by Shawn Guo on 2015/12/15.
 */
public class filterBaiKe {
    public static void main(String[] args) throws Exception {
        ICTCLASSeger seger = new ICTCLASSeger();
        seger.initLib();

        File inputFile = new File(System.getProperty("user.dir") + "\\data\\dicts\\databasic\\words-my.dic");
        FileInputStream inputStream = new FileInputStream(inputFile);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        File outputFile = new File(System.getProperty("user.dir") + "\\data\\dicts\\words-filter.dic");
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

        String lineString;
        while ((lineString = bufferedReader.readLine()) != null) {
            if (seger.tokenizeAndTag(lineString).split(" ").length > 1) {
                bufferedWriter.write(lineString);
                bufferedWriter.newLine();
            }
        }
        bufferedReader.close();
        inputStreamReader.close();
        inputStream.close();
    }
}

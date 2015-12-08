package com.question;

import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.chenlb.mmseg4j.example.Complex;

/**
 * Author:             Shawn Guo
 * E-mail:             air_fighter@163.com
 *
 * Create Time:        2015/11/26 09:05
 * Last Modified Time: 2015/12/07 17:13
 *
 * Class Name:         Question
 * Class Function:
 *                     该类的主要功能存储一个问题的题干和对应的四个选项，并且可以存储正确选项。
 *                     答案的初始化字符为“ ”。
 */
public class Question {
    protected String dictionaryPath = "data\\dicts\\databasic";

    private String question = null;                                 //题目原文
    private HashSet<String> wordSet = new HashSet<>();              //题目分词结果
    private int[] wordVector = null;                                //题目词向量

    private String[] candidates = {"", "", "", ""};                 //四个候选答案
    private char answer = ' ';                                      //正确答案标号
    private int type = 0;                                           //问题类型

    private String material = new String();                         //材料文本
    private HashSet<String> materialWordSet = new HashSet<>();     //材料分词结果
    private int[] materialWordVec = null;                           //材料词向量

    private String questionStem = new String();                     //题干
    private HashSet<String> stemWordSet = new HashSet<>(); //题干分词结果
    private int[] stemWordVec = null;                               //题干词向量

    private String[] originalMaterialRegex = {                      //原始材料正则表达
            "“.+?”"
    };
    private HashSet<String> orignialMaterials = new HashSet<>();    //提取出来的原始材料

    private HashSet<String> punctuationSet = new HashSet<String>() {{         //需要排除的标点符号
        add("；");
        add("。");
        add("、");
        add("，");
        add("？");
        add("：");
        add("…");
        add("(");
        add(")");
        add("（");
        add("）");
        add("《");
        add("》");
        add("”");
        add("“");
        add("：");
    }}; //抛弃不要的标点符号



    private Complex seger = new Complex(dictionaryPath);

    public boolean setQuestion(String input) {
        question = input;
        if (question == null)
            return false;
        return true;
    }
    public String getQuestion() {
        return question;
    }

    public void buildWordSet() throws IOException {
        String[] wordsSet = seger.segWords(question, "|").split("\\|");
        for (String word : wordsSet) {
            if (!punctuationSet.contains(word)){
                wordSet.add(word);
            }
        }
    }
    public HashSet<String> getWordSet() {
        return wordSet;
    }

    public boolean setWordVec(int index, int value) {
        wordVector[index] = value;
        return true;
    }
    public int[] getWordVec() {
        return wordVector;
    }
    public void initWordVec(int dimension) {
        wordVector = new int[dimension];
    }

    public boolean setCandidate(int index, String input) {
        candidates[index] = input.trim();
        if (candidates[index] == "")
            return false;
        return true;
    }
    public String getCandidates(int index) {
        return candidates[index];
    }

    public boolean setAnswer(char input) {
        answer = input;
        if (answer == ' ')
            return false;
        return true;
    }
    public char getAnswer() {
        return answer;
    }

    public boolean setType(int typeInt) {
        type = typeInt;
        if (type == 0)
            return false;
        return true;
    }
    public int getType() {
        return type;
    }

    public boolean setMaterial(String input) {
        material = input;
        if (material == null)
            return false;
        return true;
    }
    public String getMaterial() {
        return material;
    }

    public void buildMaterialWordSet() throws IOException {
        String[] wordsSet = seger.segWords(material, "|").split("\\|");
        for (String word : wordsSet) {
            if (!punctuationSet.contains(word)){
                materialWordSet.add(word);
            }
        }
    }
    public HashSet<String> getMaterialWordSet() {
        return materialWordSet;
    }

    public boolean setMaterialWordVec(int index, int value) {
        materialWordVec[index] = value;
        return true;
    }
    public int[] getMaterialWordVec() {
        return materialWordVec;
    }
    public void initMaterialWordVec(int dimension) {
        materialWordVec = new int[dimension];
    }

    private void buildOriginalMaterial() {
        for (String regex : originalMaterialRegex) {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(question);
            while(m.find()) {
                orignialMaterials.add(m.group());
            }
        }
    }
    public HashSet<String> getOrignialMaterials() {
        buildOriginalMaterial();
        return orignialMaterials;
    }


    public boolean setQuestionStem() {
        String questionString = question;
        if (material != null) {
            questionStem = questionString.replace(material, "");
        }
        else {
            questionStem = question;
        }
        return true;
    }
    public String getQuestionStem() {
        return questionStem;
    }
    public void buildStemWordSet() throws IOException {
        String[] wordsSet = seger.segWords(questionStem, "|").split("\\|");
        for (String word : wordsSet) {
            if (!punctuationSet.contains(word)){
                stemWordSet.add(word);
            }
        }
    }
    public HashSet<String> getStemWordSet() {
        return stemWordSet;
    }

    public boolean setStemWordVec(int index, int value) {
        stemWordVec[index] = value;
        return true;
    }
    public int[] getStemWordVec() {
        return stemWordVec;
    }
    public void initStemWordVec(int dimension) {
        stemWordVec = new int[dimension];
    }

}

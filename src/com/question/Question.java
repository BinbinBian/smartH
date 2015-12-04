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
 * Last Modified Time: 2015/12/04 17:13
 *
 * Class Name:         Question
 * Class Function:
 *                     该类的主要功能存储一个问题的题干和对应的四个选项，并且可以存储正确选项。
 *                     答案的初始化字符为“ ”。
 */
public class Question {
    protected String dictionaryPath = "data\\fenciDict\\databasic";

    private String question = null;                                 //题目原文
    private String[] candidates = {"", "", "", ""};                 //四个候选答案
    private char answer = ' ';                                      //正确答案标号
    private String material = new String();                         //材料文本
    private HashSet<String> materialWordsSet = new HashSet<>();     //材料分词结果
    private String quesitonStem = new String();                     //题干
    private HashSet<String> questionStemWordsSet = new HashSet<>(); //题干分词结果
    private String[] originalMaterialRegex = {                      //原始材料正则表达
            "“.+?”"
    };
    private HashSet<String> orignialMaterials = new HashSet<>();    //提取出来的原始材料

    private Complex seger = new Complex();

    public boolean setQuestion(String input) {
        question = input;
        if (question == null)
            return false;
        return true;
    }

    public boolean setCandidate(int index, String input) {
        candidates[index] = input.trim();
        if (candidates[index] == "")
            return false;
        return true;
    }

    public boolean setAnswer(char input) {
        answer = input;
        if (answer == ' ')
            return false;
        return true;
    }

    public boolean setMaterial(String input) {
        material = input;
        if (material == null)
            return false;
        return true;
    }

    public boolean setQuestionStem() {
        String questionString = question;
        if (material != null) {
            quesitonStem = questionString.replace(material, "");
        }
        else {
            quesitonStem = question;
        }
        return true;
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

    public String getQuestion() {
        return question;
    }

    public String getCandidates(int index) {
        return candidates[index];
    }

    public char getAnswer() {
        return answer;
    }

    public String getMaterial() {
        return material;
    }

    public String getQuesitonStem() {
        return quesitonStem;
    }

    public HashSet<String> getOrignialMaterials() {
        buildOriginalMaterial();
        return orignialMaterials;
    }

    public HashSet<String> getMaterialWordsSet() throws IOException {

        String[] wordsSet = seger.segWords(material, "|").split("\\|");
        HashSet<String> retSet = new HashSet<>();
        for (String word : wordsSet) {
            retSet.add(word);
        }
        return retSet;
    }
}

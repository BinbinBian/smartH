package com.question;

import com.shawn.BasicIO;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author:             Shawn Guo
 * E-mail:             air_fighter@163.com
 *
 * Create Time:        2015/11/26 10:37
 * Last Modified Time: 2015/12/07 17:03
 *
 * Class Name:         QuestionAnalyzer
 * Class Function:
 *                     该类在使用Question类的基础上，对题干进行分析，确定题目类型。
 */

public class QuestionAnalyzer {
    private String[] regexes = {                                        //切分标识符
            ".+。”",
            ".+。",
            ".+！”",
            ".+？”"
            };

    private ArrayList<String> wordSet = new ArrayList<>();              //用队列模拟集合，方便后面生成向量。

    public ArrayList<String> getWordSet() {
        return wordSet;
    }

    public void buildQuestionMaterial(ArrayList<Question> questionList) {
        //int questionNum = 0;  //for debug
        for (Question question : questionList) {
            //questionNum++;
            String questionString = question.getQuestion();
            boolean findInRegex = false;
            for (String regex : regexes) {
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(questionString);
                if (m.find()) {
                    question.setMaterial(m.group());
                    question.setQuestionStem();
                    findInRegex = true;
                    break;
                }
            }

            if (findInRegex && question.getMaterial().length() < (question.getQuestionStem().length() - 7)) {
                Pattern p = Pattern.compile(".+，");
                String questionStemString = question.getQuestionStem();
                Matcher m = p.matcher(questionStemString);
                if (m.find()) {
                    question.setMaterial(m.group());
                    question.setQuestionStem();
                }
            }

            if (!findInRegex) {
                Pattern p = Pattern.compile(".+，");
                Matcher m = p.matcher(questionString);
                if (m.find() && m.group().length() >= 7) {
                    question.setMaterial(m.group());
                }
               question.setQuestionStem();
            }
        }
    }

    public void buildWordSet(ArrayList<Question> questionList) {
        for (Question question : questionList)
            try {
                /**
                question.buildMaterialWordSet();
                question.getMaterialWordSet().stream().filter(pos -> !wordSet.contains(pos)).forEach(wordSet::add);
                question.buildStemWordSet();
                question.getStemWordSet().stream().filter(pos -> !wordSet.contains(pos)).forEach(wordSet::add);
                 */
                question.buildWordSet();
                question.getWordSet().stream().filter(pos -> !wordSet.contains(pos)).forEach(wordSet::add);
            } catch (IOException e) {
                e.printStackTrace();
            }
        if (wordSet.isEmpty()) {
            System.err.println("生成的分词结果集合为空！请检查分词结果。");
        }
    }

    public void buildWordVector(ArrayList<Question> questionList) {
        for (Question question : questionList) {
            //question.initMaterialWordVec(wordSet.size());
            //question.initStemWordVec(wordSet.size());\
            question.initWordVec(wordSet.size());
            for (String word : wordSet) {
                /**if (question.getMaterialWordSet().contains(word)) {
                    question.setMaterialWordVec(wordSet.indexOf(word), 1);
                }
                else {
                    question.setMaterialWordVec(wordSet.indexOf(word), 0);
                }
                if (question.getStemWordSet().contains(word)) {
                    question.setStemWordVec(wordSet.indexOf(word), 1);
                }
                else {
                    question.setStemWordVec(wordSet.indexOf(word), 0);
                }*/
                if (question.getWordSet().contains(word)) {
                    question.setWordVec(wordSet.indexOf(word), 1);
                }
                else {
                    question.setWordVec(wordSet.indexOf(word), 0);
                }
            }
        }
    }

    public void buildQuestionType(ArrayList<Question> questionList) {
        int[] readType = new int[questionList.size()];
        try {
            readType = BasicIO.readFile2IntArray(questionList.size(), "data\\questions\\classification87.txt");
            for (int index = 0; index < questionList.size(); index++) {
                questionList.get(index).setType(readType[index]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception{
        QuestionAnalyzer self = new QuestionAnalyzer();
        QuestionAcquisition acquisition = new QuestionAcquisition();

        try {
            acquisition.init("data\\questions\\questions87.txt");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        self.buildQuestionMaterial(acquisition.questionList);
        self.buildWordSet(acquisition.questionList);
        self.buildWordVector(acquisition.questionList);
        self.buildQuestionType(acquisition.questionList);

        System.out.println("Now, it's outputting");

        File outputFile = new File("out\\output.txt");
        File svmTrainFile = new File("data\\svm\\train.txt");
        try {
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            PrintStream printStream = new PrintStream(outputStream);

            FileOutputStream svmTrainOutputFile = new FileOutputStream(svmTrainFile);
            PrintStream svmTrainPrint = new PrintStream(svmTrainOutputFile);

            int i = 0;
            for (Question question : acquisition.questionList) {
                i += 1;
                printStream.println("Question #" + i);
                printStream.println("Question:\t" + question.getQuestion());
                printStream.println("Candidates:\tA." + question.getCandidates(0)
                        + " B." + question.getCandidates(1)
                        + " C." + question.getCandidates(2)
                        + " D." + question.getCandidates(3));
                printStream.println("Type:\t" + question.getType());
                svmTrainPrint.print(question.getType() + " ");

                printStream.println("Materials:\t" + question.getMaterial());
                /**printStream.print("POS result:\t");
                for (String word : question.getMaterialWordSet()) {
                    printStream.print(word + " ");
                }
                printStream.println();
                printStream.print("MaterialWordVec:\t");
                for (int index = 0; index < self.wordSet.size(); index++) {
                    printStream.print(question.getMaterialWordVec()[index] + " ");
                    if (question.getMaterialWordVec()[index] != 0) {
                        svmTrainPrint.print(index + ":" + question.getMaterialWordVec()[index] + " ");
                    }
                }
                printStream.println();
                svmTrainPrint.println();
                 */

                printStream.println("QuestionStem:\t" + question.getQuestionStem());
                /**printStream.print("POS result:\t");
                for (String word : question.getStemWordSet()) {
                    printStream.print(word + " ");
                }
                printStream.println();
                printStream.print("StemWordVec:\t");
                for (int index = 0; index < self.wordSet.size(); index++) {
                    printStream.print(question.getStemWordVec()[index] + " ");
                    if (question.getStemWordVec()[index] != 0) {
                        svmTrainPrint.print(index + ":" + question.getStemWordVec()[index] + " ");
                    }
                }
                printStream.println();
                svmTrainPrint.println();
                 */

                printStream.print("POS result:\t");
                for (String word : question.getWordSet()) {
                    printStream.print(word + " ");
                }
                printStream.println();
                printStream.print("WordVec:\t");
                for (int index = 0; index < self.wordSet.size(); index++) {
                    printStream.print(question.getWordVec()[index] + " ");
                    if (question.getWordVec()[index] != 0) {
                        svmTrainPrint.print(index + ":" + question.getWordVec()[index] + " ");
                    }
                }
                printStream.println();
                svmTrainPrint.println();

                printStream.print("Original Material:\t");
                for (String originalMaterial : question.getOrignialMaterials()) {
                    printStream.print(originalMaterial + " ");
                }
                printStream.println();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        System.out.println("词目数量：" + self.getWordSet().size());
        for (String word : self.getWordSet()) {
            System.out.print(word + " ");
        }

    }
}

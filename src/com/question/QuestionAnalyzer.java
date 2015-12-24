package com.question;

import com.shawn.BasicIO;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.Math;

/**
 * Author:             Shawn Guo
 * E-mail:             air_fighter@163.com
 *
 * Create Time:        2015/11/26 10:37
 * Last Modified Time: 2015/12/15 09:29
 *
 * Class Name:         QuestionAnalyzer
 * Class Function:
 *                     该类在使用Question类的基础上，对题干进行分析，确定题目类型。
 */

public class QuestionAnalyzer {
    private String[] regexes = {                                        //切分标识符
            ".+。”",
            ".+！”",
            ".+？”",
            ".+。"
            };

    private ArrayList<Integer> typeSet = new ArrayList<>();             //使用type作为建立TFIDF向量
    private ArrayList<String>  stopWordSet = new ArrayList<>();
    private ICTCLASSeger seger = null;

    public void buildStopWordSet(String fileName) {
        try {
            stopWordSet = BasicIO.readFile2StringArray(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
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

    public void buildQuestionType(ArrayList<Question> questionList, String fileName) {
        int[] readType = new int[questionList.size()];
        try {
            readType = BasicIO.readFile2IntArray(questionList.size(), fileName);
            for (int index = 0; index < questionList.size(); index++) {
                questionList.get(index).setStemType(readType[index]);
                if (!typeSet.contains(readType[index])) {
                    typeSet.add(readType[index]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testQuesitonType(ArrayList<Question> questionList) {
        int rightNum = 0;
        QuestionClassifier classifier = new QuestionClassifier();
        classifier.initRegexes();
        for (Question question : questionList) {
            String regex = question.getQuestionStem();
            //regex = regex.replaceAll(".*”", "");
            int type = classifier.computeStemType(regex);
            if(type != question.getStemType()) {
                System.out.println("#" + Integer.sum(questionList.indexOf(question),1) + "\t" + regex);
                System.out.println("\ttype:" + type + "\trightType:" + question.getStemType());
            }
            else {
                rightNum++;
            }
        }

        System.out.println("共有" + questionList.size() + "道题目，其中" + rightNum +"道分类正确。");
    }

    public void buildCandidateType(ArrayList<Question> questionList) {
        String[] multiConj = {
                "、",
                "与",
                "和",
                "——",
                "，",
                "→"
        };
        for (Question question : questionList) {
            //System.out.println("#" + questionList.indexOf(question));
            int singleEntity = 0;
            int multiEntity = 0;
            int sentence = 0;
            for (int i = 1; i <= 4; i++) {
                try {
                    String tokenStr = seger.tokenizeAndTag(question.getCandidates(i - 1));
                    ArrayList<String> words = new ArrayList<>();
                    ArrayList<String> pos = new ArrayList<>();

                    for (String wordPOS : tokenStr.split(" ")) {
                        if (wordPOS.length() >=3 && wordPOS.contains("/")) {
                            words.add(wordPOS.split("/")[0]);
                            pos.add(wordPOS.split("/")[1]);
                        }
                    }
                    question.setCandidateWordandPOS(i, words, pos);

                    boolean posContainOther = false;
                    for (String po : pos) {
                        if (po.startsWith("v") ||                           //wyz是书名号，所以不能通过!startsWith("n")判断
                            po.startsWith("d") ||
                            po.startsWith("r") ||
                            po.startsWith("a") // ||
                            //po.startsWith("u")
                           ) {
                            posContainOther = true;
                            break;
                        }
                    }

                    boolean wordContainConj = false;
                    for (String conj : multiConj) {
                        if (words.contains(conj)) {
                            wordContainConj = true;
                            break;
                        }
                    }

                    if (posContainOther) {
                        sentence++;
                    }
                    else if (wordContainConj) {
                        multiEntity++;
                    }
                    else {
                        singleEntity++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (max(singleEntity, multiEntity, sentence) == singleEntity
                    &&  singleEntity >= 3) {
                question.setCandidateType(0);
            }
            else if (max(singleEntity, multiEntity, sentence) == multiEntity) {
                question.setCandidateType(1);
            }
            else {
                question.setCandidateType(2);
            }
        }
    }

    public int max(int a, int b, int c) {
        return Math.max(a, Math.max(b, c));
    }

    public void testCandidateType(ArrayList<Question> questionList) {
        int rightNum = 0;
        int[] type = null;
        try {
            type = BasicIO.readFile2IntArray(questionList.size(),
                    System.getProperty("user.dir") + "\\data\\questions\\entityclassification700.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Question question : questionList) {

            if(type[questionList.indexOf(question)] != question.getCandidateType()) {
                System.out.print("#" + Integer.sum(questionList.indexOf(question),1) + "\t");
                for (int i = 1; i <= 4; i++) {
                    ArrayList<String> words = question.getCandidateWord(i);
                    for (String word : words) {
                        System.out.print(word + " ");
                    }
                    ArrayList<String> pos = question.getCandidatePOS(i);
                    for (String po : pos) {
                        System.out.print(po + " ");
                    }
                    System.out.println();
                }
                System.out.println("type:" + question.getCandidateType() +
                        "\trightType:" + type[questionList.indexOf(question)]);
            }
            else {
                rightNum++;
            }
        }

        System.out.println("共有" + questionList.size() + "道题目，其中" + rightNum +"道分类正确。");
    }

    public static void main(String[] args) throws Exception{
        QuestionAnalyzer self = new QuestionAnalyzer();
        QuestionAcquisition acquisition = new QuestionAcquisition();

        System.out.print("Getting questions...");
        try {
            acquisition.init("\\data\\questions\\questions700.txt");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Done!");

        //self.seger = new ICTCLASSeger();
        self.seger = new ICTCLASSeger("\\data\\dicts\\databasic\\words-filter.dic");

        System.out.print("Analyzer: building stop words...");
        self.buildStopWordSet("data\\dicts\\stopwords_cn.txt");
        System.out.println("Done!");

        System.out.print("Analyzer: building Question's material...");
        self.buildQuestionMaterial(acquisition.questionList);
        System.out.println("Done!");

        System.out.print("Analyzer: building Question's stemType");
        self.buildQuestionType(acquisition.questionList, "data\\questions\\classification700.txt");
        System.out.println("Done!");

        System.out.print("Analyzer: building Question's candidateType");
        self.buildCandidateType(acquisition.questionList);
        System.out.println("Done!");

        System.out.println("Now, it's outputting");
        File outputFile = new File("out\\output.txt");
        try {
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            PrintStream printStream = new PrintStream(outputStream);

            int i = 0;
            for (Question question : acquisition.questionList) {
                i += 1;
                printStream.println("Question #" + i);

                printStream.println("Question:\t" + question.getQuestion());
                printStream.println("Candidates:\tA." + question.getCandidates(0)
                        + " B." + question.getCandidates(1)
                        + " C." + question.getCandidates(2)
                        + " D." + question.getCandidates(3));
                printStream.println("Candidate Type:\t" + question.getCandidateType());

                printStream.println("Candidates Segment:\t");
                for (int j = 1; j <= 4; j++) {
                    ArrayList<String> words = question.getCandidateWord(j);
                    ArrayList<String> pos = question.getCandidatePOS(j);
                    for (String word : words) {
                        printStream.print(word + " ");
                    }
                    for (String po : pos) {
                        printStream.print(po + " ");
                    }
                    printStream.println();
                }

                printStream.println("Type:\t" + question.getStemType());

                printStream.println("Materials:\t" + question.getMaterial());
                printStream.println("QuestionStem:\t" + question.getQuestionStem());

                printStream.print("Original Material:\t");
                for (String originalMaterial : question.getOrignialMaterials()) {
                    printStream.print(originalMaterial + " ");
                }
                printStream.println();
                printStream.println();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        self.testQuesitonType(acquisition.questionList);
        self.testCandidateType(acquisition.questionList);
    }
}
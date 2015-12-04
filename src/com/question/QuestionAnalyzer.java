package com.question;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author:             Shawn Guo
 * E-mail:             air_fighter@163.com
 *
 * Create Time:        2015/11/26 10:37
 * Last Modified Time: 2015/12/03 17:03
 *
 * Class Name:         QuestionAnalyzer
 * Class Function:
 *                     该类在使用Question类的基础上，对题干进行分析，确定题目类型。
 */

public class QuestionAnalyzer {
    private String[] regexes = {
            ".+。”",
            ".+。",
            ".+！”",
            ".+？”"
            };

    public void setQuestionMaterial(ArrayList<Question> questionList) {
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

            if (findInRegex && question.getMaterial().length() < (question.getQuesitonStem().length() - 7)) {
                Pattern p = Pattern.compile(".+，");
                String questionStemString = question.getQuesitonStem();
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

    public static void main(String[] args) throws Exception{
        QuestionAnalyzer self = new QuestionAnalyzer();
        QuestionAcquisition acquisition = new QuestionAcquisition();

        try {
            acquisition.init();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        self.setQuestionMaterial(acquisition.questionList);

        int i = 0;
        for(Question question : acquisition.questionList) {
            i += 1;
            System.out.println("Question #" + i);
            System.out.println("Question:\t" + question.getQuestion());
            System.out.println("Candidates:\tA." + question.getCandidates(0)
                    + " B." + question.getCandidates(1)
                    + " C." + question.getCandidates(2)
                    + " D." + question.getCandidates(3));
            System.out.println("Materials:\t" + question.getMaterial());
            System.out.print("POS result:");
            for (String word : question.getMaterialWordsSet()) {
                System.out.print(word + " ");
            }
            System.out.println();
            System.out.println("QuestionStem:\t" + question.getQuesitonStem());
            System.out.print("Original Material:\t");
            for (String originalMaterial : question.getOrignialMaterials()) {
                System.out.print(originalMaterial + " ");
            }
            System.out.println();

        }

    }
}

package com.question;

import java.io.IOException;
import java.util.ArrayList;

import com.shawn.BasicIO;

/**
 * Author:             Shawn Guo
 * E-mail:             air_fighter@163.com
 *
 * Create Time:        2015/11/26 08:41
 * Last Modified Time: 2015/12/2  09:31
 *
 * Class Name:         QuestionAcquisition
 * Class Function:
 *                     该类的主要功能是获取并且切分问题，得到题干和四个选项。
 */

public class QuestionAcquisition {
    public ArrayList<Question> questionList = new ArrayList<>();

    public void init() throws IOException, ClassNotFoundException {
        ArrayList<String> questionCandidateList = BasicIO.readFile2StringArray("questionFactory.txt");
        int odd = 1;
        Question question;
        for (String indexString : questionCandidateList) {
            if (odd == 1) {
                question = new Question();
                question.setQuestion(indexString);
                questionList.add(question);
                odd = 0;
                continue;
            }
            else {
                for (int i = 3; i >= 0; i--) {
                    questionList.get(questionList.size() - 1).setCandidate(i, indexString.split((char) Integer.sum(65, i) + ".")[1]);
                    indexString = indexString.split((char) Integer.sum(65, i) + ".")[0];
                    //System.out.println(question.getCandidates(i-1));
                }
                odd = 1;
                continue;
            }
        }


        /**
         String inputString = BasicIO.readFile2String("questionFactory.txt");
         inputString += "1.";
        String splitString = "\\d+" + "\\.";

        for(String indexString : inputString.split(splitString)) {
            if (indexString.length() <= 5)
                continue;

            String questionString = indexString;
            questionString += "E. ";
            //System.out.println(questionString);

            Question question = new Question();
            for (int i = 0; i < 5; i++) {
                if (i == 0) {
                    question.setQuestion(questionString.split((char) Integer.sum(65, i) + ".")[0]);
                    //System.out.print(question.getQuestion() + "\n");
                }
                else {
                    question.setCandidate(i - 1, questionString.split((char) Integer.sum(65, i) + ".")[0]);
                    //System.out.println(question.getCandidates(i-1));
                }
                questionString =  questionString.split((char)Integer.sum(65, i) + ".")[1];
            }

            questionList.add(question);
        }
         */

    }

    public static void main(String[] args) throws Exception {
        QuestionAcquisition self = new QuestionAcquisition();
        self.init();

        for (int i = 0; i < self.questionList.size(); i++) {
            System.out.print("Question " + (i + 1) +": ");
            System.out.println(self.questionList.get(i).getQuestion());
        }
    }
}

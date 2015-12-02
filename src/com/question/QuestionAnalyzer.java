package com.question;


import com.shawn.BasicIO;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Author:             Shawn Guo
 * E-mail:             air_fighter@163.com
 *
 * Create Time:        2015/11/26 10:37
 * Last Modified Time: 2015/11/30
 *
 * Class Name:         QuestionAnalyzer
 * Class Function:
 *                     该类在使用Question类的基础上，对题干进行分析，确定题目类型。
 */

public class QuestionAnalyzer {
    private double[] materialW = new double[2];
    private double materialB = 0.0;

    public void materialRelevanceModelTrain(ArrayList<Question> questionList) {
        int[] y = new int[questionList.size()];

        try {
            y = BasicIO.readFile2IntArray(questionList.size(), "questionMaterialRelevance.txt");

        } catch (IOException e) {
            e.printStackTrace();
        }
        /**
        for (int i = 0; i< questionList.size(); i++) {
            System.out.print(y[i] + " ");
        }
         */
    }

    public double[] getQuoteRatio(ArrayList<Question> questionList) {
        double[] quoteRatio = new double[2];
        for (int i = 0; i < questionList.size(); i++) {

        }
        return quoteRatio;
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

        self.materialRelevanceModelTrain(acquisition.questionList);
    }
}

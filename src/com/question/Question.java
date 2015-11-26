package com.question;

/**
 * Author:             Shawn Guo
 * E-mail:             air_fighter@163.com
 *
 * Create Time:        2015/11/26 09:05
 * Last Modified Time: 2015/11/26 10:37
 *
 * Class Name:         Question
 * Class Function:
 *                     该类的主要功能存储一个问题的题干和对应的四个选项，并且可以存储正确选项。
 *                     答案的初始化字符为“ ”。
 */
public class Question {
    private String question = null;
    private String[] candidates = {"", "", "", ""};
    private char answer = ' ';

    public boolean setQuestion(String input) {
        question = input;
        if (question == null)
            return false;
        return true;
    }

    public boolean setCandidate(int index, String input) {
        candidates[index] = input;
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

    public String getQuestion() {
        if (question == null)
            System.err.println("WARNING: This Question's question hasn't been initialized");
        return question;
    }

    public String getCandidates(int index) {
        if (candidates[index] == "")
            System.err.println("WARNING: This Question's candidates hasn't been initialized");
        return candidates[index];
    }

    public char getAnswer() {
        if (answer == ' ')
            System.err.println("WARNING: This Question's answer hasn't been initialized");
        return answer;
    }
}

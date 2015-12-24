package com.question;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author:             Shawn Guo
 * E-mail:             air_fighter@163.com
 *
 * Create Time:        2015/12/14 08:28
 * Last Modified Time: 2015/12/14
 *
 * Class Name:         QuestionClassifier
 * Class Function:
 *                     该类在使用Question类的基础上，直接通过小众题型的关键词匹配确定题目类型。
 */

public class QuestionClassifier {
    private ArrayList<ArrayList<String>> regexes = new ArrayList<>();

    public void initRegexes() {
        ArrayList<String> regex00 = new ArrayList<String>(){{
            add(".*“.+”处.*是.*");
        }};
        ArrayList<String> regex01 = new ArrayList<String>(){{
            add(".*不同.*");
            add(".*分歧.*");
        }};
        ArrayList<String> regex02 = new ArrayList<String>(){{
            add(".*相同.*");
            add(".*相似的.*");
            //add(".*也.*");
            add(".*都.*");
            add(".*共同.*");
            add(".*不仅.*还.*");
            add(".*下列.*同.*");
        }};
        ArrayList<String> regex03 = new ArrayList<String>(){{
            add(".*排序.*");
        }};
        ArrayList<String> regex04 = new ArrayList<String>(){{
            add(".*不.*");
            add(".*无.*");
            add(".*未.*");
            add(".*错误.*");
            add(".*有误.*");
            add(".*违背.*");
        }};
        ArrayList<String> regex05 = new ArrayList<String>(){{
        }};
        regexes.add(regex00);
        regexes.add(regex01);
        regexes.add(regex02);
        regexes.add(regex03);
        regexes.add(regex04);
        regexes.add(regex05);
    }

    public int computeStemType(String input) {
        for (ArrayList<String> regexList : regexes) {
            for (String regex : regexList) {
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(input);
                if (m.find()) {
                    return regexes.indexOf(regexList);
                }
            }
        }
        return 5;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        QuestionClassifier self = new QuestionClassifier();
        self.initRegexes();

        System.out.println(self.computeStemType(input));
    }
}

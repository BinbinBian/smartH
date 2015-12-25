package com.test;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.splitWord.analysis.NlpAnalysis;

import java.util.List;

/**
 * Created by shawn on 15-12-25.
 */

public class TestANSJ {
    public static void main(String[] args) {
        List<Term> parse = NlpAnalysis.parse("第二次世界大战后，美苏两国逐渐走向政治、经济、军事领域的全面对抗。苏联在1949年成立经济互助委员会，协调和促进社会主义阵营各成员国的经济发展。此举主要针对的是\n" +
                "A.杜鲁门主义    B.马歇尔计划     C.北约组织       D.德意志联邦共和国");
        for (Term term : parse) {
            System.out.println(term.toString());
        }
    }
}

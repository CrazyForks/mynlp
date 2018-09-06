package com.mayabot.nlp.segment.analyzer;

import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.MynlpTokenizers;

/**
 * 标准的，包含过滤标点符号和停用词.
 * 用户可以实现自定义的
 *
 * @author jimichan
 */
public class StandardMynlpAnalyzer extends BaseMynlpAnalyzer {


    public StandardMynlpAnalyzer(MynlpTokenizer tokenizer) {
        super(tokenizer);
    }


    public StandardMynlpAnalyzer() {
        this(MynlpTokenizers.coreTokenizer());
    }

    @Override
    protected WordTermGenerator warp(WordTermGenerator base) {
        base = new PunctuationFilter(base);
        base = new StopwordFilter(base);
        return base;
    }

}

package com.simplespring.mvc.processor;

import com.simplespring.mvc.RequestProcessorChain;

public interface RequestProcessor {
    boolean process(RequestProcessorChain requestProcessorChain) throws Exception;
}

package com.aim.mvc.processor;

import com.aim.mvc.RequestProcessorChain;

public interface RequestProcessor {
    boolean process(RequestProcessorChain requestProcessorChain) throws Exception;
}

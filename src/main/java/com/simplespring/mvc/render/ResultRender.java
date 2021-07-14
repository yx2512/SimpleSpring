package com.simplespring.mvc.render;

import com.simplespring.mvc.RequestProcessorChain;

public interface ResultRender {
    void render(RequestProcessorChain requestProcessorChain) throws Exception;
}

package com.aim.mvc.render;

import com.aim.mvc.RequestProcessorChain;

public interface ResultRender {
    void render(RequestProcessorChain requestProcessorChain) throws Exception;
}

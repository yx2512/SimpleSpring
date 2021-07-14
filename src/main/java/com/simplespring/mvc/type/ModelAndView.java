package com.simplespring.mvc.type;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
    private String view;
    private Map<String, Object> model;

    public ModelAndView() {
        this.model = new HashMap<>();
    }

    public ModelAndView setView(String view) {
        this.view = view;
        return this;
    }

    public ModelAndView addViewData(String key, Object value) {
        model.put(key, value);
        return this;
    }

    public String getView() {
        return view;
    }

    public Map<String, Object> getModel() {
        return model;
    }
}

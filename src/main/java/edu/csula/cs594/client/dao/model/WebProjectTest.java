package edu.csula.cs594.client.dao.model;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class WebProjectTest {
    private String uri, method;
    private Map<String, String> params;


    public WebProjectTest(String uri, String method, Map<String, String> params) {
        this.uri = uri;
        this.method = method;
        this.params =  params;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;

    }

    public WebProjectTest(){

    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }








}

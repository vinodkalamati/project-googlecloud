package com.stackroute.datapopulator.zuulapi.pre;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.exception.ZuulException;
public class SimpleFilter extends ZuulFilter {
    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    public boolean shouldFilter() {
        return false;
    }

    public Object run() throws ZuulException {
        return null;
    }
}

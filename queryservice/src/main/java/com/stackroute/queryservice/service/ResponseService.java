package com.stackroute.queryservice.service;

import com.stackroute.queryservice.domain.NotFoundResponse;

import java.util.List;

public interface ResponseService {
    public List<NotFoundResponse> getResponses();
    public void saveResponse(NotFoundResponse response);
}

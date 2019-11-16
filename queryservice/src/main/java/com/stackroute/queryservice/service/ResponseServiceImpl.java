package com.stackroute.queryservice.service;

import com.stackroute.queryservice.domain.NotFoundResponse;
import com.stackroute.queryservice.repository.ResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResponseServiceImpl implements ResponseService {
    private ResponseRepository responseRepository;
    @Autowired
    public ResponseServiceImpl(ResponseRepository responseRepository) {
        this.responseRepository = responseRepository;
    }
    @Override
    public List<NotFoundResponse> getResponses() {
        return responseRepository.findAll();
    }

    @Override
    public void saveResponse(NotFoundResponse response) {
        responseRepository.save(response);
    }
}

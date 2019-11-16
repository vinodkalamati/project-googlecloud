package com.stackroute.resultfetcher.service;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
@Service
public interface ResultFetcherService {
    public List resultFetcher(String param);
    public void domainDataToFrontEndService(String param);
    public void resultToFrontEndService(String param) throws IOException, ParseException;
}

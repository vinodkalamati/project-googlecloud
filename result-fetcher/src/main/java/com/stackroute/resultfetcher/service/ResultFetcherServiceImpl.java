package com.stackroute.resultfetcher.service;
import com.google.gson.Gson;
import com.stackroute.resultfetcher.domain.ProcessedQueryModel;
import com.stackroute.resultfetcher.domain.ResultModel;
import com.stackroute.resultfetcher.repository.ResultFetcherRepository;
import org.apache.commons.lang.WordUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResultFetcherServiceImpl implements ResultFetcherService{
    private ResultFetcherRepository resultFetcherRepository;
    @Autowired
    public ResultFetcherServiceImpl(ResultFetcherRepository resultFetcherRepository) {
        this.resultFetcherRepository = resultFetcherRepository;
    }

    private ResultModel resultModel;
    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;
    public List resultFetcher(String param) {                    //method to fetch result from database
        resultModel=new ResultModel();
        Gson gson = new Gson();
        List<String> matchedConstraints;
        List resultObject=new ArrayList();                             //will be a list of 3 objects[string to be printed before actual result,actual result,querymodel object to transfer query and domain variables]
        List<Map> actualResult=new ArrayList<>();                    //list of results[{queryresult:corresponding list of result},...]
        ProcessedQueryModel processedQueryModel = gson.fromJson(param.toLowerCase(), ProcessedQueryModel.class);
        List forValuesOfConstraints=new ArrayList<>();                 //collects constraints to join them with "and" later
        List forValuesOfQueryResults=new ArrayList();                   //collects query results to join them with "and" later
        //------------------for every required query result---------------------------------------
        List<String> queryResult=processedQueryModel.getQueryresult();
        if(queryResult.size()>1){
            if(queryResult.contains("movie")|| queryResult.contains("disease")){
                for (String a: queryResult
                ) {
                    if(!a.equals("movie")&& !a.equals("disease")){
                        List<String> tempList=new ArrayList<>();                          //to store results of each queryresult
                        Map tempMap=new HashMap();                                        //stores{each queryresult:tempList}
                        List<Map> resultForMatchedNodes;
                        List<String>resultForCommonNodes;
                        if(processedQueryModel.getConstraints().size()>1){                  //for more than one constraint
                            matchedConstraints= resultFetcherRepository.getCommonNodes(param.toLowerCase());
                            resultForCommonNodes= resultFetcherRepository.getResultNodesForCommonNodes(a,matchedConstraints); //gives common Nodes
                            for (String s:matchedConstraints
                            ) {
                                if(!forValuesOfConstraints.contains(WordUtils.capitalizeFully(s)))        //to remove duplicates in repeated constraints
                                    forValuesOfConstraints.add(WordUtils.capitalizeFully(s));
                            }
                            for (String s:resultForCommonNodes
                            ) {
                                tempList.add(s);
                            }
                        }
                        else{
                            matchedConstraints= resultFetcherRepository.getMatchedNodes(param.toLowerCase());           //for single constraint
                            resultForMatchedNodes= resultFetcherRepository.getResultNodesForMatchedNodes(a,matchedConstraints);         //gives matching Nodes
                            for (Map m:resultForMatchedNodes
                            ) {
                                if(!forValuesOfConstraints.contains(WordUtils.capitalizeFully(String.valueOf(m.get("key")))))
                                    forValuesOfConstraints.add(WordUtils.capitalizeFully(String.valueOf(m.get("key"))));
                                tempList.add(String.valueOf(m.get("value")));
                            }
                        }
                        tempMap.put("key",a);
                        tempMap.put("value",tempList);
                        actualResult.add(tempMap);
                        forValuesOfQueryResults.add(WordUtils.capitalizeFully(a));
                    }
                }
            }
            else {
                for (String a: queryResult
                ) {
                    List<String> tempList=new ArrayList<>();                          //to store results of each queryresult
                    Map tempMap=new HashMap();                                        //stores{each queryresult:tempList}
                    List<Map> resultForMatchedNodes;
                    matchedConstraints= resultFetcherRepository.getMatchedNodes(param.toLowerCase());           //for single constraint
                    resultForMatchedNodes= resultFetcherRepository.getResultNodeForMatchedNodes(a,matchedConstraints);         //gives matching Nodes
                    for (Map m:resultForMatchedNodes
                    ) {
                        if(!forValuesOfConstraints.contains(WordUtils.capitalizeFully(String.valueOf(m.get("key")))))
                            forValuesOfConstraints.add(WordUtils.capitalizeFully(String.valueOf(m.get("key"))));
                        tempList.add(String.valueOf(m.get("value")));
                    }

                    tempMap.put("key",a);
                    tempMap.put("value",tempList);
                    actualResult.add(tempMap);
                    forValuesOfQueryResults.add(WordUtils.capitalizeFully(a));
                }
            }
        }
        else {
            String a=queryResult.get(0);
            if(a.equals("movie")||a.equals("disease")){
                List<String> tempList=new ArrayList<>();                          //to store results of each queryresult
                Map tempMap=new HashMap();                                        //stores{each queryresult:tempList}
                List<String>resultForCommonNodes;
                if(processedQueryModel.getConstraints().size()>1){                  //for more than one constraint
                    matchedConstraints= resultFetcherRepository.getCommonNodes(param.toLowerCase());
                    resultForCommonNodes= resultFetcherRepository.getResultNodeForCommonNodes(a,matchedConstraints);           //gives common Nodes
                    for (Map m:processedQueryModel.getConstraints()
                    ) {
                        if(!forValuesOfConstraints.contains(WordUtils.capitalizeFully(String.valueOf(m.get("value")))))        //to remove duplicates in repeated constraints
                            forValuesOfConstraints.add(WordUtils.capitalizeFully(String.valueOf(m.get("value"))));
                    }
                    for (String s:resultForCommonNodes
                    ) {
                        tempList.add(s);
                    }
                }
                else{
                    matchedConstraints= resultFetcherRepository.getMatchedNodes(param.toLowerCase());           //for single constraint
                    resultForCommonNodes= resultFetcherRepository.getResultNodesForCommonNodes(a,matchedConstraints);         //gives matching Nodes
                    for (Map m:processedQueryModel.getConstraints()
                    ) {
                        if(!forValuesOfConstraints.contains(WordUtils.capitalizeFully(String.valueOf(m.get("value")))))        //to remove duplicates in repeated constraints
                            forValuesOfConstraints.add(WordUtils.capitalizeFully(String.valueOf(m.get("value"))));
                    }
                    for (String s:resultForCommonNodes
                    ) {
                        tempList.add(s);
                    }
                }
                tempMap.put("key",a);
                tempMap.put("value",tempList);
                actualResult.add(tempMap);
                forValuesOfQueryResults.add(WordUtils.capitalizeFully(a));
            }
            else {
                List<String> tempList=new ArrayList<>();                          //to store results of each queryresult
                Map tempMap=new HashMap();                                        //stores{each queryresult:tempList}
                List<Map> resultForMatchedNodes;
                matchedConstraints= resultFetcherRepository.getMatchedNodes(param.toLowerCase());           //for single constraint
                resultForMatchedNodes= resultFetcherRepository.getResultNodeForMatchedNodes(a,matchedConstraints);         //gives matching Nodes
                for (Map m:resultForMatchedNodes
                ) {
                    if(!forValuesOfConstraints.contains(WordUtils.capitalizeFully(String.valueOf(m.get("key")))))
                        forValuesOfConstraints.add(WordUtils.capitalizeFully(String.valueOf(m.get("key"))));
                    tempList.add(String.valueOf(m.get("value")));
                }
                tempMap.put("key",a);
                tempMap.put("value",tempList);
                actualResult.add(tempMap);
                forValuesOfQueryResults.add(WordUtils.capitalizeFully(a));
            }
        }
        resultObject.add(String.join(",",forValuesOfQueryResults)+" of "+String.join(",",forValuesOfConstraints));
        resultObject.add(actualResult);
        resultObject.add(processedQueryModel);
        return resultObject;
    }

    public void domainDataToFrontEndService(String param) {        //method to display domain data to frontend
        Map data;
        List<Map> dataList=new ArrayList<>();
        org.json.JSONArray dataToFrontEnd=new org.json.JSONArray();
        List result=resultFetcher(param);
        ProcessedQueryModel processedQueryModel=(ProcessedQueryModel)result.get(2);
        List tempKeys=new ArrayList<>();
        for (Map m:processedQueryModel.getConstraints()
             ) {
            List<String> tempList=new ArrayList<>();
            data=new HashMap();
            if(!tempKeys.contains(m.get("key"))){
                tempKeys.add(m.get("key"));
                data.put("key",m.get("key"));
                tempList.add(m.get("value").toString());
            }
            else {
                List<Map> tempMapList=new ArrayList<>();
                tempList.add(m.get("value").toString());
                for (Map m1:dataList
                ) {
                    if (m1.get("key").equals(m.get("key"))){
                        for (Object s: (ArrayList)m1.get("value")
                             ) {
                            tempList.add(s.toString());
                        }
                        tempMapList.add(m1);
                    }
                }
               dataList.removeAll(tempMapList);
            }
            data.put("key",m.get("key"));
            data.put("value",tempList);
            data.put("type","constraint");
            dataList.add(data);
        }

        for (Object m:(ArrayList)result.get(1)
             ) {
            data=new HashMap();
            data.put("key",((HashMap)m).get("key"));
            data.put("value",((HashMap)m).get("value"));
            data.put("type","result");
            dataList.add(data);
        }
        for (Map m1: dataList
        ) {
            dataToFrontEnd.put(m1);
        }

        kafkaTemplate.send("DomainData", dataToFrontEnd.toString());
    }

    public void resultToFrontEndService(String param) throws IOException, ParseException {                     //method to display result and suggestions to frontend
        String[] finalResult;                                //stores the array of results
        String[] finalSuggestions;                           //stores the array of suggestions
        Gson gson = new Gson();
        List resultObject=resultFetcher(param);
        List<Map> actualResult=(ArrayList)resultObject.get(1);
        List<String> resultList=new ArrayList();
        ProcessedQueryModel processedQueryModel=(ProcessedQueryModel)resultObject.get(2);
        for (Map m:actualResult                             //for results
             ) {
            for (Object o:(ArrayList)m.get("value")
                 ) {
                resultList.add(o.toString());
            }
        }
        List<String> suggestions=new ArrayList<>();                     //for suggestions
        JSONParser jsonParser=new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader("resources/suggestions.json"));
        for (Map m: processedQueryModel.getConstraints()               //check if the constraints have a movie name or disease name if yes add respective suggestions
             ) {
            if(m.get("key").equals("movie")){
                JSONArray jsonArray=(JSONArray) jsonObject.get("movie");
                for (Object o: jsonArray
                     ) {
                    suggestions.add(o.toString()+" "+WordUtils.capitalizeFully(m.get("value").toString()));
                }
            }
            else if (m.get("key").equals("disease")){
                JSONArray jsonArray=(JSONArray)jsonObject.get("medical");
                for (Object o: jsonArray
                ) {
                    suggestions.add(o.toString()+" "+WordUtils.capitalizeFully(m.get("value").toString()));
                }
            }
        }
        for (String s: processedQueryModel.getQueryresult()) {            //check if the query result have a movie name or disease name if yes add respective suggestions
            if(s.equals("movie")){
                JSONArray jsonArray=(JSONArray)jsonObject.get("movie");
                for (Map m: actualResult) {
                    if(m.get("key").equals("movie")){
                        for (Object o: (ArrayList)m.get("value")) {
                            for (Object o1: jsonArray) {
                                suggestions.add(o1.toString()+" "+WordUtils.capitalizeFully(o.toString()));
                            }
                        }
                    }
                }
            }
            else if(s.equals("medical")){
                JSONArray jsonArray=(JSONArray)jsonObject.get("medical");
                for (Map m: actualResult) {
                    if(m.get("key").equals("disease")){
                        for (Object o: (ArrayList)m.get("value")) {
                            for (Object o1: jsonArray) {
                                suggestions.add(o1.toString()+" "+WordUtils.capitalizeFully(o.toString()));
                            }
                        }
                    }
                }
            }
        }
        finalResult=new String[resultList.size()+1];
        finalSuggestions=new String[suggestions.size()];
        finalResult[0]=resultObject.get(0).toString();
        int i=1;
        int j=0;
        for (String s:resultList) {
            finalResult[i]=WordUtils.capitalizeFully(s);
            i++;
        }
        for (String s:suggestions){
            finalSuggestions[j]=s;
            j++;
        }
        resultModel.setResult(finalResult);
        resultModel.setSuggestions(finalSuggestions);
        resultModel.setQuery(processedQueryModel.getQuery());
        resultModel.setStatus("result");
        String json = gson.toJson(resultModel);
        kafkaTemplate.send("FinalResult", json);                  //write result to FinalResult topic

    }

}

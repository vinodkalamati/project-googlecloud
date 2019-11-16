package com.stackroute.datapopulator.service;
import com.google.gson.Gson;
import com.stackroute.datapopulator.domain.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
@Service
public class NlpResultConverterService {
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;
    public boolean nlpResultConverter(String param) throws ParseException {
        Gson gson = new Gson();
        MedicalNlpResultModel medicalNlpResultModel=new MedicalNlpResultModel();
        MovieNlpResultModel movieNlpResultModel=new MovieNlpResultModel();
        JSONParser jsonParser=new JSONParser();
        JSONObject jsonObject=(JSONObject) jsonParser.parse(param);
        List<String> type;
        Map<String,String> properties;
        List<ConvertedNode> lcn=new ArrayList<>();
        List<Relationship> lrl=new ArrayList<>();
        ConvertedDataModel cdm=new ConvertedDataModel();
        if(jsonObject.get("domain").equals("movie")){
            movieNlpResultModel=gson.fromJson(param,MovieNlpResultModel.class);
            cdm.setDomain(movieNlpResultModel.getDomain());
            cdm.setUserid(movieNlpResultModel.getUserid());
            cdm.setQuery(movieNlpResultModel.getQuery());
            ConvertedNode cn=new ConvertedNode();
            type=new ArrayList<>();
            type.add("movie");
            properties=new HashMap();
            properties.put("name",movieNlpResultModel.getMovieName());
            cn.setUuid(UUID.randomUUID().toString());
            cn.setType(type);
            cn.setProperties(properties);
            lcn.add(cn);
            for (String s: movieNlpResultModel.getCasts()  //2
                 ) {
                ConvertedNode temp=new ConvertedNode();
                type=new ArrayList<>();
                type.add("artist");
                properties=new HashMap();
                properties.put("name",s);
                temp.setUuid(UUID.randomUUID().toString());
                temp.setType(type);
                temp.setProperties(properties);
                lcn.add(temp);
                Relationship tempr=new Relationship();
                tempr.setUuid(UUID.randomUUID().toString());
                tempr.setRelation("acted by");
                tempr.setSourcenode(cn.getUuid());
                tempr.setDestnode(temp.getUuid());
                lrl.add(tempr);
            }
            for (String s: movieNlpResultModel.getDirectors()   //3
            ) {
                ConvertedNode temp=new ConvertedNode();
                type=new ArrayList<>();
                type.add("director");
                properties=new HashMap();
                properties.put("name",s);
                temp.setUuid(UUID.randomUUID().toString());
                temp.setType(type);
                temp.setProperties(properties);
                lcn.add(temp);
                Relationship tempr=new Relationship();
                tempr.setUuid(UUID.randomUUID().toString());
                tempr.setRelation("directed by");
                tempr.setSourcenode(cn.getUuid());
                tempr.setDestnode(temp.getUuid());
                lrl.add(tempr);
            }
            for (String s: movieNlpResultModel.getProductionHouse() //4
            ) {
                ConvertedNode temp=new ConvertedNode();
                type=new ArrayList<>();
                type.add("producer");
                properties=new HashMap();
                properties.put("name",s);
                temp.setUuid(UUID.randomUUID().toString());
                temp.setType(type);
                temp.setProperties(properties);
                lcn.add(temp);
                Relationship tempr=new Relationship();
                tempr.setUuid(UUID.randomUUID().toString());
                tempr.setRelation("produced by");
                tempr.setSourcenode(cn.getUuid());
                tempr.setDestnode(temp.getUuid());
                lrl.add(tempr);
            }
            for (String s: movieNlpResultModel.getCollection()    //5
            ) {
                ConvertedNode temp=new ConvertedNode();
                type=new ArrayList<>();
                type.add("boxOffice");
                properties=new HashMap();
                properties.put("name",s);
                temp.setUuid(UUID.randomUUID().toString());
                temp.setType(type);
                temp.setProperties(properties);
                lcn.add(temp);
                Relationship tempr=new Relationship();
                tempr.setUuid(UUID.randomUUID().toString());
                tempr.setRelation("collection");
                tempr.setSourcenode(cn.getUuid());
                tempr.setDestnode(temp.getUuid());
                lrl.add(tempr);
            }
            for (String s: movieNlpResultModel.getCountry()  //6
            ) {
                ConvertedNode temp=new ConvertedNode();
                type=new ArrayList<>();
                type.add("country");
                properties=new HashMap();
                properties.put("name",s);
                temp.setUuid(UUID.randomUUID().toString());
                temp.setType(type);
                temp.setProperties(properties);
                lcn.add(temp);
                Relationship tempr=new Relationship();
                tempr.setUuid(UUID.randomUUID().toString());
                tempr.setRelation("released in");
                tempr.setSourcenode(cn.getUuid());
                tempr.setDestnode(temp.getUuid());
                lrl.add(tempr);
            }
            for (String s: movieNlpResultModel.getReleaseDate()    //7
            ) {
                ConvertedNode temp=new ConvertedNode();
                type=new ArrayList<>();
                type.add("release date");
                properties=new HashMap();
                properties.put("name",s);
                temp.setUuid(UUID.randomUUID().toString());
                temp.setType(type);
                temp.setProperties(properties);
                lcn.add(temp);
                Relationship tempr=new Relationship();
                tempr.setUuid(UUID.randomUUID().toString());
                tempr.setRelation("released on");
                tempr.setSourcenode(cn.getUuid());
                tempr.setDestnode(temp.getUuid());
                lrl.add(tempr);
            }

        }
        else{
            medicalNlpResultModel=gson.fromJson(param,MedicalNlpResultModel.class);
            cdm.setDomain(medicalNlpResultModel.getDomain());
            cdm.setUserid(medicalNlpResultModel.getUserid());
            cdm.setQuery(medicalNlpResultModel.getQuery());
            ConvertedNode cn=new ConvertedNode();
            type=new ArrayList<>();
            type.add("disease");
            properties=new HashMap();
            properties.put("name",medicalNlpResultModel.getDisease());
            cn.setUuid(UUID.randomUUID().toString());
            cn.setType(type);
            cn.setProperties(properties);
            lcn.add(cn);
            for (String s: medicalNlpResultModel.getSymptoms()   //2
            ) {
                ConvertedNode temp=new ConvertedNode();
                type=new ArrayList<>();
                type.add("symptoms");
                properties=new HashMap();
                properties.put("name",s);
                temp.setUuid(UUID.randomUUID().toString());
                temp.setType(type);
                temp.setProperties(properties);
                lcn.add(temp);
                Relationship tempr=new Relationship();
                tempr.setUuid(UUID.randomUUID().toString());
                tempr.setRelation("has");
                tempr.setSourcenode(cn.getUuid());
                tempr.setDestnode(temp.getUuid());
                lrl.add(tempr);
            }
            for (String s: medicalNlpResultModel.getMedicine()    //3
            ) {
                ConvertedNode temp=new ConvertedNode();
                type=new ArrayList<>();
                type.add("medication");
                properties=new HashMap();
                properties.put("name",s);
                temp.setUuid(UUID.randomUUID().toString());
                temp.setType(type);
                temp.setProperties(properties);
                lcn.add(temp);
                Relationship tempr=new Relationship();
                tempr.setUuid(UUID.randomUUID().toString());
                tempr.setRelation("required");
                tempr.setSourcenode(cn.getUuid());
                tempr.setDestnode(temp.getUuid());
                lrl.add(tempr);
            }
            for (String s: medicalNlpResultModel.getDeathCount()  //4
            ) {
                ConvertedNode temp=new ConvertedNode();
                type=new ArrayList<>();
                type.add("deaths");
                properties=new HashMap();
                properties.put("name",s);
                temp.setUuid(UUID.randomUUID().toString());
                temp.setType(type);
                temp.setProperties(properties);
                lcn.add(temp);
                Relationship tempr=new Relationship();
                tempr.setUuid(UUID.randomUUID().toString());
                tempr.setRelation("results in");
                tempr.setSourcenode(cn.getUuid());
                tempr.setDestnode(temp.getUuid());
                lrl.add(tempr);
            }
            for (String s: medicalNlpResultModel.getCauses()    //5
            ) {
                ConvertedNode temp=new ConvertedNode();
                type=new ArrayList<>();
                type.add("causes");
                properties=new HashMap();
                properties.put("name",s);
                temp.setUuid(UUID.randomUUID().toString());
                temp.setType(type);
                temp.setProperties(properties);
                lcn.add(temp);
                Relationship tempr=new Relationship();
                tempr.setUuid(UUID.randomUUID().toString());
                tempr.setRelation("cause");
                tempr.setSourcenode(cn.getUuid());
                tempr.setDestnode(temp.getUuid());
                lrl.add(tempr);
            }
            for (String s: medicalNlpResultModel.getTreatment()    //6
            ) {
                ConvertedNode temp=new ConvertedNode();
                type=new ArrayList<>();
                type.add("treatment");
                properties=new HashMap();
                properties.put("name",s);
                temp.setUuid(UUID.randomUUID().toString());
                temp.setType(type);
                temp.setProperties(properties);
                lcn.add(temp);
                Relationship tempr=new Relationship();
                tempr.setUuid(UUID.randomUUID().toString());
                tempr.setRelation("required treatment");
                tempr.setSourcenode(cn.getUuid());
                tempr.setDestnode(temp.getUuid());
                lrl.add(tempr);
            }
        }
        cdm.setNodes(lcn);
        cdm.setRelationship(lrl);
        kafkaTemplate.send("WikiScrapper",cdm.toString());
        return true;
    }
}

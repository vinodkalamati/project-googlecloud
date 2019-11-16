package com.stackroute.knowably.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stackroute.knowably.domain.HealthCare;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.UUID;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "api/v1")
@PropertySource("classpath:application.properties")
public class HealthCareWikiScrapperServiceImpl implements HealthCareWikiScrapperService {
    @Value("${topic}")
    private String topic;
    @Value("${uuid}")
    private String uuid;
    @Value("${type}")
    private String type;
    @Value("${name}")
    private String name;
    @Value("${properties}")
    private String properties;
    @Value("${destNode}")
    private String destNode;
    @Value("${sourceNode}")
    private String sourceNode;
    @Value("${relation}")
    private String relation;

    KafkaTemplate<String,String> kafkaTemplate;
    private static final Logger LOGGER= LoggerFactory.getLogger(HealthCareWikiScrapperServiceImpl.class);
    @Autowired
    public HealthCareWikiScrapperServiceImpl(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /*
            function to listen on topic "Wikipedia" to consume wikipedia links
            and crawl the wikipedia page of Medical domain and transform
            data into structured data
        */
    @KafkaListener(topics = "Wikipedia",groupId = "wiki_scrapper",containerFactory = "kafkaListenerContainerFactory")
    public void HealthCareWikiScrapper(String receivedInput) throws ParseException {
        /*
            Converting String to JSON object
        */
        String pattern = "\\[.*?\\]";
        String pattern2 = "\\(.*?\\)";
        JSONParser parser = new JSONParser();
        JSONArray jsonArray = (JSONArray) parser.parse(receivedInput);
        for (int j = 0; j <jsonArray.size(); j++){
            JSONObject jsonObject = (JSONObject) jsonArray.get(j);
            HealthCare healthCare = new HealthCare();
            JSONArray urlArray = (JSONArray) jsonObject.get("url");
            healthCare.setUrl((String)urlArray.get(0).toString());
            healthCare.setConcept((String)jsonObject.get("concept"));
            healthCare.setUserId((String)jsonObject.get("userId"));
            healthCare.setDomain((String)jsonObject.get("domain"));
            healthCare.setId((String)jsonObject.get("id"));
            healthCare.setQuery((String)jsonObject.get("query"));

         /*
            Getting url from the input object from google-search-service
         */
            String url= healthCare.getUrl();

            /*
             * Initializing nodes and realtionship array to store all nodes and their relations
             * */
            JSONArray nodesArray = new JSONArray();
            JSONArray relationArray = new JSONArray();
            String diseaseNodeId="";
            try {
                /*
                 * fetching web document from the url
                 */
                Document source = Jsoup.connect(url).get();

                /*
                 * Extracting the table that contains the data of use from wikipedia page
                 * */
                Elements table = source.select("table.infobox");
                Elements rows = table.select("tr");
                Elements table1 = source.select("table.infobox tr th");

            /*
                Scrapping the disease name from the wikipedia
                    and other names for that disease if present
            */
                for (Element el : table1.subList(0, Math.min(1, table1.size()))) {
                    String title = el.text();
                    String[] titleArray = title.split("/");
                    for(int i=0;i<titleArray.length;i++){
                        diseaseNodeId = UUID.randomUUID().toString();  //generating random IDs

                        /*
                         * Creating node for the name of Disease
                         * */
                        JSONObject nodeTitle = new JSONObject();
                        nodeTitle.put(this.uuid,diseaseNodeId);
                        nodeTitle.put(this.type,"Disease");
                        JSONObject titleJsonObject= new JSONObject();
                        titleJsonObject.put(this.name, titleArray[i].trim());
                        nodeTitle.put(this.properties,titleJsonObject);
                        nodesArray.add(nodeTitle);

                    }
                }

                /*
                 * Traversing the rows of the tables
                 * */
                for (Element row : rows) {
                    Elements data1 = row.select("th");
                    Elements data2 = row.select("td");
                    String column1 = data1.text();

                 /*
                    Scrapping the Symptoms of Disease from the wikipedia
                        and other names for that disease if present
                */
                    if (column1.matches("Symptoms")) {
                        String column2 = data2.text();
                        String[] symptomArray = column2.replaceAll(pattern, "").replaceAll(pattern2, "").replaceAll("[a-zA-Z]+[:]", ",").split(",");
                        for (int i = 0; i < symptomArray.length; i++) {
                            if (!symptomArray[i].isEmpty()) {
                                /*
                                 * Creating node for each Symptom extracted from the page
                                 * */
                                String symptomNodeId = UUID.randomUUID().toString();
                                JSONObject node = new JSONObject();
                                node.put(this.uuid,symptomNodeId);
                                node.put(this.type, "Symptom");

                                JSONObject symptomJsonObject = new JSONObject();
                                symptomJsonObject.put(this.name, symptomArray[i].trim());
                                node.put(this.properties,symptomJsonObject);

                                /*
                                 * Establishing a relationship between symptom node and the disease title node
                                 * */
                                JSONObject relationshipObject = new JSONObject();
                                relationshipObject.put(this.uuid, symptomNodeId);
                                relationshipObject.put(this.sourceNode,diseaseNodeId);
                                relationshipObject.put(this.destNode, symptomNodeId);
                                relationshipObject.put(this.relation, "has");

                                nodesArray.add(node);

                                relationArray.add(relationshipObject);
                            }
                        }
                    }

                 /*
                    Scrapping the medication for Disease from the wikipedia
                        and other names for that disease if present
                */
                    if (column1.matches("Medication")) {
                        String column2 = data2.text();
                        String[] medicationArray = column2.replaceAll(pattern, "").replaceAll(pattern2, "").replaceAll("[a-zA-Z]+[:]", ",").split(",");
                        for (int i = 0; i < medicationArray.length; i++) {
                            if (!medicationArray[i].isEmpty()) {
                                /*
                                 * Creating node for each Medication extracted from the page
                                 * */
                                String medicationNodeId = UUID.randomUUID().toString();
                                JSONObject node = new JSONObject();
                                node.put(this.uuid,medicationNodeId);
                                node.put(this.type, "Medication");

                                JSONObject medicationJsonObject = new JSONObject();
                                medicationJsonObject.put(this.name, medicationArray[i].trim());
                                node.put(this.properties,medicationJsonObject);
                                nodesArray.add(node);

                                /*
                                 * Establishing a relationship between Medication node and the disease title node
                                 * */
                                JSONObject relationshipObject = new JSONObject();
                                relationshipObject.put(this.uuid, medicationNodeId);
                                relationshipObject.put(this.sourceNode,diseaseNodeId);
                                relationshipObject.put(this.destNode, medicationNodeId);
                                relationshipObject.put(this.relation, "required");

                                relationArray.add(relationshipObject);
                            }
                        }
                    }

                 /*
                    Scrapping the complications due to disease from the wikipedia
                        and other names for that disease if present
                */
                    if (column1.matches("Complications")) {
                        String column2 = data2.text();
                        String[] complicationsArray = column2.replaceAll(pattern, "").replaceAll(pattern2, "").replaceAll("[a-zA-Z]+[:]", ",").split(",");
                        for (int i = 0; i < complicationsArray.length; i++) {
                            if (!complicationsArray[i].isEmpty()) {
                                /*
                                 * Creating node for each Complication extracted from the page
                                 * */
                                String complicationNodeId = UUID.randomUUID().toString();
                                JSONObject node = new JSONObject();
                                node.put(this.uuid,complicationNodeId);
                                node.put(this.type, "Complication");

                                JSONObject complicationJsonObject = new JSONObject();
                                complicationJsonObject.put(this.name, complicationsArray[i].trim());
                                node.put(this.properties,complicationJsonObject);
                                nodesArray.add(node);

                                /*
                                 * Establishing a relationship between Complication node and the disease title node
                                 * */
                                JSONObject relationshipObject = new JSONObject();
                                relationshipObject.put(this.uuid, complicationNodeId);
                                relationshipObject.put(this.sourceNode,diseaseNodeId);
                                relationshipObject.put(this.destNode, complicationNodeId);
                                relationshipObject.put(this.relation, "have");
                                relationArray.add(relationshipObject);
                            }
                        }
                    }

                /*
                    Scrapping the Death count due to disease from the wikipedia
                        and other names for that disease if present
                */
                    if (column1.matches("Deaths")) {
                        String column2 = data2.text().replaceAll(pattern, "").replaceAll(pattern2, "").replaceAll("[a-zA-Z]+[:]", ",");
                        if (!column2.isEmpty()) {
                            /*
                             * Creating node for Death extracted from the page
                             * */
                            String deathNodeId = UUID.randomUUID().toString();
                            JSONObject node = new JSONObject();
                            node.put(this.uuid,deathNodeId);
                            node.put(this.type, "Death");

                            JSONObject deathJsonObject = new JSONObject();
                            deathJsonObject.put("count", column2.trim());
                            node.put(this.properties,deathJsonObject);
                            nodesArray.add(node);

                            /*
                             * Establishing a relationship between Death node and the disease title node
                             * */
                            JSONObject relationshipObject = new JSONObject();
                            relationshipObject.put(this.uuid, deathNodeId);
                            relationshipObject.put(this.sourceNode,diseaseNodeId);
                            relationshipObject.put(this.destNode, deathNodeId);
                            relationshipObject.put(this.relation, "results in");
                            relationArray.add(relationshipObject);
                        }
                    }

                /*
                    Scrapping the causes of disease from the wikipedia
                        and other names for that disease if present
                */
                    if (column1.matches("Causes")) {
                        String column2 = data2.text();
                        String[] causesArray = column2.replaceAll(pattern, "").replaceAll(pattern2, "").replaceAll("[a-zA-Z]+[:]", ",").split(",");
                        for (int i = 0; i < causesArray.length; i++) {
                            if (!causesArray[i].isEmpty()) {
                                /*
                                 * Creating node for each cause extracted from the page
                                 * */
                                String causeNodeId = UUID.randomUUID().toString();
                                JSONObject node = new JSONObject();
                                node.put(this.uuid,causeNodeId);
                                node.put(this.type, "Cause");

                                JSONObject causeJsonObject = new JSONObject();
                                causeJsonObject.put(this.name, causesArray[i].trim());
                                node.put(this.properties,causeJsonObject);
                                nodesArray.add(node);

                                /*
                                 * Establishing a relationship between Cause node and the disease title node
                                 * */
                                JSONObject relationshipObject = new JSONObject();
                                relationshipObject.put(this.uuid, causeNodeId);
                                relationshipObject.put(this.sourceNode,diseaseNodeId);
                                relationshipObject.put(this.destNode, causeNodeId);
                                relationshipObject.put(this.relation, "caused by");
                                relationArray.add(relationshipObject);
                            }
                        }
                    }
                /*
                    Scrapping the frequency of disease from the wikipedia
                        and other names for that disease if present
                */
                    if (column1.matches("Frequency")) {
                        String column2 = data2.text();
                        String[] frequencyArray = column2.replaceAll(pattern, "").replaceAll(pattern2, "").replaceAll("[a-zA-Z]+[:]", ",").split(",");
                        for (int i = 0; i < frequencyArray.length; i++) {
                            if (!frequencyArray[i].isEmpty()) {
                                /*
                                 * Creating node for Frequency extracted from the page
                                 * */
                                String frequencyNodeId = UUID.randomUUID().toString();
                                JSONObject node = new JSONObject();
                                node.put(this.uuid,frequencyNodeId);
                                node.put(this.type,"Frequency");
                                JSONObject frequencyJsonObject = new JSONObject();
                                frequencyJsonObject.put(this.name, frequencyArray[i].trim());
                                node.put(this.properties,frequencyJsonObject);
                                nodesArray.add(node);

                                /*
                                 * Establishing a relationship between Frequency node and the disease title node
                                 * */
                                JSONObject relationshipObject = new JSONObject();
                                relationshipObject.put(this.uuid, frequencyNodeId);
                                relationshipObject.put(this.sourceNode,diseaseNodeId);
                                relationshipObject.put(this.destNode, frequencyNodeId);
                                relationshipObject.put(this.relation, "occurs");
                                relationArray.add(relationshipObject);
                            }
                        }
                    }

                /*
                    Scrapping the treatment for disease from the wikipedia
                        and other names for that disease if present
                */
                    if (column1.matches("Treatment")) {
                        String column2 = data2.text();
                        String[] treatmentArray = column2.replaceAll(pattern, "").replaceAll(pattern2, "").replaceAll("[a-zA-Z]+[:]", ",").split(",");
                        for (int i = 0; i < treatmentArray.length; i++) {
                            if (!treatmentArray[i].isEmpty()) {

                                /*
                                 * Creating node for Treatment extracted from the page
                                 * */
                                String treatmentNodeId = UUID.randomUUID().toString();
                                JSONObject node = new JSONObject();
                                node.put(this.uuid,treatmentNodeId);
                                node.put(this.type, "Treatment");
                                JSONObject treatmentJsonObject = new JSONObject();
                                treatmentJsonObject.put(this.name, treatmentArray[i].trim());
                                node.put(this.properties,treatmentJsonObject);
                                nodesArray.add(node);

                                /*
                                 * Establishing a relationship between treatment node and the disease title node
                                 * */
                                JSONObject relationshipObject = new JSONObject();
                                relationshipObject.put(this.uuid, treatmentNodeId);
                                relationshipObject.put(this.sourceNode,diseaseNodeId);
                                relationshipObject.put(this.destNode, treatmentNodeId);
                                relationshipObject.put(this.relation, "required treatment");
                                relationArray.add(relationshipObject);
                            }
                        }
                    }
                /*
                    Scrapping the Risk factors of disease from the wikipedia
                        and other names for that disease if present
                */
                    if (column1.matches("Risk factors")) {
                        String column2 = data2.text();
                        String[] riskFactorsArray = column2.replaceAll(pattern, "").replaceAll(pattern2, "").replaceAll("[a-zA-Z]+[:]", ",").split(",");
                        for (int i = 0; i < riskFactorsArray.length; i++) {
                            if (!riskFactorsArray[i].isEmpty()) {

                                /*
                                 * Creating node for Risk Factors extracted from the page
                                 * */
                                String riskFactorsNodeId = UUID.randomUUID().toString();
                                JSONObject node = new JSONObject();
                                node.put(this.uuid,riskFactorsNodeId);
                                node.put(this.type, "Risk factor");
                                JSONObject riskFactorsJsonObject = new JSONObject();
                                riskFactorsJsonObject.put(this.name, riskFactorsArray[i].trim());
                                node.put(this.properties,riskFactorsJsonObject);
                                nodesArray.add(node);

                                /*
                                 * Establishing a relationship between Risk factors node and the disease title node
                                 * */
                                JSONObject relationshipObject = new JSONObject();
                                relationshipObject.put(this.uuid, riskFactorsNodeId);
                                relationshipObject.put(this.sourceNode,diseaseNodeId);
                                relationshipObject.put(this.destNode, riskFactorsNodeId);
                                relationshipObject.put(this.relation, "has risk");
                                relationArray.add(relationshipObject);
                            }
                        }
                    }
                }

            /*
                Creating a jsonObject contains nodesArray
                    and relationshipArray of relations among the nodes
            */
                JSONObject wikipediaDataDTO = new JSONObject();
                wikipediaDataDTO.put("nodes",nodesArray);
                wikipediaDataDTO.put("relationship",relationArray);
                wikipediaDataDTO.put("query", healthCare.getQuery());
                wikipediaDataDTO.put("userId", healthCare.getUserId());
		 wikipediaDataDTO.put("domain", "medical");
                publishDatatoKafka(wikipediaDataDTO);


            } catch (IOException e) {
		     e.printStackTrace();
               LOGGER.error(e.getMessage());
            }
        }
    }

    public void publishDatatoKafka(JSONObject wikipediaDataDTO){
        /* producing the wikipediaDataDTO in kafka on topic WikiScrapper*/
        ObjectMapper objectMapper=new ObjectMapper();
        try {
            String resultPayload= objectMapper.writeValueAsString(wikipediaDataDTO);
            kafkaTemplate.send(topic,resultPayload);


        } catch (JsonProcessingException e) {
		 e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
    }
}

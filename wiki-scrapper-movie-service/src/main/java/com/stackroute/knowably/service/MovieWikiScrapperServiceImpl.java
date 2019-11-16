package com.stackroute.knowably.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stackroute.knowably.domain.Movie;
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
public class MovieWikiScrapperServiceImpl implements MovieWikiScrapperService {
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
    @Value("${pattern}")
    private String pattern;
    @Value("${pattern2}")
    private String pattern2;
    @Value("${language}")
    private String language;
    @Value("${tag}")
    private String tag;
    @Value("${country}")
    private String country;

    KafkaTemplate<String,String> kafkaTemplate;
    private static final Logger LOGGER= LoggerFactory.getLogger(MovieWikiScrapperServiceImpl.class);
    @Autowired
    public MovieWikiScrapperServiceImpl(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /*
            function to listen on topic "Wikipedia" to consume wikipedia links
            and crawl the wikipedia page of Medical domain and transform
            data into structured data
        */
    @KafkaListener(topics = "MovieWikipedia",groupId = "wiki_scrapper",containerFactory = "kafkaListenerContainerFactory")
    public void MovieWikiScrapper(String receivedInput) throws ParseException {

        /*
            Converting String to JSON object
        */
        JSONParser parser = new JSONParser();
        JSONArray jsonArray = (JSONArray) parser.parse(receivedInput);
        for (int j = 0; j < jsonArray.size(); j++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(j);
            Movie movie = new Movie();
            JSONArray urlArray = (JSONArray) jsonObject.get("url");
            movie.setUrl((String) urlArray.get(0).toString());
            movie.setConcept((String) jsonObject.get("concept"));
            movie.setUserId((String) jsonObject.get("userId"));
            movie.setDomain((String) jsonObject.get("domain"));
            movie.setId((String) jsonObject.get("id"));
            movie.setQuery((String) jsonObject.get("query"));

         /*
            Getting url from the input object from google-search-service
         */
            String url = movie.getUrl();

            JSONArray nodesArray = new JSONArray();
            JSONArray relationArray = new JSONArray();
            String movieNodeId = "";
            try {
                /*
                 * fetching web document from the url
                 */
                String header = "";
                Document source = Jsoup.connect(url).get();

                /*
                 * Extracting the table that contains the data of use from wikipedia page
                 * */
                Elements table = source.select("table.infobox");
                Elements title = table.select("tr th.summary");
                for(Element el : title){
                    movieNodeId = UUID.randomUUID().toString();
                    JSONObject movieTitleNode = new JSONObject();

                    /*
                     * Creating node for the name of movie
                     * */
                    movieTitleNode.put(this.uuid, movieNodeId);
                    movieTitleNode.put(this.type, "Movie");
                    JSONObject nameJsonObject = new JSONObject();
                    nameJsonObject.put(this.name, el.text().trim());
                    movieTitleNode.put(this.properties, nameJsonObject);
                    nodesArray.add(movieTitleNode);
                    break;
                }

                /*
                 * Traversing the rows of the tables
                 * */
                Elements rows = table.select("tr");
                for (Element row : rows) {
                    Elements data1 = row.select("th");
                    Elements data2 = row.select("td");
                    if (!data1.select("div").isEmpty()) {
                        Elements headerElement = data1.select("div");
                        header = headerElement.html();
                        header = header.replace("<br>", "\n").replaceAll("\n", "");
                    } else {
                        header = data1.html();
                        header = header.replace("<br>", " ");
                    }

                    //Extracting Cast of the specific movie from Html Document
                    if (header.matches("Starring")) {
                        if (!data2.select("div").isEmpty()) {
                            Elements data3 = data2.select("div ul li a");
                            String str1 = data3.html();
                            String[] starringArray = str1.replaceAll(pattern, "").replaceAll(pattern2, "").replaceAll("[a-zA-Z]+[:]", ",").split("\n");
                            for (int i = 0; i < starringArray.length; i++) {
                                if (!starringArray[i].isEmpty()) {
                                    String starringNodeId = UUID.randomUUID().toString();
                                    JSONObject starringNode = new JSONObject();
                                    /*
                                     * Creating node for each actor extracted from the page
                                     * */
                                    starringNode.put(this.uuid, starringNodeId);
                                    starringNode.put(this.type, "Artist");
                                    JSONObject nameJsonObject = new JSONObject();
                                    nameJsonObject.put(this.name, starringArray[i].trim());
                                    starringNode.put(this.properties, nameJsonObject);
                                    nodesArray.add(starringNode);

                                    /*
                                     * Establishing a relationship between actor node and the movie title node
                                     * */
                                    JSONObject relationshipJsonObject = new JSONObject();
                                    String relationId = UUID.randomUUID().toString();
                                    relationshipJsonObject.put(this.uuid, relationId);
                                    relationshipJsonObject.put(this.destNode,movieNodeId);
                                    relationshipJsonObject.put(this.sourceNode, starringNodeId);
                                    relationshipJsonObject.put(this.relation, "acted by");
                                    relationArray.add(relationshipJsonObject);
                                }
                            }
                        } else {
                            Elements data3 = data2.select("a");
                            String str1 = data3.html();
                            String[] str = str1.replaceAll(pattern, "").replaceAll(pattern2, "").replaceAll("[a-zA-Z]+[:]", ",").split("\n");
                            for (int i = 0; i < str.length; i++) {
                                if (!str[i].isEmpty()) {
                                    /*
                                     * Creating node for each actor extracted from the page
                                     * */
                                    String starringNodeId = UUID.randomUUID().toString();
                                    JSONObject starringNode = new JSONObject();
                                    starringNode.put(this.uuid, starringNodeId);
                                    starringNode.put(this.type, "Artist");

                                    JSONObject nameJsonObject = new JSONObject();
                                    nameJsonObject.put(this.name, str[i].trim());
                                    starringNode.put(this.properties, nameJsonObject);
                                    nodesArray.add(starringNode);
                                    /*
                                     * Establishing a relationship between actor node and the movie title node
                                     * */
                                    JSONObject relationshipJsonObject = new JSONObject();
                                    String relationId = UUID.randomUUID().toString();
                                    relationshipJsonObject.put(this.uuid, relationId);
                                    relationshipJsonObject.put(this.sourceNode,movieNodeId);
                                    relationshipJsonObject.put(this.destNode, starringNodeId);
                                    relationshipJsonObject.put(this.relation, "acted by");
                                    relationArray.add(relationshipJsonObject);
                                }
                            }
                        }
                    }

                    //Extracting Production Company from Html Document
                    if (header.matches("Productioncompany") && !data2.select("div").isEmpty()) {
                            Elements data3 = data2.select("div");
                            if (!data2.select("a").isEmpty()) {
                                Elements data4 = data2.select("a");
                                String str1 = data4.html();
                                str1 = str1.replace("<br>", "\n");
                                String[] productionCompanyArray = str1.replaceAll(pattern, "").replaceAll(pattern2, "").replaceAll("[a-zA-Z]+[:]", ",").split("\n");
                                for (int i = 0; i < productionCompanyArray.length; i++) {
                                    if (!productionCompanyArray[i].isEmpty()) {
                                        /*
                                         * Creating node for each production company extracted from the page
                                         * */
                                        String productionCompanyNodeId = UUID.randomUUID().toString();
                                        JSONObject productionCompanyNode = new JSONObject();
                                        productionCompanyNode.put(this.uuid, productionCompanyNodeId);
                                        productionCompanyNode.put(this.type, "Producer");

                                        JSONObject nameJsonObject = new JSONObject();
                                        nameJsonObject.put(this.name, productionCompanyArray[i].trim());
                                        productionCompanyNode.put(this.properties, nameJsonObject);
                                        nodesArray.add(productionCompanyNode);

                                        /*
                                         * Establishing a relationship between production company node and the movie title node
                                         * */
                                        JSONObject relationshipJsonObject = new JSONObject();
                                        String relationId = UUID.randomUUID().toString();
                                        relationshipJsonObject.put(this.uuid, relationId);
                                        relationshipJsonObject.put(this.sourceNode,movieNodeId);
                                        relationshipJsonObject.put(this.destNode, productionCompanyNodeId);
                                        relationshipJsonObject.put(this.relation, "produced by");
                                        relationArray.add(relationshipJsonObject);
                                    }
                                }
                            }
                            String column2 = data3.html();
                            column2 = column2.replace("<br>", "\n");
                            String[] str = column2.replace(pattern, "").replace(pattern2, "").replace("[a-zA-Z]+[:]", ",").split("\n");
                            for (int i = 0; i < str.length; i++) {
                                if (!str[i].isEmpty() && !str[i].contains(("<a "))) {
                                        /*
                                         * Creating node for each production company extracted from the page
                                         * */
                                        String productionCompanyNodeId = UUID.randomUUID().toString();
                                        JSONObject productionComapnyNode = new JSONObject();
                                        productionComapnyNode.put(this.uuid, productionCompanyNodeId);
                                        productionComapnyNode.put(this.type, "Producer");

                                        JSONObject nameJsonObject = new JSONObject();
                                        nameJsonObject.put(this.name, str[i].trim());
                                        productionComapnyNode.put(this.properties, nameJsonObject);
                                        nodesArray.add(productionComapnyNode);

                                        /*
                                         * Establishing a relationship between production company node and the movie title node
                                         * */
                                        JSONObject relationshipJsonObject = new JSONObject();
                                        String relationId = UUID.randomUUID().toString();
                                        relationshipJsonObject.put(this.uuid, relationId);
                                        relationshipJsonObject.put(this.sourceNode,movieNodeId);
                                        relationshipJsonObject.put(this.destNode, productionCompanyNodeId);
                                        relationshipJsonObject.put(this.relation, "produced by");
                                        relationArray.add(relationshipJsonObject);

                                }
                            }

                        }


                    //Extracting Directors from Html Document
                    if (header.matches("Directed by")) {
                        Elements data4 = data2.select("a");
                        String str1 = data4.html();
                        String[] directorArray = str1.replace(pattern, "").replace(pattern2, "").replace("[a-zA-Z]+[:]", ",").split("\n");
                        for (int i = 0; i < directorArray.length; i++) {
                            if (!directorArray[i].isEmpty()) {
                                /*
                                 * Creating node for each Director extracted from the page
                                 * */
                                String directorNodeId = UUID.randomUUID().toString();
                                JSONObject directorNode = new JSONObject();
                                directorNode.put(this.uuid, directorNodeId);
                                directorNode.put(this.type, "Director");

                                JSONObject nameJsonObject = new JSONObject();
                                nameJsonObject.put(this.name, directorArray[i].trim());
                                directorNode.put(this.properties, nameJsonObject);
                                nodesArray.add(directorNode);

                                /*
                                 * Establishing a relationship between Director node and the movie title node
                                 * */
                                JSONObject relationshipJsonObject = new JSONObject();
                                String relationId = UUID.randomUUID().toString();
                                relationshipJsonObject.put(this.uuid, relationId);
                                relationshipJsonObject.put(this.sourceNode,movieNodeId);
                                relationshipJsonObject.put(this.destNode, directorNodeId);
                                relationshipJsonObject.put(this.relation, "directed by");
                                relationArray.add(relationshipJsonObject);
                            }
                        }
                    }

                    //Extracting Country from Html Document
                    if (header.matches(country)) {
                        if (!data2.select("div").isEmpty()) {
                            Elements data3 = data2.select(tag);
                            String countryWiki = data3.html();
                            String[] countryArray = countryWiki.replace(pattern, "").replace(pattern2, "").replace("[a-zA-Z]+[:]", ",").split("\n");
                            for (int i = 0; i < countryArray.length; i++) {
                                if (!countryArray[i].isEmpty()) {
                                    String countryName = countryArray[i].substring(0, countryArray[i].indexOf("<sup"));
                                    /*
                                     * Creating node for each Country extracted from the page
                                     * */
                                    String countryNodeId = UUID.randomUUID().toString();
                                    JSONObject countryNode = new JSONObject();
                                    countryNode.put(this.uuid, countryNodeId);
                                    countryNode.put(this.type, country);

                                    JSONObject nameJsonObject = new JSONObject();
                                    nameJsonObject.put(this.name, countryName.trim());
                                    countryNode.put(this.properties, nameJsonObject);
                                    nodesArray.add(countryNode);

                                    /*
                                     * Establishing a relationship between Country node and the movie title node
                                     * */
                                    JSONObject relationshipJsonObject = new JSONObject();
                                    String relationId = UUID.randomUUID().toString();
                                    relationshipJsonObject.put(this.uuid, relationId);
                                    relationshipJsonObject.put(this.sourceNode,movieNodeId);
                                    relationshipJsonObject.put(this.destNode, countryNodeId);
                                    relationshipJsonObject.put(this.relation, "released in");
                                    relationArray.add(relationshipJsonObject);
                                }
                            }
                        } else {
                            String str1 = data2.html();
                            String[] countryArray = str1.replace(pattern, "").replace(pattern2, "").replace("[a-zA-Z]+[:]", ",").split("\n");
                            for (int i = 0; i < countryArray.length; i++) {
                                if (!countryArray[i].isEmpty()) {

                                    /*
                                     * Creating node for each Country extracted from the page
                                     * */
                                    String countryNodeId = UUID.randomUUID().toString();
                                    JSONObject countryNode = new JSONObject();
                                    countryNode.put(this.uuid, countryNodeId);
                                    countryNode.put(this.type, country);

                                    JSONObject nameJsonObject = new JSONObject();
                                    nameJsonObject.put(this.name, countryArray[i].trim());
                                    countryNode.put(this.properties, nameJsonObject);
                                    nodesArray.add(countryNode);

                                    /*
                                     * Establishing a relationship between Country node and the movie title node
                                     * */
                                    JSONObject relationshipJsonObject = new JSONObject();
                                    String relationId = UUID.randomUUID().toString();
                                    relationshipJsonObject.put(this.uuid, relationId);
                                    relationshipJsonObject.put(this.sourceNode,movieNodeId);
                                    relationshipJsonObject.put(this.destNode, countryNodeId);
                                    relationshipJsonObject.put(this.relation, "released in");
                                    relationArray.add(relationshipJsonObject);
                                }
                            }
                        }
                    }

                    //Extracting Language from Html Document
                    if (header.matches(language)) {
                        if (!data2.select("div").isEmpty()) {
                            Elements data3 = data2.select(tag);
                            String languageWiki = data3.html();
                            String[] languageArray = languageWiki.replace(pattern, "").replace(pattern2, "").replace("[a-zA-Z]+[:]", ",").split("\n");
                            for (int i = 0; i < languageArray.length; i++) {
                                if (!languageArray[i].isEmpty()) {
                                    /*
                                     * Creating node for each Language extracted from the page
                                     * */
                                    String languageNodeId = UUID.randomUUID().toString();
                                    JSONObject languageNode = new JSONObject();
                                    languageNode.put(this.uuid, languageNodeId);
                                    languageNode.put(this.type, language);

                                    JSONObject nameJsonObject = new JSONObject();
                                    nameJsonObject.put(this.name, languageArray[i].trim());
                                    languageNode.put(this.properties, nameJsonObject);
                                    nodesArray.add(languageNode);

                                    /*
                                     * Establishing a relationship between Language node and the movie title node
                                     * */
                                    JSONObject relationshipJsonObject = new JSONObject();
                                    String relationId = UUID.randomUUID().toString();
                                    relationshipJsonObject.put(this.uuid, relationId);
                                    relationshipJsonObject.put(this.sourceNode,movieNodeId);
                                    relationshipJsonObject.put(this.destNode, languageNodeId);
                                    relationshipJsonObject.put(this.relation, language);
                                    relationArray.add(relationshipJsonObject);
                                }
                            }
                        } else {
                            String str1 = data2.html();
                            String[] languageArray = str1.replace(pattern, "").replace(pattern2, "").replace("[a-zA-Z]+[:]", ",").split("\n");
                            for (int i = 0; i < languageArray.length; i++) {
                                if (!languageArray[i].isEmpty()) {

                                    /*
                                     * Creating node for each Language extracted from the page
                                     * */
                                    String languageNodeId = UUID.randomUUID().toString();
                                    JSONObject languageNode = new JSONObject();
                                    languageNode.put(this.uuid, languageNodeId);
                                    languageNode.put(this.type, language);

                                    JSONObject nameJsonObject = new JSONObject();
                                    nameJsonObject.put(this.name, languageArray[i].trim());
                                    languageNode.put(this.properties, nameJsonObject);
                                    nodesArray.add(languageNode);

                                    /*
                                     * Establishing a relationship between Language node and the movie title node
                                     * */
                                    JSONObject relationshipJsonObject = new JSONObject();
                                    String relationId = UUID.randomUUID().toString();
                                    relationshipJsonObject.put(this.uuid, relationId);
                                    relationshipJsonObject.put(this.sourceNode,movieNodeId);
                                    relationshipJsonObject.put(this.destNode, languageNodeId);
                                    relationshipJsonObject.put(this.relation, language);
                                    relationArray.add(relationshipJsonObject);
                                }
                            }
                        }
                    }

                    //Extracting Release Date from Html Document
                    if (header.matches("Release date")) {
                        Elements data3 = data2.select(tag);
                        String str1 = data3.html().substring(0, data3.html().indexOf("<span"));
                        String[] releaseDateArray = str1.replace("&nbsp;", " ").split("\n");
                        for (int i = 0; i < releaseDateArray.length; i++) {
                            if (!releaseDateArray[i].isEmpty()) {

                                /*
                                 * Creating node for release date extracted from the page
                                 * */
                                String releaseDateNodeId = UUID.randomUUID().toString();
                                JSONObject releaseDateNode = new JSONObject();
                                releaseDateNode.put(this.uuid, releaseDateNodeId);
                                releaseDateNode.put(this.type, "Release Date");

                                JSONObject nameJsonObject = new JSONObject();
                                nameJsonObject.put(this.name, releaseDateArray[i].trim());
                                releaseDateNode.put(this.properties, nameJsonObject);
                                nodesArray.add(releaseDateNode);

                                /*
                                 * Establishing a relationship between release date node and the movie title node
                                 * */
                                JSONObject relationshipJsonObject = new JSONObject();
                                String relationId = UUID.randomUUID().toString();
                                relationshipJsonObject.put(this.uuid, relationId);
                                relationshipJsonObject.put(this.sourceNode,movieNodeId);
                                relationshipJsonObject.put(this.destNode, releaseDateNodeId);
                                relationshipJsonObject.put(this.relation, "released on");
                                relationArray.add(relationshipJsonObject);
                            }
                        }
                    }

                    //Extracting Box Office Collection from Html Document
                    if (header.matches("Box office")) {
                        if (!data2.select("span").isEmpty()) {
                            String boxOffice = data2.html();
                            String amountTemp = "";
                            StringBuilder amountTemp1 = new StringBuilder();
                            String str1 = boxOffice.substring(boxOffice.indexOf("</span>") + 7, boxOffice.indexOf("<sup")).replace("&nbsp;"," ");
                            if (data2.select("span").html().length() > 1) {
                                int len = data2.select("span").html().length();
                                String amount = data2.select("span").html().substring(1, len);
                                amountTemp = "Rs. " + amount;
                            } else {
                                amountTemp = "Rs. ";
                            }
                            amountTemp1.append(amountTemp).append(str1);
                            /*
                             * Creating node for box office extracted from the page
                             * */
                            String boxOfficeNodeId = UUID.randomUUID().toString();
                            JSONObject boxOfficeNode = new JSONObject();
                            boxOfficeNode.put(this.uuid, boxOfficeNodeId);
                            boxOfficeNode.put(this.type, "BoxOffice");

                            JSONObject nameJsonObject = new JSONObject();
                            nameJsonObject.put(this.name, amountTemp1);
                            boxOfficeNode.put(this.properties, nameJsonObject);
                            nodesArray.add(boxOfficeNode);

                            /*
                             * Establishing a relationship between box office node and the movie title node
                             * */
                            JSONObject relationshipJsonObject = new JSONObject();
                            String relationId = UUID.randomUUID().toString();
                            relationshipJsonObject.put(this.uuid, relationId);
                            relationshipJsonObject.put(this.sourceNode,movieNodeId);
                            relationshipJsonObject.put(this.destNode, boxOfficeNodeId);
                            relationshipJsonObject.put(this.relation, "collection");
                            relationArray.add(relationshipJsonObject);
                        } else {
                            String boxOffice = data2.html().substring(0, data2.html().indexOf("<sup")).replace("&nbsp;"," ");
                            String boxOfficeNodeId = UUID.randomUUID().toString();
                            /*
                             * Creating node for box office extracted from the page
                             * */
                            JSONObject boxOfficeNode = new JSONObject();
                            boxOfficeNode.put(this.uuid, boxOfficeNodeId);
                            boxOfficeNode.put(this.type, "BoxOffice");

                            JSONObject nameJsonObject = new JSONObject();
                            nameJsonObject.put(this.name, boxOffice.trim());
                            boxOfficeNode.put(this.properties, nameJsonObject);
                            nodesArray.add(boxOfficeNode);
                            /*
                             * Establishing a relationship between box office node and the movie title node
                             * */
                            JSONObject relationshipJsonObject = new JSONObject();
                            String relationId = UUID.randomUUID().toString();
                            relationshipJsonObject.put(this.uuid, relationId);
                            relationshipJsonObject.put(this.sourceNode,movieNodeId);
                            relationshipJsonObject.put(this.destNode, boxOfficeNodeId);
                            relationshipJsonObject.put(this.relation, "collection");
                            relationArray.add(relationshipJsonObject);
                        }
                        break;
                    }
                }

                /* creating a jsonObject contains nodesArray and relationship array*/
                JSONObject wikipediaDataDTO = new JSONObject();
                wikipediaDataDTO.put("nodes",nodesArray);
                wikipediaDataDTO.put("relationship",relationArray);
                wikipediaDataDTO.put("query", movie.getQuery());
                wikipediaDataDTO.put("userId", movie.getUserId());
		wikipediaDataDTO.put("domain", "movie");
                //producing the jsonDTO in Kafaka
                publishDatatoKafka(wikipediaDataDTO);
            }

            catch (IOException e) {
                LOGGER.error(e.getMessage());
            }

        }

    }

    public void publishDatatoKafka(JSONObject wikipediaDataDTO){
        /* producing the jsonDTO in kafka on topic WikiScrapper*/
        ObjectMapper objectMapper=new ObjectMapper();
        try {
            String resultPayload= objectMapper.writeValueAsString(wikipediaDataDTO);
            kafkaTemplate.send(topic,resultPayload);


        } catch (JsonProcessingException e) {
           LOGGER.error(e.getMessage());
        }
    }
}

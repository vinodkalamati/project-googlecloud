package com.stackroute.datapopulator.service;
import com.google.gson.Gson;
import com.stackroute.datapopulator.domain.*;
import com.stackroute.datapopulator.repository.DataPopulatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DataPopulatorService {
    private DataPopulatorRepository dataRepository;
    @Autowired
    public DataPopulatorService(DataPopulatorRepository dataRepository) {
        this.dataRepository = dataRepository;
    }
    public Gson gson = new Gson();
    public Object dataPopulator(String line){                               //method to store data in database
        DataModel datamodel = gson.fromJson(line, DataModel.class);
        ConvertedDataModel convertedDataModel=new ConvertedDataModel();     // convert to required format
        List<ConvertedNode> result=new ArrayList<>();
        for (Node n: datamodel.getNodes()
        ) {
            ConvertedNode convertedNode=new ConvertedNode();
            convertedNode.setUuid(n.getUuid());
            convertedNode.setProperties(n.getProperties());
            List<String> temp=new ArrayList<>();
            temp.add(n.getType());
            convertedNode.setType(temp);
            result.add(convertedNode);
        }

        convertedDataModel.setNodes(result);
        convertedDataModel.setRelationship(datamodel.getRelationship());
        convertedDataModel.setQuery(datamodel.getQuery());
        convertedDataModel.setUserid(datamodel.getUserid());
        convertedDataModel.setDomain(datamodel.getDomain());
        String json=gson.toJson(convertedDataModel);
        dataRepository.createNodes(json);
        dataRepository.createRelations(json);
	System.out.println(convertedDataModel.getUserid());
	System.out.println(convertedDataModel.getQuery());
        if(convertedDataModel.getUserid().equals("internal")&& convertedDataModel.getQuery()!=null){        //for internal requests when result not found in the database
		System.out.println("inif");
    		return convertedDataModel;
        }
        else {
            return "null";
        }

    }
    public boolean dataFromFrontEndService(String param) {                     //for populating data coming from front end
        List<Map> constraints=new ArrayList<>();
        List<Map> results=new ArrayList<>();
        List<Map> temp=gson.fromJson(param,List.class);
        for (Map m:temp                                          //separating constraints and results
        ) {
            if(m.get("type").equals("result")){
               results.add(m);
            }
            else{
                constraints.add(m);
            }
        }
        for (Map m: results                                                     // for each result do
             ) {
            if(!dataRepository.checkForNode(m.get("key").toString(),((ArrayList)m.get("value")).get(0).toString())){  //check if the node already exists,if not then create
                ConvertedNode cn=new ConvertedNode();
                List<ConvertedNode> lcn=new ArrayList<>();
                List<String> type=new ArrayList<>();
                Map<String,String> properties=new HashMap<>();
                type.add(m.get("key").toString());
                properties.put("name",m.get("value").toString());
                cn.setUuid(UUID.randomUUID().toString());
                cn.setType(type);
                cn.setProperties(properties);
                lcn.add(cn);
                ConvertedDataModel dm=new ConvertedDataModel();
                dm.setNodes(lcn);
                dataRepository.createNodes(gson.toJson(dm));
            }
            if(m.get("status").equals("delete")){                                                       //if the user chooses to delete , delete the relationship
                for (Map constraint:constraints
                     ) {
                    for (Object s:(ArrayList)constraint.get("value")
                         ) {
                        dataRepository.deleteRelation(constraint.get("key").toString(),s.toString(),m.get("key").toString(),((ArrayList)m.get("value")).get(0).toString());
                    }
                }
            }
            if(m.get("status").equals("add")){                                                      // if the user chooses to add, create the relationship
                for (Map constraint:constraints
                ) {
                    for (Object s:(ArrayList)constraint.get("value")
                    ) {
                        String sourceNode=dataRepository.getNodeId(constraint.get("key").toString(),s.toString());
                        String destNode=dataRepository.getNodeId(m.get("key").toString(),((ArrayList)m.get("value")).get(0).toString());
                        Relationship relationship=new Relationship();
                        relationship.setSourcenode(sourceNode);
                        relationship.setDestnode(destNode);
                        if(dataRepository.getRelationName(constraint.get("key").toString(),m.get("key").toString())!=null){
                            relationship.setRelation(dataRepository.getRelationName(constraint.get("key").toString(),m.get("key").toString()));
                        }
                        else {
                            relationship.setRelation("anonymous");
                        }
                        List<Relationship> lr=new ArrayList<>();
                        lr.add(relationship);
                        ConvertedDataModel dm=new ConvertedDataModel();
                        dm.setRelationship(lr);
                        dataRepository.createRelations(gson.toJson(dm));
                    }
                }
            }
        }
        return true;
    }
    public boolean dataMerger(){                //method to merge duplicate nodes
        dataRepository.mergeNodes();
        dataRepository.mergeRelations();
        return true;
    }
}

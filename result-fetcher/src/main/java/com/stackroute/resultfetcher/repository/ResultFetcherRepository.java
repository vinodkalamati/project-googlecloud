package com.stackroute.resultfetcher.repository;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResultFetcherRepository extends Neo4jRepository {
    @Query(" MATCH (n) WHERE {0} IN labels(n)"+
            " RETURN CASE count(n) WHEN 0 THEN false ELSE true END as n")
    public Boolean checkIfNode(String param);                               //returns true if required  is a node
    @Query("WITH apoc.convert.fromJsonMap({0}) AS row "+
            " WITH [n in row.constraints | n.key] AS keys,"+
            " [n in row.constraints | n.value] AS values"+
            " WITH keys,values,[(m)--() WHERE m.name IN values AND m.label[0] IN keys|m] AS temp"+
            " MATCH p=(b) WHERE ALL (c in temp WHERE (c)--(b))"+
            " RETURN extract(n IN nodes(p)|n.name)")
    public List<String> getCommonNodes(String constraint);              //returns names of common nodes of all constraints
    @Query("WITH apoc.convert.fromJsonMap({0}) AS row "+
            " WITH [n in row.constraints | n.key] AS keys,"+
            " [n in row.constraints | n.value] AS values"+
            " WITH keys,values"+
            " MATCH p=(m) WHERE m.name IN values AND m.label[0] IN keys"+
            " RETURN extract(n IN nodes(p)|n.name)")
    public List<String> getMatchedNodes(String constraint);                // returns names of matching nodes for one constraint
    @Query(" MATCH (n)--(m) WHERE {0} IN labels(n) AND m.name IN {1}"+
            " WITH m,n,'key' AS key , 'value' AS value"+
            " RETURN (apoc.map.fromPairs([[key,m.name],[value,n.name]]))")
    public List getResultNodeForMatchedNodes(String param1, List<String> constraints);
    @Query(" MATCH p=(m) WHERE {0} IN labels(m) AND m.name IN {1}"+
            " RETURN extract(n IN nodes(p)|n.name)")
    List<String> getResultNodeForCommonNodes(String a, List<String> constraints);
    @Query(" MATCH p=(m) WHERE m.name IN {1}"+
            " RETURN collect(properties(m)) ")
    public List getResultProps(String param1, List<String> constraints);        //returns required properties of a node
    @Query(" MATCH (n)--(m) WHERE {0} IN labels(n) AND m.name IN {1}"+
            " RETURN n.name")
    public List<String> getResultNodesForCommonNodes(String param1, List<String> constraints);
    @Query(" MATCH (n)--(p)--(m) WHERE {0} IN labels(n) AND m.name IN {1}"+
            " WITH m,n,p,'key' AS key , 'value' AS value,count(p) AS c"+
            " RETURN "+
            " CASE "+
            " WHEN c>=1"+
            " THEN (apoc.map.fromPairs([[key,p.name],[value,n.name]]))"+
            " ELSE null"+
            " END as r" )
    public List getResultNodesForMatchedNodes(String param1, List<String> constraints);

}

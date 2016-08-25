package com.example.cs121.slugset;

import java.util.HashMap;
import java.util.Map;

public class CardPaths {

    Map<String, String> path = new HashMap<String, String>();

    private static CardPaths instance = null;

    private CardPaths() {
        path.put("1GDD", "card1gdd");
        path.put("1GDE", "card1gde");
        path.put("1GDF", "card1gdf");
        path.put("1GOD", "card1god");
        path.put("1GOE", "card1goe");
        path.put("1GOF", "card1gof");
        path.put("1GSD", "card1gsd");
        path.put("1GSE", "card1gse");
        path.put("1GSF", "card1gsf");
        path.put("1RDD", "card1rdd");
        path.put("1RDE", "card1rde");
        path.put("1RDF", "card1rdf");
        path.put("1ROD", "card1rod");
        path.put("1ROE", "card1roe");
        path.put("1ROF", "card1rof");
        path.put("1RSD", "card1rsd");
        path.put("1RSE", "card1rse");
        path.put("1RSF", "card1rsf");
        path.put("1YDD", "card1ydd");
        path.put("1YDE", "card1yde");
        path.put("1YDF", "card1ydf");
        path.put("1YOD", "card1yod");
        path.put("1YOE", "card1yoe");
        path.put("1YOF", "card1yof");
        path.put("1YSD", "card1ysd");
        path.put("1YSE", "card1yse");
        path.put("1YSF", "card1ysf");

        path.put("2GDD", "card2gdd");
        path.put("2GDE", "card2gde");
        path.put("2GDF", "card2gdf");
        path.put("2GOD", "card2god");
        path.put("2GOE", "card2goe");
        path.put("2GOF", "card2gof");
        path.put("2GSD", "card2gsd");
        path.put("2GSE", "card2gse");
        path.put("2GSF", "card2gsf");
        path.put("2RDD", "card2rdd");
        path.put("2RDE", "card2rde");
        path.put("2RDF", "card2rdf");
        path.put("2ROD", "card2rod");
        path.put("2ROE", "card2roe");
        path.put("2ROF", "card2rof");
        path.put("2RSD", "card2rsd");
        path.put("2RSE", "card2rse");
        path.put("2RSF", "card2rsf");
        path.put("2YDD", "card2ydd");
        path.put("2YDE", "card2yde");
        path.put("2YDF", "card2ydf");
        path.put("2YOD", "card2yod");
        path.put("2YOE", "card2yoe");
        path.put("2YOF", "card2yof");
        path.put("2YSD", "card2ysd");
        path.put("2YSE", "card2yse");
        path.put("2YSF", "card2ysf");

        path.put("3GDD", "card3gdd");
        path.put("3GDE", "card3gde");
        path.put("3GDF", "card3gdf");
        path.put("3GOD", "card3god");
        path.put("3GOE", "card3goe");
        path.put("3GOF", "card3gof");
        path.put("3GSD", "card3gsd");
        path.put("3GSE", "card3gse");
        path.put("3GSF", "card3gsf");
        path.put("3RDD", "card3rdd");
        path.put("3RDE", "card3rde");
        path.put("3RDF", "card3rdf");
        path.put("3ROD", "card3rod");
        path.put("3ROE", "card3roe");
        path.put("3ROF", "card3rof");
        path.put("3RSD", "card3rsd");
        path.put("3RSE", "card3rse");
        path.put("3RSF", "card3rsf");
        path.put("3YDD", "card3ydd");
        path.put("3YDE", "card3yde");
        path.put("3YDF", "card3ydf");
        path.put("3YOD", "card3yod");
        path.put("3YOE", "card3yoe");
        path.put("3YOF", "card3yof");
        path.put("3YSD", "card3ysd");
        path.put("3YSE", "card3yse");
        path.put("3YSF", "card3ysf");
    }

    public static CardPaths paths() {
        if(instance == null){
            instance = new CardPaths();
        }
        return instance;
    }
}

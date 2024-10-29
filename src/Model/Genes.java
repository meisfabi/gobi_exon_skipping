package Model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Genes {
    private Map<String, Gene> geneMap;

    public Map<String, Gene> getFeaturesByTranscriptByGene() {
        if(geneMap == null)
            geneMap = new ConcurrentHashMap<>();

        return geneMap;
    }

}

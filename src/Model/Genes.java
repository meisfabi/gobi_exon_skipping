package Model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Genes {
    private Map<String, Gene> geneMap;

    public Map<String, Gene> getFeaturesByTranscriptByGene() {
        if(geneMap == null)
            geneMap = new HashMap<>();

        return geneMap;
    }

}

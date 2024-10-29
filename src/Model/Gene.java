package Model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class Gene {
    private String geneId;
    private String geneName;
    private String geneSource;
    private String geneBiotype;
    private String seqName;

    private Map<String, Transcript>[] transcriptMapArray;

    public Map<String, Transcript>[] getTranscriptMapArray() {
        if(transcriptMapArray == null)
            transcriptMapArray = new HashMap[2];
        return transcriptMapArray;
    }
    public String getGeneName() {
        return geneName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public String getGeneBiotype() {
        return geneBiotype;
    }

    public void setGeneBiotype(String geneBiotype) {
        this.geneBiotype = geneBiotype;
    }

    public String getGeneId() {
        return geneId;
    }

    public void setGeneId(String geneId) {
        this.geneId = geneId;
    }

    public String getGeneSource() {
        return geneSource;
    }

    public void setGeneSource(String geneSource) {
        this.geneSource = geneSource;
    }
    public String getSeqName() {
        return seqName;
    }

    public void setSeqName(String seqName) {
        this.seqName = seqName;
    }
}

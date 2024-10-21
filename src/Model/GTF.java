package Model;


import java.util.HashMap;
import java.util.Map;

public class GTF {

    // Fields
    private String seqName;
    private String source;
    private String feature;
    private long start;
    private long end;
    private double score;
    private char strand;
    private int frame;
    private HashMap<String, String> attributes;

    // Getters and Setters
    // 1. seqName
    public String getSeqName() {
        return seqName;
    }

    public void setSeqName(String newSeqName) {
        this.seqName = newSeqName;
    }

    // 2. source
    public String getSource() {
        return source;
    }

    public void setSource(String newSource) {
        this.source = newSource;
    }

    // 3. feature
    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    // 4. start
    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    // 5. end
    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    // 6. score
    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    // 7. strand
    public char getStrand() {
        return strand;
    }

    public void setStrand(char strand) {
        if (strand != '-' && strand != '+' && strand != '.') {
            throw new IllegalArgumentException("Strand must be '+', '-', or '.'");
        }
        this.strand = strand;
    }

    // 8. frame
    public int getFrame() {
        return frame;
    }

    public void setFrame(int frame) {
        this.frame = frame;
    }

    // 9. attribute
    public Map<String, String> getAttributes() {
        return attributes != null ? new HashMap<>(attributes) : null;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = (attributes != null) ? new HashMap<>(attributes) : null;
    }
}


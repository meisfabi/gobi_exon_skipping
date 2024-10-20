package Model;

public class GTF {
    private String seqName;
    public String getSeqName() {
        return seqName;
    }
    public void setSeqName(String newSeqName) {
        this.seqName = newSeqName;
    }

    private String source;
    public String getSource() {
        return source;
    }
    public void setSource(String newSource) {
        this.source = newSource;
    }

    private String feature;
    public String getFeature(){
        return feature;
    }
    public void setFeature(String feature) {
        this.feature = feature;
    }

    private long start;
    public long getStart() {
        return start;
    }
    public void setStart(long start) {
        this.start = start;
    }

    private long end;
    public void setEnd(long end) {
        this.end = end;
    }
    public long getEnd() {
        return end;
    }

    private double score;
    public void setScore(Double score) {
        this.score = score;
    }
    public double getScore() {
        return score;
    }

    private char strand;
    public char getStrand() {
        return strand;
    }
    public void setStrand(char strand) throws Exception {
        if(strand != '-' && strand != '+' && strand != '.')
            throw new Exception("Strand must be '+', '-' or '.'");
        this.strand = strand;
    }

    private int frame;
    public void setFrame(int frame) {
        this.frame = frame;
    }
    public int getFrame() {
        return frame;
    }

    private String[] attribute;
    public void setAttribute(String[] attribute) {
        this.attribute = attribute;
    }
    public String[] getAttribute() {
        return attribute;
    }
}

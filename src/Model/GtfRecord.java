package Model;

import augmentedTree.Interval;

public class GtfRecord implements Interval {

    // Fields
    private String seqName;
    private String source;
    private String feature;
    private String proteinId;
    private String transcriptId;
    private String geneId;
    private String geneSource;
    private String geneBiotype;
    private String transcriptName;
    private String transcriptSource;
    private String tag;
    private String ccdsId;
    private String geneName;
    private int start;
    private int stop;
    private double score;
    private char strand;
    private int frame;
    private Integer exonNumber;

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
    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    // 5. end
    public int getStop() {
        return stop;
    }

    public void setStop(int end) {
        this.stop = end;
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

    // 9. protein_id
    public String getProteinId() {
        return proteinId;
    }

    public void setProteinId(String proteinId) {
        this.proteinId = proteinId;
    }

    // 10. transcript_id
    public String getTranscriptId() {
        return transcriptId;
    }

    public void setTranscriptId(String transcriptId) {
        this.transcriptId = transcriptId;
    }

    // 11. gene_id
    public String getGeneId() {
        return geneId;
    }
    public void setGeneId(String geneId) {
        this.geneId = geneId;
    }

    // 12. exon_number

    public int getExonNumber() {
        return exonNumber;
    }

    public void setExonNumber(Integer exonNumber) {
        this.exonNumber = exonNumber;
    }

    // 13. gene_source

    public String getGeneSource() {
        return geneSource;
    }

    public void setGeneSource(String geneSource) {
        this.geneSource = geneSource;
    }

    // 14. ccds_id

    public String getCcdsId() {
        return ccdsId;
    }
    public void setCcdsId(String ccdsId) {
        this.ccdsId = ccdsId;
    }

    // 15. gene_biotype

    public String getGeneBiotype() {
        return geneBiotype;
    }
    public void setGeneBiotype(String geneBiotype) {
        this.geneBiotype = geneBiotype;
    }

    // 16. tag
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    // 17. transcript_name

    public String getTranscriptName() {
        return transcriptName;
    }

    public void setTranscriptName(String transcriptName) {
        this.transcriptName = transcriptName;
    }

    // 18. transcript_source
    public String getTranscriptSource() {
        return transcriptSource;
    }

    public void setTranscriptSource(String transcriptSource) {
        this.transcriptSource = transcriptSource;
    }

    // 19. gene_name

    public String getGeneName() {
        return geneName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }
}


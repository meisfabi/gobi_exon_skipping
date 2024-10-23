package Model;

import augmentedTree.Interval;

import java.util.Objects;

public class Intron implements Interval {

    protected final String geneId;
    protected final String transcriptId;
    protected final String symbol;
    protected final String chromosome;
    protected final char strand;
    protected final int start;
    protected final int stop;
    protected final String proteinId;

    public Intron(String gene_id, String transcript_id, String proteinId, String symbol, char strand, String chromosome, int start, int stop){
        this.geneId = gene_id;
        this.transcriptId = transcript_id;
        this.proteinId = proteinId;
        this.symbol = symbol;
        this.strand = strand;
        this.chromosome = chromosome;
        this.start = start;
        this.stop = stop;
    }
    @Override
    public int getStart() {
        return start;
    }

    @Override
    public int getStop() {
        return stop;
    }

    public String getGeneId() {
        return geneId;
    }

    public String getTranscriptId() {
        return transcriptId;
    }

    public char getStrand() {
        return strand;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getProteinId() {
        return proteinId;
    }



}

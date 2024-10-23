package Model;

import augmentedTree.Interval;

public class Intron implements Interval {

    private String gene_id;
    private String transcript_id;
    private String symbol;
    private String chromosome;
    private char strand;
    private int start;
    private int stop;

    public Intron(String gene_id, String transcript_id, String symbol, char strand, String chromosome, int start, int stop){
        this.gene_id = gene_id;
        this.transcript_id = transcript_id;
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

    public String getGene_id() {
        return gene_id;
    }

    public String getTranscript_id() {
        return transcript_id;
    }

    public char getStrand() {
        return strand;
    }

    public String getSymbol() {
        return symbol;
    }
}

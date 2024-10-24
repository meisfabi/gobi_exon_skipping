package Model;

import com.sun.source.tree.Tree;

import java.util.*;

public class EsSe extends Intron  {
    private Set<Intron> wildtypes = new TreeSet<>();
    private List<String> wildtypeProteins= new ArrayList<>();
    private List<String> splicingVariantProteins = new ArrayList<>();
    private int minSkippedExon;
    private int maxSkippedExon;
    private int minSkippedBases;
    private int maxSkippedBases;
    private int nProts;
    private int nTrans;

    public EsSe(Intron intron, TreeSet<Intron> wildtypes, List<String> wildtypeProteins, List<String> splicingVariantProteins, int minSkippedExon, int maxSkippedExon, int minSkippedBases, int maxSkippedBases, int nProts, int nTrans) {
        super(intron.geneId, intron.transcriptId, intron.proteinId, intron.symbol, intron.strand, intron.chromosome, intron.start, intron.stop);
        this.minSkippedBases = minSkippedBases;
        this.maxSkippedBases = maxSkippedBases;
        this.minSkippedExon = minSkippedExon;
        this.maxSkippedExon = maxSkippedExon;
        this.nProts = nProts;
        this.nTrans = nTrans;

        this.wildtypes = Set.copyOf(wildtypes);
        this.wildtypeProteins = List.copyOf(wildtypeProteins);
        this.splicingVariantProteins = List.copyOf(splicingVariantProteins);
    }


    public int getMinSkippedBases() {
        return minSkippedBases;
    }

    public int getMaxSkippedBases() {
        return maxSkippedBases;
    }

    public int getMinSkippedExon() {
        return minSkippedExon;
    }

    public int getMaxSkippedExon() {
        return maxSkippedExon;
    }

    public int getnProts() {
        return nProts;
    }

    public int getnTrans() {
        return nTrans;
    }

    public List<Intron> getWildtypes() {
        return List.copyOf(wildtypes);
    }

    public List<String> getWildtypeProteins() {
        return List.copyOf(wildtypeProteins);
    }

    public List<String> getSplicingVariantProteins() {
        return List.copyOf(splicingVariantProteins);
    }

    @Override
    public boolean equals(Object o) {
        // self check
        if (this == o)
            return true;
        // null check
        if (o == null)
            return false;
        // type check and cast
        if (getClass() != o.getClass())
            return false;
        EsSe esSe = (EsSe) o;
        // field comparison
        return start == esSe.start
                && stop ==  esSe.stop;

    }

    @Override
    public int hashCode() {
        return Objects.hash(start, stop);
    }

}

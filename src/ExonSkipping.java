import Extensions.ExecutorServiceExtensions;
import Model.EsSe;
import Model.GtfRecord;
import Model.Intron;
import augmentedTree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExonSkipping {
    private static final Logger logger = LoggerFactory.getLogger(ExonSkipping.class);
    private static final Map<String, List<Intron>> intronByTranscriptMap = Collections.synchronizedMap(new HashMap<>());

    public static void compute(Map<String, Map<String, TreeMap<Integer, GtfRecord>>[]> data, String outputPath) {
        logger.info("Starting to compute WTs and SVs");
        var executorService = Executors.newFixedThreadPool(1);
        var esSe = new HashSet<EsSe>();
        for (var geneEntry : data.entrySet()) {
            var geneId = geneEntry.getKey();
            var transcriptArray = geneEntry.getValue();
            var transcriptCdsMap = transcriptArray[Constants.CDS_INDEX];

            var allIntronsForGene = new IntervalTree<Intron>();
            var intronsWithUniqueStartAndStop = new TreeSet<Intron>();

            // get all introns
            var getIntronFutures = new ArrayList<Future<ArrayList<Intron>>>();
            if (transcriptCdsMap == null || transcriptCdsMap.isEmpty()) continue;
            for (var transcriptEntry : transcriptCdsMap.entrySet()) {
                var transcriptId = transcriptEntry.getKey();
                var intronsForTranscriptFuture = executorService.submit(() -> getIntrons(transcriptEntry, geneId, transcriptId));
                getIntronFutures.add(intronsForTranscriptFuture);
            }

            for (var future : getIntronFutures) {
                try {
                    var introns = future.get();
                    allIntronsForGene.addAll(introns);
                    intronsWithUniqueStartAndStop.addAll(introns);
                } catch (InterruptedException e) {
                    logger.error(String.format("Got interrupted while looking at introns for gene %s", geneId), e);
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    logger.error(String.format("Execution error while looking at introns for gene %s", geneId), e);
                }
            }

            int nProts = 0, nTrans = 0;
            if(transcriptArray[Constants.CDS_INDEX] != null){
                nProts = transcriptArray[Constants.CDS_INDEX].size();
            }

            if(transcriptArray[Constants.EXON_INDEX] != null){
                nTrans = transcriptArray[Constants.EXON_INDEX].size();
            }
            // compare
            if(geneId.equals("ENSG00000073464")){
                var a = "";
            }
            calculateEsSe(esSe, allIntronsForGene, intronsWithUniqueStartAndStop, nProts , nTrans);
        }
        ExecutorServiceExtensions.shutdownExecutorService(executorService);

        Writer.writeTsv(outputPath, esSe);
    }

    private static ArrayList<Intron> getIntrons(Map.Entry<String, TreeMap<Integer, GtfRecord>> transcriptEntry, String geneId, String transcriptId) {
        var cdsMap = transcriptEntry.getValue();

        GtfRecord lastCds = null;
        var introns = new ArrayList<Intron>();

        for (var cds : cdsMap.values()) {
            if (lastCds == null) {
                lastCds = cds;
                continue;
            }

            var currentIntron = new Intron(geneId, transcriptId, cds.getProteinId(), cds.getGeneName(), cds.getStrand(), cds.getSeqName(), lastCds.getStop() + 1, cds.getStart());
            introns.add(currentIntron);
            intronByTranscriptMap.putIfAbsent(transcriptId, new ArrayList<>());
            synchronized (intronByTranscriptMap.get(transcriptId)) {
                intronByTranscriptMap.get(transcriptId).addAll(introns);
            }

            lastCds = cds;
        }
        return introns;
    }

    private static void calculateEsSe(Set<EsSe> esSe, IntervalTree<Intron> intronsForGene, Set<Intron> setWithUniqueStartAndStopIntrons, int nProts, int nTrans) {

        for (var intron : setWithUniqueStartAndStopIntrons) {
            var spannedBy = new IntronByTranscriptMap();
            intronsForGene.getIntervalsSpannedBy(intron.getStart(), intron.getStop(), spannedBy);
            var wtProts = new ArrayList<String>();
            var svProts = new ArrayList<String>();
            if (spannedBy.entrySet().size() <= 1) continue;
            var copyOfSpannedByEntrySet = Set.copyOf(spannedBy.entrySet());
            var hasSizeGreater2 = false;
            var hasSize1 = false;
            Intron sv = null;
            for (var spannedByEntry : copyOfSpannedByEntrySet) {
                var intronList = spannedByEntry.getValue();
                var intronListSize = intronList.size();
                if (intronListSize > 1) {
                    boolean hasStart = false, hasStop = false;
                    var copyOfIntronList = List.copyOf(intronList);
                    for (var intronToBeChecked : copyOfIntronList) {
                        if (intronToBeChecked.getStart() == intron.getStart()) {
                            hasStart = true;
                        }

                        if (intronToBeChecked.getStop() == intron.getStop()) {
                            hasStop = true;
                        }
                    }

                    if (!hasStart || !hasStop) {
                        spannedBy.entrySet().remove(spannedByEntry);
                        continue;
                    }

                    hasSizeGreater2 = true;

                } else if(intronList.size() == 1){
                    var onlyIntron = intronList.getFirst();

                    if(intron.getStart() != onlyIntron.getStart() || intron.getStop() != onlyIntron.getStop()){
                        spannedBy.entrySet().remove(spannedByEntry);
                        continue;
                    }
                    hasSize1 = true;
                    sv = onlyIntron;
                }
            }

            // gibt einträge die mehr start und endpunkt als verschiedene introns haben
            // und es gibt mehr als ein transkript
            if (!hasSizeGreater2 || !hasSize1 || spannedBy.size() <= 1) continue;

            var svLength = sv.getStop() - sv.getStart();

            int maxSkippedExon = 0, minSkippedExon = Integer.MAX_VALUE, maxSkippedBases = 0, minSkippedBases = Integer.MAX_VALUE;
            var wildtypes = new TreeSet<Intron>();
            for (var intronWithSV : spannedBy.entrySet()) {
                var intronList = intronWithSV.getValue();

                int skippedExons = intronList.size() - 1;

                if (skippedExons > 0 && skippedExons < minSkippedExon)
                    minSkippedExon = skippedExons;

                if (skippedExons > maxSkippedExon)
                    maxSkippedExon = skippedExons;

                if(intronList.size() > 1){
                    wildtypes.addAll(intronList);
                }

                var isFirstIntron = true;
                var intronsLength = 0;
                for (var currentIntron : intronList) {
                    if (isFirstIntron) {
                        if(intronList.size() > 1){
                            wtProts.add(currentIntron.getProteinId());
                        } else{
                            svProts.add((currentIntron.getProteinId()));
                        }
                        isFirstIntron = false;
                    }

                    if(intronList.size() > 1){
                        intronsLength += currentIntron.getStop() - currentIntron.getStart();
                    }
                }

                if(intronsLength == 0) continue;

                var skippedBases = svLength - intronsLength;

                if(maxSkippedBases < skippedBases){
                    maxSkippedBases = skippedBases;
                }

                if(minSkippedBases > skippedBases){
                    minSkippedBases = skippedBases;
                }
            }

            esSe.add(new EsSe(sv, wildtypes, wtProts, svProts, minSkippedExon, maxSkippedExon, minSkippedBases, maxSkippedBases, nProts, nTrans));
        }

    }
}

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
    public static void compute(Map<String, Map<String, TreeMap<Integer, GtfRecord>>[]> data, String outputPath){
        logger.info("Starting to compute WTs and SVs");
        var executorService = Executors.newFixedThreadPool(1);
        for(var geneEntry : data.entrySet()){
            var geneId = geneEntry.getKey();
            var transcriptArray = geneEntry.getValue();
            var transcriptCdsMap = transcriptArray[Constants.CDS_INDEX];

            var intronsForGene = new IntervalTree<Intron>();

            // get all introns
            var getIntronFutures = new ArrayList<Future<ArrayList<Intron>>>();
            if(transcriptCdsMap == null || transcriptCdsMap.isEmpty()) continue;
            for(var transcriptEntry : transcriptCdsMap.entrySet()){
                var transcriptId = transcriptEntry.getKey();
                var intronsForTranscriptFuture = executorService.submit(() -> getIntrons(transcriptEntry, geneId, transcriptId));
                getIntronFutures.add(intronsForTranscriptFuture);
            }

            for (var future : getIntronFutures) {
                try {
                    intronsForGene.addAll(future.get());
                } catch (InterruptedException e) {
                    logger.error(String.format("Got interrupted while looking at introns for gene %s", geneId), e);
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    logger.error(String.format("Execution error while looking at introns for gene %s", geneId), e);
                }
            }

            // compare
            var esSe = Collections.synchronizedSet(new HashSet<EsSe>());
            calculateEsSe(esSe, intronsForGene);
            var a = "";
        }
        ExecutorServiceExtensions.shutdownExecutorService(executorService);
    }

    private static ArrayList<Intron> getIntrons(Map.Entry<String, TreeMap<Integer, GtfRecord>> transcriptEntry, String geneId, String transcriptId) {
        var cdsMap = transcriptEntry.getValue();

        GtfRecord lastCds = null;
        var introns = new ArrayList<Intron>();

        for(var cds: cdsMap.values()){
            if(lastCds == null){
                lastCds = cds;
                continue;
            }
            var currentIntron = new Intron(geneId, transcriptId, cds.getProteinId(), cds.getGeneName(), cds.getStrand(), cds.getSeqName(), lastCds.getStop() + 1, cds.getStart());
            introns.add(currentIntron);
            intronByTranscriptMap.putIfAbsent(transcriptId, new ArrayList<>());
            synchronized (intronByTranscriptMap.get(transcriptId)){
                intronByTranscriptMap.get(transcriptId).addAll(introns);
            }

            lastCds = cds;
        }
        return introns;
    }

    private static void calculateEsSe(Set<EsSe> esSe, IntervalTree<Intron> intronsForGene){
        for(var intronEntry : intronByTranscriptMap.entrySet()){
            for(var intron : intronEntry.getValue()){

                var spannedBy = new IntronByTranscriptMap();
                intronsForGene.getIntervalsSpannedBy(intron.getStart(), intron.getStop(), spannedBy);

                if(spannedBy.entrySet().size() <= 1)  continue;
                var copyOfSpannedByEntrySet = Set.copyOf(spannedBy.entrySet());
                var hasSizeGreater2 = false;
                for(var spannedByEntry : copyOfSpannedByEntrySet){
                    var intronList = spannedByEntry.getValue();
                    if(intronList.size() > 1){
                        hasSizeGreater2 = true;
                        boolean hasStart = false, hasStop = false;
                        var copyOfIntronList = List.copyOf(intronList);
                        for(var intronToBeChecked : copyOfIntronList){
                            if(intronToBeChecked.getStart() == intron.getStart()){
                                hasStart = true;
                            }

                            if(intronToBeChecked.getStop() == intron.getStop()){
                                hasStop = true;
                            }
                        }

                        if(!hasStart || !hasStop){
                            spannedBy.entrySet().remove(spannedByEntry);
                        }
                    }
                }

                if(!hasSizeGreater2) continue;

                if(spannedBy.size() <= 1) continue;


                if(intron.getStart() == 152646438 && intron.getStop() == 152647448){
                    var a = "";
                }
                for(var intronWithSV : spannedBy.entrySet()) {
                    var list = intronWithSV.getValue();
                    esSe.add(new EsSe(list.getFirst()));
                    break;
                }

            }
        }
    }
}

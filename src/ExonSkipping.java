import Extensions.ExecutorServiceExtensions;
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
    public static void compute(Map<String, Map<String, TreeMap<Integer, GtfRecord>>[]> data, String outputPath){
        logger.info("Starting to compute WTs and SVs");
        var executorService = Executors.newFixedThreadPool(10);
        for(var geneEntry : data.entrySet()){
            var geneId = geneEntry.getKey();
            logger.info(String.format("Looking at Gene %s", geneId));
            var transcriptArray = geneEntry.getValue();
            var transcriptCdsMap = transcriptArray[Constants.CDS_INDEX];

            var intronsForTranscript = Collections.synchronizedList(new ArrayList<IntervalTree<Intron>>());

            // get all introns
            var futures = new ArrayList<Future<IntervalTree<Intron>>>();

            for(var transcriptEntry : transcriptCdsMap.entrySet()){
                var transcriptId = transcriptEntry.getKey();
                var intronsFuture = executorService.submit(() -> getIntrons(transcriptEntry, geneId, transcriptId));
                futures.add(intronsFuture);
            }

            for (var future : futures) {
                try {
                    intronsForTranscript.add(future.get());
                } catch (InterruptedException e) {
                    logger.error(String.format("Got interrupted while looking at introns for gene %s", geneId), e);
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    logger.error(String.format("Execution error while looking at introns for gene %s", geneId), e);
                }
            }
        }
        ExecutorServiceExtensions.shutdownExecutorService(executorService);
    }

    private static IntervalTree<Intron> getIntrons(Map.Entry<String, TreeMap<Integer, GtfRecord>> transcriptEntry, String geneId, String transcriptId) {
        logger.info(String.format("Starting to look at transcript %s", transcriptId));
        var cdsMap = transcriptEntry.getValue();

        GtfRecord lastCds = null;
        var introns = new IntervalTree<Intron>();

        for(var cds: cdsMap.values()){
            if(lastCds == null){
                lastCds = cds;
                continue;
            }

            var currentIntron = new Intron(geneId, transcriptId, cds.getGeneName(), cds.getStrand(), cds.getSeqName(), lastCds.getStop() + 1, cds.getStart());
            introns.add(currentIntron);
            lastCds = cds;
        }
        logger.info(String.format("Finished to look at transcript %s", transcriptId));
        return introns;
    }
}

import Model.GTF;

import java.util.*;

public class ExonSkipping {

    public static void compute(Map<String, TreeMap<Long, GTF>>[] data, String outputPath){
        var cdsMap = data[Constants.CDS_INDEX]; // nprots = cdsMap.size()
        var exonsMap = data[Constants.EXON_INDEX]; // ntrans = exonsMap.size()

        for (var transcriptId : cdsMap.keySet()) {
            var currentExons = exonsMap.getOrDefault(transcriptId, null);
            if (currentExons == null) continue; // skip, if there are no exons for this transcript
            var currentCds = cdsMap.get(transcriptId);

            for (var cdsEntry : currentCds.entrySet()) {
                var cdsStart = cdsEntry.getKey();
                var cdsGtf = cdsEntry.getValue();
                var cdsEnd = cdsGtf.getEnd();

                for(var currentExonEntry : currentExons.entrySet()){
                    var currentExon = currentExonEntry.getValue();
                    var currentExonStart = currentExonEntry.getKey();
                    var currentExonEnd = currentExon.getEnd();
                    if(cdsStart.equals(currentExonStart) && currentExonEnd == cdsEnd) {
                        // WT
                        System.out.printf("%s:%s%n", cdsStart, cdsEnd);
                        System.out.println();
                    } else if((currentExonEnd >= cdsStart && currentExonEnd <= cdsEnd)){
                        // SV found
                        System.out.printf("%s:%s%n", currentExonStart, cdsStart - 1);
                        System.out.println();
                    } else if((currentExonStart <= cdsEnd && currentExonStart >= cdsStart)){
                        // SV found
                        System.out.printf("%s:%s%n", cdsEnd + 1, currentExonEnd);
                        System.out.println();
                    }
                }
            }
        }

    }
}

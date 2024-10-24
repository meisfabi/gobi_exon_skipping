import Extensions.ExecutorServiceExtensions;
import Model.EsSe;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Writer {
   public static void writeTsv(String outputPath, Collection<EsSe> esSes){
       try(BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath + "ESSE.tsv", false))){
           StringBuilder outputLine = new StringBuilder("id\tsymbol\tchr\tstrand\tnprots\ttrans\tSV\tWT\tWT_prots\tSV_prots\tmin_skipped_exon\tmax_skipped_exon\tmin_skipped_bases\tmax_skipped_bases\n");
           for(var esSe : esSes){
               outputLine.append(esSe.getGeneId()).append("\t").append(esSe.getSymbol()).append("\t").append(esSe.getChromosome()).append("\t").append(esSe.getStrand()).append("\t").append(esSe.getnProts()).append("\t").append(esSe.getnTrans()).append("\t");
               outputLine.append(String.format("%s:%s\t", esSe.getStart(), esSe.getStop()));

               var isFirst = true;
               for(var wildtype : esSe.getWildtypes()){
                    if(isFirst){
                        outputLine.append(String.format("%s:%s", wildtype.getStart(), wildtype.getStop()));
                        isFirst = false;
                    } else{
                        outputLine.append(String.format("|%s:%s", wildtype.getStart(), wildtype.getStop()));
                    }
               }
               outputLine.append("\t");

               isFirst = true;
               for(var wildtypeProt : esSe.getWildtypeProteins()){
                   if(isFirst){
                       outputLine.append(String.format("%s", wildtypeProt));
                       isFirst = false;
                   } else{
                       outputLine.append(String.format("|%s", wildtypeProt));
                   }
               }
               outputLine.append("\t");

               isFirst = true;
               for(var svProt : esSe.getSplicingVariantProteins()){
                   if(isFirst){
                       outputLine.append(String.format("%s", svProt));
                       isFirst = false;
                   } else{
                       outputLine.append(String.format("|%s", svProt));
                   }
               }
               outputLine.append("\t");
               outputLine.append(String.format("%s\t%s\t%s\t%s\n", esSe.getMinSkippedExon(), esSe.getMaxSkippedExon(), esSe.getMinSkippedBases(), esSe.getMaxSkippedBases()));
           }
           writer.write(outputLine.toString());
       } catch (Exception e){
            e.printStackTrace();
       }
   }


}


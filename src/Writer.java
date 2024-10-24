import Model.EsSe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Collection;

public class Writer {
    private static final Logger logger = LoggerFactory.getLogger(Writer.class);
   public static void writeTsv(String outputPath, Collection<EsSe> esSes){
       try(BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath, false))){
                writer.write("id\tsymbol\tchr\tstrand\tnprots\tntrans\tSV\tWT\tSV_prots\tWT_prots\tmin_skipped_exon\tmax_skipped_exon\tmin_skipped_bases\tmax_skipped_bases\n");
           for(var esSe : esSes){
               writer.write(String.format("%s\t", esSe.getGeneId()));
               writer.write(String.format("%s\t", esSe.getSymbol()));
               writer.write(String.format("%s\t", esSe.getChromosome()));
               writer.write(String.format("%s\t", esSe.getStrand()));
               writer.write(String.format("%s\t", esSe.getnProts()));
               writer.write(String.format("%s\t", esSe.getnProts()));

               writer.write(String.format("%s:%s\t", esSe.getStart(), esSe.getStop()));

               var isFirst = true;
               for(var wildtype : esSe.getWildtypes()){
                    if(isFirst){
                        writer.write(String.format("%s:%s", wildtype.getStart(), wildtype.getStop()));
                        isFirst = false;
                    } else{
                        writer.write(String.format("|%s:%s", wildtype.getStart(), wildtype.getStop()));
                    }
               }
               writer.write("\t");

               isFirst = true;
               for(var svProt : esSe.getSplicingVariantProteins()){
                   if(isFirst){
                       writer.write(String.format("%s", svProt));
                       isFirst = false;
                   } else{
                       writer.write(String.format("|%s", svProt));
                   }
               }
               writer.write("\t");

               isFirst = true;
               for(var wildtypeProt : esSe.getWildtypeProteins()){
                   if(isFirst){
                       writer.write(String.format("%s", wildtypeProt));
                       isFirst = false;
                   } else{
                        writer.write(String.format("|%s", wildtypeProt));
                   }
               }
               writer.write("\t");

               writer.write(String.format("%s\t%s\t%s\t%s\n", esSe.getMinSkippedExon(), esSe.getMaxSkippedExon(), esSe.getMinSkippedBases(), esSe.getMaxSkippedBases()));
           }
       } catch (Exception e){
            logger.error("Error while trying to write to file", e);
       }
   }


}


import Extensions.ExecutorServiceExtensions;
import Model.GtfRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.Executors;

public class GtfParser {
    private static final Logger logger = LoggerFactory.getLogger(GtfParser.class);
    private static final int bufferSize = 8192;
    private static int errorLines;

    // Map<Gene_Id, Map<Transcript_Id, TreeMap<StartPosition, GtfRecord>>[cds or exon]>
    private static Map<String, Map<String, TreeMap<Integer, GtfRecord>>[]> parsedGTF;
    public static Map<String, Map<String, TreeMap<Integer, GtfRecord>>[]> parse(String inputPath){
        errorLines = 0;
        logger.info("Starting to parse gtf file");
        parsedGTF = Collections.synchronizedMap(new HashMap<>());
        Path path = Path.of(inputPath);
        var executorService = Executors.newFixedThreadPool(10);
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {

            var fileSize = channel.size();
            long position = 0;
            var leftover = "";

            while (position < fileSize){
                long remaining = fileSize - position;
                var bytesToRead = (int) Math.min(bufferSize, remaining);

                var buffer = channel.map(FileChannel.MapMode.READ_ONLY, position, bytesToRead);

                var bytes = new byte[bytesToRead];
                buffer.get(bytes);

                var chunk = new String(bytes, StandardCharsets.UTF_8);
                chunk = leftover + chunk;
                var lines = chunk.split("\n");

                for (int i = 0; i < lines.length - 1; i++) {
                    String line = lines[i].trim();
                    if(line.startsWith("#"))
                        continue;
                    executorService.submit(() -> processLine(line));
                }

                leftover = lines[lines.length - 1];

                position += bytesToRead;
            }

            final String leftoverFinal = leftover.trim();
            executorService.submit(() -> processLine(leftoverFinal));
        }
        catch (Exception e){
            logger.error("Error while parsing gtf file", e);
        }

        ExecutorServiceExtensions.shutdownExecutorService(executorService);

        logger.info("GTF-File parsed");
        if(errorLines > 0)
            logger.info(String.format("%s could not be saved due to an error while parsing", errorLines));
        return parsedGTF;
    }

    // TODO remove split and do it manually
    private static void processLine(String line){
        var splitLine = line.split("\t");
        if(!splitLine[2].equals("exon") && !splitLine[2].equals("CDS"))
            return;

        var gtf = new GtfRecord();
        try{
            gtf.setSeqName(splitLine[0]);
            gtf.setSource(splitLine[1]);
            gtf.setFeature(splitLine[2]);
            gtf.setStart(Integer.parseInt(splitLine[3]));
            gtf.setStop(Integer.parseInt(splitLine[4]));

            if(!splitLine[5].equals(".")){
                gtf.setScore(Double.parseDouble(splitLine[5]));
            } else{
                gtf.setScore(-1.0);
            }

            gtf.setStrand(splitLine[6].charAt(0));

            if(!splitLine[7].equals(".")){
                gtf.setFrame(Integer.parseInt(splitLine[7]));
            } else{
                gtf.setFrame(-1);
            }

            var attributes = splitLine[8].split(";");
            var geneId = "";
            var transcriptId = "";
            for(var attribute : attributes){
                var splitAttribute = attribute.trim().split(" ");
                if(splitAttribute.length != 2) continue;
                var key = splitAttribute[0];
                var value = splitAttribute[1].replace("\"", "");

                switch (key){
                    case "gene_id":
                        geneId = value;
                        gtf.setGeneId(value);
                        break;
                    case "transcript_id":
                        transcriptId = value;
                        gtf.setTranscriptId(value);
                        break;
                    case "exon_number":
                        gtf.setExonNumber(Integer.parseInt(value));
                        break;
                    case "gene_source":
                        gtf.setGeneSource(value);
                        break;
                    case "gene_biotype":
                        gtf.setGeneBiotype(value);
                        break;
                    case "transcript_name":
                        gtf.setTranscriptName(value);
                        break;
                    case "transcript_source":
                        gtf.setTranscriptSource(value);
                        break;
                    case "tag":
                        gtf.setTag(value);
                        break;
                    case "ccds_id":
                        gtf.setCcdsId(value);
                        break;
                    case "protein_id":
                        gtf.setProteinId(value);
                        break;
                    case "gene_name":
                        gtf.setGeneName(value);
                        break;
                }
            }

            parsedGTF.putIfAbsent(geneId, new HashMap[2]);
            int index = Constants.CDS_INDEX;
            if(gtf.getFeature().equals("exon")){
                index = Constants.EXON_INDEX;
            }
            if(geneId.isEmpty() || transcriptId.isEmpty()){
                logger.warn("Could not add GtfRecord because geneId or transcriptId was empty");
                return;
            }

            synchronized (parsedGTF.get(geneId)) {
                if (parsedGTF.get(geneId)[index] == null) {
                    parsedGTF.get(geneId)[index] = new HashMap<>();
                }
                parsedGTF.get(geneId)[index].putIfAbsent(transcriptId, new TreeMap<>());
                parsedGTF.get(geneId)[index].get(gtf.getTranscriptId()).put(gtf.getStart(), gtf);
            }

        } catch (Exception e){
            logger.error("Error while trying to parse line", e);
            errorLines++;
        }
    }
}

import Model.GTF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GTFParser {
    private static final Logger logger = LoggerFactory.getLogger(GTFParser.class);
    private static final int bufferSize = 8192;
    private static int errorLines;
    private static final Map<String, TreeMap<Long, GTF>>[] parsedGTF = new Map[2];
    public static Map<String, TreeMap<Long, GTF>>[] parse(String inputPath){
        errorLines = 0;
        logger.info("Starting to parse gtf file");
        parsedGTF[0] = Collections.synchronizedMap(new HashMap<>());
        parsedGTF[1] = Collections.synchronizedMap(new HashMap<>());
        Path path = Path.of(inputPath);
        var executorService = Executors.newFixedThreadPool(10);
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {

            var fileSize = channel.size();
            long position = 0;
            var leftover = "";

            while (position < fileSize){
                long remaining = fileSize - position;
                var bytesToRead = (int) Math.min(bufferSize, remaining);

                MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, position, bytesToRead);

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

        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

        logger.info("GTF-File parsed");
        if(errorLines > 0)
            logger.info(String.format("%s could not be saved due to an error while parsing", errorLines));
        return parsedGTF;
    }

    private static void processLine(String line){
        var splitLine = line.split("\t");
        if(!splitLine[2].equals("exon") && !splitLine[2].equals("CDS"))
            return;

        var gtf = new GTF();
        try{
            gtf.setSeqName(splitLine[0]);
            gtf.setSource(splitLine[1]);
            gtf.setFeature(splitLine[2]);
            gtf.setStart(Integer.parseInt(splitLine[3]));
            gtf.setEnd(Integer.parseInt(splitLine[4]));

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
            var attributeMap = new HashMap<String, String>();
            for(var attribute : attributes){
                var splitAttribute = attribute.trim().split(" ");
                var key = splitAttribute[0];
                var value = splitAttribute[1].replace("\"", "");
                attributeMap.put(key, value);
                if(splitAttribute[0].equals("transcript_id")){
                    int index = Constants.CDS_INDEX;
                    if(gtf.getFeature().equals("exon")){
                        index = Constants.EXON_INDEX;
                    }
                    parsedGTF[index].putIfAbsent(value, new TreeMap<>());
                    var treeMap = parsedGTF[index].get(value);
                    synchronized (treeMap) {
                        treeMap.putIfAbsent(gtf.getStart(), gtf);
                    }
                }
            }
            gtf.setAttributes(attributeMap);
        } catch (Exception e){
            logger.error("Error while trying to parse line", e);
            errorLines++;
        }
    }
}

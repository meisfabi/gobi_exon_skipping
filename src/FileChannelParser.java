import Extensions.ExecutorServiceExtensions;
import Model.GtfRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;

public class FileChannelParser {
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
                    if(line.contains("69577210")){
                        var a = "";
                    }
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
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

    private static void processLine(String line){

    }
}

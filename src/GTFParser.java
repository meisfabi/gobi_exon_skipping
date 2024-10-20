import Model.GTF;

import java.io.ByteArrayOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class GTFParser {
    private static final int bufferSize = 8192;
    public static List<GTF> parse(String inputPath){
        List<GTF> parsedGTF = Collections.synchronizedList(new ArrayList<>());
        Path path = Path.of(inputPath);
        var executorService = Executors.newFixedThreadPool(4);
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

                    executorService.submit(() -> processLine(line, parsedGTF));
                }

                leftover = lines[lines.length - 1];

                position += bytesToRead;
            }

            final String leftoverFinal = leftover.trim();
            executorService.submit(() -> processLine(leftoverFinal, parsedGTF));

        }
        catch (Exception e){
            e.printStackTrace();
        }

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        return parsedGTF;
    }

    private static void processLine(String line, List<GTF> gtfList){
        var splitLine = line.split("\t");
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

            gtf.setAttribute(splitLine[8].split(";"));
        } catch (Exception e){

        }

        gtfList.add(gtf);
    }
}

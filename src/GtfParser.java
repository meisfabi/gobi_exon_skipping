import Model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

public class GtfParser {
    private static final Logger logger = LoggerFactory.getLogger(GtfParser.class);

    private static int errorLines;

    // Map<Gene_Id, Map<Transcript_Id, TreeMap<StartPosition, FeatureRecord>>[cds or exon]>
    private static Genes parsedGTF;

    public static Genes parse(String inputPath) {
        errorLines = 0;
        logger.info("Starting to parse gtf file");
        parsedGTF = new Genes();
        Path path = Path.of(inputPath);

        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            lines.parallel()
                    .filter(line -> !line.trim().isEmpty() && !line.startsWith("#"))
                    .forEach(line -> processLine(line.trim()));
        } catch (Exception e) {
            logger.error("Error while parsing gtf file", e);
        }

        logger.info("GTF-File parsed");
        if (errorLines > 0)
            logger.warn(String.format("%s could not be saved due to an error while parsing", errorLines));
        return parsedGTF;
    }

    private static void processLine(String line) {
        final var stringBuilder = new StringBuilder();
        var splitLine = new String[9];
        var currentIdx = 0;
        var currentStart = 0;
        for (int i = 0; i < line.length(); i++) {
            var currentChar = line.charAt(i);
            if (currentChar == '\t') {
                splitLine[currentIdx++] = stringBuilder.toString();
                stringBuilder.setLength(0);
                if (currentIdx == 3) {
                    currentStart = i + 1;
                    break;
                }
            } else {
                stringBuilder.append(currentChar);
            }
        }

        if (!splitLine[2].equals("exon") && !splitLine[2].equals("CDS"))
            return;

        for (int i = currentStart; i < line.length(); i++) {
            var currentChar = line.charAt(i);
            if (currentChar == '\t') {
                splitLine[currentIdx++] = stringBuilder.toString();
                stringBuilder.setLength(0);
            } else {
                stringBuilder.append(currentChar);
            }
        }

        splitLine[currentIdx] = stringBuilder.toString();
        stringBuilder.setLength(0);

        var featureRecord = new FeatureRecord();
        try {
            featureRecord.setStart(Integer.parseInt(splitLine[3]));
            featureRecord.setStop(Integer.parseInt(splitLine[4]));

            if (!splitLine[5].equals(".")) {
                featureRecord.setScore(Double.parseDouble(splitLine[5]));
            } else {
                featureRecord.setScore(-1.0);
            }

            featureRecord.setStrand(splitLine[6].charAt(0));

            if (!splitLine[7].equals(".")) {
                featureRecord.setFrame(Integer.parseInt(splitLine[7]));
            } else {
                featureRecord.setFrame(-1);
            }

            var gene = new Gene();
            var transcript = new Transcript();
            var attributes = new ArrayList<String>();
            for (int i = 0; i < splitLine[8].length(); i++) {
                var currentChar = splitLine[8].charAt(i);
                if (currentChar == ';') {
                    attributes.add(stringBuilder.toString());
                    stringBuilder.setLength(0);
                } else {
                    stringBuilder.append(currentChar);
                }
            }
            stringBuilder.setLength(0);

            String key = null;

            for (var attribute : attributes) {
                stringBuilder.setLength(0);
                for (int i = 0; i < attribute.length(); i++) {
                    var currentChar = attribute.charAt(i);

                    if (currentChar == '\"') continue;
                    if (currentChar == ' ') {
                        key = stringBuilder.toString();
                        stringBuilder.setLength(0);
                    } else {
                        stringBuilder.append(currentChar);
                    }
                }

                var value = stringBuilder.toString();

                if (key == null || key.isBlank()) continue;

                switch (key) {
                    case Constants.GENE_ID:
                        gene.setGeneId(value);
                        break;
                    case Constants.TRANSCRIPT_ID:
                        transcript.setTranscriptId(value);
                        break;
                    case Constants.EXON_NUMBER:
                        featureRecord.setExonNumber(value);
                        break;
                    case Constants.GENE_SOURCE:
                        gene.setGeneSource(value);
                        break;
                    case Constants.GENE_BIOTYPE:
                        gene.setGeneBiotype(value);
                        break;
                    case Constants.TRANSCRIPT_NAME:
                        transcript.setTranscriptName(value);
                        break;
                    case Constants.TRANSCRIPT_SOURCE:
                        transcript.setTranscriptSource(value);
                        break;
                    case Constants.TAG:
                        featureRecord.setTag(value);
                        break;
                    case Constants.CCDS_ID:
                        featureRecord.setCcdsId(value);
                        break;
                    case Constants.PROTEIN_ID:
                        featureRecord.setProteinId(value);
                        break;
                    case Constants.GENE_NAME:
                        gene.setGeneName(value);
                        break;
                }
            }
            var geneId = gene.getGeneId();
            Gene currentGene;


            currentGene = parsedGTF.getFeaturesByTranscriptByGene().get(geneId);

            if (currentGene == null) {
                currentGene = new Gene();
                currentGene.setGeneBiotype(gene.getGeneBiotype());
                currentGene.setGeneName(gene.getGeneName());
                currentGene.setGeneId(geneId);
                currentGene.setGeneSource(gene.getGeneSource());
                currentGene.setSeqName(splitLine[0]);
                parsedGTF.getFeaturesByTranscriptByGene().put(geneId, currentGene);

            }

            int index = Constants.CDS_INDEX;
            if (splitLine[2].equals("exon")) {
                index = Constants.EXON_INDEX;
            }
            var transcriptId = transcript.getTranscriptId();
            if (geneId.isEmpty() || transcriptId.isEmpty()) {
                logger.warn("Could not add GtfRecord because geneId or transcriptId was empty");
                return;
            }

            var transcriptMap = currentGene.getTranscriptMapArray();
            if (transcriptMap[index] == null) {
                transcriptMap[index] = new HashMap<>();
            }
            transcriptMap[index].putIfAbsent(transcriptId, transcript);
            transcriptMap[index].get(transcript.getTranscriptId()).getTranscriptEntry().addRecord(featureRecord.getStart(), featureRecord);

        } catch (Exception e) {
            logger.error("Error while trying to parse line", e);
            errorLines++;
        }
    }
}

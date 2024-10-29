import Model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class GtfParser {
    private static final Logger logger = LoggerFactory.getLogger(GtfParser.class);
    private static int errorLines;

    // Map<Gene_Id, Map<Transcript_Id, TreeMap<StartPosition, GtfRecord>>[cds or exon]>
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
        var splitLine = new ArrayList<String>();

        var splitLineBuilder = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            var currentChar = line.charAt(i);
            if (currentChar == '\t') {
                splitLine.add(splitLineBuilder.toString());
                splitLineBuilder = new StringBuilder();
            } else {
                splitLineBuilder.append(currentChar);
            }
        }

        splitLine.add(splitLineBuilder.toString());

        if (!splitLine.get(2).equals("exon") && !splitLine.get(2).equals("CDS"))
            return;

        var featureRecord = new FeatureRecord();
        try {
            featureRecord.setStart(Integer.parseInt(splitLine.get(3)));
            featureRecord.setStop(Integer.parseInt(splitLine.get(4)));

            if (!splitLine.get(5).equals(".")) {
                featureRecord.setScore(Double.parseDouble(splitLine.get(5)));
            } else {
                featureRecord.setScore(-1.0);
            }

            featureRecord.setStrand(splitLine.get(6).charAt(0));

            if (!splitLine.get(7).equals(".")) {
                featureRecord.setFrame(Integer.parseInt(splitLine.get(7)));
            } else {
                featureRecord.setFrame(-1);
            }

            var gene = new Gene();
            var transcript = new Transcript();
            StringBuilder attributeBuilder = new StringBuilder();
            var attributes = new ArrayList<String>();
            for (int i = 0; i < splitLine.get(8).length(); i++) {
                var currentChar = splitLine.get(8).charAt(i);
                if (currentChar == ';') {
                    attributes.add(attributeBuilder.toString());
                    attributeBuilder = new StringBuilder();
                } else {
                    attributeBuilder.append(currentChar);
                }
            }


            String key = null;

            for (var attribute : attributes) {
                attributeBuilder = new StringBuilder();
                for (int i = 0; i < attribute.length(); i++) {
                    var currentChar = attribute.charAt(i);

                    if (currentChar == '\"') continue;
                    if (currentChar == ' ') {
                        key = attributeBuilder.toString();
                        attributeBuilder = new StringBuilder();
                    } else {
                        attributeBuilder.append(currentChar);
                    }
                }

                var value = attributeBuilder.toString();

                if (key == null || key.isBlank()) continue;

                switch (key) {
                    case "gene_id":
                        gene.setGeneId(value);
                        break;
                    case "transcript_id":
                        transcript.setTranscriptId(value);
                        break;
                    case "exon_number":
                        featureRecord.setExonNumber(value);
                        break;
                    case "gene_source":
                        gene.setGeneSource(value);
                        break;
                    case "gene_biotype":
                        gene.setGeneBiotype(value);
                        break;
                    case "transcript_name":
                        transcript.setTranscriptName(value);
                        break;
                    case "transcript_source":
                        transcript.setTranscriptSource(value);
                        break;
                    case "tag":
                        featureRecord.setTag(value);
                        break;
                    case "ccds_id":
                        featureRecord.setCcdsId(value);
                        break;
                    case "protein_id":
                        featureRecord.setProteinId(value);
                        break;
                    case "gene_name":
                        gene.setGeneName(value);
                        break;
                }
            }
            var geneId = gene.getGeneId();
            Gene currentGene;

            synchronized (parsedGTF.getFeaturesByTranscriptByGene()) {
                currentGene = parsedGTF.getFeaturesByTranscriptByGene().get(geneId);
            }

            if (currentGene == null) {
                currentGene = new Gene();
                currentGene.setGeneBiotype(gene.getGeneBiotype());
                currentGene.setGeneName(gene.getGeneName());
                currentGene.setGeneId(geneId);
                currentGene.setGeneSource(gene.getGeneSource());
                currentGene.setSeqName(splitLine.getFirst());
                synchronized (parsedGTF.getFeaturesByTranscriptByGene()) {
                    parsedGTF.getFeaturesByTranscriptByGene().put(geneId, currentGene);
                }
            }

            int index = Constants.CDS_INDEX;
            if (splitLine.get(2).equals("exon")) {
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
            transcriptMap[index].putIfAbsent(transcriptId, new Transcript());
            transcriptMap[index].get(transcript.getTranscriptId()).getTranscriptEntry().addRecord(featureRecord.getStart(), featureRecord);

        } catch (Exception e) {
            logger.error("Error while trying to parse line", e);
            errorLines++;
        }
    }
}

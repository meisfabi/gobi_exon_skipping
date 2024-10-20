import Model.GTF;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("Main").build().defaultHelp(true).description("Program that extracts all ES-SE defined only by coding sequences (CDS) listed in the input GTF file.");

        parser.addArgument("-gtf").required(true).help("GTF File Path").metavar("<GTF file>").type(String.class);
        parser.addArgument("-o").required(true).help("Output Path").metavar("<output file path>").type(String.class);
        try {
            Namespace res = parser.parseArgs(args);

            long start = System.currentTimeMillis();
            var data = GTFParser.parse(res.get("gtf"));
            logger.info(String.format("Time needed for parsing: %s seconds", (System.currentTimeMillis() - start) / 1000.0));

            logger.info("Starting computation") ;
        } catch(ArgumentParserException e){
            parser.printHelp();
        }
        catch (Exception e) {
            logger.error("Error while executing main", e);
        }
    }
}
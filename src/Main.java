import Model.GTF;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

public class Main {
    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("Main").build().defaultHelp(true).description("A Description");

        parser.addArgument("-gtf").required(true).help("GTF File Path").metavar("<GTF file>").type(String.class);
        parser.addArgument("-o").required(true).help("Output Path").metavar("<output file path>").type(String.class);

        try {
            Namespace res = parser.parseArgs(args);
            var data = GTFParser.parse(res.get("gtf"));
            System.out.println(data.size());
        } catch (Exception e) {

        }
    }
}
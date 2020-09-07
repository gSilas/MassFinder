package de.ovgu.mpa;

import static java.nio.file.StandardCopyOption.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.*;

public class App {
    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();

        Options options = new Options();

        Option mass = new Option("m", "mass", true, "numerical value of mass");
        mass.setRequired(true);
        options.addOption(mass);

        Option fDatabase = new Option("fdb", "fasta_database", true, "path to fasta file");
        fDatabase.setRequired(false);
        options.addOption(fDatabase);

        Option pDatabase = new Option("pdb", "peptide_database", true, "path to pep file");
        pDatabase.setRequired(false);
        options.addOption(pDatabase);

        Option tolerance = new Option("to", "tolerance", true, "tolerance");
        tolerance.setRequired(true);
        options.addOption(tolerance);

        Option timer = new Option("t", "timer", false, "time execution");
        timer.setRequired(false);
        options.addOption(timer);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("Fasta Peptide mass searcher", options);
            System.exit(1);
        }

        assert(cmd.hasOption("fasta_database") ^ cmd.hasOption("peptide_database"));

        Path tlink = null;

        if (cmd.hasOption("fasta_database")){
            String fileName = cmd.getOptionValue("fasta_database").split("/")[cmd.getOptionValue("fasta_database").split("/").length
            - 1].split("\\.")[0];
            File fastaFolder = new File("fasta");
            if (!fastaFolder.exists())
                fastaFolder.mkdir();
            File batchDir = new File("tmp_batches");
            if (!batchDir.exists())
                batchDir.mkdir();
            File databaseFolder = Paths.get(fastaFolder.getPath(), fileName).toFile();
            if (!databaseFolder.exists())
                databaseFolder.mkdir();
            try {
                tlink = Paths.get(".", "fasta", "last_database", "NonRedundant.pep");
                tlink.toFile().delete();
                Files.createSymbolicLink(tlink.toAbsolutePath(), databaseFolder.toPath().toAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            processFasta(databaseFolder.toString(), cmd.getOptionValue("fasta_database"), batchDir.toString(), fastaFolder);
        } else if (cmd.hasOption("peptide_database")) {
            tlink = Paths.get(cmd.getOptionValue("peptide_database"));
        }

        if (cmd.hasOption("mass") && tlink != null) {
            MassSearcher searcher = new MassSearcher(tlink.toString(),
                    Double.parseDouble(cmd.getOptionValue("tolerance")),
                    Double.parseDouble(cmd.getOptionValue("mass")));
            try {
                searcher.searchPeptides();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (cmd.hasOption("timer")) {
            System.out.println("execution time: " + ((double) System.currentTimeMillis() - (double) startTime) / 1000.0
                    + " seconds");
        }
    }

    public static void processFasta(String targetFolder, String fastaPath, String batchFolder, File fastaFolder) {
        // Copy Fasta
        Path source = Paths.get(fastaPath);
        Path target = Paths.get(targetFolder, fastaPath.split("/")[fastaPath.split("/").length - 1]);
        try {
            Files.copy(source, target, REPLACE_EXISTING);
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        // process fasta
        PeptideWriter writer = new PeptideWriter();
        try {
            writer.createFiles(targetFolder, batchFolder, target.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

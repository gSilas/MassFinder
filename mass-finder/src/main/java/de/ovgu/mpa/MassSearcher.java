package de.ovgu.mpa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MassSearcher {

    private final String targetPeptides;
    private final double tolerance;
    private final double targetMass;

    public MassSearcher(String targetPeptides, double tolerance, double targetMass) {
        this.targetPeptides = targetPeptides;
        this.tolerance = tolerance;
        this.targetMass = targetMass;
    }

    public void searchPeptides() throws IOException {
        final BufferedReader brT = new BufferedReader(new FileReader(new File(targetPeptides)));

        final BufferedWriter writer = new BufferedWriter(new FileWriter("result.csv", false));
        writer.write("sequence;mass;line\n");
        
        String line = brT.readLine();
        long lineCount = 1;
        long matchCount = 0;

        while (line != null) {
            String[] lineSplit = line.split(";");
            String lineSequence = lineSplit[0];
            Double lineMass = Double.valueOf(lineSplit[1]);

            if (targetMass <= lineMass + tolerance && targetMass >= lineMass - tolerance) {
                writer.write(lineSequence + ";" + lineMass.toString() + ";" + lineCount +"\n");
                matchCount++;
            }
            line = brT.readLine();
            lineCount++;

        }

        writer.close();

        System.out.println("Recognized " + matchCount + " matches!");
    }
}

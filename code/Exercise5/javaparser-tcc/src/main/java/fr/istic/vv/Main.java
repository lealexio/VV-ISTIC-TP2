package fr.istic.vv;

import com.github.javaparser.utils.SourceRoot;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {

        // Input file
        if(args.length == 0) {
            System.err.println("Should provide the path to the source code and the csv file in which to save the result");
            System.exit(1);
        }

        File inputFile = new File(args[0]);
        if(!inputFile.exists() || !inputFile.isDirectory() || !inputFile.canRead()) {
            System.err.println("Provide a path to an existing readable directory");
            System.exit(2);
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("Type 'yes' to save graphs");
        String allowGraphs = scanner.next();

        SourceRoot root = new SourceRoot(inputFile.toPath());


        String strDate = new SimpleDateFormat("dd-MM-yy_hh'h'mm'm'ss's'").format(Calendar.getInstance().getTime());


        File outputCsvDir = new File(System.getProperty("user.dir")+"/code/Exercise5/output/"+strDate);
        File outputGraphDir = new File(outputCsvDir, "/graphs");

        outputGraphDir.mkdirs();

        // Output csv
        File outputCsv = new File(outputCsvDir + "/output.csv");

        // Output Graph
        ComputeTCC printer = new ComputeTCC();
        // Create csv
        printer.createCsv(outputCsv);

        // Execute parsing
        root.parse("", (localPath, absolutePath, result) -> {
            result.ifSuccessful(unit -> unit.accept(printer, null));
            printer.toCsv(outputCsv);
            if(allowGraphs.equals("yes")){
                printer.toGraph(new File(outputGraphDir, printer.packageName+printer.className+".png"));
            }
            return SourceRoot.Callback.Result.DONT_SAVE;
        });
    }


}

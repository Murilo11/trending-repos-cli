package dev.murilo.trending;

import picocli.CommandLine;


public class Main {
    public static void main(String[] args) {


        CommandLine commandLine = new CommandLine(new TrendingCommand());

        commandLine.setCaseInsensitiveEnumValuesAllowed(true);

        int exitCode = commandLine.execute(args);
        System.exit(exitCode);


    }
}

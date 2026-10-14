package dev.murilo.trending;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "trending-repos",version = "1.0.0",description = "Lista os repositórios em alta do GitHub por período",mixinStandardHelpOptions = true)
public class TrendingCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-d", "--duration"},defaultValue = "WEEK", description = " Lista por Duração : ${COMPLETION-CANDIDATES}")
    private TimeRange duration;

    @CommandLine.Option(names = {"-l", "--limit"}, defaultValue = "10", description = "O LIMITE é : ${DEFAULT-VALUE}")
    private int limit;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        if (limit < 1 || limit > 100){
            throw  new CommandLine.ParameterException(spec.commandLine(),"O limite deve estar entre 1 a 100");

        }
        System.out.println("Duração: " + duration + ", Limite: " + limit);
        return 0;
    }
}

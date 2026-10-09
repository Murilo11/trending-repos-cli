package dev.murilo.trending;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "trending-repos",version = "1.0.0",description = "Lista os repositórios em alta do GitHub por período",mixinStandardHelpOptions = true)
public class TrendingCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-d", "--duration"},defaultValue = "WEEK", description = "Lista por Duração : ${COMPLETION-CANDIDATES}")
    private Duration duration;

    @CommandLine.Option(names = {"-l", "--limit"}, defaultValue = "10", description = "O LIMITE é : ${DEFAULT-VALUE}")
    private int limit;

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}

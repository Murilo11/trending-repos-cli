package dev.murilo.trending;

import picocli.CommandLine;

import java.time.LocalDate;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "trending-repos",version = "1.0.0",description = "Lista os repositórios em alta do GitHub por período",mixinStandardHelpOptions = true)
public class TrendingCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-d", "--duration"},defaultValue = "WEEK", description = "Período a considerar: ${COMPLETION-CANDIDATES} (padrão: ${DEFAULT-VALUE})")
    private TimeRange duration;

    @CommandLine.Option(names = {"-l", "--limit"}, defaultValue = "10", description = "Quantidade de repositórios a exibir (1 a 100, padrão: ${DEFAULT-VALUE})")
    private int limit;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        if (limit < 1 || limit > 100){
            throw  new CommandLine.ParameterException(spec.commandLine(),"O limite deve estar entre 1 e 100");

        }

        LocalDate cutOFF = duration.cutoffDate(LocalDate.now());

        GithubClient githubClient = new GithubClient();
        String json = githubClient.searchTrending(cutOFF, limit);
        System.out.println(json);

        System.out.println("Duração: " + duration + ", Limite: " + limit + ", A partir de: " + cutOFF);
        return 0;
    }
}

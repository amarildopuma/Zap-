import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

public class MainGrafica {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InterfaceGrafica ui = new InterfaceGrafica();
            Thread thread = new Thread(() -> executar(ui), "partida");
            thread.setDaemon(true);
            thread.start();
        });
    }

    private static void executar(InterfaceGrafica ui) {
        ConfiguracaoPartida configuracao = ui.escolherConfiguracao(null);
        if (configuracao == null) {
            System.exit(0);
        }

        Sequencia sequencia = new Sequencia();
        while (true) {
            sequencia.preparar(configuracao);
            ui.definirSequencia(sequencia.resumoCurto());

            Partida partida = criarPartida(configuracao, ui);
            Time campeao = partida.jogar();
            boolean venceu = campeao == partida.getTime1();

            sequencia.registrar(venceu);
            ui.definirSequencia(sequencia.resumoCurto());

            configuracao = decidirProxima(ui, configuracao, venceu, partida.placar(), sequencia);
        }
    }

    private static ConfiguracaoPartida decidirProxima(InterfaceGrafica ui, ConfiguracaoPartida atual,
                                                      boolean venceu, String placar, Sequencia sequencia) {
        while (true) {
            AcaoFinal acao = ui.fimDePartida(venceu, placar, sequencia.linhaSequencia(), sequencia.linhaGeral());
            switch (acao) {
                case JOGAR_NOVAMENTE:
                    return atual;
                case MUDAR_FORMA:
                    ConfiguracaoPartida nova = ui.escolherConfiguracao(atual);
                    if (nova != null) {
                        return nova;
                    }
                    break;
                default:
                    System.exit(0);
            }
        }
    }

    private static Partida criarPartida(ConfiguracaoPartida configuracao, InterfaceJogo ui) {
        Variante variante = configuracao.modo().criarVariante();
        int jogadoresPorTime = variante.getJogadoresPorTime();

        List<Jogador> jogadoresTime1 = new ArrayList<>();
        List<Jogador> jogadoresTime2 = new ArrayList<>();

        jogadoresTime1.add(new JogadorHumano(configuracao.nome(), ui));
        for (int i = 1; i < jogadoresPorTime; i++) {
            if (configuracao.forma() == FormaDeJogar.PARCEIRO_HUMANO) {
                jogadoresTime1.add(new JogadorHumano("Jogador " + (i + 1), ui));
            } else {
                jogadoresTime1.add(new JogadorComputador("Parceiro " + i, variante));
            }
        }
        for (int i = 1; i <= jogadoresPorTime; i++) {
            jogadoresTime2.add(new JogadorComputador("Adversário " + i, variante));
        }

        Time time1 = new Time("Seu time", jogadoresTime1);
        Time time2 = new Time("Time adversário", jogadoresTime2);

        List<Jogador> mesa = new ArrayList<>();
        for (int i = 0; i < jogadoresPorTime; i++) {
            mesa.add(jogadoresTime1.get(i));
            mesa.add(jogadoresTime2.get(i));
        }

        return new Partida(variante, mesa, time1, time2, ui);
    }
}
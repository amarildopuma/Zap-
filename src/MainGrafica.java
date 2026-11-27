import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

public class MainGrafica {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainGrafica::iniciar);
    }

    private static void iniciar() {
        ModoJogo[] modos = ModoJogo.values();
        String[] opcoes = new String[modos.length];
        for (int i = 0; i < modos.length; i++) {
            opcoes[i] = modos[i].getDescricao();
        }

        int escolha = JOptionPane.showOptionDialog(null, "Escolha o modo de jogo:", "Zap! - Truco",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[0]);
        if (escolha < 0) {
            return;
        }

        String nome = JOptionPane.showInputDialog(null, "Seu nome:", "Você");
        if (nome == null) {
            return;
        }
        nome = nome.trim();
        if (nome.isEmpty()) {
            nome = "Você";
        }

        InterfaceGrafica ui = new InterfaceGrafica();
        Partida partida = criarPartida(modos[escolha], nome, ui);

        // A partida bloqueia esperando cliques, então não pode rodar na thread da janela.
        Thread thread = new Thread(partida::jogar, "partida");
        thread.setDaemon(true);
        thread.start();
    }

    // Mesma montagem do Menu.criarPartida, só que recebendo a interface por parâmetro.
    private static Partida criarPartida(ModoJogo modo, String nome, InterfaceJogo ui) {
        Variante variante = modo.criarVariante();
        int jogadoresPorTime = variante.getJogadoresPorTime();

        List<Jogador> jogadoresTime1 = new ArrayList<>();
        List<Jogador> jogadoresTime2 = new ArrayList<>();

        jogadoresTime1.add(new JogadorHumano(nome, ui));
        for (int i = 1; i < jogadoresPorTime; i++) {
            jogadoresTime1.add(new JogadorComputador("Parceiro " + i, variante));
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

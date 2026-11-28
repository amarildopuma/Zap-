import java.util.ArrayList;
import java.util.List;

public class Partida {
    private static final int PONTOS_PARA_VENCER = 12;

    private final Variante variante;
    private final List<Jogador> mesa;
    private final Time time1;
    private final Time time2;
    private final InterfaceJogo ui;
    private int indiceMao;

    public Partida(Variante variante, List<Jogador> mesa, Time time1, Time time2, InterfaceJogo ui) {
        this.variante = variante;
        this.mesa = new ArrayList<>(mesa);
        this.time1 = time1;
        this.time2 = time2;
        this.ui = ui;
        this.indiceMao = 0;
    }

    public Time jogar() {
        ui.iniciarPartida(mesa, time1, time2);
        int numeroRodada = 1;

        while (!terminou()) {
            ui.mostrarMensagem("");
            ui.mostrarMensagem("=== Rodada " + numeroRodada + " ===");

            Rodada rodada = new Rodada(variante, mesa, time1, time2, ui, indiceMao);
            rodada.jogar();

            Time vencedorRodada = rodada.getVencedor();
            vencedorRodada.adicionarPontos(rodada.getPontosGanhos());

            ui.mostrarMensagem(vencedorRodada.getNome() + " ganhou a rodada e fez " + rodada.getPontosGanhos() + " pontos.");
            ui.mostrarMensagem("Placar: " + placar());

            indiceMao = (indiceMao + 1) % mesa.size();
            numeroRodada++;
        }

        Time campeao = getVencedor();
        ui.mostrarMensagem("");
        ui.mostrarMensagem(campeao.getNome() + " venceu a partida!");
        return campeao;
    }

    public Time getTime1() {
        return time1;
    }

    public boolean terminou() {
        return getVencedor() != null;
    }

    public Time getVencedor() {
        if (time1.getPontos() >= PONTOS_PARA_VENCER) {
            return time1;
        }
        if (time2.getPontos() >= PONTOS_PARA_VENCER) {
            return time2;
        }
        return null;
    }

    public String placar() {
        return time1.getNome() + " " + time1.getPontos() + " x " + time2.getPontos() + " " + time2.getNome();
    }
}

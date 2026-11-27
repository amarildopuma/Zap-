import java.util.List;

public interface InterfaceJogo {
    Carta lerCarta(Jogador jogador);
    Resposta lerRespostaAposta(Jogador jogador, NivelAposta nivelAtual);
    boolean pedirTruco(Jogador jogador, NivelAposta proposto);
    boolean perguntarMaoDeDez(Jogador jogador, List<Jogador> time);
    void definirMaoCega(boolean cega);
    void mostrarMensagem(String mensagem);

    // Eventos opcionais: o terminal ignora, a interface gráfica usa para redesenhar a mesa.
    default void iniciarPartida(List<Jogador> mesa, Time time1, Time time2) {}
    default void iniciarRodada() {}
    default void vezDe(Jogador jogador) {}
    default void cartaJogada(Jogador jogador, Carta carta) {}
    default void fimDaVaza() {}
}
import java.util.List;

public interface InterfaceJogo {
    Carta lerCarta(Jogador jogador);
    Resposta lerRespostaAposta(Jogador jogador, NivelAposta nivelAtual);
    boolean pedirTruco(Jogador jogador, NivelAposta proposto);
    boolean perguntarMaoDeDez(Jogador jogador, List<Jogador> time);
    void definirMaoCega(boolean cega);
    void mostrarMensagem(String mensagem);
}
public interface InterfaceJogo {
    Carta lerCarta(Jogador jogador);
    Resposta lerRespostaAposta(Jogador jogador, NivelAposta nivelAtual);
    boolean pedirTruco(Jogador jogador, NivelAposta proposto);
    void mostrarMensagem(String mensagem);
}
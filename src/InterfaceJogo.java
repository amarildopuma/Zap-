public interface InterfaceJogo {
    Carta lerCarta(Jogador jogador);
    Resposta lerResposta(Jogador jogador, NivelAposta nivelAtual);
    boolean lerPedidoAumento(Jogador jogador, NivelAposta proposto);
    void mostrarMensagem(String mensagem);
}
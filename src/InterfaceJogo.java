public interface InterfaceJogo {
    Carta lerCarta(Jogador jogador);
    Resposta lerResposta(Jogador jogador, NivelAposta nivelAtual);
}
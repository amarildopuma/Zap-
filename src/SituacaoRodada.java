import java.util.List;

public record SituacaoRodada(
        Variante variante,
        List<Jogador> mesa,
        int meuAssento,
        int[] equipe,
        Carta[] naMesa,
        int lider,
        int maoInicial,
        List<Integer> resultados,
        List<Carta> jogadasAnteriores,
        NivelAposta nivel,
        int[] pontos) {

    public int minhaEquipe() {
        return equipe[meuAssento];
    }

    public int pontosMeus() {
        return pontos[minhaEquipe()];
    }

    public int pontosAdversarios() {
        return pontos[1 - minhaEquipe()];
    }
}

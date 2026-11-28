public class Sequencia {
    private ConfiguracaoPartida configuracao;
    private int sequenciaAtual;
    private int melhorSequencia;
    private int vitorias;
    private int derrotas;

    public void preparar(ConfiguracaoPartida nova) {
        if (!nova.mesmaForma(configuracao)) {
            configuracao = nova;
            sequenciaAtual = 0;
            melhorSequencia = 0;
            vitorias = 0;
            derrotas = 0;
        }
    }

    public void registrar(boolean venceu) {
        if (venceu) {
            vitorias++;
            sequenciaAtual++;
            melhorSequencia = Math.max(melhorSequencia, sequenciaAtual);
        } else {
            derrotas++;
            sequenciaAtual = 0;
        }
    }

    public String resumoCurto() {
        return "★ Sequência: " + sequenciaAtual + " " + plural(sequenciaAtual, "vitória", "vitórias")
                + "  •  Geral: " + vitorias + " x " + derrotas;
    }

    public String linhaSequencia() {
        return "Sequência atual: " + sequenciaAtual + " " + plural(sequenciaAtual, "vitória seguida", "vitórias seguidas")
                + "  (melhor: " + melhorSequencia + ")";
    }

    public String linhaGeral() {
        return "Nesta forma de jogar: " + vitorias + " " + plural(vitorias, "vitória", "vitórias")
                + " x " + derrotas + " " + plural(derrotas, "derrota", "derrotas");
    }

    private String plural(int quantidade, String singular, String plural) {
        return quantidade == 1 ? singular : plural;
    }
}

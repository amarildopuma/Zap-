import java.util.List;

public class JogadorComputador extends Jogador {
    private static final double LIMITE_ACEITAR = 4.5;
    private static final double LIMITE_AUMENTAR = 7.5;

    private Variante variante;

    public JogadorComputador(String nome, Variante variante) {
        super(nome);
        this.variante = variante;
    }

    @Override
    public Carta escolherCarta(Variante variante) {
        List<Carta> mao = getMao();
        if (mao.isEmpty()) {
            throw new IllegalStateException("O jogador " + getNome() + " não tem cartas na mão.");
        }

        Carta melhorCarta = mao.get(0);
        int maiorForca = variante.getForca(melhorCarta);

        for (Carta carta : mao) {
            int forcaAtual = variante.getForca(carta);
            if (forcaAtual > maiorForca) {
                maiorForca = forcaAtual;
                melhorCarta = carta;
            }
        }
        return melhorCarta;
    }

    @Override
    public Resposta responderAposta(NivelAposta nivelAtual) {
        if (getMao().isEmpty()) {
            return Resposta.CORRER;
        }

        double mediaForca = calcularMediaForca();

        if (mediaForca >= LIMITE_AUMENTAR && nivelAtual.temProximo()) {
            return Resposta.AUMENTAR;
        } else if (mediaForca >= LIMITE_ACEITAR) {
            return Resposta.ACEITAR;
        } else {
            return Resposta.CORRER;
        }
    }

    @Override
    public boolean pedirAumento(NivelAposta proposto) {
        if (getMao().isEmpty()) {
            return false;
        }
        return calcularMediaForca() >= LIMITE_AUMENTAR;
    }

    private double calcularMediaForca() {
        List<Carta> mao = getMao();
        int somaForcas = 0;
        for (Carta carta : mao) {
            somaForcas += this.variante.getForca(carta);
        }
        return (double) somaForcas / mao.size();
    }
}
import java.util.List;

private static final double LIMITE_ACEITAR = 4.5;
private static final double LIMITE_AUMENTAR = 7.5;

public class JogadorComputador extends Jogador {
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
        List<Carta> mao = getMao();
        if (mao.isEmpty()) {
            return Resposta.CORRER;
        }

        int somaForcas = 0;
        for (Carta carta : mao) {
            somaForcas += this.variante.getForca(carta);
        }
        double mediaForca = (double) somaForcas / mao.size();


        if (mediaForca >= 10 && nivelAtual != NivelAposta.DOZE) {
            return Resposta.AUMENTAR;
        } else if (mediaForca >= 6) {
            return Resposta.ACEITAR;
        } else {
            return Resposta.CORRER;
        }
    }
}
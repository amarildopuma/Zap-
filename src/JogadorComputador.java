import java.util.List;
import java.util.Random;

public class JogadorComputador extends Jogador {
    private static final double LIMITE_ACEITAR = 4.5;
    private static final double LIMITE_AUMENTAR = 7.5;
    private final Random random = new Random();

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
            return Resposta.ACEITAR;
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
    public boolean pedirAumento(NivelAposta proximoNivel) {
        int fortes = 0; 
        for (Carta carta : getMao()) {
            if (variante.getForca(carta) >= 9) {
                fortes++;
            }
        }
        double chance = fortes >= 2 ? 0.60 : (fortes == 1 ? 0.15 : 0.03);
        if (proximoNivel.ordinal() > NivelAposta.TRUCO.ordinal()) {
            chance /= 2;
        }
        return random.nextDouble() < chance;
    }
}
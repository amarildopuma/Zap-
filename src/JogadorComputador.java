import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class JogadorComputador extends Jogador {
    private static final int SIMULACOES_JOGADA = 600;
    private static final int SIMULACOES_RESPOSTA = 800;
    private static final int SIMULACOES_MAO_DE_DEZ = 1500;
    private static final int PONTOS_PARA_VENCER = 12;
    private static final double[] LIMITE_PEDIR = {0, 0.62, 0.70, 0.78, 0.84};
    private static final double[] LIMITE_AUMENTAR = {0, 0.72, 0.78, 0.84, 0.90};

    private final Random random = new Random();
    private final Variante variante;
    private SituacaoRodada situacao;

    public JogadorComputador(String nome, Variante variante) {
        super(nome);
        this.variante = variante;
    }

    @Override
    public void observar(SituacaoRodada situacao) {
        this.situacao = situacao;
    }

    @Override
    public Carta escolherCarta(Variante variante) {
        List<Carta> mao = getMao();
        if (mao.isEmpty()) {
            throw new IllegalStateException("O jogador " + getNome() + " não tem cartas na mão.");
        }
        if (situacao == null) {
            return maisForte(mao, this.variante);
        }
        return Estrategia.escolherCarta(situacao, mao);
    }

    @Override
    public Resposta lerRespostaAposta(NivelAposta proposto) {
        if (situacao == null) {
            return Resposta.ACEITAR;
        }

        double chance = Estrategia.chanceDeVencer(situacao, getMao(), new HashMap<>(), SIMULACOES_RESPOSTA);
        int anterior = situacao.nivel().getPontos();
        int pontosEmJogo = proposto.getPontos();
        int meus = situacao.pontosMeus();
        int deles = situacao.pontosAdversarios();

        double limiteAceitar = 0.5 - anterior / (2.0 * pontosEmJogo) + 0.18;
        if (deles + anterior >= PONTOS_PARA_VENCER) {
            limiteAceitar = 0;
        } else if (deles + pontosEmJogo >= PONTOS_PARA_VENCER) {
            limiteAceitar += 0.12;
        }
        if (meus + pontosEmJogo >= PONTOS_PARA_VENCER) {
            limiteAceitar -= 0.05;
        }
        limiteAceitar = Math.max(limiteAceitar, deles + anterior >= PONTOS_PARA_VENCER ? 0 : 0.12);

        if (chance < limiteAceitar) {
            return Resposta.CORRER;
        }
        if (proposto.temProximo() && deveAumentar(proposto, chance, meus)) {
            return Resposta.AUMENTAR;
        }
        return Resposta.ACEITAR;
    }

    @Override
    public boolean pedirTruco(NivelAposta proximoNivel) {
        if (situacao == null) {
            return false;
        }
        int anterior = situacao.nivel().getPontos();
        int pontosEmJogo = proximoNivel.getPontos();
        int meus = situacao.pontosMeus();
        int deles = situacao.pontosAdversarios();

        if (meus + anterior >= PONTOS_PARA_VENCER) {
            return false;
        }

        double chance = Estrategia.chanceDeVencer(situacao, getMao(), new HashMap<>(), SIMULACOES_JOGADA);
        double limite = LIMITE_PEDIR[proximoNivel.ordinal()];
        if (deles + anterior >= PONTOS_PARA_VENCER) {
            limite -= 0.12;
        } else if (deles + pontosEmJogo >= PONTOS_PARA_VENCER) {
            limite += 0.06;
        }
        if (meus + pontosEmJogo >= PONTOS_PARA_VENCER) {
            limite -= 0.04;
        }

        if (chance >= limite) {
            return true;
        }
        return proximoNivel == NivelAposta.TRUCO && chance >= 0.38 && random.nextDouble() < 0.05;
    }

    @Override
    public boolean irMaoDeDez(List<Jogador> time) {
        if (situacao == null) {
            return false;
        }
        Map<Integer, List<Carta>> conhecidas = new HashMap<>();
        for (Jogador colega : time) {
            if (colega != this) {
                conhecidas.put(situacao.mesa().indexOf(colega), colega.getMao());
            }
        }

        int deles = situacao.pontosAdversarios();
        if (deles + 2 >= PONTOS_PARA_VENCER) {
            return true;
        }
        double chance = Estrategia.chanceDeVencer(situacao, getMao(), conhecidas, SIMULACOES_MAO_DE_DEZ);
        double limite = deles + 4 >= PONTOS_PARA_VENCER ? 0.50 : 0.40;
        return chance >= limite;
    }

    private boolean deveAumentar(NivelAposta proposto, double chance, int meus) {
        NivelAposta proximo = proposto.proximo();
        double limite = LIMITE_AUMENTAR[proximo.ordinal()];
        if (meus + proximo.getPontos() >= PONTOS_PARA_VENCER) {
            limite -= 0.05;
        }
        if (chance >= limite) {
            return true;
        }
        return proposto == NivelAposta.TRUCO && chance >= 0.40 && chance <= 0.60 && random.nextDouble() < 0.04;
    }

    private Carta maisForte(List<Carta> mao, Variante regras) {
        Carta melhor = mao.get(0);
        for (Carta carta : mao) {
            if (regras.getForca(carta) > regras.getForca(melhor)) {
                melhor = carta;
            }
        }
        return melhor;
    }
}

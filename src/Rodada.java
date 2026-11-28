import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Rodada {
    private static final int CARTAS_POR_JOGADOR = 3;
    private static final int PONTOS_MAO_DE_DEZ = 10;
    private static final int PONTOS_FUGA_MAO_DE_DEZ = 2;

    private final Variante variante;
    private final List<Jogador> mesa;
    private final Time time1;
    private final Time time2;
    private final InterfaceJogo ui;
    private final int indiceMao;
    private final List<Time> resultados = new ArrayList<>();
    private final List<Carta> jogadasAnteriores = new ArrayList<>();

    private Vaza vazaAtual = new Vaza();
    private int lider;
    private NivelAposta nivelAtual;
    private Time ultimoAumento;
    private Time vencedor;
    private boolean semAposta;
    private int pontosGanhos;

    public Rodada(Variante variante, List<Jogador> mesa, Time time1, Time time2, InterfaceJogo ui, int indiceMao) {
        this.variante = variante;
        this.mesa = new ArrayList<>(mesa);
        this.time1 = time1;
        this.time2 = time2;
        this.ui = ui;
        this.indiceMao = indiceMao;
        this.lider = indiceMao;
        this.nivelAtual = NivelAposta.SEM_APOSTA;
    }

    public void jogar() {
        distribuirCartas();
        ui.iniciarRodada();

        if (verificarMaoDeDez()) {
            return;
        }

        Time decisao = null;

        while (decisao == null) {
            vazaAtual = new Vaza();

            for (int i = 0; i < mesa.size(); i++) {
                Jogador jogador = mesa.get((lider + i) % mesa.size());
                ui.vezDe(jogador);
                if (pedirTruco(jogador)) {
                    return;
                }
                jogarCartaDe(jogador, vazaAtual);
            }

            Jogador ganhador = vazaAtual.getVencedor(variante, time1);
            if (ganhador == null) {
                resultados.add(null);
                ui.mostrarMensagem("A rodada empatou.");
            } else {
                resultados.add(timeDe(ganhador));
                lider = mesa.indexOf(ganhador);
                ui.mostrarMensagem(ganhador.getNome() + " ganhou a rodada.");
            }
            jogadasAnteriores.addAll(vazaAtual.getJogadas().values());
            ui.fimDaVaza();
            decisao = decidirVencedor(resultados);
        }

        ui.definirMaoCega(false);
        vencedor = decisao;
        pontosGanhos = nivelAtual.getPontos();
    }

    public Time getVencedor() {
        return vencedor;
    }

    public int getPontosGanhos() {
        return pontosGanhos;
    }

    private void distribuirCartas() {
        Baralho baralho = new Baralho(variante);
        baralho.embaralhar();
        for (Jogador jogador : mesa) {
            jogador.receberCartas(baralho.distribuir(CARTAS_POR_JOGADOR));
        }
    }

    private void jogarCartaDe(Jogador jogador, Vaza vaza) {
        while (true) {
            jogador.observar(situacaoPara(jogador));
            Carta carta = jogador.escolherCarta(variante);
            try {
                if (!jogador.temCarta(carta)) {
                    throw new JogadaInvalidaException("A carta escolhida não está na mão do jogador.");
                }
                vaza.adicionar(jogador, carta);
                jogador.jogarCarta(carta);
                ui.mostrarMensagem(jogador.getNome() + " jogou " + carta);
                ui.cartaJogada(jogador, carta);
                return;
            } catch (JogadaInvalidaException e) {
                ui.mostrarMensagem(e.getMessage());
            }
        }
    }

    private boolean verificarMaoDeDez() {
        boolean time1Dez = time1.getPontos() == PONTOS_MAO_DE_DEZ;
        boolean time2Dez = time2.getPontos() == PONTOS_MAO_DE_DEZ;

        if (!time1Dez && !time2Dez) {
            return false;
        }

        nivelAtual = NivelAposta.TRUCO;
        semAposta = true;

        if (time1Dez && time2Dez) {
            ui.mostrarMensagem("Mão de ferro! Os dois times têm 10 pontos: rodada valendo 4, sem ver as cartas e sem truco.");
            ui.definirMaoCega(true);
            return false;
        }

        Time timeDez = time1Dez ? time1 : time2;
        Time outro = time1Dez ? time2 : time1;
        List<Jogador> jogadores = timeDez.getJogadores();

        Jogador decisor = jogadores.getFirst();
        for (Jogador j : jogadores) {
            if (j.ehHumano()) {
                decisor = j;
                break;
            }
        }

        decisor.observar(situacaoPara(decisor));
        if (decisor.irMaoDeDez(jogadores)) {
            ui.mostrarMensagem(timeDez.getNome() + " foi à mão de dez! A rodada vale 4 pontos.");
            return false;
        }

        ui.mostrarMensagem(timeDez.getNome() + " correu da mão de dez.");
        vencedor = outro;
        pontosGanhos = PONTOS_FUGA_MAO_DE_DEZ;
        return true;
    }

    private boolean pedirTruco(Jogador jogador) {
        Time time = timeDe(jogador);
        if (semAposta) {
            return false;
        }
        if (!nivelAtual.temProximo() || time == ultimoAumento) {
            return false;
        }
        jogador.observar(situacaoPara(jogador));
        if (!jogador.pedirTruco(nivelAtual.proximo())) {
            return false;
        }
        return resolverAposta(jogador);
    }

    private boolean resolverAposta(Jogador pedinte) {
        Jogador quemPede = pedinte;

        while (true) {
            NivelAposta proposto = nivelAtual.proximo();
            Jogador adversario = primeiroAdversario(quemPede);
            ui.mostrarMensagem(quemPede.getNome() + " pediu " + proposto + "!");

            Resposta resposta = responder(adversario, proposto);

            if (resposta == Resposta.CORRER) {
                ui.mostrarMensagem(adversario.getNome() + " correu.");
                vencedor = timeDe(quemPede);
                pontosGanhos = nivelAtual.getPontos();
                return true;
            }

            nivelAtual = proposto;
            ultimoAumento = timeDe(quemPede);

            if (resposta == Resposta.ACEITAR) {
                ui.mostrarMensagem(adversario.getNome() + " aceitou. A rodada vale " + nivelAtual.getPontos() + " pontos.");
                return false;
            }
            quemPede = adversario;
        }
    }

    private Resposta responder(Jogador jogador, NivelAposta proposto) {
        while (true) {
            jogador.observar(situacaoPara(jogador));
            Resposta resposta = jogador.lerRespostaAposta(proposto);
            try {
                validarResposta(resposta, proposto);
                return resposta;
            } catch (ApostaInvalidaException e) {
                ui.mostrarMensagem(e.getMessage());
            }
        }
    }

    private void validarResposta(Resposta resposta, NivelAposta proposto) throws ApostaInvalidaException {
        if (resposta == Resposta.AUMENTAR && !proposto.temProximo()) {
            throw new ApostaInvalidaException("Não é possível aumentar depois do " + proposto + ".");
        }
    }

    private Jogador primeiroAdversario(Jogador jogador) {
        int inicio = mesa.indexOf(jogador);
        Jogador primeiro = null;
        for (int i = 1; i < mesa.size(); i++) {
            Jogador candidato = mesa.get((inicio + i) % mesa.size());
            if (timeDe(candidato) != timeDe(jogador)) {
                if (candidato.ehHumano()) {
                    return candidato;
                }
                if (primeiro == null) {
                    primeiro = candidato;
                }
            }
        }
        if (primeiro == null) {
            throw new IllegalStateException("Não há adversários para " + jogador.getNome() + ".");
        }
        return primeiro;
    }

    private SituacaoRodada situacaoPara(Jogador jogador) {
        int assentos = mesa.size();
        int[] equipe = new int[assentos];
        Carta[] naMesa = new Carta[assentos];
        Map<Jogador, Carta> jogadas = vazaAtual.getJogadas();
        for (int i = 0; i < assentos; i++) {
            Jogador j = mesa.get(i);
            equipe[i] = time1.temJogador(j) ? 0 : 1;
            naMesa[i] = jogadas.get(j);
        }
        List<Integer> placarDasVazas = new ArrayList<>();
        for (Time time : resultados) {
            placarDasVazas.add(time == null ? -1 : (time == time1 ? 0 : 1));
        }
        return new SituacaoRodada(variante, new ArrayList<>(mesa), mesa.indexOf(jogador), equipe, naMesa,
                lider, indiceMao, placarDasVazas, new ArrayList<>(jogadasAnteriores), nivelAtual,
                new int[] {time1.getPontos(), time2.getPontos()});
    }

    private Time timeDe(Jogador jogador) {
        return time1.temJogador(jogador) ? time1 : time2;
    }

    private Time decidirVencedor(List<Time> resultados) {
        int vitoriasTime1 = 0;
        int vitoriasTime2 = 0;
        for (Time time : resultados) {
            if (time == time1) {
                vitoriasTime1++;
            } else if (time == time2) {
                vitoriasTime2++;
            }
        }
        if (vitoriasTime1 == 2) {
            return time1;
        }
        if (vitoriasTime2 == 2) {
            return time2;
        }

        int vazasJogadas = resultados.size();

        if (vazasJogadas == 2) {
            Time primeira = resultados.get(0);
            Time segunda = resultados.get(1);
            if (primeira == null && segunda != null) {
                return segunda;
            }
            if (primeira != null && segunda == null) {
                return primeira;
            }
        }

        if (vazasJogadas == 3) {
            Time primeira = resultados.get(0);
            Time segunda = resultados.get(1);
            Time terceira = resultados.get(2);
            if (primeira == null && segunda == null) {
                return terceira != null ? terceira : timeDe(mesa.get(indiceMao));
            }
            return primeira;
        }

        return null;
    }
}

import java.util.ArrayList;
import java.util.List;

public class Rodada {
    private static final int CARTAS_POR_JOGADOR = 3;

    private final Variante variante;
    private final List<Jogador> mesa;
    private final Time time1;
    private final Time time2;
    private final InterfaceJogo ui;
    private final int indiceMao;

    private NivelAposta nivelAtual;
    private Time ultimoAumento;
    private Time vencedor;
    private int pontosGanhos;

    public Rodada(Variante variante, List<Jogador> mesa, Time time1, Time time2, InterfaceJogo ui, int indiceMao) {
        this.variante = variante;
        this.mesa = new ArrayList<>(mesa);
        this.time1 = time1;
        this.time2 = time2;
        this.ui = ui;
        this.indiceMao = indiceMao;
        this.nivelAtual = NivelAposta.SEM_APOSTA;
    }

    public void jogar() {
        distribuirCartas();

        List<Time> resultados = new ArrayList<>();
        int lider = indiceMao;
        Time decisao = null;

        while (decisao == null) {
            Vaza vaza = new Vaza();

            for (int i = 0; i < mesa.size(); i++) {
                Jogador jogador = mesa.get((lider + i) % mesa.size());
                if (tentarAumento(jogador)) {
                    return;
                }
                jogarCartaDe(jogador, vaza);
            }

            Jogador ganhador = vaza.getVencedor(variante, time1);
            if (ganhador == null) {
                resultados.add(null);
                ui.mostrarMensagem("A vaza empatou.");
            } else {
                resultados.add(timeDe(ganhador));
                lider = mesa.indexOf(ganhador);
                ui.mostrarMensagem(ganhador.getNome() + " ganhou a vaza.");
            }
            decisao = decidirVencedor(resultados);
        }

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
            Carta carta = jogador.escolherCarta(variante);
            try {
                jogador.jogarCarta(carta);
                vaza.adicionar(jogador, carta);
                ui.mostrarMensagem(jogador.getNome() + " jogou " + carta);
                return;
            } catch (JogadaInvalidaException e) {
                ui.mostrarMensagem(e.getMessage());
            }
        }
    }

    private boolean tentarAumento(Jogador jogador) {
        Time time = timeDe(jogador);
        if (!nivelAtual.temProximo() || time == ultimoAumento) {
            return false;
        }
        if (!jogador.pedirAumento(nivelAtual.proximo())) {
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
            Resposta resposta = jogador.responderAposta(proposto);
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
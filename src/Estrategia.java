import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public final class Estrategia {
    private static final int CARTAS_POR_JOGADOR = 3;
    private static final int FORCA_ALTA = 9;
    private static final Random SORTEIO = new Random();
    private static final Map<String, Regras> REGRAS = new ConcurrentHashMap<>();

    private Estrategia() {
    }

    public static Carta escolherCarta(SituacaoRodada situacao, List<Carta> mao) {
        Regras regras = regras(situacao.variante());
        int indice = escolherIndice(regras, mao, situacao.naMesa(), situacao.meuAssento(),
                situacao.equipe(), situacao.resultados(), situacao.jogadasAnteriores());
        return mao.get(indice);
    }

    public static double chanceDeVencer(SituacaoRodada situacao, List<Carta> minhaMao,
                                        Map<Integer, List<Carta>> conhecidas, int simulacoes) {
        Regras regras = regras(situacao.variante());
        int assentos = situacao.mesa().size();
        int meu = situacao.meuAssento();
        int minhaEquipe = situacao.minhaEquipe();
        int vazasJogadas = situacao.resultados().size();
        Carta[] naMesa = situacao.naMesa();

        boolean[] ocupada = new boolean[regras.baralho.size()];
        marcar(regras, ocupada, minhaMao);
        for (List<Carta> mao : conhecidas.values()) {
            marcar(regras, ocupada, mao);
        }
        for (Carta carta : naMesa) {
            if (carta != null) {
                ocupada[regras.id(carta)] = true;
            }
        }
        marcar(regras, ocupada, situacao.jogadasAnteriores());

        List<Carta> desconhecidas = new ArrayList<>();
        for (int i = 0; i < ocupada.length; i++) {
            if (!ocupada[i]) {
                desconhecidas.add(regras.baralho.get(i));
            }
        }

        int[] faltam = new int[assentos];
        for (int i = 0; i < assentos; i++) {
            boolean conhecida = i == meu || conhecidas.containsKey(i);
            int restantes = CARTAS_POR_JOGADOR - vazasJogadas - (naMesa[i] != null ? 1 : 0);
            faltam[i] = conhecida ? 0 : Math.max(0, restantes);
        }

        int vitorias = 0;
        for (int s = 0; s < simulacoes; s++) {
            Collections.shuffle(desconhecidas, SORTEIO);
            List<List<Carta>> maos = new ArrayList<>(assentos);
            int cursor = 0;
            for (int i = 0; i < assentos; i++) {
                if (i == meu) {
                    maos.add(new ArrayList<>(minhaMao));
                } else if (conhecidas.containsKey(i)) {
                    maos.add(new ArrayList<>(conhecidas.get(i)));
                } else {
                    int fim = Math.min(desconhecidas.size(), cursor + faltam[i]);
                    maos.add(new ArrayList<>(desconhecidas.subList(cursor, fim)));
                    cursor = fim;
                }
            }
            int vencedor = simular(regras, maos, naMesa.clone(), situacao.lider(), situacao.equipe(),
                    situacao.resultados(), situacao.jogadasAnteriores(), situacao.maoInicial());
            if (vencedor == minhaEquipe) {
                vitorias++;
            }
        }
        return (double) vitorias / simulacoes;
    }

    private static int simular(Regras regras, List<List<Carta>> maos, Carta[] mesa, int lider, int[] equipe,
                               List<Integer> resultadosIniciais, List<Carta> vistasIniciais, int maoInicial) {
        List<Integer> resultados = new ArrayList<>(resultadosIniciais);
        List<Carta> vistas = new ArrayList<>(vistasIniciais);
        int assentos = mesa.length;

        while (true) {
            for (int k = 0; k < assentos; k++) {
                int assento = (lider + k) % assentos;
                if (mesa[assento] != null) {
                    continue;
                }
                List<Carta> mao = maos.get(assento);
                int indice = escolherIndice(regras, mao, mesa, assento, equipe, resultados, vistas);
                mesa[assento] = mao.remove(indice);
            }

            int vencedor = vencedorDaVaza(regras, mesa, equipe, lider);
            for (int i = 0; i < assentos; i++) {
                vistas.add(mesa[i]);
                mesa[i] = null;
            }
            if (vencedor < 0) {
                resultados.add(-1);
            } else {
                resultados.add(equipe[vencedor]);
                lider = vencedor;
            }

            int decisao = decidir(resultados, equipe[maoInicial]);
            if (decisao >= 0) {
                return decisao;
            }
        }
    }

    private static int vencedorDaVaza(Regras regras, Carta[] mesa, int[] equipe, int lider) {
        int maior = -1;
        for (Carta carta : mesa) {
            if (carta != null) {
                maior = Math.max(maior, regras.forca(carta));
            }
        }
        int vencedor = -1;
        for (int k = 0; k < mesa.length; k++) {
            int assento = (lider + k) % mesa.length;
            if (mesa[assento] == null || regras.forca(mesa[assento]) != maior) {
                continue;
            }
            if (vencedor < 0) {
                vencedor = assento;
            } else if (equipe[vencedor] != equipe[assento]) {
                return -1;
            }
        }
        return vencedor;
    }

    private static int decidir(List<Integer> resultados, int equipeDaMao) {
        int vitorias0 = 0;
        int vitorias1 = 0;
        for (int resultado : resultados) {
            if (resultado == 0) {
                vitorias0++;
            } else if (resultado == 1) {
                vitorias1++;
            }
        }
        if (vitorias0 == 2) {
            return 0;
        }
        if (vitorias1 == 2) {
            return 1;
        }
        if (resultados.size() == 2) {
            int primeira = resultados.get(0);
            int segunda = resultados.get(1);
            if (primeira == -1 && segunda != -1) {
                return segunda;
            }
            if (primeira != -1 && segunda == -1) {
                return primeira;
            }
        }
        if (resultados.size() == 3) {
            int primeira = resultados.get(0);
            int segunda = resultados.get(1);
            int terceira = resultados.get(2);
            if (primeira == -1 && segunda == -1) {
                return terceira != -1 ? terceira : equipeDaMao;
            }
            return primeira;
        }
        return -1;
    }

    private static int escolherIndice(Regras regras, List<Carta> mao, Carta[] naMesa, int assento, int[] equipe,
                                      List<Integer> resultados, List<Carta> vistas) {
        int n = mao.size();
        if (n == 1) {
            return 0;
        }

        int[] ordem = ordenarPorForca(regras, mao);
        int minhaEquipe = equipe[assento];
        int melhorParceiro = -1;
        int melhorAdversario = -1;
        int adversariosRestantes = 0;
        boolean abertura = true;

        for (int i = 0; i < naMesa.length; i++) {
            if (i == assento) {
                continue;
            }
            if (naMesa[i] != null) {
                abertura = false;
                int forca = regras.forca(naMesa[i]);
                if (equipe[i] == minhaEquipe) {
                    melhorParceiro = Math.max(melhorParceiro, forca);
                } else {
                    melhorAdversario = Math.max(melhorAdversario, forca);
                }
            } else if (equipe[i] != minhaEquipe) {
                adversariosRestantes++;
            }
        }

        if (abertura) {
            return abrir(regras, mao, ordem, resultados);
        }

        boolean parceiroGanha = melhorParceiro > melhorAdversario;

        if (adversariosRestantes == 0) {
            if (parceiroGanha) {
                return ordem[0];
            }
            return vencerOuEmpatar(regras, mao, ordem, melhorAdversario, melhorParceiro);
        }

        if (parceiroGanha) {
            int[] desconhecidas = forcasDesconhecidas(regras, mao, naMesa, vistas);
            if (melhorParceiro > maximo(desconhecidas)) {
                return ordem[0];
            }
            int cartasAdversarias = adversariosRestantes * n;
            double perigo = chanceDeSerSuperada(desconhecidas, melhorParceiro, cartasAdversarias);
            int reforco = menorMaiorQue(regras, mao, ordem, melhorParceiro);
            if (reforco >= 0 && perigo >= 0.5) {
                double perigoDoReforco = chanceDeSerSuperada(desconhecidas,
                        regras.forca(mao.get(reforco)), cartasAdversarias);
                if (perigoDoReforco + 0.15 <= perigo) {
                    return reforco;
                }
            }
            return ordem[0];
        }

        return vencerOuEmpatar(regras, mao, ordem, melhorAdversario, melhorParceiro);
    }

    private static int abrir(Regras regras, List<Carta> mao, int[] ordem, List<Integer> resultados) {
        int n = mao.size();
        if (resultados.isEmpty() && n == 3) {
            int fortes = 0;
            for (Carta carta : mao) {
                if (regras.forca(carta) >= FORCA_ALTA) {
                    fortes++;
                }
            }
            if (fortes >= 2) {
                return ordem[1];
            }
        }
        return ordem[n - 1];
    }

    private static int vencerOuEmpatar(Regras regras, List<Carta> mao, int[] ordem,
                                       int melhorAdversario, int melhorParceiro) {
        int vencedora = menorMaiorQue(regras, mao, ordem, melhorAdversario);
        if (vencedora >= 0) {
            return vencedora;
        }
        if (melhorParceiro < melhorAdversario) {
            for (int indice : ordem) {
                if (regras.forca(mao.get(indice)) == melhorAdversario) {
                    return indice;
                }
            }
        }
        return ordem[0];
    }

    private static int menorMaiorQue(Regras regras, List<Carta> mao, int[] ordem, int limite) {
        for (int indice : ordem) {
            if (regras.forca(mao.get(indice)) > limite) {
                return indice;
            }
        }
        return -1;
    }

    private static int[] ordenarPorForca(Regras regras, List<Carta> mao) {
        int n = mao.size();
        int[] ordem = new int[n];
        for (int i = 0; i < n; i++) {
            int j = i;
            while (j > 0 && regras.forca(mao.get(ordem[j - 1])) > regras.forca(mao.get(i))) {
                ordem[j] = ordem[j - 1];
                j--;
            }
            ordem[j] = i;
        }
        return ordem;
    }

    private static int[] forcasDesconhecidas(Regras regras, List<Carta> mao, Carta[] naMesa, List<Carta> vistas) {
        boolean[] vista = new boolean[regras.baralho.size()];
        marcar(regras, vista, mao);
        marcar(regras, vista, vistas);
        for (Carta carta : naMesa) {
            if (carta != null) {
                vista[regras.id(carta)] = true;
            }
        }
        int total = 0;
        for (boolean v : vista) {
            if (!v) {
                total++;
            }
        }
        int[] forcas = new int[total];
        int k = 0;
        for (int i = 0; i < vista.length; i++) {
            if (!vista[i]) {
                forcas[k++] = regras.forcaPorId[i];
            }
        }
        return forcas;
    }

    private static int maximo(int[] valores) {
        int maior = -1;
        for (int valor : valores) {
            maior = Math.max(maior, valor);
        }
        return maior;
    }

    private static double chanceDeSerSuperada(int[] desconhecidas, int forca, int cartasAdversarias) {
        int total = desconhecidas.length;
        if (total == 0 || cartasAdversarias <= 0) {
            return 0;
        }
        int superiores = 0;
        for (int valor : desconhecidas) {
            if (valor > forca) {
                superiores++;
            }
        }
        int sorteadas = Math.min(cartasAdversarias, total);
        double nenhuma = 1.0;
        for (int i = 0; i < sorteadas; i++) {
            nenhuma *= (double) Math.max(0, total - superiores - i) / (total - i);
        }
        return 1.0 - nenhuma;
    }

    private static void marcar(Regras regras, boolean[] marcadas, List<Carta> cartas) {
        for (Carta carta : cartas) {
            marcadas[regras.id(carta)] = true;
        }
    }

    private static Regras regras(Variante variante) {
        return REGRAS.computeIfAbsent(variante.getNome(), nome -> new Regras(variante));
    }

    private static final class Regras {
        final List<Carta> baralho;
        final int[] forcaPorId;
        private final Map<Carta, Integer> ids = new HashMap<>();

        Regras(Variante variante) {
            baralho = new Baralho(variante).getCartas();
            forcaPorId = new int[baralho.size()];
            for (int i = 0; i < baralho.size(); i++) {
                ids.put(baralho.get(i), i);
                forcaPorId[i] = variante.getForca(baralho.get(i));
            }
        }

        int id(Carta carta) {
            return ids.get(carta);
        }

        int forca(Carta carta) {
            return forcaPorId[ids.get(carta)];
        }
    }
}


import java.util.LinkedHashMap;
import java.util.Map;

public class Vaza {
    private Map<Jogador, Carta> jogadas;

    public Vaza() {
        this.jogadas = new LinkedHashMap<>();
    }

    public void adicionar(Jogador j, Carta c) throws JogadaInvalidaException {
        if (jogadas.containsKey(j)) {
            throw new JogadaInvalidaException(j.getNome() + " já jogou nesta vaza.");
        }
        jogadas.put(j, c);
    }

    public Map<Jogador, Carta> getJogadas() {
        return new LinkedHashMap<>(jogadas);
    }

    public Jogador getVencedor(Variante v, Time referencia) {
        int maiorForca = -1;
        for (Carta c : jogadas.values()) {
            maiorForca = Math.max(maiorForca, v.getForca(c));
        }

        Jogador vencedor = null;
        for (Map.Entry<Jogador, Carta> jogada : jogadas.entrySet()) {
            if (v.getForca(jogada.getValue()) == maiorForca) {
                Jogador atual = jogada.getKey();
                if (vencedor == null) {
                    vencedor = atual;
                } else if (referencia.temJogador(vencedor) != referencia.temJogador(atual)) {
                    return null;
                }
            }
        }
        return vencedor;
    }

    public boolean empatou(Variante v, Time referencia) {
        return !jogadas.isEmpty() && getVencedor(v, referencia) == null;
    }
}

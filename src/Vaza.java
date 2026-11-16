import java.util.HashMap;
import java.util.Map;

public class Vaza {
    private Map<Jogador, Carta> jogadas;

    public Vaza() {
        this.jogadas = new HashMap<>();
    }

    public void adicionar(Jogador j, Carta c) {
        jogadas.put(j, c);
    }

    public Jogador getVencedor(Variante v, Time referencia){
    int maior = -1;
    for (Carta c : jogadas.values()){
        maior = Math.max(maior, v.getForca(c));
    }
    Jogador vencedor = null;
    for (Jogador j : jogadas.keySet()){
        if (v.getForca(jogadas.get(j)) == maior){
            if (vencedor == null){
                vencedor = j;
            } else if (referencia.temJogador(vencedor) != referencia.temJogador(j)){
                return null;
            }
        }
    }
    return vencedor;
}

    public boolean empatou(Variante v, Time referencia){
    return getVencedor(v, referencia) == null;
    }
}
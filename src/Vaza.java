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

    public Jogador getVencedor(Variante v) {
        Jogador vencedor = null;
        int maiorForca = -1;

        for (Jogador j : jogadas.keySet()) {
            Carta c = jogadas.get(j);
            int forca = v.getForca(c);

            if (forca > maiorForca) {
                maiorForca = forca;
                vencedor = j;
            } else if (forca == maiorForca) {
                vencedor = null;
            }
        }

        return vencedor;
    }

    public boolean empatou(Variante v) {
        return getVencedor(v) == null;
    }
}
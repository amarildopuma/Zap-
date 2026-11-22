import java.util.ArrayList;
import java.util.List;

public abstract class Jogador {
    private String nome;
    private List<Carta> mao = new ArrayList<>();

    public Jogador(String nome){
        this.nome=nome;
        this.mao=new ArrayList<>();
    }

    public void receberCartas(List<Carta> cartasRecebidas){
        this.mao.clear();
        if(cartasRecebidas != null){
            this.mao.addAll(cartasRecebidas);
        }
    }
    public void jogarCarta(Carta carta) throws JogadaInvalidaException {
        if (!this.mao.contains(carta)) {
            throw new JogadaInvalidaException("A carta escolhida não está na mão do jogador.");
        }
        this.mao.remove(carta);
    }

    public String getNome() {
        return this.nome;
    }

    public List<Carta> getMao() {
        return new ArrayList<>(this.mao);
    }
    public abstract Carta escolherCarta(Variante variante);
    public abstract Resposta responderAposta(NivelAposta nivelAtual);
    public abstract boolean pedirAumento(NivelAposta proposto);
}
public class JogadorHumano extends Jogador {
    private InterfaceJogo ui;

    public JogadorHumano(String nome, InterfaceJogo ui) {
        super(nome);
        this.ui = ui;
    }

    @Override
    public Carta escolherCarta(Variante variante) {
        return ui.lerCarta(this);
    }

    @Override
    public Resposta responderAposta(NivelAposta nivelAtual) {
        return ui.lerResposta(this, nivelAtual);
    }

    @Override
    public boolean pedirAumento(NivelAposta proposto) {
        return ui.lerPedidoAumento(this, proposto);
    }

    @Override
    public boolean ehHumano() {
        return true;
    }
}
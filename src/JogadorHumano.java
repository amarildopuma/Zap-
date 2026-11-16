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
}
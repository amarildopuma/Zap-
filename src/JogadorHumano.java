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
    public Resposta lerRespostaAposta(NivelAposta nivelAtual) {
        return ui.lerRespostaAposta(this, nivelAtual);
    }

    @Override
    public boolean pedirTruco(NivelAposta proposto) {
        return ui.pedirTruco(this, proposto);
    }

    @Override
    public boolean irMaoDeDez(java.util.List<Jogador> time) {
        return ui.perguntarMaoDeDez(this, time);
    }

    @Override
    public boolean ehHumano() {
        return true;
    }
}

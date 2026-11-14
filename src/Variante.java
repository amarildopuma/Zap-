public abstract class Variante {
    private final String nome;

    protected Variante(String nome){
        this.nome = nome;
    }

    public String getNome(){
        return nome;
    }

    public abstract int getForca(Carta carta);
    public abstract int getJogadoresPorTime();
    public abstract boolean temCoringas();
}

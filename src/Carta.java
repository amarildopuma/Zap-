import java.util.Objects;

public class Carta {
    private final String valor;
    private final Naipe naipe;

    public Carta(String valor, Naipe naipe){
        this.valor = valor;
        this.naipe = naipe;
    }

    public String getValor(){
        return valor;
    }

    public Naipe getNaipe(){
        return naipe;
    }

    @Override
    public String toString(){
        return valor+naipe.getSimbolo();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Carta)) return false;
        Carta outra = (Carta) o;
        return valor.equals(outra.valor) && naipe == outra.naipe;
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor, naipe);
    }
}

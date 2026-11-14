import java.util.List;

public class TrucoMineiro extends Variante{
    public TrucoMineiro(){
        super("Truco Mineiro");
    }

    private static final List<String> ORDEM_COMUM = List.of("4", "5", "6", "7", "Q", "J", "K", "A", "2", "3");

    private static final List<Carta> MANILHAS = List.of(
            new Carta("7", Naipe.OUROS),
            new Carta("A", Naipe.ESPADAS),
            new Carta("7", Naipe.COPAS),
            new Carta("4", Naipe.PAUS));

    @Override
    public int getForca(Carta carta){
        if (MANILHAS.contains(carta)){
            return ORDEM_COMUM.size() + MANILHAS.indexOf(carta);
        }else{
            return ORDEM_COMUM.indexOf(carta.getValor());
        }
    }

    @Override
    public int getJogadoresPorTime(){
        return 2;
    }

    @Override
    public boolean temCoringas(){
        return false;
    }
}

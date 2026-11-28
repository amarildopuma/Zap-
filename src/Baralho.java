import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Baralho {
    private List<Carta> cartas;

    public Baralho(Variante v){
        this.cartas=new ArrayList<>();

        String[] valores={"4","5","6","7","Q","J","K","A","2","3"};

        Naipe[] naipes={Naipe.COPAS,Naipe.ESPADAS,Naipe.OUROS,Naipe.PAUS};

        for(Naipe n:naipes){
            for(String valor:valores){
                cartas.add(new Carta(valor,n));
            }
        }

        if(v.temCoringas()){
            cartas.add(new Carta("COPAO",Naipe.PRETO));
            cartas.add(new Carta("COPAO",Naipe.DOURADO));
        }
    }

    public List<Carta> getCartas(){
        return new ArrayList<>(cartas);
    }

    public void embaralhar(){
        Collections.shuffle(cartas);
    }

    public List<Carta> distribuir(int n){
        List<Carta> mao=new ArrayList<>();

        for(int i=0;i<n;i++){
            mao.add(cartas.remove(0));
        }
        return mao;
    }
}

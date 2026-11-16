import java.util.ArrayList;
import java.util.List;
public class Time {
    private List<Jogador>jogadores=new ArrayList<>();
    private int pontos;

    public Time(List<Jogador> jogadores){
        this.jogadores=new ArrayList<>(jogadores);
        this.pontos=0;
    }
    public void adicionarPontos(int p){
        this.pontos=this.pontos+p;
    }
    public boolean temJogador(Jogador j){
        return this.jogadores.contains(j);
    }
    public int getPontos(){
        return this.pontos;
    }
    public List<Jogador> getJogadores(){
        return new ArrayList<>(this.jogadores);
    }
}

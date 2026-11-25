import java.util.ArrayList;
import java.util.List;
public class Time {
    private final String nome;
    private List<Jogador>jogadores=new ArrayList<>();
    private int pontos;

    public Time(String nome, List<Jogador> jogadores){
        this.nome=nome;
        this.jogadores=new ArrayList<>(jogadores);
        this.pontos=0;
    }
    public Time(List<Jogador> jogadores){
        this("Time", jogadores);
    }
    public void adicionarPontos(int p){
        this.pontos=this.pontos+p;
    }
    public boolean temJogador(Jogador j){
        return this.jogadores.contains(j);
    }
    public String getNome(){
        return this.nome;
    }
    public int getPontos(){
        return this.pontos;
    }
    public List<Jogador> getJogadores(){
        return new ArrayList<>(this.jogadores);
    }
}
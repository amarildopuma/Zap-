import java.util.List;
import java.util.Scanner;

public class InterfaceTerminal implements InterfaceJogo{

    private final Scanner scanner = new Scanner(System.in);

    private void mostrarMao(Jogador jogador) {
        List<Carta> mao = jogador.getMao();
        for (int i = 0; i < mao.size(); i++) {
            System.out.println("[" + (i + 1) + "] " + mao.get(i));
        }
    
}

    @Override 
    public Carta lerCarta(Jogador jogador){
        List<Carta> mao = jogador.getMao();
        mostrarMao(jogador);
        while(true){
            System.out.print("Insira a carta à ser jogada: ");
            String resposta = scanner.nextLine();
            try{
                int numeroCarta = Integer.parseInt(resposta);
                if(numeroCarta >= 1 && numeroCarta <= mao.size()){
                    return mao.get(numeroCarta - 1);
                }
            }
            catch(NumberFormatException e){
                
            }
            System.out.println("Opção inválida!");
        }
    }
    @Override
    public Resposta lerResposta(Jogador jogador, NivelAposta nivelAtual) {
        mostrarMao(jogador);
        System.out.println("[A] - Aceitar");
        System.out.println("[C] - Correr");

        if(nivelAtual.temProximo()){
            System.out.println("[R] - Aumentar para " + nivelAtual.proximo());
        }

        while(true){
            System.out.println("Sua resposta: ");
            String entrada = scanner.nextLine().trim().toLowerCase();

            if (entrada.equals("a")) {
                return Resposta.ACEITAR;
            }
            else if (entrada.equals("c")){
                return Resposta.CORRER;
            }
            else if (entrada.equals("r") && nivelAtual.temProximo()){
                return Resposta.AUMENTAR;
            }

            System.out.println("Opção inválida!");
        }
    }

}
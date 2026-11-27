import java.util.List;
import java.util.Scanner;

public class InterfaceTerminal implements InterfaceJogo {
    private final Scanner scanner;

    public InterfaceTerminal(Scanner scanner) {
        this.scanner = scanner;
    }

    public String perguntar(String pergunta) {
        System.out.print(pergunta);
        return scanner.nextLine().trim();
    }

    private void mostrarMao(Jogador jogador) {
        List<Carta> mao = jogador.getMao();
        for (int i = 0; i < mao.size(); i++) {
            System.out.println("[" + (i + 1) + "] " + mao.get(i));
        }
    
    }

    @Override
    public Carta lerCarta(Jogador jogador) {
        List<Carta> mao = jogador.getMao();
        mostrarMao(jogador);

        while (true) {
            String resposta = perguntar("Insira a carta a ser jogada: ");
            try {
                int numeroCarta = Integer.parseInt(resposta);
                if (numeroCarta >= 1 && numeroCarta <= mao.size()) {
                    return mao.get(numeroCarta - 1);
                }
            } catch (NumberFormatException e) {
                // não era número: cai na mensagem abaixo
            }
            System.out.println("Opção inválida!");
        }
    }

    @Override
    public Resposta lerRespostaAposta(Jogador jogador, NivelAposta nivelAtual) {
        mostrarMao(jogador);
        System.out.println("[A] - Aceitar");
        System.out.println("[C] - Correr");
        if (nivelAtual.temProximo()) {
            System.out.println("[R] - Aumentar para " + nivelAtual.proximo());
        }

        while (true) {
            String entrada = perguntar("Sua resposta: ").toLowerCase();

            if (entrada.equals("a")) {
                return Resposta.ACEITAR;
            } else if (entrada.equals("c")) {
                return Resposta.CORRER;
            } else if (entrada.equals("r") && nivelAtual.temProximo()) {
                return Resposta.AUMENTAR;
            }
            System.out.println("Opção inválida!");
        }
    }

    @Override
    public void mostrarMensagem(String mensagem) {
        System.out.println(mensagem);
    }

    @Override
    public boolean pedirTruco(Jogador jogador, NivelAposta proposta) {
        mostrarMao(jogador);


        while(true){
            if (proposta == NivelAposta.TRUCO) {
                System.out.println("Deseja trucar?");
            } else {
                System.out.println("Deseja pedir " + proposta + "?");
            }
            System.out.println("[S] - Sim");
            System.out.println("[N] - Não");
            String entrada = perguntar("Sua resposta: ").toLowerCase();

            if (entrada.equals("s")) {
                return true;
            }
            else if (entrada.equals("n")){
                return false;
            }

            System.out.println("Opção inválida!");
        }

    }

}
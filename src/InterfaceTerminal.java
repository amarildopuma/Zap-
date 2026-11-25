import java.util.List;
import java.util.Scanner;

public class InterfaceTerminal implements InterfaceJogo {
    private final Scanner scanner;

    public InterfaceTerminal(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public Carta lerCarta(Jogador jogador) {
        List<Carta> mao = jogador.getMao();
        while (true) {
            System.out.println("Sua mão:");
            for (int i = 0; i < mao.size(); i++) {
                System.out.println("  " + (i + 1) + ") " + mao.get(i));
            }
            System.out.print("Escolha a carta: ");
            Integer escolha = lerNumero();
            if (escolha != null && escolha >= 1 && escolha <= mao.size()) {
                return mao.get(escolha - 1);
            }
            System.out.println("Opção inválida.");
        }
    }

    @Override
    public Resposta lerResposta(Jogador jogador, NivelAposta nivelAtual) {
        System.out.println();
        System.out.println(jogador.getNome() + ", pediram " + nivelAtual + "! A rodada passa a valer " + nivelAtual.getPontos() + " pontos.");
        while (true) {
            System.out.println("  1) Aceitar");
            if (nivelAtual.temProximo()) {
                System.out.println("  2) Aumentar para " + nivelAtual.proximo() + " (" + nivelAtual.proximo().getPontos() + " pontos)");
            }
            System.out.println("  3) Correr");
            System.out.print("Sua resposta: ");
            Integer escolha = lerNumero();
            if (escolha != null) {
                if (escolha == 1) {
                    return Resposta.ACEITAR;
                }
                if (escolha == 2 && nivelAtual.temProximo()) {
                    return Resposta.AUMENTAR;
                }
                if (escolha == 3) {
                    return Resposta.CORRER;
                }
            }
            System.out.println("Opção inválida.");
        }
    }

    @Override
    public boolean lerPedidoAumento(Jogador jogador, NivelAposta proposto) {
        System.out.println();
        System.out.println("Sua mão: " + formatarMao(jogador.getMao()));
        System.out.print("Pedir " + proposto + " (a rodada passaria a valer " + proposto.getPontos() + ")? (s/N): ");
        String resposta = scanner.nextLine().trim();
        return resposta.equalsIgnoreCase("s");
    }

    @Override
    public void mostrarMensagem(String mensagem) {
        System.out.println(mensagem);
    }

    private Integer lerNumero() {
        String linha = scanner.nextLine().trim();
        try {
            return Integer.parseInt(linha);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatarMao(List<Carta> mao) {
        StringBuilder texto = new StringBuilder();
        for (Carta carta : mao) {
            texto.append(carta).append("  ");
        }
        return texto.toString().trim();
    }
}
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Menu {
    private final Scanner scanner;
    private final InterfaceTerminal ui;

    public Menu(Scanner scanner) {
        this.scanner = scanner;
        this.ui = new InterfaceTerminal(scanner);
    }

    public void iniciar() {
        System.out.println("=================================");
        System.out.println("   ZAP! - Truco no terminal");
        System.out.println("=================================");

        boolean rodando = true;
        while (rodando) {
            exibirOpcoes();
            String opcao = scanner.nextLine().trim();
            switch (opcao) {
                case "1":
                    jogar();
                    break;
                case "2":
                    exibirRegras();
                    break;
                case "0":
                    rodando = false;
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        }
        System.out.println("Até a próxima!");
    }

    private void exibirOpcoes() {
        System.out.println();
        System.out.println("1) Jogar");
        System.out.println("2) Regras");
        System.out.println("0) Sair");
        System.out.print("Escolha: ");
    }

    private void jogar() {
        ModoJogo modo = escolherModo();
        if (modo == null) {
            return;
        }

        System.out.print("Seu nome: ");
        String nome = scanner.nextLine().trim();
        if (nome.isEmpty()) {
            nome = "Você";
        }

        Partida partida = criarPartida(modo, nome);
        partida.jogar();
    }

    private ModoJogo escolherModo() {
        ModoJogo[] modos = ModoJogo.values();
        while (true) {
            System.out.println();
            for (int i = 0; i < modos.length; i++) {
                System.out.println((i + 1) + ") " + modos[i].getDescricao());
            }
            System.out.println("0) Voltar");
            System.out.print("Modo de jogo: ");

            String entrada = scanner.nextLine().trim();
            if (entrada.equals("0")) {
                return null;
            }
            try {
                int indice = Integer.parseInt(entrada);
                if (indice >= 1 && indice <= modos.length) {
                    return modos[indice - 1];
                }
            } catch (NumberFormatException e) {
            }
            System.out.println("Opção inválida.");
        }
    }

    private Partida criarPartida(ModoJogo modo, String nome) {
        Variante variante = modo.criarVariante();
        int jogadoresPorTime = variante.getJogadoresPorTime();

        List<Jogador> jogadoresTime1 = new ArrayList<>();
        List<Jogador> jogadoresTime2 = new ArrayList<>();

        jogadoresTime1.add(new JogadorHumano(nome, ui));
        for (int i = 1; i < jogadoresPorTime; i++) {
            jogadoresTime1.add(new JogadorComputador("Parceiro " + i, variante));
        }
        for (int i = 1; i <= jogadoresPorTime; i++) {
            jogadoresTime2.add(new JogadorComputador("Adversário " + i, variante));
        }

        Time time1 = new Time("Seu time", jogadoresTime1);
        Time time2 = new Time("Time adversário", jogadoresTime2);

        List<Jogador> mesa = new ArrayList<>();
        for (int i = 0; i < jogadoresPorTime; i++) {
            mesa.add(jogadoresTime1.get(i));
            mesa.add(jogadoresTime2.get(i));
        }

        return new Partida(variante, mesa, time1, time2, ui);
    }

    private void exibirRegras() {
        System.out.println();
        System.out.println("--- REGRAS ---");
        System.out.println("Objetivo: ser o primeiro time a chegar a 12 pontos.");
        System.out.println();
        System.out.println("Rodada: cada jogador recebe 3 cartas e ganha quem vencer 2 de 3 vazas.");
        System.out.println("A carta mais forte da mesa ganha a vaza, e quem ganha começa a próxima.");
        System.out.println();
        System.out.println("Força das cartas (da mais fraca para a mais forte):");
        System.out.println("  4 5 6 7 Q J K A 2 3");
        System.out.println("  Manilhas (acima de todas): 7♦, A♠, 7♥, 4♣ (o zap, a mais forte)");
        System.out.println();
        System.out.println("Apostas: a rodada vale 2 pontos. Truco = 4, Seis = 8, Nove = 10, Doze = 12.");
        System.out.println("Quem recebe o pedido pode aceitar, aumentar ou correr.");
        System.out.println("Se correr, o time que pediu ganha os pontos da aposta anterior.");
        System.out.println("Só pode aumentar o time que não fez o último pedido.");
        System.out.println();
        System.out.println("Empates:");
        System.out.println("  - Se a 1ª vaza empata, vale a 2ª.");
        System.out.println("  - Se um time ganhou a 1ª e a 2ª empata, esse time ganha.");
        System.out.println("  - Se cada time ganhou uma e a 3ª empata, vale quem ganhou a 1ª.");
        System.out.println("  - Se tudo empata, ganha o time de quem começou a rodada.");
        System.out.println("  - Cartas iguais de parceiros não empatam: o time ganha a vaza.");
        System.out.println();
        System.out.println("Douradinha: 3 contra 3, com dois coringas (copão preto e dourado).");
        System.out.println("  Acima das manilhas comuns: catatau (4♠), 7 rato (7♣), copão dourado e copão preto (o mais forte).");
    }
}
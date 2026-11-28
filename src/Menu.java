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
        System.out.println("[1] -  Jogar");
        System.out.println("[2] - Regras");
        System.out.println("[0] - Sair");
        System.out.print("Escolha uma opção: ");
    }

    private void jogar() {
        ModoJogo modo = escolherModo();
        if (modo == null) {
            return;
        }

        Boolean contraBot = escolherContraBot();
        if (contraBot == null) {
            return;
        }

        Partida partida = criarPartida(modo, contraBot);
        partida.jogar();
    }

    private ModoJogo escolherModo() {
        ModoJogo[] modos = ModoJogo.values();
        String[] textos = new String[modos.length];
        for (int i = 0; i < modos.length; i++) {
            textos[i] = modos[i].getDescricao();
        }
        int indice = escolherOpcao("Modo de jogo: ", textos);
        return indice < 0 ? null : modos[indice];
    }

    private Boolean escolherContraBot() {
        String[] textos = {
                "Jogadores contra jogadores (todos humanos)",
                "Jogadores contra o computador (bots)"
        };
        int indice = escolherOpcao("Contra quem quer jogar? ", textos);
        if (indice < 0) {
            return null;
        }
        return indice == 1;
    }

    private int escolherOpcao(String pergunta, String[] textos) {
        while (true) {
            System.out.println();
            for (int i = 0; i < textos.length; i++) {
                System.out.println((i + 1) + ") " + textos[i]);
            }
            System.out.println("[0] - Voltar");
            System.out.print(pergunta);

            String entrada = scanner.nextLine().trim();
            if (entrada.equals("0")) {
                return -1;
            }
            try {
                int indice = Integer.parseInt(entrada);
                if (indice >= 1 && indice <= textos.length) {
                    return indice - 1;
                }
            } catch (NumberFormatException e) {
            }
            System.out.println("Opção inválida.");
        }
    }

    private String lerNome(String padrao) {
        System.out.print("Nome de " + padrao + ": ");
        String nome = scanner.nextLine().trim();
        return nome.isEmpty() ? padrao : nome;
    }

    private Partida criarPartida(ModoJogo modo, boolean contraBot) {
        Variante variante = modo.criarVariante();
        int jogadoresPorTime = variante.getJogadoresPorTime();

        List<Jogador> jogadoresTime1 = new ArrayList<>();
        List<Jogador> jogadoresTime2 = new ArrayList<>();

        System.out.println();
        System.out.println("--- Time 1 ---");
        for (int i = 1; i <= jogadoresPorTime; i++) {
            jogadoresTime1.add(new JogadorHumano(lerNome("Jogador " + i), ui));
        }

        if (!contraBot) {
            System.out.println("--- Time 2 ---");
            for (int i = 1; i <= jogadoresPorTime; i++) {
                jogadoresTime2.add(new JogadorHumano(lerNome("Jogador " + (jogadoresPorTime + i)), ui));
            }
        } else {
            for (int i = 1; i <= jogadoresPorTime; i++) {
                jogadoresTime2.add(new JogadorComputador("Bot " + i, variante));
            }
        }

        Time time1 = new Time("Time 1", jogadoresTime1);
        Time time2 = new Time(contraBot ? "Computador" : "Time 2", jogadoresTime2);

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

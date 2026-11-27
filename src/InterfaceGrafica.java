import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Rectangle;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Interface gráfica (Swing) do jogo. A partida roda em outra thread e esta classe
 * bloqueia essa thread esperando o clique do usuário, sem travar a janela.
 */
public class InterfaceGrafica implements InterfaceJogo {
    private static final Object TRUCO = new Object(); // marcador: clicou no botão de truco

    private final JFrame janela = new JFrame("Zap! - Truco");
    private final JLabel status = new JLabel(" ");
    private final JTextArea log = new JTextArea(4, 40);
    private final JButton btnTruco = new JButton();
    private final JButton btnAceitar = new JButton("Aceitar");
    private final JButton btnCorrer = new JButton("Correr");
    private final JButton btnAumentar = new JButton();
    private final MesaPanel mesaPanel = new MesaPanel();

    // Cliques do usuário (carta, botão de truco ou resposta) chegam aqui.
    private final BlockingQueue<Object> entrada = new LinkedBlockingQueue<>();

    // Usados só pela thread do jogo.
    private List<Jogador> mesa = new ArrayList<>();
    private Carta cartaPreEscolhida;

    /** Deve ser criada na thread de eventos do Swing (EDT). */
    public InterfaceGrafica() {
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        status.setFont(status.getFont().deriveFont(Font.BOLD, 16f));
        status.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        log.setEditable(false);
        log.setLineWrap(true);
        JScrollPane rolagem = new JScrollPane(log);
        rolagem.setPreferredSize(new Dimension(100, 90));

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        botoes.setPreferredSize(new Dimension(100, 52));
        botoes.add(btnTruco);
        botoes.add(btnAceitar);
        botoes.add(btnAumentar);
        botoes.add(btnCorrer);

        JPanel sul = new JPanel(new BorderLayout());
        sul.add(rolagem, BorderLayout.CENTER);
        sul.add(botoes, BorderLayout.SOUTH);

        janela.add(status, BorderLayout.NORTH);
        janela.add(mesaPanel, BorderLayout.CENTER);
        janela.add(sul, BorderLayout.SOUTH);

        btnTruco.addActionListener(e -> entrada.offer(TRUCO));
        btnAceitar.addActionListener(e -> entrada.offer(Resposta.ACEITAR));
        btnAumentar.addActionListener(e -> entrada.offer(Resposta.AUMENTAR));
        btnCorrer.addActionListener(e -> entrada.offer(Resposta.CORRER));
        mesaPanel.aoClicarCarta = carta -> entrada.offer(carta);

        esconderBotoes();
        janela.pack();
        janela.setLocationRelativeTo(null);
        janela.setVisible(true);
    }

    // ------------------------------------------------------------------
    // InterfaceJogo: perguntas ao usuário (rodam na thread do jogo e bloqueiam)
    // ------------------------------------------------------------------

    @Override
    public Carta lerCarta(Jogador jogador) {
        if (cartaPreEscolhida != null) { // o usuário já clicou numa carta em vez de trucar
            Carta carta = cartaPreEscolhida;
            cartaPreEscolhida = null;
            return carta;
        }
        entrada.clear();
        SwingUtilities.invokeLater(() -> {
            esconderBotoes();
            mesaPanel.cartasClicaveis = true;
            status.setText("Sua vez: clique em uma carta.");
            mesaPanel.repaint();
        });
        while (true) {
            Object o = esperar();
            if (o instanceof Carta) {
                bloquearCartas();
                return (Carta) o;
            }
        }
    }

    @Override
    public boolean pedirTruco(Jogador jogador, NivelAposta proposto) {
        entrada.clear();
        String rotulo = proposto == NivelAposta.TRUCO ? "TRUCO!" : "Pedir " + proposto + "!";
        SwingUtilities.invokeLater(() -> {
            esconderBotoes();
            btnTruco.setText(rotulo);
            btnTruco.setVisible(true);
            mesaPanel.cartasClicaveis = true;
            status.setText("Sua vez: clique em uma carta ou peça " + proposto + ".");
            mesaPanel.repaint();
        });
        while (true) {
            Object o = esperar();
            if (o == TRUCO) {
                bloquearCartas();
                return true;
            }
            if (o instanceof Carta) {
                cartaPreEscolhida = (Carta) o; // lerCarta devolve essa carta sem perguntar de novo
                bloquearCartas();
                return false;
            }
        }
    }

    @Override
    public Resposta lerRespostaAposta(Jogador jogador, NivelAposta proposto) {
        entrada.clear();
        SwingUtilities.invokeLater(() -> {
            esconderBotoes();
            btnAceitar.setText("Aceitar (" + proposto.getPontos() + " pts)");
            btnAceitar.setVisible(true);
            btnCorrer.setVisible(true);
            if (proposto.temProximo()) {
                btnAumentar.setText("Aumentar para " + proposto.proximo());
                btnAumentar.setVisible(true);
            }
            status.setText("Pediram " + proposto + "! Aceitar, aumentar ou correr?");
            mesaPanel.repaint();
        });
        while (true) {
            Object o = esperar();
            if (o instanceof Resposta) {
                SwingUtilities.invokeLater(this::esconderBotoes);
                return (Resposta) o;
            }
        }
    }

    @Override
    public boolean perguntarMaoDeDez(Jogador jogador, List<Jogador> time) {
        StringBuilder texto = new StringBuilder("MÃO DE DEZ\n\nCartas do seu time:\n");
        for (Jogador j : time) {
            texto.append("  ").append(j.getNome()).append(": ").append(j.getMao()).append("\n");
        }
        texto.append("\nSe jogar, a rodada vale 4 pontos e ninguém pode pedir truco.\nO time vai à mão?");
        String[] opcoes = {"Jogar", "Correr"};

        int[] escolha = {-1};
        while (escolha[0] < 0) { // fechar a janela no X não vale como resposta
            try {
                SwingUtilities.invokeAndWait(() -> escolha[0] = JOptionPane.showOptionDialog(
                        janela, texto.toString(), "Mão de dez", JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[0]));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Jogo interrompido.", e);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException(e.getCause());
            }
        }
        return escolha[0] == 0;
    }

    @Override
    public void definirMaoCega(boolean cega) {
        SwingUtilities.invokeLater(() -> {
            mesaPanel.maoCega = cega; // mão de ferro: joga sem ver as cartas
            mesaPanel.repaint();
        });
    }

    // ------------------------------------------------------------------
    // InterfaceJogo: eventos que só atualizam a tela
    // ------------------------------------------------------------------

    @Override
    public void mostrarMensagem(String mensagem) {
        if (mensagem == null || mensagem.isBlank()) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            log.append(mensagem + "\n");
            log.setCaretPosition(log.getDocument().getLength());
            status.setText(mensagem);
            mesaPanel.repaint(); // o placar é lido dos Times ao desenhar
        });
    }

    @Override
    public void iniciarPartida(List<Jogador> mesa, Time time1, Time time2) {
        this.mesa = new ArrayList<>(mesa);
        List<Jogador> copia = new ArrayList<>(mesa);
        SwingUtilities.invokeLater(() -> {
            mesaPanel.mesa = copia;
            mesaPanel.time1 = time1;
            mesaPanel.time2 = time2;
            mesaPanel.repaint();
        });
    }

    @Override
    public void iniciarRodada() {
        pausa(900);
        Map<Jogador, List<Carta>> maos = copiarMaos();
        SwingUtilities.invokeLater(() -> {
            mesaPanel.maos = maos;
            mesaPanel.jogadas.clear();
            mesaPanel.vez = null;
            mesaPanel.repaint();
        });
    }

    @Override
    public void vezDe(Jogador jogador) {
        SwingUtilities.invokeLater(() -> {
            mesaPanel.vez = jogador;
            mesaPanel.repaint();
        });
    }

    @Override
    public void cartaJogada(Jogador jogador, Carta carta) {
        Map<Jogador, List<Carta>> maos = copiarMaos();
        SwingUtilities.invokeLater(() -> {
            mesaPanel.maos = maos;
            mesaPanel.jogadas.put(jogador, carta);
            mesaPanel.repaint();
        });
        pausa(jogador.ehHumano() ? 250 : 800);
    }

    @Override
    public void fimDaVaza() {
        pausa(1300);
        SwingUtilities.invokeLater(() -> {
            mesaPanel.jogadas.clear();
            mesaPanel.repaint();
        });
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private Object esperar() {
        try {
            return entrada.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Jogo interrompido.", e);
        }
    }

    private void pausa(int milissegundos) {
        try {
            Thread.sleep(milissegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Tira uma "foto" das mãos na thread do jogo, que é a única que as altera. */
    private Map<Jogador, List<Carta>> copiarMaos() {
        Map<Jogador, List<Carta>> maos = new LinkedHashMap<>();
        for (Jogador jogador : mesa) {
            maos.put(jogador, jogador.getMao());
        }
        return maos;
    }

    private void bloquearCartas() {
        SwingUtilities.invokeLater(() -> {
            mesaPanel.cartasClicaveis = false;
            esconderBotoes();
            mesaPanel.repaint();
        });
    }

    private void esconderBotoes() {
        btnTruco.setVisible(false);
        btnAceitar.setVisible(false);
        btnAumentar.setVisible(false);
        btnCorrer.setVisible(false);
    }

    // ------------------------------------------------------------------
    // A mesa desenhada (só roda na EDT)
    // ------------------------------------------------------------------

    static class MesaPanel extends JPanel {
        private static final Color VERDE = new Color(0x28, 0x7A, 0x0E);
        private static final Color CINZA = new Color(0xD9, 0xD9, 0xD9);
        private static final Color AZUL_TIME = new Color(0x4A, 0x90, 0xE2);
        private static final Color VERMELHO_TIME = new Color(0xE2, 0x57, 0x4A);
        private static final Color DOURADO_VEZ = new Color(0xFF, 0xD5, 0x4A);

        private static final int AVATAR_RAIO = 30;
        private static final int CARTA_W_HUMANO = 56, CARTA_H_HUMANO = 80;
        private static final int CARTA_W_OUTRO = 26, CARTA_H_OUTRO = 38;
        private static final int CARTA_W_MESA = 46, CARTA_H_MESA = 66;

        List<Jogador> mesa = new ArrayList<>();
        Map<Jogador, List<Carta>> maos = new LinkedHashMap<>();
        Map<Jogador, Carta> jogadas = new LinkedHashMap<>();
        Jogador vez;
        Time time1;
        Time time2;
        boolean cartasClicaveis;
        boolean maoCega;
        java.util.function.Consumer<Carta> aoClicarCarta = carta -> { };

        private final List<Rectangle> areas = new ArrayList<>();
        private final List<Carta> cartasDasAreas = new ArrayList<>();
        private int indiceHover = -1;

        MesaPanel() {
            setPreferredSize(new Dimension(820, 600));
            setBackground(new Color(0x1E, 0x1E, 0x1E));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (!cartasClicaveis) {
                        return;
                    }
                    int i = indiceDaCartaEm(e.getX(), e.getY());
                    if (i >= 0) {
                        aoClicarCarta.accept(cartasDasAreas.get(i));
                    }
                }
            });
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int i = cartasClicaveis ? indiceDaCartaEm(e.getX(), e.getY()) : -1;
                    if (i != indiceHover) {
                        indiceHover = i;
                        repaint();
                    }
                }
            });
        }

        private int indiceDaCartaEm(int x, int y) {
            for (int i = areas.size() - 1; i >= 0; i--) {
                if (areas.get(i).contains(x, y)) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            g.setColor(VERDE);
            g.fillRoundRect(12, 12, w - 24, h - 24, 30, 30);

            areas.clear();
            cartasDasAreas.clear();

            desenharPlacar(g, w);

            int n = mesa.size();
            for (int i = 0; i < n; i++) {
                desenharAssento(g, mesa.get(i), i, n, w, h);
            }
            for (Map.Entry<Jogador, Carta> jogada : jogadas.entrySet()) {
                int i = mesa.indexOf(jogada.getKey());
                if (i >= 0) {
                    desenharCartaJogada(g, jogada.getKey(), jogada.getValue(), i, n, w, h);
                }
            }
            g.dispose();
        }

        /** Assento i de n: o 0 fica embaixo e os demais seguem no sentido anti-horário. */
        private double[] posicaoAssento(int i, int n, int w, int h) {
            double ang = Math.PI / 2 - i * 2 * Math.PI / n;
            return new double[] {
                w / 2.0 + w * 0.36 * Math.cos(ang),
                h / 2.0 + h * 0.34 * Math.sin(ang),
                Math.cos(ang)
            };
        }

        private void desenharPlacar(Graphics2D g, int w) {
            if (time1 == null || time2 == null) {
                return;
            }
            g.setFont(getFont().deriveFont(Font.BOLD, 16f));
            g.setColor(Color.WHITE);
            String texto = time1.getNome() + " " + time1.getPontos() + "  x  "
                    + time2.getPontos() + " " + time2.getNome();
            centralizado(g, texto, w / 2, 38);
        }

        private void desenharAssento(Graphics2D g, Jogador jogador, int i, int n, int w, int h) {
            double[] p = posicaoAssento(i, n, w, h);
            int x = (int) p[0];
            int y = (int) p[1];

            Color corTime = (time1 != null && time1.temJogador(jogador)) ? AZUL_TIME : VERMELHO_TIME;
            if (jogador == vez) {
                g.setColor(DOURADO_VEZ);
                g.fillOval(x - AVATAR_RAIO - 7, y - AVATAR_RAIO - 7, 2 * (AVATAR_RAIO + 7), 2 * (AVATAR_RAIO + 7));
            }
            g.setColor(corTime);
            g.fillOval(x - AVATAR_RAIO - 3, y - AVATAR_RAIO - 3, 2 * (AVATAR_RAIO + 3), 2 * (AVATAR_RAIO + 3));
            g.setColor(CINZA);
            g.fillOval(x - AVATAR_RAIO, y - AVATAR_RAIO, 2 * AVATAR_RAIO, 2 * AVATAR_RAIO);

            String nome = jogador.getNome();
            g.setColor(new Color(0x55, 0x55, 0x55));
            g.setFont(getFont().deriveFont(Font.BOLD, 24f));
            centralizado(g, nome.isEmpty() ? "?" : nome.substring(0, 1).toUpperCase(), x, y);

            g.setColor(Color.WHITE);
            g.setFont(getFont().deriveFont(Font.PLAIN, 14f));
            centralizado(g, nome, x, y + AVATAR_RAIO + 22);

            // Cartas na mão: do lado do avatar que fica para dentro da mesa.
            List<Carta> mao = maos.getOrDefault(jogador, new ArrayList<>());
            boolean humano = jogador.ehHumano();
            int cw = humano ? CARTA_W_HUMANO : CARTA_W_OUTRO;
            int ch = humano ? CARTA_H_HUMANO : CARTA_H_OUTRO;
            int gap = 8;
            int larguraTotal = mao.size() * cw + Math.max(0, mao.size() - 1) * gap;
            int inicio = p[2] > 0.3
                    ? x - AVATAR_RAIO - 16 - larguraTotal
                    : x + AVATAR_RAIO + 16;

            for (int k = 0; k < mao.size(); k++) {
                int cx = inicio + k * (cw + gap);
                int cy = y - ch / 2;
                if (humano) {
                    int indiceGlobal = areas.size();
                    if (cartasClicaveis && indiceGlobal == indiceHover) {
                        cy -= 10;
                    }
                    areas.add(new Rectangle(cx, cy, cw, ch));
                    cartasDasAreas.add(mao.get(k));
                    if (maoCega) {
                        desenharVerso(g, cx, cy, cw, ch); // continua clicável, só não dá para ver
                    } else {
                        desenharFrente(g, mao.get(k), cx, cy, cw, ch);
                    }
                } else {
                    desenharVerso(g, cx, cy, cw, ch);
                }
            }
        }

        private void desenharCartaJogada(Graphics2D g, Jogador jogador, Carta carta, int i, int n, int w, int h) {
            double[] p = posicaoAssento(i, n, w, h);
            double cx = w / 2.0 + (p[0] - w / 2.0) * 0.42;
            double cy = h / 2.0 + (p[1] - h / 2.0) * 0.42;
            int x = (int) cx - CARTA_W_MESA / 2;
            int y = (int) cy - CARTA_H_MESA / 2;
            desenharFrente(g, carta, x, y, CARTA_W_MESA, CARTA_H_MESA);
            g.setColor(Color.WHITE);
            g.setFont(getFont().deriveFont(Font.PLAIN, 11f));
            centralizado(g, jogador.getNome(), (int) cx, y + CARTA_H_MESA + 13);
        }

        private void desenharVerso(Graphics2D g, int x, int y, int w, int h) {
            g.setColor(CINZA);
            g.fillRoundRect(x, y, w, h, 8, 8);
            g.setColor(new Color(0x9E, 0x9E, 0x9E));
            g.drawRoundRect(x, y, w, h, 8, 8);
            g.drawRoundRect(x + 4, y + 4, w - 8, h - 8, 4, 4);
        }

        private void desenharFrente(Graphics2D g, Carta carta, int x, int y, int w, int h) {
            Naipe naipe = carta.getNaipe();
            boolean coringa = naipe == Naipe.PRETO || naipe == Naipe.DOURADO;
            boolean vermelho = naipe == Naipe.COPAS || naipe == Naipe.OUROS;

            Color fundo = Color.WHITE;
            Color tinta = vermelho ? new Color(0xC6, 0x28, 0x28) : new Color(0x21, 0x21, 0x21);
            if (naipe == Naipe.PRETO) {
                fundo = new Color(0x22, 0x22, 0x22);
                tinta = Color.WHITE;
            } else if (naipe == Naipe.DOURADO) {
                fundo = new Color(0xE0, 0xB0, 0x30);
                tinta = new Color(0x22, 0x22, 0x22);
            }

            g.setColor(fundo);
            g.fillRoundRect(x, y, w, h, 10, 10);
            Stroke antigo = g.getStroke();
            g.setStroke(new BasicStroke(1.2f));
            g.setColor(new Color(0x55, 0x55, 0x55));
            g.drawRoundRect(x, y, w, h, 10, 10);
            g.setStroke(antigo);

            g.setColor(tinta);
            if (coringa) {
                g.setFont(getFont().deriveFont(Font.BOLD, h * 0.17f));
                centralizado(g, "COPÃO", x + w / 2, y + h / 2);
            } else {
                g.setFont(getFont().deriveFont(Font.BOLD, h * 0.26f));
                g.drawString(carta.getValor(), x + 5, y + (int) (h * 0.28f));
                g.setFont(getFont().deriveFont(Font.PLAIN, h * 0.42f));
                centralizado(g, naipe.getSimbolo(), x + w / 2, y + (int) (h * 0.66f));
            }
        }

        private void centralizado(Graphics2D g, String texto, int cx, int cy) {
            FontMetrics fm = g.getFontMetrics();
            int x = cx - fm.stringWidth(texto) / 2;
            int y = cy + (fm.getAscent() - fm.getDescent()) / 2;
            g.drawString(texto, x, y);
        }
    }
}

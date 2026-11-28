import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class InterfaceGrafica implements InterfaceJogo {
    private static final Object TRUCO = new Object();

    private static final Color FUNDO = new Color(0x0F, 0x12, 0x15);
    private static final Color PAINEL = new Color(0x17, 0x1B, 0x20);
    private static final Color TEXTO = new Color(0xE8, 0xEC, 0xEF);
    private static final Color DOURADO = new Color(0xFF, 0xD5, 0x4A);

    private final JFrame janela = new JFrame("Zap! - Truco");
    private final JLabel status = new JLabel(" ", SwingConstants.CENTER);
    private final JTextArea log = new JTextArea(4, 40);
    private final BotaoZap btnTruco = new BotaoZap("", new Color(0xE0, 0x7A, 0x1F));
    private final BotaoZap btnAceitar = new BotaoZap("Aceitar", new Color(0x2E, 0x9E, 0x5B));
    private final BotaoZap btnAumentar = new BotaoZap("", new Color(0xC9, 0x93, 0x1A));
    private final BotaoZap btnCorrer = new BotaoZap("Correr", new Color(0xC9, 0x4A, 0x4A));
    private final BotaoZap btnNovamente = new BotaoZap("Jogar novamente", new Color(0x2E, 0x9E, 0x5B));
    private final BotaoZap btnMudar = new BotaoZap("Mudar forma de jogar", new Color(0x3D, 0x7F, 0xD6));
    private final BotaoZap btnSair = new BotaoZap("Sair", new Color(0x5A, 0x63, 0x6E));
    private final MesaPanel mesaPanel = new MesaPanel();

    private final BlockingQueue<Object> entrada = new LinkedBlockingQueue<>();

    private List<Jogador> mesa = new ArrayList<>();
    private Carta cartaPreEscolhida;
    private int humanos;

    public InterfaceGrafica() {
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.getContentPane().setBackground(FUNDO);

        status.setOpaque(true);
        status.setBackground(PAINEL);
        status.setForeground(TEXTO);
        status.setFont(status.getFont().deriveFont(Font.BOLD, 16f));
        status.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.setBackground(PAINEL);
        log.setForeground(new Color(0xB8, 0xC2, 0xCC));
        log.setCaretColor(PAINEL);
        log.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        log.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        JScrollPane rolagem = new JScrollPane(log);
        rolagem.setBorder(BorderFactory.createEmptyBorder());
        rolagem.setPreferredSize(new Dimension(100, 104));

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        botoes.setBackground(FUNDO);
        botoes.setPreferredSize(new Dimension(100, 64));
        botoes.add(btnTruco);
        botoes.add(btnAceitar);
        botoes.add(btnAumentar);
        botoes.add(btnCorrer);
        botoes.add(btnNovamente);
        botoes.add(btnMudar);
        botoes.add(btnSair);

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
        btnNovamente.addActionListener(e -> entrada.offer(AcaoFinal.JOGAR_NOVAMENTE));
        btnMudar.addActionListener(e -> entrada.offer(AcaoFinal.MUDAR_FORMA));
        btnSair.addActionListener(e -> entrada.offer(AcaoFinal.SAIR));
        mesaPanel.aoClicarCarta = carta -> entrada.offer(carta);

        esconderBotoes();
        janela.pack();
        janela.setLocationRelativeTo(null);
        janela.setVisible(true);
    }

    public ConfiguracaoPartida escolherConfiguracao(ConfiguracaoPartida atual) {
        ModoJogo[] modos = ModoJogo.values();
        FormaDeJogar[] formas = FormaDeJogar.values();
        ConfiguracaoPartida[] resultado = {null};

        executarNaTela(() -> {
            JComboBox<String> caixaModo = new JComboBox<>();
            for (ModoJogo modo : modos) {
                caixaModo.addItem(modo.getDescricao());
            }
            JComboBox<String> caixaForma = new JComboBox<>();
            for (FormaDeJogar forma : formas) {
                caixaForma.addItem(forma.getDescricao());
            }
            JTextField campoNome = new JTextField(atual == null ? "Você" : atual.nome(), 18);
            if (atual != null) {
                caixaModo.setSelectedIndex(atual.modo().ordinal());
                caixaForma.setSelectedIndex(atual.forma().ordinal());
            }

            JPanel painel = new JPanel(new GridLayout(0, 1, 0, 6));
            painel.add(new JLabel("Modo de jogo:"));
            painel.add(caixaModo);
            painel.add(new JLabel("Forma de jogar:"));
            painel.add(caixaForma);
            painel.add(new JLabel("Seu nome:"));
            painel.add(campoNome);

            String titulo = atual == null ? "Zap! - Nova partida" : "Mudar forma de jogar";
            int resposta = JOptionPane.showConfirmDialog(janela, painel, titulo,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (resposta == JOptionPane.OK_OPTION) {
                String nome = campoNome.getText().trim();
                if (nome.isEmpty()) {
                    nome = "Você";
                }
                resultado[0] = new ConfiguracaoPartida(modos[caixaModo.getSelectedIndex()],
                        formas[caixaForma.getSelectedIndex()], nome);
            }
        });
        return resultado[0];
    }

    public void definirSequencia(String resumo) {
        SwingUtilities.invokeLater(() -> {
            mesaPanel.resumoSequencia = resumo;
            mesaPanel.repaint();
        });
    }

    public AcaoFinal fimDePartida(boolean venceu, String placar, String linhaSequencia, String linhaGeral) {
        entrada.clear();
        cartaPreEscolhida = null;
        SwingUtilities.invokeLater(() -> {
            esconderBotoes();
            mesaPanel.cartasClicaveis = false;
            mesaPanel.vez = null;
            mesaPanel.ativo = null;
            mesaPanel.maos = new LinkedHashMap<>();
            mesaPanel.fim = new ResultadoFinal(venceu, placar, linhaSequencia, linhaGeral);
            btnNovamente.setVisible(true);
            btnMudar.setVisible(true);
            btnSair.setVisible(true);
            status.setText(venceu ? "Vitória! Jogar novamente ou mudar a forma de jogar?"
                    : "Derrota. Jogar novamente ou mudar a forma de jogar?");
            mesaPanel.repaint();
        });
        while (true) {
            Object o = esperar();
            if (o instanceof AcaoFinal) {
                SwingUtilities.invokeLater(this::esconderBotoes);
                return (AcaoFinal) o;
            }
        }
    }

    @Override
    public Carta lerCarta(Jogador jogador) {
        if (cartaPreEscolhida != null) {
            Carta carta = cartaPreEscolhida;
            cartaPreEscolhida = null;
            return carta;
        }
        entrada.clear();
        SwingUtilities.invokeLater(() -> {
            esconderBotoes();
            mesaPanel.ativo = jogador;
            mesaPanel.cartasClicaveis = true;
            status.setText(chamada(jogador) + ": clique em uma carta.");
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
            mesaPanel.ativo = jogador;
            mesaPanel.cartasClicaveis = true;
            status.setText(chamada(jogador) + ": clique em uma carta ou peça " + proposto + ".");
            mesaPanel.repaint();
        });
        while (true) {
            Object o = esperar();
            if (o == TRUCO) {
                bloquearCartas();
                return true;
            }
            if (o instanceof Carta) {
                cartaPreEscolhida = (Carta) o;
                bloquearCartas();
                return false;
            }
        }
    }

    @Override
    public Resposta lerRespostaAposta(Jogador jogador, NivelAposta proposto) {
        entrada.clear();
        String prefixo = humanos > 1 ? jogador.getNome() + ": " : "";
        SwingUtilities.invokeLater(() -> {
            esconderBotoes();
            mesaPanel.ativo = jogador;
            btnAceitar.setText("Aceitar (" + proposto.getPontos() + " pts)");
            btnAceitar.setVisible(true);
            btnCorrer.setVisible(true);
            if (proposto.temProximo()) {
                btnAumentar.setText("Aumentar para " + proposto.proximo());
                btnAumentar.setVisible(true);
            }
            status.setText(prefixo + "Pediram " + proposto + "! Aceitar, aumentar ou correr?");
            mesaPanel.repaint();
        });
        while (true) {
            Object o = esperar();
            if (o instanceof Resposta) {
                SwingUtilities.invokeLater(() -> {
                    esconderBotoes();
                    mesaPanel.ativo = null;
                    mesaPanel.repaint();
                });
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
        while (escolha[0] < 0) {
            executarNaTela(() -> escolha[0] = JOptionPane.showOptionDialog(
                    janela, texto.toString(), "Mão de dez", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[0]));
        }
        return escolha[0] == 0;
    }

    @Override
    public void definirMaoCega(boolean cega) {
        SwingUtilities.invokeLater(() -> {
            mesaPanel.maoCega = cega;
            mesaPanel.repaint();
        });
    }

    @Override
    public void mostrarMensagem(String mensagem) {
        if (mensagem == null || mensagem.isBlank()) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            log.append(mensagem + "\n");
            log.setCaretPosition(log.getDocument().getLength());
            status.setText(mensagem);
            mesaPanel.repaint();
        });
    }

    @Override
    public void iniciarPartida(List<Jogador> mesa, Time time1, Time time2) {
        this.mesa = new ArrayList<>(mesa);
        this.cartaPreEscolhida = null;
        this.humanos = (int) mesa.stream().filter(Jogador::ehHumano).count();
        entrada.clear();
        List<Jogador> copia = new ArrayList<>(mesa);
        int totalHumanos = this.humanos;
        SwingUtilities.invokeLater(() -> {
            log.setText("");
            esconderBotoes();
            mesaPanel.mesa = copia;
            mesaPanel.time1 = time1;
            mesaPanel.time2 = time2;
            mesaPanel.humanos = totalHumanos;
            mesaPanel.maos = new LinkedHashMap<>();
            mesaPanel.jogadas.clear();
            mesaPanel.vez = null;
            mesaPanel.ativo = null;
            mesaPanel.cartasClicaveis = false;
            mesaPanel.maoCega = false;
            mesaPanel.fim = null;
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

    private String chamada(Jogador jogador) {
        return humanos > 1 ? "Vez de " + jogador.getNome() : "Sua vez";
    }

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

    private void executarNaTela(Runnable acao) {
        try {
            SwingUtilities.invokeAndWait(acao);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Jogo interrompido.", e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getCause());
        }
    }

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
            mesaPanel.ativo = null;
            esconderBotoes();
            mesaPanel.repaint();
        });
    }

    private void esconderBotoes() {
        btnTruco.setVisible(false);
        btnAceitar.setVisible(false);
        btnAumentar.setVisible(false);
        btnCorrer.setVisible(false);
        btnNovamente.setVisible(false);
        btnMudar.setVisible(false);
        btnSair.setVisible(false);
    }

    private record ResultadoFinal(boolean venceu, String placar, String linhaSequencia, String linhaGeral) {
    }

    static class BotaoZap extends JButton {
        private final Color base;
        private boolean sobre;

        BotaoZap(String texto, Color base) {
            super(texto);
            this.base = base;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(getFont().deriveFont(Font.BOLD, 14f));
            setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    sobre = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    sobre = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color cor = getModel().isPressed() ? base.darker() : (sobre ? base.brighter() : base);
            g.setPaint(new GradientPaint(0, 0, cor, 0, getHeight(), cor.darker()));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g.setColor(new Color(255, 255, 255, 60));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g.dispose();
            super.paintComponent(g0);
        }
    }

    static class MesaPanel extends JPanel {
        private static final Color AZUL_TIME = new Color(0x3D, 0x8B, 0xE8);
        private static final Color VERMELHO_TIME = new Color(0xE0, 0x4F, 0x4F);

        private static final int AVATAR_RAIO = 30;
        private static final int CARTA_W_HUMANO = 56, CARTA_H_HUMANO = 80;
        private static final int CARTA_W_OUTRO = 26, CARTA_H_OUTRO = 38;
        private static final int CARTA_W_MESA = 46, CARTA_H_MESA = 66;

        List<Jogador> mesa = new ArrayList<>();
        Map<Jogador, List<Carta>> maos = new LinkedHashMap<>();
        Map<Jogador, Carta> jogadas = new LinkedHashMap<>();
        Jogador vez;
        Jogador ativo;
        Time time1;
        Time time2;
        int humanos;
        boolean cartasClicaveis;
        boolean maoCega;
        String resumoSequencia = "";
        ResultadoFinal fim;
        java.util.function.Consumer<Carta> aoClicarCarta = carta -> { };

        private final List<Rectangle> areas = new ArrayList<>();
        private final List<Carta> cartasDasAreas = new ArrayList<>();
        private int indiceHover = -1;
        private int mesaX, mesaY, mesaW, mesaH;

        MesaPanel() {
            setPreferredSize(new Dimension(900, 660));
            setBackground(FUNDO);
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
                        setCursor(Cursor.getPredefinedCursor(i >= 0 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
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
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            int w = getWidth();
            int h = getHeight();
            g.setPaint(new GradientPaint(0, 0, new Color(0x1B, 0x20, 0x26), 0, h, new Color(0x0C, 0x0F, 0x12)));
            g.fillRect(0, 0, w, h);

            mesaX = 14;
            mesaY = 92;
            mesaW = w - 28;
            mesaH = h - mesaY - 14;

            areas.clear();
            cartasDasAreas.clear();

            desenharMesa(g);
            desenharPlacar(g, w);

            int n = mesa.size();
            for (int i = 0; i < n; i++) {
                desenharAssento(g, mesa.get(i), i, n);
            }
            for (Map.Entry<Jogador, Carta> jogada : jogadas.entrySet()) {
                int i = mesa.indexOf(jogada.getKey());
                if (i >= 0) {
                    desenharCartaJogada(g, jogada.getKey(), jogada.getValue(), i, n);
                }
            }
            if (fim != null) {
                desenharFim(g, w, h);
            }
            g.dispose();
        }

        private double[] posicaoAssento(int i, int n) {
            double ang = Math.PI / 2 - i * 2 * Math.PI / n;
            double cx = mesaX + mesaW / 2.0;
            double cy = mesaY + mesaH / 2.0;
            return new double[] {
                cx + mesaW * 0.36 * Math.cos(ang),
                cy + mesaH * 0.34 * Math.sin(ang),
                Math.cos(ang)
            };
        }

        private void desenharMesa(Graphics2D g) {
            int arco = 110;
            int borda = 14;
            g.setColor(new Color(0, 0, 0, 90));
            g.fillRoundRect(mesaX + 4, mesaY + 7, mesaW, mesaH, arco, arco);
            g.setPaint(new GradientPaint(mesaX, mesaY, new Color(0x80, 0x52, 0x2A),
                    mesaX, mesaY + mesaH, new Color(0x3E, 0x24, 0x10)));
            g.fillRoundRect(mesaX, mesaY, mesaW, mesaH, arco, arco);

            Point2D centro = new Point2D.Double(mesaX + mesaW / 2.0, mesaY + mesaH / 2.0);
            g.setPaint(new RadialGradientPaint(centro, (float) (Math.max(mesaW, mesaH) * 0.62),
                    new float[] {0f, 1f},
                    new Color[] {new Color(0x31, 0x98, 0x55), new Color(0x12, 0x56, 0x2B)}));
            g.fillRoundRect(mesaX + borda, mesaY + borda, mesaW - 2 * borda, mesaH - 2 * borda, arco - borda, arco - borda);

            g.setStroke(new BasicStroke(2f));
            g.setColor(new Color(0, 0, 0, 80));
            g.drawRoundRect(mesaX + borda, mesaY + borda, mesaW - 2 * borda, mesaH - 2 * borda, arco - borda, arco - borda);
            g.setStroke(new BasicStroke(1.5f));
            g.setColor(new Color(255, 255, 255, 28));
            g.drawRoundRect(mesaX + borda + 16, mesaY + borda + 16, mesaW - 2 * borda - 32,
                    mesaH - 2 * borda - 32, arco - 44, arco - 44);

            g.setFont(getFont().deriveFont(Font.BOLD, 72f));
            g.setColor(new Color(255, 255, 255, 22));
            centralizado(g, "ZAP!", (int) centro.getX(), (int) centro.getY());
        }

        private void desenharPlacar(Graphics2D g, int w) {
            if (time1 == null || time2 == null) {
                return;
            }
            int lado = 170;
            int meio = 150;
            int altura = 44;
            int total = 2 * lado + meio;
            int x = (w - total) / 2;
            int y = 16;

            Shape pilula = new RoundRectangle2D.Double(x, y, total, altura, altura, altura);
            Graphics2D c = (Graphics2D) g.create();
            c.clip(pilula);
            c.setColor(AZUL_TIME);
            c.fillRect(x, y, lado, altura);
            c.setColor(new Color(0x12, 0x15, 0x19));
            c.fillRect(x + lado, y, meio, altura);
            c.setColor(VERMELHO_TIME);
            c.fillRect(x + lado + meio, y, lado, altura);
            c.dispose();
            g.setColor(new Color(255, 255, 255, 70));
            g.setStroke(new BasicStroke(1.5f));
            g.draw(pilula);

            g.setColor(Color.WHITE);
            g.setFont(getFont().deriveFont(Font.BOLD, 14f));
            centralizado(g, time1.getNome(), x + lado / 2, y + altura / 2);
            centralizado(g, time2.getNome(), x + lado + meio + lado / 2, y + altura / 2);
            g.setFont(getFont().deriveFont(Font.BOLD, 22f));
            centralizado(g, time1.getPontos() + "  x  " + time2.getPontos(), x + lado + meio / 2, y + altura / 2);

            if (resumoSequencia != null && !resumoSequencia.isEmpty()) {
                g.setFont(getFont().deriveFont(Font.BOLD, 13f));
                g.setColor(DOURADO);
                centralizado(g, resumoSequencia, w / 2, y + altura + 17);
            }
        }

        private boolean vePropriaMao(Jogador jogador) {
            return jogador.ehHumano() && (humanos <= 1 || jogador == ativo);
        }

        private void desenharAssento(Graphics2D g, Jogador jogador, int i, int n) {
            double[] p = posicaoAssento(i, n);
            int x = (int) p[0];
            int y = (int) p[1];

            Color corTime = (time1 != null && time1.temJogador(jogador)) ? AZUL_TIME : VERMELHO_TIME;
            if (jogador == vez) {
                for (int k = 4; k >= 1; k--) {
                    int r = AVATAR_RAIO + 3 + k * 4;
                    g.setColor(new Color(0xFF, 0xD5, 0x4A, 34));
                    g.fillOval(x - r, y - r, 2 * r, 2 * r);
                }
            }
            g.setColor(new Color(0, 0, 0, 80));
            g.fillOval(x - AVATAR_RAIO - 3 + 2, y - AVATAR_RAIO - 3 + 4, 2 * (AVATAR_RAIO + 3), 2 * (AVATAR_RAIO + 3));
            g.setColor(jogador == vez ? DOURADO : corTime);
            g.fillOval(x - AVATAR_RAIO - 3, y - AVATAR_RAIO - 3, 2 * (AVATAR_RAIO + 3), 2 * (AVATAR_RAIO + 3));
            g.setPaint(new GradientPaint(x, y - AVATAR_RAIO, corTime.brighter(), x, y + AVATAR_RAIO, corTime.darker()));
            g.fillOval(x - AVATAR_RAIO, y - AVATAR_RAIO, 2 * AVATAR_RAIO, 2 * AVATAR_RAIO);

            String nome = jogador.getNome();
            g.setColor(Color.WHITE);
            g.setFont(getFont().deriveFont(Font.BOLD, 26f));
            centralizado(g, nome.isEmpty() ? "?" : nome.substring(0, 1).toUpperCase(), x, y);

            g.setFont(getFont().deriveFont(Font.BOLD, 13f));
            FontMetrics fm = g.getFontMetrics();
            int larguraNome = fm.stringWidth(nome) + 20;
            int yNome = y + AVATAR_RAIO + 22;
            g.setColor(new Color(0, 0, 0, 140));
            g.fillRoundRect(x - larguraNome / 2, yNome - 11, larguraNome, 22, 22, 22);
            g.setColor(Color.WHITE);
            centralizado(g, nome, x, yNome);

            List<Carta> mao = maos.getOrDefault(jogador, new ArrayList<>());
            boolean humano = jogador.ehHumano();
            boolean visivel = vePropriaMao(jogador);
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
                if (visivel) {
                    int indiceGlobal = areas.size();
                    if (cartasClicaveis && indiceGlobal == indiceHover) {
                        cy -= 10;
                    }
                    areas.add(new Rectangle(cx, cy, cw, ch));
                    cartasDasAreas.add(mao.get(k));
                    if (maoCega) {
                        desenharVerso(g, cx, cy, cw, ch);
                    } else {
                        desenharFrente(g, mao.get(k), cx, cy, cw, ch);
                    }
                } else {
                    desenharVerso(g, cx, cy, cw, ch);
                }
            }
        }

        private void desenharCartaJogada(Graphics2D g, Jogador jogador, Carta carta, int i, int n) {
            double[] p = posicaoAssento(i, n);
            double centroX = mesaX + mesaW / 2.0;
            double centroY = mesaY + mesaH / 2.0;
            double cx = centroX + (p[0] - centroX) * 0.42;
            double cy = centroY + (p[1] - centroY) * 0.42;
            int x = (int) cx - CARTA_W_MESA / 2;
            int y = (int) cy - CARTA_H_MESA / 2;
            desenharFrente(g, carta, x, y, CARTA_W_MESA, CARTA_H_MESA);

            g.setFont(getFont().deriveFont(Font.BOLD, 11f));
            FontMetrics fm = g.getFontMetrics();
            int larguraNome = fm.stringWidth(jogador.getNome()) + 14;
            int yNome = y + CARTA_H_MESA + 14;
            g.setColor(new Color(0, 0, 0, 130));
            g.fillRoundRect((int) cx - larguraNome / 2, yNome - 9, larguraNome, 18, 18, 18);
            g.setColor(Color.WHITE);
            centralizado(g, jogador.getNome(), (int) cx, yNome);
        }

        private void desenharVerso(Graphics2D g, int x, int y, int w, int h) {
            g.setColor(new Color(0, 0, 0, 70));
            g.fillRoundRect(x + 2, y + 3, w, h, 10, 10);
            g.setPaint(new GradientPaint(x, y, new Color(0x2F, 0x52, 0xB0), x, y + h, new Color(0x16, 0x2A, 0x66)));
            g.fillRoundRect(x, y, w, h, 10, 10);
            g.setStroke(new BasicStroke(1.2f));
            g.setColor(new Color(255, 255, 255, 200));
            g.drawRoundRect(x, y, w, h, 10, 10);

            Shape clipAntigo = g.getClip();
            g.clip(new RoundRectangle2D.Double(x + 3, y + 3, w - 6, h - 6, 6, 6));
            g.setColor(new Color(255, 255, 255, 55));
            g.setStroke(new BasicStroke(1f));
            for (int k = -h; k < w; k += 6) {
                g.drawLine(x + k, y + h, x + k + h, y);
                g.drawLine(x + k, y, x + k + h, y + h);
            }
            g.setClip(clipAntigo);
            g.setColor(new Color(255, 255, 255, 120));
            g.drawRoundRect(x + 3, y + 3, w - 6, h - 6, 6, 6);
        }

        private void desenharFrente(Graphics2D g, Carta carta, int x, int y, int w, int h) {
            Naipe naipe = carta.getNaipe();
            boolean coringa = naipe == Naipe.PRETO || naipe == Naipe.DOURADO;
            boolean vermelho = naipe == Naipe.COPAS || naipe == Naipe.OUROS;

            Color topo = Color.WHITE;
            Color base = new Color(0xEC, 0xEC, 0xEC);
            Color tinta = vermelho ? new Color(0xC6, 0x28, 0x28) : new Color(0x21, 0x21, 0x21);
            if (naipe == Naipe.PRETO) {
                topo = new Color(0x3A, 0x3A, 0x3A);
                base = new Color(0x14, 0x14, 0x14);
                tinta = Color.WHITE;
            } else if (naipe == Naipe.DOURADO) {
                topo = new Color(0xF2, 0xCB, 0x5A);
                base = new Color(0xC9, 0x97, 0x1E);
                tinta = new Color(0x22, 0x22, 0x22);
            }

            g.setColor(new Color(0, 0, 0, 80));
            g.fillRoundRect(x + 2, y + 3, w, h, 10, 10);
            g.setPaint(new GradientPaint(x, y, topo, x, y + h, base));
            g.fillRoundRect(x, y, w, h, 10, 10);
            g.setStroke(new BasicStroke(1.2f));
            g.setColor(new Color(0x55, 0x55, 0x55));
            g.drawRoundRect(x, y, w, h, 10, 10);

            g.setColor(tinta);
            if (coringa) {
                g.setFont(getFont().deriveFont(Font.BOLD, h * 0.17f));
                centralizado(g, "COPÃO", x + w / 2, y + h / 2);
                return;
            }
            Font fonteValor = getFont().deriveFont(Font.BOLD, h * 0.26f);
            g.setFont(fonteValor);
            g.drawString(carta.getValor(), x + 5, y + (int) (h * 0.28f));
            Graphics2D girado = (Graphics2D) g.create();
            girado.rotate(Math.PI, x + w / 2.0, y + h / 2.0);
            girado.setFont(fonteValor);
            girado.drawString(carta.getValor(), x + 5, y + (int) (h * 0.28f));
            girado.dispose();
            g.setFont(getFont().deriveFont(Font.PLAIN, h * 0.42f));
            centralizado(g, naipe.getSimbolo(), x + w / 2, y + (int) (h * 0.62f));
        }

        private void desenharFim(Graphics2D g, int w, int h) {
            g.setColor(new Color(0, 0, 0, 160));
            g.fillRect(0, 0, w, h);

            int cw = 560;
            int ch = 250;
            int x = (w - cw) / 2;
            int y = (h - ch) / 2 - 20;
            Color cor = fim.venceu() ? new Color(0x2E, 0x9E, 0x5B) : new Color(0xC9, 0x4A, 0x4A);
            Color corTitulo = fim.venceu() ? new Color(0x6F, 0xE0, 0x9A) : new Color(0xFF, 0x86, 0x86);

            g.setColor(new Color(0, 0, 0, 100));
            g.fillRoundRect(x + 4, y + 8, cw, ch, 32, 32);
            g.setPaint(new GradientPaint(x, y, new Color(0x24, 0x2A, 0x31), x, y + ch, new Color(0x15, 0x19, 0x1E)));
            g.fillRoundRect(x, y, cw, ch, 32, 32);
            g.setStroke(new BasicStroke(3f));
            g.setColor(cor);
            g.drawRoundRect(x, y, cw, ch, 32, 32);

            int cx = w / 2;
            g.setColor(corTitulo);
            g.setFont(getFont().deriveFont(Font.BOLD, 48f));
            centralizado(g, fim.venceu() ? "VITÓRIA!" : "DERROTA", cx, y + 56);

            g.setColor(Color.WHITE);
            g.setFont(getFont().deriveFont(Font.BOLD, 20f));
            centralizado(g, fim.placar(), cx, y + 108);

            g.setColor(DOURADO);
            g.setFont(getFont().deriveFont(Font.BOLD, 17f));
            centralizado(g, "★ " + fim.linhaSequencia(), cx, y + 150);

            g.setColor(new Color(0xB8, 0xC2, 0xCC));
            g.setFont(getFont().deriveFont(Font.PLAIN, 15f));
            centralizado(g, fim.linhaGeral(), cx, y + 180);

            g.setColor(new Color(0x8A, 0x95, 0xA0));
            g.setFont(getFont().deriveFont(Font.PLAIN, 13f));
            centralizado(g, "Escolha abaixo: jogar novamente ou mudar a forma de jogar.", cx, y + 216);
        }

        private void centralizado(Graphics2D g, String texto, int cx, int cy) {
            FontMetrics fm = g.getFontMetrics();
            int x = cx - fm.stringWidth(texto) / 2;
            int y = cy + (fm.getAscent() - fm.getDescent()) / 2;
            g.drawString(texto, x, y);
        }
    }
}

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Interface extends JFrame {
    private Processador processador;

    private JTextArea editorCodigo;
    private JTextArea logExecucao;
    private JTable tabelaRegistradores;
    private DefaultTableModel modeloRegs;
    private JTable tabelaMemoria;
    private DefaultTableModel modeloMem;
    private JLabel lblStatus;

    public Interface() {
        this.processador = new Processador();
        configurarJanela();
        inicializarComponentes();
        atualizarTela();
    }

    private void configurarJanela() {
        setTitle("Simulador de Processador Microprogramado (MIC-1)");
        setSize(1250, 740);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
    }

    private void inicializarComponentes() {
        // --- BARRA SUPERIOR ---
        JPanel painelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        painelSuperior.setBackground(new Color(33, 37, 43));

        JButton btnCarregar = new JButton("1. Montar & Carregar");
        JButton btnPasso = new JButton("2. Próximo Ciclo (Step)");
        JButton btnReset = new JButton("Resetar");

        lblStatus = new JLabel("Ciclos: 0  |  MPC: 0");
        lblStatus.setForeground(Color.WHITE);
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 14));

        painelSuperior.add(btnCarregar);
        painelSuperior.add(btnPasso);
        painelSuperior.add(btnReset);
        painelSuperior.add(Box.createHorizontalStrut(30));
        painelSuperior.add(lblStatus);

        add(painelSuperior, BorderLayout.NORTH);

        // --- PAINEL CENTRAL (3 COLUNAS) ---
        JPanel painelCentral = new JPanel(new GridLayout(1, 3, 10, 10));
        painelCentral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Coluna 1: Editor de Microcódigo (formato compatível com o Montador.java)
        JPanel colCodigo = new JPanel(new BorderLayout());
        colCodigo.setBorder(BorderFactory.createTitledBorder("Microprograma (Formato: CAMPO=valor)"));
        String exemploMicrocodigo =
            "// Exemplo de Microcódigo para o Montador\n" +
            "inicio: AMUX=0 ALU=0 C=1 ADDR=1\n" +
            "loop:   AMUX=1 ALU=2 SH=0 C=2 ADDR=inicio\n";
        editorCodigo = new JTextArea(exemploMicrocodigo);
        editorCodigo.setFont(new Font("Monospaced", Font.PLAIN, 14));
        colCodigo.add(new JScrollPane(editorCodigo), BorderLayout.CENTER);

        // Coluna 2: Banco de Registradores
        JPanel colRegs = new JPanel(new BorderLayout());
        colRegs.setBorder(BorderFactory.createTitledBorder("Banco de Registradores"));
        modeloRegs = new DefaultTableModel(new Object[]{"Registrador", "Decimal", "Hex"}, 0);
        tabelaRegistradores = new JTable(modeloRegs);
        tabelaRegistradores.setFont(new Font("Monospaced", Font.PLAIN, 13));
        tabelaRegistradores.setRowHeight(24);
        colRegs.add(new JScrollPane(tabelaRegistradores), BorderLayout.CENTER);

        // Coluna 3: Memória de Controle & Log
        JSplitPane colDireita = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        colDireita.setResizeWeight(0.5);

        JPanel painelMem = new JPanel(new BorderLayout());
        painelMem.setBorder(BorderFactory.createTitledBorder("Memória de Controle (Microinstruções)"));
        modeloMem = new DefaultTableModel(new Object[]{"Endereço", "Hex (32-bit)", "Binário"}, 0);
        tabelaMemoria = new JTable(modeloMem);
        tabelaMemoria.setFont(new Font("Monospaced", Font.PLAIN, 12));
        painelMem.add(new JScrollPane(tabelaMemoria), BorderLayout.CENTER);

        JPanel painelLog = new JPanel(new BorderLayout());
        painelLog.setBorder(BorderFactory.createTitledBorder("Log de Execução (Unidade de Controle)"));
        logExecucao = new JTextArea();
        logExecucao.setEditable(false);
        logExecucao.setFont(new Font("Monospaced", Font.PLAIN, 12));
        painelLog.add(new JScrollPane(logExecucao), BorderLayout.CENTER);

        colDireita.setTopComponent(painelMem);
        colDireita.setBottomComponent(painelLog);

        painelCentral.add(colCodigo);
        painelCentral.add(colRegs);
        painelCentral.add(colDireita);

        add(painelCentral, BorderLayout.CENTER);

        // --- AÇÕES DOS BOTÕES ---
        btnCarregar.addActionListener(e -> {
            try {
                processador.carregarMicroprograma(editorCodigo.getText());
                logExecucao.setText("Microprograma montado e carregado com sucesso!\n");
                atualizarTela();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao montar: " + ex.getMessage(), "Erro de Sintaxe", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPasso.addActionListener(e -> {
            String log = processador.executarCiclo();
            logExecucao.append(log);
            atualizarTela();
        });

        btnReset.addActionListener(e -> {
            processador.resetar();
            logExecucao.setText("Processador reiniciado.\n");
            atualizarTela();
        });
    }

    private void atualizarTela() {
        lblStatus.setText("Ciclos: " + processador.getCiclosExecutados() + "  |  MPC: " + processador.getMpc().getValor());

        // Atualiza tabela de registradores lendo reg.saida()
        modeloRegs.setRowCount(0);
        processador.getRegistradores().forEach((nome, reg) -> {
            int valor = reg.saida();
            modeloRegs.addRow(new Object[]{nome, valor, String.format("0x%08X", valor)});
        });

        // Atualiza visualização das primeiras 32 posições da Memória de Controle
        modeloMem.setRowCount(0);
        for (int i = 0; i < 32; i++) {
            int instrucao = processador.getMemoriaControle().leitura(i);
            String bin = String.format("%32s", Integer.toBinaryString(instrucao)).replace(' ', '0');
            modeloMem.addRow(new Object[]{
                String.format("0x%02X (%d)", i, i),
                String.format("0x%08X", instrucao),
                bin
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Interface().setVisible(true);
        });
    }
}
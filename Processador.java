import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class Processador {
    private UnidadeControle uc;
    private MemoriaControle memControle;
    private Memoria memoriaPrincipal;
    private Cache cache;
    private ULA ula;
    private Deslocador deslocador;
    private AMux amux;
    private Multiplexador mux;
    private Montador montador;

    private MIR mir;
    private MPC mpc;
    private MAR mar;
    private MBR mbr;
    private Latch latchA;
    private Latch latchB;

    private Map<String, Registrador> registradores;
    private int ciclosExecutados;
    private boolean finalizado;

    public Processador() {
        inicializarComponentes(new int[256]);
    }

    private void inicializarComponentes(int[] microprograma) {
        // 1. Memória Principal (256 posições) e Cache (8 linhas, bloco de 4)
        this.memoriaPrincipal = new Memoria(256);
        this.cache = new Cache(memoriaPrincipal, 8, 4);

        // 2. Unidade de Controle e Memória de Controle (256 microinstruções)
        this.memControle = new MemoriaControle(microprograma);
        this.mir = new MIR();
        this.mpc = new MPC();
        this.uc = new UnidadeControle(mpc, mir, memControle);

        // 3. Caminho de Dados (Datapath)
        this.ula = new ULA();
        this.deslocador = new Deslocador();
        this.amux = new AMux();
        this.mux = new Multiplexador();
        this.mar = new MAR();
        this.mbr = new MBR();
        this.latchA = new Latch(0);
        this.latchB = new Latch(0);
        this.montador = new Montador();

        // 4. Banco de Registradores (ordem fixa para exibição na tabela)
        this.registradores = new LinkedHashMap<>();
        String[] nomesRegs = {"PC", "AC", "SP", "IR", "TIR", "0", "+1", "-1", "AMASK", "SMASK", "A", "B", "C", "D", "E", "F"};
        for (String nome : nomesRegs) {
            registradores.put(nome, new Registrador());
        }

        // Inicializa constantes padrão do MIC-1 se existirem no banco
        registradores.get("0").carregar(0);
        registradores.get("+1").carregar(1);
        registradores.get("-1").carregar(-1);
        registradores.get("AMASK").carregar(0x0FFF);
        registradores.get("SMASK").carregar(0x00FF);

        this.ciclosExecutados = 0;
        this.finalizado = false;
    }

    /**
     * Recebe o texto do editor da interface, salva em um arquivo temporário,
     * usa o Montador do projeto para gerar o microcódigo e carrega na MemoriaControle.
     */
    public void carregarMicroprograma(String codigoMicroinstrucoes) throws IOException {
        Path tempFile = Files.createTempFile("microprograma_", ".txt");
        Files.writeString(tempFile, codigoMicroinstrucoes);

        this.montador = new Montador();
        int[] microprogramaMontado = montador.montar(tempFile.toString());
        Files.deleteIfExists(tempFile);

        inicializarComponentes(microprogramaMontado);
    }

    /**
     * Executa 1 ciclo de clock da Unidade de Controle e registra o estado.
     */
    public String executarCiclo() {
        if (finalizado) return "Execução já finalizada.\n";

        int mpcAtual = mpc.getValor();
        int microinstrucao = memControle.leitura(mpcAtual);

        // Executa busca e decodificação na UnidadeControle do projeto
        uc.ciclo();

        ciclosExecutados++;

        return String.format(
            "Ciclo %d | MPC: %d (0x%02X) | MIR: 0x%08X | ALU=%d SH=%d AMUX=%d COND=%d ADDR=%d\n",
            ciclosExecutados,
            mpcAtual, mpcAtual,
            microinstrucao,
            mir.getALU(0), mir.getSH(0), mir.getAMUX(0), mir.getCOND(0), mir.getADDR(0)
        );
    }

    public void resetar() {
        inicializarComponentes(new int[256]);
    }

    // Getters para a Interface Gráfica
    public Map<String, Registrador> getRegistradores() { return registradores; }
    public int getCiclosExecutados() { return ciclosExecutados; }
    public Memoria getMemoriaPrincipal() { return memoriaPrincipal; }
    public MemoriaControle getMemoriaControle() { return memControle; }
    public MPC getMpc() { return mpc; }
    public MIR getMir() { return mir; }
}
public class BancoRegistradores {

    public static final int PC = 0;
    public static final int AC = 1;
    public static final int SP = 2;
    public static final int IR = 3;
    public static final int TIR = 4;
    public static final int ZERO = 5;
    public static final int MAIS_UM = 6;
    public static final int MENOS_UM = 7;
    public static final int AMASK = 8;
    public static final int SMASK = 9;

    // Objetos do registrador
    private final Registrador[] regs;

    public BancoRegistradores() {
        this.regs = new Registrador[16];

        for (int i = 0; i < 16; i++) {
            this.regs[i] = new Registrador();
        }

        this.regs[ZERO].carregar(0x0000);      // 0
        this.regs[MAIS_UM].carregar(0x0001);   // +1
        this.regs[MENOS_UM].carregar(0xFFFF);  // -1 em complemento de 2 (16 bits)
        this.regs[AMASK].carregar(0x0FFF);     // máscara de endereço (12 bits)
        this.regs[SMASK].carregar(0x00FF);     // máscara de byte (8 bits)
    }

    public int ler(int indice) {
        if (indice < 0 || indice > 15) {
            throw new IllegalArgumentException("Registrador inexistente: " + indice);
        }
        // Valor com no máximo 16 bits
        return this.regs[indice].saida() & 0xFFFF;
    }

    public void escrever(int indice, int valor, int sinalENC) {
        if (indice < 0 || indice > 15) {
            throw new IllegalArgumentException("Registrador inexistente: " + indice);
        }

        if (sinalENC == 1 && (indice < 5 || indice > 9)) {
            this.regs[indice].carregar(valor & 0xFFFF);
        }
    }

    // Metodo so para imprimir na tela e testar!!!!
    public void imprimirEstado() {
        String[] nomes = {
            "PC", "AC", "SP", "IR", "TIR",
            "0", "+1", "-1", "AMASK", "SMASK",
            "A", "B", "C", "D", "E", "F"
        };
        System.out.println("=== BANCO DE REGISTRADORES ===");
        for (int i = 0; i < 16; i++) {
            int val = ler(i);
            System.out.printf("[%2d] %-5s = 0x%04X (%d)%n", i, nomes[i], val, val);
        }
    }
}

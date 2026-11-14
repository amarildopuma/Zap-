public enum Naipe {
    PAUS("♣"),
    ESPADAS("♠"),
    COPAS("♥"),
    OUROS("♦"),
    PRETO("(preto)"),
    DOURADO("(dourado)");

    private final String simbolo;

    Naipe(String simbolo){
        this.simbolo = simbolo;
    }
    public String getSimbolo(){
        return simbolo;
    }
}

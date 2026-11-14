public enum NivelAposta {
    SEM_APOSTA(2),
    TRUCO(4),
    SEIS(8),
    NOVE(10),
    DOZE(12);

    private final int pontos;

    NivelAposta(int pontos){
        this.pontos=pontos;
    }

    public int getPontos(){
        return pontos;
    }

    public boolean temProximo(){
        return this != DOZE;
    }

    public NivelAposta proximo(){
        if (!temProximo()) {
            throw new IllegalStateException("Não existe nivel depois do "+ this);
        }
        return values()[ordinal()+1];
    }
}

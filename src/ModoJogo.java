public enum ModoJogo {
    TRUCO_MINEIRO("Truco Mineiro (2 contra 2)"),
    DOURADINHA("Douradinha (3 contra 3, com coringas)");

    private final String descricao;

    ModoJogo(String descricao){
        this.descricao=descricao;
    }

    public String getDescricao(){
        return descricao;
    }

    public Variante criarVariante(){
        switch (this) {
            case DOURADINHA:
                return new Douradinha();
            case TRUCO_MINEIRO:
            default:
                return new TrucoMineiro();
        }
    }
}

public enum FormaDeJogar {
    PARCEIRO_BOT("Parceiro bot (você e bots contra bots)"),
    PARCEIRO_HUMANO("Parceiro humano (mesma tela, contra bots)");

    private final String descricao;

    FormaDeJogar(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}

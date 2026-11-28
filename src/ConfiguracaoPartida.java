public record ConfiguracaoPartida(ModoJogo modo, FormaDeJogar forma, String nome) {
    public boolean mesmaForma(ConfiguracaoPartida outra) {
        return outra != null && modo == outra.modo && forma == outra.forma;
    }
}
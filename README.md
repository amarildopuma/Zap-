# Zap! — Truco em Java

Trabalho Prático de **Laboratório de Programação Orientada a Objetos**
Professor: Guilherme Soares

## Integrantes

- Victor Gabriel Fernandes Ferraz
- Guilherme da Costa Marra
- Amarildo Pereira de Souza Junior
- Lucas Domingos Pereira

## Descrição

**Zap!** é um jogo de truco com duas variantes de regras, jogável no **terminal** ou em **interface gráfica** (Swing):

- **Truco Mineiro:** 2 jogadores por time (4 no total), baralho de 40 cartas.
- **Douradinha:** 3 jogadores por time (6 no total), baralho de 40 cartas mais 2 coringas (os copões).

O nome vem do *zap*, o 4 de paus, que é manilha nas duas variantes.

Os adversários e parceiros controlados pelo computador decidem jogadas e apostas simulando partidas possíveis a partir das cartas que enxergam (classe `Estrategia`).

## Regras básicas

**Objetivo.** Dois times disputam uma partida e vence o primeiro a chegar a **12 pontos ou mais**. Como as rodadas valem 2, 4, 8, 10 ou 12 pontos, o placar final pode passar de 12 (por exemplo, 14 x 6).

**Rodada.** Cada jogador recebe 3 cartas e a rodada tem até 3 vazas. Em cada vaza todos jogam uma carta, alternando entre os times. A carta mais forte vence a vaza, e quem a jogou abre a próxima. O time que vencer 2 vazas leva a rodada. Quem abre a primeira vaza muda a cada rodada.

**Empates.**
- Se a 1ª vaza empatar, quem vencer a 2ª leva a rodada.
- Se a 1ª tiver vencedor e a 2ª empatar, vale o vencedor da 1ª.
- Se as duas primeiras empatarem, a 3ª decide.
- Se todas empatarem, ganha o time de quem abriu a rodada.
- Cartas de mesma força jogadas por parceiros não empatam: o time ganha a vaza.

**Apostas.** Antes de jogar sua carta, o jogador pode pedir **Truco** e, depois, **Seis**, **Nove** e **Doze**. Quem responde é um adversário, que pode aceitar, aumentar ou correr. Só pode aumentar o time que não fez o último pedido. Se correr, o time que pediu leva o valor do nível anterior.

| Situação | Pontos |
|---|---|
| Rodada sem aposta | 2 |
| Truco | 4 |
| Seis | 8 |
| Nove | 10 |
| Doze | 12 |

**Mão de dez.** Quando só um time tem 10 pontos, ele escolhe entre jogar a rodada (que passa a valer 4 pontos e não admite truco) ou correr, dando 2 pontos ao adversário.

**Mão de ferro.** Quando os dois times têm 10 pontos, a rodada vale 4, é jogada sem ver as cartas e não admite truco.

**Força das cartas** (da mais forte para a mais fraca):

- **Truco Mineiro:** 4♣ (zap) > 7♥ > A♠ (espadilha) > 7♦ > 3 > 2 > A > K > J > Q > 7 > 6 > 5 > 4
- **Douradinha:** copão preto > copão dourado > 7♣ (7 rato) > 4♠ (catatau) > 4♣ (zap) > 7♥ > A♠ > 7♦ > 3 > 2 > A > K > J > Q > 7 > 6 > 5 > 4

### Simplificações

- Não há sinais nem conversa entre parceiros, e ninguém vê a mão do outro (exceto os parceiros na mão de dez).
- Com vários humanos no terminal, todos jogam no mesmo teclado. Antes de mostrar cada mão, o jogo limpa a tela e pede para passar o teclado.
- Não há "mão de onze"; a regra equivalente aqui é a mão de dez.
- Os jogadores do computador não combinam jogadas com o parceiro.

## Modos de jogo

| Interface | Opções |
|---|---|
| **Terminal** (`Main`) | Todos humanos, ou humanos (time 1) contra o computador (time 2), nas duas variantes. |
| **Gráfica** (`MainGrafica`) | Você contra o computador, nas duas variantes, com parceiro controlado pelo computador ou por outro humano na mesma tela. Mantém a contagem de vitórias, derrotas e sequência. |

## Como compilar e executar

**Requisito:** JDK **21 ou superior** (o código usa `List.getFirst()` e `record`). Confira com `java -version` e `javac -version`.

O código-fonte está em `src/`, sem pacotes. Na raiz do projeto:

```bash
# compilar (a pasta out/ é criada pelo javac)
javac -encoding UTF-8 -d out src/*.java

# versão de terminal
java -cp out Main

# versão com interface gráfica
java -cp out MainGrafica
```

No Windows, para os naipes (♣ ♠ ♥ ♦) aparecerem corretamente no terminal, rode `chcp 65001` antes de executar.

Também é possível abrir a pasta no IntelliJ IDEA (JDK 21) e executar `Main` ou `MainGrafica`.

## Estrutura do projeto

```
Zap!/
├── src/          código-fonte
├── README.md
└── docs/         diagrama de classes final
```

### Principais classes

| Classe | Responsabilidade |
|---|---|
| `Main`, `MainGrafica` | Pontos de entrada (terminal e gráfica). Só criam a interface e iniciam o jogo. |
| `Menu` | Menu do terminal: lê as escolhas, monta times e mesa e inicia a `Partida`. |
| `ModoJogo` (enum) | As duas variantes (Truco Mineiro e Douradinha) e a criação da `Variante` correspondente. |
| `FormaDeJogar` (enum), `ConfiguracaoPartida` (record) | Escolhas da interface gráfica: variante, parceiro humano ou bot e nome do jogador. |
| `Variante` (abstrata), `TrucoMineiro`, `Douradinha` | Ordem de força das cartas, jogadores por time e presença de coringas. |
| `Carta`, `Naipe` (enum) | Valor e naipe (ou cor, nos coringas). A carta não conhece a própria força, que depende da variante. |
| `Baralho` | Cria as cartas conforme a variante (40 ou 42), embaralha e distribui. |
| `Jogador` (abstrata), `JogadorHumano`, `JogadorComputador` | Guardam a mão e decidem carta, pedido de truco, resposta a aposta e mão de dez. O humano pergunta à interface; o computador usa a `Estrategia`. |
| `Estrategia` | Decisões do computador: simula distribuições possíveis das cartas ocultas para escolher a carta e estimar a chance de vencer. |
| `SituacaoRodada` (record) | Retrato da rodada que o computador recebe para decidir (placar, vazas, mesa, nível da aposta). |
| `Time` | Agrupa os jogadores e guarda os pontos. |
| `Vaza` | Guarda as cartas jogadas e decide vencedor ou empate usando a `Variante`. |
| `Rodada` | Controla ordem de jogada, vazas, apostas, mão de dez/ferro e o time vencedor. |
| `Partida` | Encadeia as rodadas, alterna quem abre e verifica o fim do jogo. |
| `NivelAposta`, `Resposta` (enums) | Níveis 2, 4, 8, 10 e 12 (com `proximo()`) e respostas aceitar, aumentar ou correr. |
| `InterfaceJogo` | Contrato de toda a entrada e saída usada pelas regras. |
| `InterfaceTerminal`, `InterfaceGrafica` | Implementações em terminal e em Swing. As regras não conhecem nenhuma das duas. |
| `Sequencia`, `AcaoFinal` (enum) | Placar de vitórias e derrotas e escolha do que fazer ao fim da partida (interface gráfica). |
| `TrucoException`, `JogadaInvalidaException`, `ApostaInvalidaException` | Jogada com carta fora da mão ou repetida na vaza; aumento de aposta acima de Doze. |

## Conceitos de POO aplicados

- **Encapsulamento:** a mão do jogador e os pontos do time são privados. A mão só muda por `receberCartas` e `jogarCarta`, e os pontos só por `adicionarPontos`. Os getters devolvem cópias das listas.
- **Composição:** `Partida` cria e controla as `Rodada`s; cada `Rodada` cria seu `Baralho` e suas `Vaza`s; o `Baralho` possui as `Carta`s.
- **Agregação:** `Partida` recebe prontos os `Time`s, a `Variante` e a interface; `Time` agrega `Jogador`es. Esses objetos são criados fora dela, por `Menu` ou `MainGrafica`.
- **Herança:** `Variante` → `TrucoMineiro`/`Douradinha`; `Jogador` → `JogadorHumano`/`JogadorComputador`; `TrucoException` → exceções específicas.
- **Polimorfismo:** `Partida`, `Rodada`, `Vaza` e `Baralho` trabalham só com `Variante`, `Jogador` e `InterfaceJogo`. Trocar de variante, de tipo de jogador ou de terminal para janela muda apenas o objeto criado na entrada do programa.
- **Exceções:** `JogadaInvalidaException` e `ApostaInvalidaException` cobrem jogadas e apostas inválidas, e a `Rodada` repete a pergunta ao jogador quando elas ocorrem. Entradas erradas do usuário (letra no lugar de número, opção inexistente) são tratadas na interface.
- **Separação regras/interface:** as regras ficam em `Partida`, `Rodada`, `Vaza`, `Time` e `Jogador`. Toda a entrada e saída passa por `InterfaceJogo`, implementada por `InterfaceTerminal` e `InterfaceGrafica`.

## Diagrama de classes

O diagrama final está em `docs/diagrama-classes.png`.
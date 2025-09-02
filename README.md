# Day Display Mod (Minecraft Forge 1.20.1)

Um mod simples para Forge 1.20.1 que exibe o número do dia in-game no HUD.

Estrutura do projeto

- `common/` - Código compartilhável e testável (sem dependência de Forge). Contém a lógica para converter tick em dia e formatação.
- `forge/` - Módulo com dependência do Forge que registra o mod e renderiza o HUD.
 - `forge/` - Módulo com dependência do Forge que registra o mod e renderiza o HUD. Um template de handler está em `forge/src/main/java/com/example/daydisplay/client/ClientHudHandler.java`.

Como compilar (local)

1. Ajuste `gradle.properties` para as versões desejadas.
2. Use o wrapper Gradle do ForgeMDK: `./gradlew build` (ou configure `build.gradle` para o MDK específico).

Testes

- Os testes unitários ficam no módulo `common` e cobrem a lógica de cálculo de dia.

Forge integration notes

1. Baixe e configure o Forge MDK para Minecraft 1.20.1.
2. Substitua ou integre as configurações do MDK no `forge/build.gradle` (ou use o MDK como base do módulo `forge`).
3. Implemente a classe principal anotada com `@Mod("daydisplay")` e registre `ClientHudHandler` no event bus do cliente para renderizar o dia.

O arquivo `forge/src/main/java/com/example/daydisplay/client/ClientHudHandler.java` é um template com comentários indicando onde usar APIs do Forge para acessar o world time e desenhar strings no HUD.

Padronização/formatacao

Veja `FORMAT.md` para as regras de formatação e estilo que usei neste projeto e que podem ser aplicadas a outros mods.

Bom desenvolvimento!

<div align="center">
	<h1>Day Display (Forge 1.20.1)</h1>
	<p>HUD limpo e personalizável mostrando <strong>hora</strong>, <strong>dia</strong> e <strong>estação</strong> (Serene Seasons opcional) com ícones e recarregamento dinâmico de configuração.</p>
	<img src="https://via.placeholder.com/420x120?text=Screenshot+HUD" alt="HUD Preview" />
	<br/>
</div>

## ✨ Principais Recursos
- Dia in‑game correto (usa `getDayTime`).
- Relógio 24h ou 12h (AM/PM).
- Estações (sub‑estação / tropical) via Serene Seasons – integração opcional por reflexão (sem crash se ausente).
- Ícone da estação com pipeline anti-halo configurável (bleed + premultiply + padding).
- Layout totalmente configurável (posição, cor, sombra, escala individual por linha e ícone) em `daydisplay-client.toml`.
- Hot‑reload automático do config (sem reiniciar o jogo).
- Desempenho: buscas de estação em intervalo configurável, texturas em cache, cálculo mínimo por frame.

## 🛠 Instalação do Mod
1. Instale Minecraft Forge 1.20.1.
2. Coloque o JAR (ex: `daydisplay-x.y.z.jar`) em `mods/`.
3. (Opcional) Adicione Serene Seasons para ver as estações com ícone/nome traduzido.

## ⚙️ Configuração Rápida
Arquivo: `config/daydisplay-client.toml` (recarrega automaticamente). Principais chaves:
```toml
[hud]
baseX = 8
baseY = 8
seasonRefreshInterval = 40

[hud.time]
enabled = true
x = 34
y = 8
color = 16777215
use24h = true
scaleMilli = 1000

[hud.day]
enabled = true
x = 64
y = 7
color = 5569620
scaleMilli = 1200

[hud.season]
enabled = true
x = 34
y = 20
color = 16777215
scaleMilli = 1000

[hud.icon]
enabled = true
x = -52
y = -43
size = 256
processingMethod = "bleed_premultiply" # none | bleed | premultiply | bleed_premultiply
bleedPasses = 2
padding = 2
```
Escala: `scaleMilli = 1200` -> 1.2x. Sombra por linha configurável (offset + cor). Ajuste `seasonRefreshInterval` para equilibrar responsividade e custo.

## 🧪 Desenvolvimento / Build Local
```bash
./gradlew clean :forge:build
```
Artefato: `forge/build/libs/<nome>.jar`.

## 🧰 Estrutura
- `common/` Lógica compartilhada (ex.: utilitários de tempo).
- `forge/` Código específico Forge (registro do mod, HUD, integração Serene Seasons, config spec).
- `.github/workflows/release.yml` CI: build + testes + release por tag `vX.Y.Z`.

## 🚀 Release Automática
Crie uma tag SemVer com prefixo `v` (ex.: `v1.1.0`) e faça push. O GitHub Actions:
1. Compila.
2. Roda testes (se existirem).
3. Gera changelog (últimos 50 commits).
4. Publica release com o JAR.

Workflow manual: vá em Actions > Build & Release > Run workflow e informe `version` (sem `v`).

## 🔧 Anti-Halo dos Ícones
Pipeline configurável:
1. bleed: preenche RGB de pixels alpha 0 com média de vizinhos.
2. premultiply: multiplica RGB por alpha (atenua franjas semi‑transparentes).
3. padding: cria borda de replicação para evitar sample fora.
Altere `processingMethod`, `bleedPasses`, `padding` e recarregue (hot‑reload já cuida).

## ❓ Solução de Problemas
| Problema | Causa Comum | Solução |
|----------|-------------|---------|
| Estação não aparece | Serene Seasons ausente | Instalar mod ou desabilitar seção | 
| Texto sobreposto | Coordenadas muito próximas | Ajuste `x`/`y` ou `baseX`/`baseY` | 
| Halo no ícone | Config muito branda | Aumente `bleedPasses` ou `padding` | 
| Horário estranho | Pack altera rate do dia | Verificar mods de tempo / dimension | 

## 🗺 Roadmap (Sugestões Futuras)
- Localização multi‑idioma ampliada.
- Comando `/daydisplay reload` manual.
- Suporte Fabric (módulo separado).
- Exibição opcional de fase lunar.

## 📄 Licença
MIT – use, modifique e compartilhe com crédito.
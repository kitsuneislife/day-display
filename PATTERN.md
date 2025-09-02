# PATTERN.md

Guia de Projeto (Forge / Mods Java) — ROBUSTO • LIMPO • MÁXIMA PERFORMANCE

Este documento define padrões reutilizáveis para criar projetos de mods similares. Use-o como checklist e referência de decisões arquiteturais.

---
## 1. Filosofia
- **Robusto:** Falhar com segurança, integração opcional via reflexão, evitar hard crashes por dependências ausentes.
- **Limpo:** Código legível, modular, sem acoplamento desnecessário; nomes sem ambiguidade.
- **Máxima Performance:** Trabalho mínimo por tick/render; cache sempre que valor estável; evitar alocação de curto prazo em loops críticos.

---
## 2. Estrutura de Diretórios Recomendada
```
root/
  build.gradle (raiz multi-projeto)
  settings.gradle
  gradle.properties
  PATTERN.md
  RELEASE_PATTERN_TEMPLATE.md
  README.md
  .github/workflows/
  common/                # Código puro (sem Forge) reutilizável / testável
    src/main/java/
    src/test/java/
  forge/                 # Loader específico (Forge). Cada loader em módulo próprio.
    src/main/java/
    src/main/resources/
  fabric/ (opcional)     # Outro loader se necessário
  docs/ (opcional)       # Diagramas, assets de documentação
```

Regra: tudo que não precisa do ambiente Forge fica em `common`.

---
## 3. Gradle & Dependências
- Usar `gradle.properties` para: `mod_version`, `minecraft_version`, `forge_version`.
- Wrapper validado em CI (security).
- Evitar dependências gordas: preferir libs pequenas, somar somente se ganho claro.
- Controlar versões via propriedades únicas; sem duplicação de strings em build scripts.
- Aplicar toolchains (`java { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }`).
- Habilitar incremental compilation e configuração on-demand quando seguro.

---
## 4. Naming & Pacotes
- Base: `com.<org>.<projeto>`.
- Sufixos claros: `*Config`, `*Handler`, `*Service`, `*Cache`.
- Nada de abreviações obscuras: preferir `SeasonIntegrationHelper` a `SeasIntHlpr`.

---
## 5. Configuração (TOML / ForgeConfigSpec)
- Cada bloco funcional próprio (`hud`, `performance`, `debug`).
- Comentários breves e prescritivos.
- Hot reload: monitorar mtime ou usar events se disponíveis.
- Validar valores extremos (min/max) cedo; fallback seguro se inválido.

### Boas práticas
| Cenário | Estratégia |
|---------|-----------|
| Valor caro | Cache e TTL configurável |
| Flag booleana usada por frame | Copiar para campo estático em reload |
| Escala / cor | Pré-normalizar para float/int prontos para uso |

---
## 6. Render / HUD Performance
- Calcular textos uma vez por frame; separar lógica de `fetch` versus `draw`.
- Reduzir chamadas de reflexão (throttle configurável).
- Não alocar `Component` repetidamente se string não mudou (cache por chave + versão).
- Minimizar push/pop de pose stack; agrupar transformações relacionadas.

### Checklist Render Tick
- [ ] Verificar contexto (world/player) null-safe.
- [ ] Throttle acessos externos.
- [ ] Atualizar caches expandidos somente em mudança de config.
- [ ] Evitar logs por frame (log somente em transição).

---
## 7. Integração Opcional (Reflection Pattern)
```
if (!ModList.get().isLoaded("alvo")) return null;
// Lazy init -> tentar vincular métodos uma vez
// Guardar Method refs estáticos
// Throttle falhas: se lançar exceção crítica, desabilitar integração até restart
```
Fallback sempre silencioso e seguro.

---
## 8. Texturas & Anti-Halo
- .mcmeta: `{ "texture": { "blur": false, "clamp": true } }`.
- Pipeline opcional: bleed passes + premultiply + padding.
- Reprocessar somente em reload de recurso; resultado em textura dinâmica.
- Padding mínimo 2 px para ícones grandes escalados.

---
## 9. Logging
- INFO apenas para eventos relevantes (inicialização, mudança de dia, ativação de integração).
- DEBUG para detalhes de fallback / falhas não fatais.
- Nunca spam dentro de loops de tick sem condição.

---
## 10. Testes (Módulo `common`)
- Métodos puros testados (ex.: conversão ticks → dia/hora).
- Casos limite: dia 0, cruzamento de meia-noite, transição de estação simulada.
- Usar JUnit 5; nome de método: `shouldCalcularX_whenCondicaoY`.

---
## 11. CI / Workflow Padrão
### Gatilhos
- Push de tag `v*.*.*` → build + release.
- `workflow_dispatch` com input de versão.

### Passos
1. Checkout com histórico para changelog.
2. Validar wrapper.
3. Set up JDK 17.
4. Build (excluir testes opcionais, rodar depois).
5. Testes (tolerante se zero testes).
6. Localizar JAR principal (não `-sources`).
7. Gerar changelog simples (últimos N commits).
8. Criar release com artifact.

### Boas Práticas CI
| Item | Regra |
|------|-------|
| Versioning | Tag = `v` + `mod_version` |
| Segurança | Wrapper validation + mínima permissão de token |
| Artefatos | Upload do JAR + docs auxiliares |
| Reprodutibilidade | Versões fixas em `gradle.properties` |

---
## 12. Versionamento & Release
- SemVer: API/config quebrada => major.
- Novos recursos sem quebra => minor.
- Correções / ajustes internos => patch.
- Nunca retagar release pública; crie nova versão.

---
## 13. Guia de Pull Request
Checklist PR:
- [ ] Build local passa.
- [ ] Tests verdes (quando existirem).
- [ ] Nenhum warning novo crítico.
- [ ] README / docs atualizados se mudar comportamento.
- [ ] Commits limpos ou squashados.

Convecção de mensagem: `tipo(scope): descrição curta`.
Tipos: `feat`, `fix`, `perf`, `refactor`, `docs`, `ci`, `chore`.

---
## 14. Tratamento de Config Reload
Estratégia recomendada:
1. Registrar listener de arquivo ou polling leve a cada N ticks.
2. Ao detectar mudança → reparse spec.
3. Copiar valores para campos voláteis/imediatos (ex.: `int seasonRefreshIntervalCached`).
4. Invalidação de caches dependentes.

---
## 15. Caching & Micro Otimizações
- Agrupar leituras de config em struct imutável em reload -> uso direto em render.
- Evitar `String.format` em hot path; usar `StringBuilder` ou pré-tabelas.
- Reutilizar objetos `Component` quando o texto não muda.
- Guardar `ResourceLocation` em final static ou caches pre-carregados.

---
## 16. Erros e Fallbacks
| Contexto | Ação |
|----------|------|
| Falha reflexão | Log DEBUG + desabilitar integração |
| Textura ausente | Não desenhar ícone, sem crash |
| Config inválida | Aplicar clamp e avisar DEBUG |

---
## 17. Segurança / Resiliência
- Nunca executar código de mods externos sem verificação de presença.
- Tratar exceções em blocos mínimos (evitar capturar gigante). 
- Não engolir `Throwable` sem pelo menos log DEBUG (exceto caminho ultra crítico de render com fallback silencioso controlado).

---
## 18. Internacionalização
- Chaves de tradução centralizadas (`lang/*.json`).
- Strings de HUD preferencialmente traduzíveis via `Component.translatable`.
- Fallback para inglês se chave faltar.

---
## 19. Estilo de Código
- Linhas curtas (<120 col). 
- Métodos com responsabilidade única. 
- Comentários: explicar "por quê" (não o óbvio).
- Usar `final` onde possível para sinalizar imutabilidade.

---
## 20. Roadmap Template
```markdown
### Roadmap
- [ ] Feature A
- [ ] Otimização B
- [ ] Port Fabric
- [ ] Telemetria opcional (opt-in)
```

---
## 21. Checklist de Novo Projeto (Copy & Adapt)
| Item | Feito |
|------|-------|
| Estrutura multi-módulo criada | ☐ |
| `gradle.properties` preenchido | ☐ |
| CI workflow copiado/adaptado | ☐ |
| Config Spec inicial | ☐ |
| README base | ☐ |
| PATTERN.md incluído | ☐ |
| Licença definida | ☐ |
| Teste mínimo `TimeUtils` | ☐ |
| Anti-halo pipeline (se usar ícones) | ☐ |

---
## 22. Métricas (Opcional)
- Medir ms médios por frame adicionados (perfil Forge).
- Contar chamadas reflexão pós-throttle (esperado ~0 durante frames intermediários).
- Monitorar alocações com ferramenta (ex.: VisualVM) e manter zero GC spikes notáveis.

---
## 23. Quando Otimizar
- Antes: se existe geometria de custo alto óbvio (reflexão, IO).
- Depois: somente se profiler indicar hotspot >1% do frame.

---
## 24. Licenciamento & Distribuição
- Arquivo LICENSE visível.
- Changelog por release (gerado ou manual).
- Assinatura / hashing JAR (opcional) se cadeia de confiança for relevante.

---
## 25. Extensibilidade
- Expor pontos: interfaces simples (ex.: `SeasonProvider`).
- Evitar acoplamento rígido a classes internas; depender de contratos.
- Planejar substituição injetando implementação via serviço ou registro estático.

---
## 26. Descarte / Descontinuidade
- Marcar deprecado com @Deprecated + comentário.
- Anunciar end-of-life em README / releases.

---
## 27. Exemplos Rápidos
### Ex: Cálculo de Hora
```java
long dayTime = world.getDayTime() % 24000L; // 0..23999
int hour24 = (int)((dayTime / 1000L + 6) % 24);
int minute = (int)((dayTime % 1000L) * 60L / 1000L);
```
### Ex: Throttle de Integração
```java
if (now - lastFetch >= interval) {
  lastFetch = now;
  // refresh data
}
```

---
## 28. Manutenção Contínua
- Revisar dependências a cada ciclo (mensal).
- Rodar análise estática (SpotBugs / ErrorProne) opcional para regressões.
- Automatizar verificação de versão do Forge quando aplicável.

---
## 29. Anti-Pattern Alertas
| Anti-Pattern | Alternativa |
|--------------|-------------|
| Alocação de String em loop render | Cache por frame / builder reutilizável |
| Reflexão a cada frame | Inicializar uma vez + throttle |
| Swallow Throwable silencioso | Log DEBUG + disable feature |
| Cálculo redundante de cor/escala por tick | Pré-cálculo em reload |

---
## 30. Conclusão
Seguindo este padrão você obtém um mod confiável, fácil de manter e extremamente eficiente. Adapte só o que fizer sentido; mantenha a disciplina no restante.

Foco: ROBUSTO • LIMPO • MÁXIMA PERFORMANCE.

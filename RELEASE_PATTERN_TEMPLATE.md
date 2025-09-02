# Release Pattern Template

Use este template para novos projetos / ajustes futuros.

## Versão
- SemVer: `major.minor.patch`
- Tag: prefixo `v`, exemplo: `v1.2.3`
- Campo `mod_version` em `gradle.properties` deve casar com a tag (sem o `v`).

## Fluxo Rápido
```bash
# Atualize mod_version no gradle.properties (sem v)
git add gradle.properties
git commit -m "chore: bump version to 1.1.0"
git tag v1.1.0
git push origin main --tags
```

## Workflow Dispatch
- Acesse Actions > Build & Release > Run workflow.
- Informe `version` (ex: `1.1.1`).
- Ele ajusta `gradle.properties`, compila e publica release.

## Changelog
- Gerado automaticamente via últimos 50 commits (`git log`).
- Para changelog manual, substitua `CHANGELOG_GEN.md` antes do passo de release em um fork/custom action.

## Métodos de Mitigação
- Se build falhar, corrija e force tag nova (`v1.1.1`) ou delete tag e recrie.
- Artefato principal: primeiro JAR não `-sources` em `forge/build/libs`.

## Boas Práticas
- Commits claros: feat:, fix:, chore:, refactor:, docs:
- Use branches para features grandes e squash antes de merge.
- Evite alterar tag já publicada (crie patch novo em vez disso).

## Adaptação Multi-Projeto
- Ajustar job para matrix se tiver módulos extras.
- Alvo de publicação adicional (Modrinth/CurseForge) pode ser adicionado com tokens secretos.

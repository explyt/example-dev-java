## Структура и модули
- Корень: `pom.xml` (многомодульный Maven), Makefile с готовыми целями.
- Backend: `core` (Spring Boot, домен и UI API), `cluster-api` (интеграция с Kafka-кластерами). Исходники в `src/main/java`, ресурсы в `src/main/resources`, тесты в `src/test/java`.
- Frontend: `coral` (React/Vite/TypeScript). Исходники в `coral/src`, тесты в `coral/src` + `test-setup`, статические ассеты в `coral/public`.

## Сборка, запуск и тесты
- Полная сборка: `mvn -DskipTests=true install -f pom.xml`.
- Backend модульно: `cd core && mvn clean verify`, `cd cluster-api && mvn clean verify`; Makefile цели `make klaw_core`, `make cluster_api`. Локальный запуск после сборки: `make run-core` и `make run-cluster-api` (читают собранные JAR из `target`).
- Frontend: `cd coral && pnpm install`; dev против удаленного API — `pnpm dev`, без API — `pnpm dev-without-api`; сборка — `pnpm build`; тесты — `pnpm test` или `pnpm test-dev`; линт/формат — `pnpm lint`, автоформат — `pnpm reformat` + `pnpm eslint --fix`.

## Стиль кода и качество
- Java 17, Spring Boot 3; придерживаемся текущего стиля (Google Java Format/IDE автoформат), 4 пробела, import-order по умолчанию. Имена классов UpperCamel, методов/полей lowerCamel. Используем Lombok, но без лишней магии; явные null-проверки и Optional там, где нужно.
- Тесты: JUnit 5 + AssertJ; именуем `*Test`, кладем в зеркальные пакеты `src/test/java`. Для регрессий — добавляйте узкие тесты на новый/исправленный сценарий.
- FE: TypeScript строгий, компоненты — функциональные; стиль кода через Prettier/ESLint; CSS modules с именами в camelCase.

## Коммиты и pull request
- Коммиты по Conventional Commits (`feat/fix/docs/refactor/...`) и обязательно `Signed-off-by` (`git commit -s`). Мелкие, логичные пачки изменений.
- PR: ссылка на issue (`Resolves: #...`), заполненный шаблон `.github/PULL_REQUEST_TEMPLATE.md`, зеленые проверки CI, свежий `main`. Для UI приветствуются скриншоты/видео. Предпочтительно squash-merge, понятный заголовок по гайду.

## Конфигурация и безопасность
- Не коммитим секреты: `.env*`, ключи, пароли, реальные `application.properties`. Локальные настройки держите вне VCS или в примерах.
- Проверяйте лицензии и сторонние зависимости; обновления делайте отдельными PR.
- Перед пушем: убедитесь, что сборка/тесты для затронутых модулей проходят и не падает форматирование.

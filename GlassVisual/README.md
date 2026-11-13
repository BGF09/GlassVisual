# Glass Visual

Клиентский Fabric-мод для **Minecraft 1.21.4** с тёмным glass-интерфейсом, темами и PvP-style HUD.

## Возможности

- ClickGUI открывается на **Right Shift**.
- NULL-style интерфейс с категориями, настройкой масштаба, FIT / STRETCH / 4:3 режимами и палитрой акцентов.
- Переключатели Target HUD, Keystrokes, Coordinates, FPS, ArrayList, Armor HUD, Potion HUD и Ping.
- Target HUD показывает имя, здоровье, дистанцию и настоящий скин игрока, если он доступен в сетевом списке.
- ArrayList отображает включённые HUD-модули справа экрана.
- Armor HUD показывает надетую броню и индикаторы её прочности.
- Potion HUD показывает активные эффекты, уровни и оставшееся время.
- Ping отображает задержку до сервера; FPS и координаты выводятся отдельными компактными панелями.
- Редактор HUD открывается на **Right Alt**: перетаскивай восемь групп элементов мышью. Позиции сохраняются.
- Анимированные уведомления о переключении модулей и обнаруженном использовании некоторых предметов другими игроками.
- Косметические частицы и кастомное главное меню.
- Настройки сохраняются в `.minecraft/config/glassvisual.json` и загружаются при следующем запуске.

## Требования

- Java 21
- Minecraft 1.21.4
- Fabric Loader 0.16.x или новее
- Fabric API

## Сборка

Из папки `GlassVisual`:

```bash
gradle build
```

Готовые JAR-файлы появятся в `build/libs/`. Для установки скопируйте основной `glass-visual-*.jar` в папку `.minecraft/mods` вместе с Fabric API. Файл `*-sources.jar` содержит исходный код и в папку mods не нужен.

GitHub Actions автоматически собирает проект при push в любую ветку и публикует JAR как workflow artifact.

## Управление

| Клавиша | Действие |
|---|---|
| Right Shift | Открыть / закрыть ClickGUI |
| Right Alt | Открыть редактор HUD |
| ESC | Закрыть открытый экран |

Клавишу ClickGUI можно изменить в Options → Controls → Key Binds.

## Конфигурация

Файл `.minecraft/config/glassvisual.json` хранит состояние модулей, выбранную тему, частицы, режим раскладки, масштаб GUI и позиции элементов HUD. Если файл отсутствует или повреждён, клиент использует безопасные значения по умолчанию.

## Структура

```text
src/main/java/dev/glassvisual/
├── GlassVisualClient.java
├── GlassVisualConfig.java
├── ClickGuiScreen.java
├── HudOverlayRenderer.java
├── HudEditorScreen.java
├── HudLayout.java
├── TargetHudRenderer.java
├── TargetNotifier.java
├── Notifier.java
├── NullTheme.java
├── NullUiSettings.java
└── NullParticleRenderer.java
```

## Лицензия

MIT — см. LICENSE.

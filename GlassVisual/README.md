# Glass Visual

Клиентский Fabric-мод для **Minecraft 1.21.4** с PvP-style ClickGUI и фиолетовым Target HUD.

## Возможности

- ClickGUI открывается на **Right Shift**.
- Тёмная glass-панель с фиолетовыми акцентами.
- Переключатель Target HUD.
- Target HUD появляется, когда прицел наведён на живую сущность.
- Отображаются имя цели, здоровье, дистанция и полоска HP.
- Переключатели-заготовки для Keystrokes, Coordinates и FPS Counter.

> Это стартовая версия проекта: Keystrokes, Coordinates и FPS Counter пока представлены переключателями интерфейса, их отрисовка ещё не реализована. Target HUD использует стилизованный аватар-заглушку; получение настоящей головы игрока можно добавить отдельным этапом.

## Требования

- Java 21
- Minecraft 1.21.4
- Fabric Loader 0.16.x или новее
- Fabric API
- Gradle (или Gradle Wrapper, если добавите его в репозиторий)

## Сборка

Из корневой папки проекта:

```bash
gradle build
```

Готовый JAR появится в:

```text
build/libs/glass-visual-1.0.0.jar
```

Скопируйте JAR в папку `.minecraft/mods` вместе с Fabric API.

## Управление

| Клавиша | Действие |
|---|---|
| Right Shift | Открыть ClickGUI |

Клавишу можно изменить в Options → Controls → Key Binds.

## Структура

```text
glass-visual/
├── src/main/java/dev/glassvisual/
│   ├── GlassVisualClient.java
│   ├── ClickGuiScreen.java
│   └── TargetHudRenderer.java
├── src/main/resources/
│   ├── fabric.mod.json
│   └── assets/glassvisual/lang/
├── build.gradle
├── gradle.properties
└── settings.gradle
```

## Лицензия

MIT — см. LICENSE.

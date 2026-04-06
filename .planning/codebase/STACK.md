# Технологический стек

**Дата анализа:** 2026-04-06

## Языки программирования

**Основной язык:**
- Java 8 (версия 1.8) — используется для всего проекта
- Исходный код: `L4S4-Maven/src/main/java/**/*.java`

**Второстепенные языки:**
- XML — конфигурационные файлы Maven и Eclipse
  - `L4S4-Maven/pom.xml` — манифест проекта
  - `L4S4-Maven/.classpath` — конфигурация classpath
  - `L4S4-Maven/.project` — описание проекта Eclipse

## Среда выполнения

**Платформа:**
- JVM (Java Virtual Machine)
- Целевая платформа: JavaSE-1.8 (согласно `.settings/org.eclipse.jdt.core.prefs`)

**Менеджер пакетов:**
- Apache Maven
- Lockfile: отсутствует (только `pom.xml`)
- GroupId: `org.build`
- ArtifactId: `L4S4-Maven`
- Version: `1.0-SNAPSHOT`
- Packaging: `jar`

## Фреймворки

**Графический интерфейс (GUI):**
- Java Swing — стандартная библиотека GUI
  - Используемые компоненты: `JFrame`, `JPanel`, `JButton`, `JLabel`, `JTextArea`, `JScrollPane`, `JComboBox`, `JSpinner`, `JOptionPane`
  - Файлы: `SelectTaskMenu.java`, `GUI/Task1/InputAdressFrame.java`, `GUI/Task2/ConnectionFrame.java`, `GUI/Task3/Launcher.java`, `Task3/Dispatcher.java`

**Сетевое взаимодействие:**
- Java Socket API (`java.net.*`)
  - ServerSocket, Socket, InetSocketAddress
  - Файлы: `Base/Server.java`, `Base/Client.java`, `Task1/FileServer.java`, `Task2/Controller.java`, `Task2/Dispatcher.java`, `Task3/Controller.java`, `Task3/Dispatcher.java`

**Тестирование:**
- JUnit 3.8.1 — unit-тестирование
  - Scope: test
  - Файл: `src/test/java/org/build/AppTest.java`

**Система сборки:**
- Apache Maven (m2e в Eclipse)
  - Плагины: maven-compiler-plugin (implicit)
  - Build lifecycle: стандартный Maven lifecycle

## Ключевые зависимости

**Критические (стандартная библиотека):**
- `java.io.*` — потоки ввода-вывода, сериализация
  - BufferedReader, PrintWriter, ObjectOutputStream, ObjectInputStream
- `java.net.*` — сетевое взаимодействие
  - ServerSocket, Socket, InetSocketAddress
- `java.awt.*` — базовая графика и события
  - Color, Dimension, Font, Insets, GridLayout, BorderLayout, GridBagLayout, BoxLayout
- `javax.swing.*` — компоненты GUI
- `java.beans.*` — шаблон Observer
  - PropertyChangeSupport, PropertyChangeListener, PropertyChangeEvent
- `java.util.*` — утилиты
  - Random, Locale, ArrayList, List

**Тестирование:**
- `junit:junit:3.8.1` — фреймворк unit-тестирования

## Конфигурация

**Окружение:**
- Кодировка: UTF-8 (`project.build.sourceEncoding=UTF-8`)
- Java Compliance: 1.8
- Java Target Platform: 1.8

**Сборка:**
- `L4S4-Maven/pom.xml` — основной конфиг Maven
- `L4S4-Maven/.classpath` — конфигурация Eclipse classpath
- `L4S4-Maven/.settings/org.eclipse.jdt.core.prefs` — настройки компилятора

**Вывод сборки:**
- `L4S4-Maven/target/classes/` — скомпилированные классы
- `L4S4-Maven/target/test-classes/` — скомпилированные тесты

## Требования к платформе

**Разработка:**
- JDK 8 или выше
- Apache Maven 3.x
- Eclipse IDE с плагином m2e (опционально)

**Production:**
- JRE 8 или выше
- Минимум 64MB RAM для GUI приложений
- TCP/IP сеть для socket-приложений

## Контроль версий

**Система:**
- Git
- Репозиторий: локальный (A:\Repos\Prog)
- Игнорируемые файлы: `.vs`, `obj\Debug`, `bin\Debug` (настройки для .NET проектов)

**Последние коммиты:**
- `706f0c8` — Забыл закомитить....
- `7b9302d` — Общение сервера клиента
- `af81d89` — Начал делать Model-Task1

---

*Анализ стека: 2026-04-06*

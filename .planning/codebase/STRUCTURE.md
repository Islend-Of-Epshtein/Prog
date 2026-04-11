# Структура кодовой базы

**Дата анализа:** 2026-04-06

## Структура директорий

```
L4S4-Maven/
├── src/
│   ├── main/
│   │   └── java/
│   │       ├── Base/              # Базовые абстракции сети
│   │       ├── GUI/               # Графический интерфейс
│   │       │   ├── Task1/         # GUI для Task1
│   │       │   ├── Task2/         # GUI для Task2 (заглушка)
│   │       │   └── Task3/         # GUI для Task3 (Launcher)
│   │       ├── Task1/             # Задание 1: Файловый сервер
│   │       ├── Task2/             # Задание 2: Мониторинг процесса
│   │       ├── Task3/             # Задание 3: Мониторинг установок
│   │       └── SelectTaskMenu.java # Главное меню выбора задания
│   └── test/
│       └── java/
│           └── org/build/         # Unit тесты
├── pom.xml                        # Maven конфигурация
└── .settings/                     # Eclipse настройки
```

## Назначение директорий

**Base:**
- Назначение: Базовые классы для клиент-серверного взаимодействия
- Содержит: Server.java, Client.java
- Ключевые файлы: `Base/Server.java`, `Base/Client.java`

**Task1:**
- Назначение: Файловый сервер с передачей корневых директорий
- Содержит: Специализированные Server и Client классы
- Ключевые файлы: `FileServer.java`, `ClientReqest.java`

**Task2:**
- Назначение: Мониторинг технологического процесса (MVC)
- Содержит: Controller, Dispatcher, модели, интерфейсы
- Ключевые файлы: `Controller.java`, `Dispatcher.java`, `ProcessData.Java`

**Task3:**
- Назначение: Система мониторинга промышленных установок
- Содержит: Controller, Dispatcher с state machine
- Ключевые файлы: `Controller.java`, `Dispatcher.java`

**GUI:**
- Назначение: Графические интерфейсы для всех заданий
- Содержит: JFrame наследники, лаунчеры
- Ключевые файлы: `GUI/Task1/InputAdressFrame.java`, `GUI/Task3/Launcher.java`

## Расположение ключевых файлов

**Точки входа:**
- `SelectTaskMenu.java`: Главное меню запуска
- `Task2/Task2ConsoleDemo.java`: Консольный демо Task2
- `Task3/Controller.java`: Контроллер Task3 (main)
- `Task3/Dispatcher.java`: Диспетчер Task3 (main)

**Конфигурация:**
- `pom.xml`: Maven конфигурация (зависимости, groupId: org.build)

**Базовая логика:**
- `Base/Server.java`: Абстракция сервера с ObjectOutputStream/ObjectInputStream
- `Base/Client.java`: Абстракция клиента с потоками ввода-вывода

**Модели данных:**
- `Task2/ProcessData.Java`: Данные процесса (температура, давление)
- `Task3`: Массив int[] для состояний установок (в Controller)

**Интерфейсы:**
- `Task2/ProcessDataTransmitter.java`: Передатчик данных
- `Task2/DispatcherView.java`: Представление диспетчера

## Иерархии наследования

**Server иерархия:**
```
Object
└── Base.Server (java.io.IOException)
    └── Task1.FileServer (переопределяет Accept)
```

**Client иерархия:**
```
Object
└── Base.Client (java.io.IOException)
    └── Task1.ClientReqest (implements PropertyChangeListener)
```

**GUI иерархия:**
```
Object
└── JFrame
    ├── SelectTaskMenu
    └── GUI.Task1.InputAddressFrame (implements PropertyChangeListener)
```

**Интерфейсы:**
```
ProcessDataTransmitter
└── ConsoleProcessDataTransmitter

DispatcherView
└── ConsoleDispatcherView

PropertyChangeListener (java.beans)
├── Task1.ClientReqest
└── GUI.Task1.InputAddressFrame
```

## Реализации интерфейсов

**ProcessDataTransmitter:**
- Интерфейс: `Task2/ProcessDataTransmitter.java`
- Реализация: `Task2/ConsoleProcessDataTransmitter.java`
- Методы: send(ProcessData), close()

**DispatcherView:**
- Интерфейс: `Task2/DispatcherView.java`
- Реализация: `Task2/ConsoleDispatcherView.java`
- Методы: showConnectionInfo(), showData(), showError()

**PropertyChangeListener:**
- Интерфейс: java.beans.PropertyChangeListener
- Реализации: `ClientReqest`, `InputAdressFrame`
- Назначение: Реактивное обновление GUI при изменении данных

## Соглашения об именовании

**Файлы:**
- PascalCase для классов: `SelectTaskMenu.java`, `ProcessData.Java`
- Пакеты в lowercase: `base`, `task1`, `gui`

**Классы:**
- Существительные: `Server`, `Client`, `Controller`, `Dispatcher`
- Интерфейсы без префикса I: `DispatcherView`, `ProcessDataTransmitter`

**Методы:**
- camelCase: `initBuffer()`, `showConnectionInfo()`, `attemptConnection()`
- Геттеры с префиксом Get: `GetAddress()`, `GetPort()`, `GetSocket()`

**Переменные:**
- camelCase: `clientSocket`, `serverSocket`, `numberOfUnits`
- Константы UPPER_SNAKE_CASE: `MIN_TEMPERATURE`, `MAX_PRESSURE`

## Где добавлять новый код

**Новое задание (Task4):**
- Основной код: `L4S4-Maven/src/main/java/Task4/`
- GUI: `L4S4-Maven/src/main/java/GUI/Task4/`
- Добавить кнопку в: `SelectTaskMenu.java`

**Новый компонент в существующее задание:**
- Task1: `L4S4-Maven/src/main/java/Task1/`
- Task2: `L4S4-Maven/src/main/java/Task2/`
- Task3: `L4S4-Maven/src/main/java/Task3/`

**Новая абстракция сети:**
- Базовый класс: `L4S4-Maven/src/main/java/Base/`
- Наследовать от Server или Client

**Новый GUI компонент:**
- В соответствующий GUI/Task{N}/

**Тесты:**
- `L4S4-Maven/src/test/java/org/build/`

## Специальные директории

**.settings:**
- Назначение: Eclipse project settings
- Генерируется: Да (Eclipse)
- Коммитится: Да

**target:**
- Назначение: Maven build output
- Генерируется: Да (mvn compile/package)
- Коммитится: Нет (в .gitignore)

**.planning/codebase:**
- Назначение: Документация архитектуры
- Генерируется: Да (gsd-map-codebase)
- Коммитится: Да

## Зависимости между пакетами

```
SelectTaskMenu ─────────────┬── GUI.Task1.InputAddressFrame
                            ├── GUI.Task2.ConnectionFrame
                            └── GUI.Task3.Launcher

GUI.Task1.InputAddressFrame ─┬── Base.Server
                            ├── Base.Client
                            ├── Task1.FileServer
                            └── Task1.ClientReqest

Task1.FileServer ───────────└── Base.Server (наследует)

Task1.ClientReqest ─────────└── Base.Client (наследует)

Task2.Controller ───────────┬── Task2.ProcessDataGenerator
                            └── Task2.ConsoleProcessDataTransmitter

Task2.Dispatcher ───────────┬── Task2.ProcessData (deserialize)
                            └── Task2.ConsoleDispatcherView

GUI.Task3.Launcher ─────────┴── ProcessBuilder → Task3.Controller
                                     └── ProcessBuilder → Task3.Dispatcher
```

---

*Анализ структуры: 2026-04-06*

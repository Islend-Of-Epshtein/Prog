# Архитектура

**Дата анализа:** 2026-04-06

## Обзор паттерна

**Общий подход:** Многозадачное клиент-серверное приложение

**Ключевые характеристики:**
- Три независимых задания (Task1, Task2, Task3) с общей базовой инфраструктурой
- Swing GUI с модульной организацией
- Socket-ориентированное взаимодействие клиент-сервер
- Разделение ответственности через интерфейсы и наследование

## Слои

**Слой базовой инфраструктуры (Base):**
- Назначение: Абстракции для сетевого взаимодействия
- Расположение: `L4S4-Maven/src/main/java/Base/`
- Содержит: `Server.java`, `Client.java`
- Зависит от: java.io, java.net
- Используется: Task1 (наследуется), как шаблон для Task2/Task3

**Слой бизнес-логики (Task1-3):**
- Назначение: Реализация конкретных заданий
- Расположение: `L4S4-Maven/src/main/java/Task{N}/`
- Содержит: Контроллеры, диспетчеры, модели данных
- Зависит от: Base (Task1), java.io, java.net, javax.swing
- Используется: GUI слой

**Слой представления (GUI):**
- Назначение: Графический интерфейс пользователя
- Расположение: `L4S4-Maven/src/main/java/GUI/`
- Содержит: Фреймы, лаунчеры, диалоги подключения
- Зависит от: javax.swing, Task{N} классы
- Используется: SelectTaskMenu (точка входа)

## Потоки данных

**Task1 - Файловый сервер:**

1. Сервер открывает порт через `ServerSocket`
2. Клиент подключается к серверу через `Client` класс
3. Сервер отправляет `File.listRoots()` (корневые директории)
4. Клиент получает через `ObjectInputStream`
5. GUI обновляет `JComboBox` через `PropertyChangeSupport`

**Task2 - Мониторинг процесса (MVC):**

1. `Controller` создает `ServerSocket` и ожидает подключения
2. `Dispatcher` подключается к контроллеру
3. `ProcessDataGenerator` генерирует данные каждые 1 секунду
4. `ProcessDataTransmitter` отправляет сериализованные данные
5. `DispatcherView` отображает данные (Console или GUI)

**Task3 - Мониторинг установок:**

1. `Controller` инициализирует массив состояний установок
2. Диспетчер подключается и получает количество установок
3. Контроллер обновляет состояния по вероятностной модели
4. Состояния отправляются каждые 2 секунды через `ObjectOutputStream`
5. Диспетчер обновляет кнопки с цветовой индикацией

**Управление состоянием:**
- Task1: PropertyChangeSupport для реактивного обновления GUI
- Task2: Interface `DispatcherView` для отделения логики от представления
- Task3: Массив `int[] unitStates` с тремя состояниями (0=РАБОТАЕТ, 1=АВАРИЯ, 2=РЕМОНТ)

## Ключевые абстракции

**Base.Server / Base.Client:**
- Назначение: Базовые классы для сокетного взаимодействия
- Примеры: `Base/Server.java`, `Base/Client.java`
- Паттерн: Template Method (методы Write/Read с перегрузкой)
- Поддерживает: текстовые и объектные потоки

**ProcessDataTransmitter:**
- Назначение: Интерфейс для передачи данных процесса
- Примеры: `Task2/ProcessDataTransmitter.java`
- Паттерн: Strategy (разные реализации для Console/GUI)
- Методы: `send(ProcessData)`, `close()`

**DispatcherView:**
- Назначение: Интерфейс представления диспетчера
- Примеры: `Task2/DispatcherView.java`
- Паттерн: Observer (MVC View)
- Реализации: `ConsoleDispatcherView`

## Точки входа

**SelectTaskMenu:**
- Расположение: `SelectTaskMenu.java`
- Вызывает: `InitFrame()` из статического метода `main`
- Ответственность: Меню выбора задания (Task1/2/3)

**Task1.FileServer:**
- Расположение: `Task1/FileServer.java`
- Вызывает: наследует от `Base.Server`
- Ответственность: Сервер для передачи файловой системы

**Task2.Task2ConsoleDemo:**
- Расположение: `Task2/Task2ConsoleDemo.java`
- Вызывает: Controller или Dispatcher в зависимости от аргументов
- Ответственность: Консольный запуск Task2

**Task3.Controller / Task3.Dispatcher:**
- Расположение: `Task3/Controller.java`, `Task3/Dispatcher.java`
- Вызывает: независимые main методы
- Ответственность: Сервер и клиент мониторинга установок

**GUI.Task3.Launcher:**
- Расположение: `GUI/Task3/Launcher.java`
- Вызывает: `ProcessBuilder` для запуска Controller и Dispatcher
- Ответственность: GUI лаунчер для Task3

## Обработка ошибок

**Стратегия:** Обработка исключений с GUI уведомлениями

**Паттерны:**
- try-catch с `JOptionPane.showMessageDialog()` для GUI
- System.err.println для консольного вывода
- try-with-resources для автоматического закрытия ресурсов
- Проверка `if (running)` для graceful shutdown

**Специфика:**
- Task3 использует `volatile boolean running` для контроля потока
- Обработка `SocketException` и `EOFException` для разрыва соединения

## Сквозные аспекты

**Логирование:** System.out.println с префиксами [Контроллер], [Диспетчер]

**Валидация:**
- Проверка порта через try-catch NumberFormatException
- Проверка диапазона установок (1-100)
- Проверка соединения через `IsBound()`

**Аутентификация:** Не реализована (локальная сеть)

## Модель потоков

**Swing Event Dispatch Thread (EDT):**
- Все GUI операции выполняются через `SwingUtilities.invokeLater()`
- Основной UI код работает в EDT

**Фоновые потоки:**
- Task1: Поток для `server.Accept()` и чтения объектов
- Task2: Основной цикл Controller с `Thread.sleep(1000)`
- Task3: Поток receiverThread (daemon) для приема состояний
- Task3 Launcher: Потоки мониторинга процессов через `Process.waitFor()`

**Синхронизация:**
- `volatile boolean running` для безопасного завершения
- `volatile boolean connected` для контроля соединения
- SwingUtilities.invokeLater для обновления GUI из фоновых потоков

---

*Анализ архитектуры: 2026-04-06*

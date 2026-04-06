# Внешние интеграции

**Дата анализа:** 2026-04-06

## API и внешние сервисы

**Внешние API:**
- Не обнаружено — проект работает автономно без интеграции с облачными API

## Хранение данных

**Базы данных:**
- Не используются — проект не имеет постоянного хранилища данных

**Файловая система:**
- Java I/O API (`java.io.File`)
  - Чтение корневых директорий: `File.listRoots()` в `Task1/FileServer.java`
  - Получение абсолютных путей: `file.getAbsolutePath()` в `GUI/Task1/InputAdressFrame.java`
  - Функционал: просмотр файловой системы через socket

**Кэширование:**
- Не используется

## Сетевые протоколы

**TCP/IP Sockets:**
- Протокол: TCP (Transmission Control Protocol)
- Реализация: `java.net.ServerSocket`, `java.net.Socket`
- Порт по умолчанию: 12345 (Task2, Task3)
- Автовыбор порта: порт 0 (System assigned)

**Паттерн использования:**

1. **Base Server/Client** — базовые шаблоны
   - `Base/Server.java` — серверная обёртка над ServerSocket
   - `Base/Client.java` — клиентская обёртка над Socket
   - Методы: `Accept()`, `Write()`, `Read()`, `Off()`

2. **Task1 — Файловый сервер**
   - `Task1/FileServer.java` — наследник Base.Server
   - `Task1/ClientReqest.java` — наследник Base.Client
   - Функционал: передача списка корневых директорий

3. **Task2 — Контроллер технологического процесса**
   - `Task2/Controller.java` — сервер, генерирует данные
   - `Task2/Dispatcher.java` — клиент, получает данные
   - Данные: температура (0-100°C), давление (0-6 атм)

4. **Task3 — Система мониторинга установок**
   - `Task3/Controller.java` — сервер состояний установок
   - `Task3/Dispatcher.java` — GUI клиент мониторинга
   - Состояния: 0 (РАБОТАЕТ), 1 (АВАРИЯ), 2 (РЕМОНТ)

## Сериализация данных

**Java Object Serialization:**
- `java.io.ObjectOutputStream` — сериализация объектов
- `java.io.ObjectInputStream` — десериализация объектов
- Используется для передачи объектов по сети

**Передаваемые типы данных:**
- `File[]` — массив файловых объектов (`Task1/FileServer.java`)
- `Integer` — количество установок и состояния (`Task3/Controller.java`)
- `ProcessData` — объект данных технологического процесса (`Task2`)

**Текстовый протокол:**
- `BufferedReader` / `PrintWriter` — текстовая передача
- Формат: `температура;давление` (Task2, ProcessData)
- Пример: `"45.67;3.21"` — температура и давление

## Взаимодействие между процессами

**Process API:**
- `java.lang.ProcessBuilder` — запуск дочерних процессов
- `java.lang.Process` — управление запущенными процессами

**Использование в GUI.Task3.Launcher:**
```java
ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className, args);
builder.redirectErrorStream(true);
builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
Process process = builder.start();
```

**Запускаемые процессы:**
- `Task3.Controller` — контроллер мониторинга
- `Task3.Dispatcher` — диспетчер мониторинга

**Мониторинг процессов:**
- `process.waitFor()` — ожидание завершения
- `process.isAlive()` — проверка активности
- `process.destroy()` — принудительное завершение

## Шаблон Observer (событийная модель)

**Java Beans API:**
- `java.beans.PropertyChangeSupport` — управление слушателями
- `java.beans.PropertyChangeListener` — интерфейс слушателя
- `java.beans.PropertyChangeEvent` — объект события

**Использование:**
- `Task1/ClientReqest.java` — уведомление об изменении File[]
- `GUI/Task1/InputAdressFrame.java` — уведомление об изменении файлов

**Паттерн:**
```java
private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
pcs.firePropertyChange("File", oldName, roots);  // отправка события
addPropertyChangeListener(propertyName, listener); // подписка
```

## Аутентификация и безопасность

**Аутентификация:**
- Отсутствует — соединения устанавливаются без аутентификации

**Безопасность:**
- Без шифрования — данные передаются в открытом виде
- Без валидации входных данных на сервере
- Без ограничений на количество соединений

## Мониторинг и логирование

**Логирование:**
- `System.out.println()` — вывод в консоль
- `System.err.println()` — вывод ошибок
- Стандартный вывод наследуется дочерними процессами

**Паттерны логирования в Task3:**
```java
System.out.println("=== КОНТРОЛЛЕР ЗАПУЩЕН ===");
System.out.println("Порт: " + port);
System.out.println("Количество установок: " + numberOfUnits);
```

**Отслеживание ошибок:**
- `printStackTrace()` — вывод стека исключений
- `JOptionPane.showMessageDialog()` — GUI уведомления об ошибках

## CI/CD и развёртывание

**Хостинг:**
- Локальная разработка — без production развёртывания

**CI Pipeline:**
- Отсутствует

**Сборка:**
```bash
mvn clean compile      # Компиляция
mvn test               # Запуск тестов
mvn package            # Создание JAR
```

## Конфигурация окружения

**Переменные окружения:**
- `java.home` — путь к Java runtime (используется в Launcher.java)
- `java.class.path` — classpath приложения

**Аргументы командной строки:**
- Task2: `[controller|dispatcher] [port] [host]`
- Task3 Controller: `[numberOfUnits] [port]`
- Task3 Dispatcher: `[numberOfUnits]`

**Параметры по умолчанию:**
- Host: `localhost`
- Port: `12345`
- Количество установок: `5`

## Webhooks и обратные вызовы

**Входящие:**
- Отсутствуют

**Исходящие:**
- Отсутствуют

## GUI компоненты

**Swing Look & Feel:**
- `UIManager.getSystemLookAndFeelClassName()` — системный Look & Feel

**Обработка событий:**
- `ActionListener` — обработка действий
- `MouseListener` / `MouseAdapter` — обработка мыши
- `WindowAdapter` — обработка событий окна
- `SwingUtilities.invokeLater()` — потокобезопасное обновление GUI

---

*Аудит интеграций: 2026-04-06*

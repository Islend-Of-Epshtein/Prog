# Соглашения по кодированию

**Дата анализа:** 2026-04-06

## Паттерны именования

### Классы
- **Принятый паттерн:** PascalCase (верблюжий регистр с заглавной буквы)
- **Примеры:** `Server`, `Client`, `FileServer`, `ProcessData`, `Controller`, `Dispatcher`
- **Статус:** ✅ Соблюдается корректно

### Методы
- **Требуемый паттерн:** camelCase (верблюжий регистр со строчной буквы)
- **Фактический паттерн:** ❌ PascalCase (НАРУШЕНИЕ КОНВЕНЦИЙ JAVA)
- **Примеры нарушений:**
  - `InitFrame()` → должно быть `initFrame()`
  - `CreateButton()` → должно быть `createButton()`
  - `Write()` → должно быть `write()`
  - `Read()` → должно быть `read()`
  - `IsBound()` → должно быть `isBound()`
  - `GetAddress()` → должно быть `getAddress()`
  - `GetPort()` → должно быть `getPort()`
  - `GetSocket()` → должно быть `getSocket()`
  - `Off()` → должно быть `off()` или `close()`
- **Файлы с нарушениями:**
  - `L4S4-Maven\src\main\java\SelectTaskMenu.java`
  - `L4S4-Maven\src\main\java\Base\Server.java`
  - `L4S4-Maven\src\main\java\Base\Client.java`
  - `L4S4-Maven\src\main\java\GUI\Task1\InputAdressFrame.java`

### Пакеты
- **Требуемый паттерн:** lowercase (все строчные буквы)
- **Фактический паттерн:** ❌ PascalCase (НАРУШЕНИЕ КОНВЕНЦИЙ JAVA)
- **Примеры нарушений:**
  - `Base` → должно быть `base`
  - `Task1` → должно быть `task1`
  - `Task2` → должно быть `task2`
  - `Task3` → должно быть `task3`
  - `GUI` → должно быть `gui`
- **Файлы с нарушениями:** Все файлы в соответствующих директориях

### Переменные и параметры
- **Принятый паттерн:** camelCase
- **Примеры:** `serverSocket`, `clientSocket`, `numberOfUnits`, `temperature`, `pressure`
- **Статус:** ✅ Соблюдается корректно

### Константы
- **Требуемый паттерн:** UPPER_SNAKE_CASE
- **Примеры:** `MIN_TEMPERATURE`, `MAX_TEMPERATURE`, `MIN_PRESSURE`, `MAX_PRESSURE`
- **Файл:** `L4S4-Maven\src\main\java\Task2\ProcessDataGenerator.java`
- **Статус:** ✅ Соблюдается корректно

## Расширения файлов

- **Требуемый паттерн:** `.java` (строчные буквы)
- **Фактическое нарушение:** `L4S4-Maven\src\main\java\Task2\ProcessData.Java` (заглавная J)
- **Статус:** ❌ Нарушение

## Стиль кода

### Форматирование
- **Открывающая скобка:** На той же строке (K&R стиль)
- **Пример:**
  ```java
  public Server() throws IOException {
      serverSocket = new ServerSocket(0);
  }
  ```
- **Статус:** ✅ Стандартный Java стиль

### Отступы
- **Пробелы:** 4 пробела для отступов
- **Статус:** ✅ Соблюдается

### Пробелы в выражениях
- **Паттерн:** Пробелы вокруг операторов
- **Пример:** `if(log)` вместо `if (log)`
- **Нестандартное:** Иногда пробелы опускаются внутри скобок
- **Файл:** `L4S4-Maven\src\main\java\Base\Server.java`

## Импорты

### Порядок импортов
- **Паттерн:** Без явной группировки
- **Пример:**
  ```java
  import java.io.*;
  import java.net.ServerSocket;
  import java.net.Socket;
  ```
- **Wildcard импорты:** Используются (`import java.io.*;`, `import java.net.*;`)
- **Статус:** ⚠️ Wildcard импорты не рекомендуются для production кода

### Статические импорты
- **Не используются**

## Документация и комментарии

### Javadoc комментарии
- **Формат:** Используется нестандартный тройной слеш `///`
- **Пример:**
  ```java
  /// Шаблон сервера
  public class Server {
      /// Используем свободный порт и локальный адрес
      public Server() throws IOException {
  ```
- **Файлы:** `L4S4-Maven\src\main\java\Base\Server.java`, `L4S4-Maven\src\main\java\Base\Client.java`
- **Статус:** ❌ Не является стандартом Javadoc (должен быть `/** */`)

### Стандартные комментарии
- **Паттерн:** Однострочные `//` комментарии на русском языке
- **Примеры:**
  ```java
  //отправляем
  //ждем ответ
  ```
- **Статус:** ✅ Допустимо

### Многострочные комментарии
- **Не используются**

### Javadoc теги
- **Используются редко:** `@param`, `@return` встречаются только в тестовом файле
- **Файл:** `L4S4-Maven\src\test\java\org\build\AppTest.java`
- **Пример:**
  ```java
  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  ```

## Обработка ошибок

### Паттерны обработки исключений

**Проброс исключений (throws):**
```java
public Server() throws IOException {
    serverSocket = new ServerSocket(0);
}
```
- **Файлы:** `L4S4-Maven\src\main\java\Base\Server.java`, `L4S4-Maven\src\main\java\Base\Client.java`

**Try-catch блоки:**
```java
try (ServerSocket serverSocket = new ServerSocket(port);
     Socket socket = serverSocket.accept()) {
    // код
} catch (IOException e) {
    System.err.println("Ошибка контроллера: " + e.getMessage());
}
```
- **Файлы:** `L4S4-Maven\src\main\java\Task2\Controller.java`, `L4S4-Maven\src\main\java\Task3\Controller.java`

**Try-with-resources:**
- Используется для автоматического закрытия ресурсов
- **Пример:** `L4S4-Maven\src\main\java\Task2\Controller.java`
- **Статус:** ✅ Современный паттерн Java 7+

### Вывод ошибок
- **Консольный вывод:** `System.err.println()` для ошибок
- **GUI диалоги:** `JOptionPane.showMessageDialog()` для GUI ошибок
- **Пример:**
  ```java
  JOptionPane.showMessageDialog(this, "Ошибка подключения: " + ex);
  ```

### e.printStackTrace()
- Используется во многих местах
- **Примеры:** `L4S4-Maven\src\main\java\Task3\Controller.java`, `L4S4-Maven\src\main\java\Task3\Dispatcher.java`
- **Статус:** ⚠️ Не рекомендуется для production (лучше использовать логгер)

## Логирование

### Подход
- **Используется:** `System.out.println()` и `System.err.println()`
- **Логгер:** Отсутствует (нет SLF4J, Log4j и т.д.)
- **Статус:** ⚠️ Не подходит для production

### Паттерны вывода
- **Префиксы:** `[Контроллер]`, `[Диспетчер]`, `[Ошибка диспетчера]`
- **Пример:**
  ```java
  System.out.println("[Контроллер] Сгенерировано: " + data);
  System.err.println("[Ошибка диспетчера] " + message);
  ```
- **Файл:** `L4S4-Maven\src\main\java\Task2\ConsoleDispatcherView.java`

## Модификаторы доступа

### Поля класса
- **Паттерн:** `private final` для неизменяемых полей
- **Пример:**
  ```java
  private final ServerSocket serverSocket;
  private final Random random;
  ```
- **Статус:** ✅ Хорошая практика

### Публичные поля
- **Примеры нарушений:**
  ```java
  public boolean readObjectClient = false;
  public boolean readStringClient = false;
  ```
- **Файл:** `L4S4-Maven\src\main\java\GUI\Task1\InputAdressFrame.java`
- **Статус:** ❌ Нарушение инкапсуляции

### Геттеры
- **Паттерн именования:** PascalCase вместо camelCase
- **Пример:** `GetAddress()` вместо `getAddress()`
- **Статус:** ❌ Нарушение JavaBean конвенции

## Дизайн классов

### Интерфейсы
- **Именование:** Без префикса `I` (корректно)
- **Примеры:** `ProcessDataTransmitter`, `DispatcherView`
- **Файлы:**
  - `L4S4-Maven\src\main\java\Task2\ProcessDataTransmitter.java`
  - `L4S4-Maven\src\main\java\Task2\DispatcherView.java`

### Реализации интерфейсов
- **Паттерн:** Имя интерфейса + суффикс `Impl` НЕ используется
- **Альтернативные имена:** `ConsoleProcessDataTransmitter`, `ConsoleDispatcherView`
- **Статус:** ✅ Описательные имена

### Наследование
- **Паттерн:** `extends` для расширения базовых классов
- **Пример:**
  ```java
  public class FileServer extends Server {
  ```
- **Файл:** `L4S4-Maven\src\main\java\Task1\FileServer.java`

## Организация методов

### Порядок методов
- **Паттерн:** Нет строгого порядка
- **Обычный порядок:**
  1. Поля класса
  2. Конструкторы
  3. Публичные методы
  4. Приватные методы
  5. Метод main

### Перегрузка методов
- **Используется:** Методы `Write()` и `Read()` с разными сигнатурами
- **Файл:** `L4S4-Maven\src\main\java\Base\Server.java`
- **Пример:**
  ```java
  public String Write(String str) throws IOException { ... }
  public void Write(String str, boolean log) throws IOException { ... }
  public Object Write(Object obj) throws IOException, ClassNotFoundException { ... }
  ```

## Несоответствия и отклонения

### Критические нарушения
1. **Имена методов в PascalCase** вместо camelCase
2. **Имена пакетов в PascalCase** вместо lowercase
3. **Публичные поля** без инкапсуляции
4. **Расширение файла .Java** вместо .java

### Умеренные нарушения
1. **Wildcard импорты** вместо конкретных
2. **Отсутствие логгера** (только System.out/err)
3. **Тройной слеш ///** вместо /** */ для Javadoc
4. **e.printStackTrace()** вместо логирования

### Рекомендации
1. Переименовать все методы в camelCase
2. Переименовать все пакеты в lowercase
3. Добавить приватные поля с геттерами/сеттерами
4. Использовать стандартный Javadoc формат
5. Добавить логгер (SLF4J + Logback)
6. Заменить wildcard импорты на конкретные
7. Исправить расширение файла `ProcessData.Java` → `ProcessData.java`

---

*Анализ конвенций: 2026-04-06*

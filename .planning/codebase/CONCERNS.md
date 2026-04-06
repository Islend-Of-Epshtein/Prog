# Проблемы кодовой базы

**Дата анализа:** 2026-04-06

---

## 1. Критические проблемы (Critical)

### 1.1 Несуществующие классы — ошибка компиляции
**Серьёзность:** Critical  
**Файлы:**
- `L4S4-Maven\src\main\java\Task2\Task2ConsoleDemo.java` (строки 36, 55)
- `L4S4-Maven\src\main\java\Task2\Controller.java` (строка 61)
- `L4S4-Maven\src\main\java\Task2\Dispatcher.java` (строка 56)

**Проблема:** Код ссылается на классы `Task2Controller` и `Task2Dispatcher`, которые не существуют в проекте. Фактические классы называются `Controller` и `Dispatcher`.

**Примеры:**
```java
// Task2ConsoleDemo.java:36
new Task2Controller(port).start();  // Класс Task2Controller не существует!

// Task2ConsoleDemo.java:55
new Task2Dispatcher(host, port, new ConsoleDispatcherView()).start();  // Класс Task2Dispatcher не существует!

// Controller.java:61
new Task2Controller(port).start();  // Класс Task2Controller не существует!

// Dispatcher.java:56
new Task2Dispatcher(host, port, view).start();  // Класс Task2Dispatcher не существует!
```

**Влияние:** Код Task2 не компилируется. Приложение вылетит с `NoClassDefFoundError` или `ClassNotFoundException` при запуске.

**Решение:** Переименовать классы `Controller` и `Dispatcher` в `Task2Controller` и `Task2Dispatcher`, либо исправить все ссылки на правильные имена классов.

---

### 1.2 Некорректное именование файла
**Серьёзность:** Critical  
**Файл:** `L4S4-Maven\src\main\java\Task2\ProcessData.Java`

**Проблема:** Файл имеет расширение `.Java` с заглавной буквой, что нарушает соглашения Java. В Windows это может работать из-за нечувствительности к регистру, но на Linux/macOS возникнет ошибка компиляции.

**Влияние:** Проект не компилируется на POSIX-системах. IDE может не распознать файл как Java-класс.

**Решение:** Переименовать файл в `ProcessData.java` (нижний регистр).

---

### 1.3 Устаревшая версия JUnit
**Серьёзность:** Critical  
**Файл:** `L4S4-Maven\pom.xml` (строка 21)

**Проблема:** Используется JUnit 3.8.1 (выпущен в 2002 году). Это устаревшая версия без аннотаций, assertions и современных возможностей.

```xml
<dependency>
  <groupId>junit</groupId>
  <artifactId>junit</artifactId>
  <version>3.8.1</version>  <!-- Устарело на 20+ лет! -->
  <scope>test</scope>
</dependency>
```

**Влияние:** Невозможно использовать современные практики тестирования. Ограниченная функциональность. Несовместимость с современными IDE и инструментами.

**Решение:** Обновить до JUnit 5 (JUnit Jupiter):
```xml
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter</artifactId>
  <version>5.10.0</version>
  <scope>test</scope>
</dependency>
```

---

## 2. Проблемы безопасности (High)

### 2.1 Отсутствие валидации ввода порта
**Серьёзность:** High  
**Файлы:**
- `L4S4-Maven\src\main\java\Task3\Dispatcher.java` (строки 121-128)
- `L4S4-Maven\src\main\java\Task3\Controller.java` (строки 176-179)

**Проблема:** Парсинг порта без проверки диапазона. Допускаются порты < 0 или > 65535.

```java
// Dispatcher.java:122
port = Integer.parseInt(portField.getText().trim());  // Нет проверки диапазона!

// Controller.java:176
port = Integer.parseInt(args[1]);  // Может быть 99999 или -1!
```

**Влияние:** `IllegalArgumentException` при попытке создать сокет, или использование привилегированных портов (< 1024).

**Решение:** Добавить валидацию:
```java
int port = Integer.parseInt(portStr);
if (port < 1 || port > 65535) {
    throw new IllegalArgumentException("Порт должен быть от 1 до 65535");
}
```

---

### 2.2 Хардкод порта в ProcessBuilder
**Серьёзность:** High  
**Файл:** `L4S4-Maven\src\main\java\GUI\Task3\Launcher.java` (строка 174)

**Проблема:** Порт 12345 хардкодится при запуске контроллера:

```java
ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className,
        String.valueOf(numberOfUnits), "12345");  // Хардкод!
```

**Влияние:** Невозможно запустить несколько экземпляров системы на одной машине. Конфликты портов.

**Решение:** Использовать конфигурируемый порт или динамическое выделение.

---

### 2.3 Незащищённая передача объектов по сети
**Серьёзность:** High  
**Файлы:**
- `L4S4-Maven\src\main\java\Base\Server.java` (строки 44-49)
- `L4S4-Maven\src\main\java\Base\Client.java` (строки 40-45)
- `L4S4-Maven\src\main\java\Task3\Controller.java` (строки 119-124)

**Проблема:** Использование `ObjectInputStream`/`ObjectOutputStream` без какой-либо безопасности. Java-сериализация уязвима к атакам.

```java
// Server.java:44-49
public Object Write(Object obj) throws IOException, ClassNotFoundException {
    objOut.writeObject(obj);  // Небезопасная сериализация!
    objOut.flush();
    return objIn.readObject();  // Уязвимость десериализации!
}
```

**Влияние:** Уязвимость к атакам десериализации (CVE-2015-4852 и др.). Возможность удалённого выполнения кода.

**Решение:** Использовать безопасный формат (JSON, Protocol Buffers) или добавить whitelist классов для десериализации.

---

### 2.4 Отсутствие аутентификации
**Серьёзность:** High  
**Файлы:** Все серверные компоненты

**Проблема:** Серверы принимают подключения от любого клиента без аутентификации.

```java
// Controller.java:40
clientSocket = serverSocket.accept();  // Любой может подключиться!
```

**Влияние:** Любой может подключиться к серверу и получать данные. Нет контроля доступа.

**Решение:** Добавить механизм аутентификации (токен, логин/пароль, сертификаты).

---

## 3. Проблемы надёжности (High)

### 3.1 Бесконечные циклы без условия выхода
**Серьёзность:** High  
**Файлы:**
- `L4S4-Maven\src\main\java\Task1\ClientReqest.java` (строки 30-39)
- `L4S4-Maven\src\main\java\GUI\Task1\InputAdressFrame.java` (строки 361-372)

**Проблема:** Циклы `while(true)` без механизма корректного завершения.

```java
// ClientReqest.java:30-39
Thread objIn = new Thread(() -> {
    while(true){  // Нет условия выхода!
        try {
            Object obj = client.Read(true);
            if (obj == null) { break; }
        } catch (Exception e) {
            // Пустой catch - ошибка глотается!
        }
    }
});
```

**Влияние:** Потоки могут никогда не завершиться. Утечка ресурсов. Пустой catch блок скрывает ошибки.

**Решение:** Добавить флаг `volatile boolean running` и проверять его в цикле.

---

### 3.2 Пустой catch блок
**Серьёзность:** High  
**Файл:** `L4S4-Maven\src\main\java\Task1\ClientReqest.java` (строки 35-36)

```java
} catch (Exception e) {
    // Ничего не делается - ошибка скрыта!
}
```

**Влияние:** Ошибки silently игнорируются. Невозможно отладить проблемы.

**Решение:** Логировать ошибку или корректно обработать.

---

### 3.3 Пустая реализация метода
**Серьёзность:** Medium  
**Файл:** `L4S4-Maven\src\main\java\Task1\ClientReqest.java` (строки 42-45)

```java
@Override
public void propertyChange(PropertyChangeEvent evt) {
    // Пустая реализация!
}
```

**Влияние:** Некорректное поведение при изменении свойств.

**Решение:** Реализовать логику или удалить интерфейс, если он не нужен.

---

### 3.4 Заглушка GUI-компонента
**Серьёзность:** High  
**Файл:** `L4S4-Maven\src\main\java\GUI\Task2\ConnectionFrame.java` (строки 1-7)

```java
package GUI.Task2;

public class ConnectionFrame {
    public static void Run(){
        //для SelectMenuFrame - не удалять
    }
}
```

**Проблема:** Класс существует только чтобы не ломать SelectTaskMenu, но функционал не реализован.

**Влияние:** Task2 не работает из GUI-меню. Пользователь нажимает кнопку — ничего не происходит.

**Решение:** Реализовать ConnectionFrame или убрать кнопку Task2 из меню.

---

## 4. Проблемы производительности (Medium)

### 4.1 Sleep в основном потоке
**Серьёзность:** Medium  
**Файл:** `L4S4-Maven\src\main\java\Task3\Dispatcher.java` (строки 190-194)

```java
try {
    Thread.sleep(500);  // Блокировка EDT!
} catch (InterruptedException e) {
    e.printStackTrace();
}
```

**Проблема:** `Thread.sleep()` вызывается в SwingUtilities.invokeLater(), что блокирует Event Dispatch Thread.

**Влияние:** UI зависает на 500мс. Приложение выглядит "мёртвым".

**Решение:** Использовать `javax.swing.Timer` для отложенных действий.

---

### 4.2 Неэффективное создание объектов
**Серьёзность:** Low  
**Файл:** `L4S4-Maven\src\main\java\Task3\Dispatcher.java` (строки 349-363)

```java
switch (states[i]) {
    case 0:
        btn.setBackground(Color.GREEN);
        btn.setText("<html><center>Установка " + (i + 1) +
                "<br><font color='black'><b>РАБОТАЕТ</b></font></center></html>");
        break;
    // ... аналогично для других состояний
}
```

**Проблема:** HTML-разметка создаётся при каждом обновлении. Конкатенация строк в цикле.

**Влияние:** Ненужное создание объектов, нагрузка на GC.

**Решение:** Вынести форматирование в константы или использовать StringBuilder.

---

### 4.3 Создание массива при каждом вызове
**Серьёзность:** Low  
**Файл:** `L4S4-Maven\src\main\java\Task3\Dispatcher.java` (строка 249)

```java
int[] states = new int[numberOfUnits];  // Создаётся каждую итерацию!
```

**Влияние:** Ненужное выделение памяти в горячем цикле.

**Решение:** Создать массив один раз в конструкторе и переиспользовать.

---

## 5. Проблемы сопровождаемости (Medium)

### 5.1 God Class — Dispatcher.java (Task3)
**Серьёзность:** Medium  
**Файл:** `L4S4-Maven\src\main\java\Task3\Dispatcher.java` (349+ строк)

**Проблема:** Один класс отвечает за:
- Создание UI подключения
- Создание основного UI
- Управление сетевым соединением
- Приём данных
- Обработку ошибок
- Обновление UI

**Влияние:** Сложно тестировать, сложно модифицировать, нарушает SRP.

**Решение:** Разделить на:
- `ConnectionDialog` — UI подключения
- `DispatcherUI` — основной UI
- `NetworkClient` — сетевое взаимодействие
- `DispatcherController` — координация

---

### 5.2 God Class — InputAdressFrame.java
**Серьёзность:** Medium  
**Файл:** `L4S4-Maven\src\main\java\GUI\Task1\InputAdressFrame.java` (333 строки)

**Проблема:** Класс содержит UI, логику подключения, создание серверов, обработку файлов.

**Решение:** Применить MVC или MVP паттерн.

---

### 5.3 Дублирование кода
**Серьёзность:** Medium  
**Файлы:** 
- `L4S4-Maven\src\main\java\Base\Server.java`
- `L4S4-Maven\src\main\java\Base\Client.java`

**Проблема:** Идентичные методы `Write()`, `Read()`, `Off()` в обоих классах.

```java
// Server.java и Client.java — почти идентичны!
public Object Write(Object obj) throws IOException, ClassNotFoundException {
    objOut.writeObject(obj);
    objOut.flush();
    return objIn.readObject();
}
```

**Решение:** Вынести общий код в абстрактный базовый класс или utility-класс.

---

### 5.4 Дублирование UI-кода
**Серьёзность:** Low  
**Файлы:**
- `L4S4-Maven\src\main\java\SelectTaskMenu.java`
- `L4S4-Maven\src\main\java\GUI\Task1\InputAdressFrame.java`

**Проблема:** Метод `CreateButton()` дублируется с небольшими отличиями.

**Решение:** Создать `ButtonFactory` класс.

---

### 5.5 Несогласованные имена методов
**Серьёзность:** Low  
**Файлы:**
- `Base/Server.java`, `Base/Client.java`

**Проблема:** Методы используют PascalCase вместо Java camelCase:
- `Write()` → должно быть `write()`
- `Read()` → должно быть `read()`
- `IsBound()` → должно быть `isBound()`
- `GetAddress()` → должно быть `getAddress()`
- `GetPort()` → должно быть `getPort()`
- `Off()` → должно быть `close()` или `disconnect()`

**Влияние:** Нарушение Java Code Conventions. Несогласованность с JDK.

---

### 5.6 Магические числа
**Серьёзность:** Low  
**Файлы:** Множество

```java
// Launcher.java:174
"12345"  // Хардкод порта

// Dispatcher.java:78-79
portField = new JTextField("localhost", 15);
portField = new JTextField("12345", 15);

// Dispatcher.java:160
socket.connect(new InetSocketAddress(host, port), 5000);  // 5000ms timeout

// Controller.java:76
Thread.sleep(2000);  // 2000ms интервал
```

**Решение:** Вынести в константы или конфигурацию.

---

## 6. Технический долг (Medium)

### 6.1 Пустые обработчики кнопок
**Серьёзность:** High  
**Файл:** `L4S4-Maven\src\main\java\GUI\Task1\InputAdressFrame.java` (строки 257-262)

```java
clientTransfer.addActionListener(e -> {
    // передача клиенту
});
serverTransfer.addActionListener(e -> {
    // передача серверу
});
```

**Проблема:** Кнопки "Передать клиенту" и "Передать серверу" не реализованы.

**Влияние:** Функционал не работает. Пользователь вводит в заблуждение.

**Решение:** Реализовать или убрать кнопки.

---

### 6.2 Фиктивные тесты
**Серьёзность:** Medium  
**Файл:** `L4S4-Maven\src\test\java\org\build\AppTest.java`

```java
public void testApp()
{
    assertTrue( true );  // Тест ничего не проверяет!
}
```

**Проблема:** Тест всегда проходит. Нет реальной проверки.

**Влияние:** Ложное чувство безопасности. Нет покрытия кода тестами.

**Решение:** Написать реальные тесты или удалить фиктивные.

---

### 6.3 Использование e.printStackTrace()
**Серьёзность:** Medium  
**Файлы:** 12 вхождений

**Проблема:** `e.printStackTrace()` выводит стек в stderr, но не логирует правильно.

```java
// Множество файлов
catch (Exception e) {
    e.printStackTrace();  // Неудовлетворительное логирование!
}
```

**Влияние:** Ошибки теряются в продакшене. Нет единого подхода к логированию.

**Решение:** Использовать логгер (SLF4J, java.util.logging):
```java
logger.error("Ошибка соединения", e);
```

---

### 6.4 System.exit() в библиотечном коде
**Серьёзность:** Medium  
**Файлы:**
- `L4S4-Maven\src\main\java\Task3\Dispatcher.java` (строка 314)
- `L4S4-Maven\src\main\java\GUI\Task3\Launcher.java` (строка 155)

```java
System.exit(0);  // Принудительное завершение JVM!
```

**Проблема:** `System.exit()` завершает всю JVM, что может быть нежелательно.

**Решение:** Использовать флаг завершения и позволить вызывающему коду решить.

---

## 7. Проблемы тестирования (High)

### 7.1 Отсутствие реальных тестов
**Серьёзность:** High  
**Файл:** `L4S4-Maven\src\test\java\org\build\AppTest.java`

**Проблема:** Единственный тест ничего не проверяет.

**Влияние:** Нет защиты от регрессий. Рефакторинг рискован.

**Решение:** Добавить модульные тесты для:
- `ProcessData.serialize()` / `deserialize()`
- `ProcessDataGenerator.next()`
- Сетевого взаимодействия (с mock)

---

### 7.2 Тестируемость кода
**Серьёзность:** Medium  
**Файлы:** GUI-классы

**Проблема:** UI-код тесно связан с логикой. Невозможно unit-тестировать без запуска GUI.

**Решение:** Применить MVC/MVP для отделения логики от UI.

---

## 8. Проблемы ресурсов (Medium)

### 8.1 Утечка ресурсов при исключении
**Серьёзность:** Medium  
**Файл:** `L4S4-Maven\src\main\java\Base\Server.java` (строки 73-81)

```java
public void Off() throws IOException {
    if(clientSocket!=null) {
        this.clientSocket.close();
        this.in.close();      // Если здесь исключение...
        this.objIn.close();   // ...это не выполнится!
        this.out.close();
        this.objOut.close();
    }
    serverSocket.close();  // ...и это тоже!
}
```

**Проблема:** Если `in.close()` выбросит исключение, остальные потоки не закроются.

**Решение:** Использовать try-with-resources или закрывать каждый в отдельном try.

---

### 8.2 Отсутствие таймаута при создании ObjectInputStream
**Серьёзность:** Low  
**Файлы:** `Base/Server.java`, `Base/Client.java`

**Проблема:** `ObjectInputStream` блокируется до получения заголовка потока.

```java
objIn = new ObjectInputStream(clientSocket.getInputStream());  // Блокировка!
```

**Влияние:** Возможна вечная блокировка при некорректном клиенте.

**Решение:** Установить `SoTimeout` на сокете перед созданием потоков.

---

## 9. Проблемы архитектуры (Medium)

### 9.1 Циклические зависимости
**Серьёзность:** Low  
**Файлы:**
- `GUI\Task1\InputAdressFrame.java` импортирует `Task1.ClientReqest`
- `Task1.ClientReqest` не зависит от GUI

**Проблема:** GUI прямо зависит от domain-классов.

**Решение:** Внедрить слой DTO/ViewModel.

---

### 9.2 Отсутствие интерфейсов для сетевых компонентов
**Серьёзность:** Low  
**Файлы:** `Base/Server.java`, `Base/Client.java`

**Проблема:** Классы тесно связаны с реализацией сокетов.

**Решение:** Создать интерфейсы `INetworkServer`, `INetworkClient` для mock-тестирования.

---

## Резюме

| Категория | Количество | Приоритет |
|-----------|------------|-----------|
| Критические ошибки компиляции | 3 | 🔴 Немедленно |
| Проблемы безопасности | 4 | 🔴 Высокий |
| Проблемы надёжности | 4 | 🟠 Высокий |
| Проблемы производительности | 3 | 🟡 Средний |
| Проблемы сопровождаемости | 6 | 🟡 Средний |
| Технический долг | 4 | 🟡 Средний |
| Проблемы тестирования | 2 | 🟠 Высокий |

**Рекомендуемый порядок исправления:**
1. 🔴 Исправить ошибки компиляции (Task2Controller/Task2Dispatcher, ProcessData.Java)
2. 🔴 Обновить JUnit до версии 5
3. 🔴 Добавить валидацию портов
4. 🟠 Реализовать или убрать незавершённый функционал (ConnectionFrame, кнопки передачи)
5. 🟠 Добавить базовые тесты
6. 🟡 Рефакторинг God-классов

---

*Анализ проблем: 2026-04-06*

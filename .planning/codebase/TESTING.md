# Паттерны тестирования

**Дата анализа:** 2026-04-06

## Фреймворк тестирования

### Используемый фреймворк
- **Название:** JUnit
- **Версия:** 3.8.1 (определена в `pom.xml`)
- **Тип:** JUnit 3 (устаревшая версия)
- **Статус:** ❌ **Критически устаревшая версия** (JUnit 5.x актуален)

### Конфигурация Maven
```xml
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>3.8.1</version>
    <scope>test</scope>
</dependency>
```
- **Файл:** `L4S4-Maven\pom.xml`

### Конфигурационные файлы
- **Файлы конфигурации тестов:** Отсутствуют
- **Отсутствует:** `junit-platform.properties`, `junit5.xml`, аннотации конфигурации

## Структура тестов

### Расположение тестов
- **Директория:** `L4S4-Maven\src\test\java\`
- **Структура:**
  ```
  src/test/java/
  └── org/
      └── build/
          └── AppTest.java
  ```
- **Паттерн:** Maven стандарт (`src/test/java/`)

### Организация тестов
- **Сопутствующее размещение:** ❌ Не используется (тесты в отдельной директории)
- **Параллельность структуры:** Частично соблюдается
  - Исходный код: `org.build` → отсутствует
  - Тесты: `org.build.AppTest`
- **Статус:** ⚠️ Несоответствие пакетов (тестируется несуществующий класс)

## Единственный тестовый файл

### AppTest.java
- **Расположение:** `L4S4-Maven\src\test\java\org\build\AppTest.java`
- **Содержимое:**
  ```java
  package org.build;

  import junit.framework.Test;
  import junit.framework.TestCase;
  import junit.framework.TestSuite;

  public class AppTest extends TestCase {
      public AppTest(String testName) {
          super(testName);
      }

      public static Test suite() {
          return new TestSuite(AppTest.class);
      }

      public void testApp() {
          assertTrue(true);
      }
  }
  ```

### Характеристики теста
- **Наследование:** `extends TestCase` (JUnit 3 стиль)
- **Методы:**
  - `testApp()` — единственный тест
  - `suite()` — создание тестового набора
- **Утверждение:** `assertTrue(true)` — **пустой тест без реальной проверки**
- **Статус:** ❌ Тест не тестирует реальную логику

## Стиль тестирования (JUnit 3)

### Паттерны JUnit 3
```java
// Наследование от TestCase
public class AppTest extends TestCase

// Префикс test для методов тестов
public void testApp()

// Статический метод suite()
public static Test suite()
```

### Проблемы JUnit 3
1. **Устаревший API** (2002 год)
2. **Нет аннотаций** (@Test, @Before, @After)
3. **Нет параметризованных тестов**
4. **Нет ассертов Hamcrest/AssertJ**
5. **Нет поддержки лямбда-выражений**

## Запуск тестов

### Команды Maven
```bash
mvn test                    # Запуск всех тестов
mvn test -Dtest=AppTest     # Запуск конкретного теста
mvn clean test              # Очистка и тесты
```

### Интеграция с IDE
- **IntelliJ IDEA:** Поддерживается через конфигурацию в `.idea/`
- **VS Code:** Базовая конфигурация в `.vscode/settings.json`

## Покрытие тестами

### Тестируемые компоненты
| Компонент | Тесты | Статус |
|-----------|-------|--------|
| `Base.Server` | ❌ | Нет тестов |
| `Base.Client` | ❌ | Нет тестов |
| `Task1.FileServer` | ❌ | Нет тестов |
| `Task1.ClientReqest` | ❌ | Нет тестов |
| `Task2.Controller` | ❌ | Нет тестов |
| `Task2.Dispatcher` | ❌ | Нет тестов |
| `Task2.ProcessData` | ❌ | Нет тестов |
| `Task2.ProcessDataGenerator` | ❌ | Нет тестов |
| `Task3.Controller` | ❌ | Нет тестов |
| `Task3.Dispatcher` | ❌ | Нет тестов |
| GUI компоненты | ❌ | Нет тестов |
| `App` (класс точки входа) | ⚠️ | Пустой тест |

### Общий процент покрытия
- **Оценка:** ~0% (пустой тест `assertTrue(true)`)
- **Реальное покрытие:** 0%

## Типы тестов

### Unit тесты
- **Статус:** ❌ Отсутствуют
- **Рекомендуемые кандидаты:**
  - `ProcessData` — тесты сериализации/десериализации
  - `ProcessDataGenerator` — тесты генерации данных
  - `ProcessData.serialize()` / `ProcessData.deserialize()`

### Integration тесты
- **Статус:** ❌ Отсутствуют
- **Рекомендуемые кандидаты:**
  - Соединение Server-Client
  - Передача данных через сокеты
  - Обработка ошибок сети

### E2E тесты
- **Статус:** ❌ Отсутствуют
- **Рекомендуемые кандидаты:**
  - Полный цикл Task1 (клиент-сервер файловая система)
  - Полный цикл Task2 (контроллер-диспетчер)
  - Полный цикл Task3 (система мониторинга)

### GUI тесты
- **Статус:** ❌ Отсутствуют
- **Сложность:** Swing компоненты требуют специальных фреймворков (AssertJ Swing, FEST)

## Мокирование (Mocking)

### Фреймворки для моков
- **Используются:** ❌ Нет
- **Отсутствуют:** Mockito, EasyMock, JMock

### Что следует мокировать
1. **Сокеты:** `Socket`, `ServerSocket` — для unit тестов
2. **GUI компоненты:** `JFrame`, `JButton` — для изоляции UI логики
3. **Потоки ввода-вывода:** `InputStream`, `OutputStream`

### Пример мока (рекомендуемый)
```java
// С Mockito
@Mock
private Socket mockSocket;

@InjectMocks
private Client client;

@Before
public void setUp() {
    MockitoAnnotations.openMocks(this);
    when(mockSocket.getOutputStream()).thenReturn(mockOutputStream);
}
```

## Тестовые данные

### Фикстуры
- **Статус:** ❌ Отсутствуют
- **Рекомендуемые паттерны:**
  ```java
  @BeforeEach
  public void setUp() {
      testData = new ProcessData(25.5, 1.2);
  }
  ```

### Фабрики тестовых данных
- **Статус:** ❌ Отсутствуют
- **Рекомендуемый подход:** Test Data Builder pattern

### Пример фабрики (рекомендуемый)
```java
public class ProcessDataBuilder {
    private double temperature = 25.0;
    private double pressure = 1.0;

    public ProcessDataBuilder withTemperature(double temp) {
        this.temperature = temp;
        return this;
    }

    public ProcessData build() {
        return new ProcessData(temperature, pressure);
    }
}
```

## Паттерны тестирования

### Arrange-Act-Assert (AAA)
- **Статус:** ❌ Не используется
- **Рекомендуемый формат:**
  ```java
  @Test
  public void testSerialize() {
      // Arrange
      ProcessData data = new ProcessData(25.5, 1.2);

      // Act
      String result = data.serialize();

      // Assert
      assertEquals("25.50;1.20", result);
  }
  ```

### Тестирование граничных случаев
- **Статус:** ❌ Отсутствует
- **Рекомендуемые тесты для `ProcessData.deserialize()`:**
  - null строка
  - пустая строка
  - строка без разделителя
  - строка с некорректными числами
  - отрицательные значения

### Тестирование исключений
- **Статус:** ❌ Отсутствует
- **Рекомендуемый подход (JUnit 5):**
  ```java
  @Test
  void testDeserializeNull() {
      assertThrows(IllegalArgumentException.class, () -> {
          ProcessData.deserialize(null);
      });
  }
  ```

## Проблемы и пробелы в тестировании

### Критические проблемы
1. **Отсутствие реальных тестов** — единственный тест `assertTrue(true)`
2. **Устаревшая версия JUnit** — 3.8.1 (2002 год)
3. **Отсутствие покрытия** — 0% кода протестировано
4. **Несоответствие пакетов** — тесты для несуществующего класса `App`

### Умеренные проблемы
1. **Отсутствие моков** — невозможно тестировать сетевой код изолированно
2. **Нет тестовых фикстур** — дублирование кода создания объектов
3. **Нет параметризованных тестов** — сложно протестировать множество входных данных

### Технический долг
1. Миграция с JUnit 3.8.1 на JUnit 5.x
2. Создание тестов для всех публичных методов
3. Добавление интеграционных тестов для сокетов
4. Настройка покрытия кода (JaCoCo)

## Рекомендуемые тесты

### Приоритет 1: Unit тесты для ProcessData
```java
// L4S4-Maven/src/test/java/Task2/ProcessDataTest.java
class ProcessDataTest {
    @Test
    void testSerialize() {
        ProcessData data = new ProcessData(25.5, 1.2);
        assertEquals("25.50;1.20", data.serialize());
    }

    @Test
    void testDeserializeValid() {
        ProcessData data = ProcessData.deserialize("25.50;1.20");
        assertEquals(25.5, data.getTemperature());
        assertEquals(1.2, data.getPressure());
    }

    @Test
    void testDeserializeNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            ProcessData.deserialize(null);
        });
    }

    @Test
    void testDeserializeInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> {
            ProcessData.deserialize("invalid");
        });
    }
}
```

### Приоритет 2: Unit тесты для ProcessDataGenerator
```java
// L4S4-Maven/src/test/java/Task2/ProcessDataGeneratorTest.java
class ProcessDataGeneratorTest {
    @Test
    void testNextReturnsNonNull() {
        ProcessDataGenerator generator = new ProcessDataGenerator();
        ProcessData data = generator.next();
        assertNotNull(data);
    }

    @Test
    void testNextTemperatureInRange() {
        ProcessDataGenerator generator = new ProcessDataGenerator();
        for (int i = 0; i < 100; i++) {
            ProcessData data = generator.next();
            assertTrue(data.getTemperature() >= 0.0);
            assertTrue(data.getTemperature() <= 100.0);
        }
    }

    @Test
    void testNextPressureInRange() {
        ProcessDataGenerator generator = new ProcessDataGenerator();
        for (int i = 0; i < 100; i++) {
            ProcessData data = generator.next();
            assertTrue(data.getPressure() >= 0.0);
            assertTrue(data.getPressure() <= 6.0);
        }
    }
}
```

### Приоритет 3: Интеграционные тесты для Server-Client
```java
// L4S4-Maven/src/test/java/Base/ServerClientIntegrationTest.java
class ServerClientIntegrationTest {
    private Server server;
    private Client client;

    @BeforeEach
    void setUp() throws IOException {
        server = new Server();
        // Запуск сервера в отдельном потоке
        new Thread(() -> {
            try {
                server.Accept();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        client = new Client(InetAddress.getLocalHost(), server.GetLocalPort());
    }

    @AfterEach
    void tearDown() throws IOException {
        client.Off();
        server.Off();
    }

    @Test
    void testWriteAndRead() throws IOException {
        String response = client.Write("test message");
        assertNotNull(response);
    }
}
```

## Рекомендации по улучшению

### Миграция на JUnit 5
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.0</version>
    <scope>test</scope>
</dependency>
```

### Добавление Mockito
```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.5.0</version>
    <scope>test</scope>
</dependency>
```

### Добавление AssertJ (улучшенные assert'ы)
```xml
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <version>3.24.2</version>
    <scope>test</scope>
</dependency>
```

### Настройка JaCoCo (покрытие кода)
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Целевые показатели покрытия
- **Unit тесты:** Минимум 70% покрытия
- **Критические пути:** 100% покрытия (сериализация, сетевое взаимодействие)
- **Общий coverage:** Минимум 60%

---

*Анализ тестирования: 2026-04-06*

# Програма статистики відеоігор

Консольна програма для парсингу JSON-файлів з даними про відеоігри та генерації статистики у XML форматі. Програма підтримує паралельну обробку з використанням пулів потоків для покращення продуктивності при роботі з великими наборами даних.

## Предметна область

Програма працює з доменом **Відеоігри**, де:
- **Game** (основна сутність) - представляє відеогру з атрибутами: title, developer, year_released, genre
- **Developer** (вторинна сутність) - представляє розробника (зв'язок багато-до-одного: багато ігор можуть мати одного розробника)

### Атрибути Game
- `title` - String, назва гри
- `developer` - String, назва розробника
- `year_released` - Integer, рік випуску
- `genre` - String, список жанрів через кому (наприклад, "RPG, Action Adventure")

## Структура проекту

```
src/
├── main/
│   ├── java/com/profitsoft/gamestats/
│   │   ├── model/              # Класи сутностей
│   │   │   ├── Game.java
│   │   │   └── Developer.java
│   │   ├── parser/             # Логіка парсингу JSON
│   │   │   ├── GameJsonParser.java
│   │   │   └── ParallelGameJsonParser.java
│   │   ├── statistics/         # Розрахунок статистики
│   │   │   ├── StatisticsCalculator.java
│   │   │   └── StatisticItem.java
│   │   ├── xml/                # Генерація XML
│   │   │   └── XmlGenerator.java
│   │   └── GameStatisticsApplication.java  # Точка входу
│   └── resources/
│       └── sample-data/        # Приклади JSON файлів для тестування
│           ├── games1.json
│           ├── games2.json
│           └── games3.json
└── test/
    └── java/com/profitsoft/gamestats/
        ├── parser/
        │   └── GameJsonParserTest.java
        └── statistics/
            └── StatisticsCalculatorTest.java
```

## Вимоги

- Java 21
- Maven 3.x

## Збірка проекту

```bash
mvn clean compile
```

## Запуск тестів

```bash
mvn test
```

## Запуск програми

### Базове використання

```bash
mvn exec:java "-Dexec.args=<directory> <attribute>"
```

### З використанням пулу потоків

```bash
mvn exec:java "-Dexec.args=<directory> <attribute> <threadCount>"
```

### Параметри
- `directory` - Шлях до папки з JSON файлами
- `attribute` - Атрибут для розрахунку статистики (developer, year_released, genre)
- `threadCount` - Кількість потоків для паралельного парсингу (опціонально, за замовчуванням: 1)

### Приклад

```bash
# Використання прикладових даних з 1 потоком
mvn exec:java "-Dexec.args=src/main/resources/sample-data genre 1"

# Використання прикладових даних з 4 потоками
mvn exec:java "-Dexec.args=src/main/resources/sample-data genre 4"

# Статистика по розробниках
mvn exec:java "-Dexec.args=src/main/resources/sample-data developer 2"

# Статистика по роках випуску
mvn exec:java "-Dexec.args=src/main/resources/sample-data year_released 1"
```

## Формат вхідних файлів

JSON файли повинні містити масив об'єктів ігор:

```json
[
  {
    "title": "The Witcher 3: Wild Hunt",
    "developer": "CD Projekt Red",
    "year_released": 2015,
    "genre": "RPG, Action Adventure"
  },
  {
    "title": "Cyberpunk 2077",
    "developer": "CD Projekt Red",
    "year_released": 2020,
    "genre": "RPG, Action, Open World"
  }
]
```

## Формат вихідних даних

Програма генерує XML файл з назвою `statistics_by_{attribute}.xml` з наступною структурою:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<statistics>
  <item>
    <value>RPG</value>
    <count>2</count>
  </item>
  <item>
    <value>Action</value>
    <count>1</count>
  </item>
</statistics>
```

Результати відсортовані за кількістю у порядку зменшення.

## Експерименти з продуктивністю

Програма підтримує паралельний парсинг файлів з налаштовуваним пулом потоків. Для порівняння продуктивності з різною кількістю потоків:

```bash
# Тест з 1 потоком
mvn exec:java "-Dexec.args=src/main/resources/sample-data genre 1"

# Тест з 2 потоками
mvn exec:java "-Dexec.args=src/main/resources/sample-data genre 2"

# Тест з 4 потоками
mvn exec:java "-Dexec.args=src/main/resources/sample-data genre 4"

# Тест з 8 потоками
mvn exec:java "-Dexec.args=src/main/resources/sample-data genre 8"
```

### Результати

#### Малий набір даних (3 файли, 9 ігор)

| Потоки | Парсинг | Розрахунок | XML генерація |
|--------|---------|------------|--------------|
| 1      | 41ms    | 2ms        | 5ms          |
| 2      | 39ms    | 3ms        | 5ms          |
| 4      | 42ms    | 4ms        | 5ms          |
| 8      | 40ms    | 3ms        | 5ms          |

#### Великий набір даних (4 файли, 89 ігор)

| Потоки | Парсинг | Розрахунок | XML генерація |
|--------|---------|------------|--------------|
| 1      | 53ms    | 3ms        | 6ms          |
| 2      | 56ms    | 4ms        | 6ms          |
| 4      | 56ms    | 3ms        | 8ms          |
| 8      | 63ms    | 3ms        | 6ms          |

З малими та середніми файлами різниця в продуктивності між різною кількістю потоків мінімальна через накладні витрати на управління потоками. Покращення продуктивності найбільш помітне з дуже великими файлами (більше 1MB) та більшою кількістю файлів для обробки.

## Ключові особливості

1. **Стрімінговий парсинг JSON**: Використовує стрімінговий API Jackson для уникнення завантаження цілих файлів в пам'ять
2. **Підтримка пулу потоків**: Налаштовувана паралельна обробка для покращення продуктивності
3. **Кілька атрибутів**: Підтримка статистики для developer, year_released та genre
4. **Розділення жанрів**: Автоматичне розділення значень жанрів через кому
5. **Відсортовані результати**: Статистика відсортована за кількістю у порядку зменшення
6. **Unit тести**: Комплексне покриття тестами для логіки парсингу та статистики

## Технічна реалізація

- **JSON парсинг**: Jackson Core streaming API для ефективного парсингу з точки зору пам'яті
- **Конкурентність**: ExecutorService з фіксованим пулом потоків для паралельної обробки файлів
- **Статистика**: Підрахунок на основі HashMap з сортуванням за частотою
- **Генерація XML**: Ручне записування XML з proper escaping
- **Обробка помилок**: IOException та IllegalArgumentException для некоректних вхідних даних

# doc-json-validator

Валидатор документов (`.docx`, `.csv`) и JSON-коллекций (Postman).

## Как проверить работу приложения

### 1. Изменить тестовые файлы

Отредактируйте файлы в одной из папок:

| Папка | Что проверяется |
|---|---|
| `check_list_check/` | Сравнение `.docx` файлов (чек-лист) |
| `docs_servis_check/` | Сравнение `.docx` файлов (документация) |
| `doc_load_check/` | Сравнение `.csv` файлов |
| `ckeck_json_check/` | Анализ Postman коллекции (JSON) |

### 2. Залить изменения в Git

```bash
git add .
git commit -m "тест: описание изменений"
git push origin main
```

### 3. Проверить GitHub Actions

После пуша автоматически запустится CI/CD. Статус:
**https://github.com/JumperAlexey/doc-json-validator/actions**

## Основные команды Git

| Команда | Описание |
|---|---|
| `git status` | Показать изменённые файлы |
| `git add .` | Добавить все изменения в индекс |
| `git commit -m "описание"` | Закоммитить изменения |
| `git push origin main` | Отправить изменения на GitHub |
| `git pull origin main` | Получить обновления с GitHub |

## Локальный запуск

```bash
mvn clean package -DskipTests
java -jar target/doc-json-validator-1.0.jar all
```

### Режимы запуска:

- `docs-servis` — проверка документации
- `check-list` — проверка чек-листа
- `csv` — проверка CSV файлов
- `json` — проверка Postman коллекции
- `all` — все проверки

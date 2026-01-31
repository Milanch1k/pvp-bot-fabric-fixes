# 🚀 Railway Deployment - Quick Start

## ✅ Что уже сделано

1. ✅ Создан backend сервер (`backend/server.py`)
2. ✅ Добавлены все необходимые файлы для Railway:
   - `Procfile` - команда запуска
   - `railway.json` - конфигурация
   - `runtime.txt` - версия Python
   - `requirements.txt` - зависимости
3. ✅ Создан сайт на GitHub Pages (`docs/`)
4. ✅ Добавлена отправка статистики в мод (`StatsReporter.java`)

## 📋 Что нужно сделать

### 1. Задеплоить backend на Railway

```
1. Зайди на https://railway.app
2. New Project → Deploy from GitHub repo
3. Выбери свой репозиторий
4. Settings → Root Directory → установи "backend"
5. Дождись деплоя
6. Скопируй URL (типа https://your-app.up.railway.app)
```

**Подробная инструкция:** `backend/DEPLOYMENT.md`

### 2. Обновить URL в моде

Файл: `src/main/java/org/stepan1411/pvp_bot/stats/StatsReporter.java`

Замени:
```java
private static final String STATS_ENDPOINT = "https://api.github.com/gists/YOUR_GIST_ID";
```

На:
```java
private static final String STATS_ENDPOINT = "https://your-app.up.railway.app/api/stats";
```

### 3. Обновить URL на сайте

Файл: `docs/script.js`

Замени:
```javascript
const BACKEND_URL = 'https://your-app.up.railway.app/api/stats';
```

На свой URL от Railway.

### 4. Пересобрать мод

```bash
./gradlew build
```

### 5. Протестировать

1. Запусти сервер с модом
2. Открой `https://your-app.up.railway.app/api/stats`
3. Должна появиться статистика
4. Открой свой сайт на GitHub Pages
5. Статистика должна обновиться

## 🔧 Исправленные проблемы

- ❌ **Было:** "ModuleNotFoundError: No module named 'main'"
- ✅ **Исправлено:** Добавлен `Procfile` с правильной командой `gunicorn server:app`

- ❌ **Было:** Railway не мог найти root directory
- ✅ **Исправлено:** Нужно установить Root Directory = `backend` в настройках

- ❌ **Было:** Backend пытался писать в `../docs/data/stats.json`
- ✅ **Исправлено:** Теперь backend хранит данные в памяти и отдаёт через API

## 📊 Как это работает

```
Minecraft Server (с модом)
    ↓ POST /api/stats (каждый час)
Railway Backend
    ↓ GET /api/stats
GitHub Pages (сайт)
    ↓ показывает статистику
Пользователи
```

## 🎯 Следующие шаги

После успешного деплоя можно добавить:
- [ ] Получение downloads с GitHub API
- [ ] База данных для истории статистики
- [ ] Графики изменения статистики
- [ ] Список топ серверов по количеству ботов

## 📝 Полезные ссылки

- Railway Dashboard: https://railway.app/dashboard
- GitHub Pages: https://stepan1411.github.io/pvpbot-stats/
- Backend API: https://your-app.up.railway.app/api/stats
- Подробная инструкция: `backend/DEPLOYMENT.md`

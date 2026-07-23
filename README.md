# Документация API микросервисного проекта

## Архитектура

---
Проект состоит из 7 микросервисов:

| Сервис	              | Назначение                        | 	Порт         |
|----------------------|-----------------------------------|---------------|
| CONFIG-SERVER	       | Хранение конфигураций             | 	8888         |
| DISCOVERY-SERVICE	   | Сервис обнаружения (Eureka)       | 	8761         |
| GATEWAY-SERVICE      | 	API Gateway                      | 	8080         |
| USER-ADMINISTRATION	 | Управление пользователями	        | динамический  |
| EVENT-MANAGEMENT     | 	Управление событиями	            | динамический  |
| REQUEST-MANAGEMENT   | 	Управление запросами на участие	 | динамический  |
| COMMENTS             | 	Управление комментариями         | 	динамический |
| STATS-SERVER         | 	Сбор статистики	                 | динамический  |

___Базовые URL___

_Gateway:_ http://localhost:8080

_Discovery Service:_ http://localhost:8761

# USER-ADMINISTRATION SERVICE

---

> Управление пользователями системы.

## Административные операции

---

### Создание пользователя

POST /admin/users

Тело запроса:

```json
{
  "email": "user@example.com",
  "name": "Иван Иванов"
}
```

Ответ: 201 CREATED

```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "Иван Иванов"
}
```

### Получение списка пользователей

GET /admin/users?ids={ids}&from={from}&size={size}

Параметры:

| Параметр | Тип        | Обязательный | По умолчанию | Описание                          |
|----------|------------|--------------|--------------|-----------------------------------|
| ids      | List<Long> | Нет          | -            | ID пользователей (макс 100)       |
| from     | Integer    | Нет          | 0            | Количество пропускаемых элементов |
| size     | Integer    | Нет          | 10           | Количество элементов на странице  |

Ответ: 200 OK

```json
[
  {
    "id": 1,
    "email": "user1@example.com",
    "name": "Пользователь 1"
  }
]
```

### Получение пользователя по ID

GET /admin/users/{userId}

Ответ: 200 OK

```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "Иван Иванов"
}
```

### Проверка существования пользователя

GET /admin/users/{userId}/exists

Ответ: 200 OK

```json
true
```

### Удаление пользователя

DELETE /admin/users/{userId}

Ответ: 204 NO CONTENT

### Получение пользователей по списку ID

POST /admin/users/all

Тело запроса:

```json
[
  1,
  2,
  3
]
```

Ответ: 200 OK

```json
{
  "1": {
    "id": 1,
    "name": "Иван Иванов"
  }
}
```

# EVENT-MANAGEMENT SERVICE

---

> Управление событиями, категориями и подборками.

## Публичные операции

---

### Получение событий с фильтрацией

GET
/events?text={text}&categories={categories}&paid={paid}&rangeStart={rangeStart}&rangeEnd={rangeEnd}&onlyAvailable={onlyAvailable}&sort={sort}&from={from}&size={size}

Параметры:

| Параметр      | Тип        | Описание                             |
|---------------|------------|--------------------------------------|
| text          | String     | Поиск в аннотации и описании         |
| categories    | List<Long> | ID категорий                         |
| paid          | Boolean    | Только платные/бесплатные            |
| rangeStart    | String     | Начало периода (yyyy-MM-dd HH:mm:ss) |
| rangeEnd      | String     | Конец периода                        |
| onlyAvailable | Boolean    | Только доступные                     |
| sort          | String     | Сортировка: EVENT_DATE или VIEWS     |
| from          | Integer    | Пропуск элементов                    |
| size          | Integer    | Размер страницы                      |

Ответ: 200 OK

```json
[
  {
    "id": 1,
    "annotation": "Аннотация события",
    "category": {
      "id": 1,
      "name": "Концерты"
    },
    "confirmedRequests": 10,
    "eventDate": "2026-08-01 20:00:00",
    "initiator": {
      "id": 1,
      "name": "Иван Иванов"
    },
    "paid": true,
    "title": "Название события",
    "views": 150
  }
]
```

### Получение события по ID

GET /events/{eventId}

Ответ: 200 OK

```json
{
  "id": 1,
  "annotation": "Аннотация события",
  "category": {
    "id": 1,
    "name": "Концерты"
  },
  "confirmedRequests": 10,
  "createdOn": "2026-07-23 12:00:00",
  "description": "Полное описание события",
  "eventDate": "2026-08-01 20:00:00",
  "initiator": {
    "id": 1,
    "name": "Иван Иванов"
  },
  "location": {
    "lat": 55.7558,
    "lon": 37.6173
  },
  "paid": true,
  "participantLimit": 100,
  "publishedOn": "2026-07-23 15:00:00",
  "requestModeration": true,
  "state": "PUBLISHED",
  "title": "Название события",
  "views": 150
}
```

# Категории

---

## Публичные

---

### Получение категорий

GET /categories?from={from}&size={size}

Ответ: 200 OK

```json
[
  {
    "id": 1,
    "name": "Концерты"
  }
]
```

### Получение категории по ID

GET /categories/{catId}

Ответ: 200 OK

```json
{
  "id": 1,
  "name": "Концерты"
}
```

# Подборки событий

---

## Публичные

---

### Получение подборок

GET /compilations?pinned={pinned}&from={from}&size={size}

Ответ: 200 OK

```json
[
  {
    "id": 1,
    "events": [
      {
        "id": 1,
        "annotation": "Аннотация",
        "category": {
          "id": 1,
          "name": "Концерты"
        },
        "confirmedRequests": 10,
        "eventDate": "2026-08-01 20:00:00",
        "initiator": {
          "id": 1,
          "name": "Иван Иванов"
        },
        "paid": true,
        "title": "Название события",
        "views": 150
      }
    ],
    "pinned": true,
    "title": "Лучшие события"
  }
]
```

### Получение подборки по ID

GET /compilations/{compId}

Параметры пути:

| Параметр | 	Тип	 | Описание     |
|----------|-------|--------------|
| compId	  | Long  | 	ID подборки |

Ответ: 200 OK

```json
{
  "id": 5,
  "events": [
    {
      "id": 12,
      "annotation": "Грандиозный концерт в центре города",
      "category": {
        "id": 1,
        "name": "Концерты"
      },
      "confirmedRequests": 45,
      "eventDate": "2026-08-15 19:00:00",
      "initiator": {
        "id": 3,
        "name": "Анна Смирнова"
      },
      "paid": true,
      "title": "Летний джазовый фестиваль",
      "views": 230
    },
    {
      "id": 18,
      "annotation": "Мастер-класс по современной живописи",
      "category": {
        "id": 2,
        "name": "Мастер-классы"
      },
      "confirmedRequests": 12,
      "eventDate": "2026-08-20 14:00:00",
      "initiator": {
        "id": 7,
        "name": "Елена Козлова"
      },
      "paid": false,
      "title": "Уроки акварели",
      "views": 89
    }
  ],
  "pinned": true,
  "title": "Культурные события августа"
}
```

## Административные операции

---

### Получение событий

GET
/admin/events?users={users}&states={states}&categories={categories}&rangeStart={rangeStart}&rangeEnd={rangeEnd}&from={from}&size={size}

Параметры:

| Параметр   | Тип          | Описание          |
|------------|--------------|-------------------|
| users      | List<Long>   | ID пользователей  |
| states     | List<String> | Статусы событий   |
| categories | List<Long>   | ID категорий      |
| rangeStart | String       | Начало периода    |
| rangeEnd   | String       | Конец периода     |
| from       | Integer      | Пропуск элементов |
| size       | Integer      | Размер страницы   |

Пример запроса:

    GET /admin/events?users=1,2,3&states=PENDING,PUBLISHED&categories=5,8&rangeStart=2026-07-01 00:00:00&rangeEnd=2026-08-01 23:59:59&from=0&size=20

Ответ: 200 OK

```json
[
  {
    "id": 1,
    "annotation": "Грандиозный концерт в центре города",
    "category": {
      "id": 5,
      "name": "Концерты"
    },
    "confirmedRequests": 45,
    "createdOn": "2026-06-15 10:30:00",
    "description": "Полное описание события с деталями программы",
    "eventDate": "2026-08-15 19:00:00",
    "initiator": {
      "id": 2,
      "name": "Иван Петров"
    },
    "location": {
      "lat": 55.7558,
      "lon": 37.6173
    },
    "paid": true,
    "participantLimit": 200,
    "publishedOn": "2026-07-01 12:00:00",
    "requestModeration": true,
    "state": "PUBLISHED",
    "title": "Летний джазовый фестиваль",
    "views": 230
  },
  {
    "id": 3,
    "annotation": "Мастер-класс по живописи",
    "category": {
      "id": 8,
      "name": "Мастер-классы"
    },
    "confirmedRequests": 12,
    "createdOn": "2026-07-10 14:20:00",
    "description": "Обучение основам акварельной живописи",
    "eventDate": "2026-08-20 14:00:00",
    "initiator": {
      "id": 5,
      "name": "Елена Соколова"
    },
    "location": {
      "lat": 55.7512,
      "lon": 37.6184
    },
    "paid": false,
    "participantLimit": 20,
    "publishedOn": null,
    "requestModeration": true,
    "state": "PENDING",
    "title": "Уроки акварели",
    "views": 45
  }
]
```

### Обновление события

PATCH /admin/events/{eventId}

Тело запроса:

```json
{
  "annotation": "Обновленная аннотация",
  "category": 2,
  "description": "Новое описание",
  "eventDate": "2026-09-01 20:00:00",
  "location": {
    "lat": 55.7558,
    "lon": 37.6173
  },
  "paid": true,
  "participantLimit": 200,
  "requestModeration": true,
  "title": "Новое название",
  "stateAction": "PUBLISH_EVENT"
}
```

### Создание категории

POST /admin/categories

Тело запроса:

```json
{
  "name": "Спорт"
}
```

Ответ: 201 CREATED

### Обновление категории

PATCH /admin/categories/{catId}

Тело запроса:

```json
{
  "id": 1,
  "name": "Спортивные мероприятия"
}
```

### Удаление категории

DELETE /admin/categories/{catId}

Ответ: 204 NO CONTENT

Создание подборки (админ)

POST /admin/compilations

Тело запроса:

```json
{
  "events": [
    1,
    2,
    3
  ],
  "pinned": true,
  "title": "Лучшие события лета"
}
```

Ответ: 201 CREATED

```json
{
  "id": 5,
  "events": [
    {
      "id": 1,
      "annotation": "Грандиозный концерт",
      "category": {
        "id": 3,
        "name": "Концерты"
      },
      "confirmedRequests": 45,
      "eventDate": "2026-08-15 19:00:00",
      "initiator": {
        "id": 1,
        "name": "Иван Иванов"
      },
      "paid": true,
      "title": "Летний джазовый фестиваль",
      "views": 230
    },
    {
      "id": 2,
      "annotation": "Выставка современного искусства",
      "category": {
        "id": 7,
        "name": "Выставки"
      },
      "confirmedRequests": 23,
      "eventDate": "2026-08-20 18:00:00",
      "initiator": {
        "id": 2,
        "name": "Мария Петрова"
      },
      "paid": false,
      "title": "Арт-галерея",
      "views": 156
    },
    {
      "id": 3,
      "annotation": "Мастер-класс по кулинарии",
      "category": {
        "id": 12,
        "name": "Мастер-классы"
      },
      "confirmedRequests": 18,
      "eventDate": "2026-08-25 14:00:00",
      "initiator": {
        "id": 3,
        "name": "Алексей Смирнов"
      },
      "paid": true,
      "title": "Итальянская кухня",
      "views": 89
    }
  ],
  "pinned": true,
  "title": "Лучшие события лета"
}
```

### Обновление подборки

PATCH /admin/compilations/{compId}

Тело запроса:

```json
{
  "events": [
    1,
    2,
    4
  ],
  "pinned": false,
  "title": "Обновленная подборка"
}
```

### Удаление подборки

DELETE /admin/compilations/{compId}

Ответ: 204 NO CONTENT

## Приватные операции пользователя

---

### Создание события

POST /users/{userId}/events

Тело запроса:

```json
{
  "annotation": "Аннотация события",
  "category": 1,
  "description": "Полное описание события",
  "eventDate": "2026-08-01 20:00:00",
  "location": {
    "lat": 55.7558,
    "lon": 37.6173
  },
  "paid": false,
  "participantLimit": 0,
  "requestModeration": true,
  "title": "Название события"
}
```

Ответ: 201 CREATED

### Получение событий пользователя

GET /users/{userId}/events?from={from}&size={size}

Параметры запроса:

| Параметр	 | Тип     | 	Обязательный | 	По умолчанию	 | Описание                          |
|-----------|---------|---------------|----------------|-----------------------------------|
| from	     | Integer | 	Нет	         | 0	             | Количество пропускаемых элементов |
| size	     | Integer | 	Нет	         | 10             | 	Количество элементов на странице |

Пример запроса:

    GET /users/5/events?from=0&size=20

Ответ: 200 OK

```json
[
  {
    "id": 12,
    "annotation": "Грандиозный концерт в центре города",
    "category": {
      "id": 5,
      "name": "Концерты"
    },
    "confirmedRequests": 45,
    "eventDate": "2026-08-15 19:00:00",
    "initiator": {
      "id": 5,
      "name": "Анна Смирнова"
    },
    "paid": true,
    "title": "Летний джазовый фестиваль",
    "views": 230
  },
  {
    "id": 18,
    "annotation": "Мастер-класс по современной живописи",
    "category": {
      "id": 8,
      "name": "Мастер-классы"
    },
    "confirmedRequests": 12,
    "eventDate": "2026-08-20 14:00:00",
    "initiator": {
      "id": 5,
      "name": "Анна Смирнова"
    },
    "paid": false,
    "title": "Уроки акварели",
    "views": 89
  },
  {
    "id": 25,
    "annotation": "Выставка современного искусства",
    "category": {
      "id": 3,
      "name": "Выставки"
    },
    "confirmedRequests": 8,
    "eventDate": "2026-09-01 18:00:00",
    "initiator": {
      "id": 5,
      "name": "Анна Смирнова"
    },
    "paid": true,
    "title": "Арт-галерея современников",
    "views": 56
  }
]
```

### Получение события по ID

GET /users/{userId}/events/{eventId}

Параметры пути:

| Параметр | 	Тип	 | Описание                   |
|----------|-------|----------------------------|
| userId	  | Long	 | ID пользователя-инициатора |
| eventId  | 	Long | 	ID события                |

Пример запроса:

    GET /users/5/events/12

Ответ: 200 OK

```json
{
  "id": 12,
  "annotation": "Грандиозный концерт в центре города",
  "category": {
    "id": 5,
    "name": "Концерты"
  },
  "confirmedRequests": 45,
  "createdOn": "2026-07-10 14:30:00",
  "description": "Ежегодный джазовый фестиваль с участием известных музыкантов. В программе: современный джаз, блюз, фьюжн.",
  "eventDate": "2026-08-15 19:00:00",
  "initiator": {
    "id": 5,
    "name": "Анна Смирнова"
  },
  "location": {
    "lat": 55.7558,
    "lon": 37.6173
  },
  "paid": true,
  "participantLimit": 200,
  "publishedOn": "2026-07-15 10:00:00",
  "requestModeration": true,
  "state": "PUBLISHED",
  "title": "Летний джазовый фестиваль",
  "views": 230
}
```

### Обновление события

PATCH /users/{userId}/events/{eventId}

Тело запроса:

```json
{
  "annotation": "Обновленная аннотация",
  "category": 2,
  "description": "Новое описание",
  "eventDate": "2026-09-01 20:00:00",
  "location": {
    "lat": 55.7558,
    "lon": 37.6173
  },
  "paid": true,
  "participantLimit": 50,
  "title": "Новое название",
  "stateAction": "SEND_TO_REVIEW"
}
```

### Получение запросов на участие в событии

GET /users/{userId}/events/{eventId}/requests

Ответ: 200 OK

```json
[
  {
    "created": "2026-07-23 12:00:00",
    "event": 1,
    "id": 1,
    "requester": 2,
    "status": "PENDING"
  }
]
```

### Обновление статусов запросов

PATCH /users/{userId}/events/{eventId}/requests

Тело запроса:

```json
{
  "requestIds": [
    1,
    2
  ],
  "status": "CONFIRMED"
}
```

Ответ: 200 OK

```json
{
  "confirmedRequests": [
    {
      "created": "2026-07-23 12:00:00",
      "event": 1,
      "id": 1,
      "requester": 2,
      "status": "CONFIRMED"
    }
  ],
  "rejectedRequests": [
    {
      "created": "2026-07-23 12:30:00",
      "event": 1,
      "id": 2,
      "requester": 3,
      "status": "REJECTED"
    }
  ]
}
```

# REQUEST-MANAGEMENT SERVICE

---

> Управление запросами на участие в событиях.

## Приватные операции пользователя

---

### Создание запроса на участие

POST /users/{userId}/requests?eventId={eventId}

Параметры:

| Параметр | Тип  | Описание   |
|----------|------|------------|
| eventId  | Long | ID события |

Ответ: 201 CREATED

```json
{
  "created": "2026-07-23 12:00:00",
  "event": 1,
  "id": 1,
  "requester": 2,
  "status": "PENDING"
}
```

### Получение запросов пользователя

GET /users/{userId}/requests

Ответ: 200 OK

```json
[
  {
    "created": "2026-07-23 12:00:00",
    "event": 1,
    "id": 1,
    "requester": 2,
    "status": "PENDING"
  }
]
```

### Отмена запроса на участие

PATCH /users/{userId}/requests/{requestId}/cancel

Ответ: 200 OK

```json
{
  "created": "2026-07-23 12:00:00",
  "event": 1,
  "id": 1,
  "requester": 2,
  "status": "CANCELED"
}
```

## Внутренние операции (Feign Client)

---

### Получение запросов по событию

GET /interior/requests/{eventId}

### Подсчет запросов по статусу

GET /interior/requests/{eventId}/count?requestStates={requestStates}

### Получение запросов по ID

GET /interior/requests/all?requestIds={requestIds}

### Сохранение запросов (пакетно)

POST /interior/requests/save/all

### Подсчет подтвержденных запросов

POST /interior/requests/confirmed/all?requestStatus={requestStatus}

# COMMENTS SERVICE

---

> Управление комментариями к событиям.

## Публичные операции

---

### Получение опубликованных комментариев

GET /public/events/{eventId}/comments?status={status}&page={page}&size={size}&sort={sort}

Параметры:

| Параметр | Тип     | Описание                          |
|----------|---------|-----------------------------------|
| status   | String  | Фильтр по статусу                 |
| page     | Integer | Номер страницы                    |
| size     | Integer | Размер страницы (по умолчанию 20) |
| sort     | String  | Сортировка                        |

Ответ: 200 OK

```json
{
  "content": [
    {
      "id": 1,
      "text": "Отличное событие! Всем рекомендую посетить.",
      "createdAt": "2026-07-23 14:00:00",
      "updatedAt": "2026-07-23 15:00:00",
      "status": "PUBLISHED",
      "eventId": 1,
      "userId": 2,
      "userName": "Петр Петров"
    },
    {
      "id": 3,
      "text": "Было очень познавательно, спасибо организаторам!",
      "createdAt": "2026-07-23 16:30:00",
      "updatedAt": null,
      "status": "PUBLISHED",
      "eventId": 1,
      "userId": 5,
      "userName": "Анна Иванова"
    },
    {
      "id": 5,
      "text": "Хорошая организация, но могло быть и лучше",
      "createdAt": "2026-07-24 10:15:00",
      "updatedAt": "2026-07-24 11:00:00",
      "status": "PUBLISHED",
      "eventId": 1,
      "userId": 8,
      "userName": "Сергей Сидоров"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 5,
  "totalElements": 42,
  "last": false,
  "first": true,
  "size": 10,
  "number": 0,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "numberOfElements": 3,
  "empty": false
}
```

## Приватные операции пользователя

---

### Добавление комментария

POST /users/{userId}/events/{eventId}/comments

Тело запроса:

```json
{
  "text": "Отличное событие! Всем рекомендую."
}
```

Ответ: 201 CREATED

```json
{
  "id": 1,
  "text": "Отличное событие! Всем рекомендую.",
  "createdAt": "2026-07-23 14:00:00",
  "updatedAt": null,
  "status": "PENDING",
  "eventId": 1,
  "userId": 2,
  "userName": "Петр Петров"
}
```

### Обновление комментария

PATCH /users/{userId}/events/{eventId}/comments/{commentId}

Тело запроса:

```json
{
  "text": "Отличное событие! Всем рекомендую. Было очень интересно."
}
```

Ответ: 200 OK

### Удаление комментария

DELETE /users/{userId}/events/{eventId}/comments/{commentId}

Ответ: 204 NO CONTENT

## Административные операции

---

### Обновление статуса комментария

PATCH /admin/comments/{commentId}/status

Тело запроса:

```json
{
  "status": "PUBLISHED"
}
```

Ответ: 200 OK

```json
{
  "id": 1,
  "text": "Отличное событие!",
  "createdAt": "2026-07-23 14:00:00",
  "updatedAt": "2026-07-23 15:00:00",
  "status": "PUBLISHED",
  "eventId": 1,
  "userId": 2,
  "userName": "Петр Петров"
}
```

### Получение комментариев события

GET /admin/comments/events/{eventId}?status={status}&from={from}&size={size}

### Удаление комментария

DELETE /admin/comments/{commentId}

Ответ: 204 NO CONTENT

# STATS-SERVER

---

> Сбор и предоставление статистики.

### Создание записи статистики

POST /hit

Тело запроса:

```json
{
  "app": "ewm-main-service",
  "uri": "/events/1",
  "ip": "127.0.0.1",
  "timestamp": "2026-07-23 12:00:00"
}
```

Ответ: 201 CREATED

### Получение статистики

GET /stats?start={start}&end={end}&uris={uris}&unique={unique}

Параметры:

| Параметр | Тип          | Обязательный | Описание                             |
|----------|--------------|--------------|--------------------------------------|
| start    | String       | Да           | Начало периода (yyyy-MM-dd HH:mm:ss) |
| end      | String       | Да           | Конец периода                        |
| uris     | List<String> | Нет          | Список URI                           |
| unique   | Boolean      | Нет          | Уникальные посещения                 |

Ответ: 200 OK

```json
[
  {
    "app": "ewm-main-service",
    "uri": "/events/1",
    "hits": 150
  }
]
```

# Таблица маршрутов с фильтрами

---

| ID маршрута                   | 	Путь	                                                           | Сервис               | 	Тип	Фильтры |
|-------------------------------|------------------------------------------------------------------|----------------------|--------------|
| comments_user_route           | 	/users/{userId}/events/{eventId}/comments/**                    | 	COMMENTS            | 	Приватный   |
| request-management_route      | 	/users/{userId}/requests/**	                                    | REQUEST-MANAGEMENT   | 	Приватный   |
| user-events_route             | 	/users/{userId}/events/**                                       | 	EVENT-MANAGEMENT    | 	Приватный   |
| event-management_admin_route  | 	/admin/events/**, /admin/categories/**, /admin/compilations/**	 | EVENT-MANAGEMENT	    | Админ        |
| comments_admin_route          | 	/admin/comments/**	                                             | COMMENTS             | 	Админ       |
| user-service_route            | 	/admin/users/**	                                                | USER-ADMINISTRATION	 | Админ        |
| event-management_public_route | 	/events/**, /categories/**, /compilations/**	                   | EVENT-MANAGEMENT     | 	Публичный   |
| comments_public_route	        | /public/events/{eventId}/comments/**	                            | COMMENTS	            | Публичный    |
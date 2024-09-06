# To-Do List API

## Описание

Этот проект представляет собой простой RESTful API для управления задачами в списке To-Do. API реализовано с использованием Spring Boot 3.4 и Java 17. В качестве базы данных используется PostgeSQL. API включает 
функциональность для создания, получения, обновления и удаления задач, а также проверку выходных и праздничных дней. Также реализована сортировка задач по статусу и пагинация

## Технологический стек

- **Java**: 17
- **Spring Boot**: 3.4
- **Spring Data JPA**
- **База данных**: PostgreSQL
- **JUnit 5** для тестирования
- **RestTemplate** для запросов к внешним API

## Реализация проверки выходных дней

Для проверки, попадает ли выбранная дата завершения задачи на выходной день или праздник, мы используем внешний API https://date.nager.at/Api, который предоставляет информацию о праздничных днях в году.

Если срок выполнения задачи совпадает с субботой, воскресеньем или праздничным днем, метод isDayOff рекурсивно ищет следующий рабочий день. 
Информация о праздничных днях из API сохраняется в Map, где ключом является год, а значением — Set строк с датами праздничных дней.
1. **Кеширование**: При первом запросе к API для получения выходных дней на определенный год, результат сохраняется в Map. Это позволяет избежать повторных запросов и значительно ускоряет процесс проверки.
2. **Проверка**: При создании задачи, функция isDayOff() проверяет, является ли дата завершения выходным или праздничным днем. Если да, то предлагается выбрать следующий ближайший рабочий день.

## Документация API
Документация API доступна по адресу: `/swagger-ui.html`.
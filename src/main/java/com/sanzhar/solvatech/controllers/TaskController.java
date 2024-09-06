package com.sanzhar.solvatech.controllers;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanzhar.solvatech.dto.TaskChangeDTO;
import com.sanzhar.solvatech.dto.TaskDTO;
import com.sanzhar.solvatech.models.Task;
import com.sanzhar.solvatech.service.TaskService;
import com.sanzhar.solvatech.util.IncorrectJSONException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Задачи", description = "Управление задачами To-Do List")
public class TaskController {
    
    private final TaskService taskService;

    @Autowired
    public TaskController (TaskService taskService){
        this.taskService = taskService;
    }

    private void checkBindingResult(BindingResult bindingResult) throws IncorrectJSONException {
        if (bindingResult.hasErrors()) {
            StringBuilder msg = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                msg.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("\n");
            }
            throw new IncorrectJSONException(msg.toString());
        }
    }


    @ExceptionHandler(IncorrectJSONException.class)
    public ResponseEntity<String> handleIncorrectJSON(IncorrectJSONException ex){
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(ex.getMessage());
    }


    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFoundException(NoSuchElementException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }


    Map <Integer, Set<String>> holidays = new HashMap<>();

    private String isDayOff(LocalDate deadline) throws JsonMappingException, JsonProcessingException {
        String deadlineString = deadline.toString();
        int year = deadline.getYear();
        String dayOfWeek = deadline.getDayOfWeek().toString();

        if (dayOfWeek.equals("SATURDAY") || dayOfWeek.equals("SUNDAY")){
            return isDayOff(deadline.plusDays(1));
        }

        if (holidays.containsKey(year)){
            if(holidays.get(year).contains(deadlineString)){
                return isDayOff(deadline.plusDays(1));
            }
            return deadlineString;
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://date.nager.at/api/v3/PublicHolidays/";
        url += year + "/kz";
        ResponseEntity <String> response = restTemplate.getForEntity(url, String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(response.getBody());
        Set<String> daysOff = new HashSet<>();

        for (JsonNode one : node) {
            daysOff.add(one.get("date").asText());
        }
        holidays.put(year, daysOff);
        if (daysOff.contains(deadlineString)){
            return isDayOff(deadline.plusDays(1));
        }
        return deadlineString;
    }


    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    

    @Operation(summary = "Создание новой задачи", description = "Создает новую задачу с проверкой выходного дня")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Задача успешно создана"),
            @ApiResponse(responseCode = "406", description = "Выбранная дата завершения задачи попадает на выходной или праздничный день")
    })
    @PostMapping("/tasks")
    public HttpStatus addTask(@RequestBody @Valid TaskDTO taskDTO, BindingResult bindingResult) throws IncorrectJSONException, JsonMappingException, JsonProcessingException{
        checkBindingResult(bindingResult);
        String nextPossible = isDayOff(taskDTO.getDeadline());
        if (!nextPossible.equals(taskDTO.getDeadline().toString())){
            throw new IncorrectJSONException("Выбранный крайний срок падает на выходной, ближайший следующий рабочий день: " + nextPossible);
        }
        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setCreatedAt(calendar.getTime());
        task.setDeadline(taskDTO.getDeadline());
        task.setState("UNFINISHED");
        task.setDescription(taskDTO.getDescription());
        taskService.save(task);
        return HttpStatus.ACCEPTED;
    }



    @Operation(summary = "Получение всех задач", description = "Возвращает список всех задач")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список задач успешно получен")
    })
    @GetMapping("/tasks")
    public List<Task> getTasks(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100000") int size){
        Pageable pageable = PageRequest.of(page, size);
        return taskService.findAll(pageable).getContent();
    }



    @Operation(summary = "Получение списка выполненных задач", description = "Получение списка выполненных задач")
    @GetMapping("/tasks/finished")
    public List<Task> getFinishedTasks(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100000") int size){
        Pageable pageable = PageRequest.of(page, size);
        return taskService.findFinished(pageable).getContent();
    }



    @Operation(summary = "Получение списка невыполненных задач", description = "Получение списка невыполненных задач")
    @GetMapping("/tasks/unfinished")
    public List<Task> getUnfinishedTasks(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100000") int size){
        Pageable pageable = PageRequest.of(page, size);
        return taskService.findUnfinished(pageable).getContent();
    }



    @Operation(summary = "Удаление задачи", description = "Удаляет задачу по её ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Задача успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Задача с данным ID не найдена")
    })
    @DeleteMapping("/tasks/{id}")
    public HttpStatus removeTask(@PathVariable("id") Integer id){
        if (id < 1 || id == null){
            throw new NoSuchElementException("id не может быть пустым или меньше 1");
        }
        taskService.remove(id);
        return HttpStatus.ACCEPTED;
    }



    @Operation(summary = "Обновление задачи", description = "Обновляет заголовок, описание или статус существующей задачи")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Задача успешно обновлена"),
            @ApiResponse(responseCode = "404", description = "Задача с данным ID не найдена")
    })
    @PatchMapping("/tasks/{id}")
    public HttpStatus changeTask(@PathVariable ("id") Integer id, @RequestBody @Valid TaskChangeDTO taskChangeDTO, BindingResult bindingResult) throws IncorrectJSONException, NotFoundException{
        checkBindingResult(bindingResult);
        if (id < 1 || id == null){
            throw new NoSuchElementException("id не может быть пустым или меньше 1");
        }
        Task task = taskService.findTask(id);
        task.setDescription(taskChangeDTO.getDescription());
        task.setTitle(taskChangeDTO.getTitle());
        if (taskChangeDTO.getChangeState()){
            task.setState("FINISHED");
        }
        taskService.save(task);
        return HttpStatus.ACCEPTED;
    }



}
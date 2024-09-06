package com.sanzhar.solvatech;

import static org.junit.jupiter.api.Assertions.*;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import com.sanzhar.solvatech.controllers.TaskController;
import com.sanzhar.solvatech.service.TaskService;


class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRemoveTask_ValidId_ShouldReturnHttpStatusAccepted() {
        int id = 1;
        HttpStatus result = taskController.removeTask(id);
        assertEquals(HttpStatus.ACCEPTED, result);
    }

    @Test
    void testRemoveTask_InvalidId_ShouldThrowNoSuchElementException() {

        int id = 0;
        NoSuchElementException exception = new NoSuchElementException();
        try{
            taskController.removeTask(id);
        }catch(NoSuchElementException ex){
            exception = ex;
        }
        assertEquals("id не может быть пустым или меньше 1", exception.getMessage());
    }

}


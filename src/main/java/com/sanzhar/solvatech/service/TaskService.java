package com.sanzhar.solvatech.service;


import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sanzhar.solvatech.models.Task;
import com.sanzhar.solvatech.repository.TaskRepository;


@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    public void save(Task task){
        taskRepository.save(task);
    }

    public Task findTask(int id) throws NoSuchElementException{
        Optional <Task> opt = taskRepository.findById(id);
        if(opt.isEmpty()){
            throw new NoSuchElementException("No such task with the specified id");
        }
        return opt.get();
    }

    public Page<Task> findAll(Pageable pageable){
        return taskRepository.findAll(pageable);
    }

    public Page<Task> findFinished(Pageable pageable){
        return taskRepository.findByState("FINISHED", pageable);
    }

    public Page<Task> findUnfinished(Pageable pageable){
        return taskRepository.findByState("UNFINISHED", pageable);
    }

    public List<Task> findAll(){
        return taskRepository.findAll();
    }

    public List<Task> findFinished() {
        return taskRepository.findByState("FINISHED");
    }

    public List<Task> findUnfinished() {
        return taskRepository.findByState("UNFINISHED");
    }

    public void remove(int id) {
        taskRepository.deleteById(id);
    }
}

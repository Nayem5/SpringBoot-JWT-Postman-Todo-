package com.example.todoapp.controller;

import com.example.todoapp.entity.Todo;
import com.example.todoapp.entity.User;
import com.example.todoapp.repository.TodoRepository;
import com.example.todoapp.repository.UserRepository;
import com.example.todoapp.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/todos")
public class TodoController {

    @Autowired
    private TodoRepository todoRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private JwtService jwtService;

    // Create a new todo
    @PostMapping
    public ResponseEntity<Todo> createTodo(@RequestHeader("Authorization") String authHeader,
                                           @RequestBody Todo todo) {
        String token = authHeader.substring(7); // remove "Bearer "
        String username = jwtService.extractUsername(token);

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        todo.setUser(user);
        Todo saved = todoRepo.save(todo);
        return ResponseEntity.ok(saved);
    }

    // Get all todos for logged-in user
    @GetMapping
    public ResponseEntity<List<Todo>> getTodos(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Todo> todos = todoRepo.findByUser(user);
        return ResponseEntity.ok(todos);
    }

    // Update a todo
    @PutMapping("/{id}")
    public ResponseEntity<Todo> updateTodo(@RequestHeader("Authorization") String authHeader,
                                           @PathVariable Long id,
                                           @RequestBody Todo todoDetails) {
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Todo todo = todoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found"));

        // Only allow update if the todo belongs to this user
        if (!todo.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build(); // forbidden
        }

        todo.setTitle(todoDetails.getTitle());
        todo.setCompleted(todoDetails.isCompleted());
        Todo updated = todoRepo.save(todo);
        return ResponseEntity.ok(updated);
    }

    // Delete a todo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@RequestHeader("Authorization") String authHeader,
                                           @PathVariable Long id) {
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Todo todo = todoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found"));

        if (!todo.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        todoRepo.delete(todo);
        return ResponseEntity.ok().build();
    }
}

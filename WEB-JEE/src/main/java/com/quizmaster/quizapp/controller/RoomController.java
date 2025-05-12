package com.quizmaster.quizapp.controller;

import com.quizmaster.quizapp.RoomWebSocketHandler;
import com.quizmaster.quizapp.service.RoomService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/room")
@CrossOrigin(origins = "*")  
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping("/{code}/join")
    public List<String> joinRoom(@PathVariable String code, @RequestParam String playerName) {
        roomService.addPlayerToRoom(code, playerName);
        return roomService.getPlayers(code);
    }

    @GetMapping("/{code}/players")
    public List<String> getPlayers(@PathVariable String code) {
        return roomService.getPlayers(code);
    }
}


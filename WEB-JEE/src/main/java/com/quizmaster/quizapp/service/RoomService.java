package com.quizmaster.quizapp.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

@Service
public class RoomService {

    // Utilisation d'une ConcurrentHashMap pour stocker les joueurs par code de room
    private final ConcurrentHashMap<String, List<String>> rooms = new ConcurrentHashMap<>();

    // Ajouter un joueur à une room
    public void addPlayerToRoom(String code, String playerName) {
        rooms.computeIfAbsent(code, k -> new ArrayList<>()).add(playerName);
    }

    // Récupérer la liste des joueurs dans une room
    public List<String> getPlayers(String code) {
        return rooms.getOrDefault(code, new ArrayList<>());
    }
}

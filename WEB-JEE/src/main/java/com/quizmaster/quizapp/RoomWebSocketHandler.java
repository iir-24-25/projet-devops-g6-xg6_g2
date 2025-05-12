package com.quizmaster.quizapp;

import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RoomWebSocketHandler extends TextWebSocketHandler {

    private static final Map<String, List<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = getPathVariable(session, "roomId");
        String pseudo = getQueryParam(session, "pseudo");

        session.getAttributes().put("pseudo", pseudo);
        session.getAttributes().put("roomId", roomId);

        roomSessions.computeIfAbsent(roomId, k -> new ArrayList<>()).add(session);

        System.out.println("🎮 Nouveau joueur: " + pseudo + " dans la room " + roomId);
    }

    public static List<String> getPlayersInRoom(String roomId) {
        return roomSessions.getOrDefault(roomId, List.of()).stream()
            .map(s -> (String) s.getAttributes().get("pseudo"))
            .collect(Collectors.toList());
    }

    public static void broadcastStart(String roomId) {
        List<WebSocketSession> sessions = roomSessions.getOrDefault(roomId, List.of());
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(new TextMessage("{\"type\":\"start_quiz\"}"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getPathVariable(WebSocketSession session, String key) {
        return session.getUri().getPath().split("/")[3];
    }

    private String getQueryParam(WebSocketSession session, String key) {
        return Arrays.stream(session.getUri().getQuery().split("&"))
                .map(s -> s.split("="))
                .filter(pair -> pair[0].equals(key))
                .map(pair -> pair[1])
                .findFirst()
                .orElse(null);
    }
}


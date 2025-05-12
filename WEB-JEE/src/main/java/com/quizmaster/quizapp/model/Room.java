/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.quizmaster.quizapp.model;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private String code;
    private List<String> players = new ArrayList<>();

    // Getter pour le code de la room
    public String getCode() {
        return code;
    }

    // Setter pour le code de la room
    public void setCode(String code) {
        this.code = code;
    }

    // Getter pour la liste des joueurs
    public List<String> getPlayers() {
        return players;
    }

    // Setter pour la liste des joueurs
    public void setPlayers(List<String> players) {
        this.players = players;
    }

    // Méthode pour ajouter un joueur
    public void addPlayer(String playerName) {
        this.players.add(playerName);
    }
}


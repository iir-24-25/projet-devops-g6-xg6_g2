package com.quizmaster.quizapp.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index.html";
    }

    @GetMapping("/create")
    public String createQuiz() {
        return "create.html";
    }

    @GetMapping("/auto")
    public String showAutoPage() {
        return "auto.html";
    }

    // Version unifiée pour /play avec paramètre optionnel
    @GetMapping("/play")
    public ModelAndView playQuiz(@RequestParam(required = false) String roomId) {
        if (roomId != null && !roomId.isEmpty()) {
            ModelAndView mav = new ModelAndView("group_quiz.html");
            mav.addObject("roomId", roomId);
            return mav;
        }
        return new ModelAndView("play-quiz.html");
    }
    
    @GetMapping("/multiplayer")
    public ModelAndView multiplayer() {
        return new ModelAndView("multiplayer"); // regarde dans /templates/multiplayer.html
    }


    // Version alternative avec chemin variable (/play/{roomId})
    @GetMapping("/play/{roomId}")
    public ModelAndView playQuizWithRoomId(@PathVariable String roomId) {
        ModelAndView mav = new ModelAndView("group_quiz.html");
        mav.addObject("roomId", roomId);
        return mav;
    }
    
    


    @GetMapping("/results")
    public String showResults() {
        return "results.html";
    }

    @GetMapping("/play-solo")
    public ModelAndView playQuizSolo(@RequestParam(required = false) String roomId) {
        ModelAndView mav = new ModelAndView("quiz-solo.html");
        if (roomId != null) {
            mav.addObject("roomId", roomId);
        }
        return mav;
    }
    
    @GetMapping("/waiting")
    public ModelAndView waitingPage(@RequestParam String roomId) {
        ModelAndView mav = new ModelAndView("waiting.html");
        mav.addObject("roomId", roomId);
        return mav;
    }
    
    @GetMapping("/wait")
    public ModelAndView waitPage(@RequestParam String roomId) {
        ModelAndView mav = new ModelAndView("waiting"); // sans .html => Spring cherche waiting.html dans /templates
        mav.addObject("roomId", roomId);
        return mav;
    }





}
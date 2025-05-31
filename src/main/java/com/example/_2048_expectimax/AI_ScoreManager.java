package com.example._2048_expectimax;

import javafx.scene.control.Label;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class AI_ScoreManager {

    private AI_2048_Controller controller;
    private Label currentScoreLabel;
    private Label bestScoreLabel;
    private int bestScore;
    private ArrayList<Integer> currentScoreByValue;

    public AI_ScoreManager(AI_2048_Controller controller, Label currentScoreLabel, Label bestScore) {
        this.controller = controller;
        this.currentScoreLabel = currentScoreLabel;
        this.bestScoreLabel = bestScore;
        this.currentScoreByValue = new ArrayList<>();
        getBestScoreFromFile();
    }

    public void addScore(int value, boolean actual) {
        currentScoreByValue.add(value);
        if(actual) {
            currentScoreLabel.setText("SCORE:\n" + getCurrentScore());
            if(getCurrentScore() >= bestScore) {
                bestScore = getCurrentScore();
                setBestScoreLabel(bestScore);
                saveBestScoreToFile();
            }
        }
    }


    public void removeValueFromScore(int tempScore) {
        while(getCurrentScore() > tempScore) {
            if (!currentScoreByValue.isEmpty()) {
                currentScoreByValue.remove(currentScoreByValue.size() - 1);
            }
        }
    }

    public void resetFile() {
        bestScore = 0;
        saveBestScoreToFile();
        getBestScoreFromFile();
    }

    public void setCurrentScoreLabel(int score) {
        currentScoreLabel.setText("SCORE:\n" + score);
    }

    public void setBestScoreLabel(int bestScore) {
        bestScoreLabel.setText("BEST SCORE:\n" + bestScore);
    }

    private void saveBestScoreToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/com/example/_2048/BestScore.txt"))){
            writer.write(String.valueOf(bestScore));
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public int getCurrentScore() {
        if(!currentScoreByValue.isEmpty()) {
            return currentScoreByValue.stream().mapToInt(Integer::intValue).sum();
        }
        return 0;
    }

    private void getBestScoreFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/com/example/_2048_expectimax/BestScore.txt"))){
            bestScore = Integer.parseInt(reader.readLine());
            setBestScoreLabel(bestScore);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void reset() {
        if(getCurrentScore() >= bestScore) {
            saveBestScoreToFile();
        }
        currentScoreByValue.clear();
        setCurrentScoreLabel(getCurrentScore());
        getBestScoreFromFile();
    }
}

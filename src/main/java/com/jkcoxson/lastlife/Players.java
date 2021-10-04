package com.jkcoxson.lastlife;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Players {

    public Map <String, Integer> lives = new HashMap<>();
    public String boogieman;
    public Map <String, Integer> notLoggedIn = new HashMap<>();
    public List<String> inactivePlayers;
    public Integer timeLeft;


    // Java constructor
    public Players() {
        Path configFolder = FabricLoader.getInstance().getConfigDir().resolve("Last Life");
        this.timeLeft = Lastlife.config.boogiemanTime * 60 * 20;
        this.boogieman = "none";
        this.inactivePlayers = new ArrayList<>();


        // Load the data file
        File playerData = new File(configFolder.toAbsolutePath().toFile()+"/players.data");
        if (!new File(configFolder.toAbsolutePath().toFile()+"/players.data").exists()){
            try {
                playerData.createNewFile();
                FileWriter fileWriter = new FileWriter(playerData);
                fileWriter.write("");
                this.savePlayerData();

            } catch (Exception e) {
                System.out.println("Could not create data file");
                System.out.println(e);
            }
        }else {
            try{
                // Read in the file line by line
                BufferedReader bufferedReader = new BufferedReader(new FileReader(playerData.getAbsoluteFile()));
                bufferedReader.lines().forEach(line -> {
                    if(line.length() < 3) return;
                    String[] something = line.split(":");
                    if(something.length < 2) return;
                    if(something[0].trim().equals("boogieMan")){
                        this.boogieman = something[1].trim();
                    }
                    if(something[0].trim().equals("timeLeft")){
                        this.timeLeft = Integer.parseInt(something[1].trim()) * 20 * 60;
                    }
                });
            } catch (Exception e){
                System.out.println(e);
            }
        }

        // Load the not logged in file
        File notLoggedInFile = new File(configFolder.toAbsolutePath().toFile()+"/notLoggedIn.data");
        if (!new File(configFolder.toAbsolutePath().toFile()+"/notLoggedIn.data").exists()){
            try {
                playerData.createNewFile();
                FileWriter fileWriter = new FileWriter(notLoggedInFile);
                fileWriter.write("");
            } catch (Exception e) {
                System.out.println("Could not create logged in file");
                System.out.println(e);
            }
        }else {
            try{
                // Read in the file line by line
                BufferedReader bufferedReader = new BufferedReader(new FileReader(notLoggedInFile.getAbsoluteFile()));
                bufferedReader.lines().forEach(line -> {
                    if(line.length() < 3) return;
                    String[] lifeLine = line.split(":");
                    if (lifeLine.length<2) return;
                    this.notLoggedIn.put(lifeLine[0].trim(), Integer.parseInt(lifeLine[1].trim()));
                });
            } catch (Exception e){
                System.out.println(e);
            }
        }

        // Load the not logged in file
        File inactivePlayers = new File(configFolder.toAbsolutePath().toFile()+"/inactivePlayers.data");
        if (!new File(configFolder.toAbsolutePath().toFile()+"/inactivePlayers.data").exists()){
            try {
                playerData.createNewFile();
                FileWriter fileWriter = new FileWriter(inactivePlayers);
                fileWriter.write("");
            } catch (Exception e) {
                System.out.println("Could not create logged in file");
                System.out.println(e);
            }
        }else {
            try{
                // Read in the file line by line
                BufferedReader bufferedReader = new BufferedReader(new FileReader(playerData.getAbsoluteFile()));
                bufferedReader.lines().forEach(line -> {
                    if(line.length() < 3) return;
                    this.inactivePlayers.add(line);
                });
            } catch (Exception e){
                System.out.println(e);
            }
        }

        // Load the lives file
        File livesFile = new File(configFolder.toAbsolutePath().toFile()+"/lives.data");
        if(!livesFile.exists()){
            try{
                playerData.createNewFile();
                FileWriter fileWriter = new FileWriter(livesFile);
                fileWriter.write("");
            } catch(Exception e){
                System.out.println("Could not create lives file");
                System.out.println(e);
            }
        }else {
            try{
                // Read in the file line by line
                BufferedReader bufferedReader = new BufferedReader(new FileReader(livesFile.getAbsoluteFile()));
                bufferedReader.lines().forEach(line -> {
                    if(line.length() < 3) return;
                    String[] lifeLine = line.split(":");
                    if (lifeLine.length<2) return;
                    this.lives.put(lifeLine[0].trim(), Integer.parseInt(lifeLine[1].trim()));
                });
            } catch (Exception e){
                System.out.println(e);
            }
        }
    }

    public void handleMinute(){
        this.notLoggedIn.forEach((uuid, time) -> {
            if (Lastlife.serverReference.getValue().getPlayerManager().getPlayerList().contains(Lastlife.serverReference.getValue().getPlayerManager().getPlayer(UUID.fromString(uuid)))){
                this.notLoggedIn.put(uuid, time - 1);
            }
        });
        this.save();
    }

    public void save() {
        this.savePlayerData();
        this.saveNotLoggedIn();
        this.saveInactivePlayers();
        this.saveLivesFile();
    }

    private void savePlayerData() {
        Path configFolder = FabricLoader.getInstance().getConfigDir().resolve("Last Life");
        File playerData = new File(configFolder.toAbsolutePath().toFile()+"/players.data");
        Integer minuteTime = this.timeLeft / 20 / 60;
        String toWrite = "boogieMan: " + this.boogieman + "\n"+
                "timeLeft: " + minuteTime.toString();
        try {
            FileWriter fileWriter = new FileWriter(playerData);
            fileWriter.write(toWrite);
            fileWriter.close();
        } catch (Exception e){
            // uh oh I guess
        }
    }
    private void saveNotLoggedIn() {
        Path configFolder = FabricLoader.getInstance().getConfigDir().resolve("Last Life");
        File playerData = new File(configFolder.toAbsolutePath().toFile()+"/notLoggedIn.data");
        playerData.delete();
        try {
            playerData.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.notLoggedIn.forEach((uuid,lives) -> {
            try {

                String file = Files.readString(playerData.toPath());
                file += uuid+": " + lives.toString() + "\n";
                FileWriter fileWriter = new FileWriter(playerData);
                fileWriter.write(file);
                fileWriter.close();
            }catch (Exception e){
                System.out.println(e);
            }
        });
    }

    private void saveInactivePlayers() {
        Path configFolder = FabricLoader.getInstance().getConfigDir().resolve("Last Life");
        File playerData = new File(configFolder.toAbsolutePath().toFile()+"/inactivePlayers.data");
        playerData.delete();
        try {
            playerData.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.inactivePlayers.forEach(player -> {
            try {
                String file = Files.readString(playerData.toPath());
                file += player + "\n";
                FileWriter fileWriter = new FileWriter(playerData);
                fileWriter.write(file);
                fileWriter.close();
            }catch (Exception e){
                System.out.println(e);
            }
        });
    }

    private void saveLivesFile() {
        Path configFolder = FabricLoader.getInstance().getConfigDir().resolve("Last Life");
        File livesFile = new File(configFolder.toAbsolutePath().toFile()+"/lives.data");
        livesFile.delete();
        try {
            livesFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.lives.forEach((uuid,lives) -> {
            try {

                String file = Files.readString(livesFile.toPath());
                file += uuid+": " + lives.toString() + "\n";
                FileWriter fileWriter = new FileWriter(livesFile);
                fileWriter.write(file);
                fileWriter.close();
            }catch (Exception e){
                System.out.println(e);
            }
        });
    }
}

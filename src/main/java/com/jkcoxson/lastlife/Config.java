package com.jkcoxson.lastlife;

import net.fabricmc.loader.api.FabricLoader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.Locale;


// Represents the configuration for the game
public class Config {

    public Integer chatRadius;
    public Boolean playerLock;
    public Integer boogiemanTime;
    public Integer minimumLives;
    public Integer maximumLives;
    public Boolean inactivityPenalty;
    public Boolean murderPenalty;
    public Boolean killBoogieman;
    public Boolean giftLives;

    /**
     * A minute timer in ticks
     */
    public Integer minuteTimer;

    // This is a Java constructor ig :shrug:
    public Config(){
        Path configFolder = FabricLoader.getInstance().getConfigDir().resolve("Last Life");
        File config = new File(configFolder.toAbsolutePath().toFile()+"/config.conf");

        this.loadDefaults();
        minuteTimer = 60 * 20;

        // Create a new file if it doesn't exist
        if (!new File(configFolder.toAbsolutePath().toFile()+"/config.conf").exists()){
            try {
                config.createNewFile();

                // Load the default hard-coded values
                this.save();

            } catch (Exception e) {
                System.out.println("Could not create configuration file");
                System.out.println(e);
            }
        }else {
            try{
                // Read in the file line by line
                BufferedReader bufferedReader = new BufferedReader(new FileReader(config.getAbsoluteFile()));
                bufferedReader.lines().forEach(line -> {
                    if (line.length() < 3) return;
                    if (line.startsWith("//")) return;
                    String[] readLine = line.split(":");
                    if (readLine.length<2){
                        System.out.println("Bad configuration line, skipping");
                        return;
                    }
                    try {
                        handleSetting(readLine[0].trim(), readLine[1].trim());
                    }catch (Exception e){
                        System.out.println(e);
                    }

                });
            } catch (Exception e){
                System.out.println(e);
            }
        }
    }


    /**
     * Loads the default configuration, overwriting what was there.
     */
    public void loadDefaults(){
        this.chatRadius = 100;
        this.playerLock = false;
        this.boogiemanTime = 1440;
        this.minimumLives = 2;
        this.maximumLives = 6;
        this.inactivityPenalty = true;
        this.murderPenalty = true;
        this.killBoogieman = true;
        this.giftLives = true;
    }

    private void handleSetting(String key, String value) {
        System.out.println("Setting "+key+" as "+value);
        switch (key){
            case "chat radius": {
                this.chatRadius = Integer.parseInt(value);
            }
            case "player lock": {
                this.playerLock = getBoolean(value);
            }
            case "boogieman time": {
                this.boogiemanTime = Integer.parseInt(value) * 20 * 60; // 20 ticks and 60 seconds
            }
            case "minimum lives": {
                this.minimumLives = Integer.parseInt(value);
            }
            case "maximum lives": {
                this.maximumLives = Integer.parseInt(value);
            }
            case "inactivity penalty": {
                this.inactivityPenalty = getBoolean(value);
            }
            case "murder penalty": {
                this.murderPenalty = getBoolean(value);
            }
            case "kill boogieman": {
                this.killBoogieman = getBoolean(value);
            }
            case "gift lives": {
                this.giftLives = getBoolean(value);
            }
        }
    }

    /**
     * Returns true if the string is true from the config
     * @param spool
     * @return Boolean
     */
    private Boolean getBoolean(String spool){
        switch (spool.toLowerCase(Locale.ROOT)) {
            case "true", "mhm", "yes", "ofc", "on", "heck yeah" -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    /**
     * Saves the config object to a file
     */
    public void save(){
        Path configFolder = FabricLoader.getInstance().getConfigDir().resolve("Last Life");
        File config = new File(configFolder.toAbsolutePath().toFile()+"/config.conf");
        String toWrite = "";
        toWrite+="// The radius of where people can chat with each other. Choose 0 to disable." + "\n";
        toWrite+="chat radius: "+this.chatRadius.toString() + "\n\n";

        toWrite+="// Lock new players from joining." + "\n";
        toWrite+="player lock: "+this.playerLock.toString() + "\n\n";

        toWrite+="// How long the boogie man has to kill the target. Choose 0 for manual rotation." + "\n";
        toWrite+="boogieman time: "+this.boogiemanTime.toString() + "\n\n";

        toWrite+="// The minimum number of lives people can start with" + "\n";
        toWrite+="minimum lives: "+this.minimumLives.toString() + "\n\n";

        toWrite+="// The maximum number of lives people can start with" + "\n";
        toWrite+="maximum lives: "+this.maximumLives.toString() + "\n\n";

        toWrite+="// Lose a life for being inactive for a whole boogie man rotation" + "\n";
        toWrite+="inactivity penalty: "+this.inactivityPenalty.toString() + "\n\n";

        toWrite+="// Lose a life for murdering somebody who isn't the boogie man" + "\n";
        toWrite+="murder penalty: "+this.murderPenalty.toString() + "\n\n";

        toWrite+="// Gain a life for killing the boogie man" + "\n";
        toWrite+="kill boogieman: "+this.killBoogieman.toString() + "\n\n";

        toWrite+="// Players can gift each other their lives" + "\n";
        toWrite+="gift lives: "+this.giftLives.toString() + "\n\n";

        // Write the file from this object
        try {
            FileWriter fileWriter = new FileWriter(config);
            fileWriter.write(toWrite);
            fileWriter.close();
        } catch (Exception e){
            // uh oh I guess
        }

    }


}

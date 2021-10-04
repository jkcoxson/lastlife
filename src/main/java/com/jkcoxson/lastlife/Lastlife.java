package com.jkcoxson.lastlife;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import org.apache.commons.lang3.mutable.MutableObject;

import java.nio.file.Path;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import static net.minecraft.server.command.CommandManager.*;



public class Lastlife implements ModInitializer {
    static public final MutableObject<MinecraftServer> serverReference = new MutableObject<>();
    static Path configFolder;

    public static Config config;
    public static Players playerDatabase;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> serverReference.setValue(minecraftServer));

        System.out.println("###################");
        System.out.println("###  Last Life  ###");
        System.out.println("###   jkcoxson  ###");
        System.out.println("###################");

        configFolder = FabricLoader.getInstance().getConfigDir().resolve("Last Life");

        // Create configuration files if they don't exist
        if (!configFolder.toFile().exists()) {
            configFolder.toFile().mkdir();
        }

        // Instantiate the databases
        config = new Config();
        playerDatabase = new Players();

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            // The lock command to lock new players from joining
            dispatcher.register(CommandManager.literal("lastlife-lock")
                    .requires(source -> source.hasPermissionLevel(4))
                    .executes(ctx -> {
                        if(config.playerLock){
                            config.playerLock = false;
                            ctx.getSource().sendFeedback(new LiteralText("Last life is lock unlocked"), true);
                        }else {
                            config.playerLock = true;
                            ctx.getSource().sendFeedback(new LiteralText("Last life is lock locked"), true);
                        }
                        config.save();
                        return Command.SINGLE_SUCCESS;
                    }));
            // Command to shuffle the boogie man
            dispatcher.register(CommandManager.literal("new-boogieman")
                    .requires(source -> source.hasPermissionLevel(4))
                    .executes(ctx -> {
                        newBoogieman();
                        return Command.SINGLE_SUCCESS;
                    }));
            // Command to gift lives to other players
            if(config.giftLives) {
                dispatcher.register(CommandManager.literal("gift-life")
                        .then(argument("target", EntityArgumentType.player())
                                .executes(ctx -> {
                                    // idk what to do with this so we just gonna use a mixin lol
                                    return Command.SINGLE_SUCCESS;
                                })));
            }
            dispatcher.register(CommandManager.literal("lives")
                    .executes(ctx -> {
                        ctx.getSource().getPlayer().sendMessage(new LiteralText("You have " + playerDatabase.lives.get(ctx.getSource().getPlayer().getUuidAsString()) + " lives."), false);
                        return Command.SINGLE_SUCCESS;
                    }));

        });


    }

    /**
     * Gets the color of the life value dynamically calculated
     * @param life
     * @return
     */
    public static String getLifeColor(Integer life){
        // The range will be broken into 4 colors
        Integer range = config.maximumLives;
        if (range < 2){
            return "dark_red";
        }
        Integer block = range / 4;
        if(life > block * 3){
            return "dark_green";
        }else {
            if (life > block * 2) {
                return "green";
            }else {
                if(life > block){
                    return "yellow";
                }else {
                    return "red";
                }
            }
        }
    }

    /**
     * Silently sends a title from a Minecraft JSON
     * @param serverPlayerEntity
     * @param title
     * @throws CommandSyntaxException
     */
    public static void silentTitle(ServerPlayerEntity serverPlayerEntity, String title) {
        Function<Text, Packet<?>> constructor = TitleS2CPacket::new;
        ServerCommandSource source = serverReference.getValue().getCommandSource();
        Text text = Text.Serializer.fromJson(title);
        try {
            serverPlayerEntity.networkHandler.sendPacket((Packet)constructor.apply(Texts.parse(source, text, serverPlayerEntity, 0)));
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Silently displays a subtitle
     * @param serverPlayerEntity
     * @param title
     */
    public static void silentSubTitle(ServerPlayerEntity serverPlayerEntity, String title) {
        if (serverPlayerEntity == null) return;
        Function<Text, Packet<?>> constructor = SubtitleS2CPacket::new;
        ServerCommandSource source = serverReference.getValue().getCommandSource();
        Text text = Text.Serializer.fromJson(title);
        try {
            serverPlayerEntity.networkHandler.sendPacket((Packet)constructor.apply(Texts.parse(source, text, serverPlayerEntity, 0)));
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        // Subtitles are not displayed until normal titles are I guess
        silentTitle(serverPlayerEntity, "{\"text\":\"\"}");
    }

    /**
     * Silently changes the title time of a player
     * @param serverPlayerEntity
     * @param fadeIn
     * @param stay
     * @param fadeOut
     */
    public static void silentTime(ServerPlayerEntity serverPlayerEntity, int fadeIn, int stay, int fadeOut){
        TitleFadeS2CPacket titleFadeS2CPacket = new TitleFadeS2CPacket(fadeIn, stay, fadeOut);
        serverPlayerEntity.networkHandler.sendPacket(titleFadeS2CPacket);
    }

    /**
     * Removes lives from not logged in players and moves them to inactive
     * Sets the boogieman's health to 1
     * Chooses a new boogieman
     */
    public static void newBoogieman(){
        serverReference.getValue().getCommandManager().execute(serverReference.getValue().getCommandSource(),"/tellraw @a {\"text\":\"A new boogieman has been chosen\", \"bold\":true, \"italic\":true, \"color\":\"dark_purple\"}");
        if(!playerDatabase.boogieman.equals("none")){
            playerDatabase.lives.put(playerDatabase.boogieman, 1);
            silentSubTitle(serverReference.getValue().getPlayerManager().getPlayer(UUID.fromString(playerDatabase.boogieman)), "{\"text\":\"You did not kill anyone\", \"bold\":true, \"italic\":false, \"color\":\"dark_red\"}");
        }
        List<String> uuidList = new ArrayList<>();
        playerDatabase.lives.forEach((uuid, lives) -> {
            if(lives > 0){
                uuidList.add(uuid);
            }
        });


        // Flush the random int
        for (Integer i = 0; i < serverReference.getValue().getTickTime() * ThreadLocalRandom.current().nextInt(); i++){
            ThreadLocalRandom.current().nextInt();
        }
        Integer index = ThreadLocalRandom.current().nextInt(0, uuidList.size() - 1);

        playerDatabase.boogieman = uuidList.get(index);
        playerDatabase.save();

        silentSubTitle(serverReference.getValue().getPlayerManager().getPlayer(UUID.fromString(uuidList.get(index))), "{\"text\":\"You are the new boogieman\", \"bold\":true, \"italic\":false, \"color\":\"dark_red\"}");

        // Take lives from the inactive people
        if (!config.inactivityPenalty) return;
        System.out.println(playerDatabase.notLoggedIn);
        playerDatabase.notLoggedIn.forEach((uuid,time) -> {
            if (time > 0) {
                playerDatabase.lives.put(uuid, playerDatabase.lives.get(uuid) - 1);
                playerDatabase.inactivePlayers.add(uuid);
                if(playerDatabase.lives.get(uuid) < 1){
                    serverReference.getValue().getPlayerManager().getPlayer(UUID.fromString(uuid)).networkHandler.disconnect(new LiteralText("You have run out of lives"));
                }
            }
            playerDatabase.notLoggedIn.put(uuid, 60);
        });
        playerDatabase.save();
    }


}











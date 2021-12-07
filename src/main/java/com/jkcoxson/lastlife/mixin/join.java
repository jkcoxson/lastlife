package com.jkcoxson.lastlife.mixin;

import com.jkcoxson.lastlife.Lastlife;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.ClientConnection;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(PlayerManager.class)
public class join {

    @Inject(at=@At("TAIL"), method="onPlayerConnect")
    public void asdf(ClientConnection connection, ServerPlayerEntity serverPlayerEntity, CallbackInfo ci){
        // Check if they exist in the player database
        if(Lastlife.playerDatabase.lives.containsKey(serverPlayerEntity.getUuidAsString())){
            if(Lastlife.playerDatabase.lives.get(serverPlayerEntity.getUuidAsString()) < 1){
                serverPlayerEntity.networkHandler.disconnect(new LiteralText("You have no more lives"));
            }else {
                // Check if notifications need to be sent
            }
        } else {
            if(Lastlife.config.playerLock){
                serverPlayerEntity.networkHandler.disconnect(new LiteralText("The Last Life player list is locked"));
            } else {
                // Play fancy animation I guess
                new Thread(() -> {
                    // Flush the random int
                    for (Integer i = 0; i < serverPlayerEntity.getWorld().getTime() * ThreadLocalRandom.current().nextInt(); i++){
                        ThreadLocalRandom.current().nextInt();
                    }
                    Integer randomNum = ThreadLocalRandom.current().nextInt(Lastlife.config.minimumLives, Lastlife.config.maximumLives + 1);
                    Lastlife.playerDatabase.lives.put(serverPlayerEntity.getUuidAsString(), randomNum);
                    Lastlife.playerDatabase.notLoggedIn.put(serverPlayerEntity.getUuidAsString(), 60);
                    Lastlife.playerDatabase.save();
                    Integer lastRandom = 0;
                    Lastlife.silentTime(serverPlayerEntity, 20, 40, 20);
                    Lastlife.silentSubTitle(serverPlayerEntity, "{\"text\":\"Last Life\", \"bold\":true, \"italic\":true, \"color\":\"dark_purple\"}");
                    try{
                        Thread.sleep(4500);
                    } catch (Exception e){
                        System.out.println(e);
                    }
                    Lastlife.silentTime(serverPlayerEntity, 0, 5, 0);
                    for (Integer i = 0; i<30; i++){
                        Integer tempNum = ThreadLocalRandom.current().nextInt(Lastlife.config.minimumLives, Lastlife.config.maximumLives + 1);
                        if (tempNum == lastRandom){
                            tempNum++;
                        }
                        Lastlife.silentTitle(serverPlayerEntity, "{\"text\":\"" + tempNum + "\", \"bold\":true, \"italic\":false, \"color\":\"" + Lastlife.getLifeColor(tempNum) + "\"}");
                        try {
                            Thread.sleep(100);
                        }catch (Exception e){
                            System.out.println(e);
                        }
                        lastRandom = tempNum;
                    }
                    Lastlife.silentTime(serverPlayerEntity, 0, 10, 0);
                    for (Integer i = 0; i<15; i++){
                        Integer tempNum = ThreadLocalRandom.current().nextInt(Lastlife.config.minimumLives, Lastlife.config.maximumLives + 1);
                        if (tempNum == lastRandom){
                            tempNum++;
                        }
                        Lastlife.silentTitle(serverPlayerEntity, "{\"text\":\"" + tempNum + "\", \"bold\":true, \"italic\":false, \"color\":\"" + Lastlife.getLifeColor(tempNum) + "\"}");
                        try {
                            Thread.sleep(200);
                        }catch (Exception e){
                            System.out.println(e);
                        }
                        lastRandom = tempNum;
                    }
                    Lastlife.silentTime(serverPlayerEntity, 0, 40, 20);
                    Lastlife.silentTitle(serverPlayerEntity, "{\"text\":\"" + randomNum + "\", \"bold\":true, \"italic\":false, \"color\":\"" + Lastlife.getLifeColor(randomNum) + "\"}");
                    Lastlife.silentTime(serverPlayerEntity, 20, 40, 20);
                }).start();
            }
        }
    }

}

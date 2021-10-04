package com.jkcoxson.lastlife.mixin;


import com.jkcoxson.lastlife.Lastlife;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import static com.jkcoxson.lastlife.Lastlife.*;

@Mixin(World.class)
public class tick {
    @Inject(at = @At("HEAD"), method = "tickBlockEntities")
    public void asdf( CallbackInfo ci){
        playerDatabase.timeLeft--;
        if(playerDatabase.timeLeft<0){
            Lastlife.newBoogieman();
            playerDatabase.timeLeft = config.boogiemanTime;
        }
        config.minuteTimer--;
        if(config.minuteTimer < 0){
            playerDatabase.handleMinute();
            config.minuteTimer = 60 * 20;
        }

    }

}

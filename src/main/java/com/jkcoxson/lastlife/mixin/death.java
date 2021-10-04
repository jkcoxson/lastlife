package com.jkcoxson.lastlife.mixin;

import com.jkcoxson.lastlife.Lastlife;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ServerPlayerEntity.class)
public abstract class death {


    @Shadow public abstract Entity getCameraEntity();

    @Inject(at = @At("HEAD"), method = "onDeath")
    public void asdf(DamageSource source, CallbackInfo ci){
        String uuid = this.getCameraEntity().getUuidAsString();
        // Subtract a life
        Integer life = Lastlife.playerDatabase.lives.get(uuid) - 1;
        Lastlife.playerDatabase.lives.put(uuid, life);
        Lastlife.playerDatabase.save();
        if (life < 1){
            // Try to disconnect?
            Lastlife.serverReference.getValue().getPlayerManager().getPlayer(UUID.fromString(uuid)).networkHandler.disconnect(new LiteralText("You have run out of lives"));
        }
        try{
            if(!source.getSource().isPlayer()) return;
        }catch (Exception e){
            return;
        }
        System.out.println("Boogieman: "+Lastlife.playerDatabase.boogieman);
        String attackerUuid = source.getAttacker().getUuidAsString();
        Integer attackerLife = Lastlife.playerDatabase.lives.get(attackerUuid);
        ServerPlayerEntity attackerServerPlayerEntity = Lastlife.serverReference.getValue().getPlayerManager().getPlayer(source.getAttacker().getUuid());
        if(Lastlife.playerDatabase.boogieman.equals(uuid) && Lastlife.config.killBoogieman) {
            Lastlife.playerDatabase.lives.put(attackerUuid, attackerLife + 1);
            Lastlife.playerDatabase.save();
            Lastlife.silentSubTitle(attackerServerPlayerEntity, "{\"text\":\"You have killed the Boogie Man\", \"bold\":true, \"italic\":false, \"color\":\"dark_green\"}");
            return;
        }
        if(Lastlife.playerDatabase.boogieman.equals(attackerUuid)) {
            Lastlife.playerDatabase.boogieman = "none";
            Lastlife.playerDatabase.save();
            Lastlife.silentSubTitle(attackerServerPlayerEntity, "{\"text\":\"You have been cured!\", \"bold\":true, \"italic\":false, \"color\":\"dark_green\"}");
            return;
        }
        if(Lastlife.config.murderPenalty) {
            Lastlife.playerDatabase.lives.put(attackerUuid, attackerLife - 1);
            Lastlife.playerDatabase.save();
        }

        Lastlife.silentSubTitle(attackerServerPlayerEntity, "{\"text\":\"You have killed an innocent\", \"bold\":true, \"italic\":false, \"color\":\"dark_red\"}");
    }

}

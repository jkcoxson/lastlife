package com.jkcoxson.lastlife.mixin;

import com.jkcoxson.lastlife.Lastlife;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.filter.TextStream;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class chat {

    @Shadow public ServerPlayerEntity player;

    @Shadow public abstract ServerPlayerEntity getPlayer();

    @Shadow @Final private MinecraftServer server;

    @Inject(
            method = ("handleMessage"),
            at = {@At("HEAD")},
            cancellable = true
    )
    public void asdf(TextStream.Message message, CallbackInfo ci) {
        String string = message.getRaw().replaceAll("\n", "");
        if(string.startsWith("/")) {
            if(string.startsWith("/gift-life ") && Lastlife.config.giftLives){
                String argument = string.split(" ")[1];
                if(argument.startsWith("@")){
                    this.getPlayer().sendMessage(new LiteralText("lol"), false);
                    return;
                }
                ServerPlayerEntity target = Lastlife.serverReference.getValue().getPlayerManager().getPlayer(argument);
                if(target == null) {
                    this.getPlayer().sendMessage(new LiteralText("Target not found"), false);
                    return;
                }
                this.getPlayer().sendMessage(new LiteralText("You have given " + argument + " a life."), false);
                target.sendMessage(new LiteralText("You have received a life from " + argument +"."),false);
                Lastlife.playerDatabase.lives.put(this.getPlayer().getUuidAsString(), Lastlife.playerDatabase.lives.get(this.getPlayer().getUuidAsString()) - 1);
                if(Lastlife.playerDatabase.lives.get(this.getPlayer().getUuidAsString()) < 1){
                    this.getPlayer().networkHandler.disconnect(new LiteralText("You have given away all of your lives."));
                }
                Lastlife.playerDatabase.lives.put(target.getUuidAsString(), Lastlife.playerDatabase.lives.get(target.getUuidAsString()) + 1);
                Lastlife.playerDatabase.save();
            }
            return;
        }
        if (Lastlife.config.chatRadius == 0 ) return;
        Vec3d vec3d = this.getPlayer().getPos();
        Lastlife.serverReference.getValue().getPlayerManager().getPlayerList().forEach(player -> {
            if(vec3d.distanceTo(player.getPos()) < Lastlife.config.chatRadius){
                Lastlife.serverReference.getValue().getCommandManager().execute(Lastlife.serverReference.getValue().getCommandSource(), "/tellraw " + player.getDisplayName().getString() +
                        " {" +
                        "\"text\":\"<"+this.getPlayer().getDisplayName().getString()+"> "+string.trim()+"\"" +
                        "}");
            }
        });
        ci.cancel();
    }
}

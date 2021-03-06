/*
 *     Hypixel Community Client, Client optimized for Hypixel Network
 *     Copyright (C) 2018  HCC Dev Team
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.hcc.mixins;

import com.hcc.HCC;
import com.hcc.event.*;
import com.hcc.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;
import net.minecraft.util.Util;
import net.minecraft.world.WorldSettings;
import org.apache.commons.io.IOUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStream;
import java.nio.ByteBuffer;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Accessor public abstract Timer getTimer();

    /**
     * Invoked once the game has be launched
     * @param ci {@see org.spongepowered.asm.mixin.injection.callback.CallbackInfo}
     */
    @Inject(method = "startGame", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        EventBus.INSTANCE.register(HCC.INSTANCE);
        EventBus.INSTANCE.post(new InitializationEvent());
    }


    /**
     * Invoked every tick (every 50milliseconds)
     * @param ci {@see org.spongepowered.asm.mixin.injection.callback.CallbackInfo}
     */
    @Inject(method = "runTick", at = @At("RETURN"))
    private void runTick(CallbackInfo ci) {
        EventBus.INSTANCE.post(new TickEvent());
    }

    /**
     * Invoked once the player has pressed a key
     * @param ci {@see org.spongepowered.asm.mixin.injection.callback.CallbackInfo}
     */
    @Inject(method = "dispatchKeypresses", at = @At(value = "INVOKE_ASSIGN", target = "Lorg/lwjgl/input/Keyboard;getEventKeyState()Z"))
    private void runTickKeyboard(CallbackInfo ci) {
        EventBus.INSTANCE.post(new KeypressEvent(Keyboard.getEventKey(), Keyboard.isRepeatEvent()));
    }

    /**
     * Invoked once the player has pressed mouse button 1
     * @param ci {@see org.spongepowered.asm.mixin.injection.callback.CallbackInfo}
     */
    @Inject(method = "clickMouse", at = @At("RETURN"))
    private void clickMouse(CallbackInfo ci) {
        EventBus.INSTANCE.post(new LeftMouseClickEvent());
    }

    /**
     * Invoked once the player has pressed mouse button 1
     * @param ci {@see org.spongepowered.asm.mixin.injection.callback.CallbackInfo}
     */
    @Inject(method = "rightClickMouse", at = @At("RETURN"))
    private void rightClickMouse(CallbackInfo ci) {
        EventBus.INSTANCE.post(new RightMouseClickEvent());
    }

    /**
     * Invoked once the player has joined a singleplayer world
     * @param ci {@see org.spongepowered.asm.mixin.injection.callback.CallbackInfo}
     */
    @Inject(method = "launchIntegratedServer", at = @At("HEAD"))
    private void launchIntegratedServer(String folderName, String worldName, WorldSettings worldSettingsIn, CallbackInfo ci){
        EventBus.INSTANCE.post(new SingleplayerJoinEvent());
    }

    private void setWindowIcon(){
        if(Util.getOSType() != Util.EnumOS.OSX){
            InputStream inputstream = null;
            InputStream inputstream1 = null;
            try
            {
                inputstream = Minecraft.class.getResourceAsStream("/assets/hcc/icons/icon-16x.png");
                inputstream1 = Minecraft.class.getResourceAsStream("/assets/hcc/icons/icon-32x.png");

                if (inputstream != null && inputstream1 != null)
                {
                    Display.setIcon(new ByteBuffer[]{Utils.INSTANCE.readImageToBuffer(inputstream),
                            Utils.INSTANCE.readImageToBuffer(inputstream1)});
                }
            }
            catch (Exception ex)
            {
                HCC.logger.error("Couldn't set Windows Icon", ex);
            }
            finally
            {
                IOUtils.closeQuietly(inputstream);
                IOUtils.closeQuietly(inputstream1);
            }
        }
    }
}
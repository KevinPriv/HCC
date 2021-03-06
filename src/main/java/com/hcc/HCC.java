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

package com.hcc;

import com.hcc.addons.HCCAddonBootstrap;
import com.hcc.addons.loader.DefaultAddonLoader;
import com.hcc.config.DefaultConfig;
import com.hcc.event.*;
import com.hcc.event.minigames.MinigameListener;
import com.hcc.exceptions.HCCException;
import com.hcc.gui.ModConfigGui;
import com.hcc.gui.NotificationCenter;
import com.hcc.handlers.HCCHandlers;
import com.hcc.handlers.handlers.keybinds.KeyBindHandler;
import com.hcc.mixins.MixinKeyBinding;
import com.hcc.mods.HCCModIntegration;
import com.hcc.mods.ToggleSprintContainer;
import com.hcc.mods.discord.RichPresenceManager;
import com.hcc.utils.TrueTypeFont;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

import java.io.File;
import java.security.Key;

/**
 * Hypixel Community Client
 */
public class HCC {

    public static final HCC INSTANCE = new HCC();
    /**
     * Instance of the global mod logger
     */
    public final static Logger logger = LogManager.getLogger(Metadata.getModid());
    /**
     * Instance of default addons loader
     */
    public static final DefaultAddonLoader addonLoader = new DefaultAddonLoader();
    public static final NotificationCenter notification = new NotificationCenter();
    public static File folder = new File("hcc");
    /**
     * Instance of default config
     */
    public static final DefaultConfig config = new DefaultConfig(new File(folder, "config.json"));
    private static HCCAddonBootstrap addonBootstrap;

    private static RichPresenceManager richPresenceManager = new RichPresenceManager();

    static {
        try {
            addonBootstrap = new HCCAddonBootstrap();
        } catch (HCCException e) {
            e.printStackTrace();
            logger.error("failed to initialize addonBootstrap");
        }
    }

    private HCCHandlers handlers;
    private HCCModIntegration modIntegration;

    @InvokeEvent
    public void init(InitializationEvent event) {
        EventBus.INSTANCE.register(new MinigameListener());
        EventBus.INSTANCE.register(new ToggleSprintContainer());

        folder = new File(Minecraft.getMinecraft().mcDataDir, "hcc");
        logger.info("HCC Started!");
        logger.info(TrueTypeFont.isSupported("Jokerman"));
        Display.setTitle("HCC " + Metadata.getVersion());
        try {
            addonBootstrap.loadAddons(addonLoader);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Failed to load addon(s) from addons folder");
        }
        registerCommands();


        handlers = new HCCHandlers();
        modIntegration = new HCCModIntegration();
        richPresenceManager.init();
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    private void registerCommands() {

    }

    @InvokeEvent
    public void onChat(ChatEvent event) {
        if (event.getChat().getUnformattedText().contains("configgui")) {
            event.setCancelled(true);
            Minecraft.getMinecraft().displayGuiScreen(new ModConfigGui());
        }

    }

    @InvokeEvent
    public void onTick(TickEvent event) {
        // someone can make a keybind or some shit for this crap; kevin out
    }

    @InvokeEvent
    public void onKeyPress(KeypressEvent event){
        // i got u - coal
        if((KeyBindHandler.toggleSprint.isActivated())){
            if(event.getKey() == Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode() || event.getKey() ==KeyBindHandler.toggleSprint.getKey()){
                ((MixinKeyBinding) Minecraft.getMinecraft().gameSettings.keyBindSprint).setPressed(true);
            }
        }
    }

    @InvokeEvent
    public void render(RenderEvent event) {
        notification.onTick();
    }

    private void shutdown() {
        config.save();
        richPresenceManager.shutdown();
        logger.info("Shutting down HCC..");
    }


    public HCCHandlers getHandlers() {
        return handlers;
    }

    public HCCModIntegration getModIntegration() {
        return modIntegration;
    }
}

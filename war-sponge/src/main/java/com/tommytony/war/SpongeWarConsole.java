package com.tommytony.war;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

public class SpongeWarConsole extends WarConsole {
    @Override
    public void sendMessage(String message) {
        Sponge.getGame().getServer().getConsole().sendMessage(Text.of(message));
    }
}

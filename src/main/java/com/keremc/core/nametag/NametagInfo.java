package com.keremc.core.nametag;

import com.keremc.core.packet.ScoreboardTeamPacketMod;
import lombok.Getter;

import java.util.ArrayList;

public final class NametagInfo {

    @Getter private String name;
    @Getter private String prefix;
    @Getter private String suffix;

    @Getter private ScoreboardTeamPacketMod teamAddPacket;

    protected NametagInfo(String name, String prefix, String suffix) {
        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;

        teamAddPacket = new ScoreboardTeamPacketMod(name, prefix, suffix, new ArrayList<String>(), 0);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof NametagInfo) {
            NametagInfo otherNametag = (NametagInfo) other;
            return (name.equals(otherNametag.name) && prefix.equals(otherNametag.prefix) && suffix.equals(otherNametag.suffix));
        }

        return (false);
    }

}
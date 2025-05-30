package com.vagdedes.spartan.listeners.bukkit;

import com.vagdedes.spartan.abstraction.check.implementation.movement.morepackets.MorePackets;
import com.vagdedes.spartan.abstraction.protocol.PlayerTrackers;
import com.vagdedes.spartan.abstraction.protocol.SpartanPlayer;
import com.vagdedes.spartan.abstraction.protocol.SpartanProtocol;
import com.vagdedes.spartan.functionality.server.SpartanBukkit;
import me.vagdedes.spartan.system.Enums;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportEvent implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    private void Teleport(PlayerTeleportEvent e) {
        teleport(e.getPlayer(), false);
    }

    public static void teleport(Player player, boolean packets) {
        SpartanProtocol protocol = SpartanBukkit.getProtocol(player, true);

        if (protocol.packetsEnabled() == packets) {
            SpartanPlayer p = protocol.spartan;

            // Object
            p.resetCrucialData();

            // Detections
            protocol.profile().getRunner(Enums.HackType.MorePackets).handle(false, MorePackets.TELEPORT);
            protocol.profile().getRunner(Enums.HackType.IrregularMovements).handle(false, null);
            p.trackers.add(PlayerTrackers.TrackerType.TELEPORT, "tp", 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void Respawn(PlayerRespawnEvent e) {
        respawn(e.getPlayer(), false);
    }

    public static void respawn(Player player, boolean packets) {
        SpartanProtocol protocol = SpartanBukkit.getProtocol(player, true);

        if (protocol.packetsEnabled() == packets) {
            // Objects
            protocol.spartan.resetCrucialData();
        }
    }

}

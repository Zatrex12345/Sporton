package com.vagdedes.spartan.compatibility.manual.building;

import com.vagdedes.spartan.compatibility.Compatibility;
import com.vagdedes.spartan.abstraction.protocol.SpartanPlayer;
import com.vagdedes.spartan.functionality.server.Config;
import com.vagdedes.spartan.functionality.server.SpartanBukkit;
import me.rampen88.drills.events.DrillBreakEvent;
import me.vagdedes.spartan.system.Enums;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class RampenDrills implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void Event(DrillBreakEvent e) {
        SpartanPlayer p = SpartanBukkit.getProtocol(e.getPlayer()).spartan;
        Compatibility.CompatibilityType compatibilityType = Compatibility.CompatibilityType.RAMPEN_DRILLS;

        if (compatibilityType.isFunctional()) {
            Config.compatibility.evadeFalsePositives(
                    p,
                    compatibilityType,
                    new Enums.HackType[]{
                            Enums.HackType.FastBreak,
                            Enums.HackType.NoSwing,
                            Enums.HackType.GhostHand,
                    },
                    5
            );
        }
    }
}

package com.vagdedes.spartan.listeners.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.vagdedes.spartan.Register;
import com.vagdedes.spartan.abstraction.protocol.SpartanProtocol;
import com.vagdedes.spartan.functionality.concurrent.SpartanScheduler;
import com.vagdedes.spartan.functionality.server.SpartanBukkit;
import com.vagdedes.spartan.listeners.bukkit.VelocityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

public class VelocityListener extends PacketAdapter {


    public VelocityListener() {
        super(
                Register.plugin,
                ListenerPriority.MONITOR,
                PacketType.Play.Server.ENTITY_VELOCITY
        );
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        Player player = event.getPlayer();
        SpartanProtocol protocol = SpartanBukkit.getProtocol(player);

        if (protocol.spartan.isBedrockPlayer()) {
            return;
        }
        PacketContainer packet = event.getPacket();

        if (!packet.getIntegers().getValues().isEmpty()) {
            int id = packet.getIntegers().getValues().get(0);
            if (protocol.spartan.getEntityId() == id) {
                SpartanScheduler.run(() -> {
                    double x = packet.getIntegers().read(1).doubleValue() / 8000.0D,
                                    y = packet.getIntegers().read(2).doubleValue() / 8000.0D,
                                    z = packet.getIntegers().read(3).doubleValue() / 8000.0D;
                    PlayerVelocityEvent velocityEvent = new PlayerVelocityEvent(player, new Vector(x, y, z));
                    velocityEvent.setCancelled(event.isCancelled());
                    VelocityEvent.event(velocityEvent, true);
                });
            }
        }
    }

}

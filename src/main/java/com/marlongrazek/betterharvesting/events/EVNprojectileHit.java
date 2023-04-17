//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.marlongrazek.betterharvesting.events;

import com.marlongrazek.betterharvesting.WateringEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class EVNprojectileHit implements Listener {
    public EVNprojectileHit() {
    }

    @EventHandler
    public void onHit(ProjectileHitEvent e) {
        if (e.getEntity() instanceof ThrownPotion) {
            WateringEvent event = new WateringEvent((ThrownPotion)e.getEntity(), e.getHitBlock());
            Bukkit.getPluginManager().callEvent(event);
        }

    }
}

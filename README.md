# Badlion Client Timer API

This repository explains how to use Badlion Client Timer Api.

It allows the server to display timers in the Badlion Client.
This plugin is an API and you need to call it from your own plugins for it to work.

![Timer Mod Example](https://i.gyazo.com/76f560b0be9f9585bd0afd737cdfb084.png)

### Installation

How to install the Badlion Client Timer API on your server.

#### Quick Installation

1. Download the latest bukkit plugin from our releases : https://github.com/BadlionNetwork/BadlionClientTimerAPI/releases
2. Place the downloaded plugin into your `plugins` directory on your server.
3. Turn on the Bukkit server

### API Usage

Below is an example plugin with the four different timers types implemented.

```java
import net.badlion.timers.api.Timer;
import net.badlion.timers.api.TimerApi;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

public class ExamplePlugin extends JavaPlugin implements Listener {

	private TimerApi timerApi;

	private Timer tickTimer;          // 1 minute tick timer
	private Timer repeatingTickTimer; // 1 minute repeating tick timer
	private Timer timeTimer;          // 1 minute time timer
	private Timer repeatingTimeTimer; // 1 minute repeating time timer

	@Override
	public void onEnable() {

		// Get the timer api instancce
		this.timerApi = TimerApi.getInstance();

		// Create the timers
		this.tickTimer = this.timerApi.createTickTimer("Tick Timer", new ItemStack(Material.IRON_INGOT), false, 1200L);
		this.repeatingTickTimer = this.timerApi.createTickTimer("Repeating Tick Timer", new ItemStack(Material.GOLD_INGOT), true, 1200L);
		this.timeTimer = this.timerApi.createTimeTimer("Time Timer", new ItemStack(Material.DIAMOND), false, 1L, TimeUnit.MINUTES);
		this.repeatingTimeTimer = this.timerApi.createTimeTimer("Repeating Tick Timer", new ItemStack(Material.EMERALD), true, 1L, TimeUnit.MINUTES);

		// Register the listener
		this.getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {

		// Remove the timers
		this.timerApi.removeTimer(this.tickTimer);
		this.timerApi.removeTimer(this.repeatingTickTimer);
		this.timerApi.removeTimer(this.timeTimer);
		this.timerApi.removeTimer(this.repeatingTimeTimer);
	}

	@EventHandler
	public void onLogin(PlayerJoinEvent event) {

		// Add the player to the timers
		this.tickTimer.addReceiver(event.getPlayer());
		this.repeatingTickTimer.addReceiver(event.getPlayer());
		this.timeTimer.addReceiver(event.getPlayer());
		this.repeatingTimeTimer.addReceiver(event.getPlayer());
	}

	@EventHandler
	public void onLogout(PlayerQuitEvent event) {

		// Remove the player from the timers
		this.tickTimer.removeReceiver(event.getPlayer());
		this.repeatingTickTimer.removeReceiver(event.getPlayer());
		this.timeTimer.removeReceiver(event.getPlayer());
		this.repeatingTimeTimer.removeReceiver(event.getPlayer());
	}
}
```
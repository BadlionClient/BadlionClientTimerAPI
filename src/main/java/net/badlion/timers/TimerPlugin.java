package net.badlion.timers;

import net.badlion.timers.api.TimerApiImpl;
import net.badlion.timers.impl.NmsManager;
import net.badlion.timers.listeners.TimerListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.charset.Charset;
import java.util.logging.Level;

public class TimerPlugin extends JavaPlugin {

	public static final String CHANNEL_NAME = "BLC|T";
	public static final Charset UTF_8_CHARSET = Charset.forName("UTF-8"); // Do not use Guava because of 1.7

	private TimerApiImpl timerApi;

	@Override
	public void onEnable() {
		// Only support <= 1.12.2 at the moment, we will add 1.13 support when BLC 1.13 is ready
		if (this.getServer().getBukkitVersion().startsWith("1.13")) {
			this.getLogger().log(Level.SEVERE, "BLC Timer API is not currently compatible with 1.13 Minecraft. Check back later for updates.");
			this.getPluginLoader().disablePlugin(this);
			return;
		}

		NmsManager.init(this);

		this.timerApi = new TimerApiImpl(this);

		this.getServer().getMessenger().registerOutgoingPluginChannel(this, TimerPlugin.CHANNEL_NAME);

		this.getServer().getPluginManager().registerEvents(new TimerListener(this), this);

		this.getServer().getScheduler().runTaskTimer(this, new Runnable() {
			@Override
			public void run() {
				TimerPlugin.this.timerApi.tickTimers();
			}
		}, 1L, 1L);

		this.getServer().getScheduler().runTaskTimer(this, new Runnable() {
			@Override
			public void run() {
				TimerPlugin.this.timerApi.syncTimers();
			}
		}, 60L, 60L);
	}

	@Override
	public void onDisable() {

	}

	public TimerApiImpl getTimerApi() {
		return this.timerApi;
	}
}

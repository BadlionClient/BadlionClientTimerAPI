package net.badlion.timers;

import net.badlion.timers.api.TimerApiImpl;
import net.badlion.timers.impl.NmsManager;
import net.badlion.timers.listeners.TimerListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.charset.Charset;

public class TimerPlugin extends JavaPlugin {

	public static final String CHANNEL_NAME = "badlion:timers";
	public static final Charset UTF_8_CHARSET = Charset.forName("UTF-8"); // Do not use Guava because of 1.7

	private TimerApiImpl timerApi;

	@Override
	public void onEnable() {

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

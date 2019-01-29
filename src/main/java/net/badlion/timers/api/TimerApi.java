package net.badlion.timers.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class TimerApi {
	static TimerApi instance;

	/**
	 * Get the API instance.
	 *
	 * @return The API instance
	 */
	public static TimerApi getInstance() {
		return TimerApi.instance;
	}

	/**
	 * Create a new timer and register it into the API.
	 * <p>
	 * A timer will automatically handle synchronizing with its receivers,
	 * and will repeat itself if it's mark as repeating. If not, it'll be
	 * automatically removed from the API.
	 *
	 * @param item      Item to show in the client
	 * @param repeating {@code true} if the timer is repeating, {@code false} otherwise
	 * @param time      Countdown time, in ticks (20 per seconds)
	 * @return The new timer instance
	 */
	public abstract Timer createTimer(ItemStack item, boolean repeating, long time);

	/**
	 * Create a new timer and register it into the API.
	 * <p>
	 * A timer will automatically handle synchronizing with its receivers,
	 * and will repeat itself if it's mark as repeating. If not, it'll be
	 * automatically removed from the API.
	 *
	 * @param name      Name to show in the client
	 * @param item      Item to show in the client
	 * @param repeating {@code true} if the timer is repeating, {@code false} otherwise
	 * @param time      Countdown time, in ticks (20 per seconds)
	 * @return The new timer instance
	 */
	public abstract Timer createTimer(String name, ItemStack item, boolean repeating, long time);

	/**
	 * Remove a timer from the API, disabling all API features about it.
	 *
	 * @param timer The timer instance to remove
	 */
	public abstract void removeTimer(Timer timer);

	/**
	 * Clear all timers for a player.
	 *
	 * @param player The player instance to remove
	 */
	public abstract void clearTimers(Player player);
}

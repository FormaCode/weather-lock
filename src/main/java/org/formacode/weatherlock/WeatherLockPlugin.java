/*
 * MIT License
 *
 * Copyright (c) 2018 FormaCode
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.formacode.weatherlock;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public final class WeatherLockPlugin extends JavaPlugin implements Listener
{
	private String notPlayerMessage;
	private String noPermissionMessage;
	private String weatherLockedMessage;
	private String weatherUnlockedMessage;
	private String configurationReloadedMessage;
	private String lockedWorldsHeaderMessage;
	private String lockedWorldsValueMessage;
	private List<String> lockedWorlds;

	@Override
	public void onEnable()
	{
		saveDefaultConfig();
		reloadConfig();
		getCommand("lockweather").setExecutor(this);
		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void reloadConfig()
	{
		super.reloadConfig();
		FileConfiguration config = getConfig();
		ConfigurationSection messages = config.getConfigurationSection("messages");
		this.notPlayerMessage = colorize(messages.getString("not-player"));
		this.noPermissionMessage = colorize(messages.getString("no-permission"));
		this.weatherLockedMessage = colorize(messages.getString("weather-locked"));
		this.weatherUnlockedMessage = colorize(messages.getString("weather-unlocked"));
		this.configurationReloadedMessage = colorize(messages.getString("configuration-reloaded"));
		this.lockedWorldsHeaderMessage = colorize(messages.getString("locked-worlds-header"));
		this.lockedWorldsValueMessage = colorize(messages.getString("locked-worlds-value"));
		this.lockedWorlds = config.getStringList("locked-worlds");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments)
	{
		if (!sender.hasPermission("weatherlock.lockweather"))
		{
			sender.sendMessage(this.noPermissionMessage);
			return true;
		}
		if (arguments.length == 1 && (arguments[0].equalsIgnoreCase("reload") || arguments[0].equalsIgnoreCase("list")))
		{
			if (arguments[0].equalsIgnoreCase("reload"))
			{
				if (!sender.hasPermission("weatherlock.lockweather.reload"))
				{
					sender.sendMessage(this.noPermissionMessage);
					return true;
				}
				this.reloadConfig();
				sender.sendMessage(this.configurationReloadedMessage);
				return true;
			}
			if (!sender.hasPermission("weatherlock.lockweather.list"))
			{
				sender.sendMessage(this.noPermissionMessage);
				return true;
			}
			sender.sendMessage(this.lockedWorldsHeaderMessage);
			if (this.lockedWorlds.isEmpty())
			{
				sender.sendMessage(this.lockedWorldsValueMessage.replace("{{world}}", "none"));
			}
			else
			{
				this.lockedWorlds.stream().map(world -> this.lockedWorldsValueMessage.replace("{{world}}", world)).forEach(sender::sendMessage);
			}
			return true;
		}
		if (!(sender instanceof Player))
		{
			sender.sendMessage(this.notPlayerMessage);
			return true;
		}
		Player player = (Player) sender;
		if (this.lockedWorlds.contains(player.getWorld().getName()))
		{
			this.lockedWorlds.remove(player.getWorld().getName());
		}
		else
		{
			this.lockedWorlds.add(player.getWorld().getName());
		}
		this.getConfig().set("locked-worlds", this.lockedWorlds);
		this.saveConfig();
		player.sendMessage(this.lockedWorlds.contains(player.getWorld().getName()) ? this.weatherLockedMessage : this.weatherUnlockedMessage);
		return true;
	}

	@EventHandler
	public void onWeatherChange(WeatherChangeEvent event)
	{
		if (this.lockedWorlds.contains(event.getWorld().getName()))
		{
			event.setCancelled(true);
		}
	}

	private String colorize(String message)
	{
		return ChatColor.translateAlternateColorCodes('&', message);
	}
}
package me.bigteddy98.bannerboard;

import com.google.common.io.ByteStreams;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import me.bigteddy98.bannerboard.api.BannerBoardManager;
import me.bigteddy98.bannerboard.api.PlaceHolder;
import me.bigteddy98.bannerboard.config.ConfigurationManager;
import me.bigteddy98.bannerboard.draw.ImageUtil;
import me.bigteddy98.bannerboard.util.SkinCache;
import me.bigteddy98.bannerboard.util.VersionUtil;
import me.bigteddy98.bannerboard.util.colors.ColorManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.FileUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;

public class Main extends JavaPlugin {

	public final InternalBannerBoardAPI publicApi = new InternalBannerBoardAPI();
	public static volatile Main instance;
	{
		instance = this;
		BannerBoardManager.setAPI(this.publicApi);
	}

	public static Main getInstance() {
		return instance;
	}

	{
		fixCorrupt();
	}

	public final IdManager idManager = new IdManager();
	public final PlaceHolderManager placeHolderManager = new PlaceHolderManager();
	public final RendererManager rendererManager = new RendererManager();
	private final Set<UUID> deleteSet = new HashSet<>();

	public ColorManager colorManager;
	public ConfigurationManager configurationManager;
	public BoardManager boardManager;
	public ExecutorManager executorManager;
	public BoardMemory memoryManager;
	public Map<String, BufferedImage> cachedImages;
	public SkinCache skinCache;

	private boolean loaded = false;

	@Override
	public void onDisable() {
		if (!this.loaded) {
			return;
		}
		this.executorManager.shutdown();
		instance = null;
		BannerBoardManager.setAPI(null);

		for (Player p : this.getServer().getOnlinePlayers()) {
			try {
				final Channel playerChannel = PacketManager.getChannel(p);
				uninject(playerChannel);
			} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void uninject(final Channel c) {
		c.eventLoop().execute(() -> {
			final ChannelPipeline p = c.pipeline();
			if (p.get("BannerBoard_hook") != null) {
				p.remove("BannerBoard_hook");
			}
		});
	}

	private void fixCorrupt() {
		// fix the config if its corrupted
		File config = new File(this.getDataFolder().getAbsolutePath() + "//config.yml");
		if (config.exists()) {
			YamlConfiguration conf = new YamlConfiguration();
			try {
				conf.load(config);
			} catch (IOException | InvalidConfigurationException e) {
				// make a backup
				File backup = new File(this.getDataFolder().getAbsolutePath() + "//corrupted_config_"
						+ System.currentTimeMillis() + ".yml");
				try {
					if (backup.createNewFile()) {
						FileUtil.copy(config, backup);
						
						if (config.delete()) {
							getLogger().warning("");
							getLogger().warning("============================= !!! WARNING !!! ==============================");
							getLogger().warning("============= YOUR BANNERBOARD CONFIGURATION FILE WAS CORRUPT ==============");
							getLogger().warning("== A BACKUP OF THE OLD FILE CAN BE FOUND IN THE BANNERBOARD PLUGIN FOLDER ==");
							getLogger().warning("================= FOR NOW, A NEW EMPTY CONFIG WILL BE USED =================");
							getLogger().warning("============================================================================");
							getLogger().warning("");
						}
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onEnable() {
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		{
			// org.bukkit.craftbukkit.v1_9_R1
			// 0 1 2 3
			String version = VersionUtil.getSpigotVersion();
			if (!VersionUtil.SUPPORTED_VERSIONS.contains(version)) {
				this.setEnabled(false);
				new RuntimeException("BannerBoard does not support NMS version " + version
						+ ". If this is a newer NMS version (latest supported version is "
						+ VersionUtil.SUPPORTED_VERSIONS.get(VersionUtil.SUPPORTED_VERSIONS.size() - 1)
						+ ") please contact the author as soon as possible and ask for an update. BannerBoard does not support any version older than 1.8")
								.printStackTrace();
				return;
			}
		}

		Bukkit.getScheduler().runTaskLater(this, () -> {
			try {
				enable();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}, 0);
	}

	public void enable() throws IOException {
		try {

			if (this.getConfig().contains("idrange.min") && this.getConfig().contains("idrange.max")) {
				// fix to new format
				int min = this.getConfig().getInt("idrange.min");
				int max = this.getConfig().getInt("idrange.max");

				this.getConfig().set("idrange.startid", min + 1000);
				this.getConfig().set("idrange.amount", max - min);

				this.getConfig().set("idrange.min", null);
				this.getConfig().set("idrange.max", null);

				this.saveConfig();
			}

			if (this.getConfig().contains("skins")) {
				this.getConfig().set("skins", null);
				this.saveConfig();
			}

			if (!this.getConfig().contains("idrange.startid") || !this.getConfig().contains("idrange.amount")) {
				this.getConfig().set("idrange.startid", 22000);
				this.getConfig().set("idrange.amount", 5000);
				this.saveConfig();
			}

			int startCount = this.getConfig().getInt("idrange.startid");
			int amount = this.getConfig().getInt("idrange.amount");

			if (amount < 300) {
				throw new ConfigException(
						"BannerBoard requires at least 300 ID's to run, please change the value of idrange.amount in your config");
			}
			if (amount > 7000) {
				throw new ConfigException(
						"BannerBoard supports at most 7000 ids, please change the value of idrange.amount in your config");
			}

			if (startCount < 1000 || startCount > 25000) {
				throw new ConfigException("Invalid startcount [" + startCount
						+ "], must be in range [1000 - 25000], please change the value of idrange.startCount in your config");
			}

			this.idManager.clear();
			this.idManager.load(startCount, amount);

		} catch (ConfigException e) {
			getLogger().warning("Failed to enable BannerBoard. ID range ERROR: " + e.getMessage() + ".");
			
			// Just... don't shut down the ENTIRE server when your plugin fails... *bonks Sanders*
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		loaded = true;

		File oldData = new File(this.getDataFolder(), "internaldata.yml");
		if (oldData.exists() && oldData.isFile()) {
			if (oldData.delete()) {
				getLogger().info("Removed internaldata.yml");
			}
		}
		instance = this; // reset for reload compatibility
		BannerBoardManager.setAPI(this.publicApi);

		File dataFolder = this.getDataFolder();
		if (!dataFolder.exists() && dataFolder.mkdirs()) {
			getLogger().info("Loaded BannerBoard plugin folder.");
		}

		this.configurationManager = new ConfigurationManager();
		this.configurationManager.init(this);

		this.executorManager = new ExecutorManager();

		this.boardManager = new BoardManager();
		this.getServer().getPluginManager().registerEvents(this.boardManager.init(this), this);

		this.memoryManager = new BoardMemory();
		this.getServer().getPluginManager().registerEvents(this.memoryManager.init(this), this);

		File imageFolder = new File(dataFolder.getAbsolutePath() + "//images");
		if (!imageFolder.exists() && imageFolder.mkdirs()) {
			getLogger().info("Loaded images folder.");
		}

		// load all images
		this.cachedImages = ImageUtil.loadCache(imageFolder);

		File fontFolder = new File(dataFolder.getAbsolutePath() + "//fonts");
		if (!fontFolder.exists() && fontFolder.mkdirs()) {
			getLogger().info("Loaded fonts folder.");
		}

		GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
		File[] files = fontFolder.listFiles();
		
		// The listFiles method has a chance to return null which causes NPE in the for-loop
		if (files != null) {
			for (File f : files) {
				if (f.isFile()) {
					try {
						// try loading it as a font
						g.registerFont(Font.createFont(Font.TRUETYPE_FONT, f));
						getLogger().info("Loaded font " + f.getName() + ".");
					} catch (Exception e) {
						getLogger().warning("Could not load font " + f.getName() + ". " + e.getMessage() + ".");
					}
				}
			}
		}

		if (!this.getConfig().contains("color_palette")) {
			String palette_name = "color_palette_v1_8_R1.bc";
			if (VersionUtil.isHigherThan("v1_10_R1")) {
				palette_name = "color_palette_v1_10_R1.bc";
			}
			if (VersionUtil.isHigherThan("v1_12_R1")) {
				palette_name = "color_palette_v1_12_R1.bc";
			}
			if (VersionUtil.isHigherThan("v1_16_R1")) {
				palette_name = "color_palette_v1_16_R1.bc";
			}
			this.getConfig().set("color_palette", palette_name);
			this.saveConfig();
		}
		this.colorManager = new ColorManager(this.getConfig().getString("color_palette"));

		if (!this.getConfig().contains("skinserver")) {
			this.getConfig().set("skinserver", "http://www.skinrender.com:2798/");
			this.saveConfig();
		}

		if (!this.getConfig().contains("skinrender_key")) {
			this.getConfig().set("skinrender_key", "INSERT_SKINRENDER_KEY_HERE");
			this.saveConfig();
		}
		getLogger().info("Connecting to " + getConfig().getString("skinserver") + " with key " + safeKey() + "...");

		this.skinCache = new SkinCache(this.getConfig().getString("skinserver"));

		// load all boards AFTER all plugins have enabled
		this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
			Main.this.configurationManager.loadAll();

			// find all playerjoinevents
			// currently only BoardMemory.onJoin
			for (Player p : getServer().getOnlinePlayers()) {
				memoryManager.onJoin(new PlayerJoinEvent(p, "BannerBoard reload"));
			}
		});

		// remove this old setting
		if (this.getConfig().contains("viewdistance")) {
			this.getConfig().set("viewdistance", null);
			this.saveConfig();
		}
	}

	// TODO: Not used. Remove or deprecate?
	public static void deleteFolder(File folder) {
		File[] files = folder.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f);
				} else {
					String name = f.getName();
					if (f.delete()) {
						getInstance().getLogger().info("Deleted folder " + name);
					}
				}
			}
		}
		String name = folder.getName();
		if (folder.delete()) {
			getInstance().getLogger().info("Deleted folder " + name);
		}
	}

	// we're using gray &D and gold &Y
	public static void msg(CommandSender p, String message) {
		p.sendMessage(
				ChatColor.GOLD + ">> " + message.replace("&D", ChatColor.GRAY + "").replace("&Y", ChatColor.GOLD + ""));
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {

		// bb fonts
		if (args.length == 1 && args[0].equalsIgnoreCase("fontlist")) {
			if (sender.isOp() || sender.hasPermission("bannerboard.fontlist")) {
				List<String> array = Arrays
						.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());

				if (array.size() > 100 && (sender instanceof Player)) {
					List<String> tmp = new ArrayList<>();
					for (int i = 0; i < 100; i++) {
						tmp.add(array.get(i));
					}

					String list = tmp.toString().replace("[", "").replace("]", "").replace(",",
							ChatColor.GOLD + "," + ChatColor.GRAY);
					msg(sender,
							"&YThis is a list of all fonts supported by your server. &D " + list + "&Y and "
									+ (array.size() - 100)
									+ " more... &DPlease execute this command in the console to see the full list.");
				} else {
					String list = array.toString().replace("[", "").replace("]", "").replace(",",
							ChatColor.GOLD + "," + ChatColor.GRAY);
					msg(sender, "&YThis is a list of all fonts supported by your server. &D " + list + "&Y.");
				}
			} else {
				msg(sender,
						"&DYou are not allowed to do that. Please contact a server administrator if you think this might be a mistake.");
			}
		} else if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
			// creates a new bannerboard
			if (sender.isOp() || sender.hasPermission("bannerboard.reload")) {
				// reload everything
				sender.sendMessage(ChatColor.RED + "[BannerBoard] Reloading...");
				this.reload();
				sender.sendMessage(ChatColor.RED + "[BannerBoard] Reload finished!");
			} else {
				msg(sender,
						"&DYou are not allowed to do that. Please contact a server administrator if you think this might be a mistake.");
			}
		} else if (args.length > 0 && args[0].equalsIgnoreCase("create")) {
			// creates a new bannerboard

			if (!(sender instanceof Player)) {
				Main.msg(sender, "&DThis command can only be executed as a player.");
				return false;
			}

			if (sender.isOp() || sender.hasPermission("bannerboard.create")) {
				this.boardManager.createBoard((Player) sender);
			} else {
				msg(sender,
						"&DYou are not allowed to do that. Please contact a server administrator if you think this might be a mistake.");
			}
		} else if (args.length > 0 && args[0].equalsIgnoreCase("delete")) {
			// delete a bannerboard

			if (!(sender instanceof Player)) {
				Main.msg(sender, "&DThis command can only be executed as a player.");
				return false;
			}

			if (sender.isOp() || sender.hasPermission("bannerboard.delete")) {
				Player p = (Player) sender;
				final UUID uuid = p.getUniqueId();

				if (this.deleteSet.contains(uuid)) {
					deleteSet.remove(uuid);

					Block target;

					String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",")
							.split(",")[3];
					if (version.equals("v1_8_R1")) {
						target = p.getTargetBlock((HashSet<Byte>) null, 10);
					} else {
						target = p.getTargetBlock((Set<Material>) null, 10);
					}

					for (BannerBoard board : this.memoryManager.getLoadedBannerBoards()) {
						List<ItemFrame> frameList = board.buildItemFrameList();
						for (ItemFrame frame : frameList) {
							if (frame.getWorld().equals(target.getWorld())
									&& frame.getLocation().distanceSquared(target.getLocation()) <= 2) {
								this.configurationManager.deleteBannerBoard(board);
								for (ItemFrame f : frameList) {
									f.setItem(null);
									f.remove();
								}

								msg(sender,
										"&DBanner has been deleted. If you have the configuration file open in your text editor, make sure te reopen it, it has changed.");
								this.reload();
								return false;
							}
						}
					}
					msg(sender, "&DNo banner was found at the location you were looking at.");
				} else {
					this.deleteSet.add(uuid);
					msg(sender,
							"&YExecute the same command again to confirm the removal of the banner. Make sure you are looking at the banner you want to delete.");
					
					Bukkit.getScheduler().runTaskLater(this, () -> {
						if (deleteSet.remove(uuid)) {
							msg(sender, "&DBanner removal request expired...");
						}
					}, 20 * 5);
				}
			} else {
				msg(sender,
						"&DYou are not allowed to do that. Please contact a server administrator if you think this might be a mistake.");
			}
		} else {
			msg(sender,
					"&DThere are only two commands &YBannerBoard &Dcurrently supports. Commands (&Y/bannerboard create&D) or (&Y/bannerboard delete&D).");
		}
		return false;
	}

	public void reload() {
		this.onDisable();

		HandlerList.unregisterAll(this.boardManager);
		HandlerList.unregisterAll(this.memoryManager);
		this.getServer().getScheduler().cancelTasks(this);

		this.fixCorrupt();
		this.reloadConfig();

		this.onEnable();
	}

	public String applyPlaceholders(String text, Player p) {
		for (Entry<String, PlaceHolder> place : BannerBoardManager.getAPI().getRegisteredPlaceHolders().entrySet()) {
			String totalName = "%" + place.getKey() + "%";
			if (text.contains(totalName)) {
				text = text.replace(totalName, place.getValue().onReplace(p));
			}
		}
		if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
			text = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p, text);
		}
		return text;
	}

	private String safeKey() {
		String key = this.getConfig().getString("skinrender_key");
		if (key == null) {
			return null;
		}
		int len = key.length() / 4 * 3 + 1;
		if (len >= key.length()) {
			len = key.length() - 1;
		}
		return "***********" + key.substring(len);
	}

	public BufferedImage fetchImage(String link) throws IOException {

		if (link.contains("skinrender.com:")) {
			String key = this.getConfig().getString("skinrender_key");
			link += (";KEY=" + key);

			try (InputStream stream = openStream(link)) {
				@SuppressWarnings("UnstableApiUsage")
				final byte[] result = ByteStreams.toByteArray(stream);
				// it was already an image
				return ImageIO.read(new ByteArrayInputStream(result));
			}
		} else {

			try (InputStream s = openStream(link)) {
				return ImageIO.read(s);
			}
		}
	}

	private static InputStream openStream(String link) throws IOException {
		final URLConnection url = new URL(link).openConnection();
		url.setConnectTimeout(2000);
		url.setReadTimeout(5000);
		return url.getInputStream();
	}
}

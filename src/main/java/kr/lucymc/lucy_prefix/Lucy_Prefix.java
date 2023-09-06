package kr.lucymc.lucy_prefix;

import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public final class Lucy_Prefix extends JavaPlugin {
    final String username="root";
    final String password="INTY";
    final String url = "jdbc:mysql://127.0.0.1:3307/neu";
    public static Connection connection;
    public static LuckPerms api;
    FileConfiguration config = this.getConfig();
    private static Lucy_Prefix INSTANCE;
    public static Lucy_Prefix getInstance() {
        return INSTANCE;
    }
    public void onEnable() {
        INSTANCE = this;
        File ConfigFile = new File(getDataFolder(), "config.yml");
        if(!ConfigFile.isFile()){
            setConfig();
        }
        getServer().getPluginManager().registerEvents(new Prefix_Event(), this);
        getCommand("칭호").setTabCompleter(new Prefix_TabCompleter());
        getCommand("칭호").setExecutor(new Prefix_Command());
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            api = provider.getProvider();
        }
        try {
            connection = DriverManager.getConnection(Objects.requireNonNull(config.getString("DB.URL")), config.getString("DB.ID"), config.getString("DB.PW"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        try { // using a try catch to catch connection errors (like wrong sql password...)
            if (connection != null && !connection.isClosed()) { // checking if connection isn't null to
                connection.close(); // closing the connection field variable.
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setConfig(){
        config.addDefault("DB.ID", "root");
        config.addDefault("DB.PW", "INTY");
        config.addDefault("DB.URL", "jdbc:mysql://127.0.0.1:3307/lucy?autoReconnect=true");
        config.addDefault("NamePrefix", "§f[ §b§l칭호§f ] ");
        config.addDefault("ItemLore", "§f========[ §6정보§f ]========,§8» %ex%,§8» §e좌클릭§f으로 §a사용,§f======================");
        config.addDefault("ExMatcher", "§8» (.*)");
        config.addDefault("ExIndex", 1);
        config.addDefault("message.noPrefixName", "§f[ §b§l칭호§f ] 칭호의 이름이 없습니다.");
        config.addDefault("message.noPrefixLore", "§f[ §b§l칭호§f ] 칭호의 설명이 없습니다.");
        config.addDefault("message.UsePrefix", "§f[ §b§l칭호§f ] %prefix%§f 칭호를 장착하였습니다.");
        config.addDefault("message.DelPrefix", "§f[ §b§l칭호§f ] %prefix%§f 칭호를 삭제하였습니다.");
        config.addDefault("message.addPrefix", "§f[ §b§l칭호§f ] %prefix%§f 칭호를 획득하였습니다.");
        config.addDefault("message.havePrefix", "§f[ §b§l칭호§f ] 이미 %prefix%§f 칭호가 있습니다.");
        config.addDefault("Gui.info", "§f=========[ §6정보§f ]=========,§8» §e소지중§f : %have%,§f========================");
        config.addDefault("Gui.BackPage", "§c§l이전 페이지");
        config.addDefault("Gui.NextPage", "§a§l다음 페이지");
        config.addDefault("Gui.user.Name", "§f[ §b§l칭호§f ] %p%님의 칭호");
        config.addDefault("Gui.user.ItemLore", "§f=========[ §6정보§f ]=========,§8» %ex%,§8» §e좌클릭§f으로 §a장착,§8» §e우클릭§f으로 §c삭제,§f========================");
        config.addDefault("Gui.user.Prefix", "§f[ §b§l칭호§f ] ");
        config.addDefault("Gui.staff.SettingName", "§f[ §b§l칭호 관리§f ] %p%님의 칭호");
        config.addDefault("Gui.staff.SettingNameMatcher", "§f\\[ §b§l칭호 관리§f \\] (.*)님의 칭호");
        config.addDefault("Gui.staff.SettingPrefix", "§f[ §b§l칭호 관리§f ] ");
        config.addDefault("Gui.staff.ItemLore", "§f=========[ §6정보§f ]=========,§8» %ex%,§8» §e좌클릭§f으로 §c삭제,§f========================");
        config.options().copyDefaults(true);
        saveConfig();
        //"§f[ §b§l칭호§f ] "+e.getCurrentItem().getItemMeta().getDisplayName()+"§f 칭호를 장착하였습니다."
    }
}

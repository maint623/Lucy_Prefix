package kr.lucymc.lucy_prefix;

import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PrefixNode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static kr.lucymc.lucy_prefix.Lucy_Prefix.api;
import static kr.lucymc.lucy_prefix.Prefix_DB.PrefixSelect;

public class Prefix_Command implements CommandExecutor {
    FileConfiguration config = Lucy_Prefix.getInstance().getConfig();
    static FileConfiguration configs = Lucy_Prefix.getInstance().getConfig();
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player p) {
            if(args.length == 0) {
                if (p.hasPermission("INTY_Core.Prefix.Open")) {
                    PrefixMenu(p,1,false,p);
                }
            }else if(args[0].equalsIgnoreCase("관리자")) {
                if (p.hasPermission("lucyprefix.staff")) {
                    if (args[1].equalsIgnoreCase("발급")) {
                        if (args[2].isEmpty()) {
                            p.sendMessage(config.getString("message.noPrefixName"));
                        } else if (args[3].isEmpty()) {
                            p.sendMessage(config.getString("message.noPrefixLore"));
                        } else {
                            ItemStack SUNFLOWER = new ItemStack(Material.BOOK, 1);
                            ItemMeta meta = SUNFLOWER.getItemMeta();
                            Objects.requireNonNull(meta).setDisplayName(config.getString("NamePrefix") + args[2].replaceAll("&", "§"));
                            meta.setLore(List.of((config.getString("ItemLore").replace("%ex%",args[3].replaceAll("&", "§"))).split(",")));
                            SUNFLOWER.setItemMeta(meta);
                            p.getInventory().addItem(SUNFLOWER);
                        }
                    } else if (args[1].equalsIgnoreCase("관리")) {
                            Player pl = Bukkit.getServer().getPlayer(args[2]);
                            PrefixMenu(pl, 1,true,p);
                    }
                }
            }
        }
        return true;
    }

    public static void PrefixMenu(Player p,int Page,boolean staff,Player pl){
        //if(MPage <= Page || Page <= 1) return;
        Inventory gui;
        if(staff){
            gui = Bukkit.createInventory(p, 9 * 6, configs.getString("Gui.staff.SettingName").replace("%p%",p.getName()));
        }else{
            gui = Bukkit.createInventory(p, 9 * 6, configs.getString("Gui.user.Name").replace("%p%",p.getName()));
        }

        ResultSet rs = PrefixSelect(p.getUniqueId());
        String DBPrefix = null;
        JSONArray jsonArr;
        while (true) {
            try {
                if (!Objects.requireNonNull(rs).next()) break;
                DBPrefix = rs.getString("Prefix");
            } catch (SQLException err) {
                throw new RuntimeException(err);
            }
        }

        try {
            jsonArr = (JSONArray) new JSONParser().parse(DBPrefix);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        long PrefixCount = jsonArr.size();
        String MaxPage = Integer.toString((int)Math.ceil((float)PrefixCount/(float)45));
        ItemStack info = new ItemStack(Material.SUNFLOWER, 1);
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
        ItemMeta infometa = info.getItemMeta();
        if(MaxPage.equals("0")){
            MaxPage = "1";
        }
        Objects.requireNonNull(infometa).setDisplayName("§f§l( §a§l"+Page+" §f/ §c§l"+MaxPage+" §f§l)");
        infometa.setLore(List.of((configs.getString("Gui.info").replace("%have%",""+(long)jsonArr.size())).split(",")));
        info.setItemMeta(infometa);
        gui.setItem(49, info);
        gui.setItem(45, glass);
        gui.setItem(46, glass);
        gui.setItem(47, glass);
        gui.setItem(48, glass);
        gui.setItem(50, glass);
        gui.setItem(51, glass);
        gui.setItem(52, glass);
        gui.setItem(53, glass);
        if(!((int)Math.ceil((float)PrefixCount/(float)45) <= Page)){
            ItemStack NP = new ItemStack(Material.ARROW, 1);
            ItemMeta NPmeta = NP.getItemMeta();
            assert NPmeta != null;
            NPmeta.setDisplayName(configs.getString("Gui.NextPage"));
            NP.setItemMeta(NPmeta);
            gui.setItem(53, NP);
        }
        if(!(Page <= 1)){
            ItemStack NP = new ItemStack(Material.ARROW, 1);
            ItemMeta NPmeta = NP.getItemMeta();
            assert NPmeta != null;
            NPmeta.setDisplayName(configs.getString("Gui.BackPage"));
            NP.setItemMeta(NPmeta);
            gui.setItem(45, NP);
        }
        int Icount;
        if(Page*45 > (int)PrefixCount){
            Icount = ((int)PrefixCount-(Page-1)*45)-1;
        }else{
            Icount = 44;
        }
        for (int i = 0; i <= Icount; i++) {
            JSONObject element = (JSONObject) jsonArr.get(i+45*(Page-1));
            ItemStack SUNFLOWER = new ItemStack(Material.BOOK, 1);
            ItemMeta meta = SUNFLOWER.getItemMeta();
            Objects.requireNonNull(meta).setDisplayName(element.get("name").toString());
            if(staff){
                meta.setLore(List.of((configs.getString("Gui.staff.ItemLore").replace("%ex%",element.get("ex").toString())).split(",")));
            }else{
                meta.setLore(List.of((configs.getString("Gui.user.ItemLore").replace("%ex%",element.get("ex").toString())).split(",")));
            }
            SUNFLOWER.setItemMeta(meta);
            gui.setItem(i, SUNFLOWER);
        }
        if(staff){
            pl.openInventory(gui);
        }else{
            p.openInventory(gui);
        }
    }
}

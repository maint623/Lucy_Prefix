package kr.lucymc.lucy_prefix;

import net.luckperms.api.messaging.MessagingService;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PrefixNode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static kr.lucymc.lucy_prefix.Lucy_Prefix.api;
import static kr.lucymc.lucy_prefix.Prefix_Command.PrefixMenu;
import static kr.lucymc.lucy_prefix.Prefix_Command.configs;
import static kr.lucymc.lucy_prefix.Prefix_DB.*;
import static org.bukkit.Bukkit.getLogger;

public class Prefix_Event implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String tableName = "prefix";
        String columnName = "UserID";
        String value = ""+event.getPlayer().getUniqueId();
        boolean dataExists = isDataExists(tableName, columnName, value);
        if(!dataExists) {
            PrefixInsert(event.getPlayer().getUniqueId(), "[]", 0);
        }
    }
    @EventHandler
    public static void clickEvent(InventoryClickEvent e) {
        if (e.getView().getTitle().contains(configs.getString("Gui.user.Prefix"))) {
            e.setCancelled(true);
            Player player = (Player) e.getWhoClicked();
            if(e.getCurrentItem()!=null){
                String[] Pages = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(e.getClickedInventory()).getItem(49)).getItemMeta()).getDisplayName().replaceAll("§f§l\\( ","").replaceAll(" §f§l\\)","").replaceAll("§a§l","").replaceAll("§c§l","").split(" §f/ ");
                if(e.getSlot()==46||e.getSlot()==47||e.getSlot()==48||e.getSlot()==49||e.getSlot()==50||e.getSlot()==51||e.getSlot()==52){
                    return;
                }else if(Objects.requireNonNull(Objects.requireNonNull(e.getCurrentItem()).getItemMeta()).getDisplayName().contains(configs.getString("Gui.NextPage"))){
                    PrefixMenu(player,Integer.parseInt(Pages[0])+1,false,player);
                }else if(e.getCurrentItem().getItemMeta().getDisplayName().contains(configs.getString("Gui.BackPage"))){
                    PrefixMenu(player,Integer.parseInt(Pages[0])-1,false,player);
                }else{
                    if(e.getCurrentItem().getType() == Material.BOOK) {
                        if (e.isRightClick()) {
                            try {
                                JSONParser parser = new JSONParser();
                                try {
                                    ResultSet rs = PrefixSelect(player.getUniqueId());
                                    String DBPrefix = null;
                                    int DBCount = 0;
                                    while (true) {
                                        try {
                                            if (!Objects.requireNonNull(rs).next()) break;
                                            DBPrefix = rs.getString("Prefix");
                                            DBCount = rs.getInt("Count");
                                        } catch (SQLException err) {
                                            throw new RuntimeException(err);
                                        }
                                    }
                                    JSONArray jsonArray = (JSONArray) parser.parse(DBPrefix);
                                    JSONArray newJsonArray = new JSONArray();
                                    for (Object obj : jsonArray) {
                                        JSONObject jsonObject = (JSONObject) obj;
                                        String name = (String) jsonObject.get("name");
                                        if (!name.contains(e.getCurrentItem().getItemMeta().getDisplayName())) {
                                            newJsonArray.add(jsonObject);
                                        }
                                    }
                                    ((Player) e.getWhoClicked()).sendMessage(configs.getString("message.DelPrefix").replace("%prefix%", e.getCurrentItem().getItemMeta().getDisplayName()));
                                    User user = api.getUserManager().getUser(player.getUniqueId());
                                    user.data().remove(PrefixNode.builder(e.getCurrentItem().getItemMeta().getDisplayName(), 0).build());
                                    CompletableFuture<Void> future = api.getUserManager().saveUser(user);
                                    future.thenRunAsync(() -> {
                                        Optional<MessagingService> messagingService = api.getMessagingService();
                                        if (messagingService.isPresent()) {
                                            messagingService.get().pushUserUpdate(user);
                                        }
                                    });
                                    PrefixUpdate(player.getUniqueId(), newJsonArray, DBCount - 1);
                                    PrefixMenu(player, Integer.parseInt(Pages[0]), false, (Player) e.getWhoClicked());
                                } catch (ParseException ex) {
                                    ex.printStackTrace();
                                }
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                        } else {
                            User user = api.getUserManager().getUser(player.getUniqueId());
                            String prefix = user.getCachedData().getMetaData().getPrefix();
                            if (prefix != null) {
                                user.data().remove(PrefixNode.builder(prefix, 0).build());
                            }
                            user.data().add(PrefixNode.builder(e.getCurrentItem().getItemMeta().getDisplayName(), 0).build());
                            CompletableFuture<Void> future = api.getUserManager().saveUser(user);
                            future.thenRunAsync(() -> {
                                Optional<MessagingService> messagingService = api.getMessagingService();
                                if (messagingService.isPresent()) {
                                    messagingService.get().pushUserUpdate(user);
                                }
                            });
                            player.sendMessage(configs.getString("message.UsePrefix").replace("%prefix%", e.getCurrentItem().getItemMeta().getDisplayName()));
                            player.closeInventory();
                        }
                    }
                }
            }
        }
        if (e.getView().getTitle().contains(configs.getString("Gui.staff.SettingPrefix"))) {
            e.setCancelled(true);
            String inputString = e.getView().getTitle();
            String pattern = configs.getString("Gui.staff.SettingNameMatcher"); // 정규 표현식 수정

            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(inputString);
            String nickname = null;
            if (m.find()) {
                nickname = m.group(1);
            } else {
                getLogger().log(Level.SEVERE, "[ 칭호 관리 ] 패턴과 일치하는 부분을 찾을 수 없습니다. (config.yml에 Gui.staff.SettingNameMatcher를 수정 해주세요.)");
            }

            Player player = Bukkit.getServer().getPlayer(nickname);
            if(e.getCurrentItem()!=null){
                String[] Pages = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(e.getClickedInventory()).getItem(49)).getItemMeta()).getDisplayName().replaceAll("§f§l\\( ","").replaceAll(" §f§l\\)","").replaceAll("§a§l","").replaceAll("§c§l","").split(" §f/ ");
                if(e.getSlot()==46||e.getSlot()==47||e.getSlot()==48||e.getSlot()==49||e.getSlot()==50||e.getSlot()==51||e.getSlot()==52){
                    return;
                }else if(Objects.requireNonNull(Objects.requireNonNull(e.getCurrentItem()).getItemMeta()).getDisplayName().contains(configs.getString("Gui.NextPage"))){
                    PrefixMenu(player,Integer.parseInt(Pages[0])+1,true,(Player) e.getWhoClicked());
                }else if(e.getCurrentItem().getItemMeta().getDisplayName().contains(configs.getString("Gui.BackPage"))){
                    PrefixMenu(player,Integer.parseInt(Pages[0])-1,true,(Player) e.getWhoClicked());
                }else{
                    if(!(e.getSlot()==53||e.getSlot()==45||e.getSlot()==46||e.getSlot()==47||e.getSlot()==48||e.getSlot()==49||e.getSlot()==50||e.getSlot()==51||e.getSlot()==52)) {
                        if(e.getCurrentItem().getType() == Material.BOOK) {
                            try {
                                JSONParser parser = new JSONParser();
                                try {
                                    ResultSet rs = PrefixSelect(player.getUniqueId());
                                    String DBPrefix = null;
                                    int DBCount = 0;
                                    while (true) {
                                        try {
                                            if (!Objects.requireNonNull(rs).next()) break;
                                            DBPrefix = rs.getString("Prefix");
                                            DBCount = rs.getInt("Count");
                                        } catch (SQLException err) {
                                            throw new RuntimeException(err);
                                        }
                                    }
                                    JSONArray jsonArray = (JSONArray) parser.parse(DBPrefix);
                                    JSONArray newJsonArray = new JSONArray();
                                    for (Object obj : jsonArray) {
                                        JSONObject jsonObject = (JSONObject) obj;
                                        String name = (String) jsonObject.get("name");
                                        if (!name.contains(e.getCurrentItem().getItemMeta().getDisplayName())) {
                                            newJsonArray.add(jsonObject);
                                        }
                                    }
                                    ((Player) e.getWhoClicked()).sendMessage(configs.getString("message.DelPrefix").replace("%prefix%", e.getCurrentItem().getItemMeta().getDisplayName()));
                                    User user = api.getUserManager().getUser(player.getUniqueId());
                                    user.data().remove(PrefixNode.builder(e.getCurrentItem().getItemMeta().getDisplayName(), 0).build());
                                    CompletableFuture<Void> future = api.getUserManager().saveUser(user);
                                    future.thenRunAsync(() -> {
                                        Optional<MessagingService> messagingService = api.getMessagingService();
                                        if (messagingService.isPresent()) {
                                            messagingService.get().pushUserUpdate(user);
                                        }
                                    });
                                    PrefixUpdate(player.getUniqueId(), newJsonArray, DBCount - 1);
                                    PrefixMenu(player, Integer.parseInt(Pages[0]), true, (Player) e.getWhoClicked());
                                } catch (ParseException ex) {
                                    ex.printStackTrace();
                                }
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                }
            }
        }
    }
    @EventHandler
    public void PrefixBookClick(PlayerInteractEvent event) {
        if(!(event.getHand() == EquipmentSlot.HAND)) return;
        Player player = event.getPlayer();
        if(player.getItemInHand().hasItemMeta()) {
            if (Objects.requireNonNull(player.getItemInHand().getItemMeta()).hasDisplayName()) {
                if (player.getItemInHand().getItemMeta().getDisplayName().startsWith(configs.getString("NamePrefix"))) {
                    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        String prefix = player.getItemInHand().getItemMeta().getDisplayName().replace(configs.getString("NamePrefix"), "");
                        String inputString = player.getItemInHand().getItemMeta().getLore().get(configs.getInt("ExIndex"));
                        String pattern = configs.getString("ExMatcher");
                        Pattern r = Pattern.compile(pattern);
                        Matcher m = r.matcher(inputString);
                        String lore = null;
                        if (m.find()) {
                            lore = m.group(1);
                        } else {
                            getLogger().log(Level.SEVERE, "[ 칭호북 ] 패턴과 일치하는 부분을 찾을 수 없습니다. (config.yml에 ExMatcher를 수정 해주세요.)");
                        }

                        ResultSet rs = PrefixSelect(player.getUniqueId());
                        String DBPrefix = null;
                        JSONArray jsonArr;
                        JSONObject obj = new JSONObject();
                        int PrefixCount = 0;
                        obj.put("name", prefix);
                        obj.put("ex", lore);
                        while (true) {
                            try {
                                if (!Objects.requireNonNull(rs).next()) break;
                                DBPrefix = rs.getString("Prefix");
                                PrefixCount = rs.getInt("Count");
                            } catch (SQLException err) {
                                throw new RuntimeException(err);
                            }
                        }
                        try {
                            jsonArr = (JSONArray) new JSONParser().parse(DBPrefix);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                        if (!jsonArr.contains(obj)) {
                            if (player.getInventory().getItemInHand().getAmount() == 1) {
                                player.getInventory().setItemInHand(new ItemStack(Material.AIR));
                            } else {
                                player.getInventory().getItemInHand().setAmount(player.getInventory().getItemInHand().getAmount()-1);
                            }
                            jsonArr.add(obj);
                            PrefixUpdate(player.getUniqueId(), jsonArr, PrefixCount + 1);
                            player.sendMessage(configs.getString("message.addPrefix").replace("%prefix%",prefix));
                        } else {
                            player.sendMessage(configs.getString("message.havePrefix").replace("%prefix%",prefix));
                        }
                    }
                }
            }
        }
    }
}

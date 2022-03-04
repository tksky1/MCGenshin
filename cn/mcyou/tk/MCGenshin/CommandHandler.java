package cn.mcyou.tk.MCGenshin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

class CommandHandler implements CommandExecutor {
    static HashMap<String, Integer> confirmMap;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equals("mcgenshin")){
            sender.sendMessage("§e==================");
            sender.sendMessage("§6MCGenshin插件 v1.0");
            sender.sendMessage("§6作者 tk_sky");
            sender.sendMessage("§6博客：tk.mcyou.cn");
            sender.sendMessage("§6感谢Mcyouyou公益群组服的支持");
            sender.sendMessage("§e==================");
        }

        if(command.getName().equals("givevision")){
            if(sender.hasPermission("mcgenshin.givevision")){
                if(args.length==0){
                    sender.sendMessage("§c格式错误！/givevision [玩家id]");
                    return true;
                }
                Random random = new Random();
                int element = random.nextInt(6)+1;
                while(element == 5) element = random.nextInt(6)+1;
                int amount = random.nextInt(51)+25;
                double cd = (double) amount/25;
                int elementPower = random.nextInt(100)+1;
                Vision newVision = new Vision(element, amount + random.nextInt(11)-5, cd+ random.nextDouble()-0.5, elementPower);
                ItemStack item = new ItemStack(Material.SUNFLOWER);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("§l§6「神之眼」");
                List<String> s = new LinkedList<>();
                s.add("§e神选者、改变世间之人的证明。抑或是愿望和梦想的具象。");
                s.add("§e不论如何，其凝聚的原初之力能赋予武器操纵元素的权能。");
                s.add("§6§l【神之眼】属性：");
                switch(element){
                    case 1-> s.add("§l§4火");
                    case 2-> s.add("§l§9水");
                    case 3-> s.add("§l§2风");
                    case 4-> s.add("§l§5雷");
                    case 6-> s.add("§l§b冰");
                }
                s.add("§b单次附着量："+amount);
                s.add("§9元素附着冷却："+cd);
                s.add("§d元素精通："+elementPower);
                meta.setLore(s);
                item.setItemMeta(meta);
                for(Player p: Bukkit.getOnlinePlayers()){
                    if(p.getName().equals(args[0])){
                        if(p.getInventory().firstEmpty()!=-1){
                            p.getInventory().addItem(item);
                            p.sendMessage("§e你的背包里悄悄出现了一枚崭新的神之眼。这意味着什么呢？");
                        }else{
                            p.getWorld().dropItem(p.getLocation(),item);
                            p.sendMessage("§e地上出现了一枚崭新的神之眼。这意味着什么呢？");
                        }
                    }
                }

            }else
                sender.sendMessage("§c你没有权限使用本命令！");
            return true;
        }

        if(command.getName().equalsIgnoreCase("usevision")){
            Player p = (Player)sender;
            if(!p.getInventory().getItemInOffHand().hasItemMeta()||p.getInventory().getItemInOffHand().getType()!= Material.SUNFLOWER||p.getInventory().getItemInOffHand().getItemMeta().getLore().size()!=7){
                p.getPlayer().sendMessage("§c请把要附魔的神之眼放在副手！");
                return true;
            }
            if(p.getPlayer().getInventory().getItemInMainHand().getType()==Material.AIR||p.getInventory().getItemInMainHand().getAmount()>1){
                p.getPlayer().sendMessage("§c请把要附魔的武器放在主手！");
                return true;
            }
            confirmMap.put(p.getName(),400);
            sender.sendMessage("§a你确定要绑定副手的神之眼到主手武器吗？");
            sender.sendMessage("§e在20s内在聊天发送“确定”确认将此神之眼绑定到主手武器！");
            return true;
        }

        return true;
    }
}
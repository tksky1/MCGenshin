package cn.mcyou.tk.MCGenshin;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class EventHandler implements Listener{

    @org.bukkit.event.EventHandler
    public void onWeatherChange(WeatherChangeEvent e){
        if(e.toWeatherState()&&e.getWorld().getName().equals("world")){
            Main.isRaining = true;
        }else{
            Main.isRaining = false;
        }
    }

    @org.bukkit.event.EventHandler
    public void onEntityAttack(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Player){
            //玩家攻击了实体或玩家

            Vision vision = Vision.get(((Player)e.getDamager()).getInventory().getItemInMainHand());
            if(vision == null) return;
            if(Main.playerCDMap.containsKey((Player)e.getDamager())){
                if(Main.playerCDMap.get((Player)e.getDamager())>0) return;
            }else{
                Main.playerCDMap.put((Player)e.getDamager(), vision.cd);
                Main.playerCDQueue.add((Player)e.getDamager());
            }
            Elements element = new Elements(e.getEntity());
            element.set(vision.element, vision.amount);
            Elements.give(e.getEntity(),element,false,true);
                    //下面要检查会否发生反应
            boolean hasFire = vision.element == 1;
            double newDamage = Checker.check(Main.entityElementsMap.get(e.getEntity()),e.getDamage(),vision.elementPower,hasFire,e.getDamager());
            if(newDamage>0) e.setDamage(newDamage);
        }else{
            //非玩家攻击了实体或玩家
            if(Main.entityElementsMap.containsKey(e.getDamager())){
                if(!Main.entityElementsMap.get(e.getDamager()).empty()){
                    Elements.give(e.getEntity(),Main.entityElementsMap.get(e.getDamager()),false,true);
                    //下面要检查会否发生反应
                    boolean hasFire = Main.entityElementsMap.get(e.getDamager()).get(1) > 0;
                    double newDamage = Checker.check(Main.entityElementsMap.get(e.getEntity()),e.getDamage(),50,hasFire,e.getDamager());
                    if(newDamage>0) e.setDamage(newDamage);
                    if(e.getDamager().getType()==EntityType.ARROW){
                        Main.activeElements.remove(Main.entityElementsMap.get(e.getDamager()));
                        Main.entityElementsMap.remove(e.getDamager());
                    }
                }
            }
        }
    }

    @org.bukkit.event.EventHandler
    public void onShoot(EntityShootBowEvent e){
        if(e.getEntityType()== EntityType.PLAYER){ //玩家射，由神之眼决定传递

            Vision vision = Vision.get(e.getBow());
            if(vision == null) return;
            if(Main.playerCDMap.containsKey((Player)e.getEntity())){
                if(Main.playerCDMap.get((Player)e.getEntity())>0) return;
            }else{
                Main.playerCDMap.put((Player)e.getEntity(), vision.cd);
                Main.playerCDQueue.add((Player)e.getEntity());
            }
            Elements element = new Elements(e.getProjectile());
            element.set(vision.element, vision.amount);
            Elements.give(e.getProjectile(),element,false,true);

        }else{ //非玩家射，根据传递原则传递元素
            if(Main.entityElementsMap.containsKey(e.getEntity())){
                Elements elements = Main.entityElementsMap.get(e.getEntity());
                Elements copy = new Elements(e.getProjectile());
                for(int i =1;i<=7;i++) copy.set(i,elements.get(i));
                Main.entityElementsMap.put(e.getProjectile(),copy);
            }
        }

    }

    @org.bukkit.event.EventHandler
    public void onFire(EntityDamageEvent e){
        if(e.getCause()== EntityDamageEvent.DamageCause.FIRE){
            Elements tmp = new Elements(e.getEntity());
            tmp.set(1,50);
            Elements.give(e.getEntity(),tmp,true,false);
        }
    }

    @org.bukkit.event.EventHandler
    public void onChat(PlayerChatEvent e){
        if(!CommandHandler.confirmMap.isEmpty()){
            if(CommandHandler.confirmMap.containsKey(e.getPlayer().getName())){
                if(e.getMessage().equals("确定")){
                    e.setCancelled(true);
                    CommandHandler.confirmMap.remove(e.getPlayer().getName());
                    if(!e.getPlayer().getInventory().getItemInOffHand().hasItemMeta()||e.getPlayer().getInventory().getItemInOffHand().getType()!= Material.SUNFLOWER||e.getPlayer().getInventory().getItemInOffHand().getItemMeta().getLore().size()!=7){
                        e.getPlayer().sendMessage("§c请把要附魔的神之眼放在副手！");
                        return;
                    }
                    if(e.getPlayer().getInventory().getItemInMainHand().getType()==Material.AIR||e.getPlayer().getInventory().getItemInMainHand().getAmount()>1){
                       e.getPlayer().sendMessage("§c请把要附魔的武器放在主手！");
                       return;
                    }
                    if(Vision.get(e.getPlayer().getInventory().getItemInMainHand())!=null){
                        e.getPlayer().sendMessage("§c该武器已经被附魔过了！你要闹哪样！");
                        return;
                    }

                    List<String> lores =  e.getPlayer().getInventory().getItemInOffHand().getItemMeta().getLore();
                    List<String> lore2;
                    if(e.getPlayer().getInventory().getItemInMainHand().getItemMeta().hasLore())
                        lore2 = e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getLore();
                    else
                        lore2 = new ArrayList<>();
                    for(int i = 2;i<=6;i++){
                        lore2.add(lores.get(i));
                    }
                    ItemMeta meta =  e.getPlayer().getInventory().getItemInMainHand().getItemMeta();
                    meta.setLore(lore2);
                    e.getPlayer().getInventory().getItemInMainHand().setItemMeta(meta);
                    e.getPlayer().getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                    e.getPlayer().sendMessage("§a成功将神之眼附魔到武器上！");
                }
            }
        }
    }


}

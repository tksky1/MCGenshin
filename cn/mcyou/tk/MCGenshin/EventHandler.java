package cn.mcyou.tk.MCGenshin;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
            int chanceNow = (new Random()).nextInt(101);
            if(chanceNow<=vision.burstChance){
                for(Entity entity:e.getEntity().getNearbyEntities(2,2,2)){
                    if(entity==e.getDamager()) continue;
                    Elements element2 = new Elements(entity);
                    element2.set(vision.element, vision.amount);
                    if(!Main.entityElementsMap.containsKey(entity)) Main.entityElementsMap.put(entity,element2);
                    Elements.give(entity,element2,false,true);
                    Checker.check(Main.entityElementsMap.get(entity));
                    entity.playEffect(EntityEffect.HURT_DROWN);
                }
                ((Player)e.getDamager()).playSound(e.getDamager().getLocation(),Sound.ENTITY_GENERIC_EXPLODE,10,29);
            }
                    //下面要检查会否发生反应
            boolean hasFire = vision.element == 1;
            double newDamage = Checker.check(Main.entityElementsMap.get(e.getEntity()),e.getDamage(),vision.elementPower,hasFire,e.getDamager());
            if(newDamage>50) newDamage = 50;
            if(newDamage>0) e.setDamage(newDamage);
            //if(newDamage>0){
            //    ((Player)e.getDamager()).sendMessage("加成后总伤害："+newDamage);
            //}else{
            //    ((Player)e.getDamager()).sendMessage("伤害："+e.getDamage());
            //}

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
                    if(!e.getPlayer().getInventory().getItemInOffHand().hasItemMeta()||e.getPlayer().getInventory().getItemInOffHand().getType()!= Material.SUNFLOWER||e.getPlayer().getInventory().getItemInOffHand().getItemMeta().getLore().size()!=8){
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
                    for(int i = 2;i<=7;i++){
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

    @org.bukkit.event.EventHandler
    public void onPlace(BlockPlaceEvent e){
        if(e.getItemInHand().getType()==Material.SUNFLOWER){
            if(e.getItemInHand().getItemMeta().hasLore()){
                List<String> lores= e.getItemInHand().getItemMeta().getLore();
                for(String s: lores){
                    if(s.contains("【神之眼】")){
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @org.bukkit.event.EventHandler
    public void onItemChange(PlayerItemHeldEvent e){
        if(e.getPlayer().getInventory().getItem(e.getNewSlot())!=null)
        if(e.getPlayer().getInventory().getItem(e.getNewSlot()).getType()==Material.SUNFLOWER){
            if(e.getPlayer().getInventory().getItem(e.getNewSlot()).getItemMeta().hasLore()){
                if(e.getPlayer().getInventory().getItem(e.getNewSlot()).getItemMeta().getLore().get(0).contains("拿在手中祈祷，或许会有好事发生")){
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"givevision "+e.getPlayer().getName());
                    e.getPlayer().getInventory().setItem(e.getNewSlot() ,new ItemStack(Material.AIR));
                }
            }
        }
    }

    @org.bukkit.event.EventHandler
    public void onJoin(PlayerJoinEvent e){
        if(!e.getPlayer().hasPlayedBefore()) Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"wkkit give new "+e.getPlayer().getName());
    }

    @org.bukkit.event.EventHandler
    public void onPlayerDeath(PlayerDeathEvent e){
        if(Main.entityElementsMap.containsKey(e.getEntity())){
            Elements elements = Main.entityElementsMap.get(e.getEntity());
            Main.activeElements.remove(elements);
            Main.entityElementsMap.remove(e.getEntity());
        }
    }


}

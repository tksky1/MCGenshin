package cn.mcyou.tk.MCGenshin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

public class Main extends JavaPlugin {
    static HashMap<Entity,Elements> entityElementsMap;
    static LinkedList<Elements> activeElements;
    static BukkitRunnable ElementNaturalCleaner;
    static BukkitRunnable NaturalElementAttachmentHandler;
    static boolean isRaining;
    static HashMap<Player,Double> playerCDMap;
    static ArrayDeque<Player> playerCDQueue;

    @Override
    public void onEnable(){
        entityElementsMap = new HashMap<>();
        activeElements = new LinkedList<>();
        isRaining = false;
        CommandHandler.confirmMap = new HashMap<>();
        getServer().getPluginManager().registerEvents(new EventHandler(), this);
        Elements.plugin = this;
        playerCDMap = new HashMap<>();
        playerCDQueue = new ArrayDeque<>();

        ElementNaturalCleaner = new BukkitRunnable() {
            @Override
            public void run() {

                if(!CommandHandler.confirmMap.isEmpty()){
                    Iterator<Map.Entry<String, Integer>> entries = CommandHandler.confirmMap.entrySet().iterator();
                    while(entries.hasNext()){
                        Map.Entry<String, Integer> entry = entries.next();
                        int m = CommandHandler.confirmMap.get(entry.getKey());
                        if(m>4) CommandHandler.confirmMap.put(entry.getKey(),m-4);
                        else entries.remove();
                    }
                }

                if(!playerCDQueue.isEmpty()){
                    ArrayDeque<Player> tmp = new ArrayDeque<>();
                    while(!playerCDQueue.isEmpty()){
                        Player player = playerCDQueue.poll();
                        double cd = playerCDMap.get(player);
                        if(cd>0) cd-=0.2;
                        if(cd<=0) {
                            playerCDMap.remove(player);
                        }else{
                            tmp.add(player);
                            playerCDMap.put(player,cd);
                        }
                    }
                    playerCDQueue = tmp;
                }

                Iterator<Elements> it = Main.activeElements.iterator();
                while(it.hasNext()){
                    Elements elements = it.next();
                    boolean empty = true;
                    for(int i=1;i<=7;i++){
                        if(elements.sub(i,1)>0){
                            empty = false;
                            if(i==6){ //冰元素特性：减速
                                LivingEntity livingEntity = (LivingEntity) elements.owner;
                                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,5,0));
                            }
                        }
                    }
                    if(elements.get(2)>0&&elements.get(4)>0){//感电
                        elements.sub(2,1);
                        elements.sub(4,1);
                        LivingEntity livingEntity = (LivingEntity) elements.owner;
                        livingEntity.damage(1);
                        elements.show( "感电");
                    }

                    if(elements.get(2)>0&&elements.get(6)>0){//冻结
                        elements.sub(2,1);
                        elements.sub(6,1);
                        LivingEntity livingEntity = (LivingEntity) elements.owner;
                        livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,5,4));
                        elements.show( "冻结");
                    }

                    if(empty){
                        it.remove();
                    }
                    elements.show();
                }

                }
        };
        ElementNaturalCleaner.runTaskTimer(this,4,4);

        NaturalElementAttachmentHandler = new BukkitRunnable() {
            @Override
            public void run() {
                //get附近的实体，用于附加环境元素
                HashMap<Entity,Boolean> checked = new HashMap<>();

                for(Player player: Bukkit.getOnlinePlayers()){
                    //先检测玩家自己
                    if(Main.isRaining){
                        //露天检测
                        if(player.getLocation().getY()-1==player.getWorld().getHighestBlockAt(player.getLocation()).getY()){
                            Elements tmp = new Elements(player);
                            tmp.set(2,50);
                            Elements.give(player,tmp,true,false);
                        }
                    }
                    if(player.getFireTicks()>0){
                        //挂火
                        Elements tmp = new Elements(player);
                        tmp.set(1,50);
                        Elements.give(player,tmp,true,false);
                    }
                    Material mat= player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
                    if(mat == Material.WATER||player.getLocation().getBlock().getType()==Material.WATER){ //水里挂水
                        Elements tmp = new Elements(player);
                        tmp.set(2,50);
                        Elements.give(player,tmp,true,false);
                    }
                    if(mat == Material.SNOW||mat==Material.SNOW_BLOCK||mat == Material.POWDER_SNOW){
                        Elements tmp = new Elements(player); //雪上挂冰
                        tmp.set(6,50);
                        Elements.give(player,tmp,true,false);
                    }

                    //下面检查附近生物
                    for(Entity entity : Objects.requireNonNull(player.getPlayer()).getNearbyEntities(30,30,30)){
                        if(checked.containsKey(entity)) continue;
                        if(Main.isRaining&&entity.isValid()){
                            //露天检测
                            if(entity.getLocation().getY()-1==entity.getWorld().getHighestBlockAt(entity.getLocation()).getY()){
                                //给雨天露天的生物挂水元素
                                Elements tmp = new Elements(entity);
                                tmp.set(2,50);
                                Elements.give(entity,tmp,true,false);
                            }
                        }
                        if(entity.getFireTicks()>0||entity.getType()== EntityType.BLAZE||entity.getType()== EntityType.MAGMA_CUBE){
                            //挂火
                            Elements tmp = new Elements(entity);
                            tmp.set(1,50);
                            Elements.give(entity,tmp,true,false);
                        }
                        mat= entity.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
                        if(mat == Material.WATER||entity.getLocation().getBlock().getType()==Material.WATER){ //水里挂水
                            Elements tmp = new Elements(entity);
                            tmp.set(2,50);
                            Elements.give(entity,tmp,true,false);
                        }
                        if(mat == Material.SNOW||mat==Material.SNOW_BLOCK||mat == Material.POWDER_SNOW){
                            Elements tmp = new Elements(entity); //雪上挂冰
                            tmp.set(6,50);
                            Elements.give(entity,tmp,true,false);
                        }
                        if(entity.getType()==EntityType.ENDERMAN||entity.getType()==EntityType.ENDERMITE){
                            Elements tmp = new Elements(entity); //末影人和末影螨挂雷
                            tmp.set(4,50);
                            Elements.give(entity,tmp,true,false);
                        }
                        checked.put(entity,true);
                    }
                }
            }
        };
        NaturalElementAttachmentHandler.runTaskTimer(this,40,10);
        this.getCommand("mcgenshin").setExecutor(new CommandHandler());
        this.getCommand("givevision").setExecutor(new CommandHandler());
        this.getCommand("useVision").setExecutor(new CommandHandler());
        getLogger().info("====================");
        getLogger().info("MCGenshin by tk_sky");
        getLogger().info("====================");
    }

    @Override
    public void onDisable(){
        getLogger().info("====================");
        getLogger().info("MCGenshin disabled");
        getLogger().info("====================");
    }


}

package cn.mcyou.tk.MCGenshin;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class Elements {
    //元素代号 1火2水3风4雷5草6冰7岩
    //§4火 §9水 §2风 §5雷 §a草 §b冰 §6岩
    public ArrayList<Integer> amount;
    public Entity owner;
    public boolean showingReaction = false;
    static Plugin plugin;
    BukkitRunnable runnable;

    Elements(Entity owner){
        amount = new ArrayList<>();
        for(int i =1;i<=8;i++){
            amount.add(0);
        }
        this.owner = owner;
    }

    void show(){ //在生物头上展示附着元素
        Entity entity = this.owner;
        if(showingReaction) return;
        String s = "";
        for(int i =1;i<=7;i++){
            if(this.get(i)>0){
                if(i==1) s+="§c火 ";
                if(i==2) s+="§9水 ";
                if(i==3) s+="§2风 ";
                if(i==4) s+="§5雷 ";
                if(i==5) s+="§a草 ";
                if(i==6) s+="§b冰 ";
                if(i==7) s+="§6岩 ";
            }
        }
        if(!s.equals("")){
            s = s.substring(0,s.length()-1);
            entity.setCustomName(s);
            entity.setCustomNameVisible(true);
            if(entity instanceof Player){
                ((Player) entity).spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("元素附着 "+s));
            }
        }else
            entity.setCustomName("");
            entity.setCustomNameVisible(false);

    }

    void show(String s){ //在生物头上展示指定文字，比如感电
        Entity entity = this.owner;
        showingReaction = true;
        entity.setCustomName(s);
        entity.setCustomNameVisible(true);
        if(entity instanceof Player){
            ((Player) entity).spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(s));
        }
         runnable = new BukkitRunnable() {
            @Override
            public void run() {
                showingReaction = false;
                entity.setCustomName("");
                entity.setCustomNameVisible(false);
            };
        };
        runnable.runTaskLater(plugin,10);
    }

    static void give(Entity entity, Elements elements, boolean setMode, Boolean unCheck){
        //供底层调用的元素附着，setMode==true表示是附着方式是覆盖而不是增加，uncheck用于玩家攻击导致附着时写true
        if(!(entity instanceof LivingEntity) && entity.getType()!= EntityType.ARROW) return;
        for(int i=1;i<=7;i++){
            if(elements.get(i)>100) elements.set(i,100);
        }
        Elements tmp = new Elements(entity);
        if(Main.entityElementsMap.containsKey(entity)){
            Elements tmp2 = Main.entityElementsMap.get(entity);
            for(int i =1;i<=7;i++){
                tmp.set(i,tmp.get(i)+tmp2.get(i));
            }
        }
        if (!setMode)
            for(int i =1;i<=7;i++){
                tmp.set(i,tmp.get(i)+elements.get(i));
            }
        if(setMode)
            for(int i =1;i<=7;i++){
                if(tmp.get(i)!=0&&elements.get(i)!=0)
                tmp.set(i,elements.get(i));
                else
                    tmp.set(i,tmp.get(i)+elements.get(i));
            }
        for(int i=1;i<=7;i++){
            if(elements.get(i)>100) elements.set(i,100);
        }
        Main.entityElementsMap.put(entity,tmp);
        Main.activeElements.add(tmp);
        if(!unCheck)
            Checker.check(tmp);
        tmp.show();
        String s="";
        for(int i = 1;i<=7;i++)
            if(tmp.get(i)>0)  s=s+i;
    }

    public int sub(int element, int amount){
        int tmp = this.amount.get(element)-amount;
        tmp =  Math.max(tmp, 0);
        this.amount.set(element,tmp);
        return tmp;
    }

    public void set(int element,int amount){
        this.amount.set(element,amount);
    }

    public int get(int element){
        return this.amount.get(element);
    }

    public boolean empty(){
        for(int i:amount){
            if(i!=0) return false;
        }

        return true;
    }

}

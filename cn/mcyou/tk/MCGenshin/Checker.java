package cn.mcyou.tk.MCGenshin;

import org.bukkit.EntityEffect;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Checker {
    static void check(Elements elements){
        //检查元素是否反应（非攻击导致）并执行，如果是非聚变反应则返回最终伤害值，否则返回0
        //涉及增加元素一定要check，不是giveElement就行了
        Main.activeElements.add(elements);
        for(int i=1;i<=7;i++){
            if(elements.get(i)>100) elements.set(i,100);
        }
        if(elements.get(4)>0){ //超载超导
            if(elements.get(1)>0){ //超载
                elements.owner.setFireTicks(0); //灭火
                elements.set(1,elements.get(1) - Math.min(elements.get(4),elements.get(1)));
                elements.set(4,elements.get(4) - Math.min(elements.get(4),elements.get(1)));
            }

            if(elements.get(4)>0&&elements.get(6)>0){ //超导
                int power = Math.min(elements.get(4),elements.get(6));
                elements.set(6,elements.get(6)-power);
                elements.set(4,elements.get(4)-power);
                LivingEntity le = (LivingEntity) elements.owner;
                le.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,100,2));
            }
        }

        if(elements.get(3)>0){ //扩散
            int wind = elements.get(3);
            elements.set(3,0);
            for(int i = 1;i<=6;i++){
                if(i==3) continue;
                if(elements.get(i)>0){
                    if(wind<50){ //弱扩散
                        elements.set(i,Math.max(elements.get(i)-50,0));
                        for(Entity entity : elements.owner.getNearbyEntities(4,4,4)){
                            Elements newElement = new Elements(entity);
                            newElement.set(i,20);
                            Elements.give(entity,newElement,false,false);
                        }
                    }else{ //强扩散
                        elements.set(i,0);
                        for(Entity entity : elements.owner.getNearbyEntities(4,4,4)){
                            Elements newElement = new Elements(entity);
                            newElement.set(i,50);
                            Elements.give(entity,newElement,false,false);
                        }
                    }
                    break;
                }
            }
        }

        if(elements.get(1)>0) { //加伤反应

            if(elements.get(2)>0){ //蒸发
                elements.owner.setFireTicks(0); //灭火
                int power = Math.min(elements.get(2),elements.get(1));
                elements.set(1,elements.get(1)-power);
                elements.set(2,elements.get(2)-power);
            }
            if(elements.get(6)>0){
                elements.owner.setFireTicks(0); //灭火
                int power = Math.min(elements.get(6),elements.get(1));
                elements.set(1,elements.get(1)-power);
                elements.set(6,elements.get(6)-power);
            }
        }
        //感电和冻结由自然消减函数实现
    }

    static double check(Elements elements, double damage, int elementPower,boolean hasFire,Entity damager){
        //检查元素是否反应（攻击导致）并执行，如果是非聚变反应则返回最终伤害值，否则返回0 hasFire为导致反应的攻击是否施加火附魔
        Main.activeElements.add(elements);
        for(int i=1;i<=7;i++){
            if(elements.get(i)>100) elements.set(i,100);
        }
        if(elements.get(4)>0){ //超载超导
            if(elements.get(1)>0){ //超载
                elements.show("超载");
                elements.owner.setFireTicks(0); //灭火
                elements.set(1,elements.get(1) - Math.min(elements.get(4),elements.get(1)));
                elements.set(4,elements.get(4) - Math.min(elements.get(4),elements.get(1)));
                if(elements.owner.getType()!= EntityType.ENDERMAN){
                    elements.owner.playEffect(EntityEffect.HURT_EXPLOSION);
                    ((LivingEntity)elements.owner).damage(4);
                    for(Entity e: elements.owner.getNearbyEntities(1,1,1)){
                        if(e.getType()!=EntityType.PLAYER){
                            LivingEntity livingEntity = (LivingEntity)e;
                            livingEntity.damage(4);
                        }else
                            ((Player)damager).playSound(elements.owner.getLocation(), Sound.ENTITY_GENERIC_EXPLODE,10,29);
                    }
                }
                //((Player)damager).sendMessage("超载伤害：4");
                //elements.owner.getWorld().createExplosion(elements.owner.getLocation(),1F,false,false,damager);
            }

            if(elements.get(4)>0&&elements.get(6)>0){ //超导
                elements.show("超导");
                elements.owner.setFireTicks(0); //灭火
                int power = Math.min(elements.get(4),elements.get(6));
                elements.set(6,elements.get(6)-power);
                elements.set(4,elements.get(4)-power);
                LivingEntity le = (LivingEntity) elements.owner;
                le.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,200,2));
                le.damage((double)power /50 *2* (double)elementPower/50 ,damager);
                //((Player)damager).sendMessage("超导伤害："+(double)power /50 *2* (double)elementPower/50);
            }
        }

        if(elements.get(3)>0){ //扩散
            elements.show("扩散");
            int wind = elements.get(3);
            elements.set(3,0);
            for(int i = 1;i<=6;i++){
                if(i==3) continue;
                if(elements.get(i)>0){
                    if(wind<50){ //弱扩散
                        elements.set(i,Math.max(elements.get(i)-50,0));
                        for(Entity entity : elements.owner.getNearbyEntities(4,4,4)){
                            if(damager.equals(entity)) continue;
                            Elements newElement = new Elements(entity);
                            newElement.set(i,20);
                            Elements.give(entity,newElement,false,false);
                        }
                    }else{ //强扩散
                        elements.set(i,0);
                        for(Entity entity : elements.owner.getNearbyEntities(4,4,4)){
                            if(damager.equals(entity)) continue;
                            Elements newElement = new Elements(entity);
                            newElement.set(i,50);
                            Elements.give(entity,newElement,false,false);
                        }
                    }
                    break;
                }
            }
            LivingEntity le = (LivingEntity) elements.owner;
            le.damage((double)wind /50 * (double)elementPower/50 ,damager);
        }

        if(elements.get(1)>0) { //加伤反应
            if(elements.get(2)>0){ //蒸发
                elements.show("蒸发");
                elements.owner.setFireTicks(0); //灭火
                int power = Math.min(elements.get(2),elements.get(1));
                elements.set(1,elements.get(1)-power);
                elements.set(2,elements.get(2)-power);
                if(hasFire)
                    return (((double)power) /(double)50 * ((double)elementPower)/(double)50 * 0.5 * damage + damage); //火打水
                else
                    return (((double)power) /(double)50 * ((double)elementPower)/(double)50 * 1 * damage + damage); //水打火

            }
            if(elements.get(6)>0){
                elements.owner.setFireTicks(0); //灭火
                elements.show("融化");
                int power = Math.min(elements.get(6),elements.get(1));
                elements.set(1,elements.get(1)-power);
                elements.set(6,elements.get(6)-power);
                if(!hasFire)
                    return ((double)power) /50 * ((double)elementPower)/50 * 0.5 * damage + damage; //冰打火
                else
                    return ((double)power) /50 * ((double)elementPower)/50 * 1 * damage + damage; //火打冰
            }
        }

        //感电和冻结由自然消减函数实现
        return 0;
    }

}

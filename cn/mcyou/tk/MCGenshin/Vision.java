package cn.mcyou.tk.MCGenshin;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

public class Vision {

    public int element;
    public int amount;
    public double cd;
    public int elementPower;

    Vision(int element, int amount, double cd, int elementPower){
        this.element = element;
        this.amount = amount;
        this.cd = cd;
        this.elementPower = elementPower;
    }

    static Vision get(ItemStack item){
        if(!item.hasItemMeta()) return null;
        if(!Objects.requireNonNull(item.getItemMeta()).hasLore()) return null;
        List<String> lores =  item.getItemMeta().getLore();
        if(lores==null) return null;
        int start = -1;
        for(String s:lores){
            if(s.contains("【神之眼】属性：")){
                start = lores.indexOf(s);
                break;
            }
        }
        if(start == -1) return null;
        Vision vision = new Vision(0,0,999,0);
        String tmp = lores.get(start+1);
        if(tmp.contains("火"))  vision.element = 1;
        if(tmp.contains("水"))  vision.element = 2;
        if(tmp.contains("风"))  vision.element = 3;
        if(tmp.contains("雷"))  vision.element = 4;
        if(tmp.contains("冰"))  vision.element = 6;
        tmp = lores.get(start+2);
        tmp = tmp.substring(8);
        vision.amount = Integer.parseInt(tmp);
        tmp = lores.get(start+3);
        tmp = tmp.substring(9);
        vision.cd = Double.parseDouble(tmp);
        tmp = lores.get(start+4);
        tmp = tmp.substring(7);
        vision.elementPower = Integer.parseInt(tmp);
        return vision;
    }
}

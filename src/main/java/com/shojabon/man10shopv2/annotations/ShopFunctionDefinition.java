package com.shojabon.man10shopv2.annotations;

import com.shojabon.man10shopv2.enums.Man10ShopPermission;
import com.shojabon.man10shopv2.enums.Man10ShopType;
import org.bukkit.Material;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ShopFunctionDefinition {
    String name() default "無名";
    Material iconMaterial() default Material.DIAMOND;
    boolean isAdminSetting() default true;
    String[] explanation() default {};
    String category() default "一般設定";
    Man10ShopType[] enabledShopType() default {};
    Man10ShopPermission allowedPermission() default Man10ShopPermission.MODERATOR;
    boolean enabled() default true;
}

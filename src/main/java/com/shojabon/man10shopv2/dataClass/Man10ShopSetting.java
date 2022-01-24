package com.shojabon.man10shopv2.dataClass;

import com.shojabon.man10shopv2.Man10ShopV2;
import com.shojabon.man10shopv2.dataClass.lootBox.LootBox;
import com.shojabon.man10shopv2.dataClass.quest.MQuest;
import com.shojabon.mcutils.Utils.BaseUtils;
import com.shojabon.mcutils.Utils.MySQL.MySQLAPI;
import com.shojabon.mcutils.Utils.MySQL.MySQLCachedResultSet;
import com.shojabon.mcutils.Utils.SConfigFile;
import com.shojabon.mcutils.Utils.SItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Man10ShopSetting <T>{

    public String settingId;
    T value;
    public final T defaultValue;
    public UUID shopId;
    public boolean ignoredOnFunctionEnabled;

    public ArrayList<Consumer<T>> onStateChangeEvents = new ArrayList<>();

    public void addOnStateChangeEvent(Consumer<T> event){
        onStateChangeEvents.add(event);
    }

    public Man10ShopSetting(String settingId, T defaultValue){
        this.defaultValue = defaultValue;
        this.settingId = settingId;
        ignoredOnFunctionEnabled = false;
    }

    public Man10ShopSetting(String settingId, T defaultValue, boolean ignoreOnFunctionEnabled){
        this.defaultValue = defaultValue;
        this.settingId = settingId;
        this.ignoredOnFunctionEnabled = ignoreOnFunctionEnabled;
    }

    private String calculateUniqueSettingsHash(String key){
        try {
            byte[] result;
            result = MessageDigest.getInstance("MD5")
                    .digest((shopId.toString() + "." + key).getBytes(StandardCharsets.UTF_8));
            return String.format("%020x", new BigInteger(1, result));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean delete(){
        value = defaultValue;
        return Man10ShopV2.mysql.execute("DELETE FROM man10shop_settings WHERE unique_setting_hash = '" + calculateUniqueSettingsHash(settingId) + "';");
    }

    public Type getType(){
        return Man10Shop.settingTypeMap.get(settingId);
    }

    public static Class<?> resolveBaseClass(Type type) {
        return type instanceof Class ? (Class<?>) type
                : type instanceof ParameterizedType ? (Class<?>) ((ParameterizedType) type).getRawType()
                : null;
    }

    public Class<T> getValueClass() {
        // noinspection unchecked
        return (Class<T>) resolveBaseClass(getType());
    }

    public boolean set(T value){
        Parser parser = Parser.getParser(getType());
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("shop_id", shopId);
        payload.put("unique_setting_hash", calculateUniqueSettingsHash(settingId));
        payload.put("key", settingId);
        payload.put("value", parser.toString(this, value));
        this.value = value;
//        if(this.value == this.defaultValue){
//            return delete();
//        }
        try{
            for(Consumer<T> event : onStateChangeEvents){
                event.accept(value);

            }
        }catch (Exception e){}

        return Man10ShopV2.mysql.execute(MySQLAPI.buildReplaceQuery(payload, "man10shop_settings"));
    }

    public T get(){
        if(value != null) return value;
        Parser parser = Parser.getParser(getType());
        if(!Man10Shop.settingValueMap.containsKey(shopId + "." + settingId)){
            value = defaultValue;
            return value;
        }
        Object object = parser.parse(this, Man10Shop.settingValueMap.get(shopId + "." + settingId));
        if(!getValueClass().isInstance(object)){
            return null;
        }
        // noinspection unchecked
        value = (T) object;

        return value;
    }

    public enum Parser {
        DOUBLE(Double.class, Double::parseDouble),
        BOOLEAN(Boolean.class, Boolean::parseBoolean),
        INTEGER(Integer.class, Integer::parseInt),
        FLOAT(Float.class, Float::parseFloat),
        LONG(Long.class, Long::parseLong),
        STRING(String.class, String::new),
        UUID(UUID.class, java.util.UUID::fromString),
        LIST() {
            public Object parse(Man10ShopSetting context, String raw) {
                Type type = ((ParameterizedType) context.getType()).getActualTypeArguments()[0];
                Parser parser = Parser.getParser(type);

                return Stream.of(raw.split(","))
                        .map(s -> parser.parse(context, s))
                        .collect(Collectors.toList());
            }

            public String toString(Man10ShopSetting context, Object value) {
                Type type = ((ParameterizedType) context.getType()).getActualTypeArguments()[0];
                Parser parser = Parser.getParser(type);

                return ((List<?>) value).stream()
                        .map(o -> parser.toString(context, o))
                        .collect(Collectors.joining(","));
            }

            public boolean accepts(Type type) {
                return List.class.isAssignableFrom(resolveBaseClass(type));
            }
        },
        ITEM_STACK() {
            public Object parse(Man10ShopSetting context, String raw) {
                return SItemStack.fromBase64(raw).build();
            }

            public String toString(Man10ShopSetting context, Object value) {
                return new SItemStack(((ItemStack) value)).getBase64();
            }

            public boolean accepts(Type type) {
                return ItemStack.class.isAssignableFrom(resolveBaseClass(type));
            }
        },
        YAML_CONFIG() {
            public Object parse(Man10ShopSetting context, String raw) {
                return SConfigFile.loadConfigFromBase64(raw);
            }

            public String toString(Man10ShopSetting context, Object value) {
                return SConfigFile.base64EncodeConfig((YamlConfiguration) value);
            }

            public boolean accepts(Type type) {
                return YamlConfiguration.class.isAssignableFrom(resolveBaseClass(type));
            }
        },
        LOOT_BOX() {
            public Object parse(Man10ShopSetting context, String raw) {
                LootBox box = new LootBox();
                box.loadLootBox(SConfigFile.loadConfigFromBase64(raw));
                return box;
            }

            public String toString(Man10ShopSetting context, Object value) {
                return SConfigFile.base64EncodeConfig(((LootBox) value).exportLootBox());
            }

            public boolean accepts(Type type) {
                return LootBox.class.isAssignableFrom(resolveBaseClass(type));
            }
        },
        QUEST() {
            public Object parse(Man10ShopSetting context, String raw) {
                MQuest box = new MQuest();
                box.loadLootBox(SConfigFile.loadConfigFromBase64(raw));
                return box;
            }

            public String toString(Man10ShopSetting context, Object value) {
                return SConfigFile.base64EncodeConfig(((MQuest) value).exportQuest());
            }

            public boolean accepts(Type type) {
                return MQuest.class.isAssignableFrom(resolveBaseClass(type));
            }
        },
        MAPPING() {
            public Object parse(Man10ShopSetting context, String raw) {
                Type keyType = ((ParameterizedType) context.getType()).getActualTypeArguments()[0];
                Type valueType = ((ParameterizedType) context.getType()).getActualTypeArguments()[1];
                Parser keyParser = Parser.getParser(keyType);
                Parser valueParser = Parser.getParser(valueType);

                return Stream.of(raw.split(",(?=[^,]*->)"))
                        .map(s -> s.split("->"))
                        .collect(Collectors.toMap(s -> keyParser.parse(context, s[0]), s -> valueParser.parse(context, s[1])));
            }

            public String toString(Man10ShopSetting context, Object value) {
                Type keyType = ((ParameterizedType) context.getType()).getActualTypeArguments()[0];
                Type valueType = ((ParameterizedType) context.getType()).getActualTypeArguments()[1];
                Parser keyParser = Parser.getParser(keyType);
                Parser valueParser = Parser.getParser(valueType);

                return ((Map<?,?>) value).entrySet().stream()
                        .map(o -> keyParser.toString(context, o.getKey()) + "->" + valueParser.toString(context, o.getValue()))
                        .collect(Collectors.joining(","));
            }

            public boolean accepts(Type type) {
                return Map.class.isAssignableFrom(resolveBaseClass(type));
            }
        };


        private final Class<?> cla$$;
        private final Function<String, Object> parser;
        private final Function<Object, String> toString;

        Parser() {
            this.cla$$ = null;
            this.parser = null;
            this.toString = null;
        }

        <T> Parser(Class<T> cla$$, Function<String, T> parser) {
            this(cla$$, parser, Object::toString);
        }

        <T> Parser(Class<T> cla$$, Function<String, T> parser, Function<T, String> toString) {
            this.cla$$ = cla$$;
            this.parser = parser::apply;
            this.toString = x -> toString.apply((T) x);
        }

        public Object parse(Man10ShopSetting setting, String raw) {
            Object parsed = this.parser.apply(raw);
            Objects.requireNonNull(parsed);
            return parsed;
        }

        public String toString(Man10ShopSetting<?> context, Object value) {
            return this.toString.apply(value);
        }

        public boolean accepts(Type type) {
            return type instanceof Class && this.cla$$.isAssignableFrom((Class) type);
        }

        public static Parser getParser(Type type) {
            try{
                return Stream.of(values())
                        .filter(parser -> parser.accepts(type))
                        .findFirst().orElse(null);
            }catch (Exception e){
                System.out.println(e + " " + type.getTypeName());
            }
            return null;
        }
    }
}

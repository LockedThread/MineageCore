package rip.simpleness.mineagecore.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;

import java.util.HashMap;
import java.util.Map;

public final class NMSUtils {

    private NMSUtils() {
    }

    public static org.bukkit.inventory.ItemStack attachItemTag(org.bukkit.inventory.ItemStack stack, String name, Object value) {
        if (stack == null) {
            return null;
        }

        try {
            ItemStack nmsStack;
            boolean alternative = false;
            try {
                nmsStack = (ItemStack) ReflectionUtils.getValue(stack, CraftItemStack.class, true, "handle");
            } catch (IllegalArgumentException ex) {
                nmsStack = CraftItemStack.asNMSCopy(stack);
                alternative = true;
            }

            if (nmsStack != null) {
                NBTTagCompound nbtTag = nmsStack.hasTag() ? nmsStack.getTag() : new NBTTagCompound();

                if (value instanceof Integer) {
                    nbtTag.setInt(name, (int) value);
                } else if (value instanceof String) {
                    nbtTag.setString(name, (String) value);
                } else if (value instanceof Byte) {
                    nbtTag.setByte(name, (byte) value);
                } else if (value instanceof Boolean) {
                    nbtTag.setBoolean(name, (Boolean) value);
                } else if (value instanceof Long) {
                    nbtTag.setLong(name, (Long) value);
                } else {
                    throw new UnsupportedOperationException("Unsupported argument type! " + value);
                }

                nmsStack.setTag(nbtTag);
                if (alternative) {
                    return CraftItemStack.asCraftMirror(nmsStack);
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        return stack;
    }

    public static org.bukkit.inventory.ItemStack attachItemTags(org.bukkit.inventory.ItemStack stack, Map.Entry<String, ?>[] dataPairs) {
        if (stack == null) {
            return null;
        }

        try {
            ItemStack nmsStack;
            boolean alternative = false;
            try {
                nmsStack = (ItemStack) ReflectionUtils.getValue(stack, CraftItemStack.class, true, "handle");
            } catch (IllegalArgumentException ex) {
                nmsStack = CraftItemStack.asNMSCopy(stack);
                alternative = true;
            }

            if (nmsStack != null) {
                NBTTagCompound nbtTag = nmsStack.hasTag() ? nmsStack.getTag() : new NBTTagCompound();

                for (Map.Entry<String, ?> entry : dataPairs) {
                    String name = entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof Integer) {
                        nbtTag.setInt(name, (int) value);
                    } else if (value instanceof String) {
                        nbtTag.setString(name, (String) value);
                    } else if (value instanceof Byte) {
                        nbtTag.setByte(name, (byte) value);
                    } else if (value instanceof Boolean) {
                        nbtTag.setBoolean(name, (Boolean) value);
                    } else if (value instanceof Long) {
                        nbtTag.setLong(name, (Long) value);
                    } else {
                        throw new UnsupportedOperationException("Unsupported argument type!");
                    }
                }

                nmsStack.setTag(nbtTag);
                if (alternative) {
                    return CraftItemStack.asCraftMirror(nmsStack);
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        return stack;
    }

    public static boolean hasItemTag(org.bukkit.inventory.ItemStack stack, String tagName) {
        if (stack == null) {
            return false;
        }

        try {
            ItemStack nmsStack;
            try {
                nmsStack = (ItemStack) ReflectionUtils.getValue(stack, CraftItemStack.class, true, "handle");
            } catch (IllegalArgumentException ex) {
                nmsStack = CraftItemStack.asNMSCopy(stack);
            }
            if (nmsStack != null && nmsStack.hasTag()) {
                NBTTagCompound nbtTag = nmsStack.getTag();
                return nbtTag.hasKey(tagName);
            }
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static int getItemTagInt(org.bukkit.inventory.ItemStack stack, String tagName, int def) {
        if (stack == null) {
            return def;
        }

        try {
            ItemStack nmsStack;
            try {
                nmsStack = (ItemStack) ReflectionUtils.getValue(stack, CraftItemStack.class, true, "handle");
            } catch (IllegalArgumentException ex) {
                nmsStack = CraftItemStack.asNMSCopy(stack);
            }
            if (nmsStack != null && nmsStack.hasTag()) {
                NBTTagCompound nbtTag = nmsStack.getTag();
                return nbtTag.hasKey(tagName) ? nbtTag.getInt(tagName) : def;
            }
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        return def;
    }

    public static long getItemTagLong(org.bukkit.inventory.ItemStack stack, String tagName, long def) {
        if (stack == null) {
            return def;
        }

        try {
            ItemStack nmsStack;
            try {
                nmsStack = (ItemStack) ReflectionUtils.getValue(stack, CraftItemStack.class, true, "handle");
            } catch (IllegalArgumentException ex) {
                nmsStack = CraftItemStack.asNMSCopy(stack);
            }
            if (nmsStack != null && nmsStack.hasTag()) {
                NBTTagCompound nbtTag = nmsStack.getTag();
                return nbtTag.hasKey(tagName) ? nbtTag.getLong(tagName) : def;
            }
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        return def;
    }

    public static String getItemTagString(org.bukkit.inventory.ItemStack stack, String tagName, String def) {
        if (stack == null) {
            return def;
        }

        try {
            ItemStack nmsStack;
            try {
                nmsStack = (ItemStack) ReflectionUtils.getValue(stack, CraftItemStack.class, true, "handle");
            } catch (IllegalArgumentException ex) {
                nmsStack = CraftItemStack.asNMSCopy(stack);
            }
            if (nmsStack != null && nmsStack.hasTag()) {
                NBTTagCompound nbtTag = nmsStack.getTag();
                return nbtTag.hasKey(tagName) ? nbtTag.getString(tagName) : def;
            }
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        return def;
    }

    public static byte getItemTagByte(org.bukkit.inventory.ItemStack stack, String tagName, byte def) {
        if (stack == null) {
            return def;
        }

        try {
            ItemStack nmsStack;
            try {
                nmsStack = (ItemStack) ReflectionUtils.getValue(stack, CraftItemStack.class, true, "handle");
            } catch (IllegalArgumentException ex) {
                nmsStack = CraftItemStack.asNMSCopy(stack);
            }
            if (nmsStack != null && nmsStack.hasTag()) {
                NBTTagCompound nbtTag = nmsStack.getTag();
                return nbtTag.hasKey(tagName) ? nbtTag.getByte(tagName) : def;
            }
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        return def;
    }

    public static boolean getItemTagBoolean(org.bukkit.inventory.ItemStack stack, String tagName, boolean def) {
        if (stack == null) {
            return def;
        }

        try {
            ItemStack nmsStack;
            try {
                nmsStack = (ItemStack) ReflectionUtils.getValue(stack, CraftItemStack.class, true, "handle");
            } catch (IllegalArgumentException ex) {
                nmsStack = CraftItemStack.asNMSCopy(stack);
            }
            if (nmsStack != null && nmsStack.hasTag()) {
                NBTTagCompound nbtTag = nmsStack.getTag();
                return nbtTag.hasKey(tagName) ? nbtTag.getBoolean(tagName) : def;
            }
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        return def;
    }

    public static Map<String, Object> getAllItemTags(org.bukkit.inventory.ItemStack stack) {
        Map<String, Object> map = new HashMap<>();
        if (stack == null) {
            return map;
        }

        try {
            ItemStack nmsStack;
            try {
                nmsStack = (ItemStack) ReflectionUtils.getValue(stack, CraftItemStack.class, true, "handle");
            } catch (IllegalArgumentException ex) {
                nmsStack = CraftItemStack.asNMSCopy(stack);
            }
            if (nmsStack != null && nmsStack.hasTag()) {
                NBTTagCompound nbtTag = (NBTTagCompound) nmsStack.getTag().clone();
                nbtTag.remove("display"); // Not needed
                nbtTag.remove("ench"); // Unparsable array
                nbtTag.remove("SkullOwner"); // Unparsable array

                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(nbtTag.toString());
                JsonObject object = element.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase("display")) continue;
                    map.put(entry.getKey(), entry.getValue().toString());
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            ex.printStackTrace();
        }

        return map;
    }

    public static org.bukkit.inventory.ItemStack setMaxStackSize(org.bukkit.inventory.ItemStack stack, int maxStackSize) {
        try {
            ItemStack nmsStack;
            try {
                nmsStack = (ItemStack) ReflectionUtils.getValue(stack, CraftItemStack.class, true, "handle");
            } catch (IllegalArgumentException e) {
                nmsStack = CraftItemStack.asNMSCopy(stack);
            }
            if (nmsStack != null) {
                nmsStack.getItem().c(maxStackSize);
                return CraftItemStack.asBukkitCopy(nmsStack);
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }
}

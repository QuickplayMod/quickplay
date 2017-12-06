package co.bugg.quickplay.reflection;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtilities {

    private ReflectionUtilities() {throw new AssertionError();}

    public static Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
        Field field = clazz.getField(name);
        field.setAccessible(true);
        return field;
    }

    public static Method getMethod(Class<?> clazz, String name) throws NoSuchMethodException {
        Method method = clazz.getMethod(name);
        method.setAccessible(true);
        return method;
    }

    public static String getMCVersion() throws NoSuchFieldException, IllegalAccessException {
        return (String) getField(ForgeVersion.class, "mcVersion").get(null);
    }

    public static String getForgeVersion() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        return (String) getMethod(ForgeVersion.class, "getVersion").invoke(null);
    }
}

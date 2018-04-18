package co.bugg.quickplay.util;

import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility class for runtime reflection
 */
public class ReflectionUtil {

    private ReflectionUtil() {throw new AssertionError();}

    /**
     * Get field "name" from class "clazz" and
     *  handle any universal modifications to it
     * @param clazz Class to get field from
     * @param name Name of field to get
     * @return The field
     * @throws NoSuchFieldException The field doesn't exist
     */
    public static Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
        Field field = clazz.getField(name);
        field.setAccessible(true);
        return field;
    }

    /**
     * Get method "name" from class "clazz" and
     * handle any universal modifications to it
     * @param clazz Clazz to get the method from
     * @param name Name of the method to get
     * @return The method
     * @throws NoSuchMethodException The method doesn't exist
     */
    public static Method getMethod(Class<?> clazz, String name) throws NoSuchMethodException {
        Method method = clazz.getMethod(name);
        method.setAccessible(true);
        return method;
    }

    /**
     * Get the current Minecraft version
     * @return String containing the Minecraft version
     * @throws IllegalAccessException Couldn't access the minecraft version for some reason
     */
    public static String getMCVersion() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        return (String) getMethod(Minecraft.class, "getVersion").invoke(Minecraft.getMinecraft());
    }

}

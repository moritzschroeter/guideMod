package mors.museumguide.tools;

import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class signCache  {
    
    private static final Map<BlockPos, String> cachedSigns = new ConcurrentHashMap<>();
    public static void addSign(BlockPos pos, String text)   {
        cachedSigns.put(pos, text);
    }

    public static void removeSign(BlockPos pos) {
        cachedSigns.remove(pos);
    }

    public static Map<BlockPos, String> getSigns()   {
        return cachedSigns;
    }

    public static int getSize() {
        return cachedSigns.size();
    }
}

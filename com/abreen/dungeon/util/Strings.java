package com.abreen.dungeon.util;

public class Strings {
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    
    public static <T> String join(T[] arr, String sep) {
        StringBuilder buf = new StringBuilder(DEFAULT_BUFFER_SIZE);
        
        int i = 0;
        for (T t : arr) {
            if (i == arr.length - 1) {
                buf.append(t);
            } else {
                buf.append(t + sep);  
            }
            
            i++;
        }
        
        return buf.toString();
    }
    
    public static <T> String repeat(T thing, int times) {
        String s = thing.toString();
        StringBuilder buf = new StringBuilder(s.length() * times);
        
        for (int i = 0; i < times; i++)
            buf.append(s);
        
        return buf.toString();
    }
}

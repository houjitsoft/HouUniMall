package com.houjiang.commons.util;

import java.util.UUID;

/**
 * @author Amos
 */
public class UUIDUtils {

	 public static String getUUID(){
         return UUID.randomUUID().toString().replace("-", "");
    }
}

package com.gucci.user_service.user.service.util;

import java.util.Random;

public class CodeGenerator {
    public static String generateCode() {
        return String.valueOf(new Random().nextInt(900000) + 100000); // 6-digit number
    }
}
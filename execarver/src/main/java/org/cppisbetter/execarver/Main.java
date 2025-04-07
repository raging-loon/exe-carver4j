package org.cppisbetter.execarver;

import org.cppisbetter.execarver.struct.AssocMap;
import org.cppisbetter.execarver.struct.Struct;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try {
            byte[] array = Files.readAllBytes(Paths.get("C:\\Windows\\Notepad.exe"));
            AssocMap map = Struct.unpack("vmagic/x58/Vlfanew", array);
            System.out.printf("%02x\n", map.getUINT16("magic"));
            System.out.printf("%02x\n", map.getInt("lfanew"));
        } catch(Exception e) {
            e.printStackTrace();
        }


    }
}

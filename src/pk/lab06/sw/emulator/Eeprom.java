package pk.lab06.sw.emulator;

import java.util.Arrays;

/**
 * Provides basic EEPROM functionality like set/get data. Do <b>not</b>
 * store data between emulator sessions.
 */
public class Eeprom {
    private final byte[] memory;

    public Eeprom(int size){
        memory = new byte[size];
    }

    /**
     * Returns byte at specific index.
     * @param idx index
     * @return byte at index
     */
    public int get(int idx){
        return memory[idx];
    }

    /**
     * Sets byte at specific index.
     * @param idx index
     * @param val byte to set
     */
    public void set(int idx, byte val){
        memory[idx] = val;
    }

    /**
     * Clears whole memory (sets to 0).
     */
    public void clear(){
        Arrays.fill(memory, (byte)0);
    }

    /**
     * Returns copy of memory
     * @return copy of memory
     */
    public byte[] getMemoryCopy(){
        return Arrays.copyOf(memory, memory.length);
    }

    /**
     * Returns whole memory as array.
     * @return original byte array
     */
    byte[] getMemory(){
        return memory;
    }
}

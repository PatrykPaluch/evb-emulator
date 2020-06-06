package pk.lab06.sw;

public class DigitalPinPanel {
    private byte value;

    public DigitalPinPanel(){
        this((byte)0);
    }
    public DigitalPinPanel(byte initValue){
        this.value = initValue;
    }


    public int getValue(){
        return value;
    }

    public DigitalPinPanel set(){
        this.value = (byte) 0xff;
        return this;
    }
    public DigitalPinPanel clear(){
        this.value = (byte)0;
        return this;
    }

    public DigitalPinPanel not(){
        this.value = (byte) ~this.value;
        return this;
    }

    public DigitalPinPanel valueSet(byte value){
        this.value = value;
        return this;
    }

    public DigitalPinPanel bitSet(int bit){
        this.value |= (1<<bit);
        return this;
    }

    public DigitalPinPanel bitClear(int bit){
        this.value &= ~(1<<bit);
        return this;
    }

    public DigitalPinPanel bitNot(int bit){

        return this;
    }



}

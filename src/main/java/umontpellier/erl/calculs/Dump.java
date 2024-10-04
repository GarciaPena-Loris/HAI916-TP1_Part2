package umontpellier.erl.calculs;

public class Dump {
    // Attributes
    private int attribute1;
    private String attribute2;
    private double attribute3;
    private boolean attribute4;
    private char attribute5;

    // Constructor
    public Dump(int attribute1, String attribute2, double attribute3, boolean attribute4, char attribute5) {
        this.attribute1 = attribute1;
        this.attribute2 = attribute2;
        this.attribute3 = attribute3;
        this.attribute4 = attribute4;
        this.attribute5 = attribute5;
    }

    // Methods
    public int getAttribute1() {
        return attribute1;
    }

    public void setAttribute1(int attribute1) {
        this.attribute1 = attribute1;
    }

    public String getAttribute2() {
        return attribute2;
    }

    public void setAttribute2(String attribute2) {
        this.attribute2 = attribute2;
    }

    public double getAttribute3() {
        return attribute3;
    }

    public void setAttribute3(double attribute3) {
        this.attribute3 = attribute3;
    }

    public boolean isAttribute4() {
        return attribute4;
    }

    public void setAttribute4(boolean attribute4) {
        this.attribute4 = attribute4;
    }

    public char getAttribute5() {
        return attribute5;
    }

    public void setAttribute5(char attribute5) {
        this.attribute5 = attribute5;
    }

    public void printAttributes() {
        System.out.println("Attribute1: " + attribute1);
        System.out.println("Attribute2: " + attribute2);
        System.out.println("Attribute3: " + attribute3);
        System.out.println("Attribute4: " + attribute4);
        System.out.println("Attribute5: " + attribute5);
    }

    public int calculateSum() {
        return attribute1 + (int) attribute3;
    }

    public boolean isAttribute1GreaterThanAttribute3() {
        return attribute1 > attribute3;
    }

    // Inner class
    public static class InnerDump {
        private int innerAttribute1;
        private String innerAttribute2;

        public InnerDump(int innerAttribute1, String innerAttribute2) {
            this.innerAttribute1 = innerAttribute1;
            this.innerAttribute2 = innerAttribute2;
        }

        public int getInnerAttribute1() {
            return innerAttribute1;
        }

        public void setInnerAttribute1(int innerAttribute1) {
            this.innerAttribute1 = innerAttribute1;
        }

        public String getInnerAttribute2() {
            return innerAttribute2;
        }

        public void setInnerAttribute2(String innerAttribute2) {
            this.innerAttribute2 = innerAttribute2;
        }

        public void printInnerAttributes() {
            System.out.println("InnerAttribute1: " + innerAttribute1);
            System.out.println("InnerAttribute2: " + innerAttribute2);
        }
    }
}
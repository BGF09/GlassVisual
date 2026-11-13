package dev.glassvisual;

public final class Module {
    private final String name;

    public Module(String name) {
        this.name = name == null ? "Module" : name;
    }

    public String getName() {
        return name;
    }
}

package arkadarktime.interfaces.modules;

public interface ModuleTicker extends ModuleListener {
    void start();

    void stop();

    void update();
}
